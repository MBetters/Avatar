package org.cubrc.avatar.samplerdfactivity;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import org.cubrc.avatar.samplerdfactivity.web.HttpGetTask;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static org.cubrc.avatar.samplerdfactivity.Constants.*;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by douglas.calderon on 7/28/2015.
 */
public class NewsFragment extends Fragment {
    Button fetchNewsButton;
    TextView newsStatusText;
    TableLayout snippetsTable;
    Set<NewsFetcherTask> tasks = new HashSet<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_news, container, false);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        snippetsTable = (TableLayout) view.findViewById(R.id.snippets_table);
        fetchNewsButton = (Button) view.findViewById(R.id.button_getStories);
        newsStatusText = (TextView) view.findViewById(R.id.news_status_text);
        fetchNewsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getInterests();
            }
        });
    }

    private void getInterests(){
        disableButtons();
        String query = "" +
                "PREFIX avatardom: <http://www.cubrc.org/cmif/ontologies/AvatarDomainOntology#>\n" +
                "SELECT ?Interest \n" +
                "WHERE {\n" +
                "\t?person avatardom:has_interest_in ?Interest .\n" +
                "}";
        Intent queryServiceIntent = new Intent(ACTION_SELECT);
        queryServiceIntent.putExtra(ACTION_SELECT, query);
        queryServiceIntent.putExtra(EXTENDED_DATA_REQUEST_ID, NEWS_KEY);
        newsStatusText.setText("Getting interests...");
        getActivity().startService(queryServiceIntent);
    }

    public void handleQueryResults(ArrayList<ArrayList<String>> results){
        if(results.size() > 1){
            newsStatusText.setText("Found Interests!");
            //get keywords, fire fetchers
            List<String> keywords = new ArrayList<>();
            for(int i = 1, ii = results.size(); i<ii; i++){
                ArrayList<String> row = results.get(i);
                String keyword = row.get(0);
                try {
                    keyword = URLEncoder.encode(keyword,"utf-8");
                    keywords.add(keyword);
                } catch (UnsupportedEncodingException e) {
                    Log.e(LOG_TAG, e.toString());
                    e.printStackTrace();
                }
            }
            fetchNews(keywords);
        }else{
            //no results
            newsStatusText.setText("No Interests found!");
        }
    }

    public void handleQueryFail(String response){

    }

    private void fetchNews(List<String> keywords){
        newsStatusText.setText("Fetching stories...");
        for(NewsFetcherTask t : tasks){
            t.cancel(false);
        }
        snippetsTable.removeAllViews();
        for(String keyword : keywords){
            NewsFetcherTask task = new NewsFetcherTask();
            tasks.add(task);
            task.execute("https://ajax.googleapis.com/ajax/services/search/news?v=1.0&q="+keyword);
        }

    }

    private void enableButtons(){
        fetchNewsButton.setEnabled(true);
    }

    private void disableButtons(){
        fetchNewsButton.setEnabled(false);
    }

    private void addNewsSnippet(String title, String content, String publisher, String pubDate, final String url){
        TableRow row = (TableRow) LayoutInflater.from(getActivity()).inflate(R.layout.component_news_snippet, null);
        TextView titleView = (TextView) row.findViewById(R.id.snippet_title);
        TextView contentView = (TextView) row.findViewById(R.id.snippet_content);
        TextView publisherView = (TextView) row.findViewById(R.id.snippet_publisher);
        TextView dateView = (TextView) row.findViewById(R.id.snippet_date);
        Button linkButton = (Button) row.findViewById(R.id.snippet_link);
        linkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(browserIntent);
            }
        });
        titleView.setText(Html.fromHtml(title));
        contentView.setText(Html.fromHtml(content));
        publisherView.setText(publisher);
        dateView.setText(pubDate.substring(0,16)); //TODO parse & format date?

        snippetsTable.addView(row);
    }

    private void checkStatus(){
        for(NewsFetcherTask t : tasks){
            if(t.getStatus().equals(AsyncTask.Status.FINISHED)){
                tasks.remove(t);
            }
        }
        if(tasks.isEmpty()){
            enableButtons();
            newsStatusText.setText("Stories Found!");
        }
    }

    private class NewsFetcherTask extends HttpGetTask{
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            int responseCode = 0;
            try {
                responseCode = connection.getResponseCode();
            } catch (IOException e) {
                Log.e(LOG_TAG, e.toString());
                e.printStackTrace();
            }
            if(responseCode == 200){
                try {
                    JSONObject jObj = new JSONObject(result);
                    JSONArray resultArray = jObj.getJSONObject("responseData").getJSONArray("results");
                    for(int i = 0, ii = resultArray.length(); i< ii; i++){
                        JSONObject snippet = resultArray.getJSONObject(i);
                        addNewsSnippet(
                                snippet.getString("title")
                                , snippet.getString("content")
                                , snippet.getString("publisher")
                                , snippet.getString("publishedDate")
                                , snippet.getString("unescapedUrl")
                        );
                    }
                } catch (JSONException e) {
                    Log.e(LOG_TAG, e.toString());
                    e.printStackTrace();
                }
            }else{
                Log.e(LOG_TAG, "Response code "+responseCode+" : Aborting payload parse.");
            }
            tasks.remove(this);
            checkStatus();
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            tasks.remove(this);
            checkStatus();
        }

    }
}
