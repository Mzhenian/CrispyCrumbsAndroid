package com.example.crispycrumbs.ui;

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

import com.example.crispycrumbs.model.DataManager;
import com.example.crispycrumbs.data.PreviewVideoCard;
import com.example.crispycrumbs.R;
import com.example.crispycrumbs.adapters.VideoList_Adapter;

import java.util.ArrayList;

public class HomeFragment extends Fragment implements VideoList_Adapter.OnItemClickListener {

    private static final String TAG = "HomeFragment";
    private VideoList_Adapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.video_recycler_view);

        DataManager dataManager = DataManager.getInstance();
        ArrayList<PreviewVideoCard> videoList = dataManager.getVideoList();
        adapter = new VideoList_Adapter(getContext(), videoList, this);

        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

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

        MainPage mainPage = (MainPage) getActivity();
        if (mainPage != null) {
            mainPage.updateNavigationMenu();
            mainPage.updateNavHeader();
        }

        return view;
    }

    @Override
    public void onItemClick(PreviewVideoCard videoCard) {
        VideoPlayerFragment videoPlayerFragment = new VideoPlayerFragment();
        Bundle args = new Bundle();
        args.putString("videoId", videoCard.getVideoId());
        args.putString("videoTitle", videoCard.getTitle());
        args.putString("videoDate", videoCard.getUploadDate());
        args.putString("videoPath", videoCard.getVideoFile());
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
