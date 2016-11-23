package toinsson.uiinput;

import android.app.usage.UsageEvents;
import android.graphics.Point;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
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

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }
    public native String stringFromJNI();


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

        String type = messageBundle.getString("EVENT_TYPE");
        Float posx = messageBundle.getFloat("POSITION_X");
        Float posy = messageBundle.getFloat("POSITION_Y");

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

        Log.d("#DEBUG", "onCreate: width" + width + " height:" + height);

        // Example of a call to a native method
        TextView tv = (TextView) findViewById(R.id.sample_text);
        tv.setText(stringFromJNI());

        // create the motionEvent subscriber
        new Thread(new ZeroMQSub(serverMessageHandler)).start();
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
        bubblesManager.addBubble(bubbleView, 60, 20);
    }

    private void initializeBubblesManager() {
        bubblesManager = new BubblesManager.Builder(this)
//                .setTrashLayout(R.layout.bubble_trash_layout)
                .setInitializationCallback(new OnInitializedCallback() {
                    @Override
                    public void onInitialized() {
                        addNewBubble();
                    }
                })
                .build();
        bubblesManager.initialize();
    }
}
