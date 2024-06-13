package com.example.crispycrumbs;

import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

import java.util.ArrayList;


public class VideoPlayerFragment extends Fragment {
    private CustomMediaController mediaController;
    private VideoView videoView;
    private ArrayList<CommentItem> commentItemArrayList = new ArrayList<>();
    private int[] image = {R.drawable.small_logo};
    private int currentPosition = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_video_player, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.comment_section);
        Button likeButton = view.findViewById(R.id.like_button);
        Button shareButton = view.findViewById(R.id.share_button);
        Button commentButton = view.findViewById(R.id.comment_button);
        videoView = view.findViewById(R.id.video_view);

        setCommentItemArrayList();

        CommentSection_Adapter adapter = new CommentSection_Adapter(this.getContext(), commentItemArrayList);

        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));

        // Receive the video data from the bundle
        Bundle bundle = getArguments();
        if (bundle != null) {
            String videoTitle = bundle.getString("videoTitle");
            String videoDescription = bundle.getString("videoDescription");
            String videoFile = bundle.getString("videoPath");



            // Set the video title in a TextView
            TextView titleTextView = view.findViewById(R.id.video_title);
            titleTextView.setText(videoTitle);

            // Set the video description in a TextView
            TextView descriptionTextView = view.findViewById(R.id.video_date);
            descriptionTextView.setText(videoDescription);

            // Load and play the video using the videoFile
            int videoResId = getResources().getIdentifier(videoFile, "raw", getContext().getPackageName());
            String videoPath = "android.resource://" + getContext().getPackageName() + "/" + videoResId;
            videoView.setVideoURI(Uri.parse(videoPath));

            // Initialize MediaController
            mediaController = new CustomMediaController(getContext());
            mediaController.setAnchorView(videoView);
            videoView.setMediaController(mediaController);

            videoView.start();
        }

        return view;
    }

    public void setCommentItemArrayList() {
        for (int i = 0; i < 20; i++) {
            commentItemArrayList.add(new CommentItem(image[0],"guest" + i, "some text", "some date"));
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        currentPosition = videoView.getCurrentPosition();
        videoView.pause();
    }

    @Override
    public void onResume() {
        super.onResume();
        videoView.seekTo(currentPosition);
        videoView.start();
    }

    @Override
    public void onStop() {
        super.onStop();
        videoView.stopPlayback();
    }

}