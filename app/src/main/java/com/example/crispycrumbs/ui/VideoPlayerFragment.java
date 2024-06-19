package com.example.crispycrumbs.ui;

import android.app.AlertDialog;
import android.net.Uri;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.VideoView;

import com.example.crispycrumbs.data.CommentItem;
import com.example.crispycrumbs.adapters.CommentSection_Adapter;
import com.example.crispycrumbs.data.LoggedInUser;
import com.example.crispycrumbs.data.UserItem;
import com.example.crispycrumbs.model.CustomMediaController;
import com.example.crispycrumbs.model.DataManager;
import com.example.crispycrumbs.R;

import java.util.ArrayList;

public class VideoPlayerFragment extends Fragment implements CommentSection_Adapter.CommentActionListener {
    private CustomMediaController mediaController;
    private VideoView videoView;
    private ArrayList<CommentItem> commentItemArrayList = new ArrayList<>();
    private int currentPosition = 0;
    private String videoId;

    private RecyclerView recyclerView;
    private CommentSection_Adapter adapter;

    private ImageButton likeButton;
    private ImageButton unlikeButton;
    private boolean isLiked = false;
    private boolean isUnliked = false;

    private static final String KEY_POSITION = "position";
    private static final String KEY_COMMENTS = "comments";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_video_player, container, false);

        recyclerView = view.findViewById(R.id.comment_section);
        likeButton = view.findViewById(R.id.like_button);
        unlikeButton = view.findViewById(R.id.unlike_button);
        Button shareButton = view.findViewById(R.id.share_button);
        videoView = view.findViewById(R.id.video_view);

        Button commentButton = view.findViewById(R.id.comment_button);
        commentButton.setOnClickListener(v -> showAddCommentDialog());

        shareButton.setOnClickListener(v -> showShareMenu());

        likeButton.setOnClickListener(v -> {
            UserItem currentUser = LoggedInUser.getUser();
            if (currentUser != null) {
                if (!currentUser.hasLiked(videoId)) {
                    likeButton.setColorFilter(getResources().getColor(R.color.black_div));
                    unlikeButton.setColorFilter(getResources().getColor(R.color.off_white));
                    currentUser.likeVideo(videoId);
                    isLiked = true;
                    isUnliked = false;
                } else {
                    likeButton.setColorFilter(getResources().getColor(R.color.off_white));
                    currentUser.removeLike(videoId);
                    isLiked = false;
                }
            }
        });

        unlikeButton.setOnClickListener(v -> {
            UserItem currentUser = LoggedInUser.getUser();
            if (currentUser != null) {
                if (!currentUser.hasDisliked(videoId)) {
                    unlikeButton.setColorFilter(getResources().getColor(R.color.black_div));
                    likeButton.setColorFilter(getResources().getColor(R.color.off_white));
                    currentUser.dislikeVideo(videoId);
                    isLiked = false;
                    isUnliked = true;
                } else {
                    unlikeButton.setColorFilter(getResources().getColor(R.color.off_white));
                    currentUser.removeDislike(videoId);
                    isUnliked = false;
                }
            }
        });

        Bundle bundle = getArguments();
        if (bundle != null) {
            String videoTitle = bundle.getString("videoTitle");
            String videoDescription = bundle.getString("videoDescription");
            String videoFile = bundle.getString("videoPath");
            videoId = bundle.getString("videoId");

            DataManager dataManager = DataManager.getInstance();
            commentItemArrayList = dataManager.getCommentsForVideo(videoId);

            TextView titleTextView = view.findViewById(R.id.video_title);
            titleTextView.setText(videoTitle);

            TextView descriptionTextView = view.findViewById(R.id.video_date);
            descriptionTextView.setText(videoDescription);

            int videoResId = getResources().getIdentifier(videoFile, "raw", getContext().getPackageName());
            String videoPath = "android.resource://" + getContext().getPackageName() + "/" + videoResId;
            videoView.setVideoURI(Uri.parse(videoPath));

            mediaController = new CustomMediaController(getContext());
            mediaController.setAnchorView(videoView);
            videoView.setMediaController(mediaController);

            if (savedInstanceState != null) {
                currentPosition = savedInstanceState.getInt(KEY_POSITION, 0);
                commentItemArrayList = savedInstanceState.getParcelableArrayList(KEY_COMMENTS);
                videoView.seekTo(currentPosition);
            }

            videoView.start();

            UserItem currentUser = LoggedInUser.getUser();
            String currentUserId = currentUser != null ? currentUser.getUserId() : null;

            if (currentUserId != null) {
                adapter = new CommentSection_Adapter(getContext(), commentItemArrayList, this, currentUserId);
                recyclerView.setAdapter(adapter);
                recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

                if (currentUser.hasLiked(videoId)) {
                    likeButton.setColorFilter(getResources().getColor(R.color.black_div));
                } else if (currentUser.hasDisliked(videoId)) {
                    unlikeButton.setColorFilter(getResources().getColor(R.color.black_div));
                }
            } else {
                Toast.makeText(getContext(), "Please log in to add and see comments.", Toast.LENGTH_LONG).show();
            }

            // Update user info for the video
            ImageView profilePicture = view.findViewById(R.id.profile_picture);
            TextView userNameTextView = view.findViewById(R.id.user_name);

            UserItem uploader = dataManager.getUserById(videoId);
            if (uploader != null) {
                Uri profilePicUri = Uri.parse(uploader.getProfilePicURI());
                profilePicture.setImageURI(profilePicUri);
                userNameTextView.setText(uploader.getUserName());
            } else {
                profilePicture.setImageResource(R.drawable.baseline_account_circle_24);
                userNameTextView.setText("Unknown");
            }
        }

        return view;
    }

    private void showAddCommentDialog() {
        UserItem currentUser = LoggedInUser.getUser();
        if (currentUser == null) {
            Toast.makeText(getContext(), "Please log in to add a comment.", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.add_comment_box, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();

        EditText inputContent = dialogView.findViewById(R.id.comment_input);
        Button positiveButton = dialogView.findViewById(R.id.positive_button);
        Button negativeButton = dialogView.findViewById(R.id.negative_button);

        positiveButton.setOnClickListener(v -> {
            String content = inputContent.getText().toString();
            String date = "Now";

            if (!content.isEmpty()) {
                CommentItem newComment = new CommentItem(R.drawable.small_logo, currentUser.getUserId(), currentUser.getUserName(), content, date);
                commentItemArrayList.add(newComment);
                adapter.notifyItemInserted(commentItemArrayList.size() - 1);

                DataManager dataManager = DataManager.getInstance();
                dataManager.addCommentToVideo(videoId, newComment);

                dialog.dismiss();
            } else {
                Toast.makeText(getContext(), "Please enter a comment.", Toast.LENGTH_SHORT).show();
            }
        });

        negativeButton.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void showEditCommentDialog(int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.add_comment_box, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();

        EditText inputContent = dialogView.findViewById(R.id.comment_input);
        Button positiveButton = dialogView.findViewById(R.id.positive_button);
        Button negativeButton = dialogView.findViewById(R.id.negative_button);

        // Set existing comment text
        inputContent.setText(commentItemArrayList.get(position).getComment());

        positiveButton.setOnClickListener(v -> {
            String content = inputContent.getText().toString();

            // Validate inputs
            if (!content.isEmpty()) {
                UserItem currentUser = LoggedInUser.getUser();
                // Update the comment and notify adapter
                CommentItem updatedComment = new CommentItem(R.drawable.small_logo, currentUser.getUserId(), currentUser.getUserName(), content, commentItemArrayList.get(position).getDate());
                commentItemArrayList.set(position, updatedComment);
                adapter.notifyItemChanged(position);

                // Update comment in DataManager
                DataManager dataManager = DataManager.getInstance();
                dataManager.addCommentToVideo(videoId, updatedComment);

                dialog.dismiss();
            } else {
                // Handle empty inputs (show a message or do nothing)
                Toast.makeText(getContext(), "Please enter a comment.", Toast.LENGTH_SHORT).show();
            }
        });

        negativeButton.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
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

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_POSITION, currentPosition);
        outState.putParcelableArrayList(KEY_COMMENTS, commentItemArrayList);
    }

    // Implement interface methods for edit and delete actions
    @Override
    public void onEditComment(int position) {
        showEditCommentDialog(position);
    }

    @Override
    public void onDeleteComment(int position) {

        // Remove the comment from the adapter
        adapter.removeComment(position);

        // Update DataManager
        // DataManager dataManager = DataManager.getInstance();
        //dataManager.removeCommentFromVideo(videoId, position);
    }

    private void showShareMenu() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.share_menu, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();

        Button shareEmail = dialogView.findViewById(R.id.share_email);
        Button shareFacebook = dialogView.findViewById(R.id.share_facebook);
        Button shareTwitter = dialogView.findViewById(R.id.share_twitter);
        Button shareWhatsapp = dialogView.findViewById(R.id.share_whatsapp);

        shareEmail.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Share via Email selected", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        shareFacebook.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Share on Facebook selected", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        shareTwitter.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Share on X selected", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        shareWhatsapp.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Share on Whatsapp selected", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        dialog.show();
    }
}
