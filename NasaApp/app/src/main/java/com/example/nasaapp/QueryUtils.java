package com.example.nasaapp;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.ImageSource;
import gov.nasa.worldwind.shape.Placemark;
import gov.nasa.worldwind.shape.PlacemarkAttributes;

/**
 * Helper methods related to requesting and receiving News data from API.
 */
public final class QueryUtils {
    private static final String ACCEPT_PROPERTY = "application/geo+json;version=1";
    private static final String USER_AGENT_PROPERTY = "newsapi.org (hitalo.c.a@gmail.com)"; //your email id for that site

    private static final String LOG_TAG = "QueryUtils";

    private QueryUtils() {}

    //fetch the news to a List
    public static List<Placemark> fetchNewsData(String requestUrl) {
        URL url = createUrl(requestUrl);

        String jsonRes = null;
        try {
            jsonRes = makeHttpRequest(url);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem making the HTTP request.", e);
        }

        List<Placemark> news = extractNews(jsonRes);

        return news;
    }

    //create URL obj from string
    private static URL createUrl(String stringUrl) {
        URL url = null;
        try {
            url = new URL(stringUrl);
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "Error with creating URL ", e);
        }
        return url;
    }

    /**
     * Make an HTTP request to the given URL and return a String as the response.
     */
    private static String makeHttpRequest(URL url) throws IOException {
        String jsonResponse = "";

        // If the URL is null, then return early.
        if (url == null) {
            return jsonResponse;
        }

        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setReadTimeout(10000 /* milliseconds */);
            urlConnection.setConnectTimeout(15000 /* milliseconds */);
            urlConnection.setRequestProperty("Accept", ACCEPT_PROPERTY);  // added
            urlConnection.setRequestProperty("User-Agent", USER_AGENT_PROPERTY); // added
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // If the request was successful (response code 200),
            // then read the input stream and parse the response.
            if (urlConnection.getResponseCode() == 200) {
                inputStream = urlConnection.getInputStream();
                jsonResponse = readFromStream(inputStream);
            } else {
                Log.e(LOG_TAG, "Error response code: " + urlConnection.getResponseCode());
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem retrieving the news JSON results.", e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (inputStream != null) {
                inputStream.close();
            }
        }
        return jsonResponse;
    }

    /**
     * Convert the InputStream into a String which contains the
     * whole JSON response from the server.
     */
    private static String readFromStream(InputStream inputStream) throws IOException {
        StringBuilder output = new StringBuilder();
        if (inputStream != null) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = reader.readLine();
            while (line != null) {
                output.append(line);
                line = reader.readLine();
            }
        }
        return output.toString();
    }



    //Transform the json into news objects
    private static ArrayList<Placemark> extractNews(String jsonResponse) {
        ArrayList<Placemark> news = new ArrayList<>();

        try {
            ArrayList<Placemark> finalArray = new ArrayList<>();
            JSONObject obj = new JSONObject(jsonResponse);
            JSONArray arr = obj.getJSONArray("debris");
            for(int i = 0; i < arr.length(); i++){
                JSONObject currentObj = arr.getJSONObject(i);
                String id = currentObj.getString("id");
                String timestamp = currentObj.getString("timestamp");
                double lat = currentObj.getDouble("lat");
                double lon = currentObj.getDouble("lon");
                double height = currentObj.getDouble("height");

                Placemark objAux = new Placemark(Position.fromDegrees(lat, lon, height),
                        PlacemarkAttributes.createWithImageAndLeader(ImageSource.fromResource(R.drawable.point)),
                        id);
                finalArray.add(objAux);
            }

            return finalArray;
        } catch (JSONException e) {
            Log.e("QueryUtils", "Problem parsing the news JSON results", e);
        }

        return news;
    }

}