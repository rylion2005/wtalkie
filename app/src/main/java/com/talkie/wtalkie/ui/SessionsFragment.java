package com.talkie.wtalkie.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.talkie.wtalkie.R;
import com.talkie.wtalkie.contacts.Myself;
import com.talkie.wtalkie.sessions.Packet;
import com.talkie.wtalkie.sessions.Session;
import com.talkie.wtalkie.sessions.SessionManager;

import java.text.SimpleDateFormat;
import java.util.Date;


public class SessionsFragment extends Fragment
        implements AdapterView.OnItemClickListener,
        AdapterView.OnItemLongClickListener,
        View.OnClickListener{
    private static final String TAG = "SessionsFragment";

    private MyBaseAdapter mAdapter;
    private SessionManager mSessionManager;

    private int mSelectedPosition = -1;


/* ********************************************************************************************** */

    public SessionsFragment() { }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSessionManager = SessionManager.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sessions, container, false);
        initViews(view);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshViews();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        // TODO:
        /*
        if (context instanceof OnListFragmentInteractionListener) {
            mListener = (OnListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
        */
    }

    @Override
    public void onDetach() {
        super.onDetach();
        //mListener = null;
    }

    @Override
    public void onClick(View v) {
        Log.v(TAG, "onClick: " + v.toString());

        Intent intent;
        Bundle b = new Bundle();
        switch (v.getId()){
            case R.id.LNL_ChatRoomA:
            case R.id.LNL_ChatRoomB:
                intent = new Intent(this.getActivity(), ChatActivity.class);
                b.putInt("SessionType", Session.SESSION_TYPE_CHAT_ROOM);
                if (v.getId() == R.id.LNL_ChatRoomA) {
                    b.putString("RoomName", "Chat Room A");
                } else {
                    b.putString("RoomName", "Chat Room B");
                }
                b.putString("Uid", Myself.fromMyself(this.getActivity()).getUid());
                intent.putExtras(b);
                startActivity(intent);
                break;
            case R.id.LNL_TalkChannelA:
            case R.id.LNL_TalkChannelB:
                intent = new Intent(this.getActivity(), ChatActivity.class);
                b.putInt("SessionType", Session.SESSION_TYPE_TALK_CHANNEL);
                if (v.getId() == R.id.LNL_TalkChannelA) {
                    b.putString("ChannelName", "Talk Channel A");
                } else {
                    b.putString("ChannelName", "Talk Channel B");
                }
                b.putString("Uid", Myself.fromMyself(this.getActivity()).getUid());
                intent.putExtras(b);
                startActivity(intent);
                break;
            default:
                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Log.v(TAG, "onItemClick: " + position);
        Intent intent = new Intent(this.getActivity(), ChatActivity.class);
        Bundle b = new Bundle();
        b.putInt("SessionType", Session.SESSION_TYPE_TEMPORARY);
        MyBaseAdapter.ViewHolder vh = (MyBaseAdapter.ViewHolder) mAdapter.getItem(position);
        String name = ((TextView) (vh.getView(R.id.TXV_SessionName))).getText().toString();
        b.putString("SessionName", name);
        intent.putExtras(b);
        startActivity(intent);
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        Log.v(TAG, "onItemLongClick: " + position);
        mSelectedPosition = position;
        new AlertDialog.Builder(this.getActivity())
                .setTitle("Delete session")
                .setMessage("Delete?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mSessionManager.deleteSession(mSelectedPosition);
                        refreshViews();
                    }
                })
                .setNegativeButton("No", null)
                .show();

        return true;
    }


/* ********************************************************************************************** */


    private void initViews(View rootView){

        LinearLayout chatroomA = rootView.findViewById(R.id.LNL_ChatRoomA);
        LinearLayout chatroomB = rootView.findViewById(R.id.LNL_ChatRoomB);
        LinearLayout talkChannelA = rootView.findViewById(R.id.LNL_TalkChannelA);
        LinearLayout talkChannelB = rootView.findViewById(R.id.LNL_TalkChannelB);
        chatroomA.setOnClickListener(this);
        chatroomB.setOnClickListener(this);
        talkChannelA.setOnClickListener(this);
        talkChannelB.setOnClickListener(this);

        mAdapter = new MyBaseAdapter(this.getActivity(), R.layout.session_list);
        ListView lsv = rootView.findViewById(R.id.LSV_Sessions);
        lsv.setAdapter(mAdapter);
        lsv.setOnItemClickListener(this);
        lsv.setOnItemLongClickListener(this);

        refreshViews();
    }

    private void refreshViews(){
        Log.v(TAG, "refreshViews()");

        mAdapter.clearItemList();
        Log.v(TAG, "Session List: " + mSessionManager.getSessionList(Session.SESSION_TYPE_TEMPORARY));
        for (Session s : mSessionManager.getSessionList(Session.SESSION_TYPE_TEMPORARY)){
            Log.v(TAG, "Session: " + s.encode());
            MyBaseAdapter.ViewHolder vh = mAdapter.createHolder();

            // get originator
            vh.setTextView(R.id.TXV_SessionName, s.getName());

            // get last message
            Packet msg = mSessionManager.getLastMessage(s.getTime());
            if (msg != null) {
                Log.d(TAG, "last message: " + msg.toJsonString());
                vh.setTextView(R.id.TXV_LastMessage, msg.getDescription());

                // get last message time
                Date dateTime = new Date(msg.getTime());
                SimpleDateFormat format = new SimpleDateFormat("hh:mm:ss");
                vh.setTextView(R.id.TXV_LastMessageTime, format.format(dateTime));
            }
        }
        mAdapter.notifyDataSetChanged();
    }

/* ********************************************************************************************** */

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnListFragmentInteractionListener {
        // TODO: Update argument type and name
        void onListFragmentInteraction();
    }
}
