package com.talkie.wtalkie.ui;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.talkie.wtalkie.R;
import com.talkie.wtalkie.contacts.User;


public class ProfileFragment extends Fragment{
    private static final String TAG = "ProfileFragment";

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
        mView = inflater.inflate(R.layout.fragment_profile, container, false);
        refreshViews();
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

    public void refreshViews(){
        if (mView == null){
            return;
        }

        TextView TXVUuid = mView.findViewById(R.id.TXV_Uuid);
        TextView TXVSerial = mView.findViewById(R.id.TXV_Serial);
        TextView TXVAddress = mView.findViewById(R.id.TXV_Address);

        User me = User.fromSharePreference(getActivity().getApplicationContext());
        TXVUuid.setText(me.getUuid());
        TXVSerial.setText(me.getSerial());
        TXVAddress.setText(me.getAddress());
    }
}
