package com.talkie.wtalkie.sessions;

import com.talkie.wtalkie.contacts.User;

import java.util.ArrayList;
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
public class SessionManager {
    private static SessionManager mInstance;

    private final List<Session> mSessions = new ArrayList<>();

    private SessionManager(){ }

    public static SessionManager getInstance(){
        if (mInstance == null){
            mInstance = new SessionManager();
        }
        return mInstance;
    }

    public List<Session> getSessions(){
        return mSessions;
    }

    public Session getSession(User originatorId, List<User> participants){
        Session session = null;
        for (Session s : mSessions){
            if(s.isSame(originatorId, participants)){
                session = s;
                break;
            }
        }

        if (session == null){
            session = new Session(originatorId, participants);
        }

        return session;
    }


    public void add(Session ss){
        mSessions.add(ss);
    }

    public void remove(Session ss){

    }

    public void update(Session ss){

    }


}
