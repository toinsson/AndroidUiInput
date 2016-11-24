package toinsson.uiinput;


import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import org.zeromq.ZMQ;


class ZeroMQSub implements Runnable {
    private final Handler uiThreadHandler;
    private boolean touched = false;

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

    ZeroMQSub(Handler uiThreadHandler) {
        this.uiThreadHandler = uiThreadHandler;
    }

    @Override
    public void run() {

        Log.d("#jnI addition", Integer.toString(printInt(4, 8)));
        Log.d("#jnI DEBUG", stringFromJNI());

        ZMQ.Context context = ZMQ.context(1);
        ZMQ.Socket socket = context.socket(ZMQ.SUB);

        socket.connect("tcp://192.168.42.1:5556");
        socket.subscribe("".getBytes());

        Log.d("#DEBUG", "before while loop");

        initTouchInterface();

        while(!Thread.currentThread().isInterrupted()) {
            Log.d("cew", "cewio");
            String type = socket.recvStr();
            String data = socket.recvStr();

            String[] separated = data.split(" ")[1].split(",");
            float posx = Float.parseFloat(separated[0]);
            float posy = Float.parseFloat(separated[1]);

            // for the raw display sensor
            int X_display = (int) (posy*1343);
            int Y_display = (int) (posx*2239);
            // for the current window
            int X_window = (int) (posx * 1920);
            int Y_window = (int) (1104 * (1 - posy));

            Log.d("#DEBUG", type + " " + posx + " " + posy);
            Integer motionType = 0;

            switch (type) {
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

            // filter out the hover state when in touch
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
