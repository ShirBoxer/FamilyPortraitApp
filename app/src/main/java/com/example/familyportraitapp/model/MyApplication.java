package com.example.familyportraitapp.model;

import android.app.Application;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import androidx.core.os.HandlerCompat;

public class MyApplication extends Application {
    static public Context context;
    static public final Handler handler = HandlerCompat.createAsync(Looper.getMainLooper());

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
    }
}

