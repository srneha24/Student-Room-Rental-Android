package com.example.studentroomrental;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class ImageViewerAdapter extends RecyclerView.Adapter<ImageViewerAdapter.ImageViewerViewHolder> {

    Context context;
    ArrayList<AdImages> image_viewerList;
    ImageDeleteListener imageDeleteListener;
    ArrayList<AdImages> image_deleteList;

    View view;

    int type;

    public ImageViewerAdapter(Context context, ArrayList<AdImages> image_viewer_List, ImageDeleteListener imageDeleteListener, int type) {
        this.context = context;
        this.image_viewerList = image_viewer_List;
        this.imageDeleteListener = imageDeleteListener;
        this.type = type;
    }

    public ImageViewerAdapter(Context context, ArrayList<AdImages> image_viewer_List, int type) {
        this.context = context;
        this.image_viewerList = image_viewer_List;
        this.type = type;
    }

    @NonNull
    @Override
    public ImageViewerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        if (type == 1) {

            view = LayoutInflater.from(context).inflate(R.layout.image_viewer_holder, parent, false);
        }

        if (type == 2) {

            view = LayoutInflater.from(context).inflate(R.layout.image_viewer_edit_holder, parent, false);
        }

        return new ImageViewerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewerViewHolder holder, int position) {
        if (type == 1) {

            Glide.with(context).load(image_viewerList.get(position).getImageURL()).into(holder.ad_images);
        }

        if (type == 2) {

            Glide.with(context).load(image_viewerList.get(position).getImageURL()).into(holder.ad_images_edit);

            image_deleteList = new ArrayList<>();
            int pos = position;

            if (image_viewerList != null && image_viewerList.size() > 0) {
                holder.delete_selection.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (holder.delete_selection.isChecked()) {
                            image_deleteList.add(image_viewerList.get(pos));
                        }

                        else {
                            image_deleteList.remove(image_viewerList.get(pos));
                        }

                        imageDeleteListener.onImageDeleteListenerChange(image_deleteList);
                    }
                });
            }
        }
    }

    @Override
    public int getItemCount() {
        return image_viewerList.size();
    }

    public static class ImageViewerViewHolder extends RecyclerView.ViewHolder {

        ImageView ad_images;
        ImageView ad_images_edit;
        CheckBox delete_selection;

        public ImageViewerViewHolder(@NonNull View itemView) {
            super(itemView);

            ad_images = itemView.findViewById(R.id.ViewedAdImage);
            delete_selection = itemView.findViewById(R.id.DeleteImageSelection);
            ad_images_edit = itemView.findViewById(R.id.ViewedAdImageEdit);
        }
    }
}
