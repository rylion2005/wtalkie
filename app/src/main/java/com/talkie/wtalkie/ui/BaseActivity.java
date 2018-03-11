package com.talkie.wtalkie.ui;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import java.util.ArrayList;


public class BaseActivity extends AppCompatActivity {
    private static final String TAG = "BaseActivity";

    public static final int PERMISSION_REQUEST_CODE = 0xA00F;


/* ********************************************************************************************** */


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0) {
                    for (int result : grantResults) {
                        if (result != PackageManager.PERMISSION_GRANTED) {
                            Log.e(TAG, "Not all permissions are granted");
                        }
                    }
                } else {
                    Log.e(TAG, "unknown permission error");
                }
                break;
            default:
        }
    }


/* ********************************************************************************************** */

    private void init(){
        initPermissions();
    }

    /**
     *  >android 6.0: permission granted must be allowed
     */
    private void initPermissions() {
        final String[] PERMISSIONS = {
                Manifest.permission.INTERNET,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.MODIFY_AUDIO_SETTINGS,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_SETTINGS,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.ACCESS_WIFI_STATE,
                Manifest.permission.CHANGE_WIFI_STATE
        };

        Log.i(TAG, "Init permissions");

        ArrayList<String> permissionList = new ArrayList<>();
        for (String perm : PERMISSIONS) {
            if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(this, perm)) {
                permissionList.add(perm);
            }
        }

        if ( !permissionList.isEmpty() ) {
            String[] permissions = permissionList.toArray(new String[permissionList.size()]);
            ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST_CODE);
        }else{
            Log.i(TAG, "all permissions are granted!");
        }
    }
}
