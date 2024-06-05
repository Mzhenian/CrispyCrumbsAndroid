package com.example.crispycrumbs;

import android.app.Activity;
import android.os.Bundle;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class CommentSection extends Activity {
    ArrayList<CommentItem> commentItemArrayList = new ArrayList<>();
    int[] image = {R.drawable.small_logo};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.comment_section);
        RecyclerView recyclerView = findViewById(R.id.comment_section);

        setCommentItemArrayList();

        CommentSection_Adapter adapter = new CommentSection_Adapter(this, commentItemArrayList);

        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    public void setCommentItemArrayList() {
        for (int i = 0; i < 10; i++) {
            commentItemArrayList.add(new CommentItem(image[0],"guest" + i, "some text", "some date"));
        }
    }

}
