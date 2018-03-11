package com.talkie.wtalkie.contacts;

import android.content.Context;

import java.util.UUID;

/*
**
** ${FILE}
**   ...
**
** REVISED HISTORY
**   yl7 | 18-3-11: Created
**     
*/
public class Contacts {
    private static final String TAG = "Contacts";

    private static final String MYSELF = "myself.db";
    private static final String CONTACTS_DATABASE = "contacts.db";

    private Context mContext;


/* ********************************************************************************************** */


    public Contacts(Context context){

    }


/* ********************************************************************************************** */

    private User fromBytes(byte[] bytes, int length){
        if (bytes == null){
            return null;
        }

        if (length == 0){
            return null;
        }

        User user = null;
        String buffer = new String(bytes);
        String uuid = buffer.substring(0, buffer.indexOf(','));
        String ip = buffer.substring(buffer.indexOf(',')+1, length+1);
        user.setUuid(uuid);
        user.setAddress(ip);
        return user;
    }

    private String genUuid(){
        String id = UUID.randomUUID().toString();
        return id.substring(0, 8);
    }

/* ********************************************************************************************** */


    class User {
        String uuid;
        String address;

        public String getUuid() {
            return uuid;
        }

        public void setUuid(String uuid) {
            this.uuid = uuid;
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }
    }
}
