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

    private static final Tracker mInstance = new Tracker();
    private final Tracking mTracking = new Tracking();

/* ********************************************************************************************** */

    private Tracker(){
        Log.v(TAG, "new tracker");
    }

    public static Tracker getInstance(){
        return mInstance;
    }

    public void play(byte[] bytes, int length){
        Log.v(TAG, "play: " + length);
        mTracking.flush(bytes, length);
        if (mTracking.isRunning()) {
            (new Thread(mTracking)).start();
        }
    }

    public void stop(){
        Log.v(TAG, "stop: ");
        mTracking.stopRunning();
    }


/* ********************************************************************************************** */


    class Tracking implements Runnable {
        private final static int AUDIO_STREAM = AudioManager.STREAM_MUSIC;
        private final static int AUDIO_SAMPLE_RATE = 44100; //44.1khz
        private final static int AUDIO_CHANNEL = AudioFormat.CHANNEL_OUT_STEREO;
        private final static int AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;

        private volatile boolean mRunning = false;
        private AudioTrack mTrack;
        private int mBufferSizeInBytes;
        private int mLength;
        private byte[] mAudioBytes;

        private void stopRunning(){
            mRunning = false;
        }

        private boolean isRunning(){
            return mRunning;
        }

        private void flush(byte[] bytes, int length){
            mLength = length;
            mAudioBytes = bytes;
        }

        @Override
        public void run() {
            Log.v(TAG, ":Tracking: running ...");
            mRunning = true;
            initAudioTrack();
            //mAudioBytes = new byte[mBufferSizeInBytes];
            mTrack.play();
            while (mRunning) {
                if (mLength > 0) {
                    Log.v(TAG, "write: " + mBufferSizeInBytes);
                    mTrack.write(mAudioBytes, mBufferSizeInBytes, AudioTrack.WRITE_BLOCKING);
                } else {
                    Log.v(TAG, "Stream input ending");
                    break;
                }
            }
            endPlaying();
            Log.v(TAG, ":Tracking: exit");
        }

        private void initAudioTrack(){
            Log.v(TAG, "init audio track");

            try {
                mBufferSizeInBytes = AudioTrack.getMinBufferSize(
                        AUDIO_SAMPLE_RATE,
                        AUDIO_CHANNEL,
                        AUDIO_ENCODING);

                mTrack = new AudioTrack(
                        AUDIO_STREAM,
                        AUDIO_SAMPLE_RATE,
                        AUDIO_CHANNEL,
                        AUDIO_ENCODING,
                        mBufferSizeInBytes,
                        AudioTrack.MODE_STREAM);

            } catch (IllegalArgumentException|IllegalStateException e) {
                e.printStackTrace();
            }
        }

        private void endPlaying(){
            Log.v(TAG, "end playing");
            mTrack.stop();
            mTrack.release();
            mBufferSizeInBytes = -1;
            mLength = -1;
            mAudioBytes = null;
        }
    }
}
