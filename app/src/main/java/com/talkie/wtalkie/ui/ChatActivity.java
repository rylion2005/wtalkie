package com.talkie.wtalkie.ui;


import android.content.Intent;
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
import com.talkie.wtalkie.audio.Recorder;
import com.talkie.wtalkie.contacts.Myself;
import com.talkie.wtalkie.contacts.User;
import com.talkie.wtalkie.contacts.Users;
import com.talkie.wtalkie.sessions.Session;
import com.talkie.wtalkie.sessions.SessionManager;
import com.talkie.wtalkie.sockets.Messenger;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;


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
    private boolean mMessageModeText = true;

    // Audio function
    private final Recorder mRecorder = Recorder.getInstance();
    private boolean mRecoding = false;

    // Messenger function
    private final Messenger mMessenger = Messenger.getInstance();

    private final List<Object> mMessageQueue = new ArrayList<>();
    private final UiHandler mHandler = new UiHandler();

    // Session information
    private Session mActiveSession;
    private int[] mUserIds;
    private String mTitle;


/* ********************************************************************************************** */


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        init();
        initViews();
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
                mRecoding = !mRecoding;
                if (mRecoding){
                    mTXVTalk.setText("Stop Talk");
                    mRecorder.start();
                } else {
                    mTXVTalk.setText("Start Talk");
                    mRecorder.stop();
                }
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
                try {
                    String input = mEDTText.getText().toString();
                    mMessenger.sendText(input.getBytes("UTF-8"),
                            input.getBytes("UTF-8").length);
                    // Show in list view
                    showTextMessage(false, input);
                    mEDTText.getText().clear();
                    switchAction(false);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
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
        MessageListener ml = new MessageListener();
        mMessenger.register(ml);
        mActiveSession = initSession();
    }

    private Session initSession(){
        Intent intent = getIntent();
        mUserIds = intent.getIntArrayExtra("UserIds");
        StringBuilder title = new StringBuilder(); // string for title bar

        User me = Myself.fromMyself(this);
        List<User> receivers = new ArrayList<>();
        for (int i = 0; i < mUserIds.length; i++){
            User u = Users.findAt(mUserIds[i]);
            if (i < 2) {
                title.append(u.getNick());
                if (i > 0) {
                    title.append(",");
                }
            }
            receivers.add(u);
        }

        if (mUserIds.length > 1){
            title.append("...");
        }

        Session ss = SessionManager.getInstance().getSession(me, receivers);
        if (ss.getState() == Session.SESSION_NOT_INITIALIZED){
            ss.setName(title.toString());
            ss.setType(Session.SESSION_TYPE_P2P);
            ss.setState(Session.SESSION_ACTIVE);
            SessionManager.getInstance().add(ss);
        } else {
            ss.setState(Session.SESSION_ACTIVE);
        }
        return ss;
    }

    private void initViews() {

        ActionBar ab = getSupportActionBar();
        ab.setTitle(mActiveSession.getName());

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


/* ********************************************************************************************** */




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