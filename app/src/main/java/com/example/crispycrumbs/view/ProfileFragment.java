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
import com.example.crispycrumbs.databinding.FragmentProfileBinding;
import com.example.crispycrumbs.serverAPI.ServerAPI;
import com.example.crispycrumbs.viewModel.ProfileViewModel;


public class ProfileFragment extends Fragment {
    private ProfileViewModel viewModel;
    private String userId;

    public ProfileFragment() {
    }

    public ProfileFragment(String userId) {
        this.userId = userId;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize the ProfileViewModel using AndroidViewModelFactory
        viewModel = new ViewModelProvider(this, new ViewModelProvider.AndroidViewModelFactory(getActivity().getApplication())).get(ProfileViewModel.class);

        if (userId == null) {
            viewModel.getUser(null).observe(this, user -> {
                if (user == null) {
                    Toast.makeText(getContext(), "Please sign in to see your profile.", Toast.LENGTH_LONG).show();
                    MainPage.getInstance().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new LoginFragment()).commit();
                }
            });
        } else {
            viewModel.getUser(userId).observe(this, user -> {
                if (user == null) {
                    Toast.makeText(getContext(), "Profile not found.", Toast.LENGTH_SHORT).show();
                    MainPage.getInstance().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HomeFragment()).commit();
                }
            });
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        FragmentProfileBinding binding = FragmentProfileBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        // Observe the user data from ViewModel
        viewModel.getUser(userId).observe(getViewLifecycleOwner(), userItem -> {
            if (userItem != null) {
                // Load profile picture using Glide
                String profilePhotoUrl = userItem.getProfilePhoto();

                // If the user has a profile photo, load it; otherwise, use a default picture
                if (profilePhotoUrl != null && !profilePhotoUrl.isEmpty()) {
                    Glide.with(this)
                            .load(ServerAPI.getInstance().constructUrl(profilePhotoUrl))
                            .circleCrop() // Load from server
                            .placeholder(R.drawable.default_profile_picture) // Placeholder while loading
                            .error(R.drawable.default_profile_picture) // Fallback if load fails
                            .into(binding.profilePicture);
                } else {
                    // Load a default image if no profile photo is available
                    Glide.with(this)
                            .load(R.drawable.default_profile_picture)
                            .into(binding.profilePicture);
                }

                // Set the user details
                binding.userName.setText(userItem.getDisplayedName());
                binding.userEmail.setText(userItem.getEmail());
            }
        });

        // Set up "My Videos" button to navigate to the user's playlist
        binding.btnMyVideos.setOnClickListener(v -> {
            if (viewModel.getUser(null).getValue() != null) {
                PlayListFragment playListFragment = new PlayListFragment(viewModel.getUser(null).getValue());
                MainPage.getInstance().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, playListFragment).addToBackStack(null).commit();
            }
        });

        return view;
    }

}
