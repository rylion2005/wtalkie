package com.talkie.wtalkie.contacts;

import android.database.sqlite.SQLiteDatabase;

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
public class Users {

    private UserChangeCallback mCallback;

    public Users(){
        SQLiteDatabase usersdb = LitePal.getDatabase();

    }

    public void register(UserChangeCallback cb){
        if (cb != null){
            cb.onUserChanged();
        }
    }

    public static List<User> findAll(){
        return User.findAll(User.class);
    }

    public void update(byte[] bytes, int length){
        if (bytes == null || length == 0){
            return;
        }

        User u = User.fromBytes(bytes, length);
        if (u == null || u.isAllEmptyIds()){
            return;
        }

        u.setState(User.STATE_ONLINE);
        u.setElapse(System.currentTimeMillis());
        u.saveOrUpdate("uuid = ?", u.getUuid());
        if (mCallback != null) {
            mCallback.onUserChanged();
        }
    }

    public void updateState(){
        User u = new User();
        u.setState(User.STATE_OFFLINE);
        u.updateAll("elapse - 60000 > ?", "0");
        if (mCallback != null) {
            mCallback.onUserChanged();
        }
    }


/* ********************************************************************************************** */

    public interface UserChangeCallback{
        void onUserChanged();
    }
}
