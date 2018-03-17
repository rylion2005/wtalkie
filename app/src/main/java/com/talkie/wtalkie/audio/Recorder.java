package com.talkie.wtalkie.audio;


import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;


/*
** REVISED HISTORY
**   yl7 | 18-2-16: Created
**     
*/
public class Recorder{
    private static final String TAG = "Recorder";

    private static final Recorder mInstance = new Recorder();

    private final Recording mRecorder = new Recording();
    private AudioCallback mCallback;

/* ********************************************************************************************** */

    private Recorder(){
        Log.v(TAG, "new recorder");
    }

    public static Recorder getInstance(){
        return mInstance;
    }

    public void register(AudioCallback cb){
        mCallback = cb;
    }

    public void start(){
        Log.v(TAG, "start: ");
        if (!mRecorder.isRunning()) {
            (new Thread(mRecorder)).start();
        }
    }

    public void stop(){
        Log.v(TAG, "stop: ");
        mRecorder.stopRunning();
        if (mCallback != null) {
            mCallback.onStreamEnding();
        }
    }

/* ********************************************************************************************** */

    class Recording implements Runnable {
        private final static int AUDIO_INPUT = MediaRecorder.AudioSource.MIC;
        private final static int AUDIO_SAMPLE_RATE = 44100; //44.1khz
        private final static int AUDIO_CHANNEL = AudioFormat.CHANNEL_IN_STEREO;
        private final static int AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;

        private volatile boolean mRunning;
        private AudioRecord mRecord;
        private int mBufferSizeInBytes;

        private Recording(){ }

        private boolean isRunning(){
            return mRunning;
        }

        private void stopRunning(){
            mRunning = false;
        }

        @Override
        public void run() {
            Log.v(TAG, ":recording: running ......");
            mRunning = true;
            initAudioRecord();
            byte[] buffer = new byte[mBufferSizeInBytes];
            mRecord.startRecording();
            while (mRunning) {
                int count = mRecord.read(buffer, 0, mBufferSizeInBytes);
                if (mCallback != null) {
                    mCallback.onAudioStreamOutput(buffer, count);
                }
            }
            endRecording();
            mRunning = false;
            Log.v(TAG, ":recording: exit ...");
        }

        private void initAudioRecord(){
            Log.v(TAG, "init audio record");

            try {
                mBufferSizeInBytes = AudioRecord.getMinBufferSize(
                        AUDIO_SAMPLE_RATE,
                        AUDIO_CHANNEL,
                        AUDIO_ENCODING);

                mRecord = new AudioRecord(
                        AUDIO_INPUT,
                        AUDIO_SAMPLE_RATE,
                        AUDIO_CHANNEL,
                        AUDIO_ENCODING,
                        mBufferSizeInBytes);
            } catch (IllegalStateException|NullPointerException e) {
                Log.v(TAG, "prepare exception");
                e.printStackTrace();
            }
        }

        private void endRecording(){
            Log.v(TAG, "end recording");
            mRecord.stop();
            mRecord.release();
        }
    }

/* ********************************************************************************************** */

    public interface AudioCallback {
        void onAudioStreamOutput(byte[] bytes, int length);
        void onStreamEnding();
    }
}
