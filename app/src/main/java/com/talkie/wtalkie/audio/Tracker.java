package com.talkie.wtalkie.audio;


import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;


/*
**
** ${FILE}
**   ...
**
** REVISED HISTORY
**   yl7 | 18-2-16: Created
**     
*/
public class Tracker {
    private static final String TAG = "Tracker";

    private static Tracker mInstance;
    private final PlayRunnable mPlayRunnable = new PlayRunnable();


/* ********************************************************************************************** */


    private Tracker(){
        Log.v(TAG, "new tracker");
    }

    public static Tracker newInstance(){
        if (mInstance == null){
            mInstance = new Tracker();
        }
        return mInstance;
    }

    public void play(byte[] bytes, int length){
        Log.v(TAG, "play: " + length);
        mPlayRunnable.flush(bytes, length);
        if (mPlayRunnable.getStopSignal()) {
            Thread t = new Thread(mPlayRunnable);
            t.start();
        }
    }

    public void stop(){
        mPlayRunnable.setStopSignal();
    }


/* ********************************************************************************************** */


    class PlayRunnable implements Runnable {
        private final static int AUDIO_STREAM = AudioManager.STREAM_MUSIC;
        private final static int AUDIO_SAMPLE_RATE = 44100; //44.1khz
        private final static int AUDIO_CHANNEL = AudioFormat.CHANNEL_OUT_STEREO;
        private final static int AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;

        private AudioTrack mTrack;
        private volatile boolean mStopSignal;
        private int mBufferSizeInBytes;
        private int mLength;
        private byte[] mAudioBytes;


        private void setStopSignal(){
            mStopSignal = true;
        }

        private boolean getStopSignal(){
            return mStopSignal;
        }

        private void flush(byte[] bytes, int length){
            mLength = length;
            mAudioBytes = bytes;
        }

        @Override
        public void run() {
            Log.v(TAG, ":playing: running ...");
            preparePlaying();
            while (!mStopSignal) {
                if (mLength > 0) {
                    mTrack.write(mAudioBytes, mLength, AudioTrack.WRITE_BLOCKING);
                }
            }
            endPlaying();
            Log.v(TAG, ":playing: exit");
        }

        private void preparePlaying(){
            Log.v(TAG, "prepare playing");
            mStopSignal = false;

            mBufferSizeInBytes = AudioTrack.getMinBufferSize(
                    AUDIO_SAMPLE_RATE,
                    AUDIO_CHANNEL,
                    AUDIO_ENCODING);
            Log.v(TAG, "Min Buffer Size: " + mBufferSizeInBytes);
            try {
                mTrack = new AudioTrack(
                        AUDIO_STREAM,
                        AUDIO_SAMPLE_RATE,
                        AUDIO_CHANNEL,
                        AUDIO_ENCODING,
                        mBufferSizeInBytes,
                        AudioTrack.MODE_STREAM);
                mTrack.play();
            } catch (IllegalArgumentException|IllegalStateException e) {
                e.printStackTrace();
            }
        }

        private void endPlaying(){
            Log.v(TAG, "end playing");
            mStopSignal = true;
            mBufferSizeInBytes = -1;
            mLength = -1;
            mAudioBytes = null;
        }
    }
}
