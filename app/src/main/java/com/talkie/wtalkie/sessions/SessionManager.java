package com.talkie.wtalkie.sessions;


import android.util.Log;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;
import org.litepal.crud.DataSupport;

import com.google.gson.Gson;
import com.talkie.wtalkie.contacts.User;
import com.talkie.wtalkie.sockets.Messenger;


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
    private static final String TAG = "SessionManager";

    public static final long CHAT_ROOM_A_ID = 99887766;
    public static final long CHAT_ROOM_B_ID = 99887767;
    public static final long TALK_CHANNEL_A_ID = 99887788;
    public static final long TALK_CHANNEL_B_ID = 99887789;

    private static SessionManager mInstance;
    private Session mActiveSession;
    private final Messenger mMessenger = Messenger.getInstance();
    private final List<OnMessageListener> mListeners = new ArrayList<>();

/* ********************************************************************************************** */

    private SessionManager(){
        mMessenger.register(new MessageListener());

        Session chatRoomA = new Session(99887766, "Chat Room A", Session.SESSION_TYPE_CHAT_ROOM);
        Session chatRoomB = new Session(99887767, "Chat Room B", Session.SESSION_TYPE_CHAT_ROOM);
        Session talkChannelA = new Session(99887788, "Talk Channel A", Session.SESSION_TYPE_TALK_CHANNEL);
        Session talkChannelB = new Session(99887789, "Talk Channel B", Session.SESSION_TYPE_TALK_CHANNEL);

        chatRoomA.saveOrUpdate("sid = ?", "99887766");
        chatRoomB.saveOrUpdate("sid = ?", "99887767");
        talkChannelA.saveOrUpdate("sid = ?", "99887788");
        talkChannelB.saveOrUpdate("sid = ?", "99887789");
    }

    public static SessionManager getInstance(){
        if (mInstance == null){
            mInstance = new SessionManager();
        }
        return mInstance;
    }

    public void register(OnMessageListener listener){
        if (listener != null){
            mListeners.add(listener);
        }
    }

/* ********************************************************************************************** */
    // Session related
/* ********************************************************************************************** */

    public Session buildSession(String originatorUid, int... indexes){
        Log.v(TAG, "build session: receivers=" + indexes.length);

        if (indexes == null || indexes.length == 0){
            return null;
        }

        // find receivers from user table and build receiver uid list
        List<String> receivers = new ArrayList<>();

        List<User> users = User.findAll(User.class);
        for (int ii = 0; ii < indexes.length; ii++){
            receivers.add(users.get(indexes[ii]).getUid());
        }
        Log.v(TAG, "uids: " + receivers.size());

        mActiveSession = new Session(originatorUid, receivers);
        mActiveSession.setState(Session.SESSION_ACTIVE);

        // save into session table
        mActiveSession.saveOrUpdate("sid = ?", Long.toString(mActiveSession.getSid()));
        Log.v(TAG, ">>>>>>>>>> ");
        mActiveSession.dump();
        Log.v(TAG, "~~~~~~~~~~ ");
        return mActiveSession;
    }

    public void enterChatRoom(String name, String me){
        Log.d(TAG, "enterChatRoom: " + name);
        List<Session> ss = Session.where("name = ?", name).find(Session.class);
        mActiveSession = ss.get(0);

        // update active information
        mActiveSession.addReceiver(me);
        mActiveSession.setState(Session.SESSION_ACTIVE);
        mActiveSession.dump();

        // save back into database
        mActiveSession.saveOrUpdate("sid = ?",
                Long.toString(mActiveSession.getSid()));
    }

    public void activateSession(String name){
        List<Session> ss = Session.where("name = ?", name).find(Session.class);
        mActiveSession = ss.get(0);
        mActiveSession.setState(Session.SESSION_ACTIVE);
        // save back into database
        mActiveSession.saveOrUpdate("sid = ?",
                Long.toString(mActiveSession.getSid()));
    }

    public void enterTalkChannel(long sid){
        Log.d(TAG, "enterTalkChannel: " + sid);
    }

    public Session findSessionById(long sid){
        List<Session> ss = Session.where("sid = ?", Long.toString(sid))
                .find(Session.class);
        return ss.get(0);
    }

    public Session findTemporarySessionByIndex(int index){
        Session session = null;
        List<Session> sessions = new ArrayList<>();

        List<Session> allSessions = Session.findAll(Session.class);
        // FIXME: 18-3-20, optimize by sql sentence
        for (Session s : allSessions){
            if (s.getType() == Session.SESSION_TYPE_TEMPORARY){
                sessions.add(s);
            }
        }

        Log.v(TAG, "all: " + allSessions.size());
        Log.v(TAG, "temporary: " + sessions.size());
        session = sessions.get(index);
        return session;
    }

    public Session getActiveSession(){
        if (mActiveSession == null) {
            List<Session> ss = Session.where("state = ?",
                    Integer.toString(Session.SESSION_ACTIVE))
                    .find(Session.class);
            if (ss.size() == 0){
                Log.e(TAG, "no Active Session:");
            } else if (ss.size() > 1){
                Log.e(TAG, "ERROR: more than one active !!!!");
            } else {
                mActiveSession = ss.get(0);
            }
        }
        return mActiveSession;
    }

    public void resetActiveSession(){
        if (mActiveSession != null) {
            mActiveSession.setState(Session.SESSION_INACTIVE);
            mActiveSession.saveOrUpdate("sid = ?",
                    Long.toString(mActiveSession.getSid()));
            mActiveSession = null;
        }
    }

    public List<Session> getSessionList(int type){
        List<Session> ss = Session.where("type = ?", Integer.toString(type))
                .find(Session.class);
        return ss;
    }

    public Session findSession(String originatorUid, List<String> receivers){

        if (originatorUid == null){
            return null;
        }

        if (receivers == null || receivers.isEmpty()){
            return null;
        }

        Session sess = null;
        List<Session> list = Session.where("originator = ?", originatorUid)
                .find(Session.class);
        for (Session s : list){
            if(s.has(originatorUid, receivers)){
                sess = s;
                Log.v(TAG, "found old session");
                break;
            }
        }
        return sess;
    }

    public void deleteSession(int index){
        Log.v(TAG, "delete session: " + index);

        // find session
        Session ss = findTemporarySessionByIndex(index);
        ss.dump();
        // delete session
        Session.deleteAll(Session.class, "sid = ?", Long.toString(ss.getSid()));

        // delete all messages in this session
        deleteMessages(ss.getSid());
    }

/* ********************************************************************************************** */
    // Message and packet
/* ********************************************************************************************** */

    public List<Packet> getAllMessages(long sid){
        Log.v(TAG, "getAllMessages: " + sid);
        List<Packet> pl = Packet.where("sid = ?", Long.toString(sid))
                .find(Packet.class);
        return pl;
    }

    public List<Packet> getUnreadMessages(long sid){
        Log.v(TAG, "getUnreadMessages: " + sid);
        List<Packet> pl = Packet.where("sid = ? and incoming = ? and unread = ?",
                Long.toString(sid), "1", "1")
                .find(Packet.class);
        return pl;
    }

    public Packet getLastMessage(long sid){
        Packet packet = null;
        List<Packet> pl = Packet.where("sid = ?", Long.toString(sid))
                .find(Packet.class);
        if (pl.size() > 0){
            packet = pl.get(pl.size() - 1);
        }

        return packet;
    }

    public void deleteMessages(long sid){
        Packet.deleteAll(Packet.class, "sid = ?",
                Long.toString(sid));
    }

/* ********************************************************************************************** */

    public void sendSession(long sid){
        Packet p = new Packet();
        p.setType(Packet.MESSAGE_TYPE_SESSION);
        p.setSid(sid);
    }

    public void sendText(User originator, String text){
        Log.v(TAG, "send text: " + text);
        try {

            // wrap message packet
            Packet p = new Packet();
            p.setSid(mActiveSession.getSid());
            p.setType(Packet.MESSAGE_TYPE_TEXT);
            p.setIncoming(0);
            p.setUnread(1);
            byte[] data = text.getBytes(Packet.DEFAULT_ENCODING_FORMAT);
            p.setMessageLength(data.length);
            p.setMessageBody(data);
            p.setDescription(text);
            Log.v(TAG, ":S: " + p.toJsonString());
            // save message into database table
            p.save();

            // encode and send message
            byte[] pb = p.encode();
            mMessenger.sendText(pb, pb.length);

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

    }

    public void sendFile(){}

    public void sendPicture(){}

    public void sendAudio(){}

    public void sendVideo(){}

/* ********************************************************************************************** */

    class MessageListener implements Messenger.MessageCallback{

        @Override
        public void onNewMessage(byte[] data, int length) {
            Log.v(TAG, "onNewMessage: " + length);
            if (data == null || length == 0){
                return;
            }

            // decode byte buffer
            Packet p = Packet.decode(data, length);
            p.setIncoming(1);
            p.setUnread(1);

            // save into database table
            boolean saved = p.save();

            // notify clients
            for (OnMessageListener l : mListeners){
                l.onNewMessage();
            }
        }
    }

/* ********************************************************************************************** */

    public interface OnMessageListener{
        void onNewMessage();
    }
}
