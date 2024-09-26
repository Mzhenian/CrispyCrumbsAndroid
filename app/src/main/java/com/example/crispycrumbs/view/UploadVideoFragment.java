package com.example.crispycrumbs.view;


import static android.app.Activity.RESULT_OK;
import static androidx.core.content.ContextCompat.getSystemService;
import static com.example.crispycrumbs.model.DataManager.getUriFromResOrFile;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Size;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.crispycrumbs.R;
import com.example.crispycrumbs.adapter.TagsAdapter;
import com.example.crispycrumbs.databinding.FragmentUploadVideoBinding;
import com.example.crispycrumbs.model.DataManager;
import com.example.crispycrumbs.viewModel.UploadVideoViewModel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class UploadVideoFragment extends Fragment {
    private static final int REQUEST_VIDEO_PICK = 12;
    private static final int REQUEST_THUMBNAIL_PICK = 13;
    private FragmentUploadVideoBinding binding;
    private EditText etVideoTitle, etVideoDescription;
    private TextView txtChooseVideo;
    private TextView txtChooseThumbnail;
    private ImageView thumbnailImageHolder;
//    private Button btnOpenCamera, btnChooseFromGallery;
//    private ImageButton btnUpload, btnCancelUpload;
    private ProgressBar progressBar;
    private String currentThumbnailPath;
    private String currentVideoPath;
    private Uri thumbnailUri,
            videoUri;
    private UploadVideoViewModel uploadVideoViewModel;
    private ActivityResultLauncher<Intent> videoPickerLauncher;
    private ActivityResultLauncher<Intent> photoPickerLauncher;

    private Spinner categorySelector;
    private EditText etVideoTag;
    private Button btnAddVideoTag;
    private RecyclerView rvTagsPreview;
    private TagsAdapter tagsAdapter;
    private List<String> tags;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getContext().getTheme().applyStyle(R.style.Base_Theme_CrispyCrumbs_Light, true);

        binding = FragmentUploadVideoBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        uploadVideoViewModel = new ViewModelProvider(this).get(UploadVideoViewModel.class);


        etVideoTitle = view.findViewById(R.id.etVideoTitle);
        thumbnailImageHolder = view.findViewById(R.id.thumbnailImageHolder);
        progressBar = view.findViewById(R.id.progressBar);
        etVideoDescription = view.findViewById(R.id.etVideoDescription);
        txtChooseVideo = view.findViewById(R.id.txtChooseVideo);
        txtChooseThumbnail = view.findViewById(R.id.txtChooseThumbnail);
        categorySelector = view.findViewById(R.id.spinnerCategory);
        etVideoTag = view.findViewById(R.id.etVideoTag);
        btnAddVideoTag = view.findViewById(R.id.btnAddVideoTag);
        rvTagsPreview = view.findViewById(R.id.rvTagsPreview);

        initializeVideoPicker();
        initializeThumbnailPicker();
        binding.btnChooseVideo.setOnClickListener(v -> setVideo());
        binding.btnChooseThumbnail.setOnClickListener(v -> setPhoto());

        binding.btnUpload.setOnClickListener(v -> upload());
        binding.btnCancelUpload.setOnClickListener(v -> cancelUpload());
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, UploadVideoViewModel.CATEGORIES);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySelector.setAdapter(adapter);

        tags = new ArrayList<>();
        tagsAdapter = new TagsAdapter(tags);
        rvTagsPreview.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvTagsPreview.setAdapter(tagsAdapter);

//        etVideoTag.setOnEditorActionListener((v, actionId, event) -> {
//            if (actionId == EditorInfo.IME_ACTION_DONE) {
//                String tagText = etVideoTag.getText().toString().trim();
//                if (tagText.isEmpty()) {
//                    // Close the keyboard
//                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//                    imm.hideSoftInputFromWindow(etVideoTag.getWindowToken(), 0);
//                } else {
//                    // Trigger the button's onClick event
//                    btnAddVideoTag.performClick();
//                }
//                return true;
//            }
//            return false;
//        });

        btnAddVideoTag.setOnClickListener(v -> {
            String tag = etVideoTag.getText().toString().trim();
            if (!tag.isEmpty()) {
                tags.add(tag);
                tagsAdapter.notifyItemInserted(tags.size() - 1);
                etVideoTag.setText("");
            }
        });

        return view;
    }

    private Bitmap getVideoThumbnail(Uri videoUri) {
        Bitmap thumbnail = null;
        try {
            Size size = new Size(binding.videoHolder.getWidth(), binding.videoHolder.getHeight());
            thumbnail = MainPage.getInstance().getContentResolver().loadThumbnail(videoUri, size, null);
        } catch (Exception e) {
            Log.e("Thumbnail", "Could not get thumbnail", e);
        }
        return thumbnail;
    }

    public void setVideo() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
        if (videoPickerLauncher != null) {
            videoPickerLauncher.launch(intent);
        } else {
            Log.e("UploadVideoFragment", "videoPickerLauncher is not initialized.");
        }    }

    public void setPhoto() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        if (photoPickerLauncher != null) {
            photoPickerLauncher.launch(intent);
        } else {
            Log.e("UploadVideoFragment", "photoPickerLauncher is not initialized.");
        }
    }

    //todo move to ViewModel
    public Boolean validateUploadable() {
        if (etVideoTitle.getText().toString().trim().isEmpty()) {
            etVideoTitle.setError("Video title is required");
            etVideoTitle.requestFocus();
            return false;
        } else if (videoUri == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle("Error");
            builder.setMessage("Please select a video to upload");
            builder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss());
            builder.show();
            return false;
        } else if (thumbnailUri == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle("Error");
            builder.setMessage("Please select a thumbnail for the video");
            builder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss());
            builder.show();
            return false;
        }
        return true;
    }

    private void upload() {
        if (!validateUploadable()) {
            return;
        }
        MutableLiveData<Boolean> uploadStatus = new MutableLiveData<>();
        uploadStatus.observe(getViewLifecycleOwner(), status -> {
            if (status) {
                Toast.makeText(getContext(), "Video uploaded successfully", Toast.LENGTH_SHORT).show();
                MainPage.getInstance().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new PlayListFragment()).addToBackStack(null).commit();
            } else {
                Toast.makeText(getContext(), "Failed to upload video", Toast.LENGTH_SHORT).show();
            }
            progressBar.setVisibility(View.GONE);
        });

        progressBar.setVisibility(View.VISIBLE);
        uploadVideoViewModel.upload(videoUri, thumbnailUri, etVideoTitle.getText().toString(), etVideoDescription.getText().toString(), categorySelector.getSelectedItem().toString(), tags, uploadStatus);
    }


    private void cancelUpload() {
        MainPage.getInstance().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new PlayListFragment()).addToBackStack(null).commit();
    }

    private String getRealPathFromURI(Uri uri) {
        String[] projection = { MediaStore.Video.Media.DATA };
        Cursor cursor = getContext().getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null) {
            int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
            cursor.moveToFirst();
            String filePath = cursor.getString(columnIndex);
            cursor.close();
            return filePath;
        }
        return null;
    }

    private void initializeVideoPicker() {
        videoPickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        try {
                            videoUri = result.getData().getData();
                            if (videoUri == null) {
                                Toast.makeText(getContext(), "Failed to get video from user", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            //todo call Bitmap thumbnail = getVideoThumbnail(videoUri); and set it to the thumbnail
                            binding.videoHolder.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.baseline_cloud_done_24));
                            txtChooseVideo.setText("");
                            Toast.makeText(getContext(), "Video selected: " + videoUri.toString(), Toast.LENGTH_SHORT).show();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        Toast.makeText(getContext(), "Failed to get thumbnail from user", Toast.LENGTH_SHORT).show();
                    }
                }
        );

    }
        public void initializeThumbnailPicker() {
        photoPickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        try {
                            thumbnailUri = result.getData().getData();
                            if (thumbnailUri == null) {
                                Toast.makeText(getContext(), "Failed to get thumbnail from user", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            ImageDecoder.Source source = ImageDecoder.createSource(getContext().getContentResolver(), thumbnailUri);
                            Bitmap thumbnailBitmap = ImageDecoder.decodeBitmap(source);
                            binding.thumbnailImageHolder.setImageBitmap(thumbnailBitmap);
                        txtChooseThumbnail.setText("");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        Toast.makeText(getContext(), "Failed to get thumbnail from user", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }
}








