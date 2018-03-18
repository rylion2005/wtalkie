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
    private static final String MYSELF_PREF = "myself";

    private Myself(){ }

    public static SharedPreferences makeMyself(Context c){
        SharedPreferences sp = c.getSharedPreferences(MYSELF_PREF, Context.MODE_PRIVATE);
        String uid = sp.getString("uid", null);
        if (null == uid){
            Log.v(TAG, "generate myself");
            Identity id = Identity.getInstance(c);
            uid = id.genUid();
            SharedPreferences.Editor editor = sp.edit();
            /*
            Bitmap bm = BitmapFactory.decodeResource(c.getResources(), R.mipmap.default_avatar);
            ByteArrayOutputStream bo = new ByteArrayOutputStream();
            bm.compress(Bitmap.CompressFormat.PNG, 100, bo);
            editor.putString("avatar", new String(bo.toByteArray()));
            */
            editor.putString("uid", uid);
            editor.putString("user", "anonymous");
            editor.putString("nick", id.getModel());
            editor.putString("address", id.getLocalAddress());
            editor.putString("netaddr", "f.f.f.f");
            editor.putLong("elapse", 0);
            editor.putInt("state", 0xA);
            editor.commit();
        }
        return sp;
    }

    public static User fromMyself(Context c){
        User user = new User();
        SharedPreferences sp = makeMyself(c);
        //user.setAvatar(sp.getString("avatar", null).getBytes());
        user.setUid(sp.getString("uid", null));
        user.setUser(sp.getString("user", null));
        user.setNick(sp.getString("nick", null));
        user.setAddress(sp.getString("address", null));
        user.setNetaddr(sp.getString("netaddr", null));
        user.setElapse(sp.getLong("elapse", 0));
        user.setState(sp.getInt("state", 0));
        Log.v(TAG, "getMyself: " + user.toJsonString());
        return user;
    }

    public static void updateAddress(Context c){
        Log.v(TAG, "updateIds: ");
        Identity id = Identity.getInstance(c);
        SharedPreferences sp = c.getSharedPreferences(MYSELF_PREF, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("address", id.getLocalAddress());
        editor.apply();
    }

    public static void updateUserId(Context c, String s){
        Log.v(TAG, "updateUserId: ");
        SharedPreferences sp = c.getSharedPreferences(MYSELF_PREF, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("user", s);
        editor.apply();
    }

    public static void updateNickName(Context c, String s){
        Log.v(TAG, "updateNickName: ");
        SharedPreferences sp = c.getSharedPreferences(MYSELF_PREF, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("nick", s);
        editor.apply();
    }
}
