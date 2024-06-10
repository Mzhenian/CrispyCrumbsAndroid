package com.example.crispycrumbs;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class VideoPlayer extends AppCompatActivity {

    private final Button likeButton = findViewById(R.id.like_button);
    private final Button shareButton = findViewById(R.id.share_button);
    private final Button commentButton = findViewById(R.id.comment_button);
    private final LinearLayout shareMenu = findViewById(R.id.share_menu);

    private final boolean menuState = false;


    ArrayList<CommentItem> commentItemArrayList = new ArrayList<>();
    int[] image = {R.drawable.small_logo};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //EdgeToEdge.enable(this);
        setContentView(R.layout.video_player);
        RecyclerView recyclerView = findViewById(R.id.comment_section);

        setCommentItemArrayList();

        CommentSection_Adapter adapter = new CommentSection_Adapter(this, commentItemArrayList);

        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleShareMenu();
            }
        });
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.video_player), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;

        });
    }

    private void toggleShareMenu() {
        if (!menuState) {
            shareMenu.setVisibility(View.VISIBLE);
        } else {
            shareMenu.setVisibility(View.INVISIBLE);
        }
    }

    public void setCommentItemArrayList() {
        for (int i = 0; i < 20; i++) {
            commentItemArrayList.add(new CommentItem(image[0],"guest" + i, "some text", "some date"));
        }
    }

}
