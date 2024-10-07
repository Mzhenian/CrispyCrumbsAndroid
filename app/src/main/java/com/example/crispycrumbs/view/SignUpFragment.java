package com.example.crispycrumbs.view;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.crispycrumbs.R;
import com.example.crispycrumbs.databinding.FragmentSignUpBinding;
import com.example.crispycrumbs.localDB.LoggedInUser;
import com.example.crispycrumbs.serverAPI.ServerAPI;
import com.example.crispycrumbs.serverAPI.serverDataUnit.CheckResponse;
import com.example.crispycrumbs.serverAPI.serverDataUnit.SignUpRequest;
import com.example.crispycrumbs.serverAPI.serverDataUnit.SignUpResponse;
import com.example.crispycrumbs.serverAPI.serverDataUnit.UsernameEmailCheckCallback;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SignUpFragment extends Fragment {
    private static final int REQUEST_IMAGE_PICK = 3;
    private static final String TAG = "SignUpFragment";
    private FragmentSignUpBinding binding;
    private View view;
    private String formattedBirthdayForServer; // Stores the formatted birthday for the server
    private Uri photoUri = Uri.parse("android.resource://" + MainPage.getInstance().getPackageName() + "/" + R.drawable.default_profile_picture);
    private ActivityResultLauncher<Intent> photoPickerLauncher;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSignUpBinding.inflate(inflater, container, false);
        view = binding.getRoot();

        initializeProfilePhotoPicker();

        // Handle the birthday selection using a DatePickerDialog
        binding.btnSelectBirthday.setOnClickListener(v -> showDatePicker());

        binding.btnToSignIn.setOnClickListener(v -> {
            MainPage.getInstance().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new LoginFragment()).commit();
        });

        binding.btnSighUp.setOnClickListener(v -> signUpAttempt());

        binding.btnAddProfileImg.setOnClickListener(v -> uploadPhoto());

        return view;
    }

    // Function to show a DatePickerDialog and format the selected date
    private void showDatePicker() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        // Create a calendar instance for 13 years ago
        final Calendar minAgeCalendar = Calendar.getInstance();
        minAgeCalendar.add(Calendar.YEAR, -13); // Subtract 13 years

        DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), (view, year1, month1, dayOfMonth) -> {
            // Update the selected date and format it to the required formats
            Calendar selectedDate = Calendar.getInstance();
            selectedDate.set(year1, month1, dayOfMonth);

            if (selectedDate.after(minAgeCalendar)) {
                // If selected date is after the date for minimum age (younger than 13), show error
                Toast.makeText(getContext(), "You must be at least 13 years old to sign up.", Toast.LENGTH_SHORT).show();
            } else {
                // Format for display: "17 April 2000"
                SimpleDateFormat displayFormat = new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault());
                String displayDate = displayFormat.format(selectedDate.getTime());
                binding.btnSelectBirthday.setText(displayDate); // Display the formatted date on the button

                // Format for the server: "1989-12-31T22:00:00.000+00:00"
                SimpleDateFormat serverFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.getDefault());
                formattedBirthdayForServer = serverFormat.format(selectedDate.getTime());
            }

        }, year, month, day);

        // Limit the date picker to ensure user can't pick a future date
        datePickerDialog.getDatePicker().setMaxDate(minAgeCalendar.getTimeInMillis());
        datePickerDialog.show();
    }

    private void signUpAttempt() {
        enableInput(false);

        String emailInput = binding.etEmailAddress.getText().toString();
        String usernameInput = binding.etUsername.getText().toString();
        String passwordInput = binding.etPassword.getText().toString();
        String confirmPasswordInput = binding.etConfirmPassword.getText().toString();
        String phoneNumberInput = binding.etPhoneNumber.getText().toString();
        String fullNameInput = binding.etDisplayName.getText().toString();
        String countryInput = binding.etCountry.getText().toString();
        String birthdayInput = formattedBirthdayForServer; // Use the server-formatted birthday

        // Validate form fields based on the requirements
        if (!validateForm(usernameInput, passwordInput, confirmPasswordInput, phoneNumberInput, emailInput, fullNameInput, birthdayInput, countryInput)) {
            enableInput(true);
            return;
        }

        // Check username and email availability
        checkUsernameAndEmailAvailability(usernameInput, emailInput, new UsernameEmailCheckCallback() {
            @Override
            public void onResult(boolean isAvailable) {
                if (isAvailable) {
                    // If available, proceed with sign up
                    signUpUser(emailInput, usernameInput, passwordInput, fullNameInput, phoneNumberInput, birthdayInput, countryInput, photoUri);
                }
            }

            @Override
            public void onFailure(Throwable t) {
                enableInput(true);
                showError("Failed to check username or email availability");
            }
        });
    }

    // Validate form fields
    private boolean validateForm(String username, String password, String confirmPassword, String phoneNumber, String email, String fullName, String birthday, String country) {
        if (username.isEmpty()) {
            showError("Username is required.");
            return false;
        }

        if (username.length() < 3) {
            showError("Username must be at least 3 characters long.");
            return false;
        }

        if (password.isEmpty()) {
            showError("Password is required.");
            return false;
        }

        if (password.length() < 8) {
            showError("Password must be at least 8 characters long.");
            return false;
        }

        if (!password.equals(confirmPassword)) {
            showError("Passwords do not match.");
            return false;
        }

        if (phoneNumber.isEmpty()) {
            showError("Phone number is required.");
            return false;
        }

        if (phoneNumber.length() < 10 || phoneNumber.length() > 15) {
            showError("Phone number must be between 10 and 15 digits.");
            return false;
        }

        if (email.isEmpty()) {
            showError("Email is required.");
            return false;
        }

        if (fullName.isEmpty()) {
            showError("Full name is required.");
            return false;
        }

        if (birthday.isEmpty()) {
            showError("Birthday is required.");
            return false;
        }

        if (country.isEmpty()) {
            showError("Country is required.");
            return false;
        }

        return true; // All fields are valid
    }

    private void checkUsernameAndEmailAvailability(String username, String email, UsernameEmailCheckCallback callback) {
        ServerAPI serverAPI = ServerAPI.getInstance();

        // First check username availability
        serverAPI.checkUsernameAvailability(username, new Callback<CheckResponse>() {
            @Override
            public void onResponse(Call<CheckResponse> call, Response<CheckResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isAvailable()) {
                    // If username is available, check email
                    serverAPI.checkEmailAvailability(email, new Callback<CheckResponse>() {
                        @Override
                        public void onResponse(Call<CheckResponse> call, Response<CheckResponse> response) {
                            if (response.isSuccessful() && response.body() != null && response.body().isAvailable()) {
                                callback.onResult(true); // Username and email are both available
                            } else {
                                showError("Email is already taken");
                                callback.onResult(false);
                            }
                        }

                        @Override
                        public void onFailure(Call<CheckResponse> call, Throwable t) {
                            showError("Failed to check email availability");
                            callback.onResult(false);
                        }
                    });
                } else {
                    showError("Username is already taken");
                    callback.onResult(false);
                }
            }

            @Override
            public void onFailure(Call<CheckResponse> call, Throwable t) {
                showError("Failed to check username availability");
                callback.onResult(false);
            }
        });
    }

    private void signUpUser(String email, String username, String password, String fullName, String phoneNumber, String birthday, String country, Uri photoUri) {
        ServerAPI serverAPI = ServerAPI.getInstance();
        SignUpRequest signUpRequest = new SignUpRequest(username, email, password, fullName, phoneNumber, birthday, country);

        serverAPI.signUp(signUpRequest, photoUri, new Callback<SignUpResponse>() {
            @Override
            public void onResponse(Call<SignUpResponse> call, Response<SignUpResponse> response) {
                MainPage.getInstance().runOnUiThread(() -> {
                    enableInput(true);
                    if (response.isSuccessful() && response.body() != null) {
                        Toast.makeText(getContext(), "Sign Up Successful", Toast.LENGTH_SHORT).show();

                        LoggedInUser.setLoggedInUser(response.body().getUser());
                        LoggedInUser.setToken(response.body().getToken());

                        // Switch to home fragment after sign up
                        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                        transaction.replace(R.id.fragment_container, new HomeFragment());
                        transaction.commit();
                    } else {
                        showError("Sign Up failed: " + response.message());
                    }
                });
            }

            @Override
            public void onFailure(Call<SignUpResponse> call, Throwable t) {
                enableInput(true);
                showError("Sign Up failed: " + t.getMessage());
            }
        });
    }

    private void showError(String message) {
        MainPage.getInstance().runOnUiThread(() -> {
            enableInput(true);
            binding.errorDisplay.setText(message);
            binding.errorDisplay.setVisibility(View.VISIBLE);
        });
    }

    private void enableInput(Boolean enable) {
        binding.signUpProgressBar.setVisibility(enable ? View.GONE : View.VISIBLE);

        binding.btnSighUp.setEnabled(enable);
        binding.btnToSignIn.setEnabled(enable);
        binding.btnAddProfileImg.setEnabled(enable);
        binding.btnSelectBirthday.setEnabled(enable);
    }

    public void initializeProfilePhotoPicker() {
        photoPickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        try {
                            photoUri = result.getData().getData();
                            if (null == photoUri) {
                                Toast.makeText(getContext(), "Failed to get Profile Picture from user", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            ImageDecoder.Source source = ImageDecoder.createSource(getContext().getContentResolver(), photoUri);
                            Bitmap profilePicBitmap = ImageDecoder.decodeBitmap(source);
                            binding.btnAddProfileImg.setImageBitmap(profilePicBitmap);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        Toast.makeText(getContext(), "Failed to get Profile Picture from user", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    // Launch gallery to pick an image
    private void uploadPhoto() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        if (photoPickerLauncher != null) {
            photoPickerLauncher.launch(intent);
        } else {
            Log.e(TAG, "photoPickerLauncher is not initialized.");
        }
    }
}