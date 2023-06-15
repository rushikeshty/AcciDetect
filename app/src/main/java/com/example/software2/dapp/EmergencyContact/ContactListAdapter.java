package com.example.software2.dapp.EmergencyContact;

/**
 * Created by sanya on 10/11/16.
 */

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.software2.dapp.R;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;


public class ContactListAdapter extends RecyclerView.Adapter<ContactListAdapter.ViewHolder>  {
    String email;
    Context context;
    FirebaseAuth firebaseAuth;
    DBEmergency db;
    int layoutResourceId;
    ArrayList<EmerContact> data=new ArrayList<EmerContact>();
    RelativeLayout relativeLayout;
    public ContactListAdapter(ArrayList<EmerContact> data, int layoutResourceId, Context context,String email) {
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.data = data;
        this.email=email;
        db=new DBEmergency(context);

    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(layoutResourceId, viewGroup, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        EmerContact contact=data.get(position);
        holder.textName.setText(contact._name);
        holder.textContact.setText(contact._phone);


    }

    @Override
    public int getItemCount() {
        return data == null ? 0 : data.size();
    }





    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        public TextView textName;
        public TextView textContact;
        public TextView btnEdit,btnDelete;
        public RelativeLayout relativeLayout;

        public ViewHolder(View row) {
            super(row);
            relativeLayout = (RelativeLayout) row.findViewById(R.id.relativeLayout);
            textName=(TextView)row.findViewById(R.id.cont_name);
            textContact=(TextView)row.findViewById(R.id.cont_number);
            btnEdit=(TextView)row.findViewById(R.id.btn_edit);
            btnDelete=(TextView)row.findViewById(R.id.btn_delete);
            btnDelete.setOnClickListener(this);
            btnEdit.setOnClickListener(this);
            relativeLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlertDialog.Builder build = new AlertDialog.Builder(context);
                    build.setMessage("Do you want to make a phone call?")
                            .setPositiveButton("Call !", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    // make the phone call here
                                    Intent intent = new Intent(Intent.ACTION_DIAL);
                                    intent.setData(Uri.parse("tel:"+textContact.getText().toString()));
                                    context.startActivity(intent);
                                }
                            })
                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    Toast.makeText(context, "call cancelled", Toast.LENGTH_SHORT).show();
                                }
                            });
                    AlertDialog dialog = build.create();
                    dialog.show();


                }
            });

        }

        @Override
        public void onClick(View v) {

            if(v.getId()==R.id.btn_edit)
            {
                Bundle b=new Bundle();
                b.putString("Name",textName.getText().toString());
                b.putString("Phone",textContact.getText().toString());
                b.putInt("ContactIndex", getPosition());

                Edit_ContactActivity edit_contactActivity = new Edit_ContactActivity(context,b);
                edit_contactActivity.show();
//                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                intent.putExtras(b);

                //context.startActivity(intent);
            }
            else if(v.getId()==R.id.btn_delete)
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                        builder.setTitle("Are you sure you want to delete?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                int position = getPosition() + 1;
                                db.updatecontact("cont" + position, "NULL", email);
                                 removeAt(getPosition());
                            }
                        }).setNegativeButton("No", null).show();
                builder.create();

            }


        }
        private void removeAt(int position) {
            data.remove(position);

            notifyItemRemoved(position);
            notifyItemRangeChanged(getPosition(), data.size());
        }

    }
    public void update(int position) {
        notifyItemChanged(position);
        notifyItemRangeChanged(position, data.size());
    }



    /*
     to store images efficiently in android
     retrieving is done in backgroud so as to avoid UI to slow down
     */


}



