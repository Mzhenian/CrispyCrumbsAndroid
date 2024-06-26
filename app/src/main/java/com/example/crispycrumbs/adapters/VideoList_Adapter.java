package com.example.crispycrumbs.adapters;

import static com.example.crispycrumbs.model.DataManager.getUriFromResOrFile;
import static com.example.crispycrumbs.ui.MainPage.getDataManager;

import android.content.Context;
import android.net.Uri;
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
import com.example.crispycrumbs.data.UserItem;
import com.example.crispycrumbs.model.DataManager;
import com.example.crispycrumbs.ui.MainPage;
import com.example.crispycrumbs.ui.VideoPlayerFragment;

import java.util.ArrayList;

public class VideoList_Adapter extends RecyclerView.Adapter<VideoList_Adapter.ViewHolder> {

    private Context context;
    protected ArrayList<PreviewVideoCard> originalVideoList;
    protected ArrayList<PreviewVideoCard> filteredVideoList;
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(PreviewVideoCard videoCard);
    }

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
        PreviewVideoCard videoCard = filteredVideoList.get(position);

        holder.bind(videoCard, listener);

        holder.videoTitle.setText(videoCard.getTitle());
        holder.videoUser.setText(videoCard.getUserId());
        holder.videoViews.setText(String.valueOf(videoCard.getViews()));
        holder.videoDate.setText(videoCard.getUploadDate());

        holder.videoThumbnail.setImageURI(getUriFromResOrFile(videoCard.getThumbnail()));

        // Fetch user information
        UserItem user = getDataManager().getUserById(videoCard.getUserId());
        if (user != null) {
            holder.profilePicture.setImageURI(getUriFromResOrFile(user.getProfilePhoto()));
            holder.videoUser.setText(user.getUserName());
        } else {
            holder.profilePicture.setImageResource(R.drawable.default_profile_picture);
            holder.videoUser.setText("[deleted user]");
        }

        // Handle click events on items
        holder.itemView.setOnClickListener(v -> {
            // Pass data to VideoPlayerFragment using BundleF
            Bundle bundle = new Bundle();
            bundle.putString("videoId", videoCard.getVideoId());


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

        public void bind(final PreviewVideoCard video, final OnItemClickListener listener) {
            videoTitle.setText(video.getTitle());
            UserItem uploader = DataManager.getInstance().getUserById(video.getUserId());
            if (uploader != null) {
                profilePicture.setImageURI(getUriFromResOrFile(uploader.getProfilePhoto()));
                videoUser.setText(uploader.getUserName());
            } else {
                profilePicture.setImageResource(R.drawable.default_profile_picture);
                videoUser.setText("[deleted user]");
            }
            videoViews.setText(video.getViews() + " views");
            videoDate.setText(video.getUploadDate());
            videoThumbnail.setImageURI(getUriFromResOrFile(video.getThumbnail()));

            itemView.setOnClickListener(v -> listener.onItemClick(video));
        }
    }
}
