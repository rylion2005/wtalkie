package com.talkie.wtalkie.sockets;


import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;



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

    private static final String MULTICAST_ADDRESS = "233.230.200.111";
    private static final int SOCKET_PORT = 52555;
    private static final int SOCKET_BUFFER_BYTES = 10 * 1024;

    private final Listener mListener = new Listener();
    private final Sender mSender = new Sender();
    private StreamCallback mCallback;


/* ============================================================================================== */


    public Streamer(){
        (new Thread(mListener)).start();
    }

    public void register(StreamCallback cb){
        if (cb != null){
            mCallback = cb;
        }
	}

    public void flush(byte[] stream, int length){
        Log.v(TAG, "flush: " + length);
        mSender.flush(stream, length);
        if (!mSender.isRunning()){
            (new Thread(mSender)).start();
        }
    }

    public void stop(){
        mSender.setRunning(false);
    }


/* ============================================================================================== */

    class Listener implements Runnable {
        private volatile boolean mRunning = false;

        private MulticastSocket mSocket;
        private final DatagramPacket mPacket = new DatagramPacket(
                new byte[SOCKET_BUFFER_BYTES],
                SOCKET_BUFFER_BYTES);

        private Listener(){
            try {
                mSocket = new MulticastSocket(SOCKET_PORT);
                mSocket.setTimeToLive(1);
                mSocket.setBroadcast(true);
                mSocket.joinGroup(InetAddress.getByName(MULTICAST_ADDRESS));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void setRunning(boolean running){
            mRunning = running;
        }

        @Override
        public void run(){
            mRunning = true;
            Log.v(TAG, ":Listener: running ...");

            while (mRunning){
                try {
                    mSocket.receive(mPacket);
                    Log.v(TAG, ":Listener: receive: " + mPacket.getLength());
                    if (mCallback != null){
                        mCallback.onStreamBytes(mPacket.getData(), mPacket.getLength());
                    }
                } catch (IOException e) {
                    Log.e(TAG, ":Listener: IOException !");
                }
            }
            mSocket.close();
            Log.v(TAG, ":Listener: exit !");
        }
    }


    class Sender implements Runnable {

        volatile boolean mRunning = false;

        private MulticastSocket mSocket;
        private InetAddress mInetAddress;
        private final DatagramPacket mPacket = new DatagramPacket(
                new byte[SOCKET_BUFFER_BYTES],
                SOCKET_BUFFER_BYTES);


        private Sender(){
            try {
                mInetAddress = InetAddress.getByName(MULTICAST_ADDRESS);
                mSocket = new MulticastSocket();
                mSocket.setTimeToLive(1);
                mSocket.setLoopbackMode(true);
                mSocket.setBroadcast(true);
                mSocket.joinGroup(mInetAddress);

                mPacket.setAddress(mInetAddress);
                mPacket.setPort(SOCKET_PORT);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void flush(byte[] bytes, int length){
            synchronized (mPacket) {
                mPacket.setData(bytes, 0, length);
            }
        }

        private void setRunning(boolean running){
            mRunning = running;
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
                    synchronized (mPacket) {
                        if (mPacket.getLength() > 0) {
                            mSocket.send(mPacket);
                        } else {
                            break;
                        }
                    }
                }
                mSocket.close();
            } catch (IOException e) {
                Log.e(TAG, ":Sender: IOException !");
                e.printStackTrace();
            }
            Log.v(TAG, ":Sender: exit !");
        }
    }

    public interface StreamCallback {
        void onStreamBytes(byte[] data, int length);
    }
}
