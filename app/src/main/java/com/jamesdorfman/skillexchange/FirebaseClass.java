package com.jamesdorfman.skillexchange;

import android.app.Application;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.firebase.client.Firebase;

// This part of the project was made before the big Firebase ease of usage update
// That is why it may seem a bit strange

public class FirebaseClass extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Firebase.setAndroidContext(this);
    }
}
