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

        ExecutorService executorService = Executors.newSingleThreadExecutor();

        executorService.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    JsonObject json = cryptoLib.getNewTask();
                    cryptoLib.sendTaskResult("shreking around");
                } catch (Exception e) {
                    System.out.println("wtf");
                    // Handle the exception here, e.g. show an error dialog
                }
            }
        });
    }
}