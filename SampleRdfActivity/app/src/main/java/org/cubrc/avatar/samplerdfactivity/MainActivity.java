package org.cubrc.avatar.samplerdfactivity;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;

import org.cubrc.avatar.samplerdfactivity.adapter.TabsPagerAdapter;

import java.util.ArrayList;

import static org.cubrc.avatar.samplerdfactivity.Constants.*;


public class MainActivity extends FragmentActivity
        implements ActionBar.TabListener{

    private ViewPager viewPager;
    private TabsPagerAdapter mAdapter;
    private ActionBar actionBar;
    //Tab Titles
    private String[] tabs = { "Query", "Create", "News" };

    private BroadcastReceiver responseReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String status = intent.getStringExtra(EXTENDED_DATA_STATUS);
            Log.d("SampleRdf", "Received MobileRdf Broadcast [" + status + "]");
            if(status.equals(STATUS_DONE)) {
                String reqId = intent.getStringExtra(EXTENDED_DATA_REQUEST_ID);
                switch (reqId) {
                    case QUERY_KEY: {
                        @SuppressWarnings("unchecked")
                        ArrayList<ArrayList<String>> response = (ArrayList<ArrayList<String>>) intent.getSerializableExtra(EXTENDED_DATA_RESPONSE);
                        QueryFragment qFrag = getQueryFragment();
                        if (qFrag != null) {
                            qFrag.handleQueryResults(response);
                        }
                        break;
                    }
                    case UPDATE_KEY: {
                        String response = intent.getStringExtra(EXTENDED_DATA_RESPONSE);
                        QueryFragment qFrag = getQueryFragment();
                        if (qFrag != null) {
                            qFrag.handleUpdateResults(response);
                        }
                        simpleDialog(response);
                        break;
                    }
                    case CREATE_PERSON_KEY: {
                        String response = intent.getStringExtra(EXTENDED_DATA_RESPONSE);
                        CreatePersonFragment cpFrag = getCreatePersonFragment();
                        if (cpFrag != null) {
                            cpFrag.handleQueryResults(response);
                        }
                        simpleDialog(response);
                        break;
                    }
                    case NEWS_KEY: {
                        @SuppressWarnings("unchecked")
                        ArrayList<ArrayList<String>> response = (ArrayList<ArrayList<String>>) intent.getSerializableExtra(EXTENDED_DATA_RESPONSE);
                        NewsFragment nFrag = getNewsFragment();
                        if (nFrag != null) {
                            nFrag.handleQueryResults(response);
                        }
                        break;
                    }
                }
            }else if(status.equals(STATUS_FAIL)){
                String response = intent.getStringExtra(EXTENDED_DATA_RESPONSE);
                simpleDialog(response);
                String reqId = intent.getStringExtra(EXTENDED_DATA_REQUEST_ID);
                switch (reqId) {
                    case QUERY_KEY:
                        QueryFragment qFrag = getQueryFragment();
                        if (qFrag != null) {
                            qFrag.handleQueryFail(response);
                        }
                        break;
                    case CREATE_PERSON_KEY:
                        CreatePersonFragment cpFrag = getCreatePersonFragment();
                        if (cpFrag != null) {
                            cpFrag.handleQueryFail(response);
                        }
                        simpleDialog(response);
                        break;
                    case NEWS_KEY:
                        NewsFragment nFrag = getNewsFragment();
                        if (nFrag != null) {
                            nFrag.handleQueryFail(response);
                        }
                        break;
                }
            }else{
                simpleDialog("Undefined response type RDFQueryService");
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        viewPager = (ViewPager) findViewById(R.id.pager);
        viewPager.setOffscreenPageLimit(3);
        actionBar = getActionBar();
        mAdapter = new TabsPagerAdapter(getSupportFragmentManager());

        viewPager.setAdapter(mAdapter);
        actionBar.setHomeButtonEnabled(false);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        for(String tab_name : tabs){
            actionBar.addTab(actionBar.newTab().setText(tab_name)
                    .setTabListener(this));
        }

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageScrolled(int i, float v, int i1) {
            }

            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
            }

            @Override
            public void onPageScrollStateChanged(int i) {
            }
        });

    }

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

    private void simpleDialog(String message){
        AlertDialog.Builder builder1 = new AlertDialog.Builder(MainActivity.this);
        builder1.setMessage(message)
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert11 = builder1.create();
        alert11.show();
    }

    private QueryFragment getQueryFragment(){
        return ((QueryFragment)mAdapter.getRegisteredFragment(QUERY_PAGE_IDX));
    }
    private CreatePersonFragment getCreatePersonFragment(){
        return ((CreatePersonFragment)mAdapter.getRegisteredFragment(CREATE_PERSON_PAGE_IDX));
    }
    private NewsFragment getNewsFragment(){
        return ((NewsFragment)mAdapter.getRegisteredFragment(NEWS_PAGE_IDX));
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
        viewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {

    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {

    }
}
