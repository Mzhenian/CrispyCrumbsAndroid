package com.example.crispycrumbs.view;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.EditText;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import android.widget.FrameLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.imageview.ShapeableImageView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.crispycrumbs.R;
import com.example.crispycrumbs.adapter.CommentSection_Adapter;
import com.example.crispycrumbs.adapter.VideoList_Adapter;
import com.example.crispycrumbs.dataUnit.CommentItem;
import com.example.crispycrumbs.dataUnit.PreviewVideoCard;
import com.example.crispycrumbs.dataUnit.UserItem;
import com.example.crispycrumbs.localDB.LoggedInUser;
import com.example.crispycrumbs.serverAPI.ServerAPI;
import com.example.crispycrumbs.viewModel.UserViewModel;
import com.example.crispycrumbs.viewModel.VideoPlayerViewModel;

import java.util.ArrayList;

public class VideoPlayerFragment extends Fragment implements CommentSection_Adapter.CommentActionListener {
    private static final String TAG = "VideoPlayerFragment";
    private static final String KEY_POSITION = "position";
    private VideoPlayerViewModel viewModel;
    private UserViewModel userViewModel;
    private MediaController mediaController;
    private int currentPosition = 0;
    private PreviewVideoCard video;

    // Declare UI components
    private View view;
    private VideoView videoView;
    private ProgressBar progressBar;
    private ShapeableImageView profilePicture;
    private TextView videoTitle, userName, videoDate, videoLikes, videoViews, txtVideoDescription;
    private Button  commentButton, shareButton;
    private TextView btnVideoDescription, btnShowComments;
    private ImageButton likeButton, unlikeButton;
    private RecyclerView commentSection, rvRecommendedVideos;
    private FrameLayout contentContainer;
    private CommentSection_Adapter CS_Adapter;
    private VideoList_Adapter VL_Adapter;
    private VideoList_Adapter.OnItemClickListener listener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_video_player, container, false);

        viewModel = new ViewModelProvider(this).get(VideoPlayerViewModel.class);
        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);

        // Initialize UI components
        videoView = view.findViewById(R.id.video_view);
        progressBar = view.findViewById(R.id.progressBar2);
        profilePicture = view.findViewById(R.id.profile_picture);
        videoTitle = view.findViewById(R.id.video_title);
        userName = view.findViewById(R.id.user_name);
        videoDate = view.findViewById(R.id.video_date);
        videoLikes = view.findViewById(R.id.video_likes);
        videoViews = view.findViewById(R.id.video_views);
        txtVideoDescription = view.findViewById(R.id.txt_video_description);
        btnVideoDescription = view.findViewById(R.id.btn_video_description);
        btnShowComments = view.findViewById(R.id.btn_show_comments);
        commentButton = view.findViewById(R.id.comment_button);
        shareButton = view.findViewById(R.id.share_button);
        likeButton = view.findViewById(R.id.like_button);
        unlikeButton = view.findViewById(R.id.unlike_button);
        contentContainer = view.findViewById(R.id.content_container);
        commentSection = view.findViewById(R.id.comment_section);
        rvRecommendedVideos = view.findViewById(R.id.rv_recommendedVideos);

        // Initially, show recommended videos, hide comments and description
        txtVideoDescription.setVisibility(View.GONE);
        commentSection.setVisibility(View.GONE);
        rvRecommendedVideos.setVisibility(View.VISIBLE);

        // Set click listener for show comments button
        btnShowComments.setOnClickListener(v -> {
            if (commentSection.getVisibility() == View.VISIBLE) {
                // Hide comments and show recommended videos
                commentSection.setVisibility(View.GONE);
                rvRecommendedVideos.setVisibility(View.VISIBLE);
                btnShowComments.setText("Show Comments");
            } else {
                // Show comments and hide recommended videos and description
                commentSection.setVisibility(View.VISIBLE);
                rvRecommendedVideos.setVisibility(View.GONE);
                txtVideoDescription.setVisibility(View.GONE);
                btnShowComments.setText("Show Recommended Videos");
            }
        });

        // Get videoId from arguments
        Bundle bundle = getArguments();
        if (bundle == null) {
            Toast.makeText(getContext(), "No video selected", Toast.LENGTH_SHORT).show();
            return view;
        }

        profilePicture.setOnClickListener(v -> {
            loadingMessage();
        });

        likeButton.setOnClickListener(v -> {
            if (LoggedInUser.getUser().getValue() == null) {
                MainPage.getInstance().showLoginSnackbar(view);
                return;
            }

            boolean isLiked = likeButton.isSelected();
            likeButton.setSelected(!isLiked); // Toggle selected state
            unlikeButton.setSelected(false); // Deselect unlike button
            viewModel.likeVideo();

        });

        unlikeButton.setOnClickListener(v -> {
            if (LoggedInUser.getUser().getValue() == null) {
                MainPage.getInstance().showLoginSnackbar(view);
                return;
            }

            boolean isDisliked = unlikeButton.isSelected();
            unlikeButton.setSelected(!isDisliked); // Toggle selected state
            likeButton.setSelected(false); // Deselect like button
            viewModel.dislikeVideo();

        });


        viewModel.setVideo(bundle.getString("videoId"));
        viewModel.getVideo().observe(getViewLifecycleOwner(), video -> {
            if (video == null) {
                Toast.makeText(getContext(), "Failed to load video", Toast.LENGTH_SHORT).show();
                return;
            }

            if (this.video != null && this.video.getVideoId().equals(video.getVideoId())) {
                this.video = video;
                updateVideoDetails();
                initializeCommentsSection(video.getComments());  // Update the comment section
                return;
            }
            this.video = video;

            initializeVideo(video);
            initializeVideoDetails(video);

            profilePicture.setOnClickListener(v -> {
                MainPage.getInstance().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new ProfileFragment(video.getUserId())).commit();
            });

            if (LoggedInUser.getUser().getValue() == null) {
                likeButton.setOnClickListener(v -> {
                    MainPage.getInstance().showLoginSnackbar(view);
                });

                unlikeButton.setOnClickListener(v -> {
                    MainPage.getInstance().showLoginSnackbar(view);
                });
            } else {
                likeButton.setOnClickListener(v -> {
                    viewModel.likeVideo();
                });

                unlikeButton.setOnClickListener(v -> {
                    viewModel.dislikeVideo();
                });
            }

            initializeCommentsSection(video.getComments());  // Update the comment section

            viewModel.loadRecommendedVideos(video.getVideoId());
            viewModel.getRecommendedVideos().observe(getViewLifecycleOwner(), videos -> {
                VL_Adapter.updateVideoList(videos);
            });

            // Log when UI refreshes with the updated video
            Log.d("LiveData update", "Refreshing UI for updated video data");
        });

        commentButton.setOnClickListener(v -> {
            showAddCommentDialog();
        });

        listener = video -> {
            VideoPlayerFragment videoPlayerFragment = new VideoPlayerFragment();
            Bundle args = new Bundle();
            args.putString("videoId", video.getVideoId());
            videoPlayerFragment.setArguments(args);

            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, videoPlayerFragment)
                    .addToBackStack(null)
                    .commit();
        };

        VL_Adapter = new VideoList_Adapter(MainPage.getInstance(), new ArrayList<>(), listener);
        rvRecommendedVideos.setAdapter(VL_Adapter);
        rvRecommendedVideos.setLayoutManager(new LinearLayoutManager(MainPage.getInstance()));

        // Handle description toggle
        btnVideoDescription.setOnClickListener(v -> {
            if (txtVideoDescription.getVisibility() == View.VISIBLE) {
                // Hide description and show recommended videos
                txtVideoDescription.setVisibility(View.GONE);
                rvRecommendedVideos.setVisibility(View.VISIBLE);
                commentSection.setVisibility(View.GONE);
                btnShowComments.setVisibility(View.VISIBLE); // Show the comments toggle button
                btnVideoDescription.setText(getString(R.string.more));
            } else {
                // Show description and hide recommended videos and comments
                txtVideoDescription.setVisibility(View.VISIBLE);
                rvRecommendedVideos.setVisibility(View.GONE);
                commentSection.setVisibility(View.GONE);
                btnShowComments.setVisibility(View.GONE); // Hide the comments toggle button when showing description
                btnVideoDescription.setText(getString(R.string.less));
            }
        });

        return view;
    }

    private void loadingMessage() {
        Toast.makeText(getContext(), "Loading video and its data...", Toast.LENGTH_SHORT).show();
    }

    private void initializeVideo(PreviewVideoCard video) {
        String videoUrl = ServerAPI.getInstance().constructUrl(video.getVideoFile());
        Uri videoUri = Uri.parse(videoUrl);

        videoView.setVideoURI(videoUri);
        mediaController = new MediaController(getContext());
        mediaController.setAnchorView(videoView);
        videoView.setMediaController(mediaController);

        videoView.setOnPreparedListener(mp -> {
            progressBar.setVisibility(View.GONE);

            // Adjust VideoView dimensions dynamically
            int videoWidth = mp.getVideoWidth();
            int videoHeight = mp.getVideoHeight();
            float videoRatio = (float) videoWidth / videoHeight;

            int screenWidth = getResources().getDisplayMetrics().widthPixels;
            int adjustedHeight = (int) (screenWidth / videoRatio);

            ViewGroup.LayoutParams layoutParams = videoView.getLayoutParams();
            layoutParams.width = screenWidth;
            layoutParams.height = adjustedHeight;
            videoView.setLayoutParams(layoutParams);

            videoView.start();
            viewModel.incrementVideoViews(); // Increment view count
        });

        videoView.setOnErrorListener((mp, what, extra) -> {
            Toast.makeText(getContext(), "Error loading video", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE);
            return true;
        });
    }

    private void initializeVideoDetails(PreviewVideoCard video) {
        videoTitle.setText(video.getTitle());
        txtVideoDescription.setText(video.getDescription());

        updateVideoDetails();

        // Observe user profile data
        userViewModel.getUser(video.getUserId()).observe(getViewLifecycleOwner(), uploader -> {
            if (uploader != null) {
                String userProfileUrl = ServerAPI.getInstance().constructUrl(uploader.getProfilePhoto());
                Glide.with(VideoPlayerFragment.this)
                        .load(userProfileUrl)
                        .placeholder(R.drawable.default_profile_picture) // Optional: Add a placeholder
                        .skipMemoryCache(true)
                        .into(profilePicture);
                userName.setText(uploader.getUserName());
            } else {
                profilePicture.setImageResource(R.drawable.default_profile_picture);
                userName.setText(R.string.deleted_user);
                Log.e(TAG, "Uploader not found");
            }
        });

        String formattedDate = viewModel.formatDate(video.getUploadDate());
        videoDate.setText(formattedDate);

        shareButton.setOnClickListener(v -> showShareMenu());
    }

    private void initializeCommentsSection(ArrayList<CommentItem> comments) {
        Log.d("Comment update", "Initializing comment section. Comments size: " + (comments != null ? comments.size() : 0));

        if (comments == null || comments.isEmpty()) {
            // No comments to display
            // Do not change visibility here
            return;
        }

        UserItem currentUser = LoggedInUser.getUser().getValue();
        String currentUserId = currentUser != null ? currentUser.getUserId() : null;

        Log.d("Comment update", "Setting up adapter with comments: " + comments.size());
        CS_Adapter = new CommentSection_Adapter(getContext(), comments, this, currentUserId);
        commentSection.setAdapter(CS_Adapter);
        commentSection.setLayoutManager(new LinearLayoutManager(getContext()));
        // Do not change visibility here

        Log.d("Comment update", "End of Initialize comment section in Fragment");
    }

    private void updateVideoDetails() {
        if (video == null) return;

        videoLikes.setText(video.getLikes() + " likes");
        videoViews.setText(video.getViews() + " views");

        // Reset to default state
        likeButton.setSelected(false);
        unlikeButton.setSelected(false);

        UserItem currentUser = LoggedInUser.getUser().getValue();
        if (currentUser != null) {
            if (video.getLikedBy().contains(currentUser.getUserId())) {
                likeButton.setSelected(true); // Mark as liked
            } else if (video.getDislikedBy().contains(currentUser.getUserId())) {
                unlikeButton.setSelected(true); // Mark as disliked
            }
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
        Button shareX = dialogView.findViewById(R.id.share_twitter);
        Button shareWhatsapp = dialogView.findViewById(R.id.share_whatsapp);

        String CrispyCrumbsLink = "https://github.com/Mzhenian/CrispyCrumbsAndroid";
        ClipboardManager clipboard = (ClipboardManager) getContext().getSystemService(MainPage.CLIPBOARD_SERVICE);

        shareEmail.setOnClickListener(v -> {
            Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                    "mailto", "", null));
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Check out CrispyCrumbs!");
            emailIntent.putExtra(Intent.EXTRA_TEXT, CrispyCrumbsLink);
            startActivity(Intent.createChooser(emailIntent, "Send email..."));
            dialog.dismiss();
        });

        shareFacebook.setOnClickListener(v -> {
            ClipData clip = ClipData.newPlainText("CrispyCrumbs Link", CrispyCrumbsLink);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(getContext(), "Link copied to clipboard", Toast.LENGTH_SHORT).show();

            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.facebook.com/"));
            startActivity(browserIntent);

            dialog.dismiss();
        });

        shareX.setOnClickListener(v -> {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://twitter.com/intent/tweet?url=" + CrispyCrumbsLink + "&text=Check this out!"));
            startActivity(browserIntent);
            dialog.dismiss();
        });

        shareWhatsapp.setOnClickListener(v -> {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/?text=" + CrispyCrumbsLink));
            startActivity(browserIntent);
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
    public void onDestroyView() {
        super.onDestroyView();
        viewModel.getVideo().removeObservers(getViewLifecycleOwner());
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_POSITION, currentPosition);
    }

    private void showAddCommentDialog() {
        UserItem currentUser = LoggedInUser.getUser().getValue();
        if (currentUser == null) {
            MainPage.getInstance().showLoginSnackbar(view);
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

            if (!content.isEmpty()) {
                viewModel.insertComment(viewModel.getVideo(), content);
                dialog.dismiss();
            } else {
                Toast.makeText(getContext(), "Please enter a comment.", Toast.LENGTH_SHORT).show();
            }
        });

        negativeButton.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    @Override
    public void onEditComment(int position) {
        CommentItem comment = video.getComments().get(position);
        UserItem currentUser = LoggedInUser.getUser().getValue();

        if (currentUser != null && comment.getUserId().equals(currentUser.getUserId())) {
            // Open the dialog to edit the comment
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            LayoutInflater inflater = getLayoutInflater();
            View dialogView = inflater.inflate(R.layout.add_comment_box, null);
            builder.setView(dialogView);

            AlertDialog dialog = builder.create();

            EditText inputContent = dialogView.findViewById(R.id.comment_input);
            inputContent.setText(comment.getComment()); // Pre-fill with the current comment text

            Button positiveButton = dialogView.findViewById(R.id.positive_button);
            Button negativeButton = dialogView.findViewById(R.id.negative_button);

            positiveButton.setOnClickListener(v -> {
                String newContent = inputContent.getText().toString();

                if (!newContent.isEmpty()) {
                    viewModel.editComment(viewModel.getVideo(), comment.getId(), newContent);
                    dialog.dismiss();
                } else {
                    Toast.makeText(getContext(), "Please enter a comment.", Toast.LENGTH_SHORT).show();
                }
            });

            negativeButton.setOnClickListener(v -> dialog.dismiss());

            dialog.show();
        } else {
            Toast.makeText(getContext(), "You can only edit your own comments", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDeleteComment(int position) {
        Log.d("Comment deletion", "Attempting to delete comment at position: " + position + ". Total comments: " + CS_Adapter.getItemCount());
        CommentItem comment = video.getComments().get(position);

        UserItem currentUser = LoggedInUser.getUser().getValue();
        if (currentUser != null && comment.getUserId().equals(currentUser.getUserId())) {
            // Ensure only the user who made the comment can delete it

            if (position >= 0 && position < video.getComments().size()) {
                viewModel.deleteComment(viewModel.getVideo(), comment.getId(), currentUser.getUserId()); // Use String commentId
            } else {
                Log.e("Comment deletion", "Attempted to delete a comment at invalid position: " + position);
            }
        } else {
            Toast.makeText(getContext(), "You can only delete your own comments", Toast.LENGTH_SHORT).show();
        }
    }

    public void hideMediaController() {
        if (mediaController != null) {
            mediaController.hide();
        }
    }
}
