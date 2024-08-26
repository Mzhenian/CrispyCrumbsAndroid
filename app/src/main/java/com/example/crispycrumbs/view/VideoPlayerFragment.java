package com.example.crispycrumbs.view;

import android.app.AlertDialog;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.crispycrumbs.R;
import com.example.crispycrumbs.adapter.CommentSection_Adapter;
import com.example.crispycrumbs.dataUnit.CommentItem;
import com.example.crispycrumbs.dataUnit.PreviewVideoCard;
import com.example.crispycrumbs.dataUnit.UserItem;
import com.example.crispycrumbs.localDB.LoggedInUser;
import com.example.crispycrumbs.viewModel.UserViewModel;
import com.example.crispycrumbs.viewModel.VideoViewModel;

import java.util.ArrayList;
import java.util.List;


public class VideoPlayerFragment extends Fragment implements CommentSection_Adapter.CommentActionListener {
    private static final String TAG = "VideoPlayerFragment";
    private static final String KEY_POSITION = "position";
    private static final String KEY_COMMENTS = "comments";


    private MediaController mediaController;
    private VideoView videoView;
    private ArrayList<CommentItem> commentItemArrayList = new ArrayList<>();
    private int currentPosition = 0;
    private String videoId;
    private PreviewVideoCard video;
    private ImageView profilePicture;
    private TextView userNameTextView;
    private TextView description, descriptionButton, titleTextView;
    private Button shareButton, commentButton;
    private ConstraintLayout buttonBar, commentSectionContainer;
    private RecyclerView recyclerView;
    private CommentSection_Adapter adapter;
    private ImageButton likeButton;
    private ImageButton unlikeButton;
    private TextView likesTextView;
    private TextView views;
    private VideoViewModel videoViewModel;
    private UserViewModel userViewModel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_video_player, container, false);

        userNameTextView = view.findViewById(R.id.user_name);
        videoView = view.findViewById(R.id.video_view);
        titleTextView = view.findViewById(R.id.video_title);
        description = view.findViewById(R.id.txt_video_description);
        likesTextView = view.findViewById(R.id.video_likes);
        views = view.findViewById(R.id.video_views);
        profilePicture = view.findViewById(R.id.profile_picture);
        commentButton = view.findViewById(R.id.comment_button);
        shareButton = view.findViewById(R.id.share_button);
        likeButton = view.findViewById(R.id.like_button);
        unlikeButton = view.findViewById(R.id.unlike_button);
        recyclerView = view.findViewById(R.id.comment_section);
        descriptionButton = view.findViewById(R.id.btn_video_description);
        buttonBar = view.findViewById(R.id.button_bar);
        commentSectionContainer = view.findViewById(R.id.comment_section_container);

        videoViewModel = new ViewModelProvider(this).get(VideoViewModel.class);
        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);

        // Get videoId from arguments
        Bundle bundle = getArguments();
        if (bundle != null) {
            videoId = bundle.getString("videoId");
        } else {
            Toast.makeText(getContext(), "No video selected", Toast.LENGTH_SHORT).show();
            return view;
        }

        // Observe the video data
        videoViewModel.getVideo(videoId).observe(getViewLifecycleOwner(), new Observer<PreviewVideoCard>() {
            @Override
            public void onChanged(PreviewVideoCard videoData) {
                if (videoData != null) {
                    video = videoData;
                    initializeVideo(video);
                    initializeVideoDetails(video);
//                    initializeCommentsSection(videoId);
                } else {
                    Toast.makeText(getContext(), "Failed to load video", Toast.LENGTH_SHORT).show();
                }
            }
        });

        return view;
    }

    private void initializeVideo(PreviewVideoCard video) {
        Uri videoUri = Uri.parse(video.getVideoFile());
        videoView.setVideoURI(videoUri);

        mediaController = new MediaController(getContext());
        mediaController.setAnchorView(videoView);
        videoView.setMediaController(mediaController);

        videoView.start();
    }

    private void initializeVideoDetails(PreviewVideoCard video) {
        titleTextView.setText(video.getTitle());
        description.setText(video.getDescription());
        likesTextView.setText(video.getLikes() + " likes");
        views.setText(video.getViews() + " views");

        UserItem uploader = userViewModel.getUser(video.getUserId()).getValue();
        if (uploader != null) {
            profilePicture.setImageURI(Uri.parse(uploader.getProfilePhoto()));
            userNameTextView.setText(uploader.getUserName());
        } else {
            profilePicture.setImageResource(R.drawable.default_profile_picture);
            userNameTextView.setText("[deleted user]");
            Log.e(TAG, "User not found");
        }

        profilePicture.setOnClickListener(v -> {
            MainPage.getInstance().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new ProfileFragment(video.getUserId()))
                    .commit();
        });

        descriptionButton.setOnClickListener(v -> toggleDescriptionComments());

//        likeButton.setOnClickListener(v -> likeClick());
//        unlikeButton.setOnClickListener(v -> dislikeClick());
//
//        shareButton.setOnClickListener(v -> showShareMenu());
//
//        // Increment video views
//        videoViewModel.incrementVideoViews(videoId);
    }

    private void toggleDescriptionComments() {
        if (description.getVisibility() == View.VISIBLE) {
            descriptionButton.setText(getString(R.string.more));
            //initializeComments();
        } else {
            descriptionButton.setText(getString(R.string.less));
            initializeDescription();
        }
    }

    private void initializeDescription() {
        description.setVisibility(View.VISIBLE);
        buttonBar.setVisibility(View.GONE);
        commentSectionContainer.setVisibility(View.GONE);
    }

//    private void initializeCommentsSection(String videoId) {
//        UserItem currentUser = LoggedInUser.getUser();
//        if (currentUser == null) {
//            initializeDescription();
//            return;
//        }
//
//        videoViewModel.getCommentsForVideo(videoId).observe(getViewLifecycleOwner(), new Observer<List<CommentItem>>() {
//            @Override
//            public void onChanged(List<CommentItem> comments) {
//                adapter = new CommentSection_Adapter(getContext(), new ArrayList<>(comments), VideoPlayerFragment.this, currentUser.getUserId());
//                recyclerView.setAdapter(adapter);
//                recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
//            }
//        });
//
//        description.setVisibility(View.GONE);
//        buttonBar.setVisibility(View.VISIBLE);
//        commentSectionContainer.setVisibility(View.VISIBLE);
//    }
//
//    private void showAddCommentDialog() {
//        UserItem currentUser = LoggedInUser.getUser();
//        if (currentUser == null) {
//            Toast.makeText(getContext(), "Please log in to add a comment.", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
//        LayoutInflater inflater = getLayoutInflater();
//        View dialogView = inflater.inflate(R.layout.add_comment_box, null);
//        builder.setView(dialogView);
//
//        AlertDialog dialog = builder.create();
//
//        EditText inputContent = dialogView.findViewById(R.id.comment_input);
//        Button positiveButton = dialogView.findViewById(R.id.positive_button);
//        Button negativeButton = dialogView.findViewById(R.id.negative_button);
//
//        positiveButton.setOnClickListener(v -> {
//            String content = inputContent.getText().toString();
//            String date = "Now";
//
//            if (!content.isEmpty()) {
//                String userId = LoggedInUser.getUser().getUserId(); // Assuming LoggedInUser provides the current user's details
//                String userName = LoggedInUser.getUser().getUserName();
//                int avatarResId = R.drawable.default_profile_picture; // Replace with the actual avatar resource ID
//
//                CommentItem newComment = new CommentItem(avatarResId, userId, userName, content, date);
//                videoViewModel.addCommentToVideo(videoId, newComment);
//                dialog.dismiss();
//            } else {
//                Toast.makeText(getContext(), "Please enter a comment.", Toast.LENGTH_SHORT).show();
//            }
//        });
//
//        negativeButton.setOnClickListener(v -> dialog.dismiss());
//
//        dialog.show();
//    }
//
//    private void likeClick() {
//        videoViewModel.likeVideo(videoId);
//        updateLikeButtons();
//    }
//
//    private void dislikeClick() {
//        videoViewModel.dislikeVideo(videoId);
//        updateLikeButtons();
//    }
//
//    private void updateLikeButtons() {
//        int likeDislike = videoViewModel.getLikeDislikeStatus(videoId);
//        if (likeDislike == VideoViewModel.LIKE) {
//            likeButton.setColorFilter(getResources().getColor(R.color.absolute_ofek_white));
//        } else {
//            likeButton.setColorFilter(getResources().getColor(R.color.crispy_orange_light));
//        }
//        if (likeDislike == VideoViewModel.DISLIKE) {
//            unlikeButton.setColorFilter(getResources().getColor(R.color.absolute_ofek_white));
//        } else {
//            unlikeButton.setColorFilter(getResources().getColor(R.color.crispy_orange_light));
//        }
//        updateLikesAndViewsCount();
//    }

    private void updateLikesAndViewsCount() {
        if (video != null) {
            likesTextView.setText(video.getLikes() + " likes");
            views.setText(video.getViews() + " views");
        }
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

    @Override
    public void onPause() {
        super.onPause();
        if (videoView != null && videoView.isPlaying()) {
            currentPosition = videoView.getCurrentPosition();
            videoView.pause();
        }
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
    }

    @Override
    public void onEditComment(int position) {
        // Implement the edit comment dialog
    }

    @Override
    public void onDeleteComment(int position) {
        // Implement the delete comment functionality
    }

    public void hideMediaController() {
        if (mediaController != null) {
            mediaController.hide();
        }
    }

}
