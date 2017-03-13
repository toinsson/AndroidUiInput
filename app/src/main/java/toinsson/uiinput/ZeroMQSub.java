package toinsson.uiinput;


import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import com.txusballesteros.bubbles.BubblesManager;

import org.zeromq.ZMQ;


class ZeroMQSub implements Runnable {
    private final Handler uiThreadHandler;

    // display hard constant - Nexus 7 orientation 0
    private int display_max_width = 1343, display_max_height = 2239;
    // max values in window space
    private int window_max_x = 0, window_max_y = 0;

    // scaling in y for display
    private float d_scaling_y = 1;
    // scaling in y for window
    private float w_scaling_y = 1;

    // offset in y to account for Android taskbar
    private float offset_y = 0.0f;

    // rotation
    private int rotation = 0;
    // parsed field from zmq message
    private float posx, posy;
    private String state;
    // display coordinates
    private int X_display, Y_display;

    private BubblesManager bubblesManager;
    private JsonParser parser = new JsonParser();

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    public native String stringFromJNI();
    public native String initTouchInterface();
    public native String writeEvent();
    public native int printInt(int A, int B);

    public native String touchDown(int A, int B);
    public native String touchMove(int A, int B);
    public native String touchUp();

    ZeroMQSub(Handler uiThreadHandler,  BubblesManager bubblesManager, int rotation, int width, int height) {
        this.uiThreadHandler = uiThreadHandler;
        this.window_max_x = width;
        this.window_max_y = height;
        this.rotation = rotation;

        this.bubblesManager = bubblesManager;

        // set the scaling
        // 50 accounts for the undrawable area  at the top
        switch (rotation) {
            case 0:
                d_scaling_y = 0.3125f; // 600/1920
                w_scaling_y = 600f / (1824f - 50);
                offset_y = 96f / 1920f * 2239f;
                break;
            case 1:
                d_scaling_y = 0.4333f; // 520/1200
                w_scaling_y = 520f / (1104f - 50);
                offset_y = 96f / 1200f * 1343f;
                break;
            case 2:
                d_scaling_y = 0.3125f; // 600/1920
                w_scaling_y = 600f / (1824f - 50);
                offset_y = 96f / 1920f * 2239f;
                break;
            case 3:  // test case on full screen
                d_scaling_y = 1.0f; // 520/1200
                w_scaling_y = 1.0f;
                offset_y =  0.0f; // 96f / 1200f * 1343f;
                break;
        }
        // scaling_y = 1.0f;
    }

    private void parse_message(String msg){
        String msg_clean = msg.replaceAll("\\\\", "");
        msg_clean = msg_clean.substring(1, msg_clean.length()-1);

//        Log.d("UiInput", "data received : "+msg_clean);

        JsonObject data_json = this.parser.parse(msg_clean).getAsJsonObject();

        JsonArray state_json = data_json.getAsJsonArray("state");
        String state_0 = state_json.get(0).toString().replace("\"", "");
        String state_1 = state_json.get(1).toString().replace("\"", "");
        this.state = state_0 + " " + state_1;

        JsonArray coord = data_json.getAsJsonArray("coord");
        this.posx = Float.parseFloat(coord.get(0).toString());
        this.posy = Float.parseFloat(coord.get(1).toString());
    }

    private void compute_display_xy(){

        switch (rotation) {
            case 0:
                X_display = (int) (posx               * display_max_width );
                Y_display = (int) ((1-posy*d_scaling_y) * display_max_height - offset_y);
                break;
            case 1:
                X_display = (int) (posy*d_scaling_y     * display_max_width + offset_y);
                Y_display = (int) (posx               * display_max_height);
                break;
            case 2:
                X_display = (int) ((1-posx)           * display_max_width );
                Y_display = (int) (posy*d_scaling_y     * display_max_height + offset_y);
                break;
            case 3:
                X_display = (int) ((1-posy*d_scaling_y) * display_max_width - offset_y);
                Y_display = (int) ((1-posx)           * display_max_height);
                break;
        }
    }

    @Override
    public void run() {

        ZMQ.Context context = ZMQ.context(1);
        ZMQ.Socket socket = context.socket(ZMQ.SUB);
        socket.connect("tcp://192.168.42.1:5556");
        socket.subscribe("".getBytes());  // all topic
        Log.d("#DEBUG", "before while loop");
        initTouchInterface();
        String data_string;

        boolean delayed_touch_down = false;

        while(!Thread.currentThread().isInterrupted()) {

            data_string = socket.recvStr();
            parse_message(data_string);
            Log.d("UiInput", "decoded : "+state+" "+posx+" "+posy);

            if (delayed_touch_down) {
                delayed_touch_down = false;
                touchDown(X_display, Y_display);
            }


            // mapping for coordinates
            // zmq  window display (rotates)
            // y    --x    0--x
            // |__x |y     |y
            compute_display_xy();
            int X_window = (int) (posx * window_max_x);
            int Y_window = (int) ((1 - posy*w_scaling_y) * window_max_y - 50);

            Integer motionType = 0;

            switch (state) {
                case "hover enter":
                    motionType = MotionEvent.ACTION_HOVER_ENTER;
                    break;
                case "hover move":
                    motionType = MotionEvent.ACTION_HOVER_MOVE;
                    break;
                case "hover exit":
                    motionType = MotionEvent.ACTION_HOVER_EXIT;
                    break;

                case "touch down":
                    motionType = MotionEvent.ACTION_DOWN;
                    bubblesManager.removeBubble();
                    delayed_touch_down = true;
                    // to let 1 frame for the bubble to be removed
                    // touchDown(X_display, Y_display);
                    break;
                case "touch move":
                    motionType = MotionEvent.ACTION_MOVE;
                    touchMove(X_display, Y_display);
                    break;
                case "touch up":
                    motionType = MotionEvent.ACTION_UP;
                    touchUp();
                    break;

                default:
                    Log.d("#DEBUG", "wrong format.");
                    break;
            }

            Message m = uiThreadHandler.obtainMessage();
            Bundle b = new Bundle();
            b.putInt("POSITION_X", X_window);
            b.putInt("POSITION_Y", Y_window);
            b.putInt("EVENT_TYPE", motionType);
            m.setData(b);
            uiThreadHandler.sendMessage(m);

//            switch (state) {
//                case "touch down":
////                    Log.d("#DEBUG", "in touch down");
//                    touched = true;
//                    touchDown(X_display, Y_display);
//                    break;
//                case "touch move":
////                    Log.d("#DEBUG", "in touch move");
//                    touchMove(X_display, Y_display);
//                    break;
//                case "touch up":
////                    Log.d("#DEBUG", "in touch up");
//                    touched = false;
//                    touchUp();
//                    break;
//                default:
////                    Log.d("#DEBUG", "wrong format.");
//                    break;
//            }
        }
        socket.close();
        context.term();
    }
}
