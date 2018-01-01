package com.jamesdorfman.skillexchange;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.content.Intent;
import android.content.SharedPreferences;

import android.preference.PreferenceManager;
import android.support.v4.widget.DrawerLayout;
import android.os.Bundle;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.fasterxml.jackson.databind.deser.DataFormatReaders;
import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.mikhaellopez.circularimageview.*;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class MatchesScreen extends AppCompatActivity {
    private ListView mDrawerList;
    private DrawerLayout mDrawerLayout;
    private ArrayAdapter<String> mAdapter;

    private ActionBarDrawerToggle mDrawerToggle;
    private String mActivityTitle;


    String email;
    String password;

    String sender;
    String message;

    String user_id;
    JSONArray matchesArray;
    String nameString;

    int i;

    String token;
    String id;

    ProgressDialog dialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_matches_screen);

        Firebase.setAndroidContext(this);
        mDrawerList = (ListView) findViewById(R.id.navList);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mActivityTitle = getTitle().toString();


        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        Bundle inBundle = getIntent().getExtras();

        token = preferences.getString("token", null);
        id = preferences.getString("id", null);

        addDrawerItems();
        setupDrawer();

        getSupportActionBar().setDisplayOptions(android.support.v7.app.ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.center_title_layout);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.skillBorderColor)));

        final Drawable upArrow = getResources().getDrawable(R.drawable.abc_ic_ab_back_material);
        upArrow.setColorFilter(Color.parseColor("#FFFFFF"), PorterDuff.Mode.SRC_ATOP);

        SharedPreferences prefs = getSharedPreferences(LogInScreen.MY_PREFS_NAME, MODE_PRIVATE);
        email = prefs.getString("email", null);//"No name defined" is the default value.
        password = prefs.getString("password", null);//"No name defined" is the default value.
        System.out.println("email: " + email + " password: " + password);

        AsyncTaskRunner task = new AsyncTaskRunner(new CalledByAsyncTaskInterface() {
            @Override
            public void calledByAsyncTaskFunction(JSONObject reader) {
                try {
                    String status = reader.getString("status");
                    System.out.println("status is: " + status);
                    if (status.equals("failure")) {
                        Toast.makeText(getApplicationContext(), "Error getting profile data from server. Please restart the app",
                                Toast.LENGTH_LONG).show();
                    }
                    else if(status.equals("no matches")){
                        LinearLayout holder = (LinearLayout) findViewById(R.id.matchesPreviewLinearLayout);
                        TextView t = new TextView(MatchesScreen.this);
                        t.setText("You do not have any matches yet. Keep swiping!");
                        t.setGravity(Gravity.CENTER);
                        int dpSize = 20;  // 10 dps
                        final float scale = getResources().getDisplayMetrics().density;
                        int padding_in_px = (int) (dpSize * scale + 0.5f);
                        t.setPadding(0,padding_in_px,0,0);
                        t.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                        holder.addView(t);
                    }
                    else {
                        try {
                            matchesArray = reader.getJSONArray("matches");
                            user_id = reader.getString("user_id");

                            for (int i = 0; i < matchesArray.length(); i++) {
                                System.out.println("BACK HERE");
                                JSONObject insideArray = matchesArray.getJSONObject(i);
                                final String idString = insideArray.getString("id");
                                String[] ids = {idString,user_id};
                                System.out.println("THERE");
                                Arrays.sort(ids);
                                Firebase sRef = new Firebase("https://boiling-torch-2023.firebaseio.com/" + ids[0] + ids[1]);
                                Query queryRef = sRef.orderByKey().limitToLast(1);
                                final int[] Is = new int[]{i};
                                System.out.println("https://boiling-torch-2023.firebaseio.com/" + ids[0] + ids[1]);
                                final int x = i;
                               /* queryRef.addChildEventListener(new ChildEventListener() {
                                    @Override
                                    public void onChildAdded(DataSnapshot snapshot, String previousChild) {
                                        try {
                                            HashMap<String, String> msg = (HashMap) snapshot.getValue();
                                            sender = msg.get("sender");
                                            sender = matchesArray.getJSONObject(Is[0]).getString("name");
                                            String[] names = sender.split("\\s+");
                                            sender = names[0];
                                            message = msg.get("text");
                                            if (message.length() > 80) {
                                                message = message.substring(0, Math.min(message.length(), 80)) + "...";
                                            } else {
                                                message.substring(0, Math.min(message.length(), 83));
                                            }
                                            message = message.substring(0, Math.min(message.length(), 80));
                                            *///message = sender + ": " + message;
                                            drawElement(matchesArray.getJSONObject(x), Is[0], "hey");
                                        /*} catch(JSONException e){
                                            Log.e("JSONException", "Error: " + e.toString());
                                        }
                                    }
                                    @Override
                                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                                    }

                                    @Override
                                    public void onChildRemoved(DataSnapshot dataSnapshot) {

                                    }

                                    @Override
                                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                                    }

                                    @Override
                                    public void onCancelled(FirebaseError firebaseError) {

                                    }
                                    // ....
                                });*/
                                //drawElement(matchesArray, Is[0], "the message is as follows, kanye west swallows he hollow ");
                            }
                        }
                        catch (JSONException e){
                            Log.e("JSONException", "Error ayo: " + e.toString());
                        }
                    }
                } catch (JSONException e) {
                    Log.e("JSONException", "Error uou: " + e.toString());
                }
                dialog.dismiss();
            }
        });
        task.setProgressDialog(MatchesScreen.this);
        task.setMessageOnDialog("Getting matches from server");
        task.setUrl("email=" + email + "&pword=" + password + "&getMatches=true");

        ArrayList<String> list1 = new ArrayList<String>();
        list1.add("token");
        list1.add(token);

        ArrayList<String> list2 = new ArrayList<String>();
        list2.add("id");
        list2.add(id);
        dialog = new ProgressDialog(MatchesScreen.this, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT);
        dialog.setMessage("Fetching Matches...");
        dialog.show();
        task.execute(list1, list2);
    }

    public void drawElement(final JSONObject insideArray, int i, String message){
        try {
            System.out.println(matchesArray.toString());
            message = message.substring(0,Math.min(message.length(),30));
            if(message.length()==30){
                message = message + "...";
            }
//            final JSONObject insideArray = matchesArray.getJSONObject(i);
            nameString = insideArray.getString("name");
            String[] nameArray = nameString.split(" ");
            nameString = nameArray[0].substring(0,1).toUpperCase() + nameArray[0].substring(1);
            final String fbImageUrl = insideArray.getString("fbImageUrl");
            final String idString = insideArray.getString("id");
            System.out.println(nameString);
            TextView t = new TextView(MatchesScreen.this);
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT);
            //RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)t.getLayoutParams();
            //params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            t.setLayoutParams(params);
            t.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
            t.setText(nameString);
            t.setTextColor(Color.parseColor("#000000"));

            TextView timeT = new TextView(MatchesScreen.this);
            RelativeLayout.LayoutParams timeParams = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT);
            //RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)t.getLayoutParams();
            timeParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            timeParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            timeT.setLayoutParams(timeParams);
            timeT.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
            timeT.setText(insideArray.getString("last_msg_timestamp"));
            timeT.setTextColor(Color.parseColor("#101010"));

            RelativeLayout rlay = new RelativeLayout(MatchesScreen.this);
            int dpSize = 25;  // 10 dps
            final float scale = getResources().getDisplayMetrics().density;
            int padding_in_px = (int) (dpSize * scale + 0.5f);

            int height_in_px = (int) (20 * scale + 0.5f);

            RelativeLayout.LayoutParams rlayParams = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.MATCH_PARENT,
                    height_in_px);
            rlay.setLayoutParams(rlayParams);

            RelativeLayout rlayHolder = new RelativeLayout(MatchesScreen.this);

            dpSize = 45;  // 10 dps
            padding_in_px = (int) (dpSize * scale + 0.5f);

            final TextView descriptionTV = new TextView(MatchesScreen.this);
            RelativeLayout.LayoutParams descriptionParams = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.MATCH_PARENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT);
            descriptionParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            descriptionTV.setLayoutParams(descriptionParams);
            descriptionTV.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
            //descriptionTV.setText(message);
            if(!insideArray.getString("last_msg").equals("")) {
                if(insideArray.getString("last_msg").length()<33) {
                    descriptionTV.setText(insideArray.getString("last_msg"));
                }else{
                    descriptionTV.setText(insideArray.getString("last_msg").substring(0,33) + "...  ");
                }
            } else{
                descriptionTV.setText("Send " + nameString + " a message!");
            }
            System.out.println(message);
            //May do too much work
            descriptionTV.setTypeface(Typeface.create("sans-serif-light", Typeface.NORMAL));
            //t.setTextColor(Color.parseColor("#000000"));
            LinearLayout.LayoutParams rlayHolderParams = new LinearLayout.LayoutParams(
                    RelativeLayout.LayoutParams.MATCH_PARENT,
                    padding_in_px);
            rlayHolderParams.gravity = Gravity.CENTER_VERTICAL;
            rlayHolder.setLayoutParams(rlayHolderParams);
            dpSize = 20;  // 10 dps
            int paddingRight = (int) (dpSize * scale + 0.5f);
            System.out.println("AY");
            dpSize = 0;  // 10 dps
            int paddingTop = (int) (dpSize * scale + 0.5f);
            dpSize = 0;
            int paddingLeft = (int) (dpSize * scale + 0.5f);
            dpSize = 0;
            int paddingBottom = (int) (dpSize * scale + 0.5f);
            rlayHolder.setPadding(paddingLeft,paddingTop,paddingRight,paddingBottom);
            rlay.addView(t);
            rlay.addView(timeT);
            rlay.setBackgroundColor(Color.WHITE);
            rlayHolder.addView(rlay);
            rlayHolder.addView(descriptionTV);
            rlayHolder.setOnClickListener(new View.OnClickListener()
            {

                @Override
                public void onClick(View v)
                {
                    Intent chatActivity = new Intent(MatchesScreen.this,ChatScreen.class);
                    chatActivity.putExtra("id",idString);
                    chatActivity.putExtra("user_id",user_id);
                    chatActivity.putExtra("fbImageUrl", fbImageUrl);
                    startActivity(chatActivity);
                    finish();
                }
            });
            com.mikhaellopez.circularimageview.CircularImageView c = new CircularImageView(MatchesScreen.this);

            dpSize = 80;  // 10 dps
            padding_in_px = (int) (dpSize * scale + 0.5f);

            LinearLayout.LayoutParams circleImageParams = new LinearLayout.LayoutParams(
                    padding_in_px,
                    padding_in_px);

            dpSize = 10;  // 10 dps
            padding_in_px = (int) (dpSize * scale + 0.5f);

            circleImageParams.setMargins(padding_in_px,0,padding_in_px,0);
            c.setLayoutParams(circleImageParams);
            Picasso.with(MatchesScreen.this).load(fbImageUrl).error(R.drawable.default_profile_picture).into(c);

            //c.setImageDrawable(getResources().getDrawable(R.drawable.profile_pic_good));
            dpSize = 10;  // 10 dps
            padding_in_px = (int) (dpSize * scale + 0.5f);
            c.setPadding(0,0,padding_in_px,0);
            c.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(MatchesScreen.this, FindPeopleZoom.class);
                    Bundle extras = new Bundle();
                    try {
                        extras.putString("id", insideArray.getString("id"));
                        i.putExtras(extras);
                        startActivity(i);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
            LinearLayout linLay = (LinearLayout) findViewById(R.id.matchesPreviewLinearLayout);
            LinearLayout underLinLay = new LinearLayout(MatchesScreen.this);
            LinearLayout.LayoutParams underLinLayParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            dpSize = 13;  // 10 dps
            padding_in_px = (int) (dpSize * scale + 0.5f);
            underLinLayParams.setMargins(0,padding_in_px,0,0);
            underLinLay.setLayoutParams(underLinLayParams);
            underLinLay.setPadding(0,0,0,padding_in_px);

            underLinLay.setOrientation(LinearLayout.HORIZONTAL);
            underLinLay.addView(c);
            underLinLay.addView(rlayHolder);
            underLinLay.setBackgroundResource(R.drawable.bottom_border);
            linLay.addView(underLinLay);
        } catch(JSONException e){
            Log.e("JSONException", "Error right here: " + e.toString());
        }
    }
    private void addDrawerItems() {
        String[] osArray = { "Home", "Profile", "Matches" };
        mAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, osArray){

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
                switch (position){
                    case 0:
                        Intent findPeopleScreen = new Intent(MatchesScreen.this,FindPeopleScreen.class);
                        startActivity(findPeopleScreen);
                        break;
                    case 1:
                        Intent profileActivity = new Intent(MatchesScreen.this,ProfileScreen.class);
                        startActivity(profileActivity);
                        break;
                    case 2:
                        mDrawerLayout.closeDrawers();
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
                getSupportActionBar().setTitle("Matches");
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

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        // Activate the navigation drawer toggle
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent i = new Intent(MatchesScreen.this,SettingsScreen.class);
            startActivity(i);
        }
        else if(id == R.id.action_log_out) {
            SaveSharedPreference.deletePreferences(MatchesScreen.this);
            Intent intent = new Intent(MatchesScreen.this, AccountScreen.class);
            startActivity(intent);
            finish();
        }
        else if (id == R.id.about_screen_button){
            Intent i = new Intent(MatchesScreen.this, AboutScreen.class);
            startActivity(i);
        }
        return super.onOptionsItemSelected(item);
    }
}
