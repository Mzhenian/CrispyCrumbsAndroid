package com.example.crispycrumbs.view;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.crispycrumbs.R;
import com.example.crispycrumbs.adapter.VideoList_Adapter;
import com.example.crispycrumbs.dataUnit.PreviewVideoCard;
import com.example.crispycrumbs.databinding.FragmentEditVideoBinding;
import com.example.crispycrumbs.databinding.FragmentHomeBinding;
import com.example.crispycrumbs.viewModel.VideoViewModel;

import java.util.ArrayList;

public class HomeFragment extends Fragment implements VideoList_Adapter.OnItemClickListener {
    private static final String TAG = "HomeFragment";
    private VideoViewModel videoViewModel;
    private FragmentHomeBinding binding;


    private VideoList_Adapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();

        adapter = new VideoList_Adapter(getContext(), new ArrayList<>(), this);
        binding.rvVideo.setAdapter(adapter);
        binding.rvVideo.setLayoutManager(new LinearLayoutManager(getContext()));

        videoViewModel = new ViewModelProvider(this).get(VideoViewModel.class);

        // Observe the video list LiveData from ViewModel
        videoViewModel.getAllVideos().observe(getViewLifecycleOwner(), videoList -> {
            // Update the adapter with the new video list
            if (videoList != null) {
                adapter.updateVideoList(videoList);
            } else {
                Log.e(TAG, "Video list is null");
            }
        });


        binding.searchBar.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                videoViewModel.searchVideos(query).observe(getViewLifecycleOwner(), videoList -> {
                    // Update the adapter with the new video list
                    if (videoList != null) {
                        adapter.updateVideoList(videoList);
                    } else {
                        Toast.makeText(getContext(), "No video found for:" + query, Toast.LENGTH_LONG).show();
                    }
                });
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.filter(newText);
                return false;
            }
        });

        //todo WIP
//        ImageView searchIcon = binding.searchBar.findViewById(androidx.appcompat.R.id.search_button);
//        searchIcon.setOnClickListener(v -> {
//            String query = binding.searchBar.getQuery().toString();
//            binding.searchBar.setQuery(query, true);
//        });

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
}
