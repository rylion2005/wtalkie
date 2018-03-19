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
import com.talkie.wtalkie.contacts.User;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.List;

/*
** ********************************************************************************
**
** METHODNAME
**   ......
**
** USAGE:
**   ......
**
** ********************************************************************************
*/
public class Session extends DataSupport{
    private static final String TAG = "Session";
    // type
    public static final int SESSION_TYPE_UNKNOWN = 0xD0;
    public static final int SESSION_TYPE_P2P = 0xD1;
    public static final int SESSION_TYPE_GROUP = 0xD2;

    // state
    public static final int SESSION_NOT_INITIALIZED = 0xDA;
    public static final int SESSION_INACTIVE = 0xDB;
    public static final int SESSION_ACTIVE = 0xDC;

    // data members
    private long time;   // session main key
    private String name; // session name
    private int type;    // session type
    private int state;   // session active state
    private String uidFrom;   // originator uid
    private String uidTo;     // receiver uid only for p2p session
    // receivers uid list
    // private final List<User> participants = new ArrayList<>();
    // message id list
    // private final List<Message> messages = new ArrayList<>();


/* ********************************************************************************************** */

    public Session(){
        Log.v(TAG, "new default session");
        this.time = System.currentTimeMillis();
        this.name = "Test Session";
        this.type = SESSION_TYPE_UNKNOWN;
        this.state = SESSION_NOT_INITIALIZED;
        this.uidFrom = "";
        this.uidTo = "";
    }

    public Session(User originator, List<User> participants) {
        Log.v(TAG, "new session: " + originator.getUid());

        // session time and state
        this.time = System.currentTimeMillis();
        this.state = SESSION_ACTIVE;

        this.uidFrom = originator.getUid();
        //this.participants.addAll(participants);

        // session type and session name
        StringBuilder sb = new StringBuilder();
        if(participants.size() > 1){
            this.type = SESSION_TYPE_GROUP;
            sb.append(participants.get(0).getNick());
            sb.append(",");
            sb.append(participants.get(1).getNick());
            sb.append("...");
            this.uidTo = "";
        } else if (participants.size() > 0){
            this.type = SESSION_TYPE_P2P;
            sb.append(participants.get(0).getNick());
            this.uidTo = participants.get(0).getUid();
        } else {
            this.type = SESSION_TYPE_UNKNOWN;
            sb.append("error receivers !!!");
            this.uidTo = "";
        }
        this.name = sb.toString();
    }

    public static Session decode(String jsonStr){
        Gson gson = new Gson();
        return gson.fromJson(jsonStr, Session.class);
    }


/* ********************************************************************************************** */

    public boolean existed(User originator, List<User> participants){
        boolean existed = false;

        if ((originator == null) || (participants == null)){
            return false;
        }

        if (this.uidFrom == null){
            return false;
        }

        if (!originator.getUid().equals(this.uidFrom)){
            return false;
        }

        if (participants.size() > 1){
            return true;
        }

        if (participants.size() == 1){
            if (participants.get(0).getUid().equals(this.uidTo)){
                existed = true;
            }
        }

        return existed;
    }

    public String encode(){
        Gson gson = new Gson();
        return gson.toJson(this, Session.class);
    }

/* ********************************************************************************************** */

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
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

    public String getUidFrom() {
        return uidFrom;
    }

    public void setUidFrom(String uidFrom) {
        this.uidFrom = uidFrom;
    }

    public String getUidTo() {
        return uidTo;
    }

    public void setUidTo(String uidTo) {
        this.uidTo = uidTo;
    }

    public void dump(){
        Log.v(TAG, encode());
    }
}
