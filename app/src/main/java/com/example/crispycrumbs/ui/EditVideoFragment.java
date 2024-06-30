package com.example.crispycrumbs.ui;

import static com.example.crispycrumbs.model.DataManager.getUriFromResOrFile;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
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
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.example.crispycrumbs.R;
import com.example.crispycrumbs.data.LoggedInUser;
import com.example.crispycrumbs.data.PreviewVideoCard;
import com.example.crispycrumbs.databinding.FragmentEditVideoBinding;
import com.example.crispycrumbs.model.DataManager;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class EditVideoFragment extends Fragment {
    private FragmentEditVideoBinding binding;

    private EditText etVideoTitle, etVideoDescription;
    private TextView txtChooseVideo;
    private TextView txtChooseThumbnail;
    private TextView TitleEditVideo;
    private ImageView imageView, thumbnailImageHolder;
    private Button btnOpenCamera, btnChooseFromGallery;
    private ProgressBar progressBar;

    private String currentThumbnailPath;
    private String currentVideoPath;
    private String videoId;
    private PreviewVideoCard video;
    private Uri thumbnailUri, videoUri;

    private static final int REQUEST_THUMBNAIL_PICK = 3;
    private static final int REQUEST_THUMBNAIL_CAPTURE = 4;
    private static final int REQUEST_CAMERA_PERMISSION = 5;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentEditVideoBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        Bundle bundle = getArguments();
        if (bundle == null) {
            MainPage.getInstance().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new UploadVideoFragment()).commit();
            return view;
        }
        if (null == LoggedInUser.getUser() || null == LoggedInUser.getUser().getUserId()) {
            MainPage.getInstance().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HomeFragment()).commit();
            return view;
        }

        videoId = bundle.getString("videoId");
        video = DataManager.getInstance().getVideoById(videoId);

        if (!(video.getUserId().equals(LoggedInUser.getUser().getUserId()))) {
            MainPage.getInstance().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new UploadVideoFragment()).commit();
            return view;
        }

        TitleEditVideo = view.findViewById(R.id.TitleEditVideo);
        TitleEditVideo.setText("Editing: " + video.getTitle() + " #" + video.getVideoId());
        etVideoTitle = view.findViewById(R.id.etVideoTitle);
        etVideoTitle.setText(video.getTitle());
        etVideoDescription = view.findViewById(R.id.etVideoDescription);
        etVideoDescription.setText(video.getDescription());

        imageView = view.findViewById(R.id.imageView);
        binding.imageView.setImageURI(getUriFromResOrFile(video.getThumbnail()));

        thumbnailImageHolder = view.findViewById(R.id.thumbnailImageHolder);
        progressBar = view.findViewById(R.id.progressBar);
        txtChooseVideo = view.findViewById(R.id.txtChooseVideo);
        txtChooseThumbnail = view.findViewById(R.id.txtChooseThumbnail);

        binding.btnChooseThumbnail.setOnClickListener(v -> takeThumbnail());
        binding.btnUpdate.setOnClickListener(v -> update());
        binding.btnDelete.setOnClickListener(v -> delete());

        return view;
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
                Log.e("EditVideo", "IO exception creating image file", ex);
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
            if (requestCode == REQUEST_THUMBNAIL_CAPTURE) {
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
                Log.e("EditVideoFragment", "Unknown request code: " + requestCode);
            }
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
        return true;
    }

    private void update() {
        progressBar.setVisibility(View.VISIBLE);

        video.setTitle(etVideoTitle.getText().toString());
        if (currentThumbnailPath != null) {
            video.setThumbnail(currentThumbnailPath);
        }
        video.setDescription(etVideoDescription.getText().toString());

        getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HomeFragment()).commit();
        progressBar.setVisibility(View.GONE);
    }

    private void delete() {
        // Navigate back to HomeFragment
        DataManager.getInstance().deleteVideo(video);
        LoggedInUser.removeVideo(video); //to ensure you can delete a video only if you can log into this account
        getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HomeFragment()).commit();
    }
}