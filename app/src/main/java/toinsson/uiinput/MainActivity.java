package toinsson.uiinput;

import android.app.usage.UsageEvents;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;

public class MainActivity extends AppCompatActivity {

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Example of a call to a native method
        TextView tv = (TextView) findViewById(R.id.sample_text);
        tv.setText(stringFromJNI());

//        simulateMouse();

//        Shell.runCommand("sudo chmod 666 " + "/dev/uinput");

        testFunction();

        Monitor mMonitor = Monitor.sharedInstance();
        randomCursorMove(mMonitor);

//        mMonitor.inject(5, Events.InputDevice.EV_REL, Events.InputDevice.REL_X, 10);

        Log.d("#DEBUG", "inject events");

        mMonitor.createVirtualTouchDrive(0);
        mMonitor.injectToVirtual(Events.InputDevice.EV_KEY, Events.InputDevice.BTN_TOUCH, 10);
        mMonitor.injectToVirtual(Events.InputDevice.EV_REL, Events.InputDevice.REL_X, 10);
        mMonitor.injectToVirtual(Events.InputDevice.EV_REL, Events.InputDevice.REL_X, 10);
        mMonitor.injectToVirtual(Events.InputDevice.EV_REL, Events.InputDevice.REL_X, 10);
        mMonitor.injectToVirtual(Events.InputDevice.EV_REL, Events.InputDevice.REL_X, 10);
        mMonitor.injectToVirtual(Events.InputDevice.EV_REL, Events.InputDevice.REL_X, 10);
        mMonitor.injectToVirtual(Events.InputDevice.EV_KEY, Events.InputDevice.BTN_TOUCH, 10);


        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();

    public native String testFunction();


    protected void randomCursorMove(Monitor mMonitor) {
        for (int i = 0; i < 20; i++) {
            mMonitor.inject(5, Events.InputDevice.EV_REL, Events.InputDevice.REL_X, 10);
        }

        mMonitor.inject(5, Events.InputDevice.EV_KEY, Events.InputDevice.BTN_TOUCH, 1);
        mMonitor.inject(5, Events.InputDevice.EV_SYN, 0, 0);
    }

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName("Main Page") // TODO: Define a title for the content shown.
                // TODO: Make sure this auto-generated URL is correct.
                .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
                .build();
        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        AppIndex.AppIndexApi.start(client, getIndexApiAction());
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        AppIndex.AppIndexApi.end(client, getIndexApiAction());
        client.disconnect();
    }
}
