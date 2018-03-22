package com.talkie.wtalkie.ui;


import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.talkie.wtalkie.R;
import com.talkie.wtalkie.contacts.Myself;
import com.talkie.wtalkie.sessions.Packet;
import com.talkie.wtalkie.sessions.Session;
import com.talkie.wtalkie.sessions.SessionManager;
import com.talkie.wtalkie.sockets.Messenger;

import java.io.UnsupportedEncodingException;
import java.util.List;

/*
** ********************************************************************************
**
** ChatActivity
**   This is chat UI for all sessions.
**
**   Generally, we may enter this activity from:
**     - ContactsFragment with contact uid parameters.
**     - SessionsFragment with session time
**
** USAGE:
**   ......
**
** ********************************************************************************
*/
public class ChatActivity extends BaseActivity implements View.OnClickListener,
        AdapterView.OnItemClickListener , TextWatcher {
    private static final String TAG = "ChatActivity";

    private static final int MESSAGE_INCOMING = 0xAA;
    private static final int MESSAGE_TYPE_TEXT = 0xC1;
    private static final int MESSAGE_TYPE_PICTURE = 0xC2;
    private static final int MESSAGE_TYPE_AUDIO = 0xC3;
    private static final int MESSAGE_TYPE_VIDEO = 0xC4;
    private static final int MESSAGE_TYPE_FILE = 0xC5;

    private ImageView mIMVType;
    private EditText mEDTText;
    private TextView mTXVTalk;
    private ImageView mIMVMore;
    private Button mBTNAction;

    private MyBaseAdapter mAdapter;

    // Session information
    private SessionManager mSessionManager;
    private Session mActiveSession;
    private long[] mSessionIds;

    private final UiHandler mHandler = new UiHandler();


/* ********************************************************************************************** */


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.v(TAG, "onCreate: ");
        setContentView(R.layout.activity_chat);
        init();
        initViews();
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume:");
        super.onResume();
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause()");
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy()");
        super.onDestroy();
        mSessionManager.resetActiveSession();
    }

    @Override
    public void onClick(View v) {
        Log.v(TAG, "onclick: " + v.toString());
        switch (v.getId()) {
            case R.id.IMV_MessageMode:
                //mMessageModeText = !mMessageModeText;
                //switchMessageMode(mMessageModeText);
                break;

            case R.id.TXV_Talk:
                break;

            case R.id.EDT_Input:
                //mEDTText.setFocusable(true);
                //mEDTText.setFocusableInTouchMode(true);
                //mEDTText.requestFocus();
                //mEDTText.setShowSoftInputOnFocus(true);
                break;

            case R.id.IMV_More:
                break;
            case R.id.BTN_Action:
                // Send to network
                String input = mEDTText.getText().toString();
                mSessionManager.sendText(Myself.fromMyself(this), input);
                // Show in list view
                showTextMessage(false, input);
                mEDTText.getText().clear();
                switchAction(false);
                break;
            default:
                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    @Override
    public void afterTextChanged(Editable s) {
        switchAction(true);
    }


/* ********************************************************************************************** */


    private void init() {
        Log.v(TAG, "init: ");
        handleIntent();
    }

    private void handleIntent(){
        Log.v(TAG, "handleIntent");
        mSessionManager = SessionManager.getInstance();
        try {
            Bundle b = getIntent().getExtras();
            int sessionType = b.getInt("SessionType");
            switch (sessionType) {
                case Session.SESSION_TYPE_CHAT_ROOM:
                    String roomName = b.getString("RoomName");
                    String me = Myself.fromMyself(this).getUid();
                    mSessionManager.enterChatRoom(roomName, me);
                    break;
                case Session.SESSION_TYPE_TALK_CHANNEL:
                    // TODO: 18-3-22
                    break;
                case Session.SESSION_TYPE_TEMPORARY:
                    String name = b.getString("SessionName");
                    if (name != null){ // existed session
                        mSessionManager.activateSession(name);
                    } else { // new session
                        String originatorUid = b.getString("Originator");
                        int[] indexes = b.getIntArray("ReceiverIndexes");
                        mSessionManager.buildSession(originatorUid, indexes);
                    }
                    break;
                default:
                    break;
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        mActiveSession = mSessionManager.getActiveSession();
    }

    private void initViews() {
        try {
            ActionBar ab = getSupportActionBar();
            ab.setTitle(mActiveSession.getName());
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

        mIMVType = findViewById(R.id.IMV_MessageMode);
        mEDTText = findViewById(R.id.EDT_Input);
        mTXVTalk = findViewById(R.id.TXV_Talk);
        mIMVMore = findViewById(R.id.IMV_More);
        mBTNAction = findViewById(R.id.BTN_Action);

        mIMVType.setOnClickListener(this);
        mEDTText.setOnClickListener(this);
        mEDTText.addTextChangedListener(this);
        mTXVTalk.setOnClickListener(this);
        mIMVMore.setOnClickListener(this);
        mBTNAction.setOnClickListener(this);

        mAdapter = new MyBaseAdapter(this, R.layout.chat_list);
        ListView lsv = findViewById(R.id.LSV_Messages);
        lsv.setAdapter(mAdapter);
        lsv.setOnItemClickListener(this);

        refreshMessages();
    }

    private void refreshMessages(){
        Log.v(TAG, "refreshMessages");
        mActiveSession = mSessionManager.getActiveSession();
        if (mActiveSession == null){
            Log.e(TAG, "No active session !");
            return;
        }

        List<Packet> pl = mSessionManager.getAllMessages(mActiveSession.getSid());
        Log.v(TAG, "Message count: " + pl.size());
        for (Packet p : pl){
            Log.v(TAG, p.toJsonString());
            String message = new String(p.getMessageBody(), 0, p.getMessageLength());
            showTextMessage(p.isIncoming(), message);
        }
    }

    private void showTextMessage(boolean isIncomingMessage, String text) {

        MyBaseAdapter.ViewHolder vh = mAdapter.createHolder();
        View v = vh.getConvertView();
        RelativeLayout ri = v.findViewById(R.id.RLL_IncomingMessage);
        RelativeLayout ro = v.findViewById(R.id.RLL_OutgoingMessage);
        if (isIncomingMessage) {
            ri.setVisibility(View.VISIBLE);
            ro.setVisibility(View.GONE);
            TextView tv = v.findViewById(R.id.TXV_IncomingTextMessage);
            tv.setText(text);
            vh.setView(R.id.TXV_IncomingTextMessage, View.VISIBLE);
        } else {
            ro.setVisibility(View.VISIBLE);
            ri.setVisibility(View.GONE);
            TextView tv = v.findViewById(R.id.TXV_OutgoingTextMessage);
            tv.setText(text);
            vh.setView(R.id.RLL_OutgoingMessage, View.VISIBLE);
        }
        mAdapter.notifyDataSetChanged();
    }

    private void switchAction(boolean isSend) {
        // change action button
        if (isSend) {
            mBTNAction.setVisibility(View.VISIBLE);
            mIMVMore.setVisibility(View.GONE);
        } else {
            mIMVMore.setVisibility(View.VISIBLE);
            mBTNAction.setVisibility(View.GONE);
        }
    }

    private void switchMessageMode(boolean isTextMode) {
        if (isTextMode) {
            mIMVType.setImageResource(R.mipmap.chatting_voice);
            mTXVTalk.setVisibility(View.GONE);

            mEDTText.setVisibility(View.VISIBLE);
            mEDTText.setFocusable(true);
            mEDTText.setFocusableInTouchMode(true);
            mEDTText.requestFocus();
            mEDTText.setShowSoftInputOnFocus(true);
        } else {
            mIMVType.setImageResource(R.mipmap.keyboard);
            mEDTText.setVisibility(View.GONE);
            mTXVTalk.setVisibility(View.VISIBLE);
        }
    }

/* ****************************************if****************************************************** */

    class UiHandler extends Handler{
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what){
                case MESSAGE_INCOMING:
                    switch (msg.arg1) {
                        case MESSAGE_TYPE_TEXT:
                            try {
                                String text = msg.getData().getString("Text");
                                showTextMessage(true, text);
                            } catch (NullPointerException e) {
                                e.printStackTrace();
                            }
                            break;
                        default:
                            break;
                    }
                    break;
                default:
                    break;
            }
        }
    }

    class MessageListener implements Messenger.MessageCallback{
        @Override
        public void onNewMessage(byte[] data, int length) {
            Log.v(TAG, "onNewMessage: " + length);
            try {
                Message msg = new Message();
                msg.what = MESSAGE_INCOMING;
                msg.arg1 = MESSAGE_TYPE_TEXT;
                Bundle b = new Bundle();
                String text = new String(data, 0, length, "UTF-8");
                Log.v(TAG, ": " + text);
                b.putString("Text", text);
                msg.setData(b);
                mHandler.sendMessage(msg);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
    }
}