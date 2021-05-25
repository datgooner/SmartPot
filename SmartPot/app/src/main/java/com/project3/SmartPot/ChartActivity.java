package com.project3.SmartPot;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.Calendar;

public class ChartActivity extends AppCompatActivity {
    EditText edtDateFrom, edtDateTo;
    Spinner dropdown;
    Button btnDrawChart;
    Dialog dialog;
    ImageView imgV1;
    private int lastSelectedYear;
    private int lastSelectedMonth;
    private int lastSelectedDayOfMonth;
    DatabaseReference myRef;
    StorageReference mStorageRef;
//    private int selectedChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chart);

//        selectedChart = 0;
        edtDateFrom = (EditText) findViewById(R.id.edtDateFrom);
        edtDateTo = (EditText) findViewById(R.id.edtDateTo);
        dropdown = (Spinner) findViewById(R.id.spinner1);
        btnDrawChart = (Button) findViewById(R.id.btnDrawChart);
        imgV1 = (ImageView) findViewById(R.id.imgV1);
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        myRef = database.getReference("drawChart");
        mStorageRef = FirebaseStorage.getInstance().getReference();

        String[] items = new String[]{"Humidity", "SoilM", "Temperature"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, items);
        dropdown.setAdapter(adapter);

//        dropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> parent, View view,
//                                       int position, long id) {
//
//                selectedChart = position;
//            }
//            @Override
//            public void onNothingSelected(AdapterView<?> parent) {
//                // TODO Auto-generated method stub
//            }
//        });
        edtDateFrom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickFromDate();
            }
        });
        edtDateTo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickToDate();
            }
        });
        btnDrawChart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawChart();
            }
        });
        final Calendar c = Calendar.getInstance();
        this.lastSelectedYear = c.get(Calendar.YEAR);
        this.lastSelectedMonth = c.get(Calendar.MONTH);
        this.lastSelectedDayOfMonth = c.get(Calendar.DAY_OF_MONTH);

    }

    private void pickFromDate() {
        DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year,
                                  int monthOfYear, int dayOfMonth) {

                edtDateFrom.setText(dayOfMonth + "," + (monthOfYear + 1) + "," + year);

            }
        };
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                android.R.style.Theme_Holo_Light_Dialog_NoActionBar,
                dateSetListener, lastSelectedYear, lastSelectedMonth, lastSelectedDayOfMonth);
        datePickerDialog.show();
    }

    private void pickToDate() {
        DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year,
                                  int monthOfYear, int dayOfMonth) {

                edtDateTo.setText(dayOfMonth + "," + (monthOfYear + 1) + "," + year);
                lastSelectedYear = year;
                lastSelectedMonth = monthOfYear;
                lastSelectedDayOfMonth = dayOfMonth;
            }
        };
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                android.R.style.Theme_Holo_Light_Dialog_NoActionBar,
                dateSetListener, lastSelectedYear, lastSelectedMonth, lastSelectedDayOfMonth);
        datePickerDialog.show();
    }

    private void drawChart() {
        String dateFrom, dateTo, dataWant;

        imgV1.setImageDrawable(getResources().getDrawable(R.drawable.loading));

        dateFrom = edtDateFrom.getText().toString();
        dateTo = edtDateTo.getText().toString();
        dataWant = dropdown.getSelectedItem().toString();
        myRef.setValue(dateFrom + "@" + dateTo + "@" + dataWant);

        try {

            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        StorageReference mImageStorage = FirebaseStorage.getInstance().getReference();
        StorageReference ref = mImageStorage.child("fig/figByDate.png");


        ref.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    Uri downUri = task.getResult();

                    Picasso.with(ChartActivity.this).load(downUri.toString()).into(imgV1);
                }else{
                    Toast.makeText(ChartActivity.this, ""+task.getException(), Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
}
