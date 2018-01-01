package com.jamesdorfman.skillexchange;

import android.graphics.Bitmap;
import android.widget.ImageView;

// This class contains items whose states
// were accessed so often that it didn't make sense to put them in storage.
// It's just a simple class to hold the static variables
public class Globals {
    public static ImageView iv;
    public static String siteLink = "http://jamesdorfman.com/skillExchange";
    public static int homeLoaded = 0;
}
