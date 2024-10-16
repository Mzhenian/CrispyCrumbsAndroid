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
import android.widget.EditText;
import android.widget.MediaController;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.example.crispycrumbs.R;
import com.example.crispycrumbs.adapter.CommentSection_Adapter;
import com.example.crispycrumbs.adapter.VideoList_Adapter;
import com.example.crispycrumbs.dataUnit.CommentItem;
import com.example.crispycrumbs.dataUnit.PreviewVideoCard;
import com.example.crispycrumbs.dataUnit.UserItem;
import com.example.crispycrumbs.databinding.FragmentVideoPlayerBinding;
import com.example.crispycrumbs.localDB.LoggedInUser;
import com.example.crispycrumbs.serverAPI.ServerAPI;
import com.example.crispycrumbs.viewModel.UserViewModel;
import com.example.crispycrumbs.viewModel.VideoPlayerViewModel;

import java.util.ArrayList;


public class VideoPlayerFragment extends Fragment implements CommentSection_Adapter.CommentActionListener {
    private static final String TAG = "VideoPlayerFragment";
    private static final String KEY_POSITION = "position";
    private FragmentVideoPlayerBinding binding;
    private VideoPlayerViewModel viewModel;
    private UserViewModel userViewModel;
    private MediaController mediaController;
    private int currentPosition = 0;
    private PreviewVideoCard video;
    //    private ConstraintLayout buttonBar, commentSectionContainer;
    private CommentSection_Adapter CS_Adapter;
    private View view;
    private VideoList_Adapter VL_Adapter;
    private VideoList_Adapter.OnItemClickListener listener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentVideoPlayerBinding.inflate(getLayoutInflater());
        view = binding.getRoot();

        viewModel = new ViewModelProvider(this).get(VideoPlayerViewModel.class);
        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);


        // Get videoId from arguments
        Bundle bundle = getArguments();
        if (bundle == null) {
            Toast.makeText(getContext(), "No video selected", Toast.LENGTH_SHORT).show();
            return view;
        }

        binding.profilePicture.setOnClickListener(v -> {
            loadingMessage();
        });

        binding.likeButton.setOnClickListener(v -> {
            loadingMessage();
        });

        binding.unlikeButton.setOnClickListener(v -> {
            loadingMessage();
        });


        viewModel.setVideo(bundle.getString("videoId"));
        viewModel.getVideo().observe(getViewLifecycleOwner(), video -> {
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

            initializeVideo(video);
            initializeVideoDetails(video);

            binding.profilePicture.setOnClickListener(v -> {
                MainPage.getInstance().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new ProfileFragment(video.getUserId())).commit();
            });

            if (null == LoggedInUser.getUser().getValue()) {
                binding.likeButton.setOnClickListener(v -> {
                    MainPage.getInstance().showLoginSnackbar(view);
                });

                binding.unlikeButton.setOnClickListener(v -> {
                    MainPage.getInstance().showLoginSnackbar(view);
                });
            } else {
                binding.likeButton.setOnClickListener(v -> {
                    viewModel.likeVideo();
                });

                binding.unlikeButton.setOnClickListener(v -> {
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

        binding.commentButton.setOnClickListener(v -> {
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
        binding.rvRecommendedVideos.setAdapter(VL_Adapter);
        binding.rvRecommendedVideos.setLayoutManager(new LinearLayoutManager(MainPage.getInstance()));


        return view;
    }


    private void loadingMessage() {
        Toast.makeText(getContext(), "Loading video and his data...", Toast.LENGTH_SHORT).show();
    }

    private void initializeVideo(PreviewVideoCard video) {
        String videoUrl = ServerAPI.getInstance().constructUrl(video.getVideoFile());
        Uri videoUri = Uri.parse(videoUrl);
        binding.videoView.setVideoURI(videoUri);

        mediaController = new MediaController(getContext());
        mediaController.setAnchorView(binding.videoView);
        binding.videoView.setMediaController(mediaController);

        binding.videoView.setOnPreparedListener(mp -> {
            binding.progressBar2.setVisibility(View.GONE);
            binding.videoView.start();
            viewModel.incrementVideoViews(); // Trigger view count increment
        });
    }

    private void initializeVideoDetails(PreviewVideoCard video) {
        binding.videoTitle.setText(video.getTitle());
        binding.txtVideoDescription.setText(video.getDescription());

        updateVideoDetails();


        // Observe user profile data
        userViewModel.getUser(video.getUserId()).observe(getViewLifecycleOwner(), uploader -> {
            if (uploader != null) {
                String userProfileUrl = ServerAPI.getInstance().constructUrl(uploader.getProfilePhoto());
                Glide.with(VideoPlayerFragment.this)
                        .load(userProfileUrl)
                        .placeholder(R.drawable.default_profile_picture) // Optional: Add a placeholder
                        .skipMemoryCache(true)
                        .into(binding.profilePicture);
                binding.userName.setText(uploader.getUserName());
            } else {
                binding.profilePicture.setImageResource(R.drawable.default_profile_picture);
                binding.userName.setText(R.string.deleted_user);
                Log.e(TAG, "Uploader not found");
            }
        });

        String formattedDate = viewModel.formatDate(video.getUploadDate());
        binding.videoDate.setText(formattedDate);
        binding.btnVideoDescription.setOnClickListener(v -> toggleDescriptionComments());
        binding.shareButton.setOnClickListener(v -> showShareMenu());
    }

    private void initializeCommentsSection(ArrayList<CommentItem> comments) {
        Log.d("Comment update", "Initializing comment section. Comments size: " + (comments != null ? comments.size() : 0));

        binding.txtVideoDescription.setVisibility(View.GONE);
        binding.buttonBar.setVisibility(View.VISIBLE);
        binding.commentSectionContainer.setVisibility(View.VISIBLE);

        if (comments == null || comments.isEmpty()) {
            binding.commentSectionContainer.setVisibility(View.GONE);
            return;
        }

//        if (adapter != null) {
//            Log.d("Comment update", "Updating existing adapter with new comments: " + comments.size());
//            adapter.updateComments(comments);
//        } else {
        UserItem currentUser = LoggedInUser.getUser().getValue();
        String currentUserId = currentUser != null ? currentUser.getUserId() : null;

        Log.d("Comment update", "Setting up adapter with comments: " + comments.size());
        CS_Adapter = new CommentSection_Adapter(getContext(), comments, this, currentUserId);
        binding.commentSection.setAdapter(CS_Adapter);
        binding.commentSection.setLayoutManager(new LinearLayoutManager(getContext()));
//        }

        binding.commentSectionContainer.setVisibility(View.VISIBLE);
        Log.e("Comment update", "End of Initialize comment section in Fragment");
    }

    private void toggleDescriptionComments() {
        if (binding.txtVideoDescription.getVisibility() == View.VISIBLE) {
            binding.btnVideoDescription.setText(getString(R.string.more));
            initializeCommentsSection(video.getComments());
        } else {
            binding.btnVideoDescription.setText(getString(R.string.less));
            initializeDescription();
        }
    }

    private void initializeDescription() {
        binding.txtVideoDescription.setVisibility(View.VISIBLE);
        binding.buttonBar.setVisibility(View.GONE);
        binding.commentSectionContainer.setVisibility(View.GONE);
    }

    private void updateVideoDetails() {
        if (null == video) {
            return;
        }
        Log.d(TAG, "Updating UI with likes: " + video.getLikes() + " and dislikes: " + video.getDislikes());
        binding.videoLikes.setText(video.getLikes() + " likes");
        // Assuming you have a dislikesTextView for dislikes
        // dislikesTextView.setText(video.getDislikes() + " dislikes");
        binding.videoViews.setText(video.getViews() + " views");

        binding.likeButton.setSelected(false);
        binding.likeButton.setColorFilter(getResources().getColor(R.color.crispy_orange_light));
//            likeButton.setBackgroundColor(getResources().getColor(R.color.crispy_orange));
        binding.unlikeButton.setSelected(false);
        binding.unlikeButton.setColorFilter(getResources().getColor(R.color.crispy_orange_light));

        UserItem currentUser = LoggedInUser.getUser().getValue();
        if (currentUser == null) {
            // No user is logged in, so clear the selection like buttons  states
        } else if (video.getLikedBy().contains(currentUser.getUserId())) {
            binding.likeButton.setSelected(true);
            binding.likeButton.setColorFilter(getResources().getColor(R.color.absolute_ofek_white));
//            likeButton.setBackgroundColor(getResources().getColor(R.color.crispy_orange_light));
        } else if (video.getDislikedBy().contains(currentUser.getUserId())) {
            binding.unlikeButton.setSelected(true);
            binding.unlikeButton.setColorFilter(getResources().getColor(R.color.absolute_ofek_white));
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
        if (binding.videoView != null && binding.videoView.isPlaying()) {
            currentPosition = binding.videoView.getCurrentPosition();
            binding.videoView.pause();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        binding.videoView.seekTo(currentPosition);
        binding.videoView.start();
    }

    @Override
    public void onStop() {
        super.onStop();
        binding.videoView.stopPlayback();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        viewModel.getVideo().removeObservers(getViewLifecycleOwner());
//        binding.videoView = null;
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
