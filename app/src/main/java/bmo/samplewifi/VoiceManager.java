package bmo.samplewifi;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.os.Handler;
import android.util.Log;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

public class VoiceManager implements Runnable {
    private Socket socket = null;
    private Handler handler;
    private InputStream iStream;
    private String TAG = "Voice Manager";
    public static final int START_TALK = -1;
    public static final int FREQUENCY = 44100;
    public static final int ENCODING = AudioFormat.ENCODING_PCM_16BIT;
    public static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_CONFIGURATION_MONO;
    int bufferSize;
    TalkManager talkManager;

    public VoiceManager(Socket socket, Handler handler) {
        this.socket = socket;
        this.handler = handler;
    }
    /*
    * This runs all the time when connected and listens if somebody wishes to speak
    * */

    @Override
    public void run() {
        try {
            bufferSize = AudioRecord.getMinBufferSize(FREQUENCY, CHANNEL_CONFIG, ENCODING);
            iStream = socket.getInputStream();
            handler.obtainMessage(MainActivity.MY_HANDLE, this)
                    .sendToTarget();
            byte[] flagBuffer = new byte[1];

            while(true) {
                iStream.read(flagBuffer);

                //Reading only one byte and if is START_TALK then we go to play mode
                if(flagBuffer[0] == START_TALK) {
                    Log.d(TAG, "Start listening");

                    //Disable push to talk button
                    handler.sendEmptyMessage(MainActivity.DISABLE_PTT);
                    byte[] buffer = new byte[bufferSize];
                    int read = 0;

                    AudioTrack audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,VoiceManager.FREQUENCY,
                            CHANNEL_CONFIG, VoiceManager.ENCODING, bufferSize, AudioTrack.MODE_STREAM);
                    audioTrack.play();

                    //Read everything that comes
                    while((read = iStream.read(buffer)) != 1) {
                        audioTrack.write(buffer, 0, read);
                    }
                    //Enable PTT button
                    handler.sendEmptyMessage(MainActivity.ENABLE_PTT);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if(iStream != null)
                    iStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void talk() {
        //Make voice recorder run in new thread so it doesn't block UI
        talkManager = new TalkManager(socket);
        new Thread(talkManager).start();
    }

    public void shaddap() {
        if(talkManager != null)
            talkManager.stopTalking();
        talkManager = null;
    }
}
