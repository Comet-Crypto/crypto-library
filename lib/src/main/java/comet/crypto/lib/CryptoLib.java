package comet.crypto.lib;

import java.net.URISyntaxException;
import java.util.Map;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class CryptoLib {
    // Instance
    private static final CryptoLib _instance = new CryptoLib();
    public static CryptoLib instance() {
        return _instance;
    }
    private CryptoLib() {}
    private Emitter.Listener onNewTask = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            Map<String, Object> data = (Map<String, Object>) args[0];
        }
    };

    // Socket
    private Socket mSocket;
    {
        try {
            mSocket = IO.socket("http://10.0.0.6:4000");
        } catch (URISyntaxException e) {}
    }

    // Testing
    public String runPrint() {
        return "CryptoLib is running.";
    }

    // User Functions
    public void run(){
        mSocket.on("newTask", _instance.onNewTask);
        mSocket.connect();
    }

    public void stop() {
        mSocket.close();
    }
}