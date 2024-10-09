package com.example.crispycrumbs.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.crispycrumbs.R;
import com.example.crispycrumbs.databinding.FragmentLoginBinding;
import com.example.crispycrumbs.localDB.LoggedInUser;
import com.example.crispycrumbs.repository.UserRepository;
import com.example.crispycrumbs.serverAPI.serverDataUnit.LoginResponse;
import com.example.crispycrumbs.serverAPI.serverInterface.LoginCallback;

public class LoginFragment extends Fragment {
    FragmentLoginBinding binding;
    View view;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentLoginBinding.inflate(inflater, container, false);
        view = binding.getRoot();

        binding.newUser.setOnClickListener(v -> {
            MainPage.getInstance().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new SignUpFragment()).commit();
        });

        binding.passwordInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                loginAttempt();
                return true;
            }
            return false;
        });

        binding.loginButton.setOnClickListener(v -> loginAttempt());

        return view;
    }

    //with server
    void loginAttempt() {
        UserRepository.getInstance().login(binding.usernameInput.getText().toString(), binding.passwordInput.getText().toString(), true, new LoginCallback() {
            @Override
            public void onSuccess(LoginResponse loginResponse) {
                LoggedInUser.setLoggedInUser(loginResponse.getUser());
                LoggedInUser.setToken(loginResponse.getToken());

                getActivity().runOnUiThread(() -> {
//                    Toast.makeText(getContext(), "welcome back " + LoggedInUser.getUser().getValue().getDisplayedName(), Toast.LENGTH_SHORT).show();

                    FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                    HomeFragment homeFragment = new HomeFragment();
                    transaction.replace(R.id.fragment_container, homeFragment);
                    transaction.commit();
                });
            }

            @Override
            public void onFailure(Throwable t, int statusCode) {
                getActivity().runOnUiThread(() -> {
                    if (statusCode == 400) {
                        Toast.makeText(getContext(), "Username or password is incorrect", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), "Login failed: " + (-1 == statusCode ? "" : statusCode) + " " + t.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }
}