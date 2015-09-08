package org.cubrc.avatar.avatarrdfservice;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;

import static org.cubrc.avatar.avatarrdfservice.Constants.*;


public class AvatarInterface extends Activity {

//    private static final String DIRECTORY = android.os.Environment.getExternalStorageDirectory() + "/Android/data/org.cubrc.avatar";
//    private static final String STORE = DIRECTORY + "/StoreManager/AvatarStore";
//    private static final String ONT_FOLTER = DIRECTORY + "/Ont";

//    private static Dataset dataset;

    Button scan;
    Button addTriple;
    Button submitQuery;
    Button submitUpdate;
    EditText subject;
    EditText predicate;
    EditText object;
    EditText query;
    ListView statementList;

    private BroadcastReceiver responseReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(LOG_TAG, "Received broadcast");
            enableButtons();
            String status = intent.getStringExtra(EXTENDED_DATA_STATUS);
            switch (status) {
                case STATUS_DONE:
                    String type = intent.getStringExtra(EXTENDED_DATA_TYPE);
                    switch (type) {
                        case ACTION_SCAN:
                        case ACTION_SELECT: {
                            @SuppressWarnings("unchecked")
                            ArrayList<ArrayList<String>> response = (ArrayList<ArrayList<String>>)intent.getSerializableExtra(EXTENDED_DATA_RESPONSE);
                            setStatementList(response);
                            break;
                        }
                        case ACTION_UPDATE:
                        case ACTION_TRIPLES: {
                            String response = intent.getStringExtra(EXTENDED_DATA_RESPONSE);
                            simpleDialog(response);
                            break;
                        }
                    }
                    break;
                case STATUS_FAIL:
                    String response = intent.getStringExtra(EXTENDED_DATA_RESPONSE);
                    simpleDialog(response);
                    break;
                default:
                    simpleDialog("Undefined response type RDFQueryService");
                    break;
            }
        }
    };

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(responseReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(responseReceiver, new IntentFilter(ACTION_BROADCAST));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_avatar_interface);
        statementList = (ListView)findViewById(R.id.statementList);
        scan = (Button) findViewById(R.id.scan);
        addTriple = (Button) findViewById(R.id.addTriple);
        submitQuery = (Button) findViewById(R.id.submitQuery);
        submitUpdate = (Button) findViewById(R.id.submitUpdate);

        subject = (EditText) findViewById(R.id.rdf_subject);
        predicate = (EditText) findViewById(R.id.rdf_predicate);
        object = (EditText) findViewById(R.id.rdf_object);
        query = (EditText) findViewById(R.id.query_text);

        scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AvatarInterface.this.fetchTriples();
            }
        });
        addTriple.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AvatarInterface.this.addTriple();
            }
        });
        submitQuery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AvatarInterface.this.queryStore();
            }
        });
        submitUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AvatarInterface.this.updateStore();
            }
        });
//        File storeDir = new File(STORE);
//        storeDir.mkdirs();
//        dataset = TDBFactory.createDataset(STORE);
    }

    private void fetchTriples() {
        Intent queryServiceIntent = new Intent(this, RDFQueryService.class);
        queryServiceIntent.setAction(ACTION_SCAN);
        disableButtons();
        startService(queryServiceIntent);
    }

    private void addTriple() {
        String s = subject.getText().toString().trim();
        String p = predicate.getText().toString().trim();
        String o = object.getText().toString().trim();

        Intent queryServiceIntent = new Intent(this, RDFQueryService.class);
        queryServiceIntent.setAction(ACTION_TRIPLES);
        Bundle tripleBundle = new Bundle();
        tripleBundle.putString(TRIPLE_SUBJECT, s);
        tripleBundle.putString(TRIPLE_PREDICATE, p);
        tripleBundle.putString(TRIPLE_OBJECT, o);
        ArrayList<Bundle> triplesBundle = new ArrayList<>();
        triplesBundle.add(tripleBundle);
        queryServiceIntent.putExtra(ACTION_TRIPLES, triplesBundle);
        disableButtons();
        startService(queryServiceIntent);
    }

    private void queryStore() {
        String queryText = query.getText().toString().trim();
        Intent queryServiceIntent = new Intent(this, RDFQueryService.class);
        queryServiceIntent.setAction(ACTION_SELECT);
        queryServiceIntent.putExtra(ACTION_SELECT, queryText);
        disableButtons();
        startService(queryServiceIntent);
    }

    private void updateStore() {
        String queryText = query.getText().toString().trim();
        Intent queryServiceIntent = new Intent(this, RDFQueryService.class);
        queryServiceIntent.setAction(ACTION_UPDATE);
        queryServiceIntent.putExtra(ACTION_UPDATE, queryText);
        disableButtons();
        startService(queryServiceIntent);
    }

    private void disableButtons(){
        scan.setEnabled(false);
        addTriple.setEnabled(false);
        submitQuery.setEnabled(false);
        submitUpdate.setEnabled(false);
    }

    private void enableButtons(){
        scan.setEnabled(true);
        addTriple.setEnabled(true);
        submitQuery.setEnabled(true);
        submitUpdate.setEnabled(true);
    }

    private void setStatementList(ArrayList<ArrayList<String>> data){
        ArrayList<String> displayList = new ArrayList<>();
        for(ArrayList<String> row : data){
            String rowData = "";
            for(String columnData : row){
                rowData += columnData;
                rowData += " ";
            }
            displayList.add(rowData.trim());
        }
        statementList.setAdapter(new ArrayAdapter<>(AvatarInterface.this, R.layout.listview, displayList));
    }

    private void simpleDialog(String message){
        AlertDialog.Builder builder1 = new AlertDialog.Builder(AvatarInterface.this);
        builder1.setMessage(message)
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert11 = builder1.create();
        alert11.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_avatar_interface, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}