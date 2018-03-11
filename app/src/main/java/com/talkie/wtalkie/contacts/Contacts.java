package com.talkie.wtalkie.contacts;

import android.content.Context;
import android.util.Log;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.UUID;

/*
**
** ${FILE}
**   ...
**
** REVISED HISTORY
**   yl7 | 18-3-11: Created
**     
*/
public class Contacts {
    private static final String TAG = "Contacts";

    private static final String MYSELF = "myself.db";
    private static final String CONTACTS_DATABASE = "contacts.db";
    private static final int MAX_BYTES_LENGTH = 1024;
    private static Contacts mInstance;

    private Context mContext;

    private final List<Callback> mCallbacks = new ArrayList<>();

/* ********************************************************************************************** */


    private Contacts(Context context){
        Log.v(TAG, "new Contacts");
        mContext = context;
    }

    public static Contacts newInstance(Context context){
        if (mInstance == null){
            mInstance = new Contacts(context);
        }
        return mInstance;
    }

    public static Contacts getInstance(){
        return mInstance;
    }

    public void register(Callback cb){
        if (cb != null){
            mCallbacks.add(cb);
        }
    }

    public User getMyself(){
        return initMyself();
    }


/* ********************************************************************************************** */
    private User initMyself(){
        User user = null;
        try {
            FileInputStream fis = mContext.openFileInput(MYSELF);
            if (fis.available() > 0){
                byte[] bytes = new byte[MAX_BYTES_LENGTH];
                int count = fis.read(bytes);
                user = fromBytes(bytes, count);
            }
            fis.close();
        } catch (IOException e) {
            Log.v(TAG, "failed to read myself");
            //e.printStackTrace();
        }

        if (user == null) {
            user = new User();
            user.setUuid(genUuid());
            user.setAddress(getLocalAddress());

            try{
                mContext.deleteFile(MYSELF);
                FileOutputStream fos = mContext.openFileOutput(MYSELF,Context.MODE_PRIVATE);
                fos.write(user.toString().getBytes());
                fos.close();
            } catch (IOException e) {
                Log.v(TAG, "failed to init myself");
                //e.printStackTrace();
            }
        }

        return user;
    }

    private User fromBytes(byte[] bytes, int length){
        if (bytes == null){
            return null;
        }

        if (length == 0){
            return null;
        }

        User user = new User();
        String buffer = new String(bytes);
        String uuid = buffer.substring(0, buffer.indexOf(','));
        String ip = buffer.substring(buffer.indexOf(',')+1, length+1);
        user.setUuid(uuid);
        user.setAddress(ip);
        return user;
    }

    private String genUuid(){
        String id = UUID.randomUUID().toString();
        Log.v(TAG, "uuid: " + id);
        return id;
    }

    private String getLocalAddress() {

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

/* ********************************************************************************************** */

    interface Callback {
        void onMyselfChanged();
        void onContactsUpdated();
    }
}
