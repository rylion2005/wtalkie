package com.talkie.wtalkie.sockets;


import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;



public class Connector {
    public static final String TAG = "Connector";

    private static final String MULTICAST_ADDRESS = "233.230.200.100";
    private static final int SOCKET_PORT = 52525;
    private static final int SOCKET_BUFFER_BYTES = 1024;

    private final Listener mListener = new Listener();
    private final Sender mSender = new Sender();
    private final Thread mSendThread = new Thread(mSender);

    private Callback mCallback;
    private byte[] mBytesBuffer;
    private int    mBytesLength;


/* ============================================================================================== */


    public Connector(){
        (new Thread(mListener)).start();
    }

    public void register(Callback cb){
        if (cb != null){
            mCallback = cb;
        }
	}

    public void broadcast(byte[] myself, int length){
        Log.v(TAG, "broadcast: " + length);
        mBytesBuffer= myself.clone();
        mBytesLength = length;
        Log.v(TAG, "thread alive: " + mSendThread.isAlive());
		if (!mSendThread.isAlive()){
            mSendThread.start();
        }
    }


/* ============================================================================================== */

    class Listener implements Runnable{
        private volatile boolean mRunning = false;

        public Listener(){ }

        public void setRunnable(boolean running){
            mRunning = running;
        }

        @Override
        public void run(){
            mRunning = true;
            Log.v(TAG, ":Listener: running ...");
            while (mRunning){
                try {
                    byte[] data = new byte[SOCKET_BUFFER_BYTES];
                    InetAddress addr = InetAddress.getByName(MULTICAST_ADDRESS);
                    MulticastSocket ms = new MulticastSocket(SOCKET_PORT);
                    ms.joinGroup(addr);
                    ms.setTimeToLive(32);
                    DatagramPacket packet = new DatagramPacket(data, data.length);
                    Log.v(TAG, ":Listener: listening ...");
                    ms.receive(packet);
                    int length = packet.getLength();
                    Log.v(TAG, ":Listener: receive : " + length);
                    if (length > 0){
                        if (mCallback != null){
                            mCallback.onUpdateUser(packet.getData(), length);
                        }
                    }
                    ms.close();
                } catch (IOException e) {
                    Log.e(TAG, ":Listener: IOException !");
                }
            }
            Log.v(TAG, ":Listener: exit !");
        }
    }


    class Sender implements Runnable {

        @Override
        public void run() {
            Log.v(TAG, ":Sender: running ...");
            try {
                DatagramPacket packet = new DatagramPacket(
                        mBytesBuffer,
                        mBytesLength,
                        InetAddress.getByName(MULTICAST_ADDRESS),
                        SOCKET_PORT);
                MulticastSocket ms = new MulticastSocket();
                //ms.setLoopbackMode(true);
                ms.send(packet);
                ms.close();
            } catch (IOException e) {
                Log.e(TAG, ":Sender: IOException !");
                e.printStackTrace();
            }
            Log.v(TAG, ":Sender: exit !");
        }
    }


    public interface Callback {
        void onUpdateUser(byte[] data, int length);
    }
}
