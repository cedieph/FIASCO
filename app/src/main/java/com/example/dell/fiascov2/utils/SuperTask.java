package com.example.dell.fiascov2.utils;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;
import java.util.Set;

/**
 * Created by dell on 2/28/2018.
 */

public final class SuperTask extends AsyncTask<Void, Void, String> {

    private Context context;
    private String url;
    private String id;
    private ProgressDialog progressDialog;

    private SuperTask(Context context, String id, String url) {
        this.context = context;
        this.url = url;
        this.id = id;
    }

    private SuperTask(Context context, String id, String url, String message) {
        this.context = context;
        this.url = url;
        this.id = id;
        progressDialog = new ProgressDialog(context);
        progressDialog.setCancelable(false);
        progressDialog.setMessage(message);
        progressDialog.setIndeterminate(false);
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    public static void execute(Context context, String id, String url) {
        new SuperTask(context, id, url).execute();
    }
    public static void execute(Context context, String id, String url, String message) {
        new SuperTask(context, id, url, message).execute();
    }

    public interface TaskListener {
        void onTaskRespond(String id, String json);
        ContentValues setRequestValues(String id, ContentValues contentValues);
    }

    public static String createPostString(Set<Map.Entry<String, Object>> set) throws UnsupportedEncodingException {
        StringBuilder stringBuilder = new StringBuilder();
        boolean flag = true;

        for (Map.Entry<String, Object> value : set) {
            stringBuilder.append(flag ? "" : "&");
            flag = false;
            stringBuilder.append(URLEncoder.encode(value.getKey(), "UTF-8"));
            stringBuilder.append("=");
            stringBuilder.append(URLEncoder.encode(value.getValue().toString(), "UTF-8"));
        }
        return stringBuilder.toString();
    }

    @Override
    protected String doInBackground(Void... params) {
        try {
            URL url = new URL(this.url);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setDoInput(true);
            httpURLConnection.setDoOutput(true);

            OutputStream outputStream = new BufferedOutputStream(httpURLConnection.getOutputStream());
            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream));
            Log.d("Error Tag", ((AppCompatActivity)context).getPackageName());
            String postString = createPostString(((TaskListener)this.context).setRequestValues(this.id, new ContentValues()).valueSet());
            bufferedWriter.write(postString);

            bufferedWriter.flush();
            bufferedWriter.close();
            outputStream.close();

            InputStream inputStream = new BufferedInputStream(httpURLConnection.getInputStream());
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder stringBuilder = new StringBuilder();
            String line = "";

            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
            }

            bufferedReader.close();
            inputStream.close();
            httpURLConnection.disconnect();

            Log.d("This is the return: ", stringBuilder.toString());

            return stringBuilder.toString();
        } catch (Exception ignored) {
            Log.e("Error here: ", "The error is: ", ignored);
        }
        return null;
    }

    @Override
    protected void onPostExecute(String json) {
        super.onPostExecute(json);
        ((TaskListener) this.context).onTaskRespond(this.id, json);
        if(progressDialog != null)
            progressDialog.dismiss();
    }
}
