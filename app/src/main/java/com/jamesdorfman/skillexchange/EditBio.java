package com.jamesdorfman.skillexchange;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class EditBio extends AppCompatActivity {

    String email;
    String password;

    EditText bioEditText;
    public void saveData(View v){
        AsyncTaskRunner task = new AsyncTaskRunner(new CalledByAsyncTaskInterface() {
            @Override
            public void calledByAsyncTaskFunction(JSONObject reader) {
                try {
                    String status = reader.getString("status");
                    System.out.println("status is: " + status);
                    if (status.equals("failure")) {
                        Toast.makeText(getApplicationContext(), "Error saving bio. Please restart the app",
                                Toast.LENGTH_LONG).show();
                    } else {
                        Toast toast = Toast.makeText(EditBio.this,"Bio saved succesfully",Toast.LENGTH_SHORT);
                        toast.show();
                    }
                }
                catch (JSONException e) {
                    Log.e("JSONException", "Error: " + e.toString());
                }
            }
        });
        task.setProgressDialog(EditBio.this);
        task.setMessageOnDialog("Saving...");
        task.setUrl("email=" + email + "&pword=" + password + "&setBio=" + bioEditText.getText().toString().replaceAll(" ", "%20").replaceAll("\n",""));
        task.execute();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_bio);
        SharedPreferences prefs = getSharedPreferences(LogInScreen.MY_PREFS_NAME, MODE_PRIVATE);
        email  = prefs.getString("email", null);//"No name defined" is the default value.
        password = prefs.getString("password", null);//"No name defined" is the default value.
        System.out.println("email: " + email + " password: " + password);

        bioEditText = (EditText) findViewById(R.id.editBioEditText);

        AsyncTaskRunner task = new AsyncTaskRunner(new CalledByAsyncTaskInterface() {
            @Override
            public void calledByAsyncTaskFunction(JSONObject reader) {
                try {
                    String status = reader.getString("status");
                    System.out.println("status is: " + status);
                    if (status.equals("failure")) {
                        Toast.makeText(getApplicationContext(), "Error getting bio from server. Please restart the app",
                                Toast.LENGTH_LONG).show();
                    } else {
                        String bioText = reader.getString("bio");
                        bioEditText.setText(bioText);
                        System.out.println("Success!!");
                    }
                }
                catch (JSONException e) {
                    Log.e("JSONException", "Error: " + e.toString());
                }
            }
        });
        task.setProgressDialog(EditBio.this);
        task.setMessageOnDialog("Fetching data from server...");
        task.setUrl("email=" + email + "&pword=" + password + "&getBio=true");
        task.execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_edit_bio, menu);
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
}
