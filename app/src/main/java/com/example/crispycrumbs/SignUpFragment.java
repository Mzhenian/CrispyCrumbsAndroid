//package com.example.crispycrumbs.ui;
//
//import static androidx.databinding.DataBindingUtil.setContentView;
//
//import android.os.Bundle;
//
//import androidx.fragment.app.Fragment;
//import androidx.fragment.app.FragmentTransaction;
//
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.Button;
//import android.widget.EditText;
//import android.widget.ProgressBar;
//import android.widget.Toast;
//
//import com.example.crispycrumbs.R;
//import com.example.crispycrumbs.UserLogic;
//import com.example.crispycrumbs.databinding.FragmentSignUpBinding;
//
//import java.text.ParseException;
//import java.text.SimpleDateFormat;
//import java.util.Date;
//
//public class SignUpFragment extends Fragment {
//    private FragmentSignUpBinding binding;
//    private static final int PICK_IMAGE = 1;
//
//
//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                             Bundle savedInstanceState) {
//
//        binding =  FragmentSignUpBinding.inflate(inflater, container, false);
//        View view = binding.getRoot();
//
//        final EditText usernameEditText = binding.etUsername;
//        final EditText passwordEditText = binding.etPassword;
//        final Button signUpButton = binding.btnSighUp;
//        final ProgressBar loadingProgressBar = binding.signUpProgressBar;
//
//        binding.btnToSignIn.setOnClickListener(v -> {
//            FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
//            LoginFragment loginFragment = new LoginFragment();
//            transaction.replace(R.id.container, loginFragment);
//            transaction.commit();
//        });
//
//
//
//
//        binding.btnSighUp.setOnClickListener(v -> {
//            loadingProgressBar.setVisibility(View.VISIBLE);
//            signUpButton.setEnabled(false);
//            usernameEditText.setEnabled(false);
//            passwordEditText.setEnabled(false);
//            UserLogic.ValidateSignUp(binding.etEmailAddress.getText().toString(), binding.etUsername.getText().toString(), binding.etPassword.getText().toString(), binding.etConfirmPassword.getText().toString(), binding.etDisplayName.getText().toString(), binding.etPhoneNumber.getText().toString(), binding.etDateOfBirth.toString()) {
//                @Override
//                public void onSuccess() {
//                    loadingProgressBar.setVisibility(View.GONE);
//                    signUpButton.setEnabled(true);
//                    usernameEditText.setEnabled(true);
//                    passwordEditText.setEnabled(true);
//                    Toast.makeText(view.getContext(), "Sign Up Successful", Toast.LENGTH_SHORT).show();
//                }
//
//                @Override
//                public void onError(String error) {
//                    loadingProgressBar.setVisibility(View.GONE);
//                    signUpButton.setEnabled(true);
//                    usernameEditText.setEnabled(true);
//                    passwordEditText.setEnabled(true);
//                    Toast.makeText(view.getContext(), error, Toast.LENGTH_SHORT).show();
//                }
//            });
//        });
//        return view;
//    }
//}

package com.example.crispycrumbs.ui;

import static androidx.databinding.DataBindingUtil.setContentView;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.crispycrumbs.LoggedInUser;
import com.example.crispycrumbs.R;
import com.example.crispycrumbs.UserLogic;
import com.example.crispycrumbs.data.UserItem;
import com.example.crispycrumbs.databinding.FragmentSignUpBinding;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SignUpFragment extends Fragment {
    private FragmentSignUpBinding binding;
    private static final int PICK_IMAGE = 1;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding =  FragmentSignUpBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        final EditText usernameEditText = binding.etUsername;
        final EditText passwordEditText = binding.etPassword;
        final Button signUpButton = binding.btnSighUp;
        final ProgressBar loadingProgressBar = binding.signUpProgressBar;

        binding.btnToSignIn.setOnClickListener(v -> {
            FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
            LoginFragment loginFragment = new LoginFragment();
            transaction.replace(R.id.container, loginFragment);
            transaction.commit();
        });

        binding.btnSighUp.setOnClickListener(v -> {
            loadingProgressBar.setVisibility(View.VISIBLE);
            signUpButton.setEnabled(false);
            usernameEditText.setEnabled(false);
            passwordEditText.setEnabled(false);
            String returnedError = UserLogic.ValidateSignUp(binding.etEmailAddress.getText().toString(), binding.etUsername.getText().toString(), binding.etPassword.getText().toString(), binding.etConfirmPassword.getText().toString(), binding.etDisplayName.getText().toString(), binding.etPhoneNumber.getText().toString(), binding.etDateOfBirth.toString());
            if (returnedError == null) {
                UserItem newUser = new UserItem(binding.etUsername.getText().toString(), binding.etPassword.getText().toString(), binding.etDisplayName.getText().toString(), binding.etEmailAddress.getText().toString(), binding.etPhoneNumber.getText().toString(), new Date(), null, "default");
                LoggedInUser.SetLoggedInUser(newUser);
                loadingProgressBar.setVisibility(View.GONE);
                Toast.makeText(view.getContext(), "Sign Up Successful, welcome " + newUser.getDisplayedName(), Toast.LENGTH_SHORT).show();

                FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                HomeFragment homeFragment = new HomeFragment();
                transaction.replace(R.id.container, homeFragment);
                transaction.commit();
            } else {
                TextView errorDisplay = binding.errorDisplay;
                errorDisplay.setText(returnedError);
                errorDisplay.setVisibility(View.VISIBLE);
                loadingProgressBar.setVisibility(View.GONE);
                signUpButton.setEnabled(true);
                usernameEditText.setEnabled(true);
                passwordEditText.setEnabled(true);

            }
        });
        return view;
    }
}