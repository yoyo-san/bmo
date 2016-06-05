package bmo.samplewifi;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.rtp.AudioStream;
import android.os.Handler;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Random;

public class VoiceManager implements Runnable {
    private Socket socket = null;
    private Handler handler;
    private AudioRecord recorder;
    private AudioStream audioStream;
    private InputStream iStream;
    private OutputStream oStream;
    private MediaPlayer mMediaPlayer;
    private boolean beQuiet = false;
    private String TAG = "Voice Manager";
    private boolean talking = false;
    public static final int FREQUENCY = 44100;
    public static final int ENCODING = AudioFormat.ENCODING_PCM_16BIT;
    public static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
    int bufferSize;

    public VoiceManager(Socket socket, Handler handler) {
        this.socket = socket;
        this.handler = handler;
    }

    @Override
    public void run() {
        try {
            bufferSize = AudioRecord.getMinBufferSize(FREQUENCY, CHANNEL_CONFIG, ENCODING);
            iStream = socket.getInputStream();
            DataInputStream in = new DataInputStream(new BufferedInputStream(iStream));

            byte[] buffer = new byte[bufferSize];
            handler.obtainMessage(MainActivity.MY_HANDLE, this)
                    .sendToTarget();
            int read = 0;
            AudioTrack audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,VoiceManager.FREQUENCY,
                    AudioFormat.CHANNEL_CONFIGURATION_MONO, VoiceManager.ENCODING, bufferSize, AudioTrack.MODE_STREAM);
            audioTrack.play();

            while((read = iStream.read(buffer)) != -1) {
                // Send the obtained bytes to the UI Activity
                //handler.obtainMessage(MainActivity.MESSAGE_READ, buffer).sendToTarget();
                audioTrack.write(buffer, 0, read);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            /*
            try {
                Log.d(TAG, "");
            } catch (IOException e) {
                e.printStackTrace();
            }*/
        }
    }

    public void talk() {
        new Runnable() {
            public void run() {
                Log.d(getClass().getName(), "Talking");
                DataOutputStream out = null;

                try {
                    out = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
                } catch (IOException e) {
                    Log.e(TAG, e.toString());
                }
                recorder = new AudioRecord(MediaRecorder.AudioSource.MIC, FREQUENCY, CHANNEL_CONFIG, ENCODING, bufferSize);
                byte[] buffer = new byte[bufferSize];
                int read;

                recorder.startRecording();
                talking = true;

                try {
                    while (talking) {
                        read = recorder.read(buffer, 0, bufferSize);
                        if (read != -1) {
                            Log.d(TAG, read + " bytes");
                            socket.getOutputStream().write(buffer, 0, read);
                        }
                    }
                    recorder.stop();
                    recorder.release();
                    out.close();
                } catch (IOException e) {
                    Log.e(TAG, e.toString());
                }
            }
        }.run();
    }

    public void shaddap() {
        talking = false;
    }
}
