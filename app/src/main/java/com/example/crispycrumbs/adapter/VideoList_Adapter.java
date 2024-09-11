package com.example.crispycrumbs.adapter;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.crispycrumbs.R;
import com.example.crispycrumbs.dataUnit.PreviewVideoCard;
import com.example.crispycrumbs.dataUnit.UserItem;
import com.example.crispycrumbs.serverAPI.ServerAPI;
import com.example.crispycrumbs.view.MainPage;
import com.example.crispycrumbs.view.VideoPlayerFragment;
import com.example.crispycrumbs.viewModel.UserViewModel;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


import java.util.ArrayList;
import java.util.List;

public class VideoList_Adapter extends RecyclerView.Adapter<VideoList_Adapter.ViewHolder> {
    private static final String TAG = "VideoList_Adapter";
    private final Context context;
    private final OnItemClickListener listener;
    protected List<PreviewVideoCard> originalVideoList;
    protected List<PreviewVideoCard> filteredVideoList;
    private final UserViewModel userViewModel;

    public VideoList_Adapter(Context context, List<PreviewVideoCard> videoArrayList, OnItemClickListener listener) {
        this.context = context;
        this.originalVideoList = videoArrayList;
        this.filteredVideoList = new ArrayList<>(originalVideoList);
        this.listener = listener;
        this.userViewModel = new ViewModelProvider((AppCompatActivity) context).get(UserViewModel.class);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.video_pre_item, parent, false);
        return new ViewHolder(view, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PreviewVideoCard video = filteredVideoList.get(position);
        if (video == null) {
            Log.e(TAG, "Video is null");
            return;
        }

        holder.bind(video, userViewModel);
    }

    @Override
    public int getItemCount() {
        return filteredVideoList.size();
    }

    public void filter(String query) {
        filteredVideoList.clear();
        if (query.isEmpty()) {
            filteredVideoList.addAll(originalVideoList);
        } else {
            for (PreviewVideoCard video : originalVideoList) {
                if (video.getTitle().toLowerCase().contains(query.toLowerCase())) {
                    filteredVideoList.add(video);
                }
            }
        }
        notifyDataSetChanged();
    }

    public void updateVideoList(List<PreviewVideoCard> newVideoList) {
        originalVideoList.clear();
        originalVideoList.addAll(newVideoList);
        filteredVideoList.clear();
        filteredVideoList.addAll(newVideoList);
        notifyDataSetChanged();
    }

    public interface OnItemClickListener {
        void onItemClick(PreviewVideoCard video);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView videoThumbnail, profilePicture;
        TextView videoTitle, videoUser, videoViews, videoDate;
        private Observer<UserItem> userObserver;

        public ViewHolder(@NonNull View itemView, final OnItemClickListener listener) {
            super(itemView);
            videoThumbnail = itemView.findViewById(R.id.video_thumbnail);
            profilePicture = itemView.findViewById(R.id.profile_picture);
            videoTitle = itemView.findViewById(R.id.video_title);
            videoUser = itemView.findViewById(R.id.user_name);
            videoViews = itemView.findViewById(R.id.video_views);
            videoDate = itemView.findViewById(R.id.video_date);

            itemView.setOnClickListener(v -> listener.onItemClick((PreviewVideoCard) v.getTag()));
        }

        public void bind(PreviewVideoCard video, UserViewModel userViewModel) {
            // Set video data
            videoTitle.setText(video.getTitle());
            videoViews.setText(video.getViews() + " views");

            // Format and set the video date
            String formattedDate = formatDateString(video.getUploadDate());
            videoDate.setText(formattedDate);

            // Load thumbnail using Glide
            String thumbnailUrl = ServerAPI.getInstance().constructUrl(video.getThumbnail());
            Glide.with(itemView.getContext())
                    .load(thumbnailUrl)
                    .placeholder(R.drawable.default_video_thumbnail)
                    .into(videoThumbnail);

            // Remove the old observer if it exists to prevent data from getting mixed up
            if (userObserver != null) {
                userViewModel.getUser(video.getUserId()).removeObserver(userObserver);
            }

            // Create a new observer for the current video user
            userObserver = user -> {
                if (user != null) {
                    String userProfileUrl = ServerAPI.getInstance().constructUrl(user.getProfilePhoto());
                    Glide.with(itemView.getContext())
                            .load(userProfileUrl)
                            .placeholder(R.drawable.default_profile_picture)
                            .into(profilePicture);
                    videoUser.setText(user.getUserName());
                } else {
                    profilePicture.setImageResource(R.drawable.default_profile_picture);
                    videoUser.setText("[deleted user]");
                    Log.e(TAG, "User not found");
                }
            };

            // Observe the current user's data
            userViewModel.getUser(video.getUserId()).observe((AppCompatActivity) itemView.getContext(), userObserver);

            itemView.setTag(video);
        }

        private String formatDateString(String originalDate) {
            SimpleDateFormat originalFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
            SimpleDateFormat targetFormat = new SimpleDateFormat("dd.MM.yy", Locale.getDefault());

            try {
                Date date = originalFormat.parse(originalDate);
                return targetFormat.format(date);
            } catch (ParseException e) {
                e.printStackTrace();
                // Return the original date if parsing fails
                return originalDate;
            }
        }
    }
}

