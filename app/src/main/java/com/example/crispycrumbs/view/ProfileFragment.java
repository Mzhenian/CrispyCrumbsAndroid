package com.example.crispycrumbs.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import android.widget.SearchView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.crispycrumbs.R;
import com.example.crispycrumbs.adapter.PlayList_Adapter;
import com.example.crispycrumbs.viewModel.SubscribeButton;
import com.example.crispycrumbs.dataUnit.UserItem;
import com.example.crispycrumbs.databinding.FragmentProfileBinding;
import com.example.crispycrumbs.localDB.LoggedInUser;
import com.example.crispycrumbs.repository.UserRepository;
import com.example.crispycrumbs.serverAPI.ServerAPI;
import com.example.crispycrumbs.viewModel.ProfileViewModel;
import com.example.crispycrumbs.viewModel.VideoViewModel;

import java.util.ArrayList;

public class ProfileFragment extends Fragment implements SubscribeButton.OnSubscriptionChangeListener {
    private ProfileViewModel viewModel;
    private VideoViewModel videoViewModel;
    private PlayList_Adapter adapter;
    private String userId;

    private FragmentProfileBinding binding;

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
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        // Initialize VideoViewModel
        videoViewModel = new ViewModelProvider(this).get(VideoViewModel.class);

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
                binding.userCountry.setText(userItem.getCountry());

                // Set subscriber count
                binding.userSubscriberCount.setText(userItem.getFollowersCount() + " subscribers");

                // Get current logged-in user
                UserItem currentUser = LoggedInUser.getUser().getValue();

                if (currentUser != null && currentUser.getUserId().equals(userItem.getUserId())) {
                    // Viewing own profile
                    binding.subscribeButton.setVisibility(View.GONE);
                } else {
                    // Viewing someone else's profile
                    binding.subscribeButton.setVisibility(View.VISIBLE);
                    binding.subscribeButton.setUploaderUserId(userItem.getUserId());
                    binding.subscribeButton.setOnSubscriptionChangeListener(this);
                }
            }
        });

        // Set up RecyclerView and Adapter
        UserRepository.getInstance().getUser(userId).observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                adapter = new PlayList_Adapter(getContext(), new ArrayList<>(), null, user);
                binding.rvVideo.setAdapter(adapter);
                binding.rvVideo.setLayoutManager(new LinearLayoutManager(getContext()));
            }

            // Observe videos by the user
            String userIdToFetch;
            if (null == userId) {
                if (null == LoggedInUser.getUser().getValue()) {
                    return;
                }
                userIdToFetch = LoggedInUser.getUser().getValue().getUserId();
            } else {
                userIdToFetch = userId;
            }
            videoViewModel.getVideosByUser(userIdToFetch).observe(getViewLifecycleOwner(), videoList -> {
                if (videoList != null) {
                    adapter.updateVideoList(videoList);
                }
            });
        });

        // Set up search functionality
        binding.searchBar.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (adapter != null) {
                    adapter.filter(query);
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (adapter != null) {
                    adapter.filter(newText);
                }
                return false;
            }
        });

        return view;
    }

    // Implement the SubscribeButton.OnSubscriptionChangeListener interface
    @Override
    public void onSubscriptionChanged(boolean isFollowing, int subscriberCount) {
        if (binding.userSubscriberCount != null) {
            binding.userSubscriberCount.setText(subscriberCount + " subscribers");
        }
    }
}
