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
import android.widget.ListView;

import com.talkie.wtalkie.R;
import com.talkie.wtalkie.contacts.Contacts;
import com.talkie.wtalkie.contacts.User;

import java.util.ArrayList;
import java.util.List;

public class ContactsFragment extends Fragment implements AdapterView.OnItemClickListener{
    private static final String TAG = "ContactsFragment";

    // options menu states
    private static final int OPTIONS_MENU_STATE_IDLE = 0;
    private static final int OPTIONS_MENU_STATE_SELECTED = 1;
    private static final int OPTIONS_MENU_STATE_MAX = 2;

    private static final int MESSAGE_UPDATE_MYSELF = 0xC1;
    private static final int MESSAGE_UPDATE_USER = 0xC2;

    private int mOptionsMenuState = OPTIONS_MENU_STATE_IDLE;

    private final MyHandler mHandler = new MyHandler();
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
        refreshContactViews();
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
        startActivity(new Intent(this.getActivity(), ChatActivity.class));
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.actionbar_contacts, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.SelectMore) {
            mOptionsMenuState = (mOptionsMenuState + 1) % OPTIONS_MENU_STATE_MAX;
            handleOptionsMenu(mOptionsMenuState);
        }
        return super.onOptionsItemSelected(item);
    }

    /* ********************************************************************************************** */

    public void refreshContactViews(){
        Log.v(TAG, "refreshContactViews");
        if ((mAdapter == null)){
            return;
        }

        synchronized (mAdapter.getLock()) {
            mAdapter.clearItemList();
        }
        Log.v(TAG, "Contacts: " + mContacts.getContacts().size());
        for (User user : mContacts.getContacts()) {
            MyBaseAdapter.ViewHolder vh = mAdapter.createHolder();
            vh.setTextView(R.id.TXV_IpAddress, user.getAddress());
        }

        mAdapter.notifyDataSetChanged();
    }


/* ********************************************************************************************** */

    private void initViews(View rootView){
        ActionBar ab = getActivity().getActionBar();
        initListViews(rootView);
    }

    private void initListViews(View rootView){
        mAdapter = MyBaseAdapter.newInstance(this.getActivity(), R.layout.contact_list);

        ListView lsv = rootView.findViewById(R.id.LSV_Contacts);
        lsv.setAdapter(mAdapter);
        lsv.setOnItemClickListener(this);

        refreshContactViews();
    }

    private void init(){
        mContacts = Contacts.getInstance();
        mContacts.register(new ContactsCallback());
    }

    private void handleOptionsMenu(int newState){
        Log.v(TAG, "Options Menu state: " + newState);
        switch (newState){
            case OPTIONS_MENU_STATE_IDLE:
                // reset to idle
                //refreshContacts(false);
                break;
            case OPTIONS_MENU_STATE_SELECTED:
                // show ui for user to select contacts
                //refreshContacts(true);
                break;
            default:
                break;
        }
    }

/* ********************************************************************************************** */


    class ContactsCallback implements Contacts.Callback{
        @Override
        public void onUpdateMyself() {
            mHandler.sendEmptyMessage(MESSAGE_UPDATE_MYSELF);
        }

        @Override
        public void onUpdateUsers() {
            mHandler.sendEmptyMessage(MESSAGE_UPDATE_USER);
        }
    }


/* ********************************************************************************************** */


    class MyHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case MESSAGE_UPDATE_MYSELF:
                    break;

                case MESSAGE_UPDATE_USER:
                    refreshContactViews();
                    break;
                default:
                    break;
            }
        }
    }

}
