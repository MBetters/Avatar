package org.cubrc.avatar.samplerdfactivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.UUID;

import static org.cubrc.avatar.samplerdfactivity.Constants.*;

/**
 * Created by douglas.calderon on 6/23/2015.
 */
public class QueryFragment extends Fragment {

    private TableLayout resultsTable;
    private Button queryButton;
    private Button updateButton;
    private Button findPeopleButton;
    private EditText queryText;
    private TextView statusText;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_query, container, false);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        queryText = (EditText) view.findViewById(R.id.query_text);
        queryText.setHorizontallyScrolling(true);
        statusText = (TextView) view.findViewById(R.id.status_text);
        resultsTable = (TableLayout) view.findViewById(R.id.query_results);
        queryButton = (Button) view.findViewById(R.id.query_button);
        updateButton = (Button) view.findViewById(R.id.update_button);
        findPeopleButton = (Button) view.findViewById(R.id.findPeople_button);
        queryButton.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View v) {
                queryStore();
            }
        });
        updateButton.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View v) {
                updateStore();
            }
        });
        findPeopleButton.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View v) {
                findPeople();
            }
        });
    }

    private void queryStore() {
        String query = queryText.getText().toString().trim();
        Intent queryServiceIntent = new Intent(ACTION_SELECT);
        queryServiceIntent.putExtra(ACTION_SELECT, query);
        queryServiceIntent.putExtra(EXTENDED_DATA_REQUEST_ID, QUERY_KEY);
        disableButtons();
        statusText.setText("Querying...");
        getActivity().startService(queryServiceIntent);
    }

    private void updateStore() {
        String query = queryText.getText().toString().trim();
        Intent queryServiceIntent = new Intent(ACTION_UPDATE);
        queryServiceIntent.putExtra(ACTION_UPDATE, query);
        queryServiceIntent.putExtra(EXTENDED_DATA_REQUEST_ID, QUERY_KEY);
        disableButtons();
        statusText.setText("Querying...");
        getActivity().startService(queryServiceIntent);
    }

    private void findPeople() {
        queryText.setText("PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                "PREFIX ro:  <http://www.obofoundry.org/ro/ro.owl#>\n" +
                "PREFIX ero: <http://www.cubrc.org/ontologies/KDD/Upper/ExtendedRelationOntology#>\n" +
                "PREFIX info: <http://www.cubrc.org/ontologies/KDD/Mid/InformationEntityOntology#>\n" +
                "PREFIX agent: <http://www.cubrc.org/ontologies/KDD/Mid/AgentOntology#>\n" +
                "PREFIX agentinfo: <http://www.cubrc.org/ontologies/KDD/Domain/AgentInformationOntology#>\n" +
                "PREFIX avatardom: <http://www.cubrc.org/cmif/ontologies/AvatarDomainOntology#>\n" +
                "PREFIX avatar: <http://www.cubrc.org/Avatar#>\n" +
                "\n" +
                "SELECT ?FirstNameText ?MiddleNameText ?LastNameText ?PersonInterest\n" +
                "\n" +
                "WHERE\n" +
                "{\n" +
                "\n" +
                "\t?Person rdf:type agent:Person .\n" +
                "\t?Person info:designated_by ?PersonName .\n" +
                "\t\n" +
                "\t?PersonName ero:inheres_in ?PersonFullName .\n" +
                "\t?PersonFullName rdf:type avatardom:PersonNameBearer .\n" +
                "\t?PersonFullName ro:has_part ?PersonFirstName .\n" +
                "\t?PersonFullName ro:has_part ?PersonMiddleName .\n" +
                "\t?PersonFullName ro:has_part ?PersonLastName .\n" +
                "\t?PersonFirstName rdf:type avatardom:PersonFirstNameBearer .\n" +
                "\t?PersonFirstName info:has_text_value ?FirstNameText .\n" +
                "\t\n" +
                "\t?PersonMiddleName rdf:type avatardom:PersonMiddleNameBearer .\n" +
                "\t?PersonMiddleName info:has_text_value ?MiddleNameText .\n" +
                "\t\n" +
                "\t?PersonLastName rdf:type avatardom:PersonLastNameBearer .\n" +
                "\t?PersonLastName info:has_text_value ?LastNameText .\n" +
                "\t\n" +
                "\t?Person avatardom:has_interest_in ?PersonInterest .\n" +
                "\n" +
                "}");
    }

    public void handleQueryResults(ArrayList<ArrayList<String>> results){
        statusText.setText("Displaying " + results.size() + " rows");
        enableButtons();
        Activity parent = getActivity();
        resultsTable.removeAllViews();
        for(ArrayList<String> row : results){
            TableRow tr = new TableRow(parent);
            for(String column : row){
                TextView text = new TextView(parent);
                text.setText(column);
                tr.addView(text);
            }
            resultsTable.addView(tr);
        }
    }

    public void handleUpdateResults(String response){
        enableButtons();
    }

    public void handleQueryFail(String response){
        enableButtons();
    }

    private void disableButtons(){
        queryButton.setEnabled(false);
        updateButton.setEnabled(false);
    }
    private void enableButtons(){
        queryButton.setEnabled(true);
        updateButton.setEnabled(true);
    }

}
