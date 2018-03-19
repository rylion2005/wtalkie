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
import com.google.gson.Gson;
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
    public static final int MAX_FILE_HEAD_LENGTH = 4096;
    public static final int MAX_MESSAGE_LENGTH = 10240;

    public static final String DEFAULT_ENCODING_FORMAT = "UTF-8";

    public static final int MESSAGE_TYPE_SESSION_INFO = 0xAA00;
    public static final int MESSAGE_TYPE_BYTE = 0xAAE0;
    public static final int MESSAGE_TYPE_EMOJI = 0xAAE1;
    public static final int MESSAGE_TYPE_TEXT = 0xAAE2;
    public static final int MESSAGE_TYPE_FILE_UNKOWN = 0xAAE3;
    public static final int MESSAGE_TYPE_FILE_PICTURE = 0xAAE4;
    public static final int MESSAGE_TYPE_FILE_AUDIO = 0xAAE5;
    public static final int MESSAGE_TYPE_FILE_VIDEO = 0xAAE6;

    private long time;             // use it as message id
    private long sessionTime;      // session time as session id
    private boolean isIncoming;    // if it is a incoming message
    private String originatorUid;  // originator uid
    private int type;          // message type
    private int fileSize;      // file size only for file message
    private String fileName;   // file name only for file message
    private int length;        // byte message length
    private byte[] body;       // byte message body

    /* ********************************************************************************************** */
    public Message() {
        time = System.currentTimeMillis();
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

    public boolean isIncoming() {
        return isIncoming;
    }

    public void setIncoming(boolean incoming) {
        isIncoming = incoming;
    }

    public String getOriginatorUid() {
        return originatorUid;
    }

    public void setOriginatorUid(String originatorUid) {
        this.originatorUid = originatorUid;
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