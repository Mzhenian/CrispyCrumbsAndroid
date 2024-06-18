package com.example.crispycrumbs.adapters;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.crispycrumbs.data.PreviewVideoCard;
import com.example.crispycrumbs.R;
import com.example.crispycrumbs.ui.VideoPlayerFragment;

import java.util.ArrayList;

public class VideoList_Adapter extends RecyclerView.Adapter<VideoList_Adapter.ViewHolder> {

    private Context context;
    private ArrayList<PreviewVideoCard> originalVideoList; // Original list of video items
    private ArrayList<PreviewVideoCard> filteredVideoList; // List of filtered video items

    public VideoList_Adapter(Context context, ArrayList<PreviewVideoCard> videoArrayList) {
        this.context = context;
        this.originalVideoList = videoArrayList;
        // Initially, show all items by copying the original list
        this.filteredVideoList = new ArrayList<>(originalVideoList);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the item layout and create ViewHolder instance
        View view = LayoutInflater.from(context).inflate(R.layout.video_pre_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Bind data to the ViewHolder
        PreviewVideoCard videoCard = filteredVideoList.get(position);

        holder.videoTitle.setText(videoCard.getTitle());
        holder.videoUser.setText(videoCard.getUserId());
        holder.videoViews.setText(String.valueOf(videoCard.getViews()));
        holder.videoDate.setText(videoCard.getUploadDate());

        // Load thumbnail using a resource ID
        holder.videoThumbnail.setImageResource(videoCard.getThumbnailResId());

        // Handle click events on items
        holder.itemView.setOnClickListener(v -> {
            // Pass data to VideoPlayerFragment using Bundle
            Bundle bundle = new Bundle();
            bundle.putString("videoId", videoCard.getVideoId());
            bundle.putString("videoTitle", videoCard.getTitle());
            bundle.putString("videoDescription", videoCard.getUploadDate());
            bundle.putString("videoPath", videoCard.getVideoFile());
            //bundle.putParcelableArrayList("comments", videoCard.getComments());

            VideoPlayerFragment videoPlayerFragment = new VideoPlayerFragment();
            videoPlayerFragment.setArguments(bundle);

            // Replace current fragment with VideoPlayerFragment
            ((AppCompatActivity) context).getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, videoPlayerFragment)
                    .addToBackStack(null) // Add to back stack for fragment navigation
                    .commit();
        });
    }

    @Override
    public int getItemCount() {
        return filteredVideoList.size(); // Return the size of filtered list
    }

    // ViewHolder class to hold and manage UI elements of each item
    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView videoThumbnail;
        TextView videoTitle, videoUser, videoViews, videoDate;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // Initialize UI elements from the item layout
            videoThumbnail = itemView.findViewById(R.id.video_thumbnail);
            videoTitle = itemView.findViewById(R.id.video_title);
            videoUser = itemView.findViewById(R.id.user_name);
            videoViews = itemView.findViewById(R.id.views);
            videoDate = itemView.findViewById(R.id.date);
        }
    }

    // Filter method for SearchView
    public void filter(String query) {
        filteredVideoList.clear(); // Clear the current filtered list
        if (query == null || query.trim().isEmpty()) {
            filteredVideoList.addAll(originalVideoList); // Show all items if query is empty
        } else {
            String lowerCaseQuery = query.toLowerCase();
            for (PreviewVideoCard video : originalVideoList) {
                if (video.getTitle().toLowerCase().contains(lowerCaseQuery)) {
                    filteredVideoList.add(video); // Add items matching the query to filtered list
                }
            }
        }
        notifyDataSetChanged(); // Notify adapter of data change
    }
}
