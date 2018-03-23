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
import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.List;


/*
** ********************************************************************************
**
** Packet
**   Message packet class
**
** USAGE:
**   ......
**
** ********************************************************************************
*/
public class Packet extends DataSupport {
    private static final String TAG = "Packet";

    public static final int MAX_FILE_HEAD_LENGTH = 1024;
    public static final int MAX_MESSAGE_LENGTH = 1024;

    public static final String DEFAULT_ENCODING_FORMAT = "UTF-8";

    public static final int MESSAGE_TYPE_UNKNOWN = 0xAA00;
    public static final int MESSAGE_TYPE_SESSION = 0xAADD;
    public static final int MESSAGE_TYPE_BYTE = 0xAAE0;
    public static final int MESSAGE_TYPE_EMOJI = 0xAAE1;
    public static final int MESSAGE_TYPE_TEXT = 0xAAE2;
    public static final int MESSAGE_TYPE_FILE_UNKOWN = 0xAAE3;
    public static final int MESSAGE_TYPE_FILE_PICTURE = 0xAAE4;
    public static final int MESSAGE_TYPE_FILE_AUDIO = 0xAAE5;
    public static final int MESSAGE_TYPE_FILE_VIDEO = 0xAAE6;

    // relation database key
    private long pid;       // packet id, main key, same as time
    private long sid;       // session sid, session main key
    // meta data
    private long time;
    private int incoming;       // 0 - incoming, 1- outgoing
    private int unread;         // unread status: 0 - read; 1 - unread
    private int type;           // message type
    private String description; // message summary description
    private int messageLength;  // byte message length
    private byte[] messageBody; // byte message body

/* ********************************************************************************************** */

    public Packet() {
        Log.d(TAG, "new default Packet");
        // user can not change!
        this.pid = System.currentTimeMillis();
        this.time = pid;
        this.sid = 0;
        this.incoming = 0;
        this.unread = 1;
        this.description = "";
        this.type = MESSAGE_TYPE_UNKNOWN;
        this.messageLength = 0;
        this.messageBody = null;
    }

    public static Packet decode(byte[] bytes, int length) {
        Gson g = new Gson();
        /*
        *  there are more than packet member data in this source packet,
        *  it will affect new packet structure;
        */
        Packet src = g.fromJson(new String(bytes, 0, length), Packet.class);
        Packet p = new Packet();
        p.clone(src);
        return p;
    }

/* ********************************************************************************************** */

    public byte[] encode() {
        return toJsonString().getBytes();
    }

    public String toJsonString(){
        Gson gson = new Gson();
        return gson.toJson(this, Packet.class);
    }

    public void clone(Packet p){
        this.pid = p.getPid();
        this.time = p.getTime();
        this.sid = p.getSid();
        this.incoming = p.getIncoming();
        this.unread = p.getUnread();
        this.type = p.getType();
        this.description = p.getDescription();
        this.messageLength = p.getMessageLength();
        this.messageBody = p.getMessageBody().clone();
    }


/* ********************************************************************************************** */

    public long getPid() {
        return pid;
    }

    public void setPid(long pid) {
        this.pid = pid;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public long getTime() {
        return time;
    }

    public long getSid() {
        return sid;
    }

    public void setSid(long sid) {
        this.sid = sid;
    }

    public int getUnread() {
        return unread;
    }

    public void setUnread(int unread) {
        this.unread = unread;
    }

    public int getIncoming() {
        return incoming;
    }

    public void setIncoming(int incoming) {
        this.incoming = incoming;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        if (description.length() > 16){
            this.description = description.substring(0, 16) + "...";
        } else {
            this.description = description;
        }
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getMessageLength() {
        return messageLength;
    }

    public void setMessageLength(int messageLength) {
        this.messageLength = messageLength;
    }

    public byte[] getMessageBody() {
        return messageBody;
    }

    public void setMessageBody(byte[] messageBody) {
        this.messageBody = messageBody;
    }

/* ********************************************************************************************** */

    public class FileInfo{
        private String name;
        private String path;
        private int size;

        public byte[] encode(){
            Gson gson = new Gson();
            return gson.toJson(this, FileInfo.class).getBytes();
        }

        public void decode(byte[] bytes, int length){
            Gson gson = new Gson();
            String str = new String(bytes, 0, length);
            FileInfo fi = gson.fromJson(str, FileInfo.class);
            this.clone(fi);
        }

        public void clone(FileInfo fi){
            this.name = fi.getName();
            this.path = fi.getPath();
            this.size = fi.getSize();
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public int getSize() {
            return size;
        }

        public void setSize(int size) {
            this.size = size;
        }
    }
}