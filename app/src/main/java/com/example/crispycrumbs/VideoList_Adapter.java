package com.example.crispycrumbs;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.crispycrumbs.PreviewVideoCard;
import com.example.crispycrumbs.R;

import java.util.ArrayList;

public class VideoList_Adapter extends RecyclerView.Adapter<VideoList_Adapter.ViewHolder> {

    private Context context;
    // Store the original list
    private ArrayList<PreviewVideoCard> originalVideoList;

    private ArrayList<PreviewVideoCard> filteredVideoList;

    public VideoList_Adapter(Context context, ArrayList<PreviewVideoCard> videoArrayList) {
        this.context = context;
        this.originalVideoList = videoArrayList;
        // Initially, show all items
        this.filteredVideoList = new ArrayList<>(originalVideoList);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.video_pre_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PreviewVideoCard videoCard = filteredVideoList.get(position);

        holder.videoTitle.setText(videoCard.getTitle());
        holder.videoUser.setText(videoCard.getUserId());
        holder.videoViews.setText(String.valueOf(videoCard.getViews()));
        holder.videoDate.setText(videoCard.getUploadDate());

        // Load thumbnail using a resource ID
        holder.videoThumbnail.setImageResource(videoCard.getThumbnailResId());
    }

    @Override
    public int getItemCount() {
        return filteredVideoList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView videoThumbnail;
        TextView videoTitle, videoUser, videoViews, videoDate;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            videoThumbnail = itemView.findViewById(R.id.video_thumbnail);
            videoTitle = itemView.findViewById(R.id.video_title);
            videoUser = itemView.findViewById(R.id.user_name);
            videoViews = itemView.findViewById(R.id.views);
            videoDate = itemView.findViewById(R.id.date);
        }
    }

    // Filter method for SearchView
    public void filter(String query) {
        filteredVideoList.clear();
        if (query == null || query.trim().isEmpty()) {
            filteredVideoList.addAll(originalVideoList); // Show all items if query is empty
        } else {
            String lowerCaseQuery = query.toLowerCase();
            for (PreviewVideoCard video : originalVideoList) {
                if (video.getTitle().toLowerCase().contains(lowerCaseQuery)) {
                    filteredVideoList.add(video);
                }
            }
        }
        notifyDataSetChanged(); // Notify adapter of data change
    }
}
