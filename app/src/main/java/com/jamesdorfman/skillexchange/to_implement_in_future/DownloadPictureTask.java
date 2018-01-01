package com.jamesdorfman.skillexchange.to_implement_in_future;

/**
 * Created by jamesdorfman on 16-08-08.
 */

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import com.mikhaellopez.circularimageview.CircularImageView;


//ARG #3 AKA STRING IS WHAT doInBackground() returns to onPostExecute()
class DownloadPictureTask extends AsyncTask<String,String,String>
{
    private ProgressDialog progressDialog;

    public String filePath;
    public String email;
    public String password;
    public Bitmap image;
    public CircularImageView profilePicture;

    public DownloadPictureTask(Context ctx){
        progressDialog = new ProgressDialog(ctx);
    }

    @Override
    protected void onPostExecute(String result) {
        // TODO Auto-generated method stub
        super.onPostExecute(result);
        // update textview here
        Log.d("Server message",result);
        if(result=="yes"){
            profilePicture.setImageBitmap(image);

        }
        progressDialog.dismiss();
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        progressDialog.setMessage("Loading...");
        progressDialog.show();
    }

    @Override
    protected String doInBackground(String... params) {
            String requestUrl = "http://jamesdorfman.com/skillExchange/api.php?email=" + email + "&pword=" + password + "&profilePicDownload=true";
            URL url = null;
            String result = "yes";
            try {
                url = new URL(requestUrl);
                image = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                System.out.println(image);
            } catch (MalformedURLException e) {
                result = "no";
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
                result = "no";
            }
        return result;
    }
}