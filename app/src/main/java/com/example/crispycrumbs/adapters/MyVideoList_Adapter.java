package com.example.crispycrumbs.adapters;

import static com.example.crispycrumbs.model.DataManager.getUriFromResOrFile;
import static com.example.crispycrumbs.ui.MainPage.getDataManager;

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

import com.example.crispycrumbs.R;
import com.example.crispycrumbs.data.PreviewVideoCard;
import com.example.crispycrumbs.data.UserItem;
import com.example.crispycrumbs.ui.EditVideoFragment;
import com.example.crispycrumbs.ui.MainPage;
import com.example.crispycrumbs.ui.VideoPlayerFragment;

import java.util.ArrayList;

public class MyVideoList_Adapter extends VideoList_Adapter  {

//    private final VideoList_Adapter.OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(PreviewVideoCard videoCard);
    }

    public MyVideoList_Adapter(Context context, ArrayList<PreviewVideoCard> videoArrayList, VideoList_Adapter.OnItemClickListener listener) {
        super(context, videoArrayList, listener);

    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);

        // Handle click events on items
        holder.itemView.setOnClickListener(v -> {
            // Pass data to VideoPlayerFragment using BundleF
            Bundle bundle = new Bundle();
            PreviewVideoCard previewVideoCard = filteredVideoList.get(position);
            bundle.putString("videoId", previewVideoCard.getVideoId());

            EditVideoFragment editVideoFragment = new EditVideoFragment();
            editVideoFragment.setArguments(bundle);

            // Replace current fragment with VideoPlayerFragment
            ((AppCompatActivity) MainPage.getInstance()).getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, editVideoFragment)
                    .addToBackStack(null) // Add to back stack for fragment navigation
                    .commit();
        });
    }

}
