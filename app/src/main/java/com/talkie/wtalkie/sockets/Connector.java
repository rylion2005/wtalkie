package com.talkie.wtalkie.sockets;


import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;


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
                    ms.receive(packet);
                    int length = packet.getLength();
                    Log.v(TAG, ":Listener: receive: " + length);
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
            Log.v(TAG, ":Sender: running >>>>>>>>>>");
            try {
                DatagramPacket packet = new DatagramPacket(
                        mBytesBuffer,
                        mBytesLength,
                        InetAddress.getByName(MULTICAST_ADDRESS),
                        SOCKET_PORT);
                MulticastSocket ms = new MulticastSocket();
                ms.setLoopbackMode(true);
                ms.send(packet);
                ms.close();
            } catch (IOException e) {
                Log.e(TAG, ":Sender: IOException !");
                e.printStackTrace();
            }
            Log.v(TAG, ":Sender: exit !!!!!!!!!!");
        }
    }


    public interface Callback {
        void onUpdateUser(byte[] data, int length);
    }
}
