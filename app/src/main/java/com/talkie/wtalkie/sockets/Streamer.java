package com.talkie.wtalkie.sockets;


import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;


public class Streamer {
    public static final String TAG = "Streamer";

    private static final String MULTICAST_ADDRESS = "233.230.200.111";
    private static final int SOCKET_PORT = 52555;
    private static final int SOCKET_BUFFER_BYTES = 10 * 1024;

    private final Listener mListener = new Listener();
    private final Sender mSender = new Sender();
    private final Thread mSendThread = new Thread(mSender);
    private Callback mCallback;


/* ============================================================================================== */


    public Streamer(){
        (new Thread(mListener)).start();
    }

    public void register(Callback cb){
        if (cb != null){
            mCallback = cb;
        }
	}

    public void flushStream(byte[] stream, int length){
        Log.v(TAG, "flushStream: " + length);
        mSender.flush(stream, length);
        Log.v(TAG, "thread alive: " + mSendThread.isAlive());
		if (!mSendThread.isAlive()){
            mSendThread.start();
        }
    }


/* ============================================================================================== */

    class Listener implements Runnable {
        private volatile boolean mRunning = false;

        private Listener(){ }

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
                            mCallback.onAudioBytes(packet.getData(), length);
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
        volatile boolean mStop = false;

        private byte[] mBytesBuffer = new byte[SOCKET_BUFFER_BYTES];
        private int    mBytesLength;
        private final Object mLock = new Object();

        private void flush(byte[] bytes, int length){
            mBytesBuffer = bytes.clone();
            mBytesLength = length;
        }

        @Override
        public void run() {
            Log.v(TAG, ":Sender: running ...");
            try {
                MulticastSocket ms = new MulticastSocket();
                ms.setLoopbackMode(true);
                while(!mStop) {
                    if (mBytesLength > 0) {
                        synchronized (mLock) {
                            DatagramPacket packet = new DatagramPacket(
                                    mBytesBuffer,
                                    mBytesLength,
                                    InetAddress.getByName(MULTICAST_ADDRESS),
                                    SOCKET_PORT);
                            ms.send(packet);
                            mBytesLength = -1;
                        }
                    }
                }
                ms.close();
            } catch (IOException e) {
                Log.e(TAG, ":Sender: IOException !");
                e.printStackTrace();
            }
            Log.v(TAG, ":Sender: exit !");
        }
    }

    public interface Callback {
        void onAudioBytes(byte[] data, int length);
    }
}
