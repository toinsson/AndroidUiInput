package toinsson.uiinput;

import android.app.Instrumentation;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.view.MotionEvent;


import org.zeromq.ZMQ;


class ZeroMQSub implements Runnable {
    private final Handler uiThreadHandler;

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

            int X = (int) (posy*1343);
            int Y = (int) (posx*2239);

            Log.d("#DEBUG", type + " " + posx + " " + posy);
            Integer motionType = 0;

            switch (type) {
                case "touch up":
                    Log.d("#DEBUG", "in touch up");
                    motionType = MotionEvent.ACTION_UP;
                    touchUp();
                    break;
                case "touch down":
                    Log.d("#DEBUG", "in touch down");
                    motionType = MotionEvent.ACTION_DOWN;
                    touchDown(X, Y);
                    break;
                case "touch move":
                    Log.d("#DEBUG", "in touch move");
                    motionType = MotionEvent.ACTION_MOVE;
                    touchMove(X, Y);

                    break;
                default:
                    Log.d("#DEBUG", "wrong format.");
                    break;
            }
        }

        socket.close();
        context.term();
    }
}
