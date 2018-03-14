package com.talkie.wtalkie.ui;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.talkie.wtalkie.R;
import com.talkie.wtalkie.contacts.Contacts;
import com.talkie.wtalkie.contacts.Identity;


public class ProfileFragment extends Fragment{
    private static final String TAG = "ProfileFragment";

    private Context mContext;
    private View mView;


/* ********************************************************************************************** */


    public ProfileFragment() {
        Log.v(TAG, "new ProfileFragment");
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Log.v(TAG, "onAttach");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.v(TAG, "onCreate");
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        Log.v(TAG, "onCreateView");
        mContext = getActivity().getApplicationContext();
        mView = inflater.inflate(R.layout.fragment_profile, container, false);
        updateViews();
        return mView;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }


/* ********************************************************************************************** */

    private void updateViews(){
        if (mView == null){
            return;
        }

        TextView TXVUuid = mView.findViewById(R.id.TXV_Uuid);
        TextView TXVSerial = mView.findViewById(R.id.TXV_Serial);
        TextView TXVAddress = mView.findViewById(R.id.TXV_Address);
        TXVUuid.setText(Identity.getInstance(mContext).genShortUuid());
        TXVSerial.setText(Identity.getInstance(mContext).getSerial());
        TXVAddress.setText(Identity.getInstance(mContext).getLocalAddress());
    }
}
