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
import com.example.crispycrumbs.serverAPI.serverDataUnit.SignUpResponse;
import com.example.crispycrumbs.serverAPI.serverDataUnit.UsernameEmailCheckCallback;
import com.example.crispycrumbs.viewModel.ProfileViewModel;
import com.example.crispycrumbs.viewModel.UserViewModel;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditProfileFragment extends Fragment {
    private static final String TAG = "EditProfileFragment";
    private ProfileViewModel profileViewModel;
    private UserViewModel userViewModel; // Assuming you have a UserViewModel similar to SignUpFragment
    private FragmentEditProfileBinding binding;

    private ActivityResultLauncher<Intent> photoPickerLauncher;
    private Uri photoUri = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentEditProfileBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        // Initialize ViewModels
        profileViewModel = new ViewModelProvider(this).get(ProfileViewModel.class);
        userViewModel = new ViewModelProvider(this).get(UserViewModel.class); // Initialize UserViewModel

        initializeProfilePhotoPicker();

        // Observe logged-in user's data
        profileViewModel.getUser(null).observe(getViewLifecycleOwner(), userItem -> {
            if (null == userItem) {
                return;
            }
            Log.d(TAG, "Loading user data: " + userItem.getUserName());

            // Initialize currentPhotoPath to the existing photo path
            String currentPhotoUrl = ServerAPI.getInstance().constructUrl(userItem.getProfilePhoto());

            // Prepopulate the fields with current user data
            Glide.with(requireContext())
                    .load(currentPhotoUrl)
                    .placeholder(R.drawable.default_profile_picture)
                    .into(binding.btnChangeProfileImg);

            photoUri = Uri.parse(currentPhotoUrl); //todo test me

            binding.editUserName.setText(userItem.getUserName());
            binding.editUserEmail.setText(userItem.getEmail());
            binding.editFullName.setText(userItem.getDisplayedName());
            binding.editPhoneNumber.setText(userItem.getPhoneNumber());

            // Set onClickListener for the button to change profile picture
            binding.btnChangeProfileImg.setOnClickListener(v -> uploadPhoto());

            // Set onClickListener for Save button
            binding.btnUpdate.setOnClickListener(v -> updateProfile(userItem));

            binding.btnDelete.setOnClickListener(v -> profileViewModel.deleteUser());
        });

        return view;
    }

    private void updateProfile(UserItem currentUser) {
        // Disable input to prevent multiple clicks
        enableInput(false);

        String newUserName = binding.editUserName.getText().toString().trim();
        String newUserEmail = binding.editUserEmail.getText().toString().trim();
        String newFullName = binding.editFullName.getText().toString().trim();
        String newPhoneNumber = binding.editPhoneNumber.getText().toString().trim();
        String newPassword = binding.editUserPassword.getText().toString();
        String confirmPassword = binding.confirmPassword.getText().toString();

        // Validate form fields
        if (!validateForm(newUserName, newPassword, confirmPassword, newPhoneNumber, newUserEmail, newFullName, currentUser)) {
            enableInput(true);
            return;
        }

        // Check username availability if it has changed
        if (!newUserName.equals(currentUser.getUserName())) {
            userViewModel.checkUsernameAvailability(newUserName, new UsernameEmailCheckCallback() {
                @Override
                public void onResult(Boolean isAvailable) {
                    if (isAvailable == null) {
                        showError("Failed to check username availability");
                        enableInput(true);
                        return;
                    }
                    if (!isAvailable) {
                        showError("Username is already taken");
                        enableInput(true);
                        return;
                    }
                    // Username is available, proceed to check email
                    checkEmailAndProceed(newUserEmail, newUserName, newFullName, newPhoneNumber, newPassword, currentUser);
                }

                @Override
                public void onFailure(Throwable t) {
                    showError("Failed to check username availability");
                    enableInput(true);
                }
            });
        } else if (!newUserEmail.equals(currentUser.getEmail())) {
            // Username unchanged, check email availability
            userViewModel.checkEmailAvailability(newUserEmail, new UsernameEmailCheckCallback() {
                @Override
                public void onResult(Boolean isAvailable) {
                    if (isAvailable == null) {
                        showError("Failed to check email availability");
                        enableInput(true);
                        return;
                    }
                    if (!isAvailable) {
                        showError("Email is already taken");
                        enableInput(true);
                        return;
                    }
                    // Email is available, proceed to update
                    proceedToUpdate(newUserName, newUserEmail, newFullName, newPhoneNumber, newPassword, currentUser);
                }

                @Override
                public void onFailure(Throwable t) {
                    showError("Failed to check email availability");
                    enableInput(true);
                }
            });
        } else {
            // Username and email unchanged, proceed to update
            proceedToUpdate(newUserName, newUserEmail, newFullName, newPhoneNumber, newPassword, currentUser);
        }
    }

    private void checkEmailAndProceed(String newUserEmail, String newUserName, String newFullName, String newPhoneNumber, String newPassword, UserItem currentUser) {
        userViewModel.checkEmailAvailability(newUserEmail, new UsernameEmailCheckCallback() {
            @Override
            public void onResult(Boolean isAvailable) {
                if (isAvailable == null) {
                    showError("Failed to check email availability");
                    enableInput(true);
                    return;
                }
                if (!isAvailable) {
                    showError("Email is already taken");
                    enableInput(true);
                    return;
                }
                // Email is available, proceed to update
                proceedToUpdate(newUserName, newUserEmail, newFullName, newPhoneNumber, newPassword, currentUser);
            }

            @Override
            public void onFailure(Throwable t) {
                showError("Failed to check email availability");
                enableInput(true);
            }
        });
    }

    private void proceedToUpdate(String newUserName, String newUserEmail, String newFullName, String newPhoneNumber, String newPassword, UserItem currentUser) {
        // Create updated UserItem, leaving the password empty if not updated
        UserItem updatedUser = new UserItem(
                newUserName,
                newFullName,
                newUserEmail,
                newPhoneNumber,
                currentUser.getDateOfBirth(),
                currentUser.getCountry(),
                currentUser.getProfilePhoto()
        );
        if (!newPassword.isEmpty()) {
            updatedUser.setPassword(newPassword);
        }

        // Call ViewModel to update Room and Server
        profileViewModel.updateUser(updatedUser, photoUri, new UserUpdateCallback() {
            @Override
            public void onSuccess() {
                // Navigate to ProfileFragment on success
                Log.d("Update user", "Update successful. Navigating to ProfileFragment.");
                requireActivity().runOnUiThread(() -> {
                    enableInput(true);
                    requireActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, new ProfileFragment())
                            .commit();
                });
            }

            @Override
            public void onFailure(String errorMessage) {
                // Show error message on failure
                requireActivity().runOnUiThread(() -> {
                    Log.e("Update user", "Update failed: " + errorMessage);
                    showError("Update failed: " + errorMessage);
                    enableInput(true);
                });
            }
        });
    }

    // Validate form fields
    private boolean validateForm(String username, String password, String confirmPassword, String phoneNumber, String email, String fullName, UserItem currentUser) {
        if (username == null || username.isEmpty()) {
            showError("Username is required.");
            return false;
        }

        if (username.length() < 3) {
            showError("Username must be at least 3 characters long.");
            return false;
        }

        if (password != null && !password.isEmpty()) {
            if (password.length() < 8) {
                showError("Password must be at least 8 characters long.");
                return false;
            }

            if (!password.equals(confirmPassword)) {
                showError("Passwords do not match.");
                return false;
            }
        }

        if (phoneNumber == null || phoneNumber.isEmpty()) {
            showError("Phone number is required.");
            return false;
        }

        if (phoneNumber.length() < 10 || phoneNumber.length() > 15) {
            showError("Phone number must be between 10 and 15 digits.");
            return false;
        }

        if (email == null || email.isEmpty()) {
            showError("Email is required.");
            return false;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showError("Invalid email format.");
            return false;
        }

        if (fullName == null || fullName.isEmpty()) {
            showError("Full name is required.");
            return false;
        }

        return true; // All fields are valid
    }

    private void showError(String message) {
        requireActivity().runOnUiThread(() -> {
            binding.errorDisplay.setText(message);
            binding.errorDisplay.setVisibility(View.VISIBLE);
        });
    }

    private void enableInput(Boolean enable) {
        requireActivity().runOnUiThread(() -> {

            binding.btnUpdate.setEnabled(enable);
            binding.btnDelete.setEnabled(enable);
            binding.btnChangeProfileImg.setEnabled(enable);
        });
    }

    public void initializeProfilePhotoPicker() {
        photoPickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        try {
                            photoUri = result.getData().getData();
                            if (null == photoUri) {
                                Toast.makeText(getContext(), "Failed to get Profile Picture from user", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            ImageDecoder.Source source = ImageDecoder.createSource(requireContext().getContentResolver(), photoUri);
                            Bitmap profilePicBitmap = ImageDecoder.decodeBitmap(source);
                            binding.btnChangeProfileImg.setImageBitmap(profilePicBitmap);
                        } catch (IOException e) {
                            e.printStackTrace();
                            Toast.makeText(getContext(), "Error loading image", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(getContext(), "Failed to get Profile Picture from user", Toast.LENGTH_SHORT).show();
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
