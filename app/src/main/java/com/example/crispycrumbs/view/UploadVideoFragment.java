package com.example.crispycrumbs.view;


import static android.app.Activity.RESULT_OK;
import static com.example.crispycrumbs.model.DataManager.getUriFromResOrFile;
import static com.example.crispycrumbs.view.MainPage.getDataManager;

import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.crispycrumbs.R;
import com.example.crispycrumbs.localDB.LoggedInUser;
import com.example.crispycrumbs.dataUnit.PreviewVideoCard;
import com.example.crispycrumbs.databinding.FragmentUploadVideoBinding;
import com.example.crispycrumbs.model.DataManager;
import com.example.crispycrumbs.model.UserLogic;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class UploadVideoFragment extends Fragment {
    private static final int REQUEST_VIDEO_PICK = 12;
    private static final int REQUEST_THUMBNAIL_PICK = 13;
    private FragmentUploadVideoBinding binding;
    private EditText etVideoTitle, etVideoDescription;
    private TextView txtChooseVideo;
    private TextView txtChooseThumbnail;
    private ImageView thumbnailImageHolder;
    private Button btnOpenCamera, btnChooseFromGallery;
    private ImageButton btnUpload, btnCancleUpload;
    private ProgressBar progressBar;
    private String currentThumbnailPath;
    private String currentVideoPath;
    private Uri thumbnailUri, videoUri;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getContext().getTheme().applyStyle(R.style.Base_Theme_CrispyCrumbs_Light, true);

        binding = FragmentUploadVideoBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        etVideoTitle = view.findViewById(R.id.etVideoTitle);
        thumbnailImageHolder = view.findViewById(R.id.thumbnailImageHolder);
        progressBar = view.findViewById(R.id.progressBar);
        etVideoDescription = view.findViewById(R.id.etVideoDescription);
        txtChooseVideo = view.findViewById(R.id.txtChooseVideo);
        txtChooseThumbnail = view.findViewById(R.id.txtChooseThumbnail);

        binding.btnChooseVideo.setOnClickListener(v -> uploadVideo());
        binding.btnChooseThumbnail.setOnClickListener(v -> uploadPhoto());
        binding.btnUpload.setOnClickListener(v -> upload());
        binding.btnCancleUpload.setOnClickListener(v -> cancelUpload());

        return view;
    }

    private File createVideoFile() throws IOException {
        // Create an video file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String videoFileName = "VIDEO_" + timeStamp + "_";
        File storageDir = getActivity().getExternalFilesDir(null);
        File video = File.createTempFile(videoFileName,  /* prefix */
                getDataManager().getFileExtension(videoUri),         /* suffix */
                storageDir      /* directory */);

        return video;
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);
        currentThumbnailPath = image.getAbsolutePath();
        return image;
    }

    private Bitmap getVideoThumbnail(Uri videoUri) {
        Bitmap thumbnail = null;
        try {
            thumbnail = MediaStore.Video.Thumbnails.getThumbnail(getContext().getContentResolver(), ContentUris.parseId(videoUri), MediaStore.Video.Thumbnails.MINI_KIND, null);
        } catch (Exception e) {
            Log.e("Thumbnail", "Could not get thumbnail", e);
        }
        return thumbnail;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            if (requestCode == REQUEST_THUMBNAIL_PICK) {
                Uri photoUri = data.getData();
                try {
                    Bitmap thumbnailBitmap = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), photoUri);
                    binding.thumbnailImageHolder.setImageBitmap(thumbnailBitmap);

//                    currentThumbnailPath = thumbnailBitmap.toString(); // Update the photo path to the selected image's URI
                    currentThumbnailPath = photoUri.toString();
                    txtChooseThumbnail.setText("");

                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (requestCode == REQUEST_VIDEO_PICK) {
                videoUri = data.getData();
                try {
                    Bitmap thumbnail = getVideoThumbnail(videoUri);
                    saveVideoLocally(videoUri);

                    binding.imageView.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.baseline_cloud_done_24));
                    txtChooseVideo.setText("");
//                    binding.thumbnailImageHolder.setImageBitmap(thumbnail);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void uploadPhoto() {
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_THUMBNAIL_PICK);
    }

    private void uploadVideo() {
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_VIDEO_PICK);
    }


    private void saveVideoLocally(Uri videoUri) {
        try {
            InputStream in = getContext().getContentResolver().openInputStream(videoUri);
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            String videoFileName = "VIDEO_" + timeStamp + getDataManager().getFileExtension(videoUri);
            File storageDir = getContext().getExternalFilesDir(Environment.DIRECTORY_MOVIES);
            File videoFile = new File(storageDir, videoFileName);
            OutputStream out = new FileOutputStream(videoFile);
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }

            currentVideoPath = videoUri.toString();
            out.close();
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private Boolean validateUploadable() {
        if (etVideoTitle.getText().toString().trim().isEmpty()) {
            etVideoTitle.setError("Video title is required");
            etVideoTitle.requestFocus();
            return false;
        } else if (currentVideoPath == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle("Error");
            builder.setMessage("Please select a video to upload");
            builder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss());
            builder.show();
            return false;
        } else if (currentThumbnailPath == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle("Error");
            builder.setMessage("Please select a thumbnail for the video");
            builder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss());
            builder.show();
            return false;
        }

        // Test that previewVideoCard.getVideoFile() is playable by VideoPlayerFragment
        MediaPlayer mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(getContext(), getUriFromResOrFile(currentVideoPath));
            mediaPlayer.prepare();
            mediaPlayer.release();
        } catch (IOException e) {
            Log.e("UploadVideoFragment", "Video not playable: " + videoUri, e);
            return false;
        }
        return true;
    }

    private void upload() {
        progressBar.setVisibility(View.VISIBLE);
        String lastVideoId = getDataManager().getLastVideoId();

        PreviewVideoCard previewVideoCard = new PreviewVideoCard(UserLogic.nextId(lastVideoId), etVideoTitle.getText().toString(), currentThumbnailPath, currentVideoPath, etVideoDescription.getText().toString());


        if (validateUploadable()) {
            DataManager.getInstance().addVideo(previewVideoCard);
            LoggedInUser.getUser().addVideo(previewVideoCard.getVideoId());
            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HomeFragment()).commit();
            Toast.makeText(getContext(), "uploaded " + previewVideoCard.getTitle() + " successfully", Toast.LENGTH_SHORT).show();
        }
        progressBar.setVisibility(View.GONE);

    }

    private void cancelUpload() {
        // Navigate back to HomeFragment
        getActivity().getSupportFragmentManager().popBackStack();
    }

}