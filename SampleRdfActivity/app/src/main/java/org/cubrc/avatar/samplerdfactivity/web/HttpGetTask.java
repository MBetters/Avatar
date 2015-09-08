package org.cubrc.avatar.samplerdfactivity.web;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import static org.cubrc.avatar.samplerdfactivity.Constants.*;

/**
 * Created by douglas.calderon on 7/28/2015.
 */
public class HttpGetTask extends AsyncTask<String, Integer, String> {

    public InputStream inputStream = null;
    public String result = "";
    public HttpURLConnection connection = null;

    @Override
    protected String doInBackground(String... params) {

        try {
            if(isCancelled()) {
                inputStream.close();
                return "";
            }
            connection = (HttpURLConnection)new URL(params[0]).openConnection();
            connection.setRequestMethod("GET");
            inputStream = connection.getInputStream();
            if(isCancelled()) {
                inputStream.close();
                return "";
            }

            BufferedReader bReader = new BufferedReader(new InputStreamReader(inputStream, "utf-8"), 8);
            StringBuilder sBuilder = new StringBuilder();

            String line = null;
            while ((line = bReader.readLine()) != null) {
                if(isCancelled()) {
                    inputStream.close();
                    return "";
                }
                sBuilder.append(line).append("\n");
            }

            inputStream.close();
            result = sBuilder.toString();

        } catch (Exception e) {
            Log.e(LOG_TAG, "Error converting result " + e.toString());
        }

        return result;
    }
}
