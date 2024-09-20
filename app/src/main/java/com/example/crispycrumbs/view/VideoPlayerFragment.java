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
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
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
import com.example.crispycrumbs.viewModel.VideoPlayerViewModel;

import java.util.ArrayList;


public class VideoPlayerFragment extends Fragment implements CommentSection_Adapter.CommentActionListener {
    private static final String TAG = "VideoPlayerFragment";
    private static final String KEY_POSITION = "position";
    private static final String KEY_COMMENTS = "comments";
    private final ArrayList<CommentItem> commentItemArrayList = new ArrayList<>();
    TextView date;
    private MediaController mediaController;
    private VideoView videoView;
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
    private VideoPlayerViewModel videoPlayerViewModel;
    private UserViewModel userViewModel;
    private LiveData<PreviewVideoCard> videoCardLiveData;
    private ProgressBar progressBar;
    private View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_video_player, container, false);

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
        progressBar = view.findViewById(R.id.progressBar2);
        videoPlayerViewModel = new ViewModelProvider(this).get(VideoPlayerViewModel.class);
        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);
        date = view.findViewById(R.id.video_date);


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
            loadingMessage();
        });

        unlikeButton.setOnClickListener(v -> {
            loadingMessage();
        });


        videoPlayerViewModel.setVideo(bundle.getString("videoId"));
        videoCardLiveData = videoPlayerViewModel.getVideo();
        videoCardLiveData.observe(getViewLifecycleOwner(), video -> {
            if (null == video) {
                Toast.makeText(getContext(), "Failed to load video", Toast.LENGTH_SHORT).show();
                return;
            }

            if (null != this.video && this.video.getVideoId().equals(video.getVideoId())) {
                this.video = video;
                updateVideoDetails();
                initializeCommentsSection(video.getComments());  // Update the comment section
                return;
            }
            this.video = video;
            videoId = video.getVideoId();

            initializeVideo(video);
            initializeVideoDetails(video);

            profilePicture.setOnClickListener(v -> {
                MainPage.getInstance().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new ProfileFragment(video.getUserId())).commit();
            });

            if (null == LoggedInUser.getUser().getValue()) {
                likeButton.setOnClickListener(v -> {
                    MainPage.getInstance().showLoginSnackbar(view);
                });

                unlikeButton.setOnClickListener(v -> {
                    MainPage.getInstance().showLoginSnackbar(view);
                });
            } else {
                likeButton.setOnClickListener(v -> {
                    videoPlayerViewModel.likeVideo();
                });

                unlikeButton.setOnClickListener(v -> {
                    videoPlayerViewModel.dislikeVideo();
                });
            }

            initializeCommentsSection(video.getComments());  // Update the comment section

            // Log when UI refreshes with the updated video
            Log.d("LiveData update", "Refreshing UI for updated video data");
        });


        Log.e("Comment update", "End of oncreateview in Fragment");
        commentButton.setOnClickListener(v -> {
            showAddCommentDialog();
        });

        return view;
    }

    private void loadingMessage() {
        Toast.makeText(getContext(), "Loading video and his data...", Toast.LENGTH_SHORT).show();
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
            videoView.start();
            videoPlayerViewModel.incrementVideoViews(); // Trigger view count increment
        });
    }

    private void initializeVideoDetails(PreviewVideoCard video) {
        titleTextView.setText(video.getTitle());
        description.setText(video.getDescription());

        updateVideoDetails();


        // Observe user profile data
        userViewModel.getUser(video.getUserId()).observe(getViewLifecycleOwner(), uploader -> {
            if (uploader != null) {
                String userProfileUrl = ServerAPI.getInstance().constructUrl(uploader.getProfilePhoto());
                Glide.with(VideoPlayerFragment.this)
                        .load(userProfileUrl)
                        .placeholder(R.drawable.default_profile_picture) // Optional: Add a placeholder
                        .into(profilePicture);
                userNameTextView.setText(uploader.getUserName());
            } else {
                profilePicture.setImageResource(R.drawable.default_profile_picture);
                userNameTextView.setText(R.string.deleted_user);
                Log.e(TAG, "Uploader not found");
            }
        });

        String formattedDate = videoPlayerViewModel.formatDate(video.getUploadDate());
        date.setText(formattedDate);
        descriptionButton.setOnClickListener(v -> toggleDescriptionComments());
        shareButton.setOnClickListener(v -> showShareMenu());


    }

    private void initializeCommentsSection(ArrayList<CommentItem> comments) {
        Log.d("Comment update", "Initializing comment section. Comments size: " + (comments != null ? comments.size() : 0));

        if (comments == null || comments.isEmpty()) {
            commentSectionContainer.setVisibility(View.GONE);
            return;
        }

        if (adapter == null) {
            UserItem currentUser = LoggedInUser.getUser().getValue();
            String currentUserId = currentUser != null ? currentUser.getUserId() : null;

            Log.d("Comment update", "Setting up adapter with comments: " + comments.size());
            adapter = new CommentSection_Adapter(getContext(), comments, this, currentUserId);
            recyclerView.setAdapter(adapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        } else {
            Log.d("Comment update", "Updating existing adapter with new comments: " + comments.size());
            adapter.updateComments(comments);
        }

        commentSectionContainer.setVisibility(View.VISIBLE);
        Log.e("Comment update", "End of Initialize comment section in Fragment");
    }

    private void toggleDescriptionComments() {
        if (description.getVisibility() == View.VISIBLE) {
            descriptionButton.setText(getString(R.string.more));
            initializeCommentsSection(video.getComments());
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

    private void updateVideoDetails() {
        if (null == video) {
            return;
        }
        Log.d(TAG, "Updating UI with likes: " + video.getLikes() + " and dislikes: " + video.getDislikes());
        likesTextView.setText(video.getLikes() + " likes");
        // Assuming you have a dislikesTextView for dislikes
        // dislikesTextView.setText(video.getDislikes() + " dislikes");
        views.setText(video.getViews() + " views");

        likeButton.setSelected(false);
        likeButton.setColorFilter(getResources().getColor(R.color.crispy_orange_light));
//            likeButton.setBackgroundColor(getResources().getColor(R.color.crispy_orange));
        unlikeButton.setSelected(false);
        unlikeButton.setColorFilter(getResources().getColor(R.color.crispy_orange_light));

        UserItem currentUser = LoggedInUser.getUser().getValue();
        if (currentUser == null) {
            // No user is logged in, so clear the selection like buttons  states
        } else if (video.getLikedBy().contains(currentUser.getUserId())) {
            likeButton.setSelected(true);
            likeButton.setColorFilter(getResources().getColor(R.color.absolute_ofek_white));
//            likeButton.setBackgroundColor(getResources().getColor(R.color.crispy_orange_light));
        } else if (video.getDislikedBy().contains(currentUser.getUserId())) {
            unlikeButton.setSelected(true);
            unlikeButton.setColorFilter(getResources().getColor(R.color.absolute_ofek_white));
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
    public void onDestroyView() {
        super.onDestroyView();
        videoCardLiveData.removeObservers(getViewLifecycleOwner());
        videoView = null;
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
                videoPlayerViewModel.insertComment(videoCardLiveData, content);
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
        // Implement the edit comment dialog
    }

    @Override
    public void onDeleteComment(int position) {
        Log.d("Comment deletion", "Attempting to delete comment at position: " + position + ". Total comments: " + adapter.getItemCount());
        CommentItem comment = video.getComments().get(position);

        UserItem currentUser = LoggedInUser.getUser().getValue();
        if (currentUser != null && comment.getUserId().equals(currentUser.getUserId())) {
            // Ensure only the user who made the comment can delete it

            if (position >= 0 && position < video.getComments().size()) {
                adapter.removeComment(position);
                videoPlayerViewModel.deleteComment(video.getVideoId(), comment.getId(), currentUser.getUserId()); // Use String commentId
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
