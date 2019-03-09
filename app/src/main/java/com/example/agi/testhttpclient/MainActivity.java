package com.example.agi.testhttpclient;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.JsonReader;
import android.util.Log;
import android.widget.TextView;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.URI;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Read image file
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.test2);

        // Convert n compress the image to outputStream
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);

        // execute the request in new thread
        new ocr_request(MainActivity.this).execute(outputStream);

    }

    private static class ocr_request extends AsyncTask<ByteArrayOutputStream, String, String>{
        private WeakReference<MainActivity> activityWeakReference;

        public ocr_request(MainActivity context) {
            activityWeakReference = new WeakReference<>(context);
        }

        @Override
        protected String doInBackground(ByteArrayOutputStream... outputStream) {
            return post_request_ocr(outputStream[0]);
        }

        @Override
        protected void onPostExecute(String s) {
            // set text view to received text data after post request in doInBackground
            Activity activity = activityWeakReference.get();
            if (activity != null) {
                TextView textView = activity.findViewById(R.id.tvPreview);
                textView.setText(s);
            }
        }
    }

    public static String post_request_ocr(ByteArrayOutputStream outputStream){

        HttpClient httpclient = HttpClients.createDefault();

        try
        {
            Log.d(TAG, "post_request_ocr: initializing URIBuilder...");
            URIBuilder builder = new URIBuilder("https://southeastasia.api.cognitive.microsoft.com/vision/v2.0/ocr");

            Log.d(TAG, "post_request_ocr: setting builder parameter...");
            builder.setParameter("language", "unk");
            builder.setParameter("detectOrientation", "true");

            Log.d(TAG, "post_request_ocr: building...");
            URI uri = builder.build();

            Log.d(TAG, "post_request_ocr: setting request header...");
            HttpPost request = new HttpPost(uri);
            request.setHeader("Content-Type", "application/octet-stream");
            request.setHeader("Ocp-Apim-Subscription-Key", "f21b4f194bb1480c8dde294d9baf18e7");

            Log.d(TAG, "post_request_ocr: setting request body...");
            // Request body
            ByteArrayEntity reqEntity = new ByteArrayEntity(outputStream.toByteArray());
            //StringEntity reqEntity = new StringEntity("{\"url\":\"http://downloads.bbc.co.uk/skillswise/english/en03text/images/en03text-v-types-of-text-448x252.jpg\"}");
            request.setEntity(reqEntity);
            //Log.d(TAG, "request body: " + "{\"url\":\"http://downloads.bbc.co.uk/skillswise/english/en03text/images/en03text-v-types-of-text-448x252.jpg\"}");

            Log.d(TAG, "post_request_ocr: executing request...");
            HttpResponse response = httpclient.execute(request);

            Log.d(TAG, "post_request_ocr: receiving response...");
            HttpEntity entity = response.getEntity();

            if (entity != null)
            {
                StringBuilder sb = new StringBuilder();
                String the_response = EntityUtils.toString(entity);
                Log.d(TAG, "post_request_ocr_1: " + the_response);

                Log.d(TAG, "post_request_ocr: reading json...");
                JSONObject json = (JSONObject) new JSONTokener(the_response).nextValue();
                Log.d(TAG, "post_request_ocr: reading regions array");
                JSONArray json2 = json.getJSONArray("regions");
                for (int i = 0; i < json2.length(); i++) {
                    Log.d(TAG, "post_request_ocr: opening json array in regions");
                    JSONObject json3 = json2.getJSONObject(i);
                    Log.d(TAG, "post_request_ocr: reading lines array");
                    JSONArray json4 = json3.getJSONArray("lines");
                    for (int j = 0; j < json4.length(); j++) {
                        Log.d(TAG, "post_request_ocr: opening json array in lines");
                        JSONObject json5 = json4.getJSONObject(j);
                        Log.d(TAG, "post_request_ocr: reading words array");
                        JSONArray json6 = json5.getJSONArray("words");
                        for (int k = 0; k < json6.length(); k++) {
                            Log.d(TAG, "post_request_ocr: opening json array in words");
                            JSONObject json7 = json6.getJSONObject(k);
                            String test = (String) json7.get("text");
                            sb.append(test).append(" ");
                        }
                        sb.append("\n");
                    }
                    sb.append("\n\n");
                }

                Log.d(TAG, "request_ocr LINES: " + sb);
                return sb.toString();
            }
            return "NULL";
        }
        catch (Exception e)
        {
            //System.out.println(e.getMessage());
            e.printStackTrace();
            Log.d(TAG, "post_request_ocr_2: " + e.getMessage());
            return e.getMessage();
        }

    }
}
