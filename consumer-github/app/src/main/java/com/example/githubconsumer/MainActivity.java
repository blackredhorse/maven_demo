package com.example.githubconsumer;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import com.example.mylibrary.Calculator;
import com.example.mylibrary.Greeter;

/** Compiles against the AAR fetched from GitHub Packages. */
public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TextView view = new TextView(this);
        view.setText(
                Greeter.greet("github packages")
                        + "\n1 + 2 = " + Calculator.add(1, 2)
                        + "\n" + getString(com.example.mylibrary.R.string.mylibrary_greeting));
        setContentView(view);
    }
}
