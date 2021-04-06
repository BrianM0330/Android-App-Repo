package com.example.rewards;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class RewardsSharedprefs {
    private static final String TAG = "MyProjectSharedPreference";
    private SharedPreferences prefs;

    RewardsSharedprefs(Activity activity) {
        super();

        prefs = activity.getSharedPreferences("API_KEY", Context.MODE_PRIVATE);
    }

    void save (String key, String text) {
        Log.d(TAG, "Saved API key: " + text);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(key, text);
        editor.apply();
    }

    String getValue(String key) {
        String text = prefs.getString(key, "");
        Log.d(TAG, "getValue() fetched: " + text);
        return text;
    }

    void clearPrefs() {
        Log.d(TAG, "Clearing preferences");
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.apply();
    }

    void deleteKey(String key) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove(key);
        editor.apply();
    }
}
