package com.talkie.wtalkie.contacts;


import android.content.ContentResolver;
import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Enumeration;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    public String genUid(){
        String id = null;
        id = getDeviceID();
        if (id == null) {
            id = getImei();
            if (id == null) {
                id = getMeid();
                if (id == null) {
                    id = getSerial();
                }
            }
        }
        return id;
    }

    public String genUuid(){
        String id = UUID.randomUUID().toString();
        Log.v(TAG, "Uuid: " + id);
        return id;
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

        Log.v(TAG, "imei: " + str);
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
        Log.v(TAG, "meid: " + str);
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
        Log.v(TAG, "SubscriberId: " + str);
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
        Log.v(TAG, "SimSerialNumber: " + str);
        return str;
    }

    public String getDeviceID(){
        String str = null;
        try {
            str = mTm.getDeviceId();
        } catch (SecurityException e) {
            //
        }
        return str;
    }


    public String getSerial(){
        String str = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                str = android.os.Build.getSerial();
            } catch (SecurityException e) {
                //
            }
        } else {
            try {
                str = getAndroidId();
            } catch (SecurityException e) {
                //
            }
        }
        Log.v(TAG, "serial: " + str);
        return str;
    }

    public String getAndroidId(){
        return Settings.Secure.getString(mCr, Settings.Secure.ANDROID_ID);
    }

    public String getModel(){
        return Build.MODEL;
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

    public static String getInternetAddress() {
        URL infoUrl = null;
        InputStream inStream = null;
        String ipLine = "";
        HttpURLConnection httpConnection = null;
        try {
            // infoUrl = new URL("http://ip168.com/");
            infoUrl = new URL("http://pv.sohu.com/cityjson?ie=utf-8");
            URLConnection connection = infoUrl.openConnection();
            httpConnection = (HttpURLConnection) connection;
            int responseCode = httpConnection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                inStream = httpConnection.getInputStream();
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(inStream, "utf-8"));
                StringBuilder strber = new StringBuilder();
                String line = null;
                while ((line = reader.readLine()) != null){
                    strber.append(line + "\n");
                }
                Pattern pattern = Pattern.compile("((?:(?:25[0-5]|2[0-4]\\d|((1\\d{2})|([1-9]?\\d)))\\.){3}(?:25[0-5]|2[0-4]\\d|((1\\d{2})|([1-9]?\\d))))");
                Matcher matcher = pattern.matcher(strber.toString());
                if (matcher.find()) {
                    ipLine = matcher.group();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                inStream.close();
                httpConnection.disconnect();
            } catch (IOException|NullPointerException e) {
                e.printStackTrace();
            }
        }
        return ipLine;
    }

}
