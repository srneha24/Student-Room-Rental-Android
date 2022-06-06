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

public class UserRentalAdapter extends RecyclerView.Adapter<UserRentalAdapter.UserRentalViewHolder> {

    Context context;
    ArrayList<Ad> user_rental_detailsList;

    UserRentalViewHolder.onUserRentalClickListener user_rentalClickListener;

    public UserRentalAdapter(Context context, ArrayList<Ad> user_rental_holderList, UserRentalViewHolder.onUserRentalClickListener user_rentalClickListener) {
        this.context = context;
        this.user_rental_detailsList = user_rental_holderList;
        this.user_rentalClickListener = user_rentalClickListener;
    }

    @NonNull
    @Override
    public UserRentalViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.user_rental_holder, parent, false);
        return new UserRentalViewHolder(view, user_rentalClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull UserRentalViewHolder holder, int position) {
        Ad user_rental = user_rental_detailsList.get(position);

        holder.title.setText(user_rental.getHeading());
        holder.ad_publisher.setText(user_rental.getUser_name());
        holder.rent.setText(String.valueOf(user_rental.getRent()));

        SimpleDateFormat old_format = new SimpleDateFormat("yyyy-MM-dd");
        Date new_format = null;
        try {
            new_format = old_format.parse(user_rental.getPost_date());
            old_format.applyPattern("dd MMMM yyyy");
        } catch (ParseException e) {
            e.printStackTrace();
        }

        holder.post_date.setText(old_format.format(new_format));

        old_format = new SimpleDateFormat("yyyy-MM-dd");
        new_format = null;
        try {
            new_format = old_format.parse(user_rental.getRent_date());
            old_format.applyPattern("dd MMMM yyyy");
        } catch (ParseException e) {
            e.printStackTrace();
        }

        holder.rent_date.setText(old_format.format(new_format));
    }

    @Override
    public int getItemCount() {
        return user_rental_detailsList.size();
    }

    public static class UserRentalViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView title, ad_publisher, rent, post_date, rent_date;
        onUserRentalClickListener user_rentalClickListener;

        public UserRentalViewHolder(@NonNull View itemView, onUserRentalClickListener user_rentalClickListener) {
            super(itemView);

            title = itemView.findViewById(R.id.title_rental);
            ad_publisher = itemView.findViewById(R.id.ad_publisher_rental);
            rent = itemView.findViewById(R.id.rent_rental);
            post_date = itemView.findViewById(R.id.post_date_rental);
            rent_date = itemView.findViewById(R.id.rent_date_rental);
            this.user_rentalClickListener = user_rentalClickListener;

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            user_rentalClickListener.onUserRentalClick(getAdapterPosition());
        }

        public interface onUserRentalClickListener {
            void onUserRentalClick(int position);
        }
    }
}
