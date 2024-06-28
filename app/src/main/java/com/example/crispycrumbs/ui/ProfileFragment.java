package com.example.crispycrumbs.ui;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.crispycrumbs.R;
import com.example.crispycrumbs.data.LoggedInUser;
import com.example.crispycrumbs.data.UserItem;
import com.example.crispycrumbs.databinding.FragmentProfileBinding;
import com.example.crispycrumbs.model.DataManager;


public class ProfileFragment extends Fragment {
    private UserItem user;
    private FragmentProfileBinding binding;

    public ProfileFragment() {
            this.user = LoggedInUser.getUser();
        if (this.user == null) {
            Toast.makeText(getContext(), "please sign in to see your profile", Toast.LENGTH_LONG).show();
            MainPage.getInstance().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new LoginFragment()).commit();
        }
    }
    public ProfileFragment(String userId) {
        this.user =  MainPage.getDataManager().getUserById(userId);
        if (this.user == null) {
            Toast.makeText(getContext(), "no such user ", Toast.LENGTH_SHORT).show();
            MainPage.getInstance().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HomeFragment()).commit();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        if (user == null) {
            Toast.makeText(getContext(), "no such user ", Toast.LENGTH_SHORT).show();
            MainPage.getInstance().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HomeFragment()).commit();
        }
        binding.profilePicture.setImageURI(DataManager.getUriFromResOrFile(user.getProfilePhoto()));
        binding.userName.setText(user.getDisplayedName());
        binding.userEmail.setText(user.getEmail());


        binding.btnMyVideos.setOnClickListener(v -> {
            PlayListFragment playListFragment = new PlayListFragment(user);
            MainPage.getInstance().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, playListFragment)
                    .addToBackStack(null) // Add transaction to back stack
                    .commit();
        });
        //todo enable in next release
//        binding.btnSettings.setOnClickListener(v -> {
//            SettingsFragment settingsFragment = new SettingsFragment();
//            MainPage.getInstance().getSupportFragmentManager().beginTransaction()
//                    .replace(R.id.fragment_container, settingsFragment)
//                    .addToBackStack(null) // Add transaction to back stack
//                    .commit();
//        });

        return view;
    }
}