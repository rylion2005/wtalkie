package com.talkie.wtalkie.sockets;


import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;


/*
** ********************************************************************************
**
** Streamer
**   This class is used to broadcast stream data.
**
** USAGE:
**   Listener MUST be always running
*    Sender will be always running, and should be stoppable by clients!
**
** ********************************************************************************
*/
public class Streamer {
    public static final String TAG = "Streamer";

    //private static final String SOCKET_ADDRESS = "224.0.0.131";
    private static final int SOCKET_PORT = 52545;
    private static final int SOCKET_BUFFER_BYTES = 10 * 1024;

    private static final Streamer mInstance = new Streamer();
    private final Listener mListener = new Listener();
    private final Sender mSender = new Sender();
    private final List<Sender> mSenders = new ArrayList<>();


/* ********************************************************************************************** */

    private Streamer(){
        (new Thread(mListener)).start();
    }

    public static Streamer getInstance(){
        return mInstance;
    }

    public void register(StreamCallback cb){
        mListener.register(cb);
	}

	public void startStream(List<String> hostList){
        mSenders.clear();
        for (String ip : hostList){
            Sender s = new Sender(ip);
            mSenders.add(s);
        }
    }

    public void startStream(String host){
        mSender.init(host);
        (new Thread(mSender)).start();
    }

    public void stopStream(){
        Log.v(TAG, "stop stream: ");
        //synchronized (mSender) {
            //mSender.stopRunning();
        //}
    }

    public void flush(byte[] stream, int length){
        Log.v(TAG, "flush: " + length);
        //synchronized (mSender) {
            mSender.flush(stream, length);
            if (!mSender.isRunning()) {
                (new Thread(mSender)).start();
            }
        //}
    }

/* ********************************************************************************************** */

    class Listener implements Runnable {
        private volatile boolean mRunning = false;
        private StreamCallback mCallback;

        private final DatagramPacket mPacket = new DatagramPacket(
                new byte[SOCKET_BUFFER_BYTES],
                SOCKET_BUFFER_BYTES);

        private Listener(){}

        private void register(StreamCallback scb){
            if (scb != null){
                mCallback = scb;
            }
        }

        public boolean isRunning(){
            return mRunning;
        }

        public void stopRunning(){
            mRunning = false;
        }

        @Override
        public void run(){
            mRunning = true;
            Log.v(TAG, ":Listener: running ...");
            try {
                while (mRunning) {
                    DatagramSocket ds = new DatagramSocket(SOCKET_PORT);
                    ds.receive(mPacket);
                    Log.v(TAG, ":Listener: receive: " + mPacket.getLength());
                    if (mCallback != null) {
                        mCallback.onStreamInput(mPacket.getData(), mPacket.getLength());
                    }
                    ds.close();
                }
            } catch (IOException e) {
                Log.e(TAG, ":Listener: IOException !");
            }
            Log.v(TAG, ":Listener: exit !");
        }
    }

/* ********************************************************************************************** */

    class Sender implements Runnable {

        volatile boolean mRunning = false;
        private DatagramSocket mSocket;
        private final DatagramPacket mPacket = new DatagramPacket(
                new byte[SOCKET_BUFFER_BYTES],
                SOCKET_BUFFER_BYTES);

        public Sender(){}

        private Sender(String host){
            try {
                InetAddress address = InetAddress.getByName(host);
                mSocket = new DatagramSocket();
                mPacket.setAddress(address);
                mPacket.setPort(SOCKET_PORT);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void init(String host){
            try {
                InetAddress address = InetAddress.getByName(host);
                mSocket = new DatagramSocket();
                mPacket.setAddress(address);
                mPacket.setPort(SOCKET_PORT);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void flush(byte[] bytes, int length){
            //synchronized (mPacket) {
                mPacket.setData(bytes, 0, length);
            //}
        }

        private void stopRunning(){
            mRunning = false;
        }

        private boolean isRunning(){
            return mRunning;
        }

        @Override
        public void run() {
            Log.v(TAG, ":Sender: running ...");
            mRunning = true;

            try {
                while(mRunning) {
                    Log.v(TAG, "Length: " + mPacket.getLength());
                    if (mPacket.getLength() > 0) {
                        mSocket.send(mPacket);
                    }
                }
            } catch (IOException e) {
                Log.e(TAG, ":Sender: IOException !");
                e.printStackTrace();
            } finally {
               mSocket.close();
               mRunning = false;
            }
            Log.v(TAG, ":Sender: exit !");
        }
    }

/* ********************************************************************************************** */

    public interface StreamCallback {
        void onStreamInput(byte[] data, int length);
    }
}
