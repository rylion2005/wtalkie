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

import android.text.TextUtils;
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
    private int unreadCount; // unread message count
    private String name;     // session name
    private String originator; // originator uid
    private final List<String> receivers = new ArrayList<>(); // receivers uid


/* ********************************************************************************************** */

    public Session(){
        Log.v(TAG, "new default session");

        // auto generated, user can not change!
        this.sid = System.currentTimeMillis();
        this.time = sid;

        this.state = SESSION_NOT_INITIALIZED;
        this.originator = "default";
        this.unreadCount = 0;
        this.type = SESSION_TYPE_UNKNOWN;
        this.name = "default";
    }

    public Session(long sid, String name, int type){
        Log.v(TAG, "new fixed session with id :" + sid);

        // auto generated, user can not change!
        this.sid = sid;
        this.name = name;
        this.type = type;

        this.state = SESSION_INACTIVE;
        this.unreadCount = 0;

        this.time = System.currentTimeMillis();
        this.originator = "default";
    }

    public Session(String originatorUid, List<String> receivers) {
        Log.v(TAG, "new session: " + originatorUid);

        // session time and state
        this.sid = System.currentTimeMillis();
        this.time = sid;
        this.state = SESSION_INACTIVE;
        this.originator = originatorUid;
        this.receivers.addAll(receivers);
        buildNameAndType(receivers);
    }

    public static Session decode(String jsonStr){
        Gson gson = new Gson();
        return gson.fromJson(jsonStr, Session.class);
    }

/* ********************************************************************************************** */

    // FIXME: 18-3-20, optimize by database sql sentence
    public boolean has(String uid, List<String> receivers){
        boolean existed = false;

        if (TextUtils.isEmpty(uid) || receivers == null || receivers.isEmpty()){
            Log.e(TAG, "parameter has null or empty!");
            return false;
        }

        if (!this.originator.equals(uid)){
            return false;
        }

        if (this.receivers.size() != receivers.size()){
            return false;
        }

        for (String r : receivers){
            int count = 0;
            for (String now : this.receivers){
                count++;
                if (r.equals(now)) {
                    existed = true;
                    break;
                } else {
                    existed = false;
                }
            }

            if (count >= this.receivers.size() && !existed){
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
        if (uids == null || uids.size() == 0){
            Log.v(TAG, "no any receiver !!!");
            return;
        }

        StringBuilder sb = new StringBuilder();
        int size = uids.size();
        for (int i = 0; i < size; i++){
            sb.append(UserManager.findByUid(uids.get(i)).getNick());
            if (i < size -1){
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

    public String getOriginator() {
        return originator;
    }

    public int getUnreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(int unreadCount) {
        this.unreadCount = unreadCount;
    }

    public void setOriginator(String originator) {
        this.originator = originator;
    }

    public List<String> getReceivers() {
        return receivers;
    }

    public void addReceiver(String uid){
        this.receivers.add(uid);
    }

    public void setReceivers(List<String> receivers) {
        this.receivers.addAll(receivers);
    }
}
