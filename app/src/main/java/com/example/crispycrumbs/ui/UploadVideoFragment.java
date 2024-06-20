package com.example.crispycrumbs.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.fragment.app.Fragment;

import com.example.crispycrumbs.R;
import com.example.crispycrumbs.data.PreviewVideoCard;
import com.example.crispycrumbs.model.UserLogic;

public class UploadVideoFragment extends Fragment {

    private PreviewVideoCard previewVideoCard;
    private EditText etVideoTitle;
    private ImageView thumbnailImageHolder;
    private Button btnOpenCamera, btnChooseFromGallery;
    private ImageButton btnUpload, btnCancleUpload;
    private static final int REQUEST_VIDEO_CAPTURE = 1;
    private static final int REQUEST_VIDEO_PICK = 2;
    private final ActivityResultLauncher<Intent> mGetContent = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Uri videoUri = result.getData().getData();
                    // Set the video file for the PreviewVideoCard instance
                    previewVideoCard.setVideoFile(videoUri.toString());
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_upload_video, container, false);

        etVideoTitle = view.findViewById(R.id.etVideoTitle);
        thumbnailImageHolder = view.findViewById(R.id.thumbnailImageHolder);
        btnOpenCamera = view.findViewById(R.id.btnOpenCamera);
        btnChooseFromGallery = view.findViewById(R.id.btnChooseFromGallery);
        btnUpload = view.findViewById(R.id.btnUpload);
        btnCancleUpload = view.findViewById(R.id.btnCancleUpload);

        previewVideoCard = new PreviewVideoCard();

        btnOpenCamera.setOnClickListener(v -> openCamera());
        btnChooseFromGallery.setOnClickListener(v -> chooseFromGallery());
        btnUpload.setOnClickListener(v -> uploadVideo());
        btnCancleUpload.setOnClickListener(v -> cancelUpload());

        return view;
    }

    private void openCamera() {
        Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        if (takeVideoIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            mGetContent.launch(takeVideoIntent);
        }
    }

    private void chooseFromGallery() {
        Intent pickVideoIntent = new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
        pickVideoIntent.setType("video/*");
        startActivityForResult(pickVideoIntent, REQUEST_VIDEO_PICK);
    }

    private void uploadVideo() {
        String title = etVideoTitle.getText().toString();
        if (!title.isEmpty() && previewVideoCard.getVideoFile() != null) {
            previewVideoCard.setVideoId(UserLogic.nextId(MainPage.getDataManager().getLastUserId()));
            previewVideoCard.setTitle(title);
            if (previewVideoCard.getThumbnail() == null) {
                previewVideoCard.setThumbnail("default_thumbnail");
            }
            MainPage.getDataManager().addVideo(previewVideoCard);
            // Navigate back to HomeFragment
            getActivity().getSupportFragmentManager().popBackStack();
        }
    }

    private void cancelUpload() {
        previewVideoCard = null;
        // Navigate back to HomeFragment
        getActivity().getSupportFragmentManager().popBackStack();
    }
}