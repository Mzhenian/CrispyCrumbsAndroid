package com.example.crispycrumbs.view;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
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

import com.example.crispycrumbs.R;
import com.example.crispycrumbs.adapter.TagsAdapter;
import com.example.crispycrumbs.databinding.FragmentUploadVideoBinding;
import com.example.crispycrumbs.model.DataManager;
import com.example.crispycrumbs.viewModel.UploadVideoViewModel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class UploadVideoFragment extends Fragment {
    private final static String TAG = "UploadVideoFragment";
    private FragmentUploadVideoBinding binding;
    private UploadVideoViewModel uploadVideoViewModel;
    private Executor executor = Executors.newSingleThreadExecutor();


    private Uri thumbnailUri,
            videoUri;
    private ActivityResultLauncher<Intent> videoPickerLauncher,
            photoPickerLauncher;
    private TagsAdapter tagsAdapter;
    private List<String> tags;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getContext().getTheme().applyStyle(R.style.Base_Theme_CrispyCrumbs_Light, true);

        binding = FragmentUploadVideoBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        uploadVideoViewModel = new ViewModelProvider(this).get(UploadVideoViewModel.class);

        initializeVideoPicker();
        initializeThumbnailPicker();
        binding.btnChooseVideo.setOnClickListener(v -> setVideo());
        binding.btnChooseThumbnail.setOnClickListener(v -> setPhoto());

        binding.btnUpload.setOnClickListener(v -> upload());
        binding.btnCancelUpload.setOnClickListener(v -> cancelUpload());
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, UploadVideoViewModel.CATEGORIES);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerCategory.setAdapter(categoryAdapter);

        tags = new ArrayList<>();
        tagsAdapter = new TagsAdapter(tags);
        binding.rvTagsPreview.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.rvTagsPreview.setAdapter(tagsAdapter);

        binding.btnAddVideoTag.setOnClickListener(v -> addTag());

        binding.etVideoTag.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                addTag();
                return true;
            }
            return false;
        });

        return view;
    }

    public void addTag() {
        String tag = binding.etVideoTag.getText().toString().trim();
        if (!tag.isEmpty()) {
            tags.add(tag);
            tagsAdapter.notifyItemInserted(tags.size() - 1);
            binding.etVideoTag.setText("");
        } else {
            //hide the keyboard
            InputMethodManager imm = (InputMethodManager) MainPage.getInstance().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(binding.etVideoTag.getWindowToken(), 0);
        }
    }

    public void setVideo() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
        if (videoPickerLauncher != null) {
            videoPickerLauncher.launch(intent);
        } else {
            Log.e("UploadVideoFragment", "videoPickerLauncher is not initialized.");
        }
    }

    public void setPhoto() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        if (photoPickerLauncher != null) {
            photoPickerLauncher.launch(intent);
        } else {
            Log.e(TAG, "photoPickerLauncher is not initialized.");
        }
    }

    //todo move to ViewModel
    public Boolean validateUploadable() {
        if (binding.etVideoTitle.getText().toString().trim().isEmpty()) {
            binding.etVideoTitle.setError("Video title is required");
            binding.etVideoTitle.requestFocus();
            return false;
        } else if (binding.etVideoDescription.getText().toString().trim().isEmpty()) {
            binding.etVideoDescription.setError("Video description is required");
            binding.etVideoDescription.requestFocus();
            return false;
        } else if (videoUri == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle("Error");
            builder.setMessage("Please select a video to upload");
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
        enableInput(false);

        MutableLiveData<Boolean> uploadStatus = new MutableLiveData<>();
        uploadStatus.observe(getViewLifecycleOwner(), status -> {
            if (status) {
                Toast.makeText(getContext(), "Video uploaded successfully", Toast.LENGTH_SHORT).show();
                MainPage.getInstance().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new PlayListFragment()).addToBackStack(null).commit();
            } else {
                Toast.makeText(getContext(), "Failed to upload video", Toast.LENGTH_SHORT).show();
            }
            enableInput(true);
        });

        uploadVideoViewModel.upload(videoUri, thumbnailUri, binding.etVideoTitle.getText().toString(), binding.etVideoDescription.getText().toString(), binding.spinnerCategory.getSelectedItem().toString(), tags, uploadStatus);
    }


    private void cancelUpload() {
        MainPage.getInstance().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new PlayListFragment()).addToBackStack(null).commit();
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
                            binding.txtChooseVideo.setText("");
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
                            binding.txtChooseThumbnail.setText("");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        Toast.makeText(getContext(), "Failed to get thumbnail from user", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    public void enableInput(Boolean enable) {
        MainPage.getInstance().runOnUiThread(() -> {
            binding.progressBarUploadVideo.setVisibility(enable ? View.GONE : View.VISIBLE);

            binding.etVideoTitle.setEnabled(enable);
            binding.etVideoDescription.setEnabled(enable);
            binding.spinnerCategory.setEnabled(enable);
            binding.btnAddVideoTag.setEnabled(enable);
            binding.btnChooseThumbnail.setEnabled(enable);
            binding.btnUpload.setEnabled(enable);
            binding.btnCancelUpload.setEnabled(enable);
        });
    }
// todo fix or remove: keep input on theme change, currently may crush the app
//    @Override
//    public void onSaveInstanceState(@NonNull Bundle outState) {
//        super.onSaveInstanceState(outState);
//        if (videoUri != null) {
//            outState.putString("videoUri", videoUri.toString());
//        }
//        if (thumbnailUri != null) {
//            outState.putString("thumbnailUri", thumbnailUri.toString());
//        }
//        outState.putStringArrayList("tags", new ArrayList<>(tags));
//    }
//
//    @Override
//    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
//        super.onViewStateRestored(savedInstanceState);
//        if (savedInstanceState != null) {
//            if (savedInstanceState.containsKey("videoUri")) {
//                videoUri = Uri.parse(savedInstanceState.getString("videoUri"));
//                binding.videoHolder.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.baseline_cloud_done_24));
//                binding.txtChooseVideo.setText("");
//            }
//            if (savedInstanceState.containsKey("thumbnailUri")) {
//                thumbnailUri = Uri.parse(savedInstanceState.getString("thumbnailUri"));
//                executor.execute(() -> {
//                    try {
//                        ImageDecoder.Source source = ImageDecoder.createSource(getContext().getContentResolver(), thumbnailUri);
//                        Bitmap thumbnailBitmap = ImageDecoder.decodeBitmap(source);
//                        MainPage.getInstance().runOnUiThread(() -> {
//                            binding.thumbnailImageHolder.setImageBitmap(thumbnailBitmap);
//                            binding.txtChooseThumbnail.setText("");
//                        });
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                });
//            }
//            if (savedInstanceState.containsKey("tags")) {
//                tags = savedInstanceState.getStringArrayList("tags");
//                tagsAdapter.notifyDataSetChanged();
//            }
//        }
//    }
}
