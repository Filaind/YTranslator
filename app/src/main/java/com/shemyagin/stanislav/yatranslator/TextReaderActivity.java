package com.shemyagin.stanislav.yatranslator;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

public class TextReaderActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        Intent intent = getIntent();
        if(intent!=null && intent.hasExtra("text"))
        {
            setContentView(R.layout.activity_text_reader);
            TextView text = (TextView) findViewById(R.id.translateReader);
            text.setText(intent.getStringExtra("text"));
        }
        else
            finish();
    }
}
