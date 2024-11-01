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
import com.example.crispycrumbs.adapter.PlayList_Adapter;
import com.example.crispycrumbs.dataUnit.UserItem;
import com.example.crispycrumbs.databinding.FragmentPlaylistBinding;
import com.example.crispycrumbs.localDB.LoggedInUser;
import com.example.crispycrumbs.viewModel.VideoViewModel;

import java.util.ArrayList;

public class PlayListFragment extends Fragment {

    private final String TAG = "PlayListFragment";
    private FragmentPlaylistBinding binding;
    private PlayList_Adapter adapter;
    private UserItem user;
    private VideoViewModel videoViewModel;

    public PlayListFragment() {
        if (null != LoggedInUser.getUser().getValue()) {
            this.user = LoggedInUser.getUser().getValue();
        }
    }

    public PlayListFragment(UserItem user) {
        this.user = user;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentPlaylistBinding.inflate(getLayoutInflater(), container, false);
        View view = binding.getRoot();

        if (user != null && user != LoggedInUser.getUser().getValue()) {
            binding.playlistTitle.setText(user.getDisplayedName() + " Videos");
        }

        RecyclerView recyclerView = view.findViewById(R.id.rv_video);

        adapter = new PlayList_Adapter(getContext(), new ArrayList<>(), null, user != null ? user : LoggedInUser.getUser().getValue());
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Initialize VideoViewModel
        videoViewModel = new ViewModelProvider(this).get(VideoViewModel.class);

        // Observe videos by the user
        String userId = user != null ? user.getUserId() : LoggedInUser.getUser().getValue().getUserId();
        videoViewModel.getVideosByUser(userId).observe(getViewLifecycleOwner(), videoList -> {
            if (videoList != null) {
                adapter.updateVideoList(videoList);
            } else {
                Log.e(TAG, "Video list is null");
            }
        });

        // Search functionality
        SearchView searchBar = view.findViewById(R.id.search_bar);
//        customizeSearchViewIcon(searchBar);

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
}
