package com.example.crispycrumbs.view;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.crispycrumbs.R;
import com.example.crispycrumbs.dataUnit.UserItem;
import com.example.crispycrumbs.databinding.FragmentSignUpBinding;
import com.example.crispycrumbs.localDB.LoggedInUser;
import com.example.crispycrumbs.model.DataManager;
import com.example.crispycrumbs.model.UserLogic;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SignUpFragment extends Fragment {
    private static final int REQUEST_IMAGE_PICK = 3;
    private FragmentSignUpBinding binding;
    private String currentPhotoPath;
    private Uri photoURI;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSignUpBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        final EditText usernameEditText = binding.etUsername;
        final EditText passwordEditText = binding.etPassword;
        final Button signUpButton = binding.btnSighUp;
        final ProgressBar loadingProgressBar = binding.signUpProgressBar;

        binding.btnToSignIn.setOnClickListener(v -> {
            MainPage.getInstance().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new LoginFragment()).commit();
        });

        binding.btnAddProfileImg.setOnClickListener(v -> uploadPhoto());


        binding.btnSighUp.setOnClickListener(v -> {
            loadingProgressBar.setVisibility(View.VISIBLE);
            signUpButton.setEnabled(false);
            usernameEditText.setEnabled(false);
            passwordEditText.setEnabled(false);
            String returnedError = UserLogic.ValidateSignUp(binding.etEmailAddress.getText().toString(), binding.etUsername.getText().toString(), binding.etPassword.getText().toString(), binding.etConfirmPassword.getText().toString(), binding.etDisplayName.getText().toString(), binding.etPhoneNumber.getText().toString(), binding.etDateOfBirth.toString());
            if (returnedError == null) {
                DataManager dataManager = DataManager.getInstance();
                // Use default profile picture if no photo is taken
                String profilePicPath = DataManager.getUriFromResOrFile(currentPhotoPath).toString();
//                currentPhotoPath != null ? currentPhotoPath : "android.resource://" + getContext().getPackageName() + "/" + R.drawable.default_profile_picture;
                UserItem newUser = dataManager.createUser(view.getContext(), binding.etUsername.getText().toString(), binding.etPassword.getText().toString(), binding.etDisplayName.getText().toString(), binding.etEmailAddress.getText().toString(), binding.etPhoneNumber.getText().toString(), new Date(), null, profilePicPath);
                dataManager.addUser(newUser);
                LoggedInUser.setLoggedInUser(newUser);
                loadingProgressBar.setVisibility(View.GONE);
                Toast.makeText(view.getContext(), "Sign Up Successful, welcome " + newUser.getDisplayedName(), Toast.LENGTH_SHORT).show();

                MainPage.getInstance().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HomeFragment()).commit();

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


    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        getActivity();
        if (resultCode == Activity.RESULT_OK && data != null) {
            if (requestCode == REQUEST_IMAGE_PICK) {

                Uri photoUri = data.getData();
                try {
                    Bitmap thumbnailBitmap = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), photoUri);
                    binding.btnAddProfileImg.setImageBitmap(thumbnailBitmap);

//                    currentThumbnailPath = thumbnailBitmap.toString(); // Update the photo path to the selected image's URI
                    currentPhotoPath = photoUri.toString();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void uploadPhoto() {
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_IMAGE_PICK);
    }

}
