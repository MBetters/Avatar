package org.cubrc.avatar.avatarrdfservice;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ReadWrite;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.shared.impl.PrefixMappingImpl;
import com.hp.hpl.jena.tdb.TDBFactory;
import com.hp.hpl.jena.update.UpdateAction;
import com.hp.hpl.jena.update.UpdateFactory;
import com.hp.hpl.jena.update.UpdateRequest;

import org.apache.jena.riot.RDFDataMgr;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.cubrc.avatar.avatarrdfservice.Constants.*;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * helper methods.
 */
public class RDFQueryService extends IntentService {

    private static final String DIRECTORY = android.os.Environment.getExternalStorageDirectory() + "/Android/data/org.cubrc.avatar";
    private static final String STORE = DIRECTORY + "/StoreManager/AvatarStore";
    private static final String ONT_FOLDER = DIRECTORY + "/Ont";
    private static final String SCHEMA_MODEL = "SCHEMA_MODEL";
    private static final PrefixMapping prefixes;
    private static final int RESULT_LIMIT = 100;
    private static final Pattern prefixPattern = Pattern.compile("^([^:]*):");

    private static Dataset dataset;

//    private static OntModel ontModel;

    static {
        prefixes = new PrefixMappingImpl();
        prefixes.withDefaultMappings(PrefixMapping.Standard);
        prefixes.setNsPrefix("artifact", "http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#");
        prefixes.setNsPrefix("quality", "http://www.cubrc.org/ontologies/KDD/Mid/QualityOntology#");
        prefixes.setNsPrefix("event", "http://www.cubrc.org/ontologies/KDD/Mid/EventOntology#");
        prefixes.setNsPrefix("ero", "http://www.cubrc.org/ontologies/KDD/Upper/ExtendedRelationOntology#");
        prefixes.setNsPrefix("agent", "http://www.cubrc.org/ontologies/KDD/Mid/AgentOntology#");
        prefixes.setNsPrefix("geo", "http://www.cubrc.org/ontologies/KDD/Mid/GeospatialOntology#");
        prefixes.setNsPrefix("info", "http://www.cubrc.org/ontologies/KDD/Mid/InformationEntityOntology#");
        prefixes.setNsPrefix("agentinfo", "http://www.cubrc.org/ontologies/KDD/Domain/AgentInformationOntology#");
        prefixes.setNsPrefix("ro", "http://www.obofoundry.org/ro/ro.owl#");
        prefixes.setNsPrefix("avatardom", "http://www.cubrc.org/cmif/ontologies/AvatarDomainOntology#");
        prefixes.setNsPrefix("avatar", "http://www.cubrc.org/Avatar#");
    }

    public RDFQueryService() {
        super("RDFQueryService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        initialize();
    }

    protected void initialize() {
//        OntModelSpec ontModelSpec = OntModelSpec.OWL_MEM;
//        ontModelSpec.setReasoner(ReasonerRegistry.getOWLReasoner());
        File storeDir = new File(STORE);
        storeDir.mkdirs();
        dataset = TDBFactory.createDataset(STORE);
        dataset.begin(ReadWrite.WRITE);
        try {
            Model schemaModel = dataset.getNamedModel(SCHEMA_MODEL);
            if (schemaModel == null || schemaModel.isEmpty()) {
                Log.d(LOG_TAG, "Schema Model is empty, loading ontologies");
                File folder = new File(ONT_FOLDER);
                folder.mkdirs();
                if(schemaModel == null){
                    schemaModel = ModelFactory.createDefaultModel();
                }
                for (File file : folder.listFiles()) {
                    try {
                        Log.d(LOG_TAG, "Loading ontology " + file.getName());
                        RDFDataMgr.read(schemaModel, file.getPath());
                    } catch (Exception e) {
                        Log.e(LOG_TAG, "Error loading ontology " + file.getName(), e);
                    }
                }
            } else {
                Log.d(LOG_TAG, "Schema Model not empty, skipping ontology load");
            }
            dataset.commit();
        } finally {
            dataset.end();
        }
    }

    @Override
    protected void onHandleIntent(Intent workIntent) {
        if (workIntent != null) {
            final String action = workIntent.getAction();
            String requestId = workIntent.getStringExtra(EXTENDED_DATA_REQUEST_ID);
            Log.d(LOG_TAG, "Received Intent [" + action + "]"+requestId);
            Intent localIntent = null;
            switch (action) {
                case ACTION_SELECT:
                    localIntent = handleQuery(workIntent);
                    break;
                case ACTION_UPDATE:
                    localIntent = handleUpdate(workIntent);
                    break;
                case ACTION_CONSTRUCT:
                    localIntent = handleConstruct(workIntent);
                    break;
                case ACTION_ASK:
                    localIntent = handleAsk(workIntent);
                    break;
                case ACTION_SCAN:
                    localIntent = handleScan(workIntent);
                    break;
                case ACTION_TRIPLES:
                    localIntent = handleTriples(workIntent);
                    break;
                case ACTION_PURGE:
                    localIntent = handlePurge(workIntent);
                    break;
                default:
                    Log.d(LOG_TAG, "Unknown RDFQueryService Action: '" + action + "'");
                    break;
            }

            if (localIntent != null) {
                if(requestId != null){
                    //carry over requestId to response object
                    localIntent.putExtra(EXTENDED_DATA_REQUEST_ID, requestId);
                }
                sendBroadcast(localIntent);
            } else {
                Log.e(LOG_TAG, "Error handling Intent [" + action + "]" + requestId);
            }
        }else{
            Log.e(LOG_TAG, "onHandleIntent called with null intent");
        }
    }

    protected Intent handleScan(Intent workIntent) {
        ArrayList<ArrayList<String>> response = new ArrayList<>();
        dataset.begin(ReadWrite.READ);
        try{
            Model model = dataset.getDefaultModel();
            StmtIterator iter = model.listStatements();
            while (iter.hasNext() && response.size() <= RESULT_LIMIT) {
                ArrayList<String> row = new ArrayList<>();
                Statement stmt = iter.nextStatement();
                Triple data = stmt.asTriple();
                row.add(data.getSubject().toString());
                row.add(data.getPredicate().toString());
                row.add(data.getObject().toString());
                response.add(row);
            }
        } finally {
            dataset.end();
        }

        Intent localIntent = new Intent(ACTION_BROADCAST)
                .putExtra(EXTENDED_DATA_STATUS, STATUS_DONE)
                .putExtra(EXTENDED_DATA_TYPE, ACTION_SCAN)
                .putExtra(EXTENDED_DATA_RESPONSE, response);
        return localIntent;
    }

    protected Intent handleQuery(Intent workIntent) {
        Intent localIntent;
        ArrayList<ArrayList<String>> list = new ArrayList<>();
        String queryString = workIntent.getStringExtra(ACTION_SELECT);
        dataset.begin(ReadWrite.READ);
        try {
//            Model targetModel = dataset.getDefaultModel();
            Model targetModel = ModelFactory.createUnion(dataset.getDefaultModel(),dataset.getNamedModel(SCHEMA_MODEL));
            try (QueryExecution qExec = QueryExecutionFactory.create(queryString, targetModel)) {
                ResultSet itr = qExec.execSelect();
                ArrayList<String> columns = new ArrayList<>(itr.getResultVars());//column headers
                list.add(columns);
                while (itr.hasNext() && list.size() <= RESULT_LIMIT) {
                    QuerySolution sol = itr.next();
                    ArrayList<String> row = new ArrayList<>();
                    for (String key : columns) {
                        RDFNode node = sol.get(key);
                        Node uriNode = node.asNode();
                        row.add(uriNode.toString(prefixes));
                    }
                    list.add(row);
                }
            }
            Log.d(LOG_TAG, "Query completed, sending response");
            localIntent = new Intent(ACTION_BROADCAST)
                    .putExtra(EXTENDED_DATA_STATUS, STATUS_DONE)
                    .putExtra(EXTENDED_DATA_TYPE, ACTION_SELECT)
                    .putExtra(EXTENDED_DATA_RESPONSE, list);
        }catch(Exception e){
            Log.e("AvatarRdf", "Error executing query: '"+queryString+"'", e);
            localIntent = new Intent(ACTION_BROADCAST)
                    .putExtra(EXTENDED_DATA_STATUS, STATUS_FAIL)
                    .putExtra(EXTENDED_DATA_TYPE, ACTION_SELECT)
                    .putExtra(EXTENDED_DATA_RESPONSE, "Error parsing query");
        }finally{
            dataset.end();
        }
        return localIntent;
    }

    protected Intent handleUpdate(Intent workIntent) {
        Intent localIntent;
        String updateString = workIntent.getStringExtra(ACTION_UPDATE);
        dataset.begin(ReadWrite.WRITE);
        try {
            UpdateAction.parseExecute(updateString, dataset);
            dataset.commit();
            localIntent = new Intent(ACTION_BROADCAST)
                    .putExtra(EXTENDED_DATA_STATUS, STATUS_DONE)
                    .putExtra(EXTENDED_DATA_TYPE, ACTION_UPDATE)
                    .putExtra(EXTENDED_DATA_RESPONSE, "Update executed");
        }catch(Exception e){
            Log.e("AvatarRdf", "Error executing update: '"+updateString+"'", e);
            localIntent = new Intent(ACTION_BROADCAST)
                    .putExtra(EXTENDED_DATA_STATUS, STATUS_FAIL)
                    .putExtra(EXTENDED_DATA_TYPE, ACTION_UPDATE)
                    .putExtra(EXTENDED_DATA_RESPONSE, "Error parsing update");
        }finally{
            dataset.end();
        }
        return localIntent;
    }

    protected Intent handleConstruct(Intent workIntent) {
        Intent localIntent;
        ArrayList<ArrayList<String>> list = new ArrayList<>();
        String queryString = workIntent.getStringExtra(ACTION_CONSTRUCT);
        dataset.begin(ReadWrite.READ);
        try {
//            Model targetModel = dataset.getDefaultModel();
            Model targetModel = ModelFactory.createUnion(dataset.getDefaultModel(),dataset.getNamedModel(SCHEMA_MODEL));
            try (QueryExecution qExec = QueryExecutionFactory.create(queryString, targetModel)) {
                Model results = qExec.execConstruct();
                StmtIterator iter = results.listStatements();
                while (iter.hasNext() && list.size() <= RESULT_LIMIT) {
                    ArrayList<String> row = new ArrayList<>();
                    Statement stmt = iter.nextStatement();
                    Triple data = stmt.asTriple();
                    row.add(data.getSubject().toString());
                    row.add(data.getPredicate().toString());
                    row.add(data.getObject().toString());
                    list.add(row);
                }
            }
            Log.d(LOG_TAG, "Construct completed, sending response");
            localIntent = new Intent(ACTION_BROADCAST)
                    .putExtra(EXTENDED_DATA_STATUS, STATUS_DONE)
                    .putExtra(EXTENDED_DATA_TYPE, ACTION_CONSTRUCT)
                    .putExtra(EXTENDED_DATA_RESPONSE, list);
        }catch(Exception e){
            Log.e("AvatarRdf", "Error executing query: '"+queryString+"'", e);
            localIntent = new Intent(ACTION_BROADCAST)
                    .putExtra(EXTENDED_DATA_STATUS, STATUS_FAIL)
                    .putExtra(EXTENDED_DATA_TYPE, ACTION_CONSTRUCT)
                    .putExtra(EXTENDED_DATA_RESPONSE, "Error parsing construct");
        }finally{
            dataset.end();
        }
        return localIntent;
    }

    protected Intent handleAsk(Intent workIntent) {
        Intent localIntent;
        String queryString = workIntent.getStringExtra(ACTION_ASK);
        boolean result;
        dataset.begin(ReadWrite.READ);
        try {
//            Model targetModel = dataset.getDefaultModel();
            Model targetModel = ModelFactory.createUnion(dataset.getDefaultModel(),dataset.getNamedModel(SCHEMA_MODEL));
            try (QueryExecution qExec = QueryExecutionFactory.create(queryString, targetModel)) {
                result = qExec.execAsk();
            }
            Log.d(LOG_TAG, "Ask completed, sending response");
            localIntent = new Intent(ACTION_BROADCAST)
                    .putExtra(EXTENDED_DATA_STATUS, STATUS_DONE)
                    .putExtra(EXTENDED_DATA_TYPE, ACTION_ASK)
                    .putExtra(EXTENDED_DATA_RESPONSE, result);
        }catch(Exception e){
            Log.e("AvatarRdf", "Error executing Ask: '"+queryString+"'", e);
            localIntent = new Intent(ACTION_BROADCAST)
                    .putExtra(EXTENDED_DATA_STATUS, STATUS_FAIL)
                    .putExtra(EXTENDED_DATA_TYPE, ACTION_ASK)
                    .putExtra(EXTENDED_DATA_RESPONSE, "Error parsing query");
        }finally{
            dataset.end();
        }
        return localIntent;
    }

    protected Intent handleTriples(Intent workIntent) {
        ArrayList<Bundle> triplesBundle = workIntent.getParcelableArrayListExtra(ACTION_TRIPLES);
        Iterator<Bundle> itr = triplesBundle.iterator();

        dataset.begin(ReadWrite.WRITE);
        try{
            Model model = dataset.getDefaultModel();
            while (itr.hasNext()) {
                Bundle tripleBundle = itr.next();
                String subject = tripleBundle.getString(TRIPLE_SUBJECT);
                String subjectType = tripleBundle.getString(SUBJECT_TYPE);
                String predicate = tripleBundle.getString(TRIPLE_PREDICATE);
                String object = tripleBundle.getString(TRIPLE_OBJECT);
                String objectType = tripleBundle.getString(OBJECT_TYPE);
                Resource s;
                Property p;
                RDFNode o;
                if (subjectType != null) {
                    switch (subjectType) {
                        case SUBJECT_TYPE_BLANK:
                            s = ResourceFactory.createResource();
                            break;
                        case SUBJECT_TYPE_URI:
                            s = ResourceFactory.createResource(expandPrefix(subject));
                            break;
                        default:
                            s = ResourceFactory.createResource(expandPrefix(subject));
                    }
                } else {
                    s = ResourceFactory.createResource(expandPrefix(subject));
                }
                p = ResourceFactory.createProperty(expandPrefix(predicate));
                if (objectType != null) {
                    switch (objectType) {
                        case OBJECT_TYPE_BLANK:
                            o = ResourceFactory.createResource();
                            break;
                        case OBJECT_TYPE_LITERAL:
                            o = ResourceFactory.createPlainLiteral(expandPrefix(object));
                            break;
                        case OBJECT_TYPE_URI:
                            o = ResourceFactory.createResource(expandPrefix(object));
                            break;
                        default:
                            o = ResourceFactory.createResource(expandPrefix(object));
                    }
                } else {
                    o = ResourceFactory.createResource(expandPrefix(object));
                }
                Statement triple = ResourceFactory.createStatement(s,p,o);
                model.add(triple);
                Log.d(LOG_TAG, "Created Triple: ["
                        + s.toString() + ","
                        + p.toString() + ","
                        + o.toString() + "]");
            }
            dataset.commit();
        }finally{
            dataset.end();
        }
        Intent localIntent = new Intent(ACTION_BROADCAST)
                .putExtra(EXTENDED_DATA_STATUS, STATUS_DONE)
                .putExtra(EXTENDED_DATA_TYPE, ACTION_TRIPLES)
                .putExtra(EXTENDED_DATA_RESPONSE, "Triples Inserted");
        return localIntent;
    }

    protected Intent handlePurge(Intent workIntent){
        dataset.begin(ReadWrite.WRITE);
        try{
            dataset.getDefaultModel().removeAll();
            dataset.commit();
        }finally{
            dataset.end();
        }
        Intent localIntent = new Intent(ACTION_BROADCAST)
                .putExtra(EXTENDED_DATA_STATUS, STATUS_DONE)
                .putExtra(EXTENDED_DATA_TYPE, ACTION_PURGE)
                .putExtra(EXTENDED_DATA_RESPONSE, "Datastore Purged");
        return localIntent;
    }

    private String expandPrefix(String uri){
        Matcher matcher = prefixPattern.matcher(uri);
        if(matcher.find()){
            String prefix = matcher.group(1);
            String expandedUri = prefixes.getNsPrefixURI(prefix);
            if(expandedUri != null){
                return matcher.replaceFirst(expandedUri);
            }else{
                Log.d(LOG_TAG, "Prefix found ["+prefix+"] without matching uri");
            }
        }
        return uri;
    }

    public void onDestroy() {
//        if (ontModel != null && !ontModel.isClosed()) {
//            ontModel.close();
//        }
    }

}
