package com.example.crispycrumbs.ui;

import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.crispycrumbs.R;
import com.example.crispycrumbs.adapters.CommentSection_Adapter;
import com.example.crispycrumbs.data.CommentItem;

import java.util.ArrayList;

public class VideoPlayer extends AppCompatActivity {

    private Button likeButton ;
    private Button shareButton ;
    private Button commentButton ;
    ArrayList<CommentItem> commentItemArrayList = new ArrayList<>();
    int[] image = {R.drawable.small_logo};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.video_player);
        RecyclerView recyclerView = findViewById(R.id.comment_section);

        likeButton = findViewById(R.id.like_button);
        shareButton = findViewById(R.id.share_button);
        commentButton = findViewById(R.id.comment_button);


        setCommentItemArrayList();

        CommentSection_Adapter adapter = new CommentSection_Adapter(this, commentItemArrayList);

        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.video_player), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;

        });

    }



    public void setCommentItemArrayList() {
        for (int i = 0; i < 20; i++) {
            commentItemArrayList.add(new CommentItem(image[0],"guest" + i, "some text", "some date"));
        }
    }

}


