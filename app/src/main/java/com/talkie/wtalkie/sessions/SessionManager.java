package com.talkie.wtalkie.sessions;


import android.util.Log;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;


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

    public Session buildSession(String originator, int... indexes){
        Log.v(TAG, "build session");

        if (indexes.length == 0){
            return null;
        }

        // build users from user table
        List<String> users = new ArrayList<>();
        users.add(originator);
        List<User> allUsers = User.findAll(User.class);
        for (int ii = 0; ii < indexes.length; ii++){
            users.add(allUsers.get(indexes[ii]).getUid());
        }
        Log.v(TAG, "uids: " + users.size());

        // find if there is a same user list
        mActiveSession = findSession(users);
        if (mActiveSession == null) {
            mActiveSession = new Session(users);
        }
        mActiveSession.setState(Session.SESSION_ACTIVE);
        // save into session table
        mActiveSession.saveOrUpdate("sid = ?", Long.toString(mActiveSession.getSid()));
        return mActiveSession;
    }

    public void enterChatRoom(String name, String me){
        Log.d(TAG, "enterChatRoom: " + name);
        List<Session> ss = Session.where("name = ?", name).find(Session.class);
        mActiveSession = ss.get(0);

        // update active information
        mActiveSession.addUser(me);
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

    public Session findTemporarySessionByIndex(int index){
        List<Session> sessions = Session.where("type = ?", Integer.toString(Session.SESSION_TYPE_TEMPORARY))
                .find(Session.class);
        return sessions.get(index);
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

    public Session findSession(List<String> users){

        if (users == null || users.isEmpty()){
            return null;
        }

        Session sesssion = null;
        List<Session> list = Session.findAll(Session.class);
        for (Session s : list){
            if(s.has(users)){
                sesssion = s;
                Log.v(TAG, "exist a session !");
                break;
            }
        }
        return sesssion;
    }

    public Session findSession(long sid){
        Session s = null;
        List<Session> ss = Session.where("sid = ?", Long.toString(sid)).find(Session.class);
        if (ss != null && !ss.isEmpty()){
            s = ss.get(0);
        }
        return s;
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

    public boolean isPublicChatRoom(long sid){
        boolean result = false;
        if (sid == CHAT_ROOM_A_ID || sid == CHAT_ROOM_B_ID){
            result = true;
        }
        return result;
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

        Session s = findSession(sid);
        if (s == null){
            return;
        }

        Packet p = new Packet();
        p.setSid(sid);
        p.setType(Packet.MESSAGE_TYPE_SESSION);
        p.setIncoming(0);
        p.setUnread(1);
        // wrap session to packet
        byte[] data = s.encode().getBytes();
        p.setMessageLength(data.length);
        p.setMessageBody(data);
        p.setDescription("");
        Log.v(TAG, ":S: " + p.toJsonString());

        // encode and send message
        byte[] pb = p.encode();
        mMessenger.sendText(pb, pb.length);
    }

    public void sendText(String text){
        Log.v(TAG, "send text: " + text);
        try {

            // if it is 1st message in the session
            List<Packet> pl = Packet.where("sid = ?",
                    Long.toString(mActiveSession.getSid()))
                    .find(Packet.class);

            if (pl.isEmpty() && !isPublicChatRoom(mActiveSession.getSid())){
                Log.v(TAG, "The first message");
                sendSession(mActiveSession.getSid());
            }

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
            Log.v(TAG, "packet type: " + p.getType());
            switch (p.getType()){
                case Packet.MESSAGE_TYPE_SESSION:
                    Session session = Session.decode(p.getMessageBody(), p.getMessageLength());
                    session.dump();
                    // find by sid
                    Session s = findSession(session.getSid());
                    if (s == null){ // new session
                        // find by user list
                        s = findSession(session.getUsers());
                        if (s == null) {
                            // save into database
                            if (!isPublicChatRoom(session.getSid())) {
                                Log.v(TAG, "new session incoming");
                                session.setState(Session.SESSION_INACTIVE);
                                session.save();

                                for (OnMessageListener l: mListeners){
                                    l.onNewSession();
                                }
                            }
                        }
                    }
                    break;
                case Packet.MESSAGE_TYPE_BYTE:
                case Packet.MESSAGE_TYPE_EMOJI:
                case Packet.MESSAGE_TYPE_TEXT:
                    p.setIncoming(1);
                    p.setUnread(1);
                    p.save();
                    for (OnMessageListener l: mListeners){
                        l.onNewMessage();
                    }
                    break;
                case Packet.MESSAGE_TYPE_FILE_UNKOWN:
                case Packet.MESSAGE_TYPE_FILE_PICTURE:
                case Packet.MESSAGE_TYPE_FILE_VIDEO:
                case Packet.MESSAGE_TYPE_FILE_AUDIO:
                    p.setIncoming(1);
                    p.setUnread(1);
                    p.save();
                    for (OnMessageListener l: mListeners){
                        l.onNewMessage();
                    }
                    break;
                default:
                    break;
            }
        }
    }

/* ********************************************************************************************** */

    public interface OnMessageListener{
        void onNewMessage();
        void onNewSession();
    }
}
