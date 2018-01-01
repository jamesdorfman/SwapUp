package com.jamesdorfman.skillexchange;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.widget.ImageView;

import java.util.ArrayList;

/**
 * Created by jamesdorfman on 16-08-22.
 */
public class SwipeData{
    public Activity activity;

    private String name;


    private String imagePath;

    private ArrayList<String> skills;

    private String matchId;

    private String distance;

    public SwipeData(String imagePath, String name, String match_id, String distance, ArrayList<String> skills){
        this.name=name;
        this.imagePath = imagePath;
        this.skills = skills;
        this.matchId = match_id;
        this.distance = distance;
    }

    public String getName() { return name; }

    public String getImagePath(){
        return imagePath;
    }

    public ArrayList<String> getSkills(){
        return skills;
    }

    public String getMatchId(){ return matchId; }

    public String getDistance() { return distance; }
}
