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
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.crispycrumbs.R;
import com.example.crispycrumbs.dataUnit.UserItem;
import com.example.crispycrumbs.databinding.FragmentSignUpBinding;
import com.example.crispycrumbs.localDB.LoggedInUser;
import com.example.crispycrumbs.localDB.LoggedInUser;
import com.example.crispycrumbs.serverAPI.ServerAPI;
import com.example.crispycrumbs.serverAPI.serverDataUnit.SignUpRequest;
import com.example.crispycrumbs.serverAPI.serverDataUnit.SignUpResponse;
import com.example.crispycrumbs.serverAPI.serverDataUnit.UsernameEmailCheckCallback;
import com.example.crispycrumbs.viewModel.UserViewModel;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SignUpFragment extends Fragment {
    private static final String TAG = "SignUpFragment";
    private FragmentSignUpBinding binding;
    private View view;
    private UserViewModel viewModel;

    private String formattedBirthdayForServer; // Stores the formatted birthday for the server
    private Uri photoUri = Uri.parse("android.resource://" + MainPage.getInstance().getPackageName() + "/" + R.drawable.default_profile_picture);
    private ActivityResultLauncher<Intent> photoPickerLauncher;
    private ServerAPI serverAPI;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSignUpBinding.inflate(inflater, container, false);
        view = binding.getRoot();
        viewModel = new UserViewModel(MainPage.getInstance().getApplication());

        serverAPI = ServerAPI.getInstance();

        initializeProfilePhotoPicker();

        // Initialize spinner with country array
        Spinner countrySpinner = binding.spinnerCountry;
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.countries_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        countrySpinner.setAdapter(adapter);

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
        minAgeCalendar.add(Calendar.DAY_OF_MONTH, 1);


        DatePickerDialog datePickerDialog = new DatePickerDialog(MainPage.getInstance(), (view, year1, month1, dayOfMonth) -> {
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
        String birthdayInput = formattedBirthdayForServer; // Use the server-formatted birthday
        String countryInput = binding.spinnerCountry.getSelectedItem().toString(); // Get selected country from Spinner

        // Validate form fields based on the requirements
        if (!validateForm(usernameInput, passwordInput, confirmPasswordInput, phoneNumberInput, emailInput, fullNameInput, birthdayInput, countryInput)) {
            enableInput(true);
            return;
        }

        signUpUser(emailInput, usernameInput, passwordInput, fullNameInput, phoneNumberInput, birthdayInput, countryInput, photoUri);
    }

    // Validate form fields
    private boolean validateForm(String username, String password, String confirmPassword, String phoneNumber, String email, String fullName, String birthday, String country) {
        if (null == username || username.isEmpty()) {
            showError("Username is required.");
            return false;
        }

        if (username.length() < 3) {
            showError("Username must be at least 3 characters long.");
            return false;
        }

        if (null == password || password.isEmpty()) {
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

        if (null == phoneNumber || phoneNumber.isEmpty()) {
            showError("Phone number is required.");
            return false;
        }

        if (phoneNumber.length() < 10 || phoneNumber.length() > 15) {
            showError("Phone number must be between 10 and 15 digits.");
            return false;
        }

        if (null == email || email.isEmpty()) {
            showError("Email is required.");
            return false;
        }

        if (null == fullName || fullName.isEmpty()) {
            showError("Full name is required.");
            return false;
        }

        if (null == birthday || birthday.isEmpty()) {
            showError("Birthday is required.");
            return false;
        }

        if (null == country || country.isEmpty()) {
            showError("Country is required.");
            return false;
        }

        return true; // All fields are valid
    }

    private void signUpUser(String email, String username, String password, String fullName, String phoneNumber, String birthday, String country, Uri photoUri) {
        SignUpRequest signUpRequest = new SignUpRequest(username, email, password, fullName, phoneNumber, birthday, country);
        serverAPI.signUp(signUpRequest, photoUri, new Callback<SignUpResponse>() {
            @Override
            public void onResponse(Call<SignUpResponse> call, Response<SignUpResponse> signUpResponse) {
                MainPage.getInstance().runOnUiThread(() -> {
                    enableInput(true);
                    if (signUpResponse.isSuccessful() && signUpResponse.body() != null) {
                        Toast.makeText(getContext(), "Sign Up Successful", Toast.LENGTH_SHORT).show();

                        LoggedInUser.setLoggedInUser(signUpResponse.body().getUser());
                        LoggedInUser.setToken(signUpResponse.body().getToken());

                        // Switch to home fragment after sign up
                        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                        transaction.replace(R.id.fragment_container, new HomeFragment());
                        transaction.commit();
                        return;
                    }

                    //Signup got to the server and failed:
                    viewModel.checkUsernameAvailability(username, new UsernameEmailCheckCallback() {
                        @Override
                        public void onResult(Boolean isAvailable) {
                            if (null == isAvailable) {
                                showError("Failed to check username availability");
                                return;
                            }
                            if (!isAvailable) {
                                showError("Username is already taken");
                                return;
                            }

                            // Check email availability
                            viewModel.checkEmailAvailability(email, new UsernameEmailCheckCallback() {
                                @Override
                                public void onResult(Boolean isAvailable) {
                                    if (null == isAvailable) {
                                        showError("Failed to check email availability");
                                        return;
                                    }
                                    if (!isAvailable) {
                                        showError("Email is already taken");
                                        return;
                                    }
                                    showError("Sign Up failed: " + signUpResponse.message());
                                }

                                @Override
                                public void onFailure(Throwable t) {
                                    showError("Failed to check email availability");
                                }
                            });
                        }

                        @Override
                        public void onFailure(Throwable t) {
                            showError("Failed to check username availability");
                        }
                    });
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
        MainPage.getInstance().runOnUiThread(() -> {
            binding.signUpProgressBar.setVisibility(enable ? View.GONE : View.VISIBLE);

            binding.btnSighUp.setEnabled(enable);
            binding.btnToSignIn.setEnabled(enable);
            binding.btnAddProfileImg.setEnabled(enable);
            binding.btnSelectBirthday.setEnabled(enable);
        });
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
