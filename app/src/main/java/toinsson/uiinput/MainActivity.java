package toinsson.uiinput;

import android.app.usage.UsageEvents;
import android.graphics.Point;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.txusballesteros.bubbles.BubbleLayout;
import com.txusballesteros.bubbles.BubblesManager;
import com.txusballesteros.bubbles.OnInitializedCallback;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private TextView textView;
    private BubblesManager bubblesManager;

    private final String TAG = "MainActivity";

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }
    public native String stringFromJNI();
//    public native String initTouchInterface();


    private List<String> errorLog = new ArrayList<String>();
    private static final int MAX_ERROR_LINES = 7;

    public void addToLog(String str) {
        if (str.length() > 0) {errorLog.add( str) ;}
        // remove the first line if log is too large
        if (errorLog.size() >= MAX_ERROR_LINES) {errorLog.remove(0);}
        updateLog();
    }

    private void updateLog() {
        String log = "";
        for (String str : errorLog) {log += str + "\n";}
        textView.setText(log);
    }

    private void subMessageReceived(Bundle messageBundle) {

        int type = messageBundle.getInt("EVENT_TYPE");
        int posx = messageBundle.getInt("POSITION_X");
        int posy = messageBundle.getInt("POSITION_Y");

        Log.d(TAG, type + " " + posx + " " + posy);

        if (type == MotionEvent.ACTION_HOVER_ENTER) {
            addNewBubble();
            // change the layout
            bubblesManager.paintBubble();
        }

        if (type == MotionEvent.ACTION_HOVER_EXIT) {
            bubblesManager.removeBubble();
        }

        if (type == MotionEvent.ACTION_MOVE ||
                type == MotionEvent.ACTION_HOVER_MOVE) {
            bubblesManager.moveBubble(posx, posy);
        }
    }

    private final MessageListenerHandler serverMessageHandler = new MessageListenerHandler(
        new IMessageListener() {
            @Override
            public void messageReceived(Bundle messageBundle) {subMessageReceived(messageBundle);}
        });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeBubblesManager();


        findViewById(R.id.add).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addNewBubble();
            }
        });

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = size.y;
        int rotation = display.getRotation();

//        if (width > height)
        Log.d("#DEBUG", "onCreate: w h r " + width +" "+ height +" "+ rotation);

        Shell s = new Shell();
        Log.d("#DEBUG", "is root :"+s.isSuAvailable());

//        initTouchInterface();

        // Example of a call to a native method
        TextView tv = (TextView) findViewById(R.id.sample_text);
        tv.setText(stringFromJNI());

        // create the motionEvent subscriber
        new Thread(new ZeroMQSub(serverMessageHandler, rotation, width, height)).start();
    }

    private void addNewBubble() {
        BubbleLayout bubbleView = (BubbleLayout)LayoutInflater.from(MainActivity.this).inflate(R.layout.bubble_layout, null);
        bubbleView.setOnBubbleRemoveListener(new BubbleLayout.OnBubbleRemoveListener() {
            @Override
            public void onBubbleRemoved(BubbleLayout bubble) { }
        });
        bubbleView.setOnBubbleClickListener(new BubbleLayout.OnBubbleClickListener() {
            @Override
            public void onBubbleClick(BubbleLayout bubble) {
                Toast.makeText(getApplicationContext(), "Clicked !",
                        Toast.LENGTH_SHORT).show();
            }
        });
        bubbleView.setShouldStickToWall(false);
        bubblesManager.addBubble(bubbleView, 100, 400);
    }

    private void initializeBubblesManager() {
        bubblesManager = new BubblesManager.Builder(this)
//                .setTrashLayout(R.layout.bubble_trash_layout)
//                .setInitializationCallback(new OnInitializedCallback() {
//                    @Override
//                    public void onInitialized() {
//                        addNewBubble();
//                    }
//                })
                .build();
        bubblesManager.initialize();
    }
}
