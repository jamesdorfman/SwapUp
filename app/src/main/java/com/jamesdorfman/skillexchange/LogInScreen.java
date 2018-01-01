package com.jamesdorfman.skillexchange;

import android.support.v7.app.AppCompatActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;
import com.facebook.FacebookSdk;


public class LogInScreen extends AppCompatActivity {
    public static final String MY_PREFS_NAME = "MyPrefsFile";
    EditText email;
    EditText password;
    Button logInButton;
    LinearLayout loginLinearLayout;

    public void loginActionHandler(View v) {
        if (email.getText().toString().trim().length() == 0 || password.getText().toString().trim().length() == 0) {
            //do nothing
        } else {

            AsyncTaskRunner task = new AsyncTaskRunner(new CalledByAsyncTaskInterface() {
                @Override
                public void calledByAsyncTaskFunction(JSONObject reader) {
                    try {
                        String status = reader.getString("status");
                        System.out.println("status is: " + status);
                        if (status.equals("failure")) {
                            TextView errorMessage = new TextView(LogInScreen.this);
                            errorMessage.setText("Incorrect username and password combination");
                            loginLinearLayout.addView(errorMessage);
                        } else {
                            String emailAddress = email.getText().toString();
                            String pass = password.getText().toString();
                            SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
                            editor.putString("email", emailAddress);
                            editor.putString("password", pass);
                            editor.commit();

                            SaveSharedPreference preference = new SaveSharedPreference();
                            preference.setUserName(LogInScreen.this, emailAddress);
                            Intent profileScreen = new Intent(LogInScreen.this, ProfileScreen.class);
                            startActivity(profileScreen);
                        }
                    }
                    catch (JSONException e) {
                        Log.e("JSONException", "Error: " + e.toString());
                    }
                    }
            });
            task.setProgressDialog(LogInScreen.this);
            task.setMessageOnDialog("Loggin in...");
            task.setUrl("email=" + email.getText().toString().replaceAll("@", "%40") + "&pword=" + password.getText().toString());
            task.execute();
        }
    }


    @Override

    protected void onCreate(Bundle savedInstanceState) {
        System.out.println("in LoginScreen.java");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in_screen);
        email = (EditText) findViewById(R.id.loginEmailEditText);
        password = (EditText) findViewById(R.id.loginPasswordEditText);
        logInButton = (Button) findViewById(R.id.loginSubmitButton);
        loginLinearLayout = (LinearLayout) findViewById(R.id.loginLinearLayout);
        System.out.println("in facebook class");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_log_in_screen, menu);
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