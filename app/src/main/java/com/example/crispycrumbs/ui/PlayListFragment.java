package com.example.crispycrumbs.ui;

import static com.example.crispycrumbs.ui.MainPage.getDataManager;

import com.example.crispycrumbs.adapter.PlayList_Adapter;
import com.example.crispycrumbs.data.LoggedInUser;
import android.os.Bundle;
import android.util.Log;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SearchView;


import com.example.crispycrumbs.data.UserItem;
import com.example.crispycrumbs.databinding.FragmentPlaylistBinding;
import com.example.crispycrumbs.model.DataManager;
import com.example.crispycrumbs.data.PreviewVideoCard;
import com.example.crispycrumbs.R;

import java.util.ArrayList;

public class PlayListFragment extends Fragment {

    // Adapter for the RecyclerView
    private FragmentPlaylistBinding binding;
    private PlayList_Adapter adapter;
    private  UserItem user;

    public PlayListFragment() {
    }
    public PlayListFragment(UserItem user) {
        this.user=user;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding =  FragmentPlaylistBinding.inflate(getLayoutInflater(), container, false);

        // Inflate the layout for this fragment
        View view =  binding.getRoot();

        if (user != null && user != LoggedInUser.getUser()) {
            binding.playlistTitle.setText(user.getDisplayedName() + " Videos");
        }

        // Find the RecyclerView in the layout
        RecyclerView recyclerView = view.findViewById(R.id.video_recycler_view);

        // Initialize the adapter with the context and video list
        ArrayList<PreviewVideoCard> subVideoList;
        if (user != null) {
            subVideoList = getSubVideoList(user);
            adapter = new PlayList_Adapter(getContext(), subVideoList, null, user);
        } else {
            subVideoList = getSubVideoList();
            adapter = new PlayList_Adapter(getContext(), subVideoList, null, LoggedInUser.getUser());
        }



        // Set the adapter and layout manager for the RecyclerView
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Find the SearchView in the layout
        SearchView searchBar = view.findViewById(R.id.search_bar);

        // Customize the search icon in the SearchView
        customizeSearchViewIcon(searchBar);

        // Set a listener for query text changes in the SearchView
        searchBar.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // Perform the final search when the user submits the query
                adapter.filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // Filter the video list as the user types
                adapter.filter(newText);
                return false;
            }
        });

        return view; // Return the created view
    }
    public ArrayList<PreviewVideoCard> getSubVideoList() {
        DataManager dataManager = DataManager.getInstance();
        ArrayList<PreviewVideoCard> originalVideoList = dataManager.getVideoList();
        ArrayList<PreviewVideoCard> filteredVideoList = new ArrayList<>();

        for (String videoId : LoggedInUser.getUser().getUploadedVideos()) {
                    filteredVideoList.add( getDataManager().getVideoById(videoId));
        }
//        notifyDataSetChanged(); // Notify adapter of data change
        return filteredVideoList;
    }
    public ArrayList<PreviewVideoCard> getSubVideoList(UserItem user) {
        DataManager dataManager = DataManager.getInstance();
        ArrayList<PreviewVideoCard> originalVideoList = dataManager.getVideoList();
        ArrayList<PreviewVideoCard> filteredVideoList = new ArrayList<>();

        for (String videoId :user.getUploadedVideos()) {
            filteredVideoList.add( getDataManager().getVideoById(videoId));
        }
        return filteredVideoList;
    }

    private void customizeSearchViewIcon(SearchView searchView) {
        try {
            // Find the ImageView for the search icon
            int searchIconId = searchView.getContext().getResources().getIdentifier("android:id/search_mag_icon", null, null);
            ImageView searchIcon = searchView.findViewById(searchIconId);
            if (searchIcon != null) {
                searchIcon.setImageResource(R.drawable.search_icon); // Replace with your custom icon
            } else {
                Log.e("MyVideosFragment", "Search icon ImageView not found");
            }
        } catch (Exception e) {
            Log.e("MyVideosFragment", "Error customizing search icon", e);
        }
    }
}
