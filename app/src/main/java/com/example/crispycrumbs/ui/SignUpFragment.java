package com.example.crispycrumbs.ui;

import static com.example.crispycrumbs.ui.MainPage.getDataManager;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.crispycrumbs.R;
import com.example.crispycrumbs.data.LoggedInUser;
import com.example.crispycrumbs.data.UserItem;
import com.example.crispycrumbs.databinding.FragmentSignUpBinding;
import com.example.crispycrumbs.model.DataManager;
import com.example.crispycrumbs.model.UserLogic;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SignUpFragment extends Fragment {
    private FragmentSignUpBinding binding;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_CAMERA_PERMISSION = 2;
    private static final int REQUEST_IMAGE_PICK = 3;

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

        binding.btnAddProfileImg.setOnClickListener(v -> {
            CharSequence[] options = {"Take Photo", "Choose from Gallery"};
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle("Select Profile Picture");
            builder.setItems(options, (dialog, which) -> {
                if (which == 0) {
                    if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                            ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        requestPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CAMERA_PERMISSION);
                    } else {
                        dispatchTakePictureIntent();
                    }
                } else if (which == 1) {
                    Intent pickPhotoIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(pickPhotoIntent, REQUEST_IMAGE_PICK);
                }
            });
            builder.show();
        });


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
                LoggedInUser.SetLoggedInUser(newUser);
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

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                Log.e("SignUp", "IO exception creating image file", ex);
            }
            if (photoFile != null) {
                photoURI = FileProvider.getUriForFile(getContext(), "com.example.crispycrumbs.fileprovider", photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
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
        if (resultCode == getActivity().RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_CAPTURE) {
                binding.btnAddProfileImg.setImageURI(photoURI);
                currentPhotoPath = photoURI.toString();
            } else if (requestCode == REQUEST_IMAGE_PICK) {
                if (data != null) {
                    Uri selectedImage = data.getData();
                    if (selectedImage != null) {
                        binding.btnAddProfileImg.setImageURI(selectedImage);
                        currentPhotoPath = selectedImage.toString();
                    }
                }
            }
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                dispatchTakePictureIntent();
            } else {
                // Permission denied
            }
        }
    }

}
