package com.talkie.wtalkie.contacts;

import android.util.Log;

/*
**
** ${FILE}
**   ...
**
** REVISED HISTORY
**   yl7 | 18-3-11: Created
**     
*/
public class User {
    private String uuid;
    private String serial;
    private String address;

    // New user from a csv bytes
    public static User fromBytes(byte[] bytes, int length){
        if (bytes == null){
            return null;
        }
        User user = new User();
        String buffer = new String(bytes);
        int pos1 = buffer.indexOf(',');
        int pos2 = buffer.lastIndexOf(',');
        String uuid = buffer.substring(0, pos1);
        String serial = buffer.substring(pos1+1, pos2);
        String ip = buffer.substring(pos2+1, length);
        user.setUuid(uuid);
        user.setSerial(serial);
        user.setAddress(ip);
        return user;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getSerial() {
        return serial;
    }

    public void setSerial(String serial) {
        this.serial = serial;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String toString(){
        return uuid + "," + serial + "," + address;
    }
}
