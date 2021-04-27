package com.project3.SmartPot;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;

import uk.co.senab.photoview.PhotoViewAttacher;

public class ShowActivity extends AppCompatActivity {
    private String tempDHT, humDHT, soilMoist;
    private TextView tvTemp, tvHum, tvSoilM;
    private Button btnRefresh;
    private ImageView imgVi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show);
        tvHum = (TextView) findViewById(R.id.hum);
        tvTemp = (TextView) findViewById(R.id.temp);
        tvSoilM = (TextView) findViewById(R.id.soilM);
        imgVi = (ImageView) findViewById(R.id.img);
        btnRefresh = (Button) findViewById(R.id.btnRefresh);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference tempRef = database.getReference("tempDHT");
        DatabaseReference humRef = database.getReference("humDHT");
        DatabaseReference soilMoistRef = database.getReference("soilMoist");
        tempRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                Float value = dataSnapshot.getValue(Float.class);
                tvTemp.setText(value + " C");

            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Toast.makeText(ShowActivity.this,
                        "Error", Toast.LENGTH_LONG).show();
            }
        });
        humRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                int value = dataSnapshot.getValue(Integer.class);
                tvHum.setText(value + " %");

            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Toast.makeText(ShowActivity.this,
                        "Error", Toast.LENGTH_LONG).show();
            }
        });
        soilMoistRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                int value = dataSnapshot.getValue(Integer.class);
                tvSoilM.setText(value + " %");

            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Toast.makeText(ShowActivity.this,
                        "Error", Toast.LENGTH_LONG).show();
            }
        });
        StorageReference mStorageRef;
        mStorageRef = FirebaseStorage.getInstance().getReference();
        mStorageRef.child("image/cat.JPG").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.with(ShowActivity.this).load(uri.toString()).into(imgVi);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle any errors
            }
        });


    }

}
