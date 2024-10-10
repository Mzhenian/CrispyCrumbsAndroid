package com.example.crispycrumbs.view;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.crispycrumbs.R;
import com.example.crispycrumbs.adapter.VideoList_Adapter;
import com.example.crispycrumbs.dataUnit.PreviewVideoCard;
import com.example.crispycrumbs.databinding.FragmentHomeBinding;
import com.example.crispycrumbs.localDB.LoggedInUser;
import com.example.crispycrumbs.repository.VideoRepository;
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

        // Set up RecyclerView and Adapter
        adapter = new VideoList_Adapter(getContext(), new ArrayList<>(), this);
        binding.rvVideo.setAdapter(adapter);
        binding.rvVideo.setLayoutManager(new LinearLayoutManager(getContext()));

        // Set up ViewModel
        videoViewModel = new ViewModelProvider(this).get(VideoViewModel.class);

        // Observe the video list LiveData from ViewModel based on initial selection
        loadVideos(VideoRepository.VideoType.MOST_VIEWED);

        // Observe the login state
        LoggedInUser.getUser().observe(getViewLifecycleOwner(), loggedInUser -> {
            if (loggedInUser != null) {
                binding.btnUserVideos.setVisibility(View.VISIBLE);
                binding.btnUserVideos.setOnClickListener(v -> {
                    String userId = loggedInUser.getUserId();
                    loadVideos(VideoRepository.VideoType.USER_VIDEOS, userId);
                });
            } else {
                binding.btnUserVideos.setVisibility(View.GONE);
                // Adjust the weight sum to evenly distribute the remaining buttons
                binding.buttonContainer.setWeightSum(2);
            }
        });

        // Set up buttons to allow the user to select which videos to display
        binding.btnMostViewed.setOnClickListener(v -> loadVideos(VideoRepository.VideoType.MOST_VIEWED));
        binding.btnMostRecent.setOnClickListener(v -> loadVideos(VideoRepository.VideoType.MOST_RECENT));

        return view;
    }

    private void loadVideos(VideoRepository.VideoType videoType) {
        loadVideos(videoType, null);
    }

    private void loadVideos(VideoRepository.VideoType videoType, String userId) {
        videoViewModel.getVideosByType(videoType, userId).observe(getViewLifecycleOwner(), videoList -> {
            if (videoList != null) {
                adapter.updateVideoList(videoList);
            } else {
                Log.e(TAG, "Video list is null");
            }
        });
    }
    private boolean search(String query) {
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
