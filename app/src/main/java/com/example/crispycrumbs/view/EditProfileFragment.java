package com.example.crispycrumbs.view;

import android.app.Activity;
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
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.crispycrumbs.R;
import com.example.crispycrumbs.dataUnit.UserItem;
import com.example.crispycrumbs.databinding.FragmentEditProfileBinding;
import com.example.crispycrumbs.serverAPI.ServerAPI;
import com.example.crispycrumbs.serverAPI.serverInterface.UserUpdateCallback;
import com.example.crispycrumbs.viewModel.ProfileViewModel;

import java.io.IOException;

public class EditProfileFragment extends Fragment {
    private static final String TAG = "EditProfileFragment";
    private ProfileViewModel viewModel;
    private FragmentEditProfileBinding binding;

    private ActivityResultLauncher<Intent> photoPickerLauncher;
    private Uri PhotoUri = null;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentEditProfileBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(ProfileViewModel.class);

        initializeProfilePhotoPicker();

        // Observe logged-in user's data
        viewModel.getUser(null).observe(getViewLifecycleOwner(), userItem -> {
            if (null == userItem) {
                return;
            }
            Log.d(TAG, "Loading user data: " + userItem.getUserName());

            // Initialize currentPhotoPath to the existing photo path
            String currentPhotoUrl = ServerAPI.getInstance().constructUrl(userItem.getProfilePhoto());

            // Prepopulate the fields with current user data
            Glide.with(MainPage.getInstance())
                    .load(currentPhotoUrl)
                    .placeholder(R.drawable.default_profile_picture)
                    .into(binding.profilePicture);


            PhotoUri = Uri.parse(currentPhotoUrl); //todo test me

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

                // Get password input
                String newPassword = binding.editUserPassword.getText().toString();  // New password field
                String confirmPassword = binding.confirmPassword.getText().toString();  // Confirmation field

                // Validate required fields
                if (newUserName.isEmpty() || newUserEmail.isEmpty() || newFullName.isEmpty() || newPhoneNumber.isEmpty()) {
                    Toast.makeText(getContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Validate password
                if (!newPassword.isEmpty() || !confirmPassword.isEmpty()) {
                    if (!newPassword.equals(confirmPassword)) {
                        Toast.makeText(getContext(), "Passwords do not match", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }

                Log.d("Update user", "Save button clicked. Updated user data: Username = " + newUserName + ", Email = " + newUserEmail);

                // Create updated UserItem, leaving the password empty if not updated
                UserItem updatedUser = new UserItem(
                        newUserName,
                        newFullName,
                        newUserEmail,
                        newPhoneNumber,
                        userItem.getDateOfBirth(),
                        userItem.getCountry(),
                        currentPhotoUrl //todo test it's in the right format
                );
                if (!newPassword.isEmpty()) {
                    updatedUser.setPassword(newPassword);
                }

                // Call ViewModel to update Room and Server
                viewModel.updateUser(updatedUser, PhotoUri, new UserUpdateCallback() {
                    @Override
                    public void onSuccess() {
                        // Navigate to ProfileFragment on success
                        Log.d("Update user", "Update successful. Navigating to ProfileFragment.");
                        MainPage.getInstance().getSupportFragmentManager().beginTransaction()
                                .replace(R.id.fragment_container, new ProfileFragment())
                                .commit();
                    }

                    @Override
                    public void onFailure(String errorMessage) {
                        // Show error message on failure
                        Log.e("Update user", "Update failed: " + errorMessage);
                        getActivity().runOnUiThread(() -> {
                            Toast.makeText(getContext(), "Update failed: " + errorMessage, Toast.LENGTH_SHORT).show();
                        });
                    }
                });
            });

            binding.btnDelete.setOnClickListener(v -> viewModel.deleteUser());
        });

        return view;
    }

    public void initializeProfilePhotoPicker() {
        photoPickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        try {
                            PhotoUri = result.getData().getData();
                            if (PhotoUri == null) {
                                Toast.makeText(getContext(), "Failed to get thumbnail from user", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            ImageDecoder.Source source = ImageDecoder.createSource(getContext().getContentResolver(), PhotoUri);
                            Bitmap thumbnailBitmap = ImageDecoder.decodeBitmap(source);
                            binding.profilePicture.setImageBitmap(thumbnailBitmap);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        Toast.makeText(getContext(), "Failed to get thumbnail from user", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    // Launch gallery to pick an image
    private void uploadPhoto() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        if (photoPickerLauncher != null) {
            photoPickerLauncher.launch(intent);
        } else {
            Log.e(TAG, "photoPickerLauncher is not initialized.");
        }
    }

}
