package com.talkie.wtalkie.sessions;

import com.google.gson.Gson;
import com.talkie.wtalkie.contacts.Myself;
import com.talkie.wtalkie.contacts.User;

import java.util.List;

/*
**
** ${FILE}
**   ...
**
** REVISED HISTORY
**   yl7 | 18-3-17: Created
**     
*/
public class Session {
    // type
    public static final int SESSION_TYPE_P2P = 0xD1;
    public static final int SESSION_TYPE_GROUP = 0xD2;

    // state
    public static final int SESSION_NOT_INITIALIZED = 0xDA;
    public static final int SESSION_INACTIVE = 0xDB;
    public static final int SESSION_ACTIVE = 0xDC;

    // data members
    private long originateTime; // as unique id
    private String name;
    private int type;
    private int state;
    private User originator;
    private List<User> participants;
    private List<Message> messages;


/* ********************************************************************************************** */

    public Session(){
        originateTime = System.currentTimeMillis();
        state = SESSION_NOT_INITIALIZED;
    }

    public Session(User originator, List<User> participants) {
        originateTime = System.currentTimeMillis();
        state = SESSION_NOT_INITIALIZED;
        this.originator = originator;
        this.participants = participants;
    }

    public static Session decode(String jsonStr){
        Gson gson = new Gson();
        return gson.fromJson(jsonStr, Session.class);
    }


/* ********************************************************************************************** */

    public boolean isSame(User originator, List<User> participants){
        boolean same = true;
        if (originator.getUid().equals(this.originator.getUid())){
            for (User newUser : participants){
                for (User oldUser : this.participants){
                    if (!newUser.getUid().equals(oldUser.getUid())){
                       return false;
                    }
                }
            }
        } else {
            same = false;
        }
        return same;
    }

    public String encode(){
        Gson gson = new Gson();
        String json = gson.toJson(this, Session.class);
        return json;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public long getOriginateTime() {
        return originateTime;
    }

    public void setOriginateTime(long originateTime) {
        this.originateTime = originateTime;
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

    public List<Message> getMessages() {
        return messages;
    }
}
