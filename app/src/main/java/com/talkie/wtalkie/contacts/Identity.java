package com.talkie.wtalkie.contacts;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
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

    private Identity mInstance;
    private Context mContext;

    private TelephonyManager mTm;
    private WifiManager mWm;

    public Identity(Context c){
        mContext = c;
        mTm = (TelephonyManager) c.getSystemService(Context.TELEPHONY_SERVICE);
        mWm = (WifiManager) c.getSystemService(Context.WIFI_SERVICE);
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
                str = mTm.getDeviceId();
            } catch (SecurityException e) {
                //
            }
        }
        return str;
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
