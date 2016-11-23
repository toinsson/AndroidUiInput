package toinsson.uiinput;

import android.app.usage.UsageEvents;
import android.graphics.Point;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.widget.TextView;


import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private TextView textView;

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;


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

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();

}
