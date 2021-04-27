package com.project3.SmartPot;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ControlActivity extends AppCompatActivity {
    private Switch swLamp, swPump, swAuto;

    private String lamp, pump, auto;
    private String status;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control);
        swLamp = findViewById(R.id.switchLamp);
        swPump = findViewById(R.id.switchPump);
        swAuto = findViewById(R.id.switchAuto);
        status = "000";
        setSwitch();

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference myRef = database.getReference("device");

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                status = dataSnapshot.getValue(String.class);
                setSwitch();
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(ControlActivity.this,
                        "Error", Toast.LENGTH_LONG).show();
            }
        });
        swLamp.setOnCheckedChangeListener(
                new CompoundButton.OnCheckedChangeListener() {
                    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton,
                                                 boolean b) {

                        lamp = getBit(swLamp.isChecked());
                        myRef.setValue(pump + lamp + auto);
                        try {
                            Thread.sleep(200);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                    }
                });

        swPump.setOnCheckedChangeListener(
                new CompoundButton.OnCheckedChangeListener() {
                    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton,
                                                 boolean b) {

                        pump = getBit(swPump.isChecked());
                        myRef.setValue(pump + lamp + auto);
                        try {
                            Thread.sleep(200);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                    }
                });

        swAuto.setOnCheckedChangeListener(
                new CompoundButton.OnCheckedChangeListener() {
                    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton,
                                                 boolean b) {

                        auto = getBit(swAuto.isChecked());
                        myRef.setValue(pump + lamp + auto);
                        try {
                            Thread.sleep(200);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                    }
                });

    }

    private String getBit(boolean check) {
        return check ? "1" : "0";
    }


    private void setSwitch() {
        if (status.charAt(0) == '1') {
            swPump.setChecked(true);
            pump = "1";
        } else {
            swPump.setChecked(false);
            pump = "0";
        }
        if (status.charAt(1) == '1') {
            swLamp.setChecked(true);
            lamp = "1";
        } else {
            swLamp.setChecked(false);
            lamp = "0";
        }
        if (status.charAt(2) == '1') {
            swAuto.setChecked(true);
            auto = "1";
        } else {
            swAuto.setChecked(false);
            auto = "0";
        }
    }
}
