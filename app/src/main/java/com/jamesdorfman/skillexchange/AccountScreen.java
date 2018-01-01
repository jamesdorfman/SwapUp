package com.jamesdorfman.skillexchange;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;
import org.json.JSONException;
import org.json.JSONObject;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;


public class AccountScreen extends AppCompatActivity {

    private CallbackManager callbackManager;
    private LoginButton loginButton;
    private AccessTokenTracker accessTokenTracker;
    private ProfileTracker profileTracker;
    final Context context = this;

    String birthday;
    String gender;
    String email;

    //Facebook login button
    private FacebookCallback<LoginResult> callback = new FacebookCallback<LoginResult>() {
        @Override
        public void onSuccess(LoginResult loginResult) {
            //Profile profile = Profile.getCurrentProfile();
            //nextActivity(profile);
        }
        @Override
        public void onCancel() {        }
        @Override
        public void onError(FacebookException e) {      }
    };

    private View.OnClickListener loginListener= new View.OnClickListener() {
        public void onClick(View v) {
            Intent loginIntent = new Intent(AccountScreen.this,LogInScreen.class);
            startActivity(loginIntent);
        }
    };

    private View.OnClickListener signUpListener= new View.OnClickListener() {
        public void onClick(View v) {
            Intent signupIntent = new Intent(AccountScreen.this,SignupScreen.class);
            startActivity(signupIntent);
        }
    };

    private void nextActivity(Profile profile){
        if(profile != null){
            Intent main = new Intent(AccountScreen.this, ProfileScreen.class);

            AccessToken accessToken = AccessToken.getCurrentAccessToken();

            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
            SharedPreferences.Editor editor = preferences.edit();

            editor.putString("name", profile.getFirstName());
            editor.putString("surname", profile.getLastName());
            editor.putString("fullName", profile.getFirstName() + " " + profile.getLastName());
            editor.putString("birthday", birthday);
            editor.putString("gender", gender);
            editor.putString("imageUrl", profile.getProfilePictureUri(200,200).toString());
            editor.putString("id", profile.getId());
            editor.putString("token",accessToken.getToken());

            main.putExtra("name", profile.getFirstName());
            main.putExtra("surname", profile.getLastName());
            main.putExtra("fullName", profile.getFirstName() + " " + profile.getLastName());
            main.putExtra("birthday", birthday);
            main.putExtra("gender", gender);
            main.putExtra("imageUrl", profile.getProfilePictureUri(200,200).toString());
            main.putExtra("id", profile.getId());
            main.putExtra("token", accessToken.getToken());

            editor.apply();
            startActivity(main);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        System.out.println("crated account screen...");

        FacebookSdk.sdkInitialize(this.getApplicationContext());
        AppEventsLogger.activateApp(this);

        callbackManager = CallbackManager.Factory.create();

        accessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken oldToken, AccessToken newToken) {

            }
        };

        profileTracker = new ProfileTracker() {
            @Override
            protected void onCurrentProfileChanged(Profile oldProfile, Profile newProfile) {
                //nextActivity(newProfile);
            }
        };
        accessTokenTracker.startTracking();
        profileTracker.startTracking();

        setContentView(R.layout.activity_account_screen);

        loginButton = (LoginButton)findViewById(R.id.loginButton);

        loginButton.setReadPermissions("user_friends");
        loginButton.setReadPermissions("public_profile");
        loginButton.setReadPermissions("email");
        //loginButton.setReadPermissions("user_birthday");
        //loginButton.setReadPermissions("user_friends");

        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            public void onSuccess(final LoginResult loginResult) {
                System.out.println("success");
                GraphRequest request = GraphRequest.newMeRequest(
                        loginResult.getAccessToken(),
                        new GraphRequest.GraphJSONObjectCallback() {
                            @Override
                            public void onCompleted(JSONObject object, GraphResponse response) {
                                try {
                                    Log.v("LoginActivity", response.toString());
                                    Log.v("LoginActivity", response.getRawResponse().toString());
                                    JSONObject rObj = new JSONObject(response.getRawResponse());
                                    gender = rObj.getString("gender");
//                                    birthday = rObj.getString("birthday");
                                    Log.v("LoginActivity", rObj.getString("gender"));
                                    AccessToken accessToken = loginResult.getAccessToken();
                                    Profile profile = Profile.getCurrentProfile();
                                    nextActivity(profile);
                                    Toast.makeText(getApplicationContext(), "Logging in...", Toast.LENGTH_SHORT).show();
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                Bundle parameters = new Bundle();
                parameters.putString("fields", "gender,birthday");
                request.setParameters(parameters);
                request.executeAsync();
            }
            @Override
            public void onCancel() {
            }

            @Override
            public void onError(FacebookException e) {
                System.out.println("ERROR WITH FACEBOOK SHINANIGENS");
                System.out.println(e.toString());
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_account_screen, menu);
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

    @Override
    protected void onResume() {
        super.onResume();
        //Facebook login
        //Profile profile = Profile.getCurrentProfile();
        //nextActivity(profile);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    protected void onStop() {
        super.onStop();
        //Facebook login
        accessTokenTracker.stopTracking();
        profileTracker.stopTracking();
    }

    @Override
    protected void onActivityResult(int requestCode, int responseCode, Intent intent) {
        super.onActivityResult(requestCode, responseCode, intent);
        //Facebook login
        callbackManager.onActivityResult(requestCode, responseCode, intent);
    }
}
