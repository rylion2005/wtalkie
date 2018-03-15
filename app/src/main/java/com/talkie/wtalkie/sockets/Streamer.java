package com.talkie.wtalkie.sockets;


import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;


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
            mSender.setRunning(true);
            (new Thread(mSender)).start();
        }
    }


/* ============================================================================================== */

    class Listener implements Runnable {
        private volatile boolean mRunning = false;

        private Listener(){ }

        public void setRunning(boolean running){
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
                    ms.receive(packet);
                    int length = packet.getLength();
                    Log.v(TAG, ":Listener: receive: " + length);
                    if (length > 0){
                        if (mCallback != null){
                            mCallback.onStreamBytes(packet.getData(), length);
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

        volatile boolean mRunning = false;
        private final DatagramPacket mPacket = new DatagramPacket(
                new byte[SOCKET_BUFFER_BYTES],
                SOCKET_BUFFER_BYTES);


        private Sender(){
            try {
                mPacket.setAddress(InetAddress.getByName(MULTICAST_ADDRESS));
                mPacket.setPort(SOCKET_PORT);
            } catch (UnknownHostException e) {
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
                MulticastSocket ms = new MulticastSocket();
                ms.setLoopbackMode(true);

                while(mRunning) {
                    synchronized (mPacket) {
                        if (mPacket.getLength() > 0) {
                            ms.send(mPacket);
                            mPacket.setLength(-1);
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

    public interface StreamCallback {
        void onStreamBytes(byte[] data, int length);
    }
}
