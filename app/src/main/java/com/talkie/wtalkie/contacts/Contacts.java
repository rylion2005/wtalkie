package com.talkie.wtalkie.contacts;

import android.content.Context;
import android.content.SharedPreferences;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.talkie.wtalkie.sockets.Connector;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
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

    private static final String CONTACTS_DATABASE = "contacts.csv";

    private static Contacts mInstance;

    private final List<UserChangeCallback> mCallbacks = new ArrayList<>();


/* ********************************************************************************************** */


    private Contacts(){
        Log.v(TAG, "new Contacts");
    }

    public static Contacts getInstance(){
        if (mInstance == null){
            mInstance = new Contacts();
        }
        return mInstance;
    }

    public void register(UserChangeCallback cb){
        if (cb != null){
            mCallbacks.add(cb);
        }
    }

    public void updateDatabase(Context c, User user){
        Log.d(TAG, "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx");
        boolean updated = false;

        if (user == null){
            return;
        }

        // no valid identities
        if ((user.getUuid() == null) && (user.getSerial() == null)){
            Log.v(TAG, "no valid Ids");
            return;
        }

        Log.d(TAG, "Incoming/User: " + user.toString());
        // read from database
        List<User> users = fromDatabase(c);
        Log.d(TAG, "Database size: " + users.size());
        // check if there is a same user
        for (User u : users){
            Log.d(TAG, "Database/User: " + u.toString());

            // find a equaled user
            if (u.equals(user)){
                Log.d(TAG, "Totally same user !!!!!!");
                return;
            }

            // check serial firstly
            if (u.sameSerial(user)){
                Log.d(TAG, "same serial");
                u.setUuid(user.getUuid());
                u.setAddress(user.getAddress());
                updated = true;
                break;
            } else { // check UUID
                if (u.sameUuid(user)) {
                    Log.d(TAG, "same uuid");
                    u.setSerial(user.getSerial());
                    u.setAddress(user.getAddress());
                    updated = true;
                    break;
                }
            }
        }

        // append to list
        if (!updated){
            users.add(user);
        }

        // write into database
        rebuild(c, users);
        users.clear();

        // notify clients
        for (UserChangeCallback cb : mCallbacks){
            cb.onUpdateUsers();
        }

        Log.d(TAG, "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
    }

    public List<User> fromDatabase(Context c){
        List<User> users = new ArrayList<>();
        Log.d(TAG, "fromDatabase");
        try{
            FileInputStream fis = c.openFileInput(CONTACTS_DATABASE);
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
            Log.e(TAG, "no database");
            //e.printStackTrace();
        }

        return users;
    }


/* ********************************************************************************************** */


    private void rebuild(Context c, List<User> users){
        Log.d(TAG, "rebuild: " + users.size());
        if (users.isEmpty()){
            c.deleteFile(CONTACTS_DATABASE);
            return;
        }

        try {
            FileOutputStream fos = c.openFileOutput(CONTACTS_DATABASE, Context.MODE_APPEND);
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
            Log.d(TAG, "rebuild: size=" + users.size());
            for (User u : users){
                bw.write(u.toString());
                bw.newLine();
            }
            bw.close();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


/* ********************************************************************************************** */

    public interface UserChangeCallback {
        void onUpdateUsers();
    }
}
