package com.project3.SmartPot;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {
    Button btnShow, btnControl;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnShow = (Button) findViewById(R.id.btnShow);
        btnShow.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //Switch show activity
                startActivity(new Intent(MainActivity.this,ShowActivity.class));
            }
        });
        btnControl = (Button) findViewById(R.id.btnControl);
        btnControl.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //Switch control activity
                startActivity(new Intent(MainActivity.this,ControlActivity.class));
            }
        });
    }

}
