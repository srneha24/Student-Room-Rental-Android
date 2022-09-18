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

public class UserAdAdapter extends RecyclerView.Adapter<UserAdAdapter.UserAdViewHolder> {

    Context context;
    ArrayList<Ad> user_ad_detailsList;

    UserAdViewHolder.onUserAdClickListener user_adClickListener;

    public UserAdAdapter(Context context, ArrayList<Ad> user_ad_holderList, UserAdViewHolder.onUserAdClickListener user_adClickListener) {
        this.context = context;
        this.user_ad_detailsList = user_ad_holderList;
        this.user_adClickListener = user_adClickListener;
    }

    @NonNull
    @Override
    public UserAdViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.user_ad_holder, parent, false);
        return new UserAdViewHolder(view, user_adClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull UserAdViewHolder holder, int position) {
        Ad user_ad = user_ad_detailsList.get(position);

        holder.title.setText(user_ad.getHeading());
        holder.rent.setText(String.valueOf(user_ad.getRent()));
        holder.seat.setText(String.valueOf(user_ad.getSeats()));

        if (user_ad.getApproved() == 1) {
            holder.approved.setText("Ad Posted");
        }

        else {
            holder.approved.setText("Ad Being Reviewed");
        }

        SimpleDateFormat old_format = new SimpleDateFormat("yyyy-MM-dd");
        Date new_format = null;
        try {
            new_format = old_format.parse(user_ad.getPost_date());
            old_format.applyPattern("dd MMMM yyyy");
        } catch (ParseException e) {
            e.printStackTrace();
        }

        holder.post_date.setText(old_format.format(new_format));
    }

    @Override
    public int getItemCount() {
        return user_ad_detailsList.size();
    }

    public static class UserAdViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView title, rent, post_date, seat, approved;
        onUserAdClickListener user_adClickListener;

        public UserAdViewHolder(@NonNull View itemView, onUserAdClickListener user_adClickListener) {
            super(itemView);

            title = itemView.findViewById(R.id.UserAdTitle);
            rent = itemView.findViewById(R.id.UserAdRent);
            post_date = itemView.findViewById(R.id.UserAdPostDate);
            seat = itemView.findViewById(R.id.UserAdSeat);
            approved = itemView.findViewById(R.id.AdStatus);
            this.user_adClickListener = user_adClickListener;

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            user_adClickListener.onUserAdClick(getAdapterPosition());
        }

        public interface onUserAdClickListener {
            void onUserAdClick(int position);
        }
    }
}
