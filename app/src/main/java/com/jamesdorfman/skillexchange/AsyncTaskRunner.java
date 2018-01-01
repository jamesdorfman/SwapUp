package com.jamesdorfman.skillexchange;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.util.ArrayList;

class AsyncTaskRunner extends AsyncTask<ArrayList, String, Void> {

    public CalledByAsyncTaskInterface delegate;
    private ProgressDialog progressDialog;
    private String messageOnDialog;
    private String url_select;
    InputStream inputStream = null;
    String result = "";
    public String findPeople;

    public AsyncTaskRunner(CalledByAsyncTaskInterface taskInterface){
        delegate = taskInterface;
    }

    public void setProgressDialog(Context ctx){
        progressDialog = new ProgressDialog(ctx);
    }

    public void setMessageOnDialog(String message){
        messageOnDialog = message;
    }

    public void setUrl(String urlPiece){
        url_select = "http://jamesdorfman.com/skillExchange/api.php?";
        url_select += urlPiece;
    }

    protected void onPreExecute() {
        progressDialog.setMessage(messageOnDialog);
        progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            public void onCancel(DialogInterface arg0) {
                AsyncTaskRunner.this.cancel(true);
            }
        });
    }

    @Override
    protected Void doInBackground(ArrayList... params) {

        System.out.println(url_select);
        ArrayList<NameValuePair> param = new ArrayList<NameValuePair>();

        try {
            // Set up HTTP post

            // HttpClient is more then less deprecated. Need to change to URLConnection
            HttpClient httpClient = new DefaultHttpClient();

            HttpPost httpPost = new HttpPost(url_select);

            if(params.length>0){
                for(int j=0;j<params.length;j++){
                    System.out.println(params[j].get(0).toString() + params[j].get(1).toString());
                    param.add(new BasicNameValuePair(params[j].get(0).toString(),params[j].get(1).toString()));
                    System.out.println("adding: " + params[j].get(0).toString() + " : " + params[j].get(1).toString());
                }
            }
            httpPost.setEntity(new UrlEncodedFormEntity(param));
            HttpResponse httpResponse = httpClient.execute(httpPost);
            HttpEntity httpEntity = httpResponse.getEntity();
            //System.out.println(EntityUtils.toString(httpEntity));
            //Read content & Log
            inputStream = httpEntity.getContent();
            System.out.println("Response: " + inputStream.toString());
        } catch (UnsupportedEncodingException e1) {
            Log.e("EncodingNotSupporTED", e1.toString());
            e1.printStackTrace();
        } catch (ClientProtocolException e2) {
            Log.e("ClientProtocolException", e2.toString());
            e2.printStackTrace();
        } catch (IllegalStateException e3) {
            Log.e("IllegalStateException", e3.toString());
            e3.printStackTrace();
        } catch (IOException e4) {
            Log.e("IOException", e4.toString());
            e4.printStackTrace();
        }
        // Convert response to string using String Builder
        try {
            BufferedReader bReader = new BufferedReader(new InputStreamReader(inputStream, "utf-8"), 8);
            StringBuilder sBuilder = new StringBuilder();

            String line = null;
            while ((line = bReader.readLine()) != null) {
                sBuilder.append(line + "\n");
            }

            System.out.println("about to do it");
            inputStream.close();
            result = sBuilder.toString();
            System.out.println("printing it: " + result);

        } catch (Exception e) {
            Log.e("JSONERRor:", "Error converting result " + e.toString());
        }
        return null;
    } // protected Void doInBackground(String... params)

    public void doThis(){

    }

    protected void onPostExecute(Void v) {
        //parse JSON data
        try {
            System.out.println("READER: " + result);
            JSONObject reader = new JSONObject(result);
            System.out.println("about to call: " + reader.toString());
            delegate.calledByAsyncTaskFunction(reader);
            this.progressDialog.dismiss();
        } catch (JSONException e) {
            Log.e("JSONException", "Error: " + e.toString());
        }
    }
}

