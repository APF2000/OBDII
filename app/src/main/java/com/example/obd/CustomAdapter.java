package com.example.obd;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.MyViewHolder> {

    Context context;
    List<Trip> trips;
    RecyclerView rv;
    Activity activity;

    public CustomAdapter(Context context, List<Trip> trips, RecyclerView rv, Activity activity){
        this.context = context;
        this.trips = trips;
        this.rv = rv;
        this.activity = activity;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.my_row, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CustomAdapter.MyViewHolder holder, int position) {
        Trip w = trips.get(position);
        holder.txtDist.setText("Freadas: " + w.getSlowdown());
        holder.txtId.setText("Viradas bruscas: " + w.getTurn());
        holder.txtSpeed.setText(w.getSpeedMax() + " Km/h");
        holder.txtTime.setText(w.getTime());

        holder.mainLayout.setOnClickListener(view -> {

        });
    }

    @Override
    public int getItemCount() {
        return trips.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        TextView txtId;
        TextView txtDist;
        TextView txtSpeed;
        TextView txtTime;
        LinearLayout mainLayout;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            txtId = itemView.findViewById(R.id.tripid_row);
            txtDist = itemView.findViewById(R.id.tripdist_row);
            txtSpeed = itemView.findViewById(R.id.tripspeed_row);
            txtTime = itemView.findViewById(R.id.triptime_row);
            mainLayout = itemView.findViewById(R.id.mainLayout);
        }
    }


}
