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
public class Messenger {
    public static final String TAG = "Connector";

    private static final String MULTICAST_ADDRESS = "224.0.0.122";
    private static final int SOCKET_PORT = 52521;
    private static final int SOCKET_BUFFER_BYTES = 1024;

    private static final Messenger mInstance = new Messenger();

    private final Listener mListener = new Listener();
    private final Sender mSender = new Sender();
    private MessageCallback mCallback;


/* ============================================================================================== */


    private Messenger(){
        (new Thread(mListener)).start();
    }

    public static Messenger getInstance(){
        return mInstance;
    }

    public void register(MessageCallback cb){
        if (cb != null){
            mCallback = cb;
        }
	}

    public void sendText(byte[] bytes, int length){
        Log.v(TAG, "sendText: " + length);
        mSender.flush(bytes, length);
        if (!mSender.isRunning()){
            (new Thread(mSender)).start();
        }
    }


/* ============================================================================================== */

    class Listener implements Runnable{
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
                    mSocket.receive(mPacket);
                    Log.v(TAG, ":Listener: receive: " + mPacket.getLength());
                    if (mCallback != null){
                        mCallback.onNewMessage(mPacket.getData(), mPacket.getLength());
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
        private volatile boolean mRunning = false;

        private final DatagramPacket mPacket = new DatagramPacket(
                new byte[SOCKET_BUFFER_BYTES],
                SOCKET_BUFFER_BYTES);

        private InetAddress mInetAddress;

        private Sender(){
            try {
                mInetAddress = InetAddress.getByName(MULTICAST_ADDRESS);
                mPacket.setAddress(mInetAddress);
                mPacket.setPort(SOCKET_PORT);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void setRunning(boolean running){
            mRunning = running;
        }

        private boolean isRunning(){
            return mRunning;
        }

        private void flush(byte[] bytes, int length){
            mPacket.setData(bytes, 0, length);
        }

        @Override
        public void run() {
            Log.v(TAG, ":Sender: >>>");
            try {
                MulticastSocket ms = new MulticastSocket();
                ms.setTimeToLive(1);
                //ms.setLoopbackMode(true);
                ms.setBroadcast(true);
                ms.joinGroup(mInetAddress);
                ms.send(mPacket);
                ms.close();
            } catch (IOException e) {
                Log.e(TAG, ":Sender: IOException !");
                e.printStackTrace();
            }
            Log.v(TAG, ":Sender: ~~~");
        }
    }

    public interface MessageCallback {
        void onNewMessage(byte[] data, int length);
    }
}
