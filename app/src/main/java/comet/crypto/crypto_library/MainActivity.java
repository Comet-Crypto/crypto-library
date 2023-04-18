package comet.crypto.crypto_library;

import static comet.crypto.lib.CryptoLib.*;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

import comet.crypto.lib.CryptoLib;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        CryptoLib cryptoLib = CryptoLib.instance();
        TextView textView = findViewById(R.id.test);
        textView.setText(cryptoLib.runPrint());

        try {
            cryptoLib.connect();
        } catch (Exception e) {
            // Handle the exception here, e.g. show an error dialog
        }
    }
}