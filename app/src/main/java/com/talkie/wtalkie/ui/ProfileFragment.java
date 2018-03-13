package com.talkie.wtalkie.ui;

import android.app.Fragment;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.talkie.wtalkie.R;
import com.talkie.wtalkie.contacts.Contacts;


public class ProfileFragment extends Fragment{
    private static final String TAG = "ProfileFragment";

    private TextView mTXVUuid;
    private TextView mTXVSerial;
    private TextView mTXVAddress;


    private OnFragmentInteractionListener mListener;


/* ********************************************************************************************** */


    public ProfileFragment() {
        // Required empty public constructor

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Log.v(TAG, "onAttach");
        /*
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
        */
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
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        initViews(view);
        return view;
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
        mListener = null;
    }


/* ********************************************************************************************** */

/* ********************************************************************************************** */


    private void initViews(View rootView){
        mTXVUuid = rootView.findViewById(R.id.TXV_Uuid);
        mTXVSerial = rootView.findViewById(R.id.TXV_Serial);
        mTXVAddress = rootView.findViewById(R.id.TXV_Address);
        mTXVUuid.setText(Contacts.getInstance().getMyself().getUuid());
        mTXVSerial.setText(Contacts.getInstance().getMyself().getSerial());
        mTXVAddress.setText(Contacts.getInstance().getMyself().getAddress());
    }


/* ********************************************************************************************** */


    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
