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


import org.zeromq.ZMQ;


class ZeroMQSub implements Runnable {
    private final Handler uiThreadHandler;
    private boolean touched = false;

    // display hard constant - Nexus 7 orientation 0
    private int display_max_width = 1343, display_max_height = 2239;
    // max values in display space
//    private int display_max_x = 0, display_max_y = 0;
    // max values in window space
    private int window_max_x = 0, window_max_y = 0;
    // scaling in y
    private float scaling_y = 1;
    // rotation
    private int rotation = 0;
    // parsed field from zmq message
    private float posx, posy;
    private String state;
    // display coordinates
    private int X_display, Y_display;


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

    ZeroMQSub(Handler uiThreadHandler,  int rotation, int width, int height) {
        this.uiThreadHandler = uiThreadHandler;
        this.window_max_x = width;
        this.window_max_y = height;
        this.rotation = rotation;

        // set the scaling
        switch (rotation) {
            case 0:
                scaling_y = 0.40f;
                break;
            case 1:
                scaling_y = 0.50f;
                break;
            case 2:
                scaling_y = 0.40f;
                break;
            case 3:
                scaling_y = 0.50f;
                break;
        }
    }

    private void parse_message(String msg){
        String msg_clean = msg.replaceAll("\\\\", "");
        msg_clean = msg_clean.substring(1, msg_clean.length()-1);

        Log.d("UiInput", "data received : "+msg_clean);

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
                Y_display = (int) ((1-posy*scaling_y) * display_max_height);
                break;
            case 1:
                X_display = (int) (posy*scaling_y     * display_max_width );
                Y_display = (int) (posx               * display_max_height);
                break;
            case 2:
                X_display = (int) ((1-posx)           * display_max_width );
                Y_display = (int) (posy*scaling_y     * display_max_height);
                break;
            case 3:
                X_display = (int) ((1-posy*scaling_y) * display_max_width );
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

        while(!Thread.currentThread().isInterrupted()) {

            data_string = socket.recvStr();
            parse_message(data_string);
            Log.d("UiInput", "decoded : "+state+" "+posx+" "+posy);

            // mapping for coordinates
            // zmq  window display (rotates)
            // y    --x    0--x
            // |__x |y     |y
            compute_display_xy();
            int X_window = (int) (posx * window_max_x);
            int Y_window = (int) ((1 - posy*scaling_y) * window_max_y);

            Integer motionType = 0;

            switch (state) {
                case "hover enter":
                    Log.d("#DEBUG", "in touch down");
                    motionType = MotionEvent.ACTION_HOVER_ENTER;
                    break;
                case "hover move":
                    Log.d("#DEBUG", "in touch down");
                    motionType = MotionEvent.ACTION_HOVER_MOVE;
                    break;
                case "hover exit":
                    Log.d("#DEBUG", "in touch down");
                    motionType = MotionEvent.ACTION_HOVER_EXIT;
                    break;

                case "touch down":
                    Log.d("#DEBUG", "in touch down");
                    touched = true;
                    motionType = MotionEvent.ACTION_DOWN;
                    touchDown(X_display, Y_display);
                    break;
                case "touch move":
                    Log.d("#DEBUG", "in touch move");
                    motionType = MotionEvent.ACTION_MOVE;
                    touchMove(X_display, Y_display);
                    break;
                case "touch up":
                    Log.d("#DEBUG", "in touch up");
                    touched = false;
                    motionType = MotionEvent.ACTION_UP;
                    touchUp();
                    break;

                default:
                    Log.d("#DEBUG", "wrong format.");
                    break;
            }

            // filter out the hover state when in touch - is this needed now??
            if (!(motionType == MotionEvent.ACTION_HOVER_MOVE && touched)) {
                // prepare the message back to UI
                Message m = uiThreadHandler.obtainMessage();
                Bundle b = new Bundle();
                b.putInt("EVENT_TYPE", motionType);
                b.putInt("POSITION_X", X_window);
                b.putInt("POSITION_Y", Y_window);
                m.setData(b);
                uiThreadHandler.sendMessage(m);
            }
        }

        socket.close();
        context.term();
    }
}
