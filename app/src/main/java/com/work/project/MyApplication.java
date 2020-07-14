package com.work.project;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;

public class MyApplication extends Application {
    private final static String APP_PREFERENCES = "mySettings";
    private final static String NIGHT_MODE_KEY = "isNightMode";

    public void onCreate() {
        super.onCreate();
        applyNightMode(isNightMode());
    }

    public void setNightMode(boolean nightMode){
        SharedPreferences preferences = getSharedPreferences();
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(NIGHT_MODE_KEY, nightMode);
        editor.apply();
        applyNightMode(nightMode);
    }

    public boolean isNightMode(){
        SharedPreferences preferences = getSharedPreferences();
        return preferences.getBoolean(NIGHT_MODE_KEY, false);
    }

    private void applyNightMode(boolean nightMode){
        AppCompatDelegate.setDefaultNightMode(
                nightMode? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
    }

    private SharedPreferences getSharedPreferences(){
        Context context = getApplicationContext();
        return context.getSharedPreferences(APP_PREFERENCES, MODE_PRIVATE);
    }
}
