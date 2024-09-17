package com.example.crispycrumbs.view;

import android.os.Bundle;
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
import com.example.crispycrumbs.serverAPI.ServerAPI;
import com.example.crispycrumbs.viewModel.ProfileViewModel;

import java.io.File;

public class EditProfileFragment extends Fragment {

    private ProfileViewModel viewModel;
    private FragmentEditProfileBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentEditProfileBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(ProfileViewModel.class);

        // Observe logged-in user's data
        viewModel.getUser(null).observe(getViewLifecycleOwner(), userItem -> {
            if (userItem != null) {
                // Prepopulate the fields with current user data
                Glide.with(this)
                        .load(userItem.getProfilePhoto() != null ? ServerAPI.getInstance().constructUrl(userItem.getProfilePhoto()) : R.drawable.default_profile_picture)
                        .into(binding.profilePicture);

                binding.editUserName.setText(userItem.getDisplayedName());
                binding.editUserEmail.setText(userItem.getEmail());
                binding.editFullName.setText(userItem.getDisplayedName());  // Add fullName
                binding.editPhoneNumber.setText(userItem.getPhoneNumber());  // Add phone number


                // Set onClickListener for Save button
                binding.btnSave.setOnClickListener(v -> {
                    // Update user details
                    String newUserName = binding.editUserName.getText().toString();
                    String newUserEmail = binding.editUserEmail.getText().toString();
                    String newFullName = binding.editFullName.getText().toString();  // Capture full name
                    String newPhoneNumber = binding.editPhoneNumber.getText().toString();  // Capture phone number

                    // Validate fields
                    if (newUserName.isEmpty() || newUserEmail.isEmpty() || newFullName.isEmpty() || newPhoneNumber.isEmpty()) {
                        Toast.makeText(getContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Create updated UserItem
                    UserItem updatedUser = new UserItem(
                            newUserName,                // User name
                            userItem.getPassword(),      // Keep the same password
                            newFullName,                 // Full name
                            newUserEmail,                // Email
                            newPhoneNumber,              // Phone number
                            userItem.getDateOfBirth(),   // Keep existing date of birth
                            userItem.getCountry(),       // Keep existing country
                            userItem.getProfilePhoto()   // Keep existing profile photo
                    );

                    // Check if the user updated their profile photo
                    File profilePhotoFile = null;
                    // Logic to get the profile photo file if the user updated it

                    // Call the ViewModel to update Room and Server
                    viewModel.updateUser(updatedUser, profilePhotoFile);
                    Toast.makeText(getContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show();
                    viewModel.refreshUser(updatedUser.getUserId());
                });
            }
        });

        return view;
    }
}
