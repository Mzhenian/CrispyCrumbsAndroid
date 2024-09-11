package com.example.crispycrumbs.view;

import android.app.AlertDialog;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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

import com.bumptech.glide.Glide;
import com.example.crispycrumbs.R;
import com.example.crispycrumbs.adapter.CommentSection_Adapter;
import com.example.crispycrumbs.dataUnit.CommentItem;
import com.example.crispycrumbs.dataUnit.PreviewVideoCard;
import com.example.crispycrumbs.dataUnit.UserItem;
import com.example.crispycrumbs.localDB.LoggedInUser;
import com.example.crispycrumbs.serverAPI.ServerAPI;
import com.example.crispycrumbs.viewModel.UserViewModel;
import com.example.crispycrumbs.viewModel.VideoViewModel;

import java.util.ArrayList;


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

        profilePicture.setOnClickListener(v -> {
            MainPage.getInstance().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new ProfileFragment(video.getUserId())).commit();
        });

        likeButton.setOnClickListener(v -> {
            if (video == null) {
                return;
            }
            if (LoggedInUser.getUser() == null) {
                Toast.makeText(getContext(), "Please log in to like videos.", Toast.LENGTH_SHORT).show();
                return;
            }
            //todo move the logic up to the view model
//            // User already liked the video, so remove the like
//            if (LoggedInUser.getUser().hasLiked(video.getVideoId())) {
//                video.setLikes(video.getLikes() - 1);
//                LoggedInUser.getUser().removeLike(video.getVideoId());
//            } else { // User has not liked the video, so add the like
//                video.setLikes(video.getLikes() + 1);
//                LoggedInUser.getUser().likeVideo(video.getVideoId());
//                if (LoggedInUser.getUser().hasDisliked(video.getVideoId())) {
//                    video.setDislikes(video.getDislikes() - 1);
//                    LoggedInUser.getUser().removeDislike(video.getVideoId());
//                }
//            }
            videoViewModel.likeVideo(video.getVideoId(), LoggedInUser.getUser().getValue().getUserId()); // Then call the server
            //todo migrate to the livedata and delete updateLikesAndViewsCount
            updateLikesAndViewsCount(); // Update UI immediately
        });

        unlikeButton.setOnClickListener(v -> {
            if (video != null) {
                if (LoggedInUser.getUser() != null) { // Check if user is logged in
                    if (LoggedInUser.getUser().getValue().hasDisliked(video.getVideoId())) {
                        // User already disliked the video, so remove the dislike
                        video.setDislikes(video.getDislikes() - 1);
                        LoggedInUser.getUser().getValue().removeDislike(video.getVideoId());
                    } else {
                        // User has not disliked the video, so add the dislike
                        video.setDislikes(video.getDislikes() + 1);
                        LoggedInUser.getUser().getValue().dislikeVideo(video.getVideoId());
                        if (LoggedInUser.getUser().getValue().hasLiked(video.getVideoId())) {
                            video.setLikes(video.getLikes() - 1);
                            LoggedInUser.getUser().getValue().removeLike(video.getVideoId());
                        }
                    }
                    updateLikesAndViewsCount(); // Update UI immediately
                    videoViewModel.dislikeVideo(video.getVideoId(), LoggedInUser.getUser().getValue().getUserId()); // Then call the server
                } else {
                    Toast.makeText(getContext(), "Please log in to dislike videos.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        videoViewModel.getVideo(videoId).observe(getViewLifecycleOwner(), new Observer<PreviewVideoCard>() {
            @Override
            public void onChanged(PreviewVideoCard videoData) {
                if (videoData != null) {
                    video = videoData;
                    initializeVideo(video);
                    initializeVideoDetails(video);
                    updateLikesAndViewsCount(); // Update UI with new view and like counts
                    updateLikeDislikeButtons(); // Update button states
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

        videoView.setOnPreparedListener(mp -> {
            videoView.start();
            incrementViewCount(video.getVideoId()); // Trigger view count increment
        });
    }

    private void initializeVideoDetails(PreviewVideoCard video) {
        titleTextView.setText(video.getTitle());
        description.setText(video.getDescription());
        likesTextView.setText(video.getLikes() + " likes");
        views.setText(video.getViews() + " views");

        // Observe user profile data
        userViewModel.getUser(video.getUserId()).observe(getViewLifecycleOwner(), new Observer<UserItem>() {
            @Override
            public void onChanged(UserItem uploader) {
                if (uploader != null) {
                    String userProfileUrl = ServerAPI.getInstance().constructUrl(uploader.getProfilePhoto());
                    Glide.with(VideoPlayerFragment.this)
                            .load(userProfileUrl)
                            .placeholder(R.drawable.default_profile_picture) // Optional: Add a placeholder
                            .into(profilePicture);
                    userNameTextView.setText(uploader.getUserName());
                } else {
                    profilePicture.setImageResource(R.drawable.default_profile_picture);
                    userNameTextView.setText("[deleted user]");
                    Log.e(TAG, "User not found");
                }
            }
        });

        // Load video file URL into VideoView
        String videoUrl = ServerAPI.getInstance().constructUrl(video.getVideoFile());
        Uri videoUri = Uri.parse(videoUrl);
        videoView.setVideoURI(videoUri);

        mediaController = new MediaController(getContext());
        mediaController.setAnchorView(videoView);
        videoView.setMediaController(mediaController);

        videoView.start();

        descriptionButton.setOnClickListener(v -> toggleDescriptionComments());

        // Initialize the comments section
        initializeCommentsSection(video.getComments());
    }

    private void initializeCommentsSection(ArrayList<CommentItem> comments) {
        if (comments == null || comments.isEmpty()) {
            commentSectionContainer.setVisibility(View.GONE);
            return;
        }

        UserItem currentUser = LoggedInUser.getUser().getValue();
        String currentUserId = currentUser != null ? currentUser.getUserId() : null;

        adapter = new CommentSection_Adapter(getContext(), comments, this, currentUserId);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        description.setVisibility(View.GONE);
        buttonBar.setVisibility(View.VISIBLE);
        commentSectionContainer.setVisibility(View.VISIBLE);
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

    private void updateLikesAndViewsCount() {
        if (video != null) {
            Log.d(TAG, "Updating UI with likes: " + video.getLikes() + " and dislikes: " + video.getDislikes());
            likesTextView.setText(video.getLikes() + " likes");
            // Assuming you have a dislikesTextView for dislikes
            // dislikesTextView.setText(video.getDislikes() + " dislikes");
            views.setText(video.getViews() + " views");

            // Log current button states
            Log.d(TAG, "Like button selected: " + likeButton.isSelected() + ", Unlike button selected: " + unlikeButton.isSelected());
        }
    }

    private void updateLikeDislikeButtons() {
        UserItem currentUser = LoggedInUser.getUser().getValue();

        if (currentUser == null) {
            // No user is logged in, so clear the selection states
            likeButton.setSelected(false);
            unlikeButton.setSelected(false);
            return;
        }

        String userId = currentUser.getUserId();

        if (video.getLikedBy().contains(userId)) {
            likeButton.setSelected(true);
            unlikeButton.setSelected(false);
        } else if (video.getDislikedBy().contains(userId)) {
            likeButton.setSelected(false);
            unlikeButton.setSelected(true);
        } else {
            likeButton.setSelected(false);
            unlikeButton.setSelected(false);
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

    private void incrementViewCount(String videoId) {
        if (videoId != null && !videoId.isEmpty()) {
            videoViewModel.incrementVideoViews(videoId);
        } else {
            Log.e(TAG, "Invalid videoId for incrementing views");
        }
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
