package comet.crypto.lib;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

import java.net.URISyntaxException;
import java.util.Map;

public class CryptoLib {

    // Instance
    private static final CryptoLib _instance = new CryptoLib();
    public static CryptoLib instance() {
        return _instance;
    }

    // Socket
    private Socket mSocket;

    // Listeners
    private Emitter.Listener onNewTask = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            //Map<String, Object> data = (Map<String, Object>) args[0];
            // Handle the received data...
        }
    };
    private Emitter.Listener taskFinished = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            String message = "Task is finished";
            mSocket.emit("taskFinished", message);
        }
    };

    // Constructor
    private CryptoLib() {}

    // Testing
    public String runPrint() {
        return "CryptoLib is running.";
    }

    // User Functions
    public void connect() throws URISyntaxException {
        mSocket = IO.socket("http://192.168.56.1:4000");
        mSocket.on("newTask", onNewTask);
        mSocket.on("connect", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                mSocket.emit("taskFinished", "We Live In Tokyo, fast and furiousssss");

            }
        });
        mSocket.connect();
        mSocket.on("taskFinished", taskFinished);
    }

    public void disconnect() {
        if (mSocket != null) {
            mSocket.disconnect();
            mSocket.close();
            mSocket = null;
        }
    }

}
