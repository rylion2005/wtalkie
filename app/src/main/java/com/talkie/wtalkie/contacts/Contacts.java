package com.talkie.wtalkie.contacts;

import android.content.Context;
import android.content.SharedPreferences;
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

    private static final String MYSELF = "myself";
    private static final String CONTACTS_DATABASE = "contacts.csv";

    private static Contacts mInstance;

    private Context mContext;
    private Identity mIdentity;

    private final List<Callback> mCallbacks = new ArrayList<>();


/* ********************************************************************************************** */


    private Contacts(Context context){
        Log.v(TAG, "new Contacts");
        mContext = context;
        mIdentity = Identity.getInstance(context);
        fromSharePreference();
    }

    public static Contacts newInstance(Context context){
        if (mInstance == null){
            mInstance = new Contacts(context);
        }
        return mInstance;
    }

    public static Contacts getInstance(){
        Log.v(TAG, "get instance");
        return mInstance;
    }

    public void register(Callback cb){
        if (cb != null){
            mCallbacks.add(cb);
        }
    }

    public User getMyself(){
        return fromSharePreference();
    }

    public List<User> getContacts(){
        return read();
    }

    // notify myself's status changed
    public void onUpdateMyself(){
        for (Callback cb : mCallbacks){
            cb.onUpdateMyself();
        }
    }

    // notify user information update
    @Override
    public void onUpdateUser(byte[] data, int length){
        Log.v(TAG, "onUpdateUser: " + length);
        User user = User.fromBytes(data, length);
        Log.v(TAG, "Incoming User: " + user.toString());
        update(user);
        for (Callback cb : mCallbacks){
            cb.onUpdateUsers();
        }
    }


/* ********************************************************************************************** */

    private User fromSharePreference(){
        Log.v(TAG, "rebuild myself");
        User user = new User();
        SharedPreferences sp = mContext.getSharedPreferences(MYSELF, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        String uuid = sp.getString("uuid", null);
        if (null == uuid){ // generate an uuid
            uuid = mIdentity.genShortUuid();
            user.setUuid(uuid);
            editor.putString("uuid", uuid);
        }
        editor.putString("serial", mIdentity.getSerial());
        editor.putString("address", mIdentity.getLocalAddress());
        editor.apply();

        user.setUuid(sp.getString("uuid", null));
        user.setSerial(mIdentity.getSerial());
        user.setAddress(mIdentity.getLocalAddress());
        return user;
    }

    private void update(User user){

        if (user == null){
            return;
        }

        if ((user.getUuid() == null) && (user.getSerial() == null)){
            Log.v(TAG, "no valid Ids");
            return;
        }

        List<User> users = read();
        if (users.isEmpty()){
            users.add(user);
        } else {
            for (User u : users){
                // check serial firstly
                if ((u.getSerial() != null)
                        && (user.getSerial() != null)
                        && u.getSerial().equals(user.getSerial())){
                    u.setUuid(user.getUuid());
                    u.setAddress(user.getAddress());
                    break;
                } else { // check UUID
                    if ((u.getUuid() != null)
                            && (user.getUuid() != null)
                            && u.getUuid().equals(user.getUuid())) {
                        u.setSerial(user.getSerial());
                        u.setAddress(user.getAddress());
                        break;
                    } else {
                        users.add(user);
                    }
                }
            }
        }
        Log.v(TAG, "User List: " + users.size());
        rebuild(users);
    }

    private List<User> read(){
        List<User> users = new ArrayList<>();

        try{
            FileInputStream fis = mContext.openFileInput(CONTACTS_DATABASE);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(isr);
            while (true){
                String line = br.readLine();
                if (line == null){
                    break;
                }
                Log.v(TAG, "read: " + line);
                users.add(User.fromBytes(line.trim().getBytes(), line.trim().length()));
            }
            fis.close();
        } catch (IOException e) {
            Log.e(TAG, "Contacts no database");
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

    public interface Callback {
        void onUpdateMyself();
        void onUpdateUsers();
    }
}
