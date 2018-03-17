package com.talkie.wtalkie.ui;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.talkie.wtalkie.R;
import com.talkie.wtalkie.contacts.Myself;
import com.talkie.wtalkie.contacts.User;


public class ProfileFragment extends Fragment
        implements View.OnClickListener,
        TextView.OnEditorActionListener{
    private static final String TAG = "ProfileFragment";

    private View mView;
    private Context mContext;


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
        mContext = getActivity().getApplicationContext();
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

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.IMV_Avatar:
                break;
            case R.id.TXV_UserId:
                TextView TXVUser = mView.findViewById(R.id.TXV_UserId);
                EditText EDTUser = mView.findViewById(R.id.EDT_UserId);
                TXVUser.setVisibility(View.GONE);
                EDTUser.setVisibility(View.VISIBLE);
                EDTUser.setHint("只能修改一次！！！");
                break;
            case R.id.TXV_NickName:
                TextView TXVNick = mView.findViewById(R.id.TXV_NickName);
                EditText EDTNick = mView.findViewById(R.id.EDT_NickName);
                TXVNick.setVisibility(View.GONE);
                EDTNick.setVisibility(View.VISIBLE);
                break;
            default:
                break;
        }
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        switch (v.getId()){
            case R.id.EDT_UserId:
                if (actionId == EditorInfo.IME_ACTION_DONE){
                    TextView TXVUser = mView.findViewById(R.id.TXV_UserId);
                    EditText EDTUser = mView.findViewById(R.id.EDT_UserId);
                    String text = EDTUser.getText().toString();
                    TXVUser.setText(text);
                    TXVUser.setVisibility(View.VISIBLE);
                    EDTUser.setVisibility(View.GONE);
                    Myself.updateUserId(mContext, text);
                }
                break;
            case R.id.EDT_NickName:
                if (actionId == EditorInfo.IME_ACTION_DONE){
                    TextView TXVNick = mView.findViewById(R.id.TXV_NickName);
                    EditText EDTNick = mView.findViewById(R.id.EDT_NickName);
                    String text = EDTNick.getText().toString();
                    TXVNick.setText(text);
                    TXVNick.setVisibility(View.VISIBLE);
                    EDTNick.setVisibility(View.GONE);
                    Myself.updateNickName(mContext, text);
                }
                break;
            default:
                break;
        }
        return false;
    }

    /* ********************************************************************************************** */

    public void refreshViews(){
        if (mView == null){
            return;
        }

        ImageView IMVAvatar = mView.findViewById(R.id.IMV_Avatar);
        TextView TXVUser = mView.findViewById(R.id.TXV_UserId);
        EditText EDTUser = mView.findViewById(R.id.EDT_UserId);
        TextView TXVNick = mView.findViewById(R.id.TXV_NickName);
        EditText EDTNick = mView.findViewById(R.id.EDT_NickName);
        TextView TXVUuid = mView.findViewById(R.id.TXV_Uuid);
        TextView TXVSerial = mView.findViewById(R.id.TXV_Serial);
        TextView TXVAddress = mView.findViewById(R.id.TXV_Address);

        User me = Myself.buildMyself(getActivity().getApplicationContext());
        TXVUser.setText(me.getUser());
        TXVNick.setText(me.getNick());
        TXVUuid.setText(me.getUuid());
        TXVSerial.setText(me.getSerial());
        TXVAddress.setText(me.getAddress());

        IMVAvatar.setOnClickListener(this);
        TXVNick.setOnClickListener(this);
        EDTNick.setOnEditorActionListener(this);
        if (me.getUser().equals("unknown")){
            TXVUser.setOnClickListener(this);
            EDTUser.setOnEditorActionListener(this);
        }
    }

}
