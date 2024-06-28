package com.example.crispycrumbs.ui;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.crispycrumbs.data.LoggedInUser;

import com.example.crispycrumbs.R;
import com.example.crispycrumbs.data.UserItem;
import com.example.crispycrumbs.model.UserLogic;

public class LoginFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);
        Button login_button = view.findViewById(R.id.login_button);
        EditText user_name = view.findViewById(R.id.username_input);
        EditText password = view.findViewById(R.id.password_input);
        TextView newUser = view.findViewById(R.id.new_user);

        newUser.setOnClickListener(v -> {
            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new SignUpFragment()).commit();
        });
        // Add a TextWatcher to the password EditText
        password.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // No action needed here
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Check if the password meets the criteria
                if (!UserLogic.isPasswordValid(s.toString())) {
                    password.setError("Password must be at least 8 characters long and contain a mix of letters and digits.");
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                // No action needed here
            }
        });

        // Set an OnClickListener for the login button
        login_button.setOnClickListener(v -> {
            UserItem user = UserLogic.ValidateLogin(user_name.getText().toString(), password.getText().toString());
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
});

        return view;
    }


}