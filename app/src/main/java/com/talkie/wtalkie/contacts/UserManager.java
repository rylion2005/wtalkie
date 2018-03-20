package com.talkie.wtalkie.contacts;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import org.litepal.LitePal;
import org.litepal.crud.DataSupport;

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
public class UserManager {
    private static final String TAG = "Users";
    private static UserManager mInstance;
    private static final long OFFLINE_MS = 20000;
    private final List<UserChangeCallback> mCallbacks = new ArrayList<>();

/* ********************************************************************************************** */

    private UserManager(){}

    public static UserManager getInstance(){
        if (mInstance == null){
            mInstance = new UserManager();
        }
        return mInstance;
    }

    public static List<User> findAll(){
        return User.findAll(User.class);
    }

    public static int getUsersCount(){
        return User.count(User.class);
    }

    public static List<User> getUsers(long[] indexes){
        return User.findAll(User.class, indexes);
    }

    public static User findAt(int index){
        return User.find(User.class, index);
    }

    // FIXME: 18-3-20, optimize by sql
    public static User findByUid(String uid){
        Log.d(TAG, "findByUid: " + uid);
        User user = null;
        for (User u : User.findAll(User.class)){
            if (u.getUid().equals(uid)){
                user = u;
                Log.d(TAG, "Found !!!!!!");
                break;
            }
        }

        return user;
    }


/* ********************************************************************************************** */

    public void register(UserChangeCallback cb){
        if (cb != null){
            mCallbacks.add(cb);
        }
    }

    public void update(byte[] bytes, int length){
        User u = User.fromBytes(bytes, length);
        Log.v(TAG, "Incoming: " + u.toJsonString());
        u.setState(User.STATE_ONLINE);
        u.setElapse(System.currentTimeMillis());
        u.saveOrUpdate("uid = ?", u.getUid());
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
