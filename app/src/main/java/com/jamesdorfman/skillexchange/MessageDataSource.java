package com.jamesdorfman.skillexchange;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

/*
  Created by jamesdorfman on 16-04-17.
  This class was created before Firebase had a big ease-of-use upgrade
  So this class still uses Firebase with the old, more complicated way of connecting
*/

public class MessageDataSource {
    private static final Firebase sRef = new Firebase("https://boiling-torch-2023.firebaseio.com/");
    private static SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
    private static final String Tag = "Message Data Source";
    private static final String COLUMN_TEXT = "text";
    private static final String COLUMN_SENDER = "sender";

    public static void saveMessage(Message message, String convoId){
        Date date = message.getmDate();
        String key = sDateFormat.format(date);
        System.out.println("date: " + message.getmDate());
        System.out.println("KEY: " + key);
        HashMap<String, String> msg = new HashMap<>();
        msg.put(COLUMN_TEXT,message.getmText());
        msg.put(COLUMN_SENDER,message.getmId());
        sRef.child(convoId).child(key).setValue(msg);
    }

    public static MessagesListener addMessagesListener(String convoId, final MessagesCallbacks callbacks){
        MessagesListener listener = new MessagesListener(callbacks);
        sRef.child(convoId).addChildEventListener(listener);
        return listener;
    }

    public static void stop(MessagesListener listener){
        sRef.removeEventListener(listener);
    }

    public static class MessagesListener implements ChildEventListener{

        private MessagesCallbacks callbacks;

        public MessagesListener(MessagesCallbacks callbacks) {
            this.callbacks = callbacks;
        }

        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            HashMap<String,String> msg = (HashMap) dataSnapshot.getValue();
            Message message = new Message();
            message.setmSender(msg.get(COLUMN_SENDER));//get("sender")
            message.setmText(msg.get(COLUMN_TEXT));
            try{
                message.setmDate(sDateFormat.parse(dataSnapshot.getKey()));
                System.out.println("date from simpleformat: " + message.getmDate());
                System.out.println("date without sf from there: " + sDateFormat.parse(dataSnapshot.getKey()));
            }
            catch (ParseException e){
                e.printStackTrace();
            }

            if(callbacks!=null){
                callbacks.onMessageAdded(message);
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
    }
    public interface MessagesCallbacks{
        public void onMessageAdded(Message message);
    }
}
