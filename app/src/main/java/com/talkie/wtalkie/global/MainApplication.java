package com.talkie.wtalkie.global;


import android.app.Application;
import android.content.Context;
import android.content.res.Configuration;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import org.litepal.LitePal;


public class MainApplication extends Application  {
    private static final String TAG = "MainApplication";
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.v(TAG, "onCreate");
        init();
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        Log.v(TAG, "onLowMemory");
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        Log.d(TAG, "onTrimMemory(" + level + ")");
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

/* ********************************************************************************************** */

    private void init(){
        Log.v(TAG, "init...");

        GlobalContext.buildGlobalContext(getApplicationContext());

        // LitePal initialize ASAP
        LitePal.initialize(this);
        LitePal.getDatabase();

        //startService(new Intent(getApplicationContext(), MyService.class));
    }
}
