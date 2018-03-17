package com.talkie.wtalkie.contacts;


import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.talkie.wtalkie.R;

import java.io.ByteArrayOutputStream;

/*
**
** ${FILE}
**   ...
**
** REVISED HISTORY
**   yl7 | 18-3-17: Created
**     
*/
public class Myself extends User {
    private static final String TAG = "Myself";
    private static final String MYSELF = "myself";

    private Myself(){ }

    public static SharedPreferences makeMyself(Context c){
        Log.v(TAG, "makeMyself: ");
        SharedPreferences sp = c.getSharedPreferences(MYSELF, Context.MODE_PRIVATE);
        String uuid = sp.getString("uuid", null);
        if (null == uuid){
            Log.v(TAG, "generate myself");
            Identity id = Identity.getInstance(c);
            uuid = id.genShortUuid();
            SharedPreferences.Editor editor = sp.edit();
            editor.putString("uuid", uuid);
            /*
            Bitmap bm = BitmapFactory.decodeResource(c.getResources(), R.mipmap.default_avatar);
            ByteArrayOutputStream bo = new ByteArrayOutputStream();
            bm.compress(Bitmap.CompressFormat.PNG, 100, bo);
            editor.putString("avatar", new String(bo.toByteArray()));
            */
            editor.putString("user", "unknown");
            editor.putString("nick", id.getModel());
            editor.putString("serial", id.getSerial());
            editor.putString("address", id.getLocalAddress());
            editor.commit();
        }
        return sp;
    }

    public static User buildMyself(Context c){
        Log.v(TAG, "buildMyself: ");
        User user = new User();
        SharedPreferences sp = makeMyself(c);
        //user.setAvatar(sp.getString("avatar", null).getBytes());
        user.setUser(sp.getString("user", null));
        user.setNick(sp.getString("nick", null));
        user.setUuid(sp.getString("uuid", null));
        user.setSerial(sp.getString("serial", null));
        user.setAddress(sp.getString("address", null));
        return user;
    }

    public static void updateAddress(Context c){
        Log.v(TAG, "updateIds: ");
        Identity id = Identity.getInstance(c);
        SharedPreferences sp = c.getSharedPreferences(MYSELF, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("address", id.getLocalAddress());
        editor.apply();
    }

    public static void updateUserId(Context c, String s){
        Log.v(TAG, "updateUserId: ");
        SharedPreferences sp = c.getSharedPreferences(MYSELF, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("user", s);
        editor.apply();
    }

    public static void updateNickName(Context c, String s){
        Log.v(TAG, "updateNickName: ");
        SharedPreferences sp = c.getSharedPreferences(MYSELF, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("nick", s);
        editor.apply();
    }
}
