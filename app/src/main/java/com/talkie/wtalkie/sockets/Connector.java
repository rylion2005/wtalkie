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
** Connector
**   This is a multicast socket useage demo
**
**
** See:
**   http://blog.csdn.net/u014602917/article/details/53303810
**
** USAGE:
**   1. Permissions
**      android.permission.CHANGE_WIFI_MULTICAST_STATE
**      <uses-permission android:name="android.permission.CHANGE_WIFI_MULTICAST_STATE"/>
*
**   2. MulticastLock
*
**   3. Broadcast address
*       Scope: 224.0.0.0—239.255.255.255
*       224.0.0.0—244.0.0.255:     永久组地址
*       224.0.1.0—244.0.2.255:     公用组播地址，可用于internet
*       224.0.2.0～238.255.255.255 临时组播地址，全网范围内有效
*       239.0.0.0—239.255.255.255: 本地管理组播地址，仅在特定的本地范围内有效
*
**
**
** ********************************************************************************
*/
public class Connector {
    public static final String TAG = "Connector";

    private static final String MULTICAST_ADDRESS = "239.230.200.100"; //"233.230.200.100";
    private static final int SOCKET_PORT = 52525;
    private static final int SOCKET_BUFFER_BYTES = 1024;

    private final Listener mListener = new Listener();
    private final Sender mSender = new Sender();
    private final Thread mSendThread = new Thread(mSender);

    private MessageCallback mCallback;



/* ============================================================================================== */


    public Connector(){
        (new Thread(mListener)).start();
    }

    public void register(MessageCallback cb){
        if (cb != null){
            mCallback = cb;
        }
	}

    public void broadcast(byte[] myself, int length){
        Log.v(TAG, "broadcast: " + length);
        mSender.flush(myself, length);
		if (!mSendThread.isAlive()){
            mSendThread.start();
        }
    }


/* ============================================================================================== */

    class Listener implements Runnable{
        private volatile boolean mRunning = false;

        private final DatagramPacket mPacket = new DatagramPacket(
                new byte[SOCKET_BUFFER_BYTES],
                SOCKET_BUFFER_BYTES);

        private Listener(){ }

        private void setRunning(boolean running){
            mRunning = running;
        }

        private boolean isRunning(){
            return mRunning;
        }

        @Override
        public void run(){
            mRunning = true;
            Log.v(TAG, ":Listener: running ...");
            while (mRunning){
                try {
                    MulticastSocket ms = new MulticastSocket(SOCKET_PORT);
                    ms.joinGroup(InetAddress.getByName(MULTICAST_ADDRESS));
                    ms.setTimeToLive(32);
                    ms.receive(mPacket);
                    if (mPacket.getLength() > 0){
                        Log.v(TAG, ":Listener: receive: " + mPacket.getLength());
                        if (mCallback != null){
                            mCallback.onUpdateUser(mPacket.getData(), mPacket.getLength());
                        }
                        mPacket.setLength(0);
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

        @Override
        public void run() {
            Log.v(TAG, ":Sender: running >>>>>>>>>>");
            try {
                MulticastSocket ms = new MulticastSocket();
                ms.setLoopbackMode(true);
                synchronized (mPacket) {
                    if (mPacket.getLength() > 0) {
                        ms.send(mPacket);
                        mPacket.setLength(0);
                    }
                }
                ms.close();
            } catch (IOException e) {
                Log.e(TAG, ":Sender: IOException !");
                e.printStackTrace();
            }
            Log.v(TAG, ":Sender: exit !!!!!!!!!!");
        }
    }


    public interface MessageCallback {
        void onUpdateUser(byte[] data, int length);
    }
}
