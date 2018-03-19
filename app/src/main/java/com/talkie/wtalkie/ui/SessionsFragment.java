package com.talkie.wtalkie.ui;

import android.content.Context;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.talkie.wtalkie.R;
import com.talkie.wtalkie.contacts.User;
import com.talkie.wtalkie.contacts.Users;
import com.talkie.wtalkie.sessions.Message;
import com.talkie.wtalkie.sessions.Session;
import com.talkie.wtalkie.sessions.Sessions;

import java.text.SimpleDateFormat;
import java.util.Date;


public class SessionsFragment extends Fragment implements AdapterView.OnItemClickListener {
    private static final String TAG = "SessionsFragment";

    private MyBaseAdapter mAdapter;
    private Sessions mSessions;


/* ********************************************************************************************** */


    public SessionsFragment() { }


    public static SessionsFragment newInstance() {
        return new SessionsFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSessions = Sessions.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sessions, container, false);
        initViews(view);
        return view;
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
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Log.v(TAG, "onItemClick: " + position);
    }

/* ********************************************************************************************** */


    private void initViews(View rootView){
        mAdapter = new MyBaseAdapter(this.getActivity(), R.layout.session_list);
        ListView lsv = rootView.findViewById(R.id.LSV_Sessions);
        lsv.setAdapter(mAdapter);
        lsv.setOnItemClickListener(this);
        refreshViews();
    }

    private void refreshViews(){
        Log.v(TAG, "refreshViews()");

        mAdapter.clearItemList();
        for (Session s : mSessions.getSessionList()){
            Log.v(TAG, "Session: " + s.encode());
            MyBaseAdapter.ViewHolder vh = mAdapter.createHolder();

            // get originator
            vh.setTextView(R.id.TXV_Originator, Users.findByUid(s.getUidFrom()).getNick());

            // get last message
            Message msg = mSessions.getLastMessage(s.getTime());
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
