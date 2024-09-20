package com.example.crispycrumbs.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.crispycrumbs.R;
import com.example.crispycrumbs.databinding.FragmentProfileBinding;
import com.example.crispycrumbs.model.DataManager;
import com.example.crispycrumbs.viewModel.ProfileViewModel;


public class ProfileFragment extends Fragment {
    private ProfileViewModel viewModel;
    private String userId;

    public ProfileFragment() {}

    public ProfileFragment(String userId) {
        this.userId = userId;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(ProfileViewModel.class);
        if (null == userId) {
            if (viewModel.getUser() == null || viewModel.getUser().getValue() == null) {
                Toast.makeText(getContext(), "Please sign in to see your profile.", Toast.LENGTH_LONG).show();
                MainPage.getInstance().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new LoginFragment()).commit();
            }
        } else {
            viewModel.setUser(MainPage.getDataManager().getUserById(userId));
            if (viewModel.getUser() == null || viewModel.getUser().getValue() == null) {
                Toast.makeText(getContext(), "Profile not found.", Toast.LENGTH_SHORT).show();
                MainPage.getInstance().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HomeFragment()).commit();
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        FragmentProfileBinding binding = FragmentProfileBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        if (viewModel.getUser() == null || viewModel.getUser().getValue() == null) {
            return  view;
        }
        binding.profilePicture.setImageURI(DataManager.getUriFromResOrFile((viewModel.getUser().getValue().getProfilePhoto() != null) ? viewModel.getUser().getValue().getProfilePhoto() : DataManager.getDefaultProfilePhoto()));
        binding.userName.setText(viewModel.getUser().getValue().getDisplayedName());
        binding.userEmail.setText(viewModel.getUser().getValue().getEmail());


        binding.btnMyVideos.setOnClickListener(v -> {
            PlayListFragment playListFragment = new PlayListFragment(viewModel.getUser().getValue());
            MainPage.getInstance().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, playListFragment).addToBackStack(null).commit();
        });

        return view;
    }
}