package bmo.samplewifi;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;

public class TalkManager implements Runnable {

    private Socket socket;
    private AudioRecord recorder;
    public static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
    private boolean talking = false;
    private String TAG = "TalkManger";

    public TalkManager(Socket socket) {
        this.socket = socket;
    }

    public void stopTalking() {
        Log.d(TAG, "TalkManager stop()");
        talking = false;
    }

    public void run() {
        Log.d(TAG, "Talking");
        OutputStream out = null;
        int bufferSize = AudioRecord.getMinBufferSize(VoiceManager.FREQUENCY, CHANNEL_CONFIG, VoiceManager.ENCODING);
        recorder = new AudioRecord(MediaRecorder.AudioSource.MIC, VoiceManager.FREQUENCY, CHANNEL_CONFIG, VoiceManager.ENCODING, bufferSize);
        byte[] buffer = new byte[bufferSize];
        int read;

        //Changes when user stop holding button
        talking = true;

        try {
            out  = socket.getOutputStream();
            out.write(VoiceManager.START_TALK);
            recorder.startRecording();

            while (talking) {
                //Read from mic
                read = recorder.read(buffer, 0, bufferSize);
                out.write(buffer, 0, read);
            }
            Log.d(TAG, "Done talking");

            //Clean up
            recorder.stop();
            recorder.release();
            //End flag
            out.write(0);
        } catch (IOException e) {
            Log.e(TAG, e.toString());
        }
    }
}
