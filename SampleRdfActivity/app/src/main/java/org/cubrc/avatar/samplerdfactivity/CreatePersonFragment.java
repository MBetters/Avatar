package org.cubrc.avatar.samplerdfactivity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.cubrc.avatar.samplerdfactivity.Constants.*;

/**
 * Created by douglas.calderon on 6/23/2015.
 */
public class  CreatePersonFragment extends Fragment {

    private TableLayout tableLayout;
    private EditText firstNameElement;
    private EditText middleNameElement;
    private EditText lastNameElement;
    private Button addPersonButton;
    private Button addInterestButton;
    private List<EditText> interestFields = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_create_person, container, false);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tableLayout = (TableLayout) view.findViewById(R.id.createPersonLayout);
        firstNameElement = (EditText) view.findViewById(R.id.edit_firstName);
        middleNameElement = (EditText) view.findViewById(R.id.edit_middleName);
        lastNameElement = (EditText) view.findViewById(R.id.edit_lastName);
        addPersonButton = (Button) view.findViewById(R.id.addPerson_button);
        addInterestButton = (Button) view.findViewById(R.id.addInterest_button);
        addPersonButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                addPerson();
            }
        });
        addInterestButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                addInterest();
            }
        });
    }

    private void addPerson(){
        disableButtons();
        String fn = firstNameElement.getText().toString().trim();
        String mn = middleNameElement.getText().toString().trim();
        String ln = lastNameElement.getText().toString().trim();

        Intent queryServiceIntent = new Intent(ACTION_TRIPLES);

        //FIRST NAME
        ArrayList<Bundle> triplesList = new ArrayList<Bundle>();
        String person = avatarNs+UUID.randomUUID();
        triplesList.add(makeTriple(person, "rdf:type", "agent:Person"));
        String personName = avatarNs+UUID.randomUUID();
        triplesList.add(makeTriple(person, "info:designated_by", personName));
        triplesList.add(makeTriple(personName, "rdf:type", "agentinfo:PersonName"));
        String fullName = avatarNs+UUID.randomUUID();
        triplesList.add(makeTriple(personName, "ero:inheres_in", fullName));
        triplesList.add(makeTriple(fullName, "rdf:type", "avatardom:PersonNameBearer"));
        String firstName = avatarNs+UUID.randomUUID();
        triplesList.add(makeTriple(fullName, "ro:has_part", firstName));
        triplesList.add(makeTriple(firstName, "rdf:type", "avatardom:PersonFirstNameBearer"));
        Bundle firstNameTriple = makeTriple(firstName, "info:has_text_value", fn);
        firstNameTriple.putString(OBJECT_TYPE, OBJECT_TYPE_LITERAL);
        triplesList.add(firstNameTriple);

        //MIDDLE NAME
        String middleName = avatarNs+UUID.randomUUID();
        triplesList.add(makeTriple(fullName, "ro:has_part", middleName));
        triplesList.add(makeTriple(middleName, "rdf:type", "avatardom:PersonMiddleNameBearer"));
        Bundle middleNameTriple = makeTriple(middleName, "info:has_text_value", mn);
        middleNameTriple.putString(OBJECT_TYPE, OBJECT_TYPE_LITERAL);
        triplesList.add(middleNameTriple);

        //LAST NAME
        String lastName = avatarNs+UUID.randomUUID();
        triplesList.add(makeTriple(fullName, "ro:has_part", lastName));
        triplesList.add(makeTriple(lastName, "rdf:type", "avatardom:PersonLastNameBearer"));
        Bundle lastNameTriple = makeTriple(lastName, "info:has_text_value", ln);
        lastNameTriple.putString(OBJECT_TYPE, OBJECT_TYPE_LITERAL);
        triplesList.add(lastNameTriple);

        //INTERESTS
        for(EditText et : interestFields){
            String interest = et.getText().toString().trim();
            if(interest.length() > 0){
                Bundle interestTriple = makeTriple(person, "avatardom:has_interest_in", interest);
                interestTriple.putString(OBJECT_TYPE, OBJECT_TYPE_LITERAL);
                triplesList.add(interestTriple);
            }
        }

        queryServiceIntent.putExtra(ACTION_TRIPLES, triplesList);
        queryServiceIntent.putExtra(EXTENDED_DATA_REQUEST_ID, CREATE_PERSON_KEY);
        getActivity().startService(queryServiceIntent);
    }

    private void addInterest(){
        TableRow row = (TableRow) LayoutInflater.from(getActivity()).inflate(R.layout.component_interest_row, null);
        EditText interest = (EditText) row.getChildAt(1);
        interestFields.add(interest);
        tableLayout.addView(row, tableLayout.getChildCount() - 2);
    }

    public void handleQueryResults(String response){
        enableButtons();
    }

    public void handleQueryFail(String response){
        enableButtons();
    }

    private Bundle makeTriple(String s, String p, String o){
        Bundle t = new Bundle();
        t.putString(TRIPLE_SUBJECT, s);
        t.putString(TRIPLE_PREDICATE, p);
        t.putString(TRIPLE_OBJECT, o);
        return t;
    }

    private void disableButtons(){
        addPersonButton.setEnabled(false);
    }

    private void enableButtons(){
        addPersonButton.setEnabled(true);
    }
}
