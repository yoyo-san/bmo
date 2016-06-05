package bmo.samplewifi;


import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * This fragment handles chat related UI which includes a list view for messages
 * and a message entry field with send button.
 */
public class WiFiChatFragment extends Fragment {

    private View view;
    private VoiceManager voiceManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_chat, container, false);
        view.findViewById(R.id.button1).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    if (voiceManager != null) {
                        Log.d("Fragment", "Pressed PTT");
                        voiceManager.talk();
                    }
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (voiceManager != null) {
                        Log.d("Fragment", "Released PTT");
                        voiceManager.shaddap();
                    }
                }
                return true;
            }
        });
        return view;
    }

    public interface MessageTarget {
        public Handler getHandler();
    }

    public void setVoiceManager(VoiceManager obj) {
        voiceManager = obj;
    }
}


