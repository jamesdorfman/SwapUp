package com.jamesdorfman.skillexchange;

import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.support.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SkillSuggestionProvider extends ContentProvider {

    List<String> skills;
    String token;
    String fb_id;

    @Override
    public boolean onCreate() {
        return false;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        if (skills == null || skills.isEmpty()){
            OkHttpClient client = new OkHttpClient();
            RequestBody formBody = new FormBody.Builder()
                    .add("id", EditSkills.id)
                    .add("token", EditSkills.token)
                    .build();
            Request request = new Request.Builder()
                    .post(formBody)
                    .url("http://jamesdorfman.com/skillExchange/api.php?skillFetch=true")
                    .build();

            try {
                Response response = client.newCall(request).execute();
                String jsonString = response.body().string();
                System.out.println("string: " + jsonString);
                JSONArray jsonArray = new JSONArray(jsonString);

                skills = new ArrayList<>();

                int length = jsonArray.length();
                for (int i = 0; i < length; i++) {
                    JSONObject skill= jsonArray.getJSONObject(i);
                    skills.add(skill.getString("skill_name"));
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        MatrixCursor cursor = new MatrixCursor(
                new String[] {
                        BaseColumns._ID,
                        SearchManager.SUGGEST_COLUMN_TEXT_1,
                        SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID
                }
        );
        if (skills != null) {
            String query = uri.getLastPathSegment().toUpperCase();
            int limit = Integer.parseInt(uri.getQueryParameter(SearchManager.SUGGEST_PARAMETER_LIMIT));

            int length = skills.size();
            for (int i = 0; i < length && cursor.getCount() < limit; i++) {
                String skill = skills.get(i);
                if (skill.toUpperCase().contains(query)){
                    cursor.addRow(new Object[]{ i, skill, i });
                }
            }
        }
        return cursor;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }

}