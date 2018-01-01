package com.jamesdorfman.skillexchange.to_implement_in_future;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

/*
    This class is currently unused
    Was written when expirementing with saving facebook images on
    a custom server instead of downloading from facebook every time

    This class was made obsolete by the discovery that facebook profile pictures
    don't need authorization to access with a direct link
*/

class UploadPictureTask extends AsyncTask<Bitmap,String,String>
{
    private ProgressDialog progressDialog;

    public String filePath;
    public String email;
    public String password;
    public Bitmap image;
    public Context context;
    public String token;
    public String id;

    public UploadPictureTask(Context ctx){
        System.out.println("in the upload task");
        this.progressDialog = new ProgressDialog(ctx);
        this.context = ctx;
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        // update textview here
        Log.d("Server message",result);
        progressDialog.dismiss();
    }

    @Override
    protected void onPreExecute() {
        System.out.println("pre execute");
        super.onPreExecute();
        progressDialog.setMessage("Uploading...");
        //progressDialog.show();
    }

    @Override
    protected String doInBackground(Bitmap... params) {
        try {
            System.out.println("IN THE TASK!");
            String requestUrl = "http://jamesdorfman.com/skillExchange/api.php?profilePicUpload=true";
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost(requestUrl);
                MultipartEntity mpEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
                Bitmap bmp = params[0];
                System.out.println("about to enter matrixed area");
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                bmp.compress(Bitmap.CompressFormat.JPEG, 30, bos);
                InputStream in = new ByteArrayInputStream(bos.toByteArray());
                System.out.println("before");
                //ContentBody contentFile = new FileBody(file);
                System.out.println("this.id" + this.id);
                ContentBody contentFile = new InputStreamBody(in, "image/jpeg", this.id);
                mpEntity.addPart("avatar", contentFile);
                mpEntity.addPart("id",new StringBody(this.id));
                mpEntity.addPart("token",new StringBody(this.token));
                System.out.println("this is the id: " + this.id);
                System.out.println("executing request " + httppost.getRequestLine());
                httppost.setEntity(mpEntity);
                System.out.println("right before request");
                HttpResponse response = httpclient.execute(httppost);
                int responseCode = response.getStatusLine().getStatusCode();
                System.out.println("response code: " + Integer.toString(responseCode));
                switch (responseCode) {
                    case 200:
                        HttpEntity entity = response.getEntity();
                        if (entity != null) {
                            String responseBody = EntityUtils.toString(entity);
                            System.out.println("response body: " + responseBody);
                        }
                        break;
                }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "success";
    }
}