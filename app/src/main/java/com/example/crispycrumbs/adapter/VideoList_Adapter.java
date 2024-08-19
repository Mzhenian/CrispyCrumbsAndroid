package com.example.crispycrumbs.adapter;

import static com.example.crispycrumbs.model.DataManager.getUriFromResOrFile;
import static com.example.crispycrumbs.view.MainPage.getDataManager;

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
import androidx.recyclerview.widget.RecyclerView;

import com.example.crispycrumbs.R;
import com.example.crispycrumbs.data.PreviewVideoCard;
import com.example.crispycrumbs.data.UserItem;
import com.example.crispycrumbs.model.DataManager;
import com.example.crispycrumbs.view.VideoPlayerFragment;

import java.util.ArrayList;

public class VideoList_Adapter extends RecyclerView.Adapter<VideoList_Adapter.ViewHolder> {
    private static final String TAG = "VideoList_Adapter";
    private final Context context;
    private final OnItemClickListener listener;
    protected ArrayList<PreviewVideoCard> originalVideoList;
    protected ArrayList<PreviewVideoCard> filteredVideoList;

    public VideoList_Adapter(Context context, ArrayList<PreviewVideoCard> videoArrayList, OnItemClickListener listener) {
        this.context = context;
        this.originalVideoList = videoArrayList;
        this.filteredVideoList = new ArrayList<>(originalVideoList);
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.video_pre_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PreviewVideoCard video = filteredVideoList.get(position);
        if (video == null) {
            Log.e(TAG, "Video is null");
            return;
        }
        holder.bind(video, listener);

        holder.videoTitle.setText(video.getTitle());
        holder.videoUser.setText(video.getUserId());
        holder.videoViews.setText(String.valueOf(video.getViews()));
        holder.videoDate.setText(video.getUploadDate());

        holder.videoThumbnail.setImageURI(getUriFromResOrFile(video.getThumbnail()));

        // Fetch user information
        UserItem user = getDataManager().getUserById(video.getUserId());
        if (user != null) {
            holder.profilePicture.setImageURI(getUriFromResOrFile(user.getProfilePhoto()));
            holder.videoUser.setText(user.getUserName());
        } else {
            holder.profilePicture.setImageResource(R.drawable.default_profile_picture);
            holder.videoUser.setText("[deleted user]");
            Log.e(TAG, "User not found");
        }

        // Handle click events on items
        holder.itemView.setOnClickListener(v -> {
            // Pass data to VideoPlayerFragment using BundleF
            Bundle bundle = new Bundle();
            bundle.putString("videoId", video.getVideoId());


            VideoPlayerFragment videoPlayerFragment = new VideoPlayerFragment();
            videoPlayerFragment.setArguments(bundle);


            // Replace current fragment with VideoPlayerFragment
            ((AppCompatActivity) context).getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, videoPlayerFragment)
                    .commit();
        });
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

    public interface OnItemClickListener {
        void onItemClick(PreviewVideoCard video);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView videoThumbnail, profilePicture;
        TextView videoTitle, videoUser, videoViews, videoDate;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            videoThumbnail = itemView.findViewById(R.id.video_thumbnail);
            profilePicture = itemView.findViewById(R.id.profile_picture);
            videoTitle = itemView.findViewById(R.id.video_title);
            videoUser = itemView.findViewById(R.id.user_name);
            videoViews = itemView.findViewById(R.id.video_views);
            videoDate = itemView.findViewById(R.id.video_date);
        }

        public void bind(PreviewVideoCard video, final OnItemClickListener listener) {
            videoTitle.setText(video.getTitle());
            UserItem uploader = DataManager.getInstance().getUserById(video.getUserId());
            if (uploader != null) {
                profilePicture.setImageURI(getUriFromResOrFile(uploader.getProfilePhoto()));
                videoUser.setText(uploader.getUserName());
            } else {
                profilePicture.setImageResource(R.drawable.default_profile_picture);
                videoUser.setText("[deleted user]");
                Log.e(TAG, "User not found");
            }
            videoViews.setText(video.getViews() + " views");
            videoDate.setText(video.getUploadDate());
            videoThumbnail.setImageURI(getUriFromResOrFile(video.getThumbnail()));

            itemView.setOnClickListener(v -> listener.onItemClick(video));
        }
    }
}
