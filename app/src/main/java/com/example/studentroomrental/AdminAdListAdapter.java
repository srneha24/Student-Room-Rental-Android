package com.example.studentroomrental;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class AdminAdListAdapter extends RecyclerView.Adapter<AdminAdListAdapter.AdminAdListViewHolder> {

    Context context;
    ArrayList<Ad> admin_ad_list_detailsList;

    AdminAdListViewHolder.onAdminAdListClickListener admin_ad_listClickListener;

    public AdminAdListAdapter(Context context, ArrayList<Ad> admin_ad_list_holderList, AdminAdListViewHolder.onAdminAdListClickListener admin_ad_listClickListener) {
        this.context = context;
        this.admin_ad_list_detailsList = admin_ad_list_holderList;
        this.admin_ad_listClickListener = admin_ad_listClickListener;
    }

    @NonNull
    @Override
    public AdminAdListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.admin_ad_list_holder, parent, false);
        return new AdminAdListViewHolder(view, admin_ad_listClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull AdminAdListViewHolder holder, int position) {
        Ad admin_ad_list = admin_ad_list_detailsList.get(position);

        holder.title.setText(admin_ad_list.getHeading());
        holder.ad_publisher.setText(admin_ad_list.getUser_name());

        SimpleDateFormat old_format = new SimpleDateFormat("yyyy-MM-dd");
        Date new_format = null;
        try {
            new_format = old_format.parse(admin_ad_list.getPost_date());
            old_format.applyPattern("dd MMMM yyyy");
        } catch (ParseException e) {
            e.printStackTrace();
        }

        holder.post_date.setText(old_format.format(new_format));
    }

    @Override
    public int getItemCount() {
        return admin_ad_list_detailsList.size();
    }

    public static class AdminAdListViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView title, ad_publisher, post_date;
        onAdminAdListClickListener admin_ad_listClickListener;

        public AdminAdListViewHolder(@NonNull View itemView, onAdminAdListClickListener admin_ad_listClickListener) {
            super(itemView);

            title = itemView.findViewById(R.id.title_review);
            ad_publisher = itemView.findViewById(R.id.ad_publisher_review);
            post_date = itemView.findViewById(R.id.post_date_review);
            this.admin_ad_listClickListener = admin_ad_listClickListener;

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            admin_ad_listClickListener.onAdminAdListClick(getAdapterPosition());
        }

        public interface onAdminAdListClickListener {
            void onAdminAdListClick(int position);
        }
    }
}
