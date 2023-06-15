package com.example.software2.dapp.AccidentDetect.Hosptialauthrity;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;


import com.example.software2.dapp.R;

import java.util.ArrayList;

public class myAdapter extends RecyclerView.Adapter<myAdapter.myviewholder> {
    ArrayList<Model> dataholder;                                               //array list to hold the reminders
     public myAdapter(ArrayList<Model> dataholder) {
        this.dataholder = dataholder;
    }

    @NonNull
    @Override
    public myviewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_accident_file, parent, false);  //inflates the xml file in recyclerview
        return new myviewholder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull myviewholder holder, int position) {
        holder.mLocation.setText(dataholder.get(position).getTitle());                                 //Binds the single reminder objects to recycler view
        holder.mStatus.setText(dataholder.get(position).getStatus());
         holder.Datetime.setText(dataholder.get(position).getDatetime());
    }

    @Override
    public int getItemCount() {
        return dataholder.size();
    }

    static class myviewholder extends RecyclerView.ViewHolder {

         TextView mLocation, mStatus,Datetime;

        public myviewholder(@NonNull View itemView) {
            super(itemView);
            mLocation = (TextView) itemView.findViewById(R.id.location);                               //holds the reference of the materials to show data in recyclerview
            mStatus = (TextView) itemView.findViewById(R.id.status);
             Datetime = (TextView) itemView.findViewById(R.id.timedate);
        }
    }
}
