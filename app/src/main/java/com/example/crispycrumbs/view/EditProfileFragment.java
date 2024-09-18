package com.example.crispycrumbs.view;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.crispycrumbs.R;
import com.example.crispycrumbs.dataUnit.UserItem;
import com.example.crispycrumbs.databinding.FragmentEditProfileBinding;
import com.example.crispycrumbs.localDB.LoggedInUser;
import com.example.crispycrumbs.serverAPI.ServerAPI;
import com.example.crispycrumbs.viewModel.ProfileViewModel;

import java.io.File;
import java.io.IOException;

public class EditProfileFragment extends Fragment {

    private static final int REQUEST_IMAGE_PICK = 3;
    private ProfileViewModel viewModel;
    private FragmentEditProfileBinding binding;
    private String currentPhotoPath;  // Path to the selected image

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentEditProfileBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(ProfileViewModel.class);

        // Observe logged-in user's data
        viewModel.getUser(null).observe(getViewLifecycleOwner(), userItem -> {
            if (userItem != null) {
                // Initialize currentPhotoPath to the existing photo path
                currentPhotoPath = userItem.getProfilePhoto();  // Set to existing photo

                // Prepopulate the fields with current user data
                Glide.with(this)
                        .load(userItem.getProfilePhoto() != null ? ServerAPI.getInstance().constructUrl(userItem.getProfilePhoto()) : R.drawable.default_profile_picture)
                        .circleCrop()  // Ensures the image is loaded as a circle
                        .into(binding.profilePicture);


                binding.editUserName.setText(userItem.getUserName());
                binding.editUserEmail.setText(userItem.getEmail());
                binding.editFullName.setText(userItem.getDisplayedName());
                binding.editPhoneNumber.setText(userItem.getPhoneNumber());

                // Set onClickListener for the button to change profile picture
                binding.btnChangeProfileImg.setOnClickListener(v -> uploadPhoto());

                // Set onClickListener for Save button
                binding.btnSave.setOnClickListener(v -> {
                    String newUserName = binding.editUserName.getText().toString();
                    String newUserEmail = binding.editUserEmail.getText().toString();
                    String newFullName = binding.editFullName.getText().toString();
                    String newPhoneNumber = binding.editPhoneNumber.getText().toString();

                    // Validate fields
                    if (newUserName.isEmpty() || newUserEmail.isEmpty() || newFullName.isEmpty() || newPhoneNumber.isEmpty()) {
                        Toast.makeText(getContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Log.d("EditProfile", "Current photo path: " + currentPhotoPath);

                    // Create updated UserItem
                    UserItem updatedUser = new UserItem(
                            newUserName,
                            userItem.getPassword(),
                            newFullName,
                            newUserEmail,
                            newPhoneNumber,
                            userItem.getDateOfBirth(),
                            userItem.getCountry(),
                            currentPhotoPath  // Use the current photo path
                    );

                    // Only create the File if currentPhotoPath is not null
                    File profilePhotoFile = null;
                    if (currentPhotoPath != null) {
                        String realPath = getRealPathFromUri(Uri.parse(currentPhotoPath));
                        if (realPath != null) {
                            profilePhotoFile = new File(realPath);  // Create the file only if realPath is valid
                        }
                    }

                    // Call ViewModel to update Room and Server
                    viewModel.updateUser(updatedUser, profilePhotoFile);
                    Toast.makeText(getContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show();

                    updatedUser.setUserId(LoggedInUser.getUser().getValue().getUserId());
                    updatedUser.setProfilePhoto(currentPhotoPath);
                    Log.d("EditProfile", "Updated User ID: " + updatedUser.getUserId());
                    Log.d("EditProfile", "Updated User image path: " + updatedUser.getProfilePhoto());

                    // Update the logged-in user
                    LoggedInUser.setLoggedInUser(updatedUser);

                    // Refresh user data
                    viewModel.refreshUser(updatedUser.getUserId());
                });
            }
        });

        return view;
    }


    // Launch gallery to pick an image
    private void uploadPhoto() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_IMAGE_PICK);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && data != null) {
            if (requestCode == REQUEST_IMAGE_PICK) {
                Uri photoUri = data.getData();
                try {
                    // Set selected image to profile picture view
                    Bitmap thumbnailBitmap = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), photoUri);
                    binding.profilePicture.setImageBitmap(thumbnailBitmap);

                    // Save the photo URI
                    currentPhotoPath = photoUri.toString();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // Helper method to get the real file path from Uri
    private String getRealPathFromUri(Uri uri) {
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContext().getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null) {
            int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            String filePath = cursor.getString(columnIndex);
            cursor.close();
            return filePath;
        }
        return null;
    }
}
