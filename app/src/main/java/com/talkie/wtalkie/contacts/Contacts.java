package com.talkie.wtalkie.contacts;

import android.content.Context;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.talkie.wtalkie.sockets.Connector;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
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
public class Contacts implements Connector.Callback{
    private static final String TAG = "Contacts";

    private static final String MYSELF = "myself.db";
    private static final String CONTACTS_DATABASE = "contacts.db";
    private static final int MAX_BYTES_LENGTH = 1024;
    private static Contacts mInstance;

    private Context mContext;

    private final List<Callback> mCallbacks = new ArrayList<>();
    private final List<User> mUserList = new ArrayList<>();


/* ********************************************************************************************** */


    private Contacts(Context context){
        Log.v(TAG, "new Contacts");
        mContext = context;
        initMyself();
        List<User> users = read();
        if (users != null) {
            mUserList.addAll(users);
        }
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

    public List<User> getContacts(){
        return mUserList;
    }

    @Override
    public void onUpdateUser(byte[] data, int length){
        Log.v(TAG, "onUpdateUser: " + length);
        User user = fromBytes(data, length);
        update(user);
        for (Callback cb : mCallbacks){
            cb.onUpdateUsers();
            cb.onUpdateMyself();
        }
    }


/* ********************************************************************************************** */

    private User initMyself(){
        User user = null;
        try {
            FileInputStream fis = mContext.openFileInput(MYSELF);
            if (fis.available() > 0){
                byte[] bytes = new byte[MAX_BYTES_LENGTH];
                int count = fis.read(bytes);
                Log.v(TAG, "Myself: " + count);
                user = fromBytes(bytes, count);
                Log.v(TAG, "User: " + user.toString());
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
        Log.v(TAG, "fromBytes: " + length);
        User user = new User();
        String buffer = new String(bytes);
        String uuid = buffer.substring(0, buffer.indexOf(','));
        String ip = buffer.substring(buffer.indexOf(',')+1, length);
        user.setUuid(uuid);
        user.setAddress(ip);
        Log.v(TAG, "Uuid: " + uuid);
        Log.v(TAG, "address: " + ip);
        return user;
    }

    private User fromString(String data){
        if (data == null){
            return null;
        }

        User user = new User();
        String uuid = data.substring(0, data.indexOf(','));
        String ip = data.substring(data.indexOf(',') + 1, data.length());
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

    private void update(User user){
        boolean existed = false;
        if (user == null){
            return;
        }

        for (User u : mUserList){
            if (u.getUuid().equals(user.getUuid())){
                existed = true;
                if (u.getAddress() == null){
                    mUserList.remove(u);
                } else {
                    u.setAddress(user.getAddress());
                }
                break;
            }
        }

        if (!existed){
            mUserList.add(user);
        }

        rebuild(mUserList);
    }

    private List<User> read(){
        List<User> users = null;

        try{
            FileInputStream fis = mContext.openFileInput(CONTACTS_DATABASE);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(isr);
            users = new ArrayList<>();
            while (true){
                String line = br.readLine();
                if (line == null){
                    break;
                }
                Log.v(TAG, "read: " + line);
                users.add(fromString(line.trim()));
            }
            fis.close();
        } catch (IOException e) {
            Log.e(TAG, "read: exception");
            //e.printStackTrace();
        }

        return users;
    }

    private void rebuild(List<User> users){
        if ((users == null) || users.isEmpty()){
            mContext.deleteFile(CONTACTS_DATABASE);
            return;
        }

        try {
            FileOutputStream fos = mContext.openFileOutput(CONTACTS_DATABASE, Context.MODE_APPEND);
            for (User u : users){
                Log.v(TAG, "write: " + u.toString());
                fos.write(u.toString().getBytes());
            }
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


/* ********************************************************************************************** */

        


/* ********************************************************************************************** */

    public interface Callback {
        void onUpdateMyself();
        void onUpdateUsers();
    }
}
