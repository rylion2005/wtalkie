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
public class MulticastSocketStreamer {
    public static final String TAG = "MulticastSocketStreamer";

    private static final String MULTICAST_ADDRESS = "224.0.0.121";
    private static final int SOCKET_PORT = 52555;
    private static final int SOCKET_BUFFER_BYTES = 10 * 1024;

    private static final MulticastSocketStreamer mInstance = new MulticastSocketStreamer();
    private final Listener mListener = new Listener();
    private final Sender mSender = new Sender();


/* ********************************************************************************************** */

    private MulticastSocketStreamer(){
        (new Thread(mListener)).start();
    }

    public static MulticastSocketStreamer getInstance(){
        return mInstance;
    }

    public void register(StreamCallback cb){
        mListener.register(cb);
	}

    public void stopStream(){
        Log.v(TAG, "stop stream: ");
        //synchronized (mSender) {
            mSender.stopRunning();
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

        private Listener(){ }

        private void register(StreamCallback scb){
            if (scb != null){
                mCallback = scb;
            }
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
                    MulticastSocket ms = new MulticastSocket(SOCKET_PORT);
                    ms.setTimeToLive(1);
                    ms.setBroadcast(true);
                    ms.joinGroup(InetAddress.getByName(MULTICAST_ADDRESS));
                    ms.receive(mPacket);
                    while(mPacket.getLength() > 0) {
                        Log.v(TAG, ":Listener: receive: " + mPacket.getLength());
                        if (mCallback != null) {
                            mCallback.onStreamInput(mPacket.getData(), mPacket.getLength());
                        }
                    }
                    ms.close();
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
                    mSocket.send(mPacket);
                }
            } catch (IOException e) {
                Log.e(TAG, ":Sender: IOException !");
                e.printStackTrace();
            } finally {
                try {
                    mSocket.close();
                }catch (NullPointerException e){
                    // do nothing
                }
            }
            Log.v(TAG, ":Sender: exit !");
        }
    }

/* ********************************************************************************************** */

    public interface StreamCallback {
        void onStreamInput(byte[] data, int length);
    }
}
