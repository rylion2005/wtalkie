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
import com.talkie.wtalkie.global.GlobalConstants;

import org.litepal.crud.DataSupport;


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
public class Message extends DataSupport {
    private static final String TAG = "Message";

    public static final int MAX_FILE_HEAD_LENGTH = 1024;
    public static final int MAX_MESSAGE_LENGTH = 1024;

    public static final String DEFAULT_ENCODING_FORMAT = "UTF-8";

    public static final int MESSAGE_TYPE_UNKNOWN = 0xAA00;
    public static final int MESSAGE_TYPE_SESSION_INFO = 0xAADD;
    public static final int MESSAGE_TYPE_BYTE = 0xAAE0;
    public static final int MESSAGE_TYPE_EMOJI = 0xAAE1;
    public static final int MESSAGE_TYPE_TEXT = 0xAAE2;
    public static final int MESSAGE_TYPE_FILE_UNKOWN = 0xAAE3;
    public static final int MESSAGE_TYPE_FILE_PICTURE = 0xAAE4;
    public static final int MESSAGE_TYPE_FILE_AUDIO = 0xAAE5;
    public static final int MESSAGE_TYPE_FILE_VIDEO = 0xAAE6;

    // relation database key
    private long time;          // message main key
    private long sessionTime;   // session main key
    private String uidFrom;     // originator uid, User main key
    private String uidTo;       // receiver uid, User main key

    // meta data
    private boolean isIncoming; // if it is a incoming message
    private int type;           // message type
    private String description; // message summary description
    private int fileSize;       // file size only for file message
    private String fileName;    // file name only for file message
    private int length;         // byte message length
    private byte[] body = new byte[MAX_MESSAGE_LENGTH]; // byte message body

/* ********************************************************************************************** */

    public Message() {
        Log.d(TAG, "new default message");
        this.time = System.currentTimeMillis();
        this.sessionTime = 0;
        this.uidFrom = "default";
        this.uidTo   = "default";
        this.description = "default";
        this.isIncoming   = false;
        this.type = MESSAGE_TYPE_UNKNOWN;
        this.fileSize = 0;
        this.fileName = "default";
        this.length = 0;
    }

    public static Message decode(byte[] bytes, int length) {
        Gson g = new Gson();
        return g.fromJson(new String(bytes, 0, length), Message.class);
    }

/* ********************************************************************************************** */

    public byte[] encode() {
        Gson gson = new Gson();
        return gson.toJson(this, Message.class).getBytes();
    }

    public String toJsonString(){
        Gson gson = new Gson();
        return gson.toJson(this, Message.class);
    }


/* ********************************************************************************************** */

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public long getSessionTime() {
        return sessionTime;
    }

    public void setSessionTime(long sessionTime) {
        this.sessionTime = sessionTime;
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

    public boolean isIncoming() {
        return isIncoming;
    }

    public void setIncoming(boolean incoming) {
        isIncoming = incoming;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getFileSize() {
        return fileSize;
    }

    public void setFileSize(int fileSize) {
        this.fileSize = fileSize;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public byte[] getBody() {
        return body;
    }

    public void setBody(byte[] body) {
        this.body = body.clone();
    }
}