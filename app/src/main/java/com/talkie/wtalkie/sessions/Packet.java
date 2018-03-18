package com.talkie.wtalkie.sessions;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

/*
**
** ${FILE}
**   ...
**
** REVISED HISTORY
**   yl7 | 18-3-18: Created
**     
*/
public class Packet {
    public static final int MAX_FILE_HEAD_LENGTH = 4096;
    public static final int MAX_MESSAGE_LENGTH = 10240;

    public static final int MESSAGE_TYPE_BYTE = 0xAAE0;
    public static final int MESSAGE_TYPE_EMOJI = 0xAAE1;
    public static final int MESSAGE_TYPE_TEXT = 0xAAE2;
    public static final int MESSAGE_TYPE_FILE_UNKOWN = 0xAAE3;
    public static final int MESSAGE_TYPE_FILE_PICTURE = 0xAAE4;
    public static final int MESSAGE_TYPE_FILE_AUDIO = 0xAAE5;
    public static final int MESSAGE_TYPE_FILE_VIDEO = 0xAAE6;

/* ********************************************************************************************** */


/* ********************************************************************************************** */

    class FileMessage{

        private int type;     // file type
        private int size;     // file size
        private int duration; // only for media file
        private String name;
        //private long md5;

        public byte[] encode(){
            ByteBuffer bb = ByteBuffer.allocate(MAX_FILE_HEAD_LENGTH);
            bb.putInt(type);
            bb.putInt(size);
            bb.putInt(duration);
            try {
                bb.put(name.getBytes("utf-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            return bb.array();
        }

        public FileMessage decode(byte[] bytes){
            if (bytes == null || bytes.length == 0){
                return null;
            }


            return this;
        }

        public int getType() {
            return type;
        }

        public void setType(int type) {
            this.type = type;
        }

        public int geSize() {
            return size;
        }

        public void setsize(int size) {
            this.size = size;
        }

        public int getDuration() {
            return duration;
        }

        public void setDuration(int duration) {
            this.duration = duration;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

/* ********************************************************************************************** */

    public class ByteMessage{
        private int type;
        private int length;
        private byte[] message = new byte[MAX_MESSAGE_LENGTH-4];

        public byte[] encode(){
            byte[] bytes = new byte[MAX_MESSAGE_LENGTH];

            return bytes;
        }

        public ByteMessage decode(byte[] bytes){

            return this;
        }

        public int getType() {
            return type;
        }

        public void setType(int type) {
            this.type = type;
        }

        public int getLength() {
            return length;
        }

        public void setLength(int length) {
            this.length = length;
        }

        public byte[] getMessage() {
            return message;
        }

        public void setMessage(byte[] message) {
            this.message = message.clone();
        }
    }
}
