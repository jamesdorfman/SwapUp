package com.jamesdorfman.skillexchange.to_implement_in_future;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;

/*
    Class is currently unused
    Lots of issues came up when image saving was implmemented
    Saving images on device instead of downloading them from facebook constantly
    is intended to be implemented in future updates
 */
class SaveImageOnDevice extends AsyncTask<Bitmap,String,String>
{
    private ProgressDialog progressDialog;

    public String filePath;
    public String email;
    public String password;
    public Bitmap image;
    public Context context;

    public SaveImageOnDevice(Context ctx){
        progressDialog = new ProgressDialog(ctx);
        context = ctx;
    }

    @Override
    protected void onPostExecute(String result) {
        // TODO Auto-generated method stub
        super.onPostExecute(result);
        // update textview here
        Log.d("Server message",result);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(Bitmap... params) {
        try {
            String requestUrl = "http://jamesdorfman.com/skillExchange/api.php?email=" + email + "&pword=" + password + "&profilePicUpload=true";
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost(requestUrl);

            MultipartEntity mpEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
            if (filePath != null) {
                File file = new File(filePath);
                Bitmap bmp = BitmapFactory.decodeFile(filePath);
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                bmp.compress(Bitmap.CompressFormat.JPEG, 70, bos);
                InputStream in = new ByteArrayInputStream(bos.toByteArray());
                System.out.println("FilePath: " + filePath);
                Log.d("EDIT USER PROFILE", "UPLOAD: file length = " + file.length());
                Log.d("EDIT USER PROFILE", "UPLOAD: file exist = " + file.exists());
                System.out.println("before");
                //ContentBody contentFile = new FileBody(file);
                ContentBody contentFile = new InputStreamBody(in, "image/jpeg", email);
                mpEntity.addPart("avatar", contentFile);

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

                            /*ContextWrapper cw = new ContextWrapper(context);
                            // path to /data/data/yourapp/app_data/imageDir
                            File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
                            // Create imageDir
                            File mypath=new File(directory,email + ".jpg");

                            FileOutputStream fos = null;
                            try {
                                fos = new FileOutputStream(mypath);
                                // Use the compress method on the BitMap object to write image to the OutputStream
                                params[0].compress(Bitmap.CompressFormat.PNG, 50, fos);
                            } catch (Exception e) {
                                e.printStackTrace();
                            } finally {
                                fos.close();
                            }
                            directory.getAbsolutePath();*/
                        }
                        break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "success";
    }
}
