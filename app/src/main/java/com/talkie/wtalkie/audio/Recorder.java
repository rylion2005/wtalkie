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
    private static Recorder mInstance;

    private final RecordRunnable mRecordRunnable = new RecordRunnable();
    private final Thread mRecordThread = new Thread(mRecordRunnable);
    private Callback mCallback;


/* ********************************************************************************************** */


    private Recorder(){
        Log.v(TAG, "new recorder");
    }

    // Single instance
    public static Recorder newInstance(){
        if (mInstance == null){
            mInstance = new Recorder();
        }
        return mInstance;
    }

    public void register(Callback cb){
        mCallback = cb;
    }

    public void start(){
        if (!mRecordThread.isAlive()) {
            mRecordThread.start();
        }
    }

    public void stop(){
        mRecordRunnable.setStopSignal(true);
    }


/* ********************************************************************************************** */


    class RecordRunnable implements Runnable {

        private final static int AUDIO_INPUT = MediaRecorder.AudioSource.MIC;
        private final static int AUDIO_SAMPLE_RATE = 44100; //44.1khz
        private final static int AUDIO_CHANNEL = AudioFormat.CHANNEL_IN_STEREO;
        private final static int AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;

        private AudioRecord mRecord;

        private volatile boolean mStopSignal;
        private int mBufferSizeInBytes;

        private RecordRunnable(){ }


        private void setStopSignal(boolean stop){
            mStopSignal = stop;
        }

        @Override
        public void run() {
            Log.v(TAG, ":recording: running ......");
            prepareRecording();
            while (!mStopSignal) {
                byte[] buf = new byte[mBufferSizeInBytes];
                int count = mRecord.read(buf, 0, mBufferSizeInBytes);
                dispatch(buf, count);
            }
            endRecording();
            Log.v(TAG, ":recording: exit ...");
        }

        private void prepareRecording(){
            Log.v(TAG, "prepare recording");
            mStopSignal = false;

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

                mRecord.startRecording();
            } catch (IllegalStateException|NullPointerException e) {
                Log.v(TAG, "prepare exception");
                e.printStackTrace();
            }
        }

        private void endRecording(){
            Log.v(TAG, "end recording");

            mRecord.stop();
            mRecord.release();

            mStopSignal = true;
            mBufferSizeInBytes = -1;
            mRecord = null;
            mStopSignal = true;
        }

        private void dispatch(byte[] bytes, int length){
            Log.v(TAG, "dispatch: " + length);
            mCallback.onAudioStream(bytes, length);
        }
    }


/* ********************************************************************************************** */


    public interface Callback {
        void onAudioStream(byte[] bytes, int length);
    }
}
