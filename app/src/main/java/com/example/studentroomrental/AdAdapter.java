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

public class AdAdapter extends RecyclerView.Adapter<AdAdapter.AdViewHolder> {

    Context context;
    ArrayList<Ad> ad_detailsList;

    AdViewHolder.onAdClickListener adClickListener;

    public AdAdapter(Context context, ArrayList<Ad> ad_holderList, AdViewHolder.onAdClickListener adClickListener) {
        this.context = context;
        this.ad_detailsList = ad_holderList;
        this.adClickListener = adClickListener;
    }

    @NonNull
    @Override
    public AdViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.ad_holder, parent, false);
        return new AdViewHolder(view, adClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull AdViewHolder holder, int position) {
        Ad ad = ad_detailsList.get(position);

        holder.title.setText(ad.getHeading());
        holder.ad_publisher.setText(ad.getUser_name());
        holder.seat.setText(String.valueOf(ad.getSeats()));
        holder.rent.setText(String.valueOf(ad.getRent()));

        SimpleDateFormat old_format = new SimpleDateFormat("yyyy-MM-dd");
        Date new_format = null;
        try {
            new_format = old_format.parse(ad.getPost_date());
            old_format.applyPattern("dd MMMM yyyy");
        } catch (ParseException e) {
            e.printStackTrace();
        }

        holder.post_date.setText(old_format.format(new_format));
    }

    @Override
    public int getItemCount() {
        return ad_detailsList.size();
    }

    public static class AdViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView title, ad_publisher, seat, rent, post_date, approved;
        onAdClickListener adClickListener;

        public AdViewHolder(@NonNull View itemView, onAdClickListener adClickListener) {
            super(itemView);

            title = itemView.findViewById(R.id.title_result);
            ad_publisher = itemView.findViewById(R.id.ad_publisher_result);
            seat = itemView.findViewById(R.id.seat_result);
            rent = itemView.findViewById(R.id.rent_result);
            post_date = itemView.findViewById(R.id.post_date_result);
            approved = itemView.findViewById(R.id.AdStatus);
            this.adClickListener = adClickListener;

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            adClickListener.onAdClick(getAdapterPosition());
        }

        public interface onAdClickListener {
            void onAdClick(int position);
        }
    }
}
