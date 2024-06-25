package com.example.crispycrumbs.ui;


import static android.content.Intent.getIntent;

import static com.example.crispycrumbs.model.DataManager.getUriFromResOrFile;
import static com.example.crispycrumbs.ui.MainPage.getDataManager;

import android.content.Intent;
import android.content.pm.PackageManager;
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import com.example.crispycrumbs.R;
import com.example.crispycrumbs.data.LoggedInUser;
import com.example.crispycrumbs.data.PreviewVideoCard;
import com.example.crispycrumbs.data.UserItem;
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
import android.app.AlertDialog;
import android.Manifest;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class UploadVideoFragment extends Fragment {
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

    private static final int REQUEST_VIDEO_CAPTURE = 1;
    private static final int REQUEST_VIDEO_PICK = 2;
    private static final int REQUEST_THUMBNAIL_PICK = 3;
    private static final int REQUEST_THUMBNAIL_CAPTURE = 4;
    private static final int REQUEST_CAMERA_PERMISSION = 5;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        binding = FragmentUploadVideoBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        etVideoTitle = view.findViewById(R.id.etVideoTitle);
        thumbnailImageHolder = view.findViewById(R.id.thumbnailImageHolder);
        progressBar = view.findViewById(R.id.progressBar);
        etVideoDescription = view.findViewById(R.id.etVideoDescription);
        txtChooseVideo = view.findViewById(R.id.txtChooseVideo);
        txtChooseThumbnail = view.findViewById(R.id.txtChooseThumbnail);

        binding.btnChooseVideo.setOnClickListener(v -> takeVideo());
        binding.btnChooseThumbnail.setOnClickListener(v -> takeThumbnail());
        binding.btnUpload.setOnClickListener(v -> upload());
        binding.btnCancleUpload.setOnClickListener(v -> cancelUpload());

        return view;
    }

    private void takeVideo() {
        CharSequence[] options = {"Record Video", "Choose from Gallery"};
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Select Video");
        builder.setItems(options, (dialog, which) -> {
            if (which == 0) { // Record Video
                if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                        ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CAMERA_PERMISSION);
                } else {
                    dispatchTakeVideoIntent();
                }
            } else if (which == 1) { // Choose from Gallery
                if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                        ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CAMERA_PERMISSION);
                } else {
                    Intent pickVideoIntent = new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(pickVideoIntent, REQUEST_VIDEO_PICK);
                }
            }
        });
        builder.show();
    }

    private void dispatchTakeVideoIntent() {
        Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        if (takeVideoIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivityForResult(takeVideoIntent, REQUEST_VIDEO_CAPTURE);
//            File videoFile = null;
//            try {
//                videoFile = createVideoFile();
//            } catch (IOException ex) {
//                Log.e("UploadVideo", "IO exception creating video file", ex);
//            }
//            if (videoFile != null) {
//                videoUri = FileProvider.getUriForFile(getContext(), "com.example.crispycrumbs.fileprovider", videoFile);
//                takeVideoIntent.putExtra(MediaStore.EXTRA_OUTPUT, videoUri);
//                startActivityForResult(takeVideoIntent, REQUEST_VIDEO_CAPTURE);
//            }
        }
    }

    private File createVideoFile() throws IOException {
        // Create an video file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String videoFileName = "VIDEO_" + timeStamp + "_";
        File storageDir = getActivity().getExternalFilesDir(null);
        File video = File.createTempFile(
                videoFileName,  /* prefix */
                getDataManager().getFileExtension(videoUri),         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
//        currentVideoPath = video.getAbsolutePath();
        return video;
    }

    private void takeThumbnail() {
        CharSequence[] options = {"Take Photo", "Choose from Gallery"};
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Select Thumbnail");
        builder.setItems(options, (dialog, which) -> {
            if (which == 0) {
                if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                        ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(getContext(), "for that please allow to open the camera and save your picture:", Toast.LENGTH_LONG).show();
                    requestPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CAMERA_PERMISSION);
                } else {
                    dispatchTakeThumbnailIntent();
                }
            } else if (which == 1) {
                Intent pickPhotoIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(pickPhotoIntent, REQUEST_THUMBNAIL_PICK);
            }
        });
        builder.show();
    }
    private void dispatchTakeThumbnailIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                Log.e("UploadVideo", "IO exception creating image file", ex);
            }
            if (photoFile != null) {
                thumbnailUri = FileProvider.getUriForFile(getContext(), "com.example.crispycrumbs.fileprovider", photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, thumbnailUri);
                startActivityForResult(takePictureIntent, REQUEST_THUMBNAIL_CAPTURE);
            }
        }
    }
    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);
        currentThumbnailPath = image.getAbsolutePath();
        return image;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == getActivity().RESULT_OK) {
            if (requestCode == REQUEST_VIDEO_PICK) {
                videoUri = data.getData();
                saveVideoLocally(videoUri);
                binding.imageView.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.baseline_cloud_done_24));
                txtChooseVideo.setText("");
            } else if (requestCode == REQUEST_VIDEO_CAPTURE) {
                Uri videoUri = data.getData();
                saveVideoLocally(videoUri);
                binding.imageView.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.baseline_cloud_done_24));
                txtChooseVideo.setText("");
            } else if (requestCode == REQUEST_THUMBNAIL_CAPTURE) {
                binding.thumbnailImageHolder.setImageURI(thumbnailUri);
                currentThumbnailPath = thumbnailUri.toString();
                txtChooseThumbnail.setText("");
            } else if (requestCode == REQUEST_THUMBNAIL_PICK) {
                if (data != null) {
                    Uri selectedImage = data.getData();
                    binding.thumbnailImageHolder.setImageURI(selectedImage);
                    currentThumbnailPath = selectedImage.toString(); // Update the photo path to the selected image's URI
                    txtChooseThumbnail.setText("");
                }
            } else {
                Log.e("UploadVideoFragment", "Unknown request code: " + requestCode);
            }
        }
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
        }
        progressBar.setVisibility(View.GONE);
    }

    private void cancelUpload() {
        // Navigate back to HomeFragment
        getActivity().getSupportFragmentManager().popBackStack();
    }

}