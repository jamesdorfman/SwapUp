package com.jamesdorfman.skillexchange;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.ArrayList;

public class SettingsScreen extends AppCompatActivity {

    SeekBar locationSlider;
    TextView locationText;

    Button maleButton;
    Button femaleButton;
    Button bothButton;
    Button saveButton;

    int defaultButtonColor;

    String gender;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_screen);

        final RelativeLayout rootLayout = (RelativeLayout) findViewById(R.id.rootLayout);
        rootLayout.setVisibility(View.INVISIBLE);

        getSupportActionBar().setDisplayOptions(android.support.v7.app.ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.center_title_layout);
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                TextView t = (TextView) findViewById(R.id.actionbar_textview);
                t.setText("Settings");
            }
        });
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.skillBorderColor)));

        final Drawable upArrow = getResources().getDrawable(R.drawable.abc_ic_ab_back_material);
        upArrow.setColorFilter(Color.parseColor("#FFFFFF"), PorterDuff.Mode.SRC_ATOP);
        getSupportActionBar().setHomeAsUpIndicator(upArrow);

        locationSlider = (SeekBar) findViewById(R.id.locationSlider);
        locationText = (TextView) findViewById(R.id.locationText);

        maleButton = (Button) findViewById(R.id.maleButton);
        femaleButton = (Button) findViewById(R.id.femaleButton);
        bothButton = (Button) findViewById(R.id.bothButton);

        saveButton = (Button) findViewById(R.id.saveButton);

        gender = "both";
        defaultButtonColor = maleButton.getDrawingCacheBackgroundColor();
        maleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                maleButton.setTextColor(Color.WHITE);
                maleButton.setBackgroundColor(getResources().getColor(R.color.primary));

                femaleButton.setTextColor(Color.BLACK);
                femaleButton.setBackgroundColor(Color.parseColor("#cccccc"));

                bothButton.setTextColor(Color.BLACK);
                bothButton.setBackgroundColor(Color.parseColor("#cccccc"));

                gender = "male";
            }
        });

        femaleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                femaleButton.setTextColor(Color.WHITE);
                femaleButton.setBackgroundColor(getResources().getColor(R.color.primary));

                maleButton.setTextColor(Color.BLACK);
                maleButton.setBackgroundColor(Color.parseColor("#cccccc"));

                bothButton.setTextColor(Color.BLACK);
                bothButton.setBackgroundColor(Color.parseColor("#cccccc"));

                gender = "female";
            }
        });

        bothButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bothButton.setTextColor(Color.WHITE);
                bothButton.setBackgroundColor(getResources().getColor(R.color.primary));

                femaleButton.setTextColor(Color.BLACK);
                femaleButton.setBackgroundColor(Color.parseColor("#cccccc"));

                maleButton.setTextColor(Color.BLACK);
                maleButton.setBackgroundColor(Color.parseColor("#cccccc"));

                gender = "both";
            }
        });
        //THE SLIDER STARTS AT 0 SO WE NEED TO ADD 1 TO ALL VALUES TAKEN FROM THE SLIDER
        locationSlider.setProgress(99);
        locationText.setText(Integer.toString(locationSlider.getProgress()+1) + " km");
        locationSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                locationText.setText(Integer.toString(locationSlider.getProgress()+1) + "km");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        Bundle inBundle = getIntent().getExtras();

        final String token = preferences.getString("token", null);
        final String id = preferences.getString("id", null);

        AsyncTaskRunner getTask = new AsyncTaskRunner(new CalledByAsyncTaskInterface() {
            @Override
            public void calledByAsyncTaskFunction(JSONObject reader) {
                try {
                    String status = reader.getString("status");
                    System.out.println("status is: " + status);
                    if (status.equals("failure")) {
                        Toast.makeText(getApplicationContext(), "Error saving bio. Please check your internet connectivity",
                                Toast.LENGTH_LONG).show();
                    } else {
                        String searchDistance = reader.getString("searchDistance");
                        String searchGender = reader.getString("searchGender");

                        locationSlider.setProgress(Integer.parseInt(searchDistance));
                        if(searchGender.equals("male")){
                            maleButton.setTextColor(Color.WHITE);
                            maleButton.setBackgroundColor(getResources().getColor(R.color.primary));

                            femaleButton.setTextColor(Color.BLACK);
                            femaleButton.setBackgroundColor(Color.parseColor("#cccccc"));

                            bothButton.setTextColor(Color.BLACK);
                            bothButton.setBackgroundColor(Color.parseColor("#cccccc"));

                            gender = "male";
                        }
                        else if(searchGender.equals("female")){
                            femaleButton.setTextColor(Color.WHITE);
                            femaleButton.setBackgroundColor(getResources().getColor(R.color.primary));

                            maleButton.setTextColor(Color.BLACK);
                            maleButton.setBackgroundColor(Color.parseColor("#cccccc"));

                            bothButton.setTextColor(Color.BLACK);
                            bothButton.setBackgroundColor(Color.parseColor("#cccccc"));

                            gender = "female";
                        }
                        else{
                            bothButton.setTextColor(Color.WHITE);
                            bothButton.setBackgroundColor(getResources().getColor(R.color.primary));

                            femaleButton.setTextColor(Color.BLACK);
                            femaleButton.setBackgroundColor(Color.parseColor("#cccccc"));

                            maleButton.setTextColor(Color.BLACK);
                            maleButton.setBackgroundColor(Color.parseColor("#cccccc"));

                            gender = "both";
                        }
                        rootLayout.setVisibility(View.VISIBLE);
                    }
                }
                catch (JSONException e) {
                    Log.e("JSONException", "Error: " + e.toString());
                }
            }
        });
        getTask.setProgressDialog(SettingsScreen.this);
        getTask.setMessageOnDialog("Saving...");
        getTask.setUrl("getSettings=true");
        ArrayList<String> list1 = new ArrayList<String>();
        list1.add("token");
        list1.add(token);
        ArrayList<String> list2 = new ArrayList<String>();
        list2.add("id");
        list2.add(id);
        getTask.execute(list1,list2);

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AsyncTaskRunner task = new AsyncTaskRunner(new CalledByAsyncTaskInterface() {
                    @Override
                    public void calledByAsyncTaskFunction(JSONObject reader) {
                        try {
                            String status = reader.getString("status");
                            System.out.println("status is: " + status);
                            if (status.equals("failure")) {
                                Toast.makeText(getApplicationContext(), "Error saving bio. Please check your internet connectivity",
                                        Toast.LENGTH_LONG).show();
                            } else {
                                Toast toast = Toast.makeText(SettingsScreen.this,"Settings were Saved",Toast.LENGTH_SHORT);
                                toast.show();
                                System.out.println("just testing just testing");
                                System.out.println(reader.toString());
                            }
                        }
                        catch (JSONException e) {
                            Log.e("JSONException", "Error: " + e.toString());
                        }
                    }
                });
                task.setProgressDialog(SettingsScreen.this);
                task.setMessageOnDialog("Saving...");
                EditText bioEditText = (EditText) findViewById(R.id.profileBioTextView);
                task.setUrl("searchDistance=" + locationSlider.getProgress() + "&searchGender=" + gender);
                ArrayList<String> list1 = new ArrayList<String>();
                list1.add("token");
                list1.add(token);
                ArrayList<String> list2 = new ArrayList<String>();
                list2.add("id");
                list2.add(id);
                task.execute(list1,list2);
            }
        });
    }
}
