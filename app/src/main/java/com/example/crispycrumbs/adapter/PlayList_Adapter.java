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

import java.util.ArrayList;

public class PlayList_Adapter extends VideoList_Adapter {
    UserItem user;
//    private final VideoList_Adapter.OnItemClickListener listener;

    public PlayList_Adapter(Context context, ArrayList<PreviewVideoCard> videoArrayList, VideoList_Adapter.OnItemClickListener listener, UserItem user) {
        super(context, videoArrayList, listener);
        this.user = user;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);


        ImageView imgEditMode = holder.itemView.findViewById(R.id.img_edit_mode);
        if (user != null && user.equals(LoggedInUser.getUser())) {
            imgEditMode.setVisibility(View.VISIBLE);

            holder.itemView.setOnClickListener(v -> {
                Bundle bundle = new Bundle();
                PreviewVideoCard previewVideoCard = filteredVideoList.get(position);
                bundle.putString("videoId", previewVideoCard.getVideoId());


                EditVideoFragment editVideoFragment = new EditVideoFragment();
                editVideoFragment.setArguments(bundle);

                MainPage.getInstance().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, editVideoFragment)
                        .addToBackStack(null)
                        .commit();
//                MainPage.getInstance().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new ProfileFragment()).addToBackStack(null).commit();

            });
        } else {
            imgEditMode.setVisibility(View.GONE);
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

    public interface OnItemClickListener {
        void onItemClick(PreviewVideoCard videoCard);
    }
}
