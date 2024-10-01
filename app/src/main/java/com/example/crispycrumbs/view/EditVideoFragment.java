package com.example.crispycrumbs.view;

import android.app.Activity;
import android.content.ContentResolver;
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
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.example.crispycrumbs.R;
import com.example.crispycrumbs.adapter.TagsAdapter;
import com.example.crispycrumbs.dataUnit.PreviewVideoCard;
import com.example.crispycrumbs.databinding.FragmentEditVideoBinding;
import com.example.crispycrumbs.localDB.LoggedInUser;
import com.example.crispycrumbs.model.DataManager;
import com.example.crispycrumbs.serverAPI.ServerAPI;
import com.example.crispycrumbs.viewModel.EditVideoViewModel;
import com.example.crispycrumbs.viewModel.UploadVideoViewModel;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okio.BufferedSink;
import okio.Okio;
import okio.Source;

public class EditVideoFragment extends Fragment {
    private static final String TAG = "EditVideoFragment";
    private FragmentEditVideoBinding binding;
    private EditVideoViewModel viewModel;

    private PreviewVideoCard video;
    private Uri thumbnailUri = null;
    private TagsAdapter tagsAdapter;
    private List<String> tags = new ArrayList<>();
    private ActivityResultLauncher<Intent> photoPickerLauncher;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentEditVideoBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        Bundle bundle = getArguments();
        viewModel = new ViewModelProvider(this).get(EditVideoViewModel.class);

        try {
            video = (PreviewVideoCard) bundle.getSerializable("video");
            if (null == video) {
                throw new NullPointerException();
            }
        } catch (NullPointerException e) {
            Toast.makeText(getContext(), "missing a video to edit.", Toast.LENGTH_SHORT).show();
            MainPage.getInstance().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new UploadVideoFragment()).commit();
            return view;
        }
        viewModel.setVideo(new MutableLiveData<>(video));

        if (null == LoggedInUser.getUser().getValue() || null == LoggedInUser.getUser().getValue().getUserId() || !(LoggedInUser.getUser().getValue().getUserId().equals(video.getUserId()))) {
            MainPage.getInstance().showLoginSnackbar(view, "Only the video owner can edit it.");
            MainPage.getInstance().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HomeFragment()).commit();
            return view;
        }

        binding.titleEditVideoEdit.setText("Editing: " + video.getTitle() + " #" + video.getVideoId());

        String thumbnailUrl = ServerAPI.getInstance().constructUrl(video.getThumbnail());
        Glide.with(MainPage.getInstance())
                .load(thumbnailUrl)
                .placeholder(R.drawable.default_video_thumbnail)
                .into(binding.imgOriginalThumbailEdit);


        binding.etVideoTitleEdit.setText(video.getTitle());
        binding.etVideoDescriptionEdit.setText(video.getDescription());

        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, UploadVideoViewModel.CATEGORIES);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerCategoryEdit.setAdapter(categoryAdapter);
        binding.spinnerCategoryEdit.setSelection(categoryAdapter.getPosition(video.getCategory()));

        tagsAdapter = new TagsAdapter(tags);

        for (String tag : video.getTags()) {
            addTag(tag);
        }

        binding.rvTagsPreviewEdit.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.rvTagsPreviewEdit.setAdapter(tagsAdapter);

        binding.btnAddVideoTagEdit.setOnClickListener(v -> addTag());

        initializeThumbnailPicker();
        binding.btnChooseThumbnailEdit.setOnClickListener(v -> setPhoto());
        binding.btnUpdate.setOnClickListener(v -> updateVideo());
        binding.btnDelete.setOnClickListener(v -> delete());

        return view;
    }

    public void initializeThumbnailPicker() {
        photoPickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        try {
                            thumbnailUri = result.getData().getData();
                            if (null == thumbnailUri) {
                                Toast.makeText(getContext(), "Failed to get thumbnail from user", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            ImageDecoder.Source source = ImageDecoder.createSource(getContext().getContentResolver(), thumbnailUri);
                            Bitmap thumbnailBitmap = ImageDecoder.decodeBitmap(source);
                            binding.thumbnailImageHolderEdit.setImageBitmap(thumbnailBitmap);
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

    private void addTag() {
        String trimmedTag = binding.etVideoTagEdit.getText().toString().trim();
        if (!trimmedTag.isEmpty()) {
            addTag(trimmedTag);
        }
    }

    private void addTag(String tag) {
        String trimmedTag = tag.trim();
        if (!trimmedTag.isEmpty()) {
            tags.add(tag);
            tagsAdapter.notifyItemInserted(tags.size() - 1);
            binding.etVideoTagEdit.setText("");
        }
    }

    private void setPhoto() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        if (photoPickerLauncher != null) {
            photoPickerLauncher.launch(intent);
        } else {
            Log.e("UploadVideoFragment", "photoPickerLauncher is not initialized.");
        }
    }

    private void updateVideo() {
//        disableEdit();
        Toast.makeText(getContext(), "Updating video...", Toast.LENGTH_LONG).show();

        // Collect video fields from the UI
        String title = binding.etVideoTitleEdit.getText().toString().trim();
        String description = binding.etVideoDescriptionEdit.getText().toString().trim();
        String category = binding.spinnerCategoryEdit.getSelectedItem().toString();
        String tagsString = String.join(",", tags);
        MultipartBody.Part thumbnailPart = null;

        // Create RequestBody for each field
        Map<String, RequestBody> videoFields = new HashMap<>();
        if (!title.isEmpty()) {
            videoFields.put("title", RequestBody.create(MediaType.parse("text/plain"), title));
        }
        if (!description.isEmpty()) {
            videoFields.put("description", RequestBody.create(MediaType.parse("text/plain"), description));
        }
        if (!category.isEmpty()) {
            videoFields.put("category", RequestBody.create(MediaType.parse("text/plain"), category));
        }
        if (!tagsString.isEmpty()) {
            videoFields.put("tags", RequestBody.create(MediaType.parse("text/plain"), tagsString));
        }

        if (null != thumbnailUri) {
            // Create MultipartBody.Part for the thumbnail if it exists
            try {
                ContentResolver contentResolver = MainPage.getInstance().getContentResolver();
                InputStream thumbnailInputStream = contentResolver.openInputStream(thumbnailUri);
                if (thumbnailInputStream == null) {
                    throw new FileNotFoundException("Unable to open input stream for thumbnail URI");
                }

                RequestBody requestBodyImage = new RequestBody() {
                    @Override
                    public MediaType contentType() {
                        return MediaType.parse("image/*");  // Set the media type to video
                    }

                    @Override
                    public void writeTo(BufferedSink sink) throws IOException {
                        // Write the InputStream to the BufferedSink
                        try (Source source = Okio.source(thumbnailInputStream)) {
                            sink.writeAll(source);
                        }
                    }
                };

                thumbnailPart = MultipartBody.Part.createFormData("thumbnail", DataManager.getFileNameFromUri(thumbnailUri), requestBodyImage);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                return;
            }
        }
        viewModel.updateVideo(videoFields, thumbnailPart);
    }

    private void delete() {
//        disableEdit();
        Toast.makeText(getContext(), "deleting...", Toast.LENGTH_LONG).show();
        viewModel.deleteVideo();
    }

    private void disableEdit() {
        binding.progressBarEdit.setVisibility(View.VISIBLE);
        binding.etVideoTitleEdit.setEnabled(false);
        binding.etVideoDescriptionEdit.setEnabled(false);
        binding.spinnerCategoryEdit.setEnabled(false);
        binding.btnAddVideoTagEdit.setEnabled(false);
        binding.btnChooseThumbnailEdit.setEnabled(false);
        binding.btnUpdate.setEnabled(false);
        binding.btnDelete.setEnabled(false);
    }

    private void enableEdit() {
        binding.progressBarEdit.setVisibility(View.GONE);
        binding.etVideoTitleEdit.setEnabled(true);
        binding.etVideoDescriptionEdit.setEnabled(true);
        binding.spinnerCategoryEdit.setEnabled(true);
        binding.btnAddVideoTagEdit.setEnabled(true);
        binding.btnChooseThumbnailEdit.setEnabled(true);
        binding.btnUpdate.setEnabled(true);
        binding.btnDelete.setEnabled(true);
    }
}