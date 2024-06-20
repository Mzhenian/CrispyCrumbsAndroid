package com.example.crispycrumbs.adapters;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.crispycrumbs.R;
import com.example.crispycrumbs.data.PreviewVideoCard;
import com.example.crispycrumbs.data.UserItem;
import com.example.crispycrumbs.model.DataManager;

import java.util.ArrayList;

public class VideoList_Adapter extends RecyclerView.Adapter<VideoList_Adapter.ViewHolder> {

    private final Context context;
    private final ArrayList<PreviewVideoCard> videoList;
    private final ArrayList<PreviewVideoCard> filteredVideoList;
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(PreviewVideoCard videoCard);
    }

    public VideoList_Adapter(Context context, ArrayList<PreviewVideoCard> videoList, OnItemClickListener listener) {
        this.context = context;
        this.videoList = videoList;
        this.filteredVideoList = new ArrayList<>(videoList);
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
    }

    @Override
    public int getItemCount() {
        return filteredVideoList.size();
    }

    public void filter(String query) {
        filteredVideoList.clear();
        if (query.isEmpty()) {
            filteredVideoList.addAll(videoList);
        } else {
            for (PreviewVideoCard video : videoList) {
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

        public void bind(final PreviewVideoCard videoCard, final OnItemClickListener listener) {
            videoTitle.setText(videoCard.getTitle());
            UserItem uploader = DataManager.getInstance().getUserById(videoCard.getUserId());
            if (uploader != null) {
                String profilePicURI = uploader.getProfilePicURI();
                if (profilePicURI != null && !profilePicURI.isEmpty()) {
                    Uri profilePicUri = Uri.parse(profilePicURI);
                    profilePicture.setImageURI(profilePicUri);
                } else {
                    profilePicture.setImageResource(R.drawable.baseline_account_circle_24);
                }
                videoUser.setText(uploader.getUserName());
            } else {
                profilePicture.setImageResource(R.drawable.baseline_account_circle_24);
                videoUser.setText("Unknown");
            }
            videoViews.setText(videoCard.getViews() + " views");
            videoDate.setText(videoCard.getUploadDate());
            videoThumbnail.setImageResource(videoCard.getThumbnailResId());

            itemView.setOnClickListener(v -> listener.onItemClick(videoCard));
        }
    }
}
