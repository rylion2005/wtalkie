package com.talkie.wtalkie.contacts;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import org.litepal.LitePal;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by jr on 18-3-15.
 */

/*
** *************************************************************************************************
**
** USERS
**   This is a litepal database manager class
*
*
*  See: https://github.com/LitePalFramework/LitePal
**
** *************************************************************************************************
*/
public class Users {
    private static final String TAG = "Users";
    private static final long OFFLINE_MS = 60000;
    private final List<UserChangeCallback> mCallbacks = new ArrayList<>();

/* ********************************************************************************************** */

    public static List<User> findAll(){
        return User.findAll(User.class);
    }

/* ********************************************************************************************** */

    public Users(){
        SQLiteDatabase usersdb = LitePal.getDatabase();
    }

    public void register(UserChangeCallback cb){
        if (cb != null){
            mCallbacks.add(cb);
        }
    }

    public void update(byte[] bytes, int length){
        if (bytes == null || length == 0){
            return;
        }

        User u = User.fromBytes(bytes, length);
        if (u == null || u.isAllEmptyIds()){
            return;
        }
        Log.v(TAG, "Incoming: " + u.toString());
        u.setState(User.STATE_ONLINE);
        u.setElapse(System.currentTimeMillis());
        u.saveOrUpdate("uuid = ? or serial = ?", u.getUuid(), u.getSerial());
        notifyChange();
    }

    public void updateState(){
        User u = new User();
        u.setState(User.STATE_OFFLINE);
        String currentMs = Long.toString(System.currentTimeMillis()-OFFLINE_MS);
        u.updateAll("elapse < ?", currentMs);
        notifyChange();
    }

/* ********************************************************************************************** */

    private void notifyChange(){
        for (UserChangeCallback uc : mCallbacks) {
            uc.onUserChanged();
        }
    }

/* ********************************************************************************************** */

    public interface UserChangeCallback{
        void onUserChanged();
    }

}
