package com.example.crispycrumbs;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class LoginFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        // Find the login button in the layout
        Button login_button = view.findViewById(R.id.login_button);

        // Find the username and password input fields in the layout
        EditText user_name = view.findViewById(R.id.username_input);
        EditText password = view.findViewById(R.id.password_input);

        // Add a TextWatcher to the password EditText to validate the password as the user types
        password.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // No action needed here
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Check if the password meets the criteria while the user is typing
                if (!isPasswordValid(s.toString())) {
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
            // Check if the password is valid when the login button is clicked
            if (isPasswordValid(password.getText().toString())) {
                // Proceed with login if the password is valid
                // For example, show a success message
                Toast.makeText(view.getContext(), "Login Successful", Toast.LENGTH_SHORT).show();
            } else {
                // Show an error message if the password is not valid
                Toast.makeText(view.getContext(), "Password does not meet the criteria", Toast.LENGTH_SHORT).show();
            }
        });

        return view; // Return the created view
    }

    // Method to validate the password
    private boolean isPasswordValid(String password) {
        // Example validation: at least 8 characters long and contains both letters and digits
        if (password.length() < 8) {
            return false;
        }
        boolean hasLetter = false;
        boolean hasDigit = false;
        for (char c : password.toCharArray()) {
            if (Character.isLetter(c)) {
                hasLetter = true;
            } else if (Character.isDigit(c)) {
                hasDigit = true;
            }
        }
        return hasLetter && hasDigit; // Return true if both letters and digits are present
    }
}
