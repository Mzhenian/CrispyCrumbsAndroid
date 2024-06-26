package com.example.crispycrumbs.ui;

import static com.example.crispycrumbs.model.DataManager.getUriFromResOrFile;

import android.app.AlertDialog;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
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
import com.example.crispycrumbs.data.PreviewVideoCard;
import com.example.crispycrumbs.model.CustomMediaController;
import com.example.crispycrumbs.model.DataManager;
import com.example.crispycrumbs.R;

import java.util.ArrayList;

public class VideoPlayerFragment extends Fragment implements CommentSection_Adapter.CommentActionListener {
    private static final String TAG = "VideoPlayerFragment";
    private CustomMediaController mediaController;
    private VideoView videoView;
    private ArrayList<CommentItem> commentItemArrayList = new ArrayList<>();
    private int currentPosition = 0;
    private String videoId, uploaderId;
    private PreviewVideoCard video;

    private RecyclerView recyclerView;
    private CommentSection_Adapter adapter;

    private ImageButton likeButton;
    private ImageButton unlikeButton;
    private TextView likesTextView;
    private TextView dislikesTextView;
    private boolean isLiked = false;
    private boolean isUnliked = false;

    private static final String KEY_POSITION = "position";
    private static final String KEY_COMMENTS = "comments";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_video_player, container, false);

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

        recyclerView = view.findViewById(R.id.comment_section);
        likeButton = view.findViewById(R.id.like_button);
        unlikeButton = view.findViewById(R.id.unlike_button);
        likesTextView = view.findViewById(R.id.video_likes);
        dislikesTextView = view.findViewById(R.id.video_dislikes);
        Button shareButton = view.findViewById(R.id.share_button);
        videoView = view.findViewById(R.id.video_view);

        Button commentButton = view.findViewById(R.id.comment_button);
        commentButton.setOnClickListener(v -> showAddCommentDialog());

        shareButton.setOnClickListener(v -> showShareMenu());

        likeButton.setOnClickListener(v -> handleLikeButtonClick());
        unlikeButton.setOnClickListener(v -> handleUnlikeButtonClick());

        String videoTitle =video.getTitle();
        String videoDescription = video.getDescription();
        String videoFile = video.getVideoFile();
        uploaderId = video.getUserId();

        DataManager dataManager = DataManager.getInstance();
        video = dataManager.getVideoById(videoId);
        commentItemArrayList = dataManager.getCommentsForVideo(videoId);

        TextView titleTextView = view.findViewById(R.id.video_title);
        titleTextView.setText(videoTitle);

        TextView dateTextView = view.findViewById(R.id.video_date);
        dateTextView.setText(video.getUploadDate());

        if (videoFile != null && !videoFile.isEmpty()) {
            try {
                int videoResId = getResources().getIdentifier(videoFile, "raw", getContext().getPackageName());
                String videoPath = "android.resource://" + getContext().getPackageName() + "/" + videoResId;
                videoView.setVideoURI(Uri.parse(videoPath));
            } catch (Exception e) {
                Log.e(TAG, "Failed to parse video URI: " + videoFile, e);
                Toast.makeText(getContext(), "Failed to load video", Toast.LENGTH_SHORT).show();
            }
        } else {
            Log.e(TAG, "Video file path is null or empty");
            Toast.makeText(getContext(), "No video file available", Toast.LENGTH_SHORT).show();
        }

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

        UserItem uploader = dataManager.getUserById(uploaderId);
        if (uploader != null) {
            profilePicture.setImageURI(getUriFromResOrFile(uploader.getProfilePhoto()));
            userNameTextView.setText(uploader.getUserName());
        } else {
            profilePicture.setImageResource(R.drawable.default_profile_picture);
            userNameTextView.setText("[deleted user]");
        }

        // Initialize videoCard
        video = dataManager.getVideoById(videoId);
        updateLikesAndDislikes();

        return view;
    }

    private void handleLikeButtonClick() {
        UserItem currentUser = LoggedInUser.getUser();
        if (currentUser != null) {
            if (!currentUser.hasLiked(videoId)) {
                likeButton.setColorFilter(getResources().getColor(R.color.black_div));
                unlikeButton.setColorFilter(getResources().getColor(R.color.off_white));
                currentUser.likeVideo(videoId);
                DataManager.getInstance().incrementLikes(videoId);
                if (currentUser.hasDisliked(videoId)) {
                    DataManager.getInstance().decrementDislikes(videoId);
                    currentUser.removeDislike(videoId);
                }
                isLiked = true;
                isUnliked = false;
            } else {
                likeButton.setColorFilter(getResources().getColor(R.color.off_white));
                currentUser.removeLike(videoId);
                DataManager.getInstance().decrementLikes(videoId);
                isLiked = false;
            }
            updateLikesAndDislikes();
        }
    }

    private void handleUnlikeButtonClick() {
        UserItem currentUser = LoggedInUser.getUser();
        if (currentUser != null) {
            if (!currentUser.hasDisliked(videoId)) {
                unlikeButton.setColorFilter(getResources().getColor(R.color.black_div));
                likeButton.setColorFilter(getResources().getColor(R.color.off_white));
                currentUser.dislikeVideo(videoId);
                DataManager.getInstance().incrementDislikes(videoId);
                if (currentUser.hasLiked(videoId)) {
                    DataManager.getInstance().decrementLikes(videoId);
                    currentUser.removeLike(videoId);
                }
                isLiked = false;
                isUnliked = true;
            } else {
                unlikeButton.setColorFilter(getResources().getColor(R.color.off_white));
                currentUser.removeDislike(videoId);
                DataManager.getInstance().decrementDislikes(videoId);
                isUnliked = false;
            }
            updateLikesAndDislikes();
        }
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

    private void updateLikesAndDislikes() {
        if (video != null) {
            likesTextView.setText(DataManager.getInstance().getLikesCount(videoId) + " likes");
            dislikesTextView.setText(DataManager.getInstance().getDislikesCount(videoId) + " dislikes");
        }
    }
}
