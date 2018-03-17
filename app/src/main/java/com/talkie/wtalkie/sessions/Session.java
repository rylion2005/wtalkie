package com.talkie.wtalkie.sessions;

import com.talkie.wtalkie.contacts.User;

import java.util.ArrayList;
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
    public static final int SESSION_TYPE_P2P = 0xD1;
    public static final int SESSION_TYPE_GROUP = 0xD2;

    public static final int SESSION_INACTIVE = 0xDA;
    public static final int SESSION_ACTIVE = 0xDB;

    private String name;
    private long sessionId;            // sent time of first message
    private long originateTime;        // sent time of first message
    private int type;
    private int sessionState;
    private User originator;           // originator
    private final List<User> participants = new ArrayList<>();   // receivers
    private final List<Message> messages = new ArrayList<>();    // messages

    public Session() {
        originateTime = System.currentTimeMillis();
        sessionId = originateTime;
        sessionState = SESSION_ACTIVE;
    }

    public Session(int type, User originator) {
        originateTime = System.currentTimeMillis();
        sessionId = originateTime;
        sessionState = SESSION_ACTIVE;
        this.type = type;
        this.originator = originator;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getSessionId() {
        return sessionId;
    }

    public void setSessionId(long sessionId) {
        this.sessionId = sessionId;
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

    public int getSessionState() {
        return sessionState;
    }

    public void setSessionState(int sessionState) {
        this.sessionState = sessionState;
    }

    public User getOriginator() {
        return originator;
    }

    public void setOriginator(User originator) {
        this.originator = originator;
    }

    public List<User> getParticipants() {
        return participants;
    }

    public void addParticipants(List<User> participants) {
        this.participants.addAll(participants);
    }

    public List<Message> getMessages() {
        return messages;
    }

    public void addMessages(List<Message> messages) {
        this.messages.addAll(messages);
    }
}
