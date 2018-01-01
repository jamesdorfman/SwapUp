package com.jamesdorfman.skillexchange;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.BaseColumns;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apmem.tools.layouts.*;
import org.apmem.tools.layouts.FlowLayout;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;


public class EditSkills extends AppCompatActivity implements SearchView.OnQueryTextListener, SearchView.OnSuggestionListener{

    String screenType;

    String toDelete;
    String email;
    String password;

    SearchView skillToAdd;
    org.apmem.tools.layouts.FlowLayout masterFlowLayout;

    SharedPreferences prefs;

    public static String token;
    public static String id;

    public SearchView globalSearchView;
    public SearchManager globalSearchManager;


   public void addSkill(View v) {
       System.out.println("text in searchbar: " + globalSearchView.getQuery().length());
       if(globalSearchView.getQuery().length()!=0) { //if skill is not empty
           System.out.println("in the query");
           AsyncTaskRunner checkTask = new AsyncTaskRunner(new CalledByAsyncTaskInterface() {
               @Override
               public void calledByAsyncTaskFunction(JSONObject reader) {
                   try {
                       System.out.println("called");
                       String status = reader.getString("status");
                       System.out.println("status is: " + status);
                       if (status.equals("notExists")) {
                           final Dialog dialog = new Dialog(EditSkills.this);
                           dialog.setContentView(R.layout.new_skill_dialog);
                           dialog.setTitle("Skill Confirmation");
                           Button addSkillButton = (Button) dialog.findViewById(R.id.add_skill_button);
                           Button cancelButton = (Button) dialog.findViewById(R.id.cancel_button);
                           dialog.show();
                           addSkillButton.setOnClickListener(new View.OnClickListener() {
                               @Override
                               public void onClick(View v) {
                                   addVerifiedSkill(v);
                                   dialog.dismiss();
                               }
                           });
                           cancelButton.setOnClickListener(new View.OnClickListener() {
                               @Override
                               public void onClick(View v) {
                                   dialog.dismiss();
                               }
                           });

                       } else {
                           addVerifiedSkill(new Button(EditSkills.this));
                       }
                   } catch (JSONException e) {
                       Log.e("JSONException", "Error: " + e.toString());
                   } // catch (JSONException e)
               }
           });
           checkTask.setProgressDialog(EditSkills.this);
           checkTask.setMessageOnDialog("Adding skill...");
           String addGetVar;
           if (screenType.equals("teach")) {
               addGetVar = "skillTeachAdd";
           } else {
               addGetVar = "skillLearnAdd";
           }
           String toAdd = skillToAdd.getQuery().toString();
           checkTask.setUrl(addGetVar + "=" + toAdd.replaceAll(" ", "%20") + "&checkIfExists=true");
           ArrayList<String> list1 = new ArrayList<String>();
           list1.add("token");
           list1.add(token);
           ArrayList<String> list2 = new ArrayList<String>();
           list2.add("id");
           list2.add(id);
           checkTask.execute(list1, list2);
       }
    }

    public void addVerifiedSkill(View v){
        AsyncTaskRunner task = new AsyncTaskRunner(new CalledByAsyncTaskInterface() {
            @Override
            public void calledByAsyncTaskFunction(JSONObject reader) {
                try {
                    String status = reader.getString("status");
                    System.out.println("status is: " + status);
                    if (status.equals("failure")) {
                        Toast.makeText(getApplicationContext(), "Error sending data to server. Please check your network connection",
                                Toast.LENGTH_SHORT).show();
                    } else if(status.equals("alreadyExists")){
                        Toast.makeText(getApplicationContext(), "You have already added this skill ",
                                Toast.LENGTH_SHORT).show();
                    }else {
                        LinearLayout rLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.skill_edit_page_rlayout, null);

                        ImageView deleteButton = (ImageView) getLayoutInflater().inflate(R.layout.skill_delete_button, null);
                        int dp = 22;  // 10 dps
                        final float scale = getResources().getDisplayMetrics().density;
                        int layout_px = (int) (dp * scale + 0.5f);
                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                layout_px,
                                layout_px);
                        //params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
                        params.gravity = Gravity.CENTER_VERTICAL;
                        layout_px = (int) (10 * scale + 0.5f);
                        params.setMargins(0,0,layout_px,0);

                        deleteButton.setLayoutParams(params);
                        deleteButton.setTag(skillToAdd.getQuery().toString());
                        deleteButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                String skill = (String) v.getTag();
                                toDelete = skill.replaceAll(" ", "%20");
                                deleteClick(v);
                            }
                        });
                        TextView textView = (TextView) getLayoutInflater().inflate(R.layout.skill_page_textview, null);
                        textView.setText(skillToAdd.getQuery().toString().substring(0,1).toUpperCase() + skillToAdd.getQuery().toString().substring(1));

                        ((SearchView)findViewById(R.id.teachSkillToAddEditText)).setQuery("",false);

                        System.out.println("The skill being added is " + skillToAdd.getQuery().toString().replaceAll(" ", "%20"));
                        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
                        rLayout.addView(textView);
                        rLayout.addView(deleteButton);
                        masterFlowLayout.addView(rLayout);
                    }
                } catch (JSONException e) {
                    Log.e("JSONException", "Error: " + e.toString());
                } // catch (JSONException e)
            }
        });
        task.setProgressDialog(EditSkills.this);
        task.setMessageOnDialog("Adding skill...");
        String addGetVar;
        if (screenType.equals("teach")) {
            addGetVar = "skillTeachAdd";
        } else {
            addGetVar = "skillLearnAdd";
        }
        String toAdd = skillToAdd.getQuery().toString();
        task.setUrl(addGetVar + "=" + toAdd.replaceAll(" ", "%20"));
        ArrayList<String> list1 = new ArrayList<String>();
        list1.add("token");
        list1.add(token);
        ArrayList<String> list2 = new ArrayList<String>();
        list2.add("id");
        list2.add(id);
        task.execute(list1,list2);
    }
    public void deleteClick(View v) {
        AsyncTaskRunner task = new AsyncTaskRunner(new CalledByAsyncTaskInterface() {
            @Override
            public void calledByAsyncTaskFunction(JSONObject reader) {
                try {
                    String status = reader.getString("status");
                    System.out.println("status is: " + status);
                    if (status.equals("failure")) {
                        Toast.makeText(getApplicationContext(), "Error sending data to server. Please check your network connection",
                                Toast.LENGTH_LONG).show();
                    } else {
                        recreate();
                    }
                } catch (JSONException e) {
                    Log.e("JSONException", "Error: " + e.toString());
                } // catch (JSONException e)
            }
        });
        task.setProgressDialog(EditSkills.this);
        task.setMessageOnDialog("Deleting skill...");
        String deleteGetVar;
        if (screenType.equals("teach")) {
            deleteGetVar = "skillTeachDelete";
        } else {
            deleteGetVar = "skillLearnDelete";
        }
        //String toDelete = skillToAdd.getQuery().toString();
         toDelete = (String) v.getTag();
        task.setUrl(deleteGetVar + "=" + toDelete.replaceAll(" ", "%20"));
        System.out.println(deleteGetVar + "=" + toDelete.replaceAll(" ", "%20"));
        ArrayList<String> list1 = new ArrayList<String>();
        list1.add("token");
        list1.add(token);
        ArrayList<String> list2 = new ArrayList<String>();
        list2.add("id");
        list2.add(id);
        task.execute(list1,list2);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_skills);

        // Fetch data from mysql table using AsyncTask
        Bundle extras = getIntent().getExtras();
        screenType = extras.getString("screenType");

        prefs = getSharedPreferences(LogInScreen.MY_PREFS_NAME, MODE_PRIVATE);
        email = prefs.getString("email", null);//"No name defined" is the default value.
        password = prefs.getString("password", null);//"No name defined" is the default value.
        System.out.println("email: " + email + " password: " + password);

        getSupportActionBar().setDisplayOptions(android.support.v7.app.ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.center_title_layout);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.skillBorderColor)));

        new Handler().post(new Runnable() {
            @Override
            public void run() {
                TextView t = (TextView) findViewById(R.id.actionbar_textview);
                t.setText("Edit Skills");
            }
        });

        final Drawable upArrow = getResources().getDrawable(R.drawable.abc_ic_ab_back_material);
        upArrow.setColorFilter(Color.parseColor("#FFFFFF"), PorterDuff.Mode.SRC_ATOP);
        getSupportActionBar().setHomeAsUpIndicator(upArrow);

        getSupportActionBar().setTitle("SwapUp");

        skillToAdd = (SearchView) findViewById(R.id.teachSkillToAddEditText);
       /* if (screenType.equals("teach")) {
            skillToAdd.setHint("Skill you want to teach");
        } else {
            skillToAdd.setHint("Skill you want to learn");
        }
        */
        masterFlowLayout = (FlowLayout) findViewById(R.id.editSkillsHolder);

        AsyncTaskRunner task = new AsyncTaskRunner(new CalledByAsyncTaskInterface() {
            @Override
            public void calledByAsyncTaskFunction(JSONObject reader) {
                try {
                    String status = reader.getString("status");
                    System.out.println("status is: " + status);
                    if (status.equals("failure")) {
                        Toast.makeText(getApplicationContext(), "Error getting data from server. Please check your network connection",
                                Toast.LENGTH_LONG).show();
                    } else {
                        JSONArray skillArray;
                        if (screenType.equals("teach")) {
                            skillArray = reader.getJSONArray("teachSkills");
                        } else {
                            skillArray = reader.getJSONArray("learnSkills");
                        }
                        //JSONArray learnSkills = reader.getJSONArray("learnSkills");

                        //views are simply there for design purposes
                        //skillsToTeachLinearLayout.removeAllViews();
                        //skillsToLearnLinearLayout.removeAllViews();

                        for (int i = 0; i < skillArray.length(); i++) {
                            JSONObject object = skillArray.getJSONObject(i);
                            String objectString = object.getString("skill");
                            LinearLayout rLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.skill_edit_page_rlayout, null);
                            ImageView deleteButton = (ImageView) getLayoutInflater().inflate(R.layout.skill_delete_button, null);
                            int dp = 22;  // 10 dps
                            final float scale = getResources().getDisplayMetrics().density;
                            int layout_px = (int) (dp * scale + 0.5f);
                            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                    layout_px,
                                    layout_px);
                            //params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
                            //params.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
                            params.gravity = Gravity.CENTER_VERTICAL;
                            layout_px = (int) (10 * scale + 0.5f);
                            params.setMargins(0,0,layout_px,0);
                            deleteButton.setLayoutParams(params);
                            deleteButton.setTag(objectString);
                            deleteButton.setId(i);
                            deleteButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    String skill = (String) v.getTag();
                                    toDelete = skill.replaceAll(" ", "%20");
                                    deleteClick(v);
                                }
                            });
                            TextView textView = (TextView) getLayoutInflater().inflate(R.layout.skill_page_textview, null);
                            textView.setText(objectString.substring(0,1).toUpperCase() + objectString.substring(1));
                            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
                            rLayout.addView(textView);
                            rLayout.addView(deleteButton);
                            FlowLayout.LayoutParams rLayParams = new FlowLayout.LayoutParams(
                                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
                            );
                            rLayParams.setMargins(0,0,(int) (5 * scale + 0.5f),(int) (5 * scale + 0.5f));
                            rLayout.setLayoutParams(rLayParams);
                            //RelativeLayout holderPaddingLayout = new FlowLayout.LayoutParams();
                            masterFlowLayout.addView(rLayout);
                        }
                    }
                } catch (JSONException e) {
                    Log.e("JSONException", "Error: " + e.toString());
                } // catch (JSONException e)
            }
        });
        task.setProgressDialog(EditSkills.this);
        task.setMessageOnDialog("Fetching skills...");
        task.setUrl("getSkills=true");
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        id = preferences.getString("id", null);
        token = preferences.getString("token", null);//"No name defined" is the default value.
        ArrayList<String> list1 = new ArrayList<String>();
        list1.add("token");
        list1.add(token);
        ArrayList<String> list2 = new ArrayList<String>();
        list2.add("id");
        list2.add(id);
        task.execute(list1,list2);
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_profile_screen, menu);

        //MenuItem searchItem = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) findViewById(R.id.teachSkillToAddEditText);
        searchView.setOnQueryTextListener(this);

        System.out.println("screenType: " + screenType);
        if(screenType.equals("teach")){
            searchView.setQueryHint("Skill you can teach");
        }
        else{
            searchView.setQueryHint("Skill you want to learn");
        }
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        globalSearchManager = searchManager;
        globalSearchView = searchView;
        searchView.setSearchableInfo(searchManager.getSearchableInfo(
                new ComponentName(this, EditSkills.class)));
        searchView.setIconifiedByDefault(false);

        int searchVoiceIconId = searchView.getContext().getResources().getIdentifier("android:id/search_voice_btn", null, null);
        ImageView searchVoiceIcon = (ImageView) searchView.findViewById(searchVoiceIconId);
        searchVoiceIcon.setImageResource(R.drawable.transparent_background);
        ((LinearLayout)searchView.findViewById(searchVoiceIconId).getParent()).setBackgroundColor(Color.TRANSPARENT);


        searchView.setOnSuggestionListener(new SearchView.OnSuggestionListener()
        {
            @Override
            public boolean onSuggestionClick(int position)
            {
                searchView.clearFocus();
                Cursor c = (Cursor) searchView.getSuggestionsAdapter().getItem(position);
                searchView.setQuery(c.getString(c.getColumnIndex(SearchManager.SUGGEST_COLUMN_TEXT_1)),true);
                return true;
            }

            @Override
            public boolean onSuggestionSelect(int position)
            {
                return false;
            }
        });

        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        // User pressed the search button
        System.out.println("submitted!!");
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        // User changed the text
        System.out.println("hey");
        //skillToAdd.getSuggestionsAdapter().getCursor().requery();
        //globalSearchView.setSearchableInfo(globalSearchManager.getSearchableInfo(
        //        new ComponentName(this, EditSkills.class)));

        return false;
    }


    @Override
    public boolean onSuggestionSelect(int position) {
        System.out.println("CLICK");
        String suggestion = getSuggestion(position);
        skillToAdd.setQuery(suggestion, true); // submit query now
        return true; // replace default search manager behaviour
    }

    @Override
    public boolean onSuggestionClick(int position) {
        System.out.println("CLICK");
        String suggestion = getSuggestion(position);
        skillToAdd.setQuery(suggestion, true); // submit query now
        return true; // replace default search manager behaviour
    }

    private String getSuggestion(int position) {
        Cursor cursor = (Cursor) skillToAdd.getSuggestionsAdapter().getItem(
                position);
        String suggest1 = cursor.getString(cursor
                .getColumnIndex(SearchManager.SUGGEST_COLUMN_TEXT_1));
        return suggest1;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {

        } else if (Intent.ACTION_VIEW.equals(intent.getAction())) {

        }
    }
}