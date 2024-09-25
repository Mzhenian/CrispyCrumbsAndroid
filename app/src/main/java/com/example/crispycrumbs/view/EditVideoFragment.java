package com.example.crispycrumbs.view;

import static com.example.crispycrumbs.model.DataManager.getUriFromResOrFile;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.crispycrumbs.R;
import com.example.crispycrumbs.localDB.LoggedInUser;
import com.example.crispycrumbs.dataUnit.PreviewVideoCard;
import com.example.crispycrumbs.databinding.FragmentEditVideoBinding;
import com.example.crispycrumbs.model.DataManager;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class EditVideoFragment extends Fragment {
    private static final int REQUEST_THUMBNAIL_PICK = 23;
    private FragmentEditVideoBinding binding;
    private EditText etVideoTitle, etVideoDescription;
    private TextView txtChooseVideo;
    private TextView txtChooseThumbnail;
    private TextView TitleEditVideo;
    private ImageView videoHolder, thumbnailImageHolder;
    private Button btnOpenCamera, btnChooseFromGallery;
    private ProgressBar progressBar;
    private String currentThumbnailPath;
    private String currentVideoPath;
    private String videoId;
    private PreviewVideoCard video;
    private Uri thumbnailUri, videoUri;

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
        if (null == LoggedInUser.getUser() || null == LoggedInUser.getUser().getValue().getUserId()) {
            MainPage.getInstance().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HomeFragment()).commit();
            return view;
        }

        videoId = bundle.getString("videoId");
        video = DataManager.getInstance().getVideoById(videoId);

        if (!(video.getUserId().equals(LoggedInUser.getUser().getValue().getUserId()))) {
            MainPage.getInstance().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new UploadVideoFragment()).commit();
            return view;
        }

        TitleEditVideo = view.findViewById(R.id.TitleEditVideo);
        TitleEditVideo.setText("Editing: " + video.getTitle() + " #" + video.getVideoId());
        etVideoTitle = view.findViewById(R.id.etVideoTitle);
        etVideoTitle.setText(video.getTitle());
        etVideoDescription = view.findViewById(R.id.etVideoDescription);
        etVideoDescription.setText(video.getDescription());

        videoHolder = view.findViewById(R.id.video_holder);
        binding.videoHolder.setImageURI(getUriFromResOrFile(video.getThumbnail()));

        thumbnailImageHolder = view.findViewById(R.id.thumbnailImageHolder);
        progressBar = view.findViewById(R.id.progressBar);
        txtChooseVideo = view.findViewById(R.id.txtChooseVideo);
        txtChooseThumbnail = view.findViewById(R.id.txtChooseThumbnail);

        binding.btnChooseThumbnail.setOnClickListener(v -> uploadPhoto());
        binding.btnUpdate.setOnClickListener(v -> update());
        binding.btnDelete.setOnClickListener(v -> delete());

        return view;
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

        getActivity();
        if (resultCode == Activity.RESULT_OK && data != null) {
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
            } else {
                Log.e("EditVideoFragment", "Unknown request code: " + requestCode);
            }
        }
    }

    private void uploadPhoto() {
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_THUMBNAIL_PICK);
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