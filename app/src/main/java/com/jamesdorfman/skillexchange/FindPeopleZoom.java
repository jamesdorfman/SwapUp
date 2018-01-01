package com.jamesdorfman.skillexchange;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.Image;
import android.preference.PreferenceManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.apmem.tools.layouts.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;

// When a user is swiping through potential matches (in the findpeoplescreen)
// This screen loads specifically when they click 'view more' on someone they like
// (to see their bio)
public class FindPeopleZoom extends AppCompatActivity implements LocationListener {

    String token;
    String id;

    LocationManager locationManager;
    String provider;

    String latitude;
    String longitude;

    ProgressDialog dialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_people_zoom);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        Bundle inBundle = getIntent().getExtras();
        id = preferences.getString("id", null);
        token = preferences.getString("token", null);


        Bundle extras = getIntent().getExtras();
        final String match_id = extras.getString("id");
        System.out.println("match_id: " + match_id);

        // Getting LocationManager object
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // Creating an empty criteria object
        Criteria criteria = new Criteria();

        // Getting the name of the provider that meets the criteria
        provider = locationManager.getBestProvider(criteria, false);

        if (provider != null && !provider.equals("")) {

            // Get the location from the given provider
            Location location = locationManager.getLastKnownLocation(provider);

            locationManager.requestLocationUpdates(provider, 20000, 1, this);

            if (location != null)
                onLocationChanged(location);
            else
                Toast.makeText(getBaseContext(), "Location can't be retrieved", Toast.LENGTH_SHORT).show();

        } else {
            Toast.makeText(getBaseContext(), "No Provider Found", Toast.LENGTH_SHORT).show();
        }

        if (longitude != null) {
            AsyncTaskRunner task = new AsyncTaskRunner(new CalledByAsyncTaskInterface() {
                @Override
                public void calledByAsyncTaskFunction(JSONObject reader) {
                    try {
                        String status = reader.getString("status");
                        System.out.println("status is: " + status);
                        if (status.equals("failure")) {
                            Toast.makeText(getApplicationContext(), "Error loading profile. Please check your internet connectivity",
                                    Toast.LENGTH_LONG).show();
                        } else {
                            JSONArray matchesArray = reader.getJSONArray("matchesArray");
                            System.out.println("NAMENAME: " + matchesArray.toString());
                            JSONObject insideArray = matchesArray.getJSONObject(0);
                            String name = insideArray.getString("name").split(" ")[0];
                            name = name.substring(0,1).toUpperCase() + name.substring(1);
                            String age = insideArray.getString("age");
                            String bio = insideArray.getString("biography");
                            String distance = insideArray.getString("distance");
                            String fbImageUrl = insideArray.getString("fbImageUrl");

                            TextView nameTV = (TextView) findViewById(R.id.profileNameTextView);
                            TextView ageTV = (TextView) findViewById(R.id.profileAgeTextView);
                            TextView bioTV = (TextView) findViewById(R.id.profileBioTextView);
                            TextView skillTeachTV = (TextView) findViewById(R.id.skillTeachTextView);
                            TextView skillLearnTV = (TextView) findViewById(R.id.skillLearnTextView);
                            ImageView profilePic = (ImageView) findViewById(R.id.profilePicture);
                            TextView bioTitle = (TextView) findViewById(R.id.bioTitleTextView);
                            bioTitle.setText("About " + name);

                            if(bio.equals("Write a short bio about yourself here. Try to seem friendly and helpful, which will help you get more matches") || bio.equals("")){
                                bioTitle.setText("");
                                bio = " ";
                            }
                            System.out.println("NAMENAME: " + name);
                            nameTV.setText(name + ", " + age);
                            ageTV.setText("");
                            skillTeachTV.setText(name + " can teach");
                            skillLearnTV.setText(name + " wants to learn");
                            bioTV.setText(bio);
                            System.out.println("findpeoplezoom new: " + "http://jamesdorfman.com/skillExchange/user_images/" + match_id + ".jpg");
                            Picasso.with(FindPeopleZoom.this).load(fbImageUrl).error(R.drawable.default_profile_picture).into(profilePic);
                            //profilePic.setLayoutParams(new LinearLayout.LayoutParams(profilePic.getMeasuredWidth(), profilePic.getMeasuredWidth()));

                            JSONArray skillsTeachArray = insideArray.getJSONArray("skillsTeach");
                            JSONArray skillsLearnArray = insideArray.getJSONArray("skillsLearn");
                            org.apmem.tools.layouts.FlowLayout fTeach = (org.apmem.tools.layouts.FlowLayout) findViewById(R.id.skillTeachDynamicLinearLayout);
                            org.apmem.tools.layouts.FlowLayout fLearn = (org.apmem.tools.layouts.FlowLayout) findViewById(R.id.skillLearnDynamicLinearLayout);

                            for(int b=0;b<skillsTeachArray.length(); b++) {
                                RelativeLayout rLayout = (RelativeLayout) getLayoutInflater().inflate(R.layout.skill_bubble, null);
                                TextView tView = (TextView) getLayoutInflater().inflate(R.layout.skill_bubble_text, null);
                                tView.setText(skillsTeachArray.getString(b));
                                rLayout.addView(tView);
                                org.apmem.tools.layouts.FlowLayout.LayoutParams rLayoutParams = new org.apmem.tools.layouts.FlowLayout.LayoutParams(
                                        ViewGroup.LayoutParams.WRAP_CONTENT,
                                        ViewGroup.LayoutParams.WRAP_CONTENT
                                );
                                Resources r = getResources();
                                int fourDP = (int) Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, r.getDisplayMetrics()));
                                rLayoutParams.setMargins(0,0,fourDP,fourDP);
                                rLayout.setLayoutParams(rLayoutParams);
                                int sevenDP = (int) Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 7, r.getDisplayMetrics()));
                                tView.setPadding(sevenDP,sevenDP,sevenDP,sevenDP);
                                fTeach.addView(rLayout);
                            }
                            org.apmem.tools.layouts.FlowLayout skillsToTeachLinearLayout = (org.apmem.tools.layouts.FlowLayout) findViewById(R.id.skillTeachDynamicLinearLayout);
                            org.apmem.tools.layouts.FlowLayout skillsToLearnLinearLayout  = (org.apmem.tools.layouts.FlowLayout) findViewById(R.id.skillLearnDynamicLinearLayout);
                            if(skillsTeachArray.length()==0){
                                TextView t = new TextView(FindPeopleZoom.this);
                                t.setText("Please add some skills");
                                t.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                                fTeach.addView(t);
                            }
                            if(skillsLearnArray.length()==0){
                                TextView t = new TextView(FindPeopleZoom.this);
                                t.setText("Please add some skills");
                                t.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                                fLearn.addView(t);
                            }
                            for(int b=0;b<skillsLearnArray.length(); b++) {
                                RelativeLayout rLayout = (RelativeLayout) getLayoutInflater().inflate(R.layout.skill_bubble, null);
                                TextView tView = (TextView) getLayoutInflater().inflate(R.layout.skill_bubble_text, null);
                                System.out.println("skill: " + skillsLearnArray.getString(b));
                                tView.setText(skillsLearnArray.getString(b));
                                rLayout.addView(tView);
                                org.apmem.tools.layouts.FlowLayout.LayoutParams rLayoutParams = new org.apmem.tools.layouts.FlowLayout.LayoutParams(
                                        ViewGroup.LayoutParams.WRAP_CONTENT,
                                        ViewGroup.LayoutParams.WRAP_CONTENT
                                );
                                Resources r = getResources();
                                int fourDP = (int) Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, r.getDisplayMetrics()));
                                rLayoutParams.setMargins(0,0,fourDP,fourDP);
                                rLayout.setLayoutParams(rLayoutParams);
                                int sevenDP = (int) Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 7, r.getDisplayMetrics()));
                                tView.setPadding(sevenDP,sevenDP,sevenDP,sevenDP);
                                fLearn.addView(rLayout);
                            }
                            dialog.dismiss();
                        }
                    } catch (JSONException e) {
                        Log.e("JSONException", "Error: " + e.toString());
                    }
                }
            });
            task.setProgressDialog(FindPeopleZoom.this);
            task.setMessageOnDialog("Loading Profile...");
            task.setUrl("lat=" + latitude + "&lng=" + longitude + "1&distance=100000"); //need to put something because the ? is put before the url automatically
            System.out.println("VARVAR");
            ArrayList<String> list1 = new ArrayList<String>();
            list1.add("token");
            list1.add(token);
            ArrayList<String> list2 = new ArrayList<String>();
            list2.add("id");
            list2.add(id);
            ArrayList<String> list3 = new ArrayList<>();
            list3.add("match_id");
            System.out.println("token: " + token + " id: " + id + " match_id: " + match_id);
            list3.add(match_id);
            dialog = new ProgressDialog(FindPeopleZoom.this, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT);
            dialog.setMessage("Loading Profile...");
            dialog.show();
            task.execute(list1, list2, list3);
        }

    }

    @Override
    public void onLocationChanged(Location location) {
        longitude = Double.toString(location.getLongitude());
        latitude = Double.toString(location.getLatitude());
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}
