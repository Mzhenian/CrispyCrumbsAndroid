package com.example.crispycrumbs;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class MainPage extends AppCompatActivity {

    ArrayList<PreviewVideoCard> videoArrayList = new ArrayList<>();
    int[] image = {R.drawable.small_logo};

    @Override
     protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.page_main);
        RecyclerView recyclerView = findViewById(R.id.video_list);

        setVideoArrayList();

        VideoList_Adapter adapter = new VideoList_Adapter(this, videoArrayList);

        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        EditText searchBar = findViewById(R.id.search_bar);
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Not used in this case
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Filter the video list as the user types
                adapter.filter(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Not used in this case
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_page), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;

        });

    }

    public void setVideoArrayList() {
        for (int i = 0; i < 20; i++) {
            videoArrayList.add(new PreviewVideoCard("Video " + i, image[0]));
        }
    }
}