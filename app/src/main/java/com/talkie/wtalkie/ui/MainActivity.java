package com.talkie.wtalkie.ui;


import android.os.Bundle;

import com.talkie.wtalkie.R;



public class MainActivity extends BaseActivity {

    private SessionsFragment mSessionsFragment;
    private ContactsFragment mContactsFragment;
    private ProfileFragment mProfileFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
}
