package com.talkie.wtalkie.sessions;


import android.util.Log;
import java.io.UnsupportedEncodingException;
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
public class Sessions {
    private static final String TAG = "Sessions";

    private static Sessions mInstance;

    private List<Session> mSessions;
    private Session mActiveSession;

    private final Messenger mMessenger = Messenger.getInstance();

/* ********************************************************************************************** */

    private Sessions(){
        mMessenger.register(new MessageListener());
    }

    public static Sessions getInstance(){
        if (mInstance == null){
            mInstance = new Sessions();
        }
        return mInstance;
    }

/* ********************************************************************************************** */

    public Session getSession(User originatorId, List<User> participants){
        Session session = null;

        Log.v(TAG, "get session: O=" + originatorId.getUid() + ", R=" + participants.size());

        // query session for database
        session = hasSession(originatorId, participants);
        if (session == null){ // no old session
            session = new Session(originatorId, participants);
            session.save();
        } else {
            session.setState(Session.SESSION_ACTIVE);
            session.saveOrUpdate("time = ?", Long.toString(session.getTime()));
        }

        Log.v(TAG, "::::>>> ");
        session.dump();
        Log.v(TAG, "::::~~~ ");

        // flag active session
        mActiveSession = session;
        return session;
    }

    public Session getActiveSession(){
        return mActiveSession;
    }

    public List<Session> getSessionList(){
        return DataSupport.findAll(Session.class);
    }

    public List<Session> getSessionList(long[] ids){
        return DataSupport.findAll(Session.class, ids);
    }

    public Session hasSession(User originatorId, List<User> participants){
        Session sess = null;
        Log.v(TAG, "look up: session");
        // FIXME: 18-3-18 : Here is very performance defect!!!
        List<Session> list = DataSupport.findAll(Session.class);
        for (Session s : list){
            if((s != null) && (s.existed(originatorId, participants))){
                sess = s;
                Log.v(TAG, "found old session");
                break;
            }
        }
        return sess;
    }

    // add to database
    public void add2(Session ss){
        ss.saveOrUpdate("time = ?", Long.toString(ss.getTime()));
    }

    public void remove(Session ss){}

    public void update(Session ss){}

/* ********************************************************************************************** */


/* ********************************************************************************************** */

    public void sendText(User originator, String text){
        Log.v(TAG, "send text: ");
        try {

            // wrap message
            Message message = new Message();
            message.setSessionTime(mActiveSession.getTime());
            message.setUidFrom(originator.getUid());
            message.setIncoming(false);
            message.setType(Message.MESSAGE_TYPE_TEXT);
            byte[] data = text.getBytes(Message.DEFAULT_ENCODING_FORMAT);
            message.setLength(data.length);
            message.setBody(data);

            // save message into message table
            message.save();

            // encode and send message
            mMessenger.sendText(message.encode(), message.encode().length);

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
