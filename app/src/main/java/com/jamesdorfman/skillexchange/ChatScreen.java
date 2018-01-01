package com.jamesdorfman.skillexchange;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.mikhaellopez.circularimageview.CircularImageView;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class ChatScreen extends AppCompatActivity implements View.OnClickListener, MessageDataSource.MessagesCallbacks {
    private ListView mDrawerList;
    private DrawerLayout mDrawerLayout;
    private ArrayAdapter<String> mNotFirebaseAdapter;

    private ActionBarDrawerToggle mDrawerToggle;
    private String mActivityTitle;

    String senderName;
    String id;
    String user_id;

    public static final String USER_EXTRA = "USER";
    public static final String TAG = "ChatActivity";
    private ArrayList<Message> mMessages;
    private MessagesAdapter mAdapter;
    private String mRecipient;
    private ListView mListView;
    private Date mLastMessageDate = new Date();
    private String mConvoId;
    private MessageDataSource.MessagesListener mListener;
    List<Message> Message_List;
    private MessageDataSource datasource;
    public static SharedPreferences noteprefs;
    HashMap<String,String> MapListMessages = new HashMap<String,String>();

    long getTimeStamp;
    TextView timestampInsert;

    String token;
    String php_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_screen);

        SharedPreferences prefs = getSharedPreferences(LogInScreen.MY_PREFS_NAME, MODE_PRIVATE);
        Bundle extras = getIntent().getExtras();

        id = "";
        user_id = "0";
        final String[] fbImageUrlNotFinal = new String[1];
        if (extras != null) {
            id = extras.getString("id");
            user_id = extras.getString("user_id");
            fbImageUrlNotFinal[0] = extras.getString("fbImageUrl");
            System.out.println("retrieved url: " + fbImageUrlNotFinal[0]);
        }

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        Bundle inBundle = getIntent().getExtras();

        token = preferences.getString("token", null);
        php_id = preferences.getString("id", null);


        System.out.println("id: " + id);
        AsyncTaskRunner task = new AsyncTaskRunner(new CalledByAsyncTaskInterface() {
            @Override
            public void calledByAsyncTaskFunction(JSONObject reader) {
                try {
                    String status = reader.getString("status");
                    System.out.println("status is: " + status);
                    if (status.equals("failure")) {
                        Toast.makeText(getApplicationContext(), "Error verifying match data with server. Please restart the app",
                                Toast.LENGTH_LONG).show();
                    }
                    else {
                        senderName = reader.getString("name");
                        setTitle(senderName);
                        user_id = reader.getString("originId");
                        System.out.println("name " + senderName);
                        final String fbImageUrl = fbImageUrlNotFinal[0];
                        new Handler().post(new Runnable() {
                            @Override
                            public void run() {
                                final CircularImageView c = (CircularImageView) findViewById(R.id.profilePicture);
                                Picasso.with(ChatScreen.this).load(fbImageUrl).error(R.drawable.default_profile_picture).into(c);
                                final TextView t = (TextView) findViewById(R.id.actionbar_textview);
                                t.setText(senderName.split(" ")[0]);
                                System.out.println("handled");
                            }
                        });
                    }
                } catch (JSONException e) {
                    Log.e("JSONException", "Error: " + e.toString());
                }
            }
        });
        task.setProgressDialog(ChatScreen.this);
        task.setMessageOnDialog("Fetching data from server");
        task.setUrl("id=" + id);
        ArrayList<String> list1 = new ArrayList<String>();
        list1.add("token");
        list1.add(token);

        ArrayList<String> list2 = new ArrayList<String>();
        list2.add("id");
        list2.add(php_id);
        task.execute(list1, list2);

        /*timestampInsert = (TextView) findViewById(R.id.timestampInsert);
        final Message[] timeMsgArray = new Message[1];
        Message timeMsg = new Message();
        timeMsg.setmSender("time");
        timeMsgArray[0] = timeMsg;
        */

        mRecipient = id;

        mListView = (ListView)findViewById(R.id.messages_list);
        mMessages = new ArrayList<>();
        mAdapter = new MessagesAdapter(mMessages);
        //mAdapter.add(timeMsgArray[0]);
        //  mAdapter.insert(timestampInsert,-1);
        mListView.setAdapter(mAdapter);

        setTitle(senderName);
        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        Button sendMessage = (Button)findViewById(R.id.send_message);
        sendMessage.setOnClickListener(this);

        String[] ids = {id,user_id};
        Arrays.sort(ids);
        mConvoId = ids[0]+ids[1];
        System.out.println(mConvoId);
        mListener = MessageDataSource.addMessagesListener(mConvoId, this);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        getSupportActionBar().setDisplayOptions(android.support.v7.app.ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.center_title_layout_with_picture);
        //com.mikhaellopez.circularimageview.CircularImageView c = (CircularImageView) R.layout

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.skillBorderColor)));

        final Drawable upArrow = getResources().getDrawable(R.drawable.abc_ic_ab_back_material);
        upArrow.setColorFilter(Color.parseColor("#FFFFFF"), PorterDuff.Mode.SRC_ATOP);

        LinearLayout l = (LinearLayout) findViewById(R.id.chatTitleHeaderHolder);
        l.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(ChatScreen.this, FindPeopleZoom.class);
                Bundle extras = new Bundle();
                System.out.println("chat_id: " + id);
                extras.putString("id",id);
                i.putExtras(extras);
                startActivity(i);
            }
        });


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

        if(id==android.R.id.home){
            this.finish();
            return true;
        }
        else if (id == R.id.action_settings) {
            return true;
        }
        else if (id == R.id.action_settings) {
            return true;
        }
        else if(id == R.id.action_log_out) {
            SaveSharedPreference.deletePreferences(ChatScreen.this);
            Intent intent = new Intent(ChatScreen.this, AccountScreen.class);
            startActivity(intent);
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onMessageAdded(Message message) {
        mMessages.add(message);
        //mMessages.add(Integer.toString(message.getmDate().getDay()));
        mAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MessageDataSource.stop(mListener);
    }

    @Override
    public void onClick(View v) {
        EditText newMessageView = (EditText)findViewById(R.id.new_message);
        String newMessage = newMessageView.getText().toString().replace(" ","%20");
        newMessageView.setText("");
        Message msg = new Message();
        System.out.println("setting date: " + new Date());
        msg.setmDate(new Date());
        msg.setmText(newMessage);
        msg.setmSender(user_id);
        msg.setmId(user_id);
        System.out.println("user id: " + msg.getmSender() + " ");

        MessageDataSource.saveMessage(msg, mConvoId);

        /*Long tsLong = System.currentTimeMillis()/1000;
        String ts = tsLong.toString();

        timestampInsert = (TextView) findViewById(R.id.timestampInsert);
        timestampInsert.setText("");
*/
        AsyncTaskRunner lastMsgTask = new AsyncTaskRunner(new CalledByAsyncTaskInterface() {
            @Override
            public void calledByAsyncTaskFunction(JSONObject reader) {
                try {
                    String status = reader.getString("status");
                    System.out.println("status is: " + status);
                    if (status.equals("failure")) {
                        Toast.makeText(getApplicationContext(), "Error verifying match data with server. Please restart the app",
                                Toast.LENGTH_LONG).show();
                    }
                    else {
                    }
                } catch (JSONException e) {
                    Log.e("JSONException", "Error: " + e.toString());
                }
            }
        });
        lastMsgTask.setProgressDialog(ChatScreen.this);
        lastMsgTask.setMessageOnDialog("Fetching data from server");
        lastMsgTask.setUrl("&setLastMsg=" + newMessage.replaceAll("\n","%20") + "&showId=" + id);


        ArrayList<String> list01 = new ArrayList<String>();
        list01.add("token");
        list01.add(token);

        ArrayList<String> list02 = new ArrayList<String>();
        list02.add("id");
        list02.add(php_id);
        System.out.println("php_id: " + php_id);
        System.out.println("token: " + token);
        lastMsgTask.execute(list01, list02);
    }

    private class MessagesAdapter extends ArrayAdapter<Message> {
        MessagesAdapter(ArrayList<Message> messages){
            super(ChatScreen.this, R.layout.message_item, R.id.message_right, messages);//super(ChatScreen.this, R.layout.message_item, R.id.message_left, messages);
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            convertView = super.getView(position, convertView, parent);
            Message message = getItem(position);



            int sdk = Build.VERSION.SDK_INT;
            System.out.println(message.getmId());
            TextView nameView;
            TextView timeView;
            LinearLayout.LayoutParams layoutParams;

            if (message.getmSender().equals(user_id)){
                nameView = (TextView)convertView.findViewById(R.id.message_right);
                timeView = (TextView)convertView.findViewById(R.id.message_time);
                nameView.setTextColor(Color.parseColor("#FFFFFF"));
                layoutParams = (LinearLayout.LayoutParams)nameView.getLayoutParams();
                if (sdk >= Build.VERSION_CODES.JELLY_BEAN) {
                    nameView.setBackground(getDrawable(R.drawable.right_msg_padding));
                } else{
                    nameView.setBackgroundDrawable(getDrawable(R.drawable.right_msg_padding));
                }
                layoutParams.gravity = Gravity.RIGHT;
                nameView.setGravity(Gravity.RIGHT);
            } else{
                nameView = (TextView)convertView.findViewById(R.id.message_right);
                timeView = (TextView)convertView.findViewById(R.id.message_time);
                if (sdk >= Build.VERSION_CODES.JELLY_BEAN) {
                    nameView.setBackground(getDrawable(R.drawable.left_msg_padding));
                } else{
                    nameView.setBackgroundDrawable(getDrawable(R.drawable.right_msg_padding));
                }
                nameView.setTextColor(Color.parseColor("#000000"));
                nameView.setGravity(Gravity.LEFT);
                layoutParams = (LinearLayout.LayoutParams)nameView.getLayoutParams();
                layoutParams.gravity = Gravity.LEFT;
            }

            int dpSize = 5;  // 10 dps
            final float scale = getResources().getDisplayMetrics().density;
            int margin_in_px = (int) (dpSize * scale + 0.5f);

            layoutParams.setMargins(margin_in_px,0,margin_in_px,margin_in_px);

            System.out.println("DATE from msg adapter: " + message.getmDate().toString());


            DateDifference dt = new DateDifference(message.getmDate(),new Date());


            String timeToSetString;


            System.out.println("new date: " + new Date().toString());
            System.out.println("original date: " + message.getmDate().toString());

            System.out.println("new date time: " + new Date().getTime());
            System.out.println("original date time: " + message.getmDate().getTime());

            long diff = new Date().getTime() - message.getmDate().getTime();
            System.out.println("diference: " + diff);
            long seconds = diff / 1000;
            long minutes = seconds / 60;
            long hours = minutes / 60;
            long days = hours / 24;
            long months = days/30;
            if(months>=2){
                timeToSetString = Long.toString(months) + " months ago";
            }
            else if (months>=1 && months<2){
                timeToSetString = Long.toString(months) + " month ago";
            }
            else if(days>=2){
                timeToSetString = Long.toString((int) days) + " days ago";
            }
            else if(days>=1 && days<2){
                timeToSetString = Long.toString((int) days) + " day ago";
            }
            else if(hours>=2){
                System.out.println(days + " : " + hours + " : " + minutes);
                timeToSetString = Long.toString((int) hours) + " hours ago";
            }
            else if(hours>=1 && hours<2){
                timeToSetString = Long.toString((int) hours) + " hour ago";
            }
            else if(minutes>=2){
                timeToSetString = Long.toString((int) minutes) + " minutes ago";
            }
            else if(minutes>=1 && minutes<2){
                timeToSetString = Integer.toString((int) minutes) + " minute ago";
            }
/*            else if(seconds>=30 ){
                timeToSetString = Integer.toString((int) seconds) + " seconds ago";
            }
  */        else{
                timeToSetString = "Just now";
            }
            System.out.println("Minutes: " + minutes);
            nameView.setText(message.getmText().replaceAll("%20"," "));
            nameView.setText(message.getmText().replaceAll("%20"," "));


            nameView.setLayoutParams(layoutParams);

            dpSize = 350;  // 10 dps
            int width_in_px = (int) (dpSize * scale + 0.5f);

            nameView.setMaxWidth(width_in_px);
            timeView.setText(timeToSetString);
            timeView.setMaxWidth(width_in_px);
            timeView.setLayoutParams(layoutParams);

            return convertView;
        }
    }
}
