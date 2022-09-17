package com.example.obd;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class DisplayTrip extends AppCompatActivity {

    RecyclerView recyclerView;
    CustomAdapter customAdapter;
    DataBaseHelper db;
    RecyclerView.LayoutManager layoutManager;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_trip);
        recyclerView = findViewById(R.id.recyclerView);

        Intent intent = getIntent();
        Bundle args = intent.getBundleExtra("BUNDLE");
        ArrayList<Trip> trips = (ArrayList<Trip>) args.getSerializable("ARRAYLIST");
        //new ListTrip().execute(email);

        customAdapter = new CustomAdapter(DisplayTrip.this, trips, recyclerView, DisplayTrip.this);
        recyclerView.setAdapter(customAdapter);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(DisplayTrip.this);
        recyclerView.setLayoutManager(layoutManager);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1){
            recreate();
        }
    }


}