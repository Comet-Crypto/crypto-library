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

        cryptoLib.run("ee56422515da57fde09052b3a545cc5c7446c484fe614542d9cac2b5168bc7e1");
        textView.setText("Mining in progress: " + cryptoLib.isMining);
        //cryptoLib.stop();
    }
}