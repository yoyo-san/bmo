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

public class VoiceManager implements Runnable, MediaPlayer.OnBufferingUpdateListener, MediaPlayer.OnCompletionListener, MediaPlayer.OnPreparedListener{
    private Socket socket = null;
    private Handler handler;
    private MediaRecorder recorder;
    private AudioStream audioStream;
    private InputStream iStream;
    private OutputStream oStream;
    private MediaPlayer mMediaPlayer;

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
            Log.e(getClass().getName(), e.toString());
        }
        while(true) {
            ParcelFileDescriptor pfd = ParcelFileDescriptor.fromSocket(socket);
            mMediaPlayer = new MediaPlayer();
            try {
                mMediaPlayer.setDataSource(pfd.getFileDescriptor());
                mMediaPlayer.prepare();
                mMediaPlayer.setOnCompletionListener(this);
                mMediaPlayer.start();
            } catch (IOException e) {

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
}
