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


public class SessionsFragment extends Fragment implements AdapterView.OnItemClickListener {
    private static final String TAG = "SessionsFragment";

    private OnListFragmentInteractionListener mListener;
    private MyBaseAdapter mAdapter;


/* ********************************************************************************************** */


    public SessionsFragment() {
    }


    public static SessionsFragment newInstance() {
        return new SessionsFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        mListener = null;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Log.v(TAG, "onItemClick: " + position);
    }

/* ********************************************************************************************** */

    private void initViews(View rootView){
        //initSessions();
        initListViews(rootView);

    }

    private void initListViews(View rootView){
        mAdapter = new MyBaseAdapter(this.getActivity(), R.layout.session_list);

        ListView lsv = rootView.findViewById(R.id.LSV_Sessions);
        lsv.setAdapter(mAdapter);
        lsv.setOnItemClickListener(this);
        reloadListView();
    }

    private void reloadListView(){
        Log.v(TAG, "reloadListView()");

        mAdapter.clearItemList();

        MyBaseAdapter.ViewHolder vh = mAdapter.createHolder();

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
