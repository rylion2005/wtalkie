package com.talkie.wtalkie.sessions;

import com.talkie.wtalkie.contacts.User;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
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
public class Message {

    private long sentTime;     // use as message id
    private Session session;    // in which session
    private User originator; // originator id
    private int  type;         // Message type
    private byte[] body;       // message body

/* ********************************************************************************************** */

/* ********************************************************************************************** */


    public long getSentTime() {
        return sentTime;
    }

    public void setSentTime(long sentTime) {
        this.sentTime = sentTime;
    }

    public Session getSession() {
        return session;
    }

    public void setSession(Session session) {
        this.session = session;
    }

    public User getOriginator() {
        return originator;
    }

    public void setOriginator(User originator) {
        this.originator = originator;
    }

    public byte[] getBody() {
        return body;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public void setBody(byte[] body) {
        this.body = body;
    }
}
