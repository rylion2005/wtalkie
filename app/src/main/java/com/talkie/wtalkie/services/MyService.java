package com.talkie.wtalkie.services;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.talkie.wtalkie.audio.Recorder;
import com.talkie.wtalkie.audio.Tracker;
import com.talkie.wtalkie.contacts.Myself;
import com.talkie.wtalkie.contacts.User;
import com.talkie.wtalkie.contacts.UserManager;
import com.talkie.wtalkie.sessions.SessionManager;
import com.talkie.wtalkie.sockets.Connector;
import com.talkie.wtalkie.sockets.Streamer;


import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


public class MyService extends Service {
    private static final String TAG = "MyService";

    private static final int TIMER_INTERVAL = 10 * 1000; //ms

    private final Connector mConnector = new Connector();
    private final Streamer mStreamer = Streamer.getInstance();
    private final UserManager mUserManager = UserManager.getInstance();
    private final Recorder mRecorder = Recorder.getInstance();
    private final Tracker mTracker = Tracker.getInstance();
    private final List<ConnectivityCallback> mCallbacks = new ArrayList<>();


    FileOutputStream fos;

/* ********************************************************************************************** */


    public MyService() {
        Log.v(TAG, "new MyService");
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
        Log.v(TAG, ":onBind");
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

        try{
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


/* ********************************************************************************************** */

    public void register(ConnectivityCallback cc, UserManager.UserChangeCallback uc){
        if (cc != null){
            mCallbacks.add(cc);
        }

        if (uc != null){
            mUserManager.register(uc);
        }
    }


/* ********************************************************************************************** */

    private void init(){
        Log.v(TAG, "init()");
        ServiceCallback scb = new ServiceCallback();
        mConnector.register(scb);
        mRecorder.register(scb);
        mStreamer.register(scb);

        SessionManager.getInstance();

        Myself.makeMyself(this.getApplicationContext());
        registerActions();
        (new Timer()).schedule((new BeatHearting()), 10, TIMER_INTERVAL);

        try{
            fos = openFileOutput("audio.pcm", Context.MODE_APPEND);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void registerActions(){
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_BOOT_COMPLETED);
        intentFilter.addAction(Intent.ACTION_SCREEN_ON);
        intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
        intentFilter.addAction(Intent.ACTION_AIRPLANE_MODE_CHANGED);
        //intentFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION);
        intentFilter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        getApplicationContext().registerReceiver(new MyReceiver(), intentFilter);
    }

    private void heartBeat(){
        User me = Myself.fromMyself(getApplicationContext());
        Log.v(TAG, "MYSELF: " + me.toJsonString());
        try {
            mConnector.broadcast(me.toJsonString().getBytes("UTF-8"),
                    me.toJsonString().getBytes("UTF-8").length);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        mUserManager.updateState();
    }


/* ********************************************************************************************** */

    public class MyBinder extends Binder{
        public MyService getService(){
            return MyService.this;
        }
    }

/* ********************************************************************************************** */

    public class MyReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent){
            Log.v(TAG, "onReceive: " + intent);
            String action = intent.getAction();
            boolean connectivity = false;

            if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)){

                int networkType = intent.getIntExtra(ConnectivityManager.EXTRA_NETWORK_TYPE, -1);
                if ( networkType == ConnectivityManager.TYPE_WIFI){
                    connectivity = intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false);
                    for (ConnectivityCallback cb : mCallbacks) {
                        cb.onWifiConnectivity(connectivity);
                    }
                }

                if ( networkType == ConnectivityManager.TYPE_MOBILE){
                    connectivity = intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false);
                    for (ConnectivityCallback cb : mCallbacks) {
                        cb.onMobileConnectivity(connectivity);
                    }
                }

                Myself.updateAddress(getApplicationContext());
            }

            heartBeat();
        }
    }

/* ********************************************************************************************** */

    public class ServiceCallback implements Connector.MessageCallback,
            Recorder.AudioCallback,
            Streamer.StreamCallback{
        @Override
        public void onUpdateUser(byte[] data, int length) {
            Log.v(TAG, "onUpdateUser: " + length);
            try {
                String s = new String(data, 0, length, "UTF-8");
                Log.v(TAG, "Incoming: " + s);
                mUserManager.update(data, length);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onAudioStreamOutput(byte[] bytes, int length) {
            Log.v(TAG, "onAudioStreamOutput: " + length);
            mStreamer.flush(bytes, length);
        }

        @Override
        public void onStreamEnding() {
            Log.v(TAG, "onStreamEnding: ");
            mStreamer.stopStream();
        }

        @Override
        public void onStreamInput(byte[] data, int length) {
            Log.v(TAG, "onStreamInput: " + length);
            //mTracker.play(data, length);

            try{
                fos.write(data, 0, length);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

/* ********************************************************************************************** */

    public interface ConnectivityCallback{
        void onWifiConnectivity(boolean connected);
        void onMobileConnectivity(boolean connected);
    }

/* ********************************************************************************************** */

    class BeatHearting extends TimerTask{
        @Override
        public void run() {
            heartBeat();
        }
    }
}
