package com.talkie.wtalkie.global;

import android.content.Context;

/*
**
** ${FILE}
**   ...
**
** REVISED HISTORY
**   yl7 | 18-3-18: Created
**     
*/
public final class GlobalContext {

    private static GlobalContext mInstance;

    private static Context mContext;

    private GlobalContext(Context c){
        mContext = c;
    }

    public static void buildGlobalContext(Context c){
        if (mInstance == null){
            mInstance = new GlobalContext(c);
        }
    }

    public static Context getGlobalContext(){
        return mContext;
    }
}
