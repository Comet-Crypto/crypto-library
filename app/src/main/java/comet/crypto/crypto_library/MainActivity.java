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
        TextView textView = findViewById(R.id.test);
        textView.setText(new CryptoLib().SimplePrint());
    }
}