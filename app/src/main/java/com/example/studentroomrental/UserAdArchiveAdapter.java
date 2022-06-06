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

public class UserAdArchiveAdapter extends RecyclerView.Adapter<UserAdArchiveAdapter.UserAdArchiveViewHolder> {

    Context context;
    ArrayList<Ad> ad_archive_detailsList;

    UserAdArchiveViewHolder.onUserAdArchiveClickListener ad_archiveClickListener;

    public UserAdArchiveAdapter(Context context, ArrayList<Ad> ad_archive_holderList, UserAdArchiveViewHolder.onUserAdArchiveClickListener ad_archiveClickListener) {
        this.context = context;
        this.ad_archive_detailsList = ad_archive_holderList;
        this.ad_archiveClickListener = ad_archiveClickListener;
    }

    @NonNull
    @Override
    public UserAdArchiveViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.user_ad_archive_holder, parent, false);
        return new UserAdArchiveViewHolder(view, ad_archiveClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull UserAdArchiveViewHolder holder, int position) {
        Ad ad_archive = ad_archive_detailsList.get(position);

        holder.title.setText(ad_archive.getHeading());
        holder.rent.setText(String.valueOf(ad_archive.getRent()));
        holder.seat.setText(String.valueOf(ad_archive.getSeats()));
        holder.rented_by.setText(ad_archive.getRented_by());

        SimpleDateFormat old_format = new SimpleDateFormat("yyyy-MM-dd");
        Date new_format = null;
        try {
            new_format = old_format.parse(ad_archive.getPost_date());
            old_format.applyPattern("dd MMMM yyyy");
        } catch (ParseException e) {
            e.printStackTrace();
        }

        holder.post_date.setText(old_format.format(new_format));

        old_format = new SimpleDateFormat("yyyy-MM-dd");
        new_format = null;
        try {
            new_format = old_format.parse(ad_archive.getRent_date());
            old_format.applyPattern("dd MMMM yyyy");
        } catch (ParseException e) {
            e.printStackTrace();
        }

        holder.rent_date.setText(old_format.format(new_format));
    }

    @Override
    public int getItemCount() {
        return ad_archive_detailsList.size();
    }

    public static class UserAdArchiveViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView title, rent, post_date, seat, rent_date, rented_by;
        onUserAdArchiveClickListener ad_archiveClickListener;

        public UserAdArchiveViewHolder(@NonNull View itemView, onUserAdArchiveClickListener ad_archiveClickListener) {
            super(itemView);

            title = itemView.findViewById(R.id.TitleArchive);
            rent = itemView.findViewById(R.id.RentArchive);
            post_date = itemView.findViewById(R.id.DatePostedArchive);
            seat = itemView.findViewById(R.id.SeatArchive);
            rent_date = itemView.findViewById(R.id.DateRentedArchive);
            rented_by = itemView.findViewById(R.id.RentedByArchive);
            this.ad_archiveClickListener = ad_archiveClickListener;

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            ad_archiveClickListener.onUserAdArchiveClick(getAdapterPosition());
        }

        public interface onUserAdArchiveClickListener {
            void onUserAdArchiveClick(int position);
        }
    }
}
