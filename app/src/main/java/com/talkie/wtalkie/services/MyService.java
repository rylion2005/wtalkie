package com.talkie.wtalkie.services;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.ColorSpace;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.talkie.wtalkie.audio.Recorder;
import com.talkie.wtalkie.audio.Tracker;
import com.talkie.wtalkie.contacts.Contacts;
import com.talkie.wtalkie.sockets.Connector;


public class MyService extends Service {
    private static final String TAG = "MyService";

    private Contacts mContacts;
    private Connector mConnector;

    private final Recorder mRecorder = Recorder.newInstance();
    private final Tracker mTracker = Tracker.newInstance();

/* ********************************************************************************************** */


    public MyService() {
        Log.v(TAG, "new an instance");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate()");
        init();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand()");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new MyBinder();
    }

    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
        Log.v(TAG, "onRebind: " + intent);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.v(TAG, "onUnbind: " + intent);
        return super.onUnbind(intent);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onDestroy() {
        Log.v(TAG, "onDestroy()");
        super.onDestroy();
    }


/* ********************************************************************************************** */
    public void registerAudioRecord(Recorder.Callback cb){
        mRecorder.register(cb);
    }

    public void playRecord(boolean start){
        if (start) {
            mRecorder.start();
        } else {
            mRecorder.stop();
        }
    }

/* ********************************************************************************************** */

    private void init(){
        Log.v(TAG, "init()");
        mContacts = Contacts.newInstance(getApplicationContext());
        mConnector = new Connector();
        mConnector.register(mContacts);

        registerActions();
    }

    private void registerActions(){
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_BOOT_COMPLETED);
        intentFilter.addAction(Intent.ACTION_SCREEN_ON);
        intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION);
        intentFilter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        getApplicationContext().registerReceiver(new MyReceiver(), intentFilter);
    }


/* ********************************************************************************************** */

    public class MyBinder extends Binder {
        public MyService getService(){
            return MyService.this;
        }
    }

/* ********************************************************************************************** */

    public class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.v(TAG, "onReceive: " + intent);
            mConnector.broadcast(mContacts.getMyself().toString().getBytes(),
                    mContacts.getMyself().toString().length());
        }
    }
}
