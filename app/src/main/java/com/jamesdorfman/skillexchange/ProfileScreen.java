package com.jamesdorfman.skillexchange;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.content.Intent;
import android.content.SharedPreferences;

import android.preference.PreferenceManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.apmem.tools.layouts.*;
import org.apmem.tools.layouts.FlowLayout;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;

public class ProfileScreen extends AppCompatActivity {
    private ListView mDrawerList;
    private DrawerLayout mDrawerLayout;
    private ArrayAdapter<String> mAdapter;

    private ActionBarDrawerToggle mDrawerToggle;
    private String mActivityTitle;

    String email;
    String password;

    String userName;
    String firstName;
    String bio;

    String id;
    String birthday;
    String age;
    String gender;
    String token;

    TextView nameTextView;
    TextView ageTextView;
    TextView bioTextView;
    LinearLayout skillsToTeachLinearLayout;
    LinearLayout skillsToLearnLinearLayout;
    TextView defaultSkillBox;

    CircularImageView profilePictureUploadImageView;
    CircularImageView profilePicture;
    com.jamesdorfman.skillexchange.CircularImageView pickImageButton;

    private static int RESULT_LOAD_IMAGE = 1;
    private String KEY_IMAGE = "image";
    private String KEY_NAME = "name";

    static Bitmap bitmap;

    private int PICK_IMAGE_REQUEST = 1;

    InputStream inputStream;

    private static final int PICK_FILE_REQUEST = 1;
    private static final String TAG = ProfileScreen.class.getSimpleName();
    private String selectedFilePath;
    private String SERVER_URL = "http://jamesdorfman.com/skillExchange/uploadPicture.php";
    ProgressDialog dialog;

    String imagePath;

    String user_id;

    String fbImageUrl;
    public void saveData(View v) {
        AsyncTaskRunner task = new AsyncTaskRunner(new CalledByAsyncTaskInterface() {
            @Override
            public void calledByAsyncTaskFunction(JSONObject reader) {
                try {
                    String status = reader.getString("status");
                    System.out.println("status is: " + status);
                    if (status.equals("failure")) {
                        Toast.makeText(getApplicationContext(), "Error saving data. Please check your network connection.",
                                Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getApplicationContext(), "Data was successfully saved",
                                Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    Log.e("JSONException", "Error: " + e.toString());
                } // catch (JSONException e)
            }
        });
        task.setProgressDialog(ProfileScreen.this);
        task.setMessageOnDialog("Saving data...");
        String name = nameTextView.getText().toString();
        String bio = bioTextView.getText().toString();
        System.out.println("name is: " + name + " bio is " + bio);
        task.setUrl("setBio=" + bio.replaceAll(" ", "a"));
        task.execute();
    }

    public void editTeachSkillsScreen(View v) {
        Intent screen = new Intent(ProfileScreen.this, EditSkills.class);
        screen.putExtra("screenType", "teach");
        startActivity(screen);
    }

    public void editLearnSkillsScreen(View v) {
        Intent screen = new Intent(ProfileScreen.this, EditSkills.class);
        screen.putExtra("screenType", "learn");
        startActivity(screen);
    }

    public void editBioScreen(View v) {
        Intent screen = new Intent(ProfileScreen.this, EditBio.class);
        startActivity(screen);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        System.out.println("CREATING ACTIVITY!!!!!");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_screen);

        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        Bundle inBundle = getIntent().getExtras();
        final String name = preferences.getString("name", null);
        String surname = preferences.getString("surname", null);
        fbImageUrl = preferences.getString("imageUrl", null);
        System.out.println("fbImageUrl: " + fbImageUrl);
        id = preferences.getString("id", null);
        user_id = id;
        birthday = preferences.getString("birthday", null);
        gender = preferences.getString("gender", null);
        System.out.println("id: " + id + " gender: " + gender);
        userName = preferences.getString("fullName", null);
        System.out.println("user name: " + userName);
        token = preferences.getString("token", null);
        mDrawerList = (ListView) findViewById(R.id.navList);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mActivityTitle = getTitle().toString();

        addDrawerItems();
        setupDrawer();

        getSupportActionBar().setDisplayOptions(android.support.v7.app.ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.center_title_layout);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.skillBorderColor)));

        final Drawable upArrow = getResources().getDrawable(R.drawable.abc_ic_ab_back_material);
        upArrow.setColorFilter(Color.parseColor("#FFFFFF"), PorterDuff.Mode.SRC_ATOP);
        getSupportActionBar().setHomeAsUpIndicator(upArrow);

        SharedPreferences prefs = getSharedPreferences(LogInScreen.MY_PREFS_NAME, MODE_PRIVATE);
        email = prefs.getString("email", null);//"No name defined" is the default value.
        password = prefs.getString("password", null);//"No name defined" is the default value.
        System.out.println("email: " + email + " password: " + password);


        nameTextView = (TextView) findViewById(R.id.profileNameTextView);
        ageTextView = (TextView) findViewById(R.id.profileAgeTextView);
        bioTextView = (TextView) findViewById(R.id.profileBioTextView);

        profilePictureUploadImageView = (CircularImageView) findViewById(R.id.profilePictureUploadImageView);
        profilePicture = (CircularImageView) findViewById(R.id.profilePicture);
        pickImageButton = (com.jamesdorfman.skillexchange.CircularImageView) findViewById(R.id.editProfilePictureBackground);

        AsyncTaskRunner task = new AsyncTaskRunner(new CalledByAsyncTaskInterface() {
            @Override
            public void calledByAsyncTaskFunction(JSONObject reader) {
                try {
                    String status = reader.getString("status");
                    System.out.println("status is: " + status);
                    if (status.equals("failure")) {
                        Toast.makeText(getApplicationContext(), "Error getting profile data from server. Please restart the app",
                                Toast.LENGTH_LONG).show();
                    } else {
                        firstName = userName.substring(0, userName.indexOf(' '));
                        //Captialize first letter of name
                        firstName = firstName.substring(0, 1).toUpperCase() + firstName.substring(1);
                        firstName = firstName + ", ";
                        nameTextView.setText(firstName);
                        ageTextView.setText(age);
                        bio = reader.getString("bio");
                        if (bio == "" || bio == null) {
                            bio = "Write a short bio about yourself here. Try to seem friendly and helpful, which will help you get more matches.";
                        }
                        bioTextView.setText(bio);

                        JSONArray skillsTeachArray = reader.getJSONArray("teachSkills");
                        JSONArray skillsLearnArray = reader.getJSONArray("learnSkills");
                        FlowLayout fTeach = (FlowLayout) findViewById(R.id.skillTeachDynamicLinearLayout);
                        FlowLayout fLearn = (FlowLayout) findViewById(R.id.skillLearnDynamicLinearLayout);

                        user_id = reader.getString("id");
                        for (int b = 0; b < skillsTeachArray.length(); b++) {
                            RelativeLayout rLayout = (RelativeLayout) getLayoutInflater().inflate(R.layout.skill_bubble, null);
                            TextView tView = (TextView) getLayoutInflater().inflate(R.layout.skill_bubble_text, null);
                            tView.setText(skillsTeachArray.getJSONObject(b).getString("skill"));
                            rLayout.addView(tView);
                            org.apmem.tools.layouts.FlowLayout.LayoutParams rLayoutParams = new org.apmem.tools.layouts.FlowLayout.LayoutParams(
                                    ViewGroup.LayoutParams.WRAP_CONTENT,
                                    ViewGroup.LayoutParams.WRAP_CONTENT
                            );
                            Resources r = getResources();
                            int fourDP = (int) Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, r.getDisplayMetrics()));
                            rLayoutParams.setMargins(0, 0, fourDP, fourDP);
                            rLayout.setLayoutParams(rLayoutParams);
                            int sevenDP = (int) Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 7, r.getDisplayMetrics()));
                            tView.setPadding(sevenDP, sevenDP, sevenDP, sevenDP);
                            fTeach.addView(rLayout);
                        }
                        FlowLayout skillsToTeachLinearLayout = (FlowLayout) findViewById(R.id.skillTeachDynamicLinearLayout);
                        FlowLayout skillsToLearnLinearLayout = (FlowLayout) findViewById(R.id.skillLearnDynamicLinearLayout);
                        if (skillsTeachArray.length() == 0) {
                            TextView t = new TextView(ProfileScreen.this);
                            t.setText("Please add some skills");
                            t.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                            fTeach.addView(t);
                        }
                        if (skillsLearnArray.length() == 0) {
                            TextView t = new TextView(ProfileScreen.this);
                            t.setText("Please add some skills");
                            t.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                            fLearn.addView(t);
                        }
                        for (int b = 0; b < skillsLearnArray.length(); b++) {
                            RelativeLayout rLayout = (RelativeLayout) getLayoutInflater().inflate(R.layout.skill_bubble, null);
                            TextView tView = (TextView) getLayoutInflater().inflate(R.layout.skill_bubble_text, null);
                            System.out.println("skill: " + skillsLearnArray.getJSONObject(b).getString("skill"));
                            tView.setText(skillsLearnArray.getJSONObject(b).getString("skill"));
                            rLayout.addView(tView);
                            org.apmem.tools.layouts.FlowLayout.LayoutParams rLayoutParams = new org.apmem.tools.layouts.FlowLayout.LayoutParams(
                                    ViewGroup.LayoutParams.WRAP_CONTENT,
                                    ViewGroup.LayoutParams.WRAP_CONTENT
                            );
                            Resources r = getResources();
                            int fourDP = (int) Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, r.getDisplayMetrics()));
                            rLayoutParams.setMargins(0, 0, fourDP, fourDP);
                            rLayout.setLayoutParams(rLayoutParams);
                            int sevenDP = (int) Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 7, r.getDisplayMetrics()));
                            tView.setPadding(sevenDP, sevenDP, sevenDP, sevenDP);
                            fLearn.addView(rLayout);
                        }


//                        bioTextView.setText(bio);

                        JSONArray teachSkills = reader.getJSONArray("teachSkills");
                        JSONArray learnSkills = reader.getJSONArray("learnSkills");

                        //views are simply there for design purposes
                        //skillsToTeachLinearLayout.removeAllViews();
                        //skillsToLearnLinearLayout.removeAllViews();

                    /*    for (int i = 0; i < teachSkills.length(); i++) {
                            JSONObject object = teachSkills.getJSONObject(i);
                            String objectString = object.getString("skill");
                            TextView textView = (TextView) getLayoutInflater().inflate(R.layout.skill_textview, null);
                            ;
                            textView.setText(objectString);
                            int dpInPixels = (int) getResources().getDimension(R.dimen.skill_textview_dimen);
                            textView.setHeight(dpInPixels);
                            skillsToTeachLinearLayout.addView(textView);
                        }

                        for (int i = 0; i < learnSkills.length(); i++) {
                            JSONObject object = learnSkills.getJSONObject(i);
                            String objectString = object.getString("skill");
                            TextView textView = (TextView) getLayoutInflater().inflate(R.layout.skill_textview, null);
                            ;
                            textView.setText(objectString);
                            int dpInPixels = (int) getResources().getDimension(R.dimen.skill_textview_dimen);
                            textView.setHeight(dpInPixels);
                            skillsToLearnLinearLayout.addView(textView);
                        }*/
                    }
                } catch (JSONException e) {
                    Log.e("JSONException", "Error no val?: " + e.toString());
                }
            }
        });
        task.setProgressDialog(ProfileScreen.this);
        task.setMessageOnDialog("Fetching data from server");
        task.setUrl("getSkills=true&bio=true");
        System.out.println("herehere123");
        //task.setUrl("j=j");
        ArrayList<String> list1 = new ArrayList<String>();
        list1.add("token");
        list1.add(token);
        ArrayList<String> list2 = new ArrayList<String>();
        list2.add("id");
        list2.add(id);
        ArrayList<String> list3 = new ArrayList<String>();
        list3.add("gender");
        list3.add(gender);
        ArrayList<String> list4 = new ArrayList<String>();
        list4.add("age");
        //  System.out.println(birthday.length() + " " + birthday);
/*        int day = Integer.parseInt(birthday.substring(0, 2));
        int month = Integer.parseInt(birthday.substring(3, 5));
        int year = Integer.parseInt(birthday.substring(6, 10));*/
//        Date currentDate = Calendar.getInstance().getTime();
        //       Date birthdate = new Date(year,month,day);
        //     Long time= currentDate.getTime() / 1000 - birthdate.getTime() / 1000;

        // age = Integer.toString(Math.round(time) / 31536000);
        //System.out.println("print: " + day + "/" + month + "/" + year);
        //age = Integer.toString(getAge(year, month, day));
        age = "17";
        System.out.println("AGE: " + age);
        list4.add(age);
        ArrayList<String> list5 = new ArrayList<String>();
        list5.add("name");
        list5.add(userName);
        ArrayList<String> list6 = new ArrayList<String>();
        list6.add("fbImageUrl");
        list6.add(fbImageUrl);
        System.out.println("list6: " + list6.get(0) + " : " + list6.get(1));
        task.execute(list1, list2, list3, list4, list5, list6);
        //      bioTextView.setText(bio);
        System.out.println("THIS IS BIO: " + bio);

        //CHECK IF FILE EXISTS
        try {
            System.out.println("about to print an image: " + "http://jamesdorfman.com/skillExchange/user_images/" + user_id + ".jpg");
            Picasso.with(ProfileScreen.this).load(fbImageUrl).resize(300,300).memoryPolicy(MemoryPolicy.NO_CACHE ).networkPolicy(NetworkPolicy.NO_CACHE).error(R.drawable.default_profile_picture).into(new Target() {
                @Override
                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                    ImageView pp = (ImageView) findViewById(R.id.profilePicture);
                    pp.setImageBitmap(bitmap);
                }

                @Override
                public void onBitmapFailed(Drawable errorDrawable) {
                    ImageView pp = (ImageView) findViewById(R.id.profilePicture);
                    pp.setImageResource(R.drawable.default_profile_picture);
                }

                @Override
                public void onPrepareLoad(Drawable placeHolderDrawable) {

                }

            });
        } catch (Exception e) {

        }
        if(Globals.homeLoaded==0) {
            Intent i = new Intent(ProfileScreen.this, FindPeopleScreen.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            ProfileScreen.this.startActivity(i);
            finish();
        }
    }

    public int getAge(int year, int month, int day) {
        Calendar currentDate = Calendar.getInstance();
        int curYear = currentDate.get(Calendar.YEAR);
        int curMonth = currentDate.get(Calendar.MONTH);
        int curDay = currentDate.get(Calendar.DATE);
        System.out.println(curDay + "/" + curMonth + "/" + curYear);
        int years = curYear - year;
        if (month > curMonth) {
            years--;
        } else if (month == curMonth) {
            if (day >= curDay) {
                years--;
            }
        }

        return years;
    }

    private void showFileChooser() {
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, 1);
    }

    private String saveToInternalStorage(Bitmap bitmapImage) throws IOException {
        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        // path to /data/data/yourapp/app_data/imageDir
        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        // Create imageDir
        File mypath = new File(directory, email + ".jpg");

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(mypath);
            // Use the compress method on the BitMap object to write image to the OutputStream
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            fos.close();
        }
        return directory.getAbsolutePath();
    }

    private void addDrawerItems() {
        String[] osArray = {"Home", "Profile", "Matches"};
        mAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, osArray) {

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {

                View view = super.getView(position, convertView, parent);
                TextView text = (TextView) view.findViewById(android.R.id.text1);

                text.setTextColor(Color.BLACK);

                return view;
            }
        };
        mDrawerList.setAdapter(mAdapter);
        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        Intent mainActivity = new Intent(ProfileScreen.this, FindPeopleScreen.class);
                        startActivity(mainActivity);
                        finish();
                        break;
                    case 1:
                        mDrawerLayout.closeDrawers();
                        break;
                    case 2:
                        Intent matchesActivity = new Intent(ProfileScreen.this, MatchesScreen.class);
                        startActivity(matchesActivity);
                        finish();
                        break;
                    case 3:
                        break;
                }
            }
        });
    }

    private void setupDrawer() {
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.drawer_open, R.string.drawer_close) {

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                getSupportActionBar().setTitle("SwapUp");
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                getSupportActionBar().setTitle(mActivityTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };

        mDrawerToggle.setDrawerIndicatorEnabled(true);
        mDrawerLayout.setDrawerListener(mDrawerToggle);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_profile_screen, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        // Activate the navigation drawer toggle
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent i = new Intent(ProfileScreen.this,SettingsScreen.class);
            startActivity(i);
        } else if (id == R.id.action_log_out) {
            SaveSharedPreference.deletePreferences(ProfileScreen.this);
            Intent intent = new Intent(ProfileScreen.this, AccountScreen.class);
            startActivity(intent);
            logout();
            finish();
        }
        else if (id == R.id.about_screen_button){
            Intent i = new Intent(ProfileScreen.this, AboutScreen.class);
            startActivity(i);
        }
        return super.onOptionsItemSelected(item);
    }

    public void logout() {
        LoginManager.getInstance().logOut();
        Intent login = new Intent(ProfileScreen.this, AccountScreen.class);
        startActivity(login);
        finish();
    }

    public void onStop() {
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
                        //Toast toast = Toast.makeText(ProfileScreen.this,"Bio saved succesfully",Toast.LENGTH_SHORT);
                        //toast.show();
                    }
                }
                catch (JSONException e) {
                    Log.e("JSONException", "Error: " + e.toString());
                }
            }
        });
        task.setProgressDialog(ProfileScreen.this);
        task.setMessageOnDialog("Saving...");
        EditText bioEditText = (EditText) findViewById(R.id.profileBioTextView);
        task.setUrl("setBio=" + bioEditText.getText().toString().replaceAll(" ", "%20").replaceAll("\n",""));
        ArrayList<String> list1 = new ArrayList<String>();
        list1.add("token");
        list1.add(token);
        ArrayList<String> list2 = new ArrayList<String>();
        list2.add("id");
        list2.add(id);
        task.execute(list1,list2);
        super.onStop();
    }

}