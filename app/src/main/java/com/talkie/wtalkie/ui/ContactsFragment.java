package com.talkie.wtalkie.ui;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.ListView;

import com.talkie.wtalkie.R;
import com.talkie.wtalkie.contacts.Contacts;
import com.talkie.wtalkie.contacts.User;

import java.util.ArrayList;
import java.util.List;

public class ContactsFragment extends Fragment implements AdapterView.OnItemClickListener{
    private static final String TAG = "ContactsFragment";
    private static final int SELECT_STATE_IDLE = 0;
    private static final int SELECT_STATE_GROUP_TALK = 1;
    private static final int SELECT_STATE_MAX = 2;

    private int mState = SELECT_STATE_IDLE;
    private boolean mChecked = false;

    private MyBaseAdapter mAdapter;
    private Contacts mContacts;


/* ********************************************************************************************** */


    public ContactsFragment() { }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Log.v(TAG, "onAttach");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.v(TAG, "onCreate");
        init();
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.v(TAG, "onCreateView");
        View view = inflater.inflate(R.layout.fragment_contacts, container, false);
        initViews(view);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.v(TAG, "onResume");
        refreshContacts(false);
    }

    @Override
    public void onPause() {
        Log.v(TAG, "onPause");
        super.onPause();
    }

    @Override
    public void onDestroy() {
        Log.v(TAG, "onDestroy");
        super.onDestroy();
    }

    @Override
    public void onDetach() {
        Log.v(TAG, "onDetach");
        super.onDetach();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Log.v(TAG, "onItemClick: " + position);
        // state machine
        if (mState == SELECT_STATE_IDLE){
            startActivity(new Intent(this.getActivity(), ChatActivity.class));
        } else { //mState == SELECT_STATE_GROUP_TALK
            CheckBox cb = view.findViewById(R.id.CHB_SelectContacts);
            mChecked = !mChecked;
            cb.setChecked(mChecked);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.actionbar_contacts, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // switch select state between SELECT_STATE_IDLE and SELECT_STATE_GROUP_TALK
        if (item.getItemId() == R.id.SelectMore) {
            if (mState == SELECT_STATE_IDLE){
                mState = SELECT_STATE_GROUP_TALK;
                item.setTitle("Originate Talk");
                refreshContacts(true);
            } else {
                mState = SELECT_STATE_IDLE;
                item.setTitle("Select More");
                refreshContacts(false);
            }
        }
        return super.onOptionsItemSelected(item);
    }

/* ********************************************************************************************** */

    public void updateUsers(){
        //TODO:
    }

/* ********************************************************************************************** */


    private void refreshContacts(boolean showCheckbox){
        Log.v(TAG, "refreshContacts");
        if ((mAdapter == null)){
            return;
        }

        synchronized (mAdapter.getLock()) {
            mAdapter.clearItemList();
        }

        Contacts c = Contacts.getInstance();
        for (User user : c.getContacts()) {
            MyBaseAdapter.ViewHolder vh = mAdapter.createHolder();
            vh.setTextView(R.id.TXV_IpAddress, user.getAddress());
            if (showCheckbox) {
                vh.setView(R.id.CHB_SelectContacts, View.VISIBLE);
            } else {
                vh.setView(R.id.CHB_SelectContacts, View.GONE);
            }
        }
        mAdapter.notifyDataSetChanged();
    }


    private void initViews(View rootView){
        ActionBar ab = getActivity().getActionBar();

        mAdapter = MyBaseAdapter.newInstance(this.getActivity(), R.layout.contact_list);
        ListView lsv = rootView.findViewById(R.id.LSV_Contacts);
        lsv.setAdapter(mAdapter);
        lsv.setOnItemClickListener(this);
        refreshContacts(false);
    }

    private void init(){
        mContacts = Contacts.getInstance();
    }

/* ********************************************************************************************** */


}
