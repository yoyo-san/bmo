package bmo.samplewifi;

import android.media.AudioFormat;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.rtp.AudioStream;
import android.os.Environment;
import android.os.Handler;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;

public class VoiceManager implements Runnable {
    public Socket socket = null;
    private Handler handler;
    private MediaRecorder mRecorder;
    private AudioStream audioStream;
    private InputStream iStream;
    private OutputStream oStream;
    private MediaPlayer mMediaPlayer;
    private boolean beQuiet = false;
    private String TAG = "Voice Manager";
    private boolean talking = false;
    public static final int FREQUENCY = 44100;
    public static final int ENCODING = AudioFormat.ENCODING_PCM_16BIT;
    public static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_CONFIGURATION_MONO;
    public static String mFileName;

    public VoiceManager(Socket socket, Handler handler) {
        this.socket = socket;
        this.handler = handler;
    }

    @Override
    public void run() {
            handler.obtainMessage(MainActivity.MY_HANDLE, this)
                    .sendToTarget();
            Log.d(TAG, "Listening");

        try {
            iStream = socket.getInputStream();
            oStream = socket.getOutputStream();

            byte[] buffer = new byte[1024];
            int bytes;

        while (true) {
            try {
                // Read from the InputStream
                bytes = iStream.read(buffer);
                if (bytes == -1) {
                    break;
                }

                // Send the obtained bytes to the UI Activity
                Log.d(TAG, "Rec:" + String.valueOf(buffer));
                handler.obtainMessage(MainActivity.MESSAGE_READ,
                        bytes, -1, buffer).sendToTarget();
            } catch (IOException e) {
                Log.e(TAG, "disconnected", e);
            }
        }
        } catch (IOException e) {
            Log.e(TAG, e.toString());
        }
    }

    public void sendLine(String s) throws IOException{
        PrintWriter out =
                new PrintWriter(socket.getOutputStream(), true);
        out.println(MainActivity.VOICE_START);
        out.close();
    }

    public void talk() throws IOException{
        Log.d(getClass().getName(), "Talking");
        sendLine(MainActivity.VOICE_START);
        ParcelFileDescriptor pfd = ParcelFileDescriptor.fromSocket(socket);
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_WB);
        mRecorder.setOutputFile(pfd.getFileDescriptor());
        mRecorder.prepare();

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            mRecorder.start();
        }
    }

    public void shaddap() {

        if(mRecorder != null) {
            try {
                mRecorder.release();
                mRecorder.reset();
                mRecorder.stop();
                sendLine(MainActivity.VOICE_END);
            } catch (Exception e) {
                Log.e(TAG, e.toString());
            }
        }
    }
}
