package com.talkie.wtalkie.sessions;

/*
**
** ${FILE}
**   ...
**
** REVISED HISTORY
**   yl7 | 18-3-17: Created
**
*/

import android.util.Log;
import com.google.gson.Gson;
import com.talkie.wtalkie.contacts.UserManager;
import org.litepal.crud.DataSupport;
import java.util.ArrayList;
import java.util.List;

/*
** ********************************************************************************
**
** Session
**   This is data class for database and socket bytes encoding with json
**
** USAGE:
**   ......
**
** ********************************************************************************
*/
public class Session extends DataSupport{
    private static final String TAG = "Session";
    // type
    public static final int SESSION_TYPE_UNKNOWN = 0xD0; // 208
    public static final int SESSION_TYPE_CHAT_ROOM = 0xD1;
    public static final int SESSION_TYPE_TALK_CHANNEL = 0xD2;
    public static final int SESSION_TYPE_TEMPORARY = 0xD3;
    //public static final int SESSION_TYPE_TEMP_P2P = 0xD4;
    //public static final int SESSION_TYPE_TEMP_GROUP = 0xD5;

    // state
    public static final int SESSION_NOT_INITIALIZED = 0xDA; // 218
    public static final int SESSION_INACTIVE = 0xDB;
    public static final int SESSION_ACTIVE = 0xDC;

    // data members
    private long sid;    // session id, main key, same as time
    private long time;   // session start time with milliseconds
    private int type;    // session type
    private int state;   // session active state
    private String name; // session name

    /*
    *  user uid list
    *  The first uis is originator
    */
    private final List<String> users = new ArrayList<>();


/* ********************************************************************************************** */

    public Session(){
        Log.v(TAG, "new default session");

        // auto generated, user can not change!
        this.sid = System.currentTimeMillis();
        this.time = sid;
        this.state = SESSION_NOT_INITIALIZED;
        this.type = SESSION_TYPE_UNKNOWN;
    }

    public Session(long sid, String name, int type){
        Log.v(TAG, "new session with id :" + sid);

        // auto generated, user can not change!
        this.sid = sid;
        this.name = name;
        this.type = type;
        this.time = System.currentTimeMillis();
        this.state = SESSION_INACTIVE;
    }

    public Session(List<String> users) {
        Log.v(TAG, "new session: " + users.size());

        // session time and state
        this.sid = System.currentTimeMillis();
        this.time = sid;
        this.state = SESSION_INACTIVE;
        this.users.addAll(users);
        buildNameAndType(users);
    }

    public static Session decode(String jsonStr){
        Gson gson = new Gson();
        return gson.fromJson(jsonStr, Session.class);
    }

    public static Session decode(byte[] bytes, int length){
        Gson gson = new Gson();
        String str = new String(bytes, 0, length);
        return gson.fromJson(str, Session.class);
    }

/* ********************************************************************************************** */

    public boolean has(List<String> users){
        boolean existed = false;

        if (users == null || users.isEmpty()){
            Log.e(TAG, "parameter has null or empty!");
            return false;
        }

        if (this.users.size() != users.size()){
            return false;
        }

        for (String r : users){
            int count = 0;
            for (String now : this.users){
                count++;
                if (r.equals(now)) {
                    existed = true;
                    break;
                } else {
                    existed = false;
                }
            }

            if (count >= this.users.size() && !existed){
                break;
            }
        }

        return existed;
    }

    public String encode(){
        Gson gson = new Gson();
        return gson.toJson(this, Session.class);
    }

    public void dump(){
        Log.v(TAG, encode());
    }

/* ********************************************************************************************** */

    private void buildNameAndType(List<String> uids){
        Log.v(TAG, "buildNameAndType: " + uids.size());
        if (uids.isEmpty()){
            Log.v(TAG, "no any users !!!");
            return;
        }

        StringBuilder sb = new StringBuilder();
        int size = uids.size();
        for (int i = 0; i < size; i++){
            sb.append(UserManager.findByUid(uids.get(i)).getNick());
            if (i < size - 1){
                sb.append(",");
            }
        }
        this.name = sb.toString();
        this.type = SESSION_TYPE_TEMPORARY;
    }

/* ********************************************************************************************** */

    public long getSid() {
        return sid;
    }

    public long getTime() {
        return time;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public List<String> getUsers() {
        return users;
    }

    public void addUser(String uid){
        this.users.add(uid);
    }

    public void addUsers(List<String> users) {
        this.users.addAll(users);
    }
}
