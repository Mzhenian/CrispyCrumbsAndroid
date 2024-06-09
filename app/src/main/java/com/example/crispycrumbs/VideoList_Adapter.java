package com.example.crispycrumbs;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class VideoList_Adapter extends RecyclerView.Adapter<VideoList_Adapter.MyViewHolder> {
    private Context context;
    private ArrayList<PreviewVideoCard> videoItemArrayList;

    private ArrayList<PreviewVideoCard> filteredVideoList;

    public VideoList_Adapter(Context context, ArrayList<PreviewVideoCard> videoItemArrayList) {
        this.context = context;
        this.videoItemArrayList = videoItemArrayList;
        this.filteredVideoList = new ArrayList<>(videoItemArrayList);
    }

    @NonNull
    @Override
    public VideoList_Adapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.video_pre_item, parent, false);
        return new VideoList_Adapter.MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VideoList_Adapter.MyViewHolder holder, int position) {
        PreviewVideoCard videoCard = filteredVideoList.get(position);
        holder.title.setText(videoCard.getTitle());
        holder.imageView.setImageResource(videoCard.getImage());

    }

    @Override
    public int getItemCount() {
        return filteredVideoList.size();
    }

    public void filter(String text) {
        filteredVideoList.clear();
        if (text.isEmpty()) {
            filteredVideoList.addAll(videoItemArrayList); // Show all items if search is empty
        } else {
            text = text.toLowerCase();
            for (PreviewVideoCard item : videoItemArrayList) {
                if (item.getTitle().toLowerCase().contains(text)) {
                    filteredVideoList.add(item);
                }
            }
        }
        notifyDataSetChanged(); // Update the RecyclerView
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        ImageView imageView;
        TextView title;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.video_thumbnail);
            title = itemView.findViewById(R.id.video_title);

        }
    }
}





