package com.talkie.wtalkie.services;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.talkie.wtalkie.contacts.User;
import com.talkie.wtalkie.contacts.Users;
import com.talkie.wtalkie.sockets.Connector;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


public class MyService extends Service {
    private static final String TAG = "MyService";

    private static final int TIMER_INTERVAL = 10 * 1000; //ms

    private Connector mConnector;
    private Users mUsers;

    private final List<ConnectivityCallback> mCallbacks = new ArrayList<>();

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
    }


/* ********************************************************************************************** */

    public void register(ConnectivityCallback conn, Users.UserChangeCallback cont){
        if (conn != null) {
            mCallbacks.add(conn);
        }

        if (cont != null){
            mUsers.register(cont);
        }
    }


/* ********************************************************************************************** */

    private void init(){
        Log.v(TAG, "init()");
        mUsers = new Users();
        mConnector = new Connector();
        mConnector.register(new ConnectorCallback());

        registerActions();

        (new Timer()).schedule((new BeatHearting()), 10, TIMER_INTERVAL);
    }

    private void registerActions(){
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_BOOT_COMPLETED);
        //intentFilter.addAction(Intent.ACTION_SCREEN_ON);
        //intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
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
        User.updateSharePreference(getApplicationContext());
        User u = User.fromSharePreference(getApplicationContext());
        mConnector.broadcast(u.toString().getBytes(),u.toString().length());

        mUsers.updateState();
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
            boolean connectivity = false;

            if (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)){

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
            }

            heartBeat();
        }
    }

/* ********************************************************************************************** */

    public class ConnectorCallback implements Connector.MessageCallback {
        @Override
        public void onUpdateUser(byte[] data, int length) {
            mUsers.update(data, length);
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
