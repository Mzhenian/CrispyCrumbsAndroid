package com.example.crispycrumbs.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.crispycrumbs.R;
import com.example.crispycrumbs.dataUnit.UserItem;
import com.example.crispycrumbs.localDB.LoggedInUser;
import com.example.crispycrumbs.serverAPI.ServerAPI;
import com.example.crispycrumbs.serverAPI.serverDataUnit.LoginResponse;
import com.example.crispycrumbs.serverAPI.serverInterface.LoginCallback;

public class LoginFragment extends Fragment {
    View view;
    Button loginButton;
    EditText userName;
    EditText password;
    TextView newUser;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_login, container, false);
        loginButton = view.findViewById(R.id.login_button);
        userName = view.findViewById(R.id.username_input);
        password = view.findViewById(R.id.password_input);
        newUser = view.findViewById(R.id.new_user);

        newUser.setOnClickListener(v -> {
            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new SignUpFragment()).commit();
        });

        password.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                loginAttempt();
                return true;
            }
            return false;
        });

        loginButton.setOnClickListener(v -> loginAttempt());

        return view;
    }

    //with server
    void loginAttempt() {
        ServerAPI serverAPI = ServerAPI.getInstance();
        serverAPI.login(userName.getText().toString(), password.getText().toString(), true, new LoginCallback() {
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