package com.talkie.wtalkie.sessions;


import android.os.Message;
import android.util.Log;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import org.litepal.crud.DataSupport;
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

    private static SessionManager mInstance;
    private Session mActiveSession;
    private final Messenger mMessenger = Messenger.getInstance();

/* ********************************************************************************************** */

    private SessionManager(){
        mMessenger.register(new MessageListener());
    }

    public static SessionManager getInstance(){
        if (mInstance == null){
            mInstance = new SessionManager();
        }
        return mInstance;
    }

/* ********************************************************************************************** */

    public Session buildSession(String originatorUid, long[] mReceiverIndexes){
        Log.v(TAG, "build session: from=" + originatorUid);
        List<String> uids = new ArrayList<>();
        for (User u : User.findAll(User.class, mReceiverIndexes)){
            uids.add(u.getUid());
        }

        mActiveSession = findSession(originatorUid, uids);
        if (mActiveSession == null){
            mActiveSession = new Session(originatorUid, uids);
        }
        mActiveSession.setState(Session.SESSION_ACTIVE);
        mActiveSession.saveOrUpdate("sid = ?",
                Long.toString(mActiveSession.getSid()));
        mActiveSession.dump();
        return mActiveSession;
    }

    public Session buildSession(long index){
        Log.v(TAG, "build session: at=" + index);
        mActiveSession = Session.find(Session.class, index);
        mActiveSession.setState(Session.SESSION_ACTIVE);
        mActiveSession.dump();
        return mActiveSession;
    }

    public Session findSessionById(long sessionid){
        Session session = null;
        // FIXME: 18-3-20, optimize by sql sentence
        //Session.select("time").where("time = ?", Long.toString(id));
        for (Session s : Session.findAll(Session.class)){
            if (s.getSid() == sessionid){
                session = s;
                break;
            }
        }
        return session;
    }

    public Session getActiveSession(){
        return mActiveSession;
    }

    public void updateActiveSessionState(boolean active){
        if (active) {
            mActiveSession.setState(Session.SESSION_ACTIVE);
        } else {
            mActiveSession.setState(Session.SESSION_INACTIVE);
            mActiveSession = null;
        }
    }

    public List<Session> getSessionList(){
        return DataSupport.findAll(Session.class);
    }

    public List<Session> getSessionList(long[] ids){
        return DataSupport.findAll(Session.class, ids);
    }

    public Session findSession(String originatorUid, List<String> receivers){
        Session sess = null;
        // FIXME: 18-3-18 : Here is very performance defect!!!
        List<Session> list = DataSupport.findAll(Session.class);
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
        Session ss = Session.find(Session.class, index);
        // delete session
        Session.delete(Session.class, index);

        // delete all messages in this session
        deleteMessages(ss.getSid());
    }


/* ********************************************************************************************** */
    public List<Packet> getAllMessages(){
        return Packet.findAll(Packet.class);
    }

    public List<Packet> getAllMessage(long sessionid){
        List<Packet> pl = new ArrayList<>();
        // FIXME: 18-3-20, we should use sql query sentence
        for(Packet p : Packet.findAll(Packet.class)){
            if (p.getSessionId() == sessionid){
                pl.add(p);
            }
        }

        return pl;
    }

    // FIXME: 18-3-20, we should use sql query sentence
    public Packet getLastMessage(long sessionId){
        Packet packet = null;
        List<Packet> pl = new ArrayList<>();
        for(Packet p : Packet.findAll(Packet.class)){
            if (p.getSessionId() == sessionId){
                pl.add(p);
            }
        }

        if (pl.size() > 0){
            packet = pl.get(pl.size() - 1);
        }

        return packet;
    }

    public void deleteMessages(long sid){
        Packet.deleteAll(Packet.class, "sessionId = ?",
                Long.toString(sid));
    }

/* ********************************************************************************************** */

    public void sendText(User originator, String text){
        Log.v(TAG, "send text: " + text);
        try {

            // wrap message packet
            Packet p = new Packet();
            p.setSessionId(mActiveSession.getSid());
            p.setType(Packet.MESSAGE_TYPE_TEXT);
            p.setIncoming(false);
            byte[] data = text.getBytes(Packet.DEFAULT_ENCODING_FORMAT);
            p.setMessageLength(data.length);
            p.setMessageBody(data);
            p.setDescription(text);

            // save message into message table
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

            // decode and save into database
            //Message msg = Message.decode(data, length);
            //msg.save();

            // decode message body



            // notify observer client
        }
    }

/* ********************************************************************************************** */

    public interface OnMessageListener{
        void onTextMessage(int indexInDatabase);
    }
}
