package com.talkie.wtalkie.ui;


import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;

import com.talkie.wtalkie.R;
import com.talkie.wtalkie.services.MyService;


public class MainActivity extends BaseActivity {
    private static final String TAG = "MainActivity";

    private SessionsFragment mSessionsFragment;
    private ContactsFragment mContactsFragment;
    private ProfileFragment mProfileFragment;

    private MyService mService;


/* ********************************************************************************************** */


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
/*
        bindService(new Intent(this, MyDaemonService.class),
                new DaemonServiceConnection(),
                BIND_AUTO_CREATE);
*/
        initFragments();
    }

    public void refreshSessions(View view){
        replaceFragment(mSessionsFragment, "SessionsFragment");
    }

    public void refreshContacts(View view){
        replaceFragment(mContactsFragment, "ContactsFragment");
    }

    public void refreshProfile(View view){
        replaceFragment(mProfileFragment, "ProfileFragment");
    }


/* ********************************************************************************************** */


    private void initFragments(){
        mSessionsFragment = new SessionsFragment();
        mContactsFragment = new ContactsFragment();
        mProfileFragment = new ProfileFragment();
        //mContactsFragment.updateContacts(mLanScanner.getNeighbours());
        //mContactsFragment.refreshContactViews();
        //mProfileFragment.updateLocalAddress(mLanScanner.getMyLocalAddress());
        //mProfileFragment.updateInternetAddress(mLanScanner.getMyInternetAddress());
        //mProfileFragment.refreshAddressViews();
        addFragment(mSessionsFragment, "SessionsFragment");
    }

    private void addFragment(Fragment fragment, String tag) {
        FragmentManager fm = this.getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.add(R.id.FRL_FragmentContainer, fragment, tag);
        ft.commit();
    }

    private void replaceFragment(Fragment fragment, String tag) {
        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.FRL_FragmentContainer, fragment, tag);
        ft.commit();
    }



/* ********************************************************************************************** */


    class MyServiceConnection implements ServiceConnection{
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.v(TAG, "onServiceConnected: " + name);
            mService = ((MyService.MyBinder) service).getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }

        @Override
        public void onBindingDied(ComponentName name) {

        }
    }
}
