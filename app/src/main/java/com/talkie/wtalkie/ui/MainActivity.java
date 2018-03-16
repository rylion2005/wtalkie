package com.talkie.wtalkie.ui;


import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.View;

import com.talkie.wtalkie.R;
import com.talkie.wtalkie.contacts.Users;
import com.talkie.wtalkie.services.MyService;


public class MainActivity extends BaseActivity {
    private static final String TAG = "MainActivity";

    private static final int MESSAGE_UPDATE_MYSELF = 0xA0;
    private static final int MESSAGE_UPDATE_USERS = 0xA1;

    private SessionsFragment mSessionsFragment;
    private ContactsFragment mContactsFragment;
    private ProfileFragment mProfileFragment;
    private Fragment mCurrentFragment;

    private MyService mService;

    private final MyServiceConnection mServiceConnection = new MyServiceConnection();
    private final UiHandler mHandler = new UiHandler();


/* ********************************************************************************************** */


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bindService(new Intent(this, MyService.class),
                mServiceConnection,
                BIND_AUTO_CREATE);
        initFragments();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
    }

    public void refreshSessions(View view){
        //replaceFragment(mSessionsFragment);
    }

    public void refreshContacts(View view){
        replaceFragment(mContactsFragment);
    }

    public void refreshProfile(View view){
        replaceFragment(mProfileFragment);
    }


/* ********************************************************************************************** */


    private void initFragments(){
        mSessionsFragment = new SessionsFragment();
        mContactsFragment = new ContactsFragment();
        mProfileFragment = new ProfileFragment();
        addFragment(mProfileFragment);
    }

    private void addFragment(Fragment fragment) {
        mCurrentFragment = fragment;
        FragmentManager fm = this.getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.add(R.id.FRL_FragmentContainer, fragment);
        ft.commit();
    }

    private void replaceFragment(Fragment fragment) {
        mCurrentFragment = fragment;
        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.FRL_FragmentContainer, fragment);
        ft.commit();
    }



/* ********************************************************************************************** */

    class MyServiceConnection implements ServiceConnection{
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.v(TAG, "onServiceConnected: " + name);
            mService = ((MyService.MyBinder) service).getService();
            EventCallback cb = new EventCallback();
            mService.register(cb, cb);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.v(TAG, "onServiceDisconnected: " + name);
        }

        @Override
        public void onBindingDied(ComponentName name) {
            Log.v(TAG, "onBindingDied: " + name);
        }
    }


    class UiHandler extends Handler{
        @Override
        public void handleMessage(Message msg) {
            Log.v(TAG, "handleMessage: " + msg.toString());
            switch (msg.what){
                case MESSAGE_UPDATE_MYSELF:
                    if (mCurrentFragment == mProfileFragment){
                        mProfileFragment.refreshViews();
                    }
                    break;
                case MESSAGE_UPDATE_USERS:
                    if (mCurrentFragment == mContactsFragment){
                        mContactsFragment.refreshViews();
                    }
                    break;
                default:
                    break;
            }
        }
    }

    class EventCallback implements Users.UserChangeCallback, MyService.ConnectivityCallback{

        @Override
        public void onUserChanged() {
            Log.v(TAG, "onUserChanged");
            mHandler.sendEmptyMessage(MESSAGE_UPDATE_USERS);
        }

        @Override
        public void onWifiConnectivity(boolean connected) {
            Log.v(TAG, "onWifiConnectivity");
            mHandler.sendEmptyMessage(MESSAGE_UPDATE_MYSELF);
        }

        @Override
        public void onMobileConnectivity(boolean connected) {
            Log.v(TAG, "onMobileConnectivity");
            mHandler.sendEmptyMessage(MESSAGE_UPDATE_MYSELF);
        }
    }
}
