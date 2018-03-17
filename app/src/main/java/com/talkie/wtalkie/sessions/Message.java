package com.talkie.wtalkie.sessions;

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
public class Message {
    public static final int MESSAGE_TYPE_BYTE = 0xE0;
    public static final int MESSAGE_TYPE_EMOJI = 0xE1;
    public static final int MESSAGE_TYPE_TEXT = 0xE2;
    public static final int MESSAGE_TYPE_FILE_UNKOWN = 0xE3;
    public static final int MESSAGE_TYPE_FILE_PICTURE = 0xE4;
    public static final int MESSAGE_TYPE_FILE_AUDIO = 0xE5;
    public static final int MESSAGE_TYPE_FILE_VIDEO = 0xE6;

    private long messageId;            // the sent time as id
    private long sentTime;             // millisecond
    private Session session;           // in which sessions
    private int type;
    private String messageDescription; // description for message
    private User originator;           // nick name
    private List<User> terminators;    // receivers
    private int byteLength;            // only for byte/text/emoji
    private String fileName;           // for file
    private String filePath;           // for file
    private int fileSize;              // for file
    private long duration;             // only for audio/video

    public long getMessageId() {
        return messageId;
    }

    public void setMessageId(long messageId) {
        this.messageId = messageId;
    }

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

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getMessageDescription() {
        return messageDescription;
    }

    public void setMessageDescription(String messageDescription) {
        this.messageDescription = messageDescription;
    }

    public User getOriginator() {
        return originator;
    }

    public void setOriginator(User originator) {
        this.originator = originator;
    }

    public List<User> getTerminators() {
        return terminators;
    }

    public void setTerminators(List<User> terminators) {
        this.terminators = terminators;
    }

    public int getByteLength() {
        return byteLength;
    }

    public void setByteLength(int byteLength) {
        this.byteLength = byteLength;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public int getFileSize() {
        return fileSize;
    }

    public void setFileSize(int fileSize) {
        this.fileSize = fileSize;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }
}
