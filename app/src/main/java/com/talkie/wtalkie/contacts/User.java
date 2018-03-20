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
    private String uid;
    private String address;
    private String netaddr;
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

    public String toJsonString(){
        Gson g = new Gson();
        return g.toJson(this, User.class);
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

    public String getUid() {
        return uid;
    }

    public void setUid(String uuid) {
        this.uid = uuid;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getNetaddr() {
        return netaddr;
    }

    public void setNetaddr(String netaddr) {
        this.netaddr = netaddr;
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

}
