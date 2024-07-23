package com.example.crispycrumbs.ui;

import static android.content.Intent.getIntent;
import static com.example.crispycrumbs.R.id.comment_section_container;
import static com.example.crispycrumbs.model.DataManager.getUriFromResOrFile;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.res.Configuration;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.crispycrumbs.R;
import com.example.crispycrumbs.adapter.CommentSection_Adapter;
import com.example.crispycrumbs.data.CommentItem;
import com.example.crispycrumbs.data.LoggedInUser;
import com.example.crispycrumbs.data.PreviewVideoCard;
import com.example.crispycrumbs.data.UserItem;
import com.example.crispycrumbs.model.DataManager;

import java.util.ArrayList;

public class VideoPlayerFragment extends Fragment implements CommentSection_Adapter.CommentActionListener {
    private static final String TAG = "VideoPlayerFragment";
    private static final String KEY_POSITION = "position";
    private static final String KEY_COMMENTS = "comments";
    private MediaController mediaController;
    private VideoView videoView;
    private ArrayList<CommentItem> commentItemArrayList = new ArrayList<>();
    private int currentPosition = 0;
    private String videoId, uploaderId;
    private PreviewVideoCard video;
    private ImageView profilePicture;
    private TextView userNameTextView;
    private TextView description, descriptionButton;
    private Button shareButton, commentButton;
    private ConstraintLayout buttonBar, commentSectionContainer;
    private RecyclerView recyclerView;
    private CommentSection_Adapter adapter;
    private ImageButton likeButton;
    private ImageButton unlikeButton;
    private TextView likesTextView;
    private TextView views;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_video_player, container, false);

        //get video to to initialize
        Bundle bundle = getArguments();
        if (bundle == null) {
            Toast.makeText(getContext(), "Please choose a video", Toast.LENGTH_SHORT).show();
            if (getParentFragmentManager().getBackStackEntryCount() > 0) {
                getParentFragmentManager().popBackStack();
            } else {
                MainPage.getInstance().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HomeFragment()).commit();
            }
        }
        videoId = bundle.getString("videoId");
        video = DataManager.getInstance().getVideoById(videoId);
        String videoFile = video.getVideoFile();
        uploaderId = video.getUserId();
        DataManager dataManager = DataManager.getInstance();

        recyclerView = view.findViewById(R.id.comment_section);
        likeButton = view.findViewById(R.id.like_button);
        unlikeButton = view.findViewById(R.id.unlike_button);
        likesTextView = view.findViewById(R.id.video_likes);
        views = view.findViewById(R.id.video_views);
        shareButton = view.findViewById(R.id.share_button);
        videoView = view.findViewById(R.id.video_view);
        profilePicture = view.findViewById(R.id.profile_picture);
        commentButton = view.findViewById(R.id.comment_button);
        userNameTextView = view.findViewById(R.id.user_name);
        commentItemArrayList = dataManager.getCommentsForVideo(videoId);
        TextView titleTextView = view.findViewById(R.id.video_title);
        TextView dateTextView = view.findViewById(R.id.video_date);
        description = view.findViewById(R.id.txt_video_description);
        buttonBar = view.findViewById(R.id.button_bar);
        commentSectionContainer = view.findViewById(comment_section_container);
        descriptionButton = view.findViewById(R.id.btn_video_description);

        titleTextView.setText(video.getTitle());
        dateTextView.setText(video.getUploadDate());
        description.setText(video.getDescription());

        profilePicture.setOnClickListener(v -> {
            MainPage.getInstance().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new ProfileFragment(uploaderId)).commit();
        });
        commentButton.setOnClickListener(v -> showAddCommentDialog());
        shareButton.setOnClickListener(v -> showShareMenu());
        likeButton.setOnClickListener(v -> likeClick());
        unlikeButton.setOnClickListener(v -> dislikeClick());


        initializeVideo(savedInstanceState);

        UserItem currentUser = LoggedInUser.getUser();
        if (currentUser != null) {
            descriptionButton.setOnClickListener(v -> toggleDescriptionComments());
            initializeComments();
        } else {
            descriptionButton.setVisibility(View.GONE);
            initializeDescription();
        }

        // Update user info for the video
        UserItem uploader = dataManager.getUserById(uploaderId);
        if (uploader != null) {
            profilePicture.setImageURI(getUriFromResOrFile(uploader.getProfilePhoto()));
            userNameTextView.setText(uploader.getUserName());
        } else {
            profilePicture.setImageResource(R.drawable.default_profile_picture);
            userNameTextView.setText("[deleted user]");
            Log.e(TAG, "User not found");
        }

        video.setViews(video.getViews() + 1);

        updateLikesAndViewsCount();
        updateLikeButtons();

        return view;
    }

    private void initializeVideo(Bundle savedInstanceState) {
        String videoFile = video.getVideoFile();
        Uri videoUri = DataManager.getUriFromResOrFile(videoFile);
        videoView.setVideoURI(videoUri);
        videoView.start();

//        mediaController = new CustomMediaController(getContext());
        mediaController = new MediaController(getContext());
        mediaController.setAnchorView(videoView);
        videoView.setMediaController(mediaController);

        if (savedInstanceState != null) {
            currentPosition = savedInstanceState.getInt(KEY_POSITION, 0);
            commentItemArrayList = savedInstanceState.getParcelableArrayList(KEY_COMMENTS);
            videoView.seekTo(currentPosition);
        }

        videoView.start();
    }

    private void toggleDescriptionComments() {
        if (description.getVisibility() == View.VISIBLE) {
            descriptionButton.setText(getString(R.string.more));
            initializeComments();
        } else {
            descriptionButton.setText(getString(R.string.less));
            initializeDescription();
        }
    }

    private void initializeDescription() {
        description.setText(video.getDescription());

        description.setVisibility(View.VISIBLE);
        buttonBar.setVisibility(View.GONE);
        commentSectionContainer.setVisibility(View.GONE);

    }

    private void initializeComments() {
        UserItem currentUser = LoggedInUser.getUser();
        if (currentUser == null) {
            initializeDescription();
        }
        String currentUserId = currentUser.getUserId();
        adapter = new CommentSection_Adapter(getContext(), commentItemArrayList, this, currentUserId);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        description.setVisibility(View.GONE);
        buttonBar.setVisibility(View.VISIBLE);
        commentSectionContainer.setVisibility(View.VISIBLE);
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

        inputContent.setText(commentItemArrayList.get(position).getComment());

        positiveButton.setOnClickListener(v -> {
            String content = inputContent.getText().toString();

            if (!content.isEmpty()) {
                UserItem currentUser = LoggedInUser.getUser();
                CommentItem updatedComment = new CommentItem(R.drawable.small_logo, currentUser.getUserId(), currentUser.getUserName(), content, commentItemArrayList.get(position).getDate());
                commentItemArrayList.set(position, updatedComment);
                adapter.notifyItemChanged(position);

                DataManager dataManager = DataManager.getInstance();
                dataManager.addCommentToVideo(videoId, updatedComment);

                dialog.dismiss();
            } else {
                Toast.makeText(getContext(), "Please enter a comment.", Toast.LENGTH_SHORT).show();
            }
        });

        negativeButton.setOnClickListener(v -> dialog.dismiss());

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
        try {
            super.onResume();
        } catch (ClassCastException e) {
            // Log the exception and continue
            Log.e("VideoPlayer", "Caught ClassCastException", e);
        }
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

    @Override
    public void onEditComment(int position) {
        showEditCommentDialog(position);
    }

    @Override
    public void onDeleteComment(int position) {
        adapter.removeComment(position);
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

    private void updateLikesAndViewsCount() {
        if (video != null) {
            likesTextView.setText(DataManager.getInstance().getLikesCount(videoId) + " likes");
            views.setText(video.getViews() + " views");
        }
    }

    private void likeClick() {
        updateLikeButtons(MainPage.getDataManager().likeClick(videoId));
    }

    private void dislikeClick() {
        updateLikeButtons(MainPage.getDataManager().dislikeClick(videoId));
    }

    private void updateLikeButtons(int likeDislike) {
        if (likeDislike == DataManager.LIKE) {
            likeButton.setColorFilter(getResources().getColor(R.color.absolute_ofek_white));
//            likeButton.setBackgroundColor(getResources().getColor(R.color.crispy_orange_light));
        } else {
            likeButton.setColorFilter(getResources().getColor(R.color.crispy_orange_light));
//            likeButton.setBackgroundColor(getResources().getColor(R.color.crispy_orange));
        }
        if (likeDislike == DataManager.DISLIKE) {
            unlikeButton.setColorFilter(getResources().getColor(R.color.absolute_ofek_white));
//            unlikeButton.setBackgroundColor(getResources().getColor(R.color.crispy_orange_light));
        } else {
            unlikeButton.setColorFilter(getResources().getColor(R.color.crispy_orange_light));
//            unlikeButton.setBackgroundColor(getResources().getColor(R.color.crispy_orange));
        }
        updateLikesAndViewsCount();
    }

    private void updateLikeButtons() {
        int likeDislike = MainPage.getDataManager().getLikeDislike(videoId);
        updateLikeButtons(likeDislike);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        // Checks the orientation of the screen
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            ViewGroup.LayoutParams params = videoView.getLayoutParams();
            params.width = ViewGroup.LayoutParams.MATCH_PARENT;
            params.height = ViewGroup.LayoutParams.MATCH_PARENT;
            videoView.setLayoutParams(params);
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            ViewGroup.LayoutParams params = videoView.getLayoutParams();
            params.width = ViewGroup.LayoutParams.MATCH_PARENT;
            params.height = ViewGroup.LayoutParams.WRAP_CONTENT; // or whatever size you need
            videoView.setLayoutParams(params);
        }
    }

    public void hideMediaController() {
        if (mediaController != null) {
            mediaController.hide();
        }
    }
}
