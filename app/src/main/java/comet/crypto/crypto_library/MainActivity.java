package comet.crypto.crypto_library;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.JsonObject;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import comet.crypto.lib.CryptoLib;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        CryptoLib cryptoLib = CryptoLib.instance();
        TextView textView = findViewById(R.id.test);
        textView.setText(cryptoLib.runPrint());

        cryptoLib.run("2fc277274f7b4ec44c90ce2d9b4ed41e8f7766204433406d732e29c4d195c498");
        //cryptoLib.stop();
    }
}