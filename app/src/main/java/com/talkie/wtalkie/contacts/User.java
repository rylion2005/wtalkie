package com.talkie.wtalkie.contacts;



import android.text.TextUtils;
import com.google.gson.Gson;
import org.litepal.crud.DataSupport;


/*
**
** ${FILE}
**   ...
**
** REVISED HISTORY
**   yl7 | 18-3-11: Created
**     
*/
public class User extends DataSupport {

    public static int STATE_ONLINE = 0xA;
    public static int STATE_OFFLINE = 0xB;
    public static int STATE_LEAVED = 0xC;

    //private byte[] avatar;
    private String user;
    private String nick;
    private String uuid;
    private String serial;
    private String address;
    private long elapse;
    private int state;


/* ********************************************************************************************** */

    // New user from a json string
    public static User fromBytes(byte[] bytes, int length){
        return fromJsonString(new String(bytes, 0, length));
    }

    public static User fromJsonString(String json){
        Gson g = new Gson();
        return g.fromJson(json, User.class);
    }

/* ********************************************************************************************** */
/*
    public byte[] getAvatar() {
        return avatar;
    }

    public void setAvatar(byte[] avatar) {
        this.avatar = avatar;
    }
*/
    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getNick() {
        return nick;
    }

    public void setNick(String nick) {
        this.nick = nick;
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

    public long getElapse() {
        return elapse;
    }

    public void setElapse(long elapse) {
        this.elapse = elapse;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }


/* ********************************************************************************************** */

    public String toJsonString(){
        Gson g = new Gson();
        return g.toJson(this, User.class);
    }

    public boolean isAllEmptyIds(){
        boolean empty = false;
        if (TextUtils.isEmpty(uuid) || uuid.equals("null")
                && (TextUtils.isEmpty(serial) || serial.equals("null"))) {
            empty = true;
        }
        return empty;
    }


}
