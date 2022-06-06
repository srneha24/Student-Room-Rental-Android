package com.example.studentroomrental;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;

public class RentRequestAdapter extends RecyclerView.Adapter<RentRequestAdapter.RentRequestViewHolder> {

    Context context;
    ArrayList<Request> rent_request_detailsList;
    String ad_id;

    RentRequestViewHolder.onRentRequestClickListener rent_requestClickListener;

    public RentRequestAdapter(Context context, ArrayList<Request> rent_request_holderList, RentRequestViewHolder.onRentRequestClickListener rent_requestClickListener, String ad_id) {
        this.context = context;
        this.rent_request_detailsList = rent_request_holderList;
        this.rent_requestClickListener = rent_requestClickListener;
        this.ad_id = ad_id;
    }

    @NonNull
    @Override
    public RentRequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.rent_request_holder, parent, false);
        return new RentRequestViewHolder(view, rent_requestClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull RentRequestViewHolder holder, int position) {
        Request rent_request = rent_request_detailsList.get(position);

        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = firebaseDatabase.getReference("user").child(rent_request.getUser_id());

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                holder.request_user.setText(snapshot.child("user_name").getValue().toString());
                holder.time.setText(rent_request.getTime());

                holder.rent_approval.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (holder.rent_approval.isChecked()) {
                            AlertDialog rent_approve_conf = new AlertDialog.Builder(context).create();
                            rent_approve_conf.setTitle("Approve Rent?");
                            rent_approve_conf.setMessage("This action cannot be undone.");
                            rent_approve_conf.setButton(AlertDialog.BUTTON_POSITIVE, "YES", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                                    DatabaseReference dbRef = database.getReference("ad").child(ad_id);

                                    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                                    LocalDateTime now = LocalDateTime.now();

                                    HashMap approve = new HashMap();
                                    approve.put("rented", 1);
                                    approve.put("rented_by", snapshot.child("user_name").getValue().toString());
                                    approve.put("rent_date", dtf.format(now));

                                    dbRef.updateChildren(approve).addOnCompleteListener(new OnCompleteListener() {
                                        @Override
                                        public void onComplete(@NonNull Task task) {
                                            FirebaseDatabase firebaseDatabase1 = FirebaseDatabase.getInstance();
                                            DatabaseReference databaseReference1 = firebaseDatabase1.getReference("rental").child(rent_request.getUser_id());

                                            HashMap ad_rental = new HashMap();
                                            ad_rental.put(ad_id, dtf.format(now));

                                            databaseReference1.updateChildren(ad_rental).addOnCompleteListener(new OnCompleteListener() {
                                                @Override
                                                public void onComplete(@NonNull Task task) {
                                                    FirebaseDatabase firebaseDatabase2 = FirebaseDatabase.getInstance();
                                                    DatabaseReference databaseReference2 = firebaseDatabase2.getReference("rent-request");

                                                    databaseReference2.child(ad_id).removeValue();

                                                    Toast.makeText(context, "Rent Approved For " + snapshot.child("user_name").getValue().toString(), Toast.LENGTH_LONG).show();

                                                    Intent intent_ad_approves = new Intent(context, Homepage.class);

                                                    context.startActivity(intent_ad_approves);
                                                    ((RentRequest)context).finish();
                                                }
                                            });

                                        }
                                    });

                                }
                            });

                            rent_approve_conf.setButton(AlertDialog.BUTTON_NEGATIVE, "CANCEL", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    holder.rent_approval.setChecked(false);
                                    rent_approve_conf.dismiss();
                                }
                            });

                            rent_approve_conf.show();
                        }
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    @Override
    public int getItemCount() {
        return rent_request_detailsList.size();
    }

    public static class RentRequestViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView request_user, time;
        CheckBox rent_approval;
        onRentRequestClickListener rent_requestClickListener;

        public RentRequestViewHolder(@NonNull View itemView, onRentRequestClickListener rent_requestClickListener) {
            super(itemView);

            request_user = itemView.findViewById(R.id.RequestName);
            time = itemView.findViewById(R.id.NotificationTime);
            rent_approval = itemView.findViewById(R.id.ApproveRentCheckBox);
            this.rent_requestClickListener = rent_requestClickListener;

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            rent_requestClickListener.onRentRequestClick(getAdapterPosition());
        }

        public interface onRentRequestClickListener {
            void onRentRequestClick(int position);
        }
    }
}
