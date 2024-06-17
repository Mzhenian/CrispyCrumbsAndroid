package com.example.crispycrumbs.ui;

import android.os.Bundle;
<<<<<<< HEAD:app/src/main/java/com/example/crispycrumbs/ui/HomeFragment.java

=======
>>>>>>> refs/remotes/origin/main:app/src/main/java/com/example/crispycrumbs/HomeFragment.java
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
<<<<<<< HEAD:app/src/main/java/com/example/crispycrumbs/ui/HomeFragment.java

import com.example.crispycrumbs.data.PreviewVideoCard;
import com.example.crispycrumbs.R;
import com.example.crispycrumbs.Lists.VideoList;
import com.example.crispycrumbs.adapters.VideoList_Adapter;
import com.google.gson.Gson;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
=======
>>>>>>> refs/remotes/origin/main:app/src/main/java/com/example/crispycrumbs/HomeFragment.java
import java.util.ArrayList;

public class HomeFragment extends Fragment {

    // Adapter for the RecyclerView
    private VideoList_Adapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Find the RecyclerView in the layout
        RecyclerView recyclerView = view.findViewById(R.id.video_recycler_view);

        // Initialize the adapter with the context and video list
        DataManager dataManager = DataManager.getInstance();
        ArrayList<PreviewVideoCard> videoList = dataManager.getVideoList();
        adapter = new VideoList_Adapter(getContext(), videoList);

        // Set the adapter and layout manager for the RecyclerView
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Find the SearchView in the layout
        SearchView searchBar = view.findViewById(R.id.search_bar);

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
}
