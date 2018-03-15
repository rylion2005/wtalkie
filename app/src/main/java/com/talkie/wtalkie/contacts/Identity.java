package com.talkie.wtalkie.contacts;


import android.content.ContentResolver;
import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.Settings;
import android.telephony.TelephonyManager;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.UUID;

import static android.text.TextUtils.isEmpty;


/*
**
** ${FILE}
**   ...
**
** REVISED HISTORY
**   yl7 | 18-3-13: Created
**     
*/
public class Identity {
    private static final String TAG = "Identity";

    private static Identity mInstance;
    private TelephonyManager mTm;
    private WifiManager mWm;
    private ContentResolver mCr;


    private Identity(Context c){
        mTm = (TelephonyManager) c.getSystemService(Context.TELEPHONY_SERVICE);
        mWm = (WifiManager) c.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        mCr = c.getContentResolver();
    }

    public static Identity getInstance(Context c){
        if (mInstance == null){
            mInstance = new Identity(c);
        }
        return mInstance;
    }

    public String genShortUuid(){
        String id = UUID.randomUUID().toString();
        String shortId = id.substring(0, id.indexOf('-')) +
                id.substring(id.lastIndexOf('-')+1, id.length());
        return shortId;
    }

    public String getLocalAddress() {

        String hostIp = null;
        try {
            Enumeration nis = NetworkInterface.getNetworkInterfaces();
            InetAddress ia = null;
            while (nis.hasMoreElements()) {
                NetworkInterface ni = (NetworkInterface) nis.nextElement();
                Enumeration<InetAddress> ias = ni.getInetAddresses();
                while (ias.hasMoreElements()) {
                    ia = ias.nextElement();
                    if (ia instanceof Inet6Address) {
                        continue;// skip ipv6
                    }
                    String ip = ia.getHostAddress();
                    if (!"127.0.0.1".equals(ip)) {
                        hostIp = ia.getHostAddress();
                        break;
                    }
                }
            }
        } catch (SocketException e) {
            //TODO
            //e.printStackTrace();
        }
        return hostIp;
    }


    public String getImei(){
        String str = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                str = mTm.getImei();
            } catch (SecurityException e) {
                //
            }
        }
        return str;
    }

    public String getMeid(){
        String str = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                str = mTm.getMeid();
            } catch (SecurityException e) {
                //
            }
        }
        return str;
    }

    public String getSubscriberId(){
        String str = null;
        //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                str = mTm.getSubscriberId();
            } catch (SecurityException e) {
                //
            }
        //}
        return str;
    }

    public String getSimSerialNumber(){
        String str = null;
        //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                str = mTm.getSimSerialNumber();
            } catch (SecurityException e) {
                //
            }
        //}
        return str;
    }


    public String getSerial(){
        String str = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                str = android.os.Build.getSerial();
            } catch (SecurityException e) {

            }
        } else {
            try {
                str = getAndroidId();
            } catch (SecurityException e) {
                //
            }
        }
        return str;
    }

    public String getAndroidId(){
        return Settings.Secure.getString(mCr, Settings.Secure.ANDROID_ID);
    }

    public String getWlanMacAddress () {
        WifiInfo info = mWm.getConnectionInfo();
        String wifiMac = info.getMacAddress();
        if(!isEmpty(wifiMac)){
            return null;
        } else {
            return wifiMac;
        }
    }

    /*
    public String getBluetoothAddress(){
        String str = null;
        BluetoothAdapter mBa = BluetoothAdapter.getDefaultAdapter();
        str = mBa.getAddress();
        return str;
    }
    */

}
