package com.example.crispycrumbs.view;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.crispycrumbs.data.LoggedInUser;

import com.example.crispycrumbs.R;
import com.example.crispycrumbs.data.UserItem;
import com.example.crispycrumbs.model.UserLogic;

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

        // Set an OnClickListener for the login button
        loginButton.setOnClickListener(v -> loginAttempt());

        return view;
    }
    void loginAttempt() {
        UserItem user = UserLogic.ValidateLogin(userName.getText().toString(), password.getText().toString());
        if (user != null) {
            LoggedInUser.SetLoggedInUser(user);

            Toast.makeText(getContext(), "welcome back " + user.getDisplayedName(), Toast.LENGTH_SHORT).show();

            FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
            HomeFragment homeFragment = new HomeFragment();
            transaction.replace(R.id.fragment_container, homeFragment);
            transaction.commit();
        } else {
            Toast.makeText(getContext(), "Username or password is incorrect", Toast.LENGTH_SHORT).show();
        }
    }
}