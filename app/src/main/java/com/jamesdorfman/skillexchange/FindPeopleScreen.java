package com.jamesdorfman.skillexchange;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.util.Pair;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import android.content.res.Configuration;
import android.graphics.Color;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.lorentzos.flingswipe.*;
import com.squareup.picasso.Picasso;

import org.apmem.tools.layouts.FlowLayout;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/*
    Class which gets user's location, and displays cards of potential
    matches for them to swipe on
*/
public class FindPeopleScreen extends AppCompatActivity implements LocationListener{

    // for slider out menu
    private ListView mDrawerList;
    private DrawerLayout mDrawerLayout;
    private ArrayAdapter<String> mAdapter;
    private ActionBarDrawerToggle mDrawerToggle;
    private String mActivityTitle;

    //for gps location access
    LocationManager locationManager;
    String provider;

    String email;
    String password;

    String latitude;
    String longitude;

    String nameString;
    String ageString;

    TextView profileNameTextView;
    TextView profileAgeTextView;
    FlowLayout skillTeachDynamicLinearLayout;
    FlowLayout skillLearnDynamicLinearLayout;


    private ArrayAdapter<Pair<String, String>> arrayAdapter;
    private int i;
    final Handler h = new Handler();

    public static MyAppAdapter myAppAdapter;
    public static ViewHolder viewHolder;
    private ArrayList<SwipeData> al;
    private SwipeFlingAdapterView flingContainer;

    String token;
    String id;

    public JSONObject globalReader;

    public ArrayList<String> usedIds;

    public Boolean isNetworkConnected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if(Globals.homeLoaded==0) {
            Intent i = new Intent(FindPeopleScreen.this, ProfileScreen.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            FindPeopleScreen.this.startActivity(i);
            Globals.homeLoaded = 1;
            finish();
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_people_screen);

        isNetworkConnected = true;

        mDrawerList = (ListView) findViewById(R.id.navList);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mActivityTitle = getTitle().toString();
        profileNameTextView = (TextView) findViewById(R.id.profileNameTextView);
        profileAgeTextView = (TextView) findViewById(R.id.profileAgeTextView);
        skillTeachDynamicLinearLayout = (FlowLayout) findViewById(R.id.skillTeachDynamicLinearLayout);
        skillLearnDynamicLinearLayout = (FlowLayout) findViewById(R.id.skillLearnDynamicLinearLayout);

        addDrawerItems();
        setupDrawer();

        getSupportActionBar().setDisplayOptions(android.support.v7.app.ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.center_title_layout);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.skillBorderColor)));

        final Drawable upArrow = getResources().getDrawable(R.drawable.abc_ic_ab_back_material);
        upArrow.setColorFilter(Color.parseColor("#FFFFFF"), PorterDuff.Mode.SRC_ATOP);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        Bundle inBundle = getIntent().getExtras();

        token = preferences.getString("token", null);
        id = preferences.getString("id", null);

        getSupportActionBar().setHomeAsUpIndicator(upArrow);

        // Getting LocationManager object
        locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);

        // Creating an empty criteria object
        Criteria criteria = new Criteria();

        // Getting the name of the provider that meets the criteria
        provider = locationManager.getBestProvider(criteria, false);

        usedIds = new ArrayList<String>();

        if(provider!=null && !provider.equals("")){

            // Get the location from the given provider
            Location location = locationManager.getLastKnownLocation(provider);

            locationManager.requestLocationUpdates(provider, 20000, 1, this);

            if(location!=null)
                onLocationChanged(location);
            else
                Toast.makeText(getBaseContext(), "Location can't be retrieved", Toast.LENGTH_SHORT).show();

        }else{
            Toast.makeText(getBaseContext(), "No Provider Found", Toast.LENGTH_SHORT).show();
        }

        final int delay = 250; //milliseconds

        h.postDelayed(new Runnable(){
            String currentDots = "Searching for friends in your area";
            TextView loadingView = (TextView) findViewById(R.id.swipeCardsBackground);

            public void run(){
                if(!currentDots.equals("Searching for friends in your area...")){
                    this.loadingView.setText(this.currentDots);
                    this.currentDots = this.currentDots + ".";
                }
                else{
                    this.loadingView.setText(this.currentDots);
                    this.currentDots = "Searching for friends in your area";
                }
                h.postDelayed(this, delay);
            }
        }, delay);
        if(longitude != null) {
            AsyncTaskRunner task = new AsyncTaskRunner(new CalledByAsyncTaskInterface() {
                @Override
                public void calledByAsyncTaskFunction(JSONObject reader) {
                    try {
                        System.out.println("trying");
                        String status = reader.getString("status");
                        System.out.println("status is: " + status);

                        if (status.equals("failure")) {
                            Toast.makeText(getApplicationContext(), "Error getting profile data from server. Please restart the app",
                                    Toast.LENGTH_LONG).show();
                        }
                        else{
                            System.out.println("name: " + reader.getString("matches"));
                            //myAppAdapter.notifyDataSetChanged();
                            globalReader = reader;
                            drawCards(reader);
                        }
                    } catch (JSONException e) {
                        Log.e("JSONException", "Error: " + e.toString());
                    }
                }
            });
            task.setProgressDialog(FindPeopleScreen.this);
            task.setMessageOnDialog("Fetching data from server");
            task.setUrl("email=" + "null" + "&pword=" + "null" + "&getUsers=true&lat=" + latitude + "&lng=" + longitude + "1&distance=100000");

            ArrayList<String> list1 = new ArrayList<String>();
            list1.add("token");
            list1.add(token);

            ArrayList<String> list2 = new ArrayList<String>();
            list2.add("id");
            list2.add(id);
            task.execute(list1, list2);
            }
            else{
                Toast t = Toast.makeText(FindPeopleScreen.this,"Please turn on location services to use this app",Toast.LENGTH_LONG);
                t.show();
            }
    }

    public static void removeBackground(){
        viewHolder.background.setVisibility(View.GONE);
        myAppAdapter.notifyDataSetChanged();
    }

    public class MyAppAdapter extends BaseAdapter {


        public ArrayList<SwipeData> parkingList;
        public Context context;
        public ArrayList<RelativeLayout> rLayouts;

        public MyAppAdapter(ArrayList<SwipeData> apps, Context context) {
            this.parkingList = apps;
            this.context = context;
        }

        @Override
        public int getCount() {
            return parkingList.size();
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            View rowView = convertView;


            if (rowView == null) {

                LayoutInflater inflater = getLayoutInflater();
                rowView = inflater.inflate(R.layout.find_people_item, parent, false);
                // configure view holder
                viewHolder = new ViewHolder();
                viewHolder.DataText = (TextView) rowView.findViewById(R.id.itemBookText);
                viewHolder.distanceView = (TextView) rowView.findViewById(R.id.distanceView);
                viewHolder.background = (FrameLayout) rowView.findViewById(R.id.itemFrameLayoutBackground);
                viewHolder.cardImage = (ImageView) rowView.findViewById(R.id.itemCardImage);
                viewHolder.cardImage = (ImageView) rowView.findViewById(R.id.itemCardImage);
                viewHolder.flowLayout = (FlowLayout) rowView.findViewById(R.id.skillLearnDynamicLinearLayout);
                viewHolder.viewMoreButton = (Button) rowView.findViewById(R.id.viewMoreButton);
                rowView.setTag(viewHolder);



            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            viewHolder.DataText.setText(parkingList.get(position).getName() + "");
            System.out.println("DISTANCE: " + parkingList.get(position).getDistance());
            viewHolder.distanceView.setText(Integer.toString((int)Float.parseFloat(parkingList.get(position).getDistance())) + "km away");
            ArrayList<String> skillsArray = parkingList.get(position).getSkills();
            for(int b=0;b<skillsArray.size(); b++) {
                RelativeLayout rLayout = (RelativeLayout) getLayoutInflater().inflate(R.layout.skill_bubble, null);
                TextView tView = (TextView) getLayoutInflater().inflate(R.layout.skill_bubble_text, null);
                tView.setText(skillsArray.get(b));
                rLayout.addView(tView);
                FlowLayout.LayoutParams rLayoutParams = new FlowLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );
                Resources r = getResources();
                int fourDP = (int) Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, r.getDisplayMetrics()));
                rLayoutParams.setMargins(0,0,fourDP,fourDP);
                rLayout.setLayoutParams(rLayoutParams);
                int sevenDP = (int) Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 7, r.getDisplayMetrics()));
                tView.setPadding(sevenDP,sevenDP,sevenDP,sevenDP);
                viewHolder.flowLayout.addView(rLayout);
                viewHolder.viewMoreButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(FindPeopleScreen.this,FindPeopleZoom.class);
                        Globals.iv = viewHolder.cardImage;
                        Bundle extras = new Bundle();
                        Bitmap viewHolderBitmap = ((BitmapDrawable)viewHolder.cardImage.getDrawable()).getBitmap();
                        final float scale = getResources().getDisplayMetrics().density;
                        int int_dp = 250;
                        int dp = (int) (int_dp * scale + 0.5f);
                        extras.putString("id",parkingList.get(position).getMatchId());
                        System.out.println("match_id: " + parkingList.get(position).getMatchId());
                        i.putExtras(extras);
                        startActivity(i);
                    }
                });

            }
            Picasso.with(FindPeopleScreen.this).load(parkingList.get(position).getImagePath()).error(R.drawable.default_profile_picture).fit().centerCrop().into(viewHolder.cardImage);
            return rowView;
        }
    }

    public static class ViewHolder{
        public static FrameLayout background;
        public TextView DataText;
        public TextView distanceView;
        public ImageView cardImage;
        public FlowLayout flowLayout;
        public String imageUrl;
        public Button viewMoreButton;
    }

    public void right(View v) {
        //Trigger the right event manually.

        flingContainer.getTopCardListener().selectRight();
    }

    public void left(View v) {
        flingContainer.getTopCardListener().selectLeft();
    }

    @Override
    public void onLocationChanged(Location location) {
        longitude = Double.toString(location.getLongitude());
        latitude = Double.toString(location.getLatitude());
    }

    @Override
    public void onProviderDisabled(String provider) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onProviderEnabled(String provider) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // TODO Auto-generated method stub
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
                        mDrawerLayout.closeDrawers();
                        break;
                    case 1:
                        Intent profileActivity = new Intent(FindPeopleScreen.this, ProfileScreen.class);
                        startActivity(profileActivity);
                        break;
                    case 2:
                        Intent matchesActivity = new Intent(FindPeopleScreen.this, MatchesScreen.class);
                        startActivity(matchesActivity);
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
            Intent i = new Intent(FindPeopleScreen.this,SettingsScreen.class);
            startActivity(i);
        } else if (id == R.id.action_log_out) {
            SaveSharedPreference.deletePreferences(FindPeopleScreen.this);
            Intent intent = new Intent(FindPeopleScreen.this, AccountScreen.class);
            startActivity(intent);
            finish();
        }
        else if (id == R.id.about_screen_button){
            Intent i = new Intent(FindPeopleScreen.this, AboutScreen.class);
            startActivity(i);
        }
        return super.onOptionsItemSelected(item);
    }

    public void drawMatchScreen(JSONObject insideArray, String nameString, String ageString) {
        System.out.println("here!");
        try {
            for (int m = 0; m < 2; m++) {
                JSONArray skillsArray;
                int bgColor;
                FlowLayout dynamicLinearLayout;
                if (m == 0) {
                    skillsArray = insideArray.getJSONArray("skillsTeach");
                    bgColor = Color.parseColor("#ea2558");
                    dynamicLinearLayout = skillTeachDynamicLinearLayout;
                } else {
                    skillsArray = insideArray.getJSONArray("skillsLearn");
                    bgColor = Color.parseColor("#f26522");
                    dynamicLinearLayout = skillLearnDynamicLinearLayout;
                }
                profileNameTextView.setText(nameString);
                profileAgeTextView.setText(ageString);

                TextView[] skillsTextViewArray = new TextView[skillsArray.length()];
                RelativeLayout[] skillsRelLayArray = new RelativeLayout[skillsArray.length()];
                if(dynamicLinearLayout.getChildCount() > 0) {
                    dynamicLinearLayout.removeAllViews();
                }
                for (int j = 0; j < skillsArray.length(); j++) {
                    skillsTextViewArray[j] = new TextView(FindPeopleScreen.this);
                    skillsRelLayArray[j] = new RelativeLayout(FindPeopleScreen.this);
                    String textViewString = skillsArray.get(j).toString().substring(0, 1).toUpperCase() + skillsArray.get(j).toString().substring(1);
                    skillsTextViewArray[j].setText(textViewString);
                    skillsTextViewArray[j].setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    int padding_in_dp = 10;  // 10 dps
                    final float scale = getResources().getDisplayMetrics().density;
                    int padding_in_px = (int) (padding_in_dp * scale + 0.5f);
                    skillsTextViewArray[j].setPadding(padding_in_px, padding_in_px, padding_in_px, padding_in_px);

                    skillsTextViewArray[j].setTextAppearance(FindPeopleScreen.this, R.style.skillSwipeTextView);
                    LinearLayout.LayoutParams relativeParams = new LinearLayout.LayoutParams(
                            new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.WRAP_CONTENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT));
                    padding_in_dp = 10;  // 1 dps
                    padding_in_px = (int) (padding_in_dp * scale + 0.5f);
                    relativeParams.setMargins(0, 0, 200, 100);
                    skillsRelLayArray[j].setLayoutParams(relativeParams);
                    skillsRelLayArray[j].requestLayout();
                    skillsRelLayArray[j].setBackgroundColor(bgColor);
                    skillsRelLayArray[j].addView(skillsTextViewArray[j]);

                    dynamicLinearLayout.addView(skillsRelLayArray[j]);
                }
            }
        } catch (JSONException e){

        }
    }

    public void drawCards(JSONObject reader) throws JSONException {
        if(reader.getString("matches").equals("false")){
            TextView t = (TextView) findViewById(R.id.swipeCardsBackground);
            h.removeCallbacksAndMessages(null);
            t.setText("No users were found that match your skills in your area.\n" +
                    "Try increasing your search radius in the settings screen or invite more of your friends to " +
                    "use the app!");
        }
        else {
            JSONArray matchesArray = reader.getJSONArray("matchesArray");

            if (matchesArray.length() == 0) {
                h.removeCallbacksAndMessages(null);
                TextView t = (TextView) findViewById(R.id.swipeCardsBackground);
                t.setText("No users were found that match your skills in your area.\n\n" +
                        "Try increasing your search radius in the settings screen or invite more of your friends to " +
                        "use the app!");
            } else {
                //drawMatchScreen(insideArray,nameString,ageString);
                flingContainer = (SwipeFlingAdapterView) findViewById(R.id.frame);
                al = new ArrayList<>();
                ImageView cardIV = (ImageView) findViewById(R.id.itemCardImage);
                FlowLayout skillLayout = (FlowLayout) findViewById(R.id.skillLearnDynamicLinearLayout);
                for (int x = 0; x < matchesArray.length(); x++) {
                    JSONObject insideArray = matchesArray.getJSONObject(x);
                    if (!usedIds.contains(insideArray.getString("id"))) {
                        nameString = insideArray.getString("name");
                        String fbImageUrl = insideArray.getString("fbImageUrl");
                        String[] names = nameString.split(" ");
                        String p1 = names[0];
                        p1 = p1.substring(0, 1).toUpperCase() + p1.substring(1);
                        nameString = p1;
                        ageString = insideArray.getString("age");
                        nameString = nameString + ", " + ageString;
                        final String match_id = insideArray.getString("id");
                        RelativeLayout rLayout = (RelativeLayout) getLayoutInflater().inflate(R.layout.skill_bubble, null);
                        JSONArray skillsArray = insideArray.getJSONArray("skillsTeach");
                        ArrayList<String> skills = new ArrayList<String>();
                        for (int z = 0; z < skillsArray.length(); z++) {
                            skills.add(skillsArray.get(z).toString());
                        }
                        al.add(new SwipeData(fbImageUrl, nameString, match_id, insideArray.getString("distance"), skills));
                    }
                }
                myAppAdapter = new MyAppAdapter(al, FindPeopleScreen.this);
                flingContainer.setAdapter(myAppAdapter);
                myAppAdapter.notifyDataSetChanged();
                flingContainer.setFlingListener(new SwipeFlingAdapterView.onFlingListener() {
                    @Override
                    public void removeFirstObjectInAdapter() {

                    }

                    @Override
                    public void onLeftCardExit(Object dataObject) {
                        usedIds.add(al.get(0).getMatchId());
                        myAppAdapter.notifyDataSetChanged();
                        AsyncTaskRunner task5 = new AsyncTaskRunner(new CalledByAsyncTaskInterface() {
                            @Override
                            public void calledByAsyncTaskFunction(JSONObject reader) {
                                try {
                                    String status = reader.getString("status");
                                    System.out.println("status is: " + status);

                                    if (status.equals("failure")) {
                                        //Toast.makeText(getApplicationContext(), "Not a match",
                                        //      Toast.LENGTH_LONG).show();
                                        System.out.println("not a match");
                                    } else {
                                        globalReader = reader;
                                        drawCards(reader);
                                    }
                                } catch (JSONException e) {
                                    Log.e("JSONException", "Error: " + e.toString());
                                }
                            }
                        });
                        task5.setProgressDialog(FindPeopleScreen.this);
                        task5.setMessageOnDialog("Fetching data from server");
                        task5.setUrl("&setMatch=false");

                        ArrayList<String> list1 = new ArrayList<String>();
                        list1.add("token");
                        list1.add(token);

                        ArrayList<String> list2 = new ArrayList<String>();
                        list2.add("id");
                        list2.add(id);

                        ArrayList<String> list3 = new ArrayList<String>();
                        list3.add("match_id");
                        list3.add(al.get(0).getMatchId());

                        task5.execute(list1, list2, list3);
                        al.remove(0);
                    }

                    @Override
                    public void onRightCardExit(Object dataObject) {
                        myAppAdapter.notifyDataSetChanged();

                        usedIds.add(al.get(0).getMatchId());

                        ImageView cardIV = (ImageView) findViewById(R.id.itemCardImage);
                        System.out.println("Right match_id" + al.get(0).getMatchId());

                        AsyncTaskRunner task = new AsyncTaskRunner(new CalledByAsyncTaskInterface() {
                            @Override
                            public void calledByAsyncTaskFunction(JSONObject reader) {
                                try {
                                    System.out.println("trying");
                                    String status = reader.getString("status");
                                    System.out.println("status is: " + status);

                                    if (status.equals("failure")) {
                                        Toast.makeText(getApplicationContext(), "Not a match",
                                                Toast.LENGTH_LONG).show();
                                    } else {
                                        if (status.equals("success")) {
                                            System.out.println("THERE IS A MATCH!");
                                        }
                                        //myAppAdapter.notifyDataSetChanged();
                                        globalReader = reader;
                                        drawCards(reader);
                                    }
                                } catch (JSONException e) {
                                    Log.e("JSONException", "Error: " + e.toString());
                                }
                            }
                        });
                        task.setProgressDialog(FindPeopleScreen.this);
                        task.setMessageOnDialog("Fetching data from server");
                        task.setUrl("&setMatch=true");

                        ArrayList<String> list1 = new ArrayList<String>();
                        list1.add("token");
                        list1.add(token);

                        ArrayList<String> list2 = new ArrayList<String>();
                        list2.add("id");
                        list2.add(id);

                        ArrayList<String> list3 = new ArrayList<String>();
                        list3.add("match_id");
                        list3.add(al.get(0).getMatchId());

                        task.execute(list1, list2, list3);

                        al.remove(0);
                    }

                    @Override
                    public void onAdapterAboutToEmpty(int itemsInAdapter) {
                        //LOAD MORE ITEMS
                        //PUT IT IN ASYNC TASK
                        Log.d("TAG", "loading more items");
                        ArrayList<String> x4 = new ArrayList<String>();
                        x4.add("piano");
                        x4.add("guitar");
                        Log.d("LIST", "notified");
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
                                        System.out.println("name: " + reader.getString("matches"));
                                        //myAppAdapter.notifyDataSetChanged();
                                        globalReader = reader;
                                        drawCards(reader);
                                    }
                                } catch (JSONException e) {
                                    Log.e("JSONException", "Error: " + e.toString());
                                }
                            }
                        });
                        task.setProgressDialog(FindPeopleScreen.this);
                        task.setMessageOnDialog("Fetching data from server");
                        task.setUrl("email=" + email + "&pword=" + password + "&getUsers=true&lat=" + latitude + "&lng=" + longitude + "1&distance=100000");

                        ArrayList<String> list1 = new ArrayList<String>();
                        list1.add("token");
                        list1.add(token);

                        ArrayList<String> list2 = new ArrayList<String>();
                        list2.add("id");
                        list2.add(id);
                        task.execute(list1, list2);
                    }

                    @Override
                    public void onScroll(float scrollProgressPercent) {

                        View view = flingContainer.getSelectedView();
                        //view.findViewById(R.id.itemFrameLayoutBackground).setAlpha(0);
                        view.findViewById(R.id.itemSwipeRightIndicator).setAlpha(scrollProgressPercent < 0 ? -scrollProgressPercent : 0);
                        view.findViewById(R.id.itemSwipeLeftIndicator).setAlpha(scrollProgressPercent > 0 ? scrollProgressPercent : 0);
                    }
                });


                // Optionally add an OnItemClickListener
                flingContainer.setOnItemClickListener(new SwipeFlingAdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClicked(int itemPosition, Object dataObject) {

                        View view = flingContainer.getSelectedView();
                        view.findViewById(R.id.itemFrameLayoutBackground).setAlpha(0);

                        myAppAdapter.notifyDataSetChanged();
                    }
                });

                // Optionally add an OnItemClickListener
                flingContainer.setOnItemClickListener(new SwipeFlingAdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClicked(int itemPosition, Object dataObject) {
                        //Toast.makeText(FindPeopleScreen.this, "Clicked!", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    }

    private BroadcastReceiver networkStateReceiver=new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            ConnectivityManager connManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
            if(connManager!=null && connManager.getActiveNetworkInfo() != null){
                System.out.println("WE ARE CONNECTED TO THE INTERNET");
                if(!isNetworkConnected) {
                    System.out.println("connected");
                    Intent i = getIntent();
                    finish();
                    startActivity(i);
                    overridePendingTransition(0,0);
                    Toast.makeText(FindPeopleScreen.this,"You have been reconnected to the internet",Toast.LENGTH_LONG).show();
                }
            }
            else{
                System.out.println("DISCONNECTED FROM INTERNET");
                Toast.makeText(FindPeopleScreen.this,"You have been disconnected from the internet. Please check your connection",Toast.LENGTH_LONG).show();
                isNetworkConnected = false;
            }
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        registerReceiver(networkStateReceiver, new IntentFilter(android.net.ConnectivityManager.CONNECTIVITY_ACTION));
    }

    @Override
    public void onPause() {
        unregisterReceiver(networkStateReceiver);
        super.onPause();
    }
}