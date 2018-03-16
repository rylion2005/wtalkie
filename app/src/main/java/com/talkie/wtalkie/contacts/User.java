package com.talkie.wtalkie.contacts;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

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

    private static final String MYSELF = "myself";

    private String uuid;
    private String serial;
    private String address;
    private long elapse;
    private int state;


/* ********************************************************************************************** */

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

    /*
     ** ----------------------------------------------------------------------
     ** fromSharePreference
     **   Generate User object from share preference
     **
     ** @PARAM : None
     ** @RETURN User: User object
     **
     ** NOTES:
     **   If it is the first, we should fill data structure
     **
     ** ----------------------------------------------------------------------
     */
    public static User fromSharePreference(Context c){

        User user = new User();

        Identity id = Identity.getInstance(c);

        //read uuid from share preference
        SharedPreferences sp = c.getSharedPreferences(MYSELF, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();

        // at first boot, all information are null, we must fill it with ids
        String uuid = sp.getString("uuid", null);
        if (null == uuid){
            uuid = id.genShortUuid();
            editor.putString("uuid", uuid);
            editor.putString("serial", id.getSerial());
            editor.putString("address", id.getLocalAddress());
            editor.commit();
        }

        // fill data structure from share preference
        user.setUuid(sp.getString("uuid", null));
        user.setSerial(sp.getString("serial", null));
        user.setAddress(sp.getString("address", null));
        return user;
    }

    public static void updateSharePreference(Context c){

        Identity id = Identity.getInstance(c);
        SharedPreferences sp = c.getSharedPreferences(MYSELF, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        String uuid = sp.getString("uuid", null);
        if (null == uuid){
            uuid = id.genShortUuid();
            editor.putString("uuid", uuid);
        }
        editor.putString("serial", id.getSerial());
        editor.putString("address", id.getLocalAddress());
        editor.apply();
    }


/* ********************************************************************************************** */

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


    public String toString(){
        return uuid + "," + serial + "," + address;
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
