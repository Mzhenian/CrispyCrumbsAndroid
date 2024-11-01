package com.example.crispycrumbs.adapter;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import com.example.crispycrumbs.R;
import com.example.crispycrumbs.localDB.LoggedInUser;
import com.example.crispycrumbs.dataUnit.PreviewVideoCard;
import com.example.crispycrumbs.dataUnit.UserItem;
import com.example.crispycrumbs.view.EditVideoFragment;
import com.example.crispycrumbs.view.MainPage;
import com.example.crispycrumbs.view.VideoPlayerFragment;

import java.util.List;

public class PlayList_Adapter extends VideoList_Adapter {
    UserItem user;
//    private final VideoList_Adapter.OnItemClickListener listener;

    public PlayList_Adapter(Context context, List<PreviewVideoCard> videoArrayList, VideoList_Adapter.OnItemClickListener listener, UserItem user) {
        super(context, videoArrayList, listener);
        this.user = user;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);


        ImageView imgEditMode = holder.itemView.findViewById(R.id.img_edit_mode);
        if (user == null || !user.equals(LoggedInUser.getUser().getValue())) {
            imgEditMode.setVisibility(View.GONE);
        } else {
            imgEditMode.setVisibility(View.VISIBLE);

            imgEditMode.setOnClickListener(v -> {
                Bundle bundle = new Bundle();
                PreviewVideoCard video = filteredVideoList.get(position);
                bundle.putSerializable("video", video);
                EditVideoFragment editVideoFragment = new EditVideoFragment();
                editVideoFragment.setArguments(bundle);

                MainPage.getInstance().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, editVideoFragment)
                        .addToBackStack(null)
                        .commit();
            });
        }
            holder.itemView.setOnClickListener(v -> {
                Bundle bundle = new Bundle();
                PreviewVideoCard previewVideoCard = filteredVideoList.get(position);
                bundle.putString("videoId", previewVideoCard.getVideoId());


                VideoPlayerFragment videoPlayerFragment = new VideoPlayerFragment();
                videoPlayerFragment.setArguments(bundle);

                MainPage.getInstance().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, videoPlayerFragment)
                        .addToBackStack(null)
                        .commit();
            });
    }
}
