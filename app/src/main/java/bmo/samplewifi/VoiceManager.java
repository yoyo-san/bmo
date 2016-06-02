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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;

public class VoiceManager implements Runnable, MediaPlayer.OnBufferingUpdateListener, MediaPlayer.OnCompletionListener, MediaPlayer.OnPreparedListener {
    private Socket socket = null;
    private Handler handler;
    private MediaRecorder recorder;
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

    public VoiceManager(Socket socket, Handler handler) {
        this.socket = socket;
        this.handler = handler;
    }

    @Override
    public void run() {
        try {
            iStream = socket.getInputStream();
            oStream = socket.getOutputStream();
            byte[] buffer = new byte[1024];
            int bytes;
            handler.obtainMessage(MainActivity.MY_HANDLE, this)
                    .sendToTarget();

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
            e.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mediaPlayer, int i) {
        Log.d(TAG, "Buffer update");
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        Log.d(TAG, "COMPLETE");
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        Log.d(TAG, "Prepared update");
    }

    public void talk() {
        Log.d(getClass().getName(), "Talking");
        int bufferSize = AudioRecord.getMinBufferSize(FREQUENCY, CHANNEL_CONFIG, ENCODING);
        AudioRecord audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,FREQUENCY,CHANNEL_CONFIG,ENCODING, bufferSize);
        byte[] audioData = new byte[bufferSize];
        audioRecord.startRecording();
        talking = true;
        int read = 0;
        Log.d(TAG, "talking: " + talking);
        while (talking) {
            read = audioRecord.read(audioData,0,bufferSize);
            if(AudioRecord.ERROR_INVALID_OPERATION != read){
                try {
                    oStream.write(audioData);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void shaddap() {
        talking = false;
        if(recorder != null) {
            try {
                talking = false;
            } catch (Exception e) {
                Log.e(TAG, e.toString());
            }
            recorder = null;
        }
    }
}
