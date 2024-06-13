package com.example.crispycrumbs;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;


public class HomeFragment extends Fragment {

    private ArrayList<PreviewVideoCard> videoArrayList = new ArrayList<>();
    private VideoList_Adapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.video_recycler_view);
        loadJSONFromAsset();
        adapter = new VideoList_Adapter(getContext(), videoArrayList);

        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        SearchView searchBar = view.findViewById(R.id.search_bar);
        searchBar.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // Perform the final search
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

        // Inflate the layout for this fragment
        return view;
    }

    private void loadJSONFromAsset() {
        try {
            InputStream is = getContext().getAssets().open("videosDB.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            String json = new String(buffer, StandardCharsets.UTF_8);

            Gson gson = new Gson();
            VideoList videos = gson.fromJson(json, VideoList.class);
            Log.d("videos2", "loadJSONFromAsset: " + videos);

            Log.d("videos3", "loadJSONFromAsset: " + videos.getVideos().get(0).getThumbnailResId());
            Log.d("videos4", "loadJSONFromAsset: " + videos.getVideos().get(1).getThumbnailResId());
            Log.d("videos5", "loadJSONFromAsset: " + videos.getVideos().get(2).getThumbnailResId());

            if (videos != null && videos.getVideos() != null) {
                for (PreviewVideoCard video : videos.getVideos()) {
                    int thumbnailResId = getResources().getIdentifier(
                            video.getThumbnail(), "drawable", getContext().getPackageName());
                    video.setThumbnailResId(thumbnailResId);
                }

                videoArrayList.addAll(videos.getVideos());
                adapter.notifyDataSetChanged();
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


}
