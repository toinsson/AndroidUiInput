package toinsson.uiinput;

import android.os.Handler;
import android.util.Log;

import org.zeromq.ZMQ;
 
public class ZeroMQPub implements Runnable {
    private final Handler uiThreadHandler;
    public static final String MESSAGE_PAYLOAD_KEY = "jeromq-service-payload";

    public ZeroMQPub(Handler uiThreadHandler) {
        this.uiThreadHandler = uiThreadHandler;
    }

    @Override
    public void run() {
        ZMQ.Context context = ZMQ.context(1);
        ZMQ.Socket publisher = context.socket(ZMQ.PUB);

        publisher.bind("tcp://127.0.0.1:5556");

        Log.d("#PUB", "before sending");

        while (!Thread.currentThread ().isInterrupted ()) {
            // Write two messages, each with an envelope and content
            publisher.sendMore ("A");
            publisher.send ("We don't want to see this");
            publisher.sendMore ("B");
            publisher.send("We would like to see this");
            publisher.sendMore ("B");
            publisher.send("We would like to see this");
            publisher.sendMore ("B");
            publisher.send("We would like to see this");
            publisher.sendMore ("B");
            publisher.send("We would like to see this");
            publisher.sendMore ("B");
            publisher.send("We would like to see this");
            publisher.sendMore ("B");
            publisher.send("We would like to see this");
            publisher.sendMore ("B");
            publisher.send("We would like to see this");
            Log.d("#PUB", "after sending");
        }

        publisher.close ();
        context.term ();
    }
}
