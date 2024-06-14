package com.example.crispycrumbs;

import android.app.AlertDialog;
import android.net.Uri;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.VideoView;

import java.util.ArrayList;

public class VideoPlayerFragment extends Fragment implements CommentSection_Adapter.CommentActionListener {
    private CustomMediaController mediaController;
    private VideoView videoView;
    private ArrayList<CommentItem> commentItemArrayList = new ArrayList<>();
    private int[] image = {R.drawable.small_logo};
    private int currentPosition = 0;

    private RecyclerView recyclerView;
    private CommentSection_Adapter adapter;
    private String videoId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_video_player, container, false);

        recyclerView = view.findViewById(R.id.comment_section);
        Button likeButton = view.findViewById(R.id.like_button);
        Button shareButton = view.findViewById(R.id.share_button);
        videoView = view.findViewById(R.id.video_view);

        // Set up buttons
        Button commentButton = view.findViewById(R.id.comment_button);
        commentButton.setOnClickListener(v -> {
            // Create an AlertDialog to get the user's comment input
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle("Add a Comment");

            // Set up the input fields
            LinearLayout layout = new LinearLayout(getContext());
            layout.setOrientation(LinearLayout.VERTICAL);

            final EditText inputContent = new EditText(getContext());
            inputContent.setHint("Comment");
            layout.addView(inputContent);

            builder.setView(layout);

            // Set up the buttons
            builder.setPositiveButton("OK", (dialog, which) -> {
                String content = inputContent.getText().toString();
                String date = "Now"; // For simplicity, setting the date as "Now"

                // Validate inputs
                if (!content.isEmpty()) {
                    // Add new comment to the list and notify adapter
                    CommentItem newComment = new CommentItem(R.drawable.small_logo, "DefaultUser", content, date);
                    commentItemArrayList.add(newComment);
                    adapter.notifyItemInserted(commentItemArrayList.size() - 1);

                    // Add comment to DataManager
                    DataManager dataManager = DataManager.getInstance();
                    dataManager.addCommentToVideo(videoId, newComment);
                } else {
                    // Handle empty inputs (show a message or do nothing)
                    Toast.makeText(getContext(), "Please enter a comment.", Toast.LENGTH_SHORT).show();
                }
            });

            builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

            builder.show();
        });

        // Receive the video data from the bundle
        Bundle bundle = getArguments();
        if (bundle != null) {
            String videoTitle = bundle.getString("videoTitle");
            String videoDescription = bundle.getString("videoDescription");
            String videoFile = bundle.getString("videoPath");
            videoId = bundle.getString("videoId"); // Store videoId for later use

            // Fetch comments for the video from the DataManager
            DataManager dataManager = DataManager.getInstance();
            commentItemArrayList = dataManager.getCommentsForVideo(videoId);

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

            // Initialize RecyclerView and Adapter with comments
            adapter = new CommentSection_Adapter(getContext(), commentItemArrayList, this);
            recyclerView.setAdapter(adapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        }

        return view;
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

    // Implement interface methods for edit and delete actions
    @Override
    public void onEditComment(int position) {
        // Handle edit action (dialog, etc.)
        // Example: Modify the comment item and notify adapter
        CommentItem item = commentItemArrayList.get(position);
        // item.setContent("Updated Content");
        commentItemArrayList.set(position, item);
        adapter.notifyItemChanged(position);
    }

    @Override
    public void onDeleteComment(int position) {
        // Handle delete action
        commentItemArrayList.remove(position);
        adapter.notifyItemRemoved(position);
    }
}
