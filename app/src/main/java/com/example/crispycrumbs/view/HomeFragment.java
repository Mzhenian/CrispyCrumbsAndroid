package com.example.crispycrumbs.view;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SearchView;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.crispycrumbs.R;
import com.example.crispycrumbs.adapter.VideoList_Adapter;
import com.example.crispycrumbs.dataUnit.PreviewVideoCard;
import com.example.crispycrumbs.viewModel.VideoViewModel;

import java.util.ArrayList;

public class HomeFragment extends Fragment implements VideoList_Adapter.OnItemClickListener {

    private static final String TAG = "HomeFragment";
    private VideoList_Adapter adapter;
    private VideoViewModel videoViewModel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.video_recycler_view);

        adapter = new VideoList_Adapter(getContext(), new ArrayList<>(), this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        videoViewModel = new ViewModelProvider(this).get(VideoViewModel.class);

        // Observe the video list LiveData from ViewModel
        videoViewModel.getAllVideos().observe(getViewLifecycleOwner(), videoList -> {
            // Update the adapter with the new video list
            if (videoList != null) {
                adapter.updateVideoList(videoList);
            } else {
                Log.e(TAG, "Video list is null");
            }
//            adapter.updateVideoList(videoList);
        });
//        // Observe the video list LiveData from ViewModel
//        videoViewModel.getAllVideos().observe(getViewLifecycleOwner(), videoList -> {
//            // Update the adapter with the new video list
//            adapter.updateVideoList(videoList);
//        });

        SearchView searchBar = view.findViewById(R.id.search_bar);
        customizeSearchViewIcon(searchBar);

        searchBar.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                adapter.filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.filter(newText);
                return false;
            }
        });

        return view;
    }

    @Override
    public void onItemClick(PreviewVideoCard video) {
        VideoPlayerFragment videoPlayerFragment = new VideoPlayerFragment();
        Bundle args = new Bundle();
        args.putString("videoId", video.getVideoId());
        videoPlayerFragment.setArguments(args);

        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, videoPlayerFragment)
                .addToBackStack(null)
                .commit();
    }

    private void customizeSearchViewIcon(SearchView searchView) {
        try {
            int searchIconId = searchView.getContext().getResources().getIdentifier("android:id/search_mag_icon", null, null);
            ImageView searchIcon = searchView.findViewById(searchIconId);
            if (searchIcon != null) {
                searchIcon.setImageResource(R.drawable.search_icon);
            } else {
                Log.e(TAG, "Search icon ImageView not found");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error customizing search icon", e);
        }
    }
}
