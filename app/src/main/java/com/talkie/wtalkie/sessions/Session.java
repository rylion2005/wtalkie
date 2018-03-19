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
    private long time;   // use it as unique id
    private String name; // session name
    private int type;    // session type
    private int state;   // session active state
    private User originator;   // originator
    // receivers uid list
    private final List<User> participants = new ArrayList<>();
    // message id list
    private final List<Message> messages = new ArrayList<>();


/* ********************************************************************************************** */

    public Session(){
        Log.v(TAG, "new default session");
        this.time = System.currentTimeMillis();
        this.name = "Test Session";
        this.type = SESSION_TYPE_UNKNOWN;
        this.state = SESSION_NOT_INITIALIZED;

        User u1 = new User();
        User u2 = new User();
        User u3 = new User();
        this.originator = u1;
        this.participants.add(u2);
        this.participants.add(u3);
    }

    public Session(User originator, List<User> participants) {
        Log.v(TAG, "new session: " + participants.size());

        // session time and state
        this.time = System.currentTimeMillis();
        this.state = SESSION_ACTIVE;

        this.originator = originator;
        this.participants.addAll(participants);

        // session type and session name
        StringBuilder sb = new StringBuilder();
        if(participants.size() > 1){
            this.type = SESSION_TYPE_GROUP;
            sb.append(participants.get(0).getNick());
            sb.append(",");
            sb.append(participants.get(1).getNick());
            sb.append("...");
        } else if (participants.size() > 0){
            this.type = SESSION_TYPE_P2P;
            sb.append(participants.get(0).getNick());
        } else {
            this.type = SESSION_TYPE_UNKNOWN;
            sb.append("error receivers !!!");
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

        if ((this.originator == null) || (this.participants == null)){
            return false;
        }

        if (originator.getUid().equals(this.originator)
                && (participants.size() == this.participants.size())){
            boolean found = false;
            int count = 0;
            for (User newer : participants){
                for (User elder : this.participants){
                    if (newer.getUid().equals(elder.getUid())){
                       found = true;
                       count++;
                       break;
                    }
                }

                if (!found){ // nothing found, go home directly
                    break;
                } else {
                    found = false; // reset status
                }
            }

            // found count is completely same
            if (count == participants.size()){
                existed = true;
                Log.v(TAG, "session found");
            } else {
                Log.v(TAG, "session not found");
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

    public User getOriginator() {
        return originator;
    }

    public void setOriginator(User originator) {
        this.originator = originator;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public List<User> getParticipants() {
        return participants;
    }

    public void addParticipants(User u){
        participants.add(u);
    }

    public List<Message> getMessages() {
        return messages;
    }

    public void addMessage(Message msg){
        messages.add(msg);
    }

    public void dump(){
        Log.v(TAG, encode());
    }
}
