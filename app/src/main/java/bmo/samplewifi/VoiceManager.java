package bmo.samplewifi;

import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.rtp.AudioStream;
import android.os.Handler;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
        } catch (IOException e) {
            Log.e(TAG, e.toString());
        }
        byte[] buffer = new byte[1024];
        int bytes;

        while (true) {
            try {
                // Read from the InputStream
                bytes = iStream.read(buffer);
                if (bytes == -1) {
                    break;
                }
            } catch (IOException e) {
                Log.e(TAG, e.toString());
            }

            // Somebody wish to speak
            if (!beQuiet && String.valueOf(buffer).equals(MainActivity.VOICE_START)) {
                beQuiet = true;
                handler.sendEmptyMessage(MainActivity.DISABLE_PTT);
                ParcelFileDescriptor pfd = ParcelFileDescriptor.fromSocket(socket);
                mMediaPlayer = new MediaPlayer();

                try {
                    mMediaPlayer.setDataSource(pfd.getFileDescriptor());
                    mMediaPlayer.prepare();
                    mMediaPlayer.setOnCompletionListener(this);
                    mMediaPlayer.start();
                } catch (IOException e) {
                    Log.e(TAG, e.toString());
                }
            }
            //But not anymore
            else if (beQuiet && String.valueOf(buffer).equals(MainActivity.VOICE_END)) {
                beQuiet = false;
                handler.sendEmptyMessage(MainActivity.ENABLE_PTT);
                if(mMediaPlayer != null)
                    mMediaPlayer.stop();
            }
        }
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mediaPlayer, int i) {
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {

    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
    }

    public void talk() {
        ParcelFileDescriptor pfd = ParcelFileDescriptor.fromSocket(socket);
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        recorder.setOutputFile(pfd.getFileDescriptor());
        try {
            recorder.prepare();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        recorder.start();
    }

    public void shaddap() {
        if(recorder != null) {
            recorder.stop();
            recorder = null;
        }
    }
}
