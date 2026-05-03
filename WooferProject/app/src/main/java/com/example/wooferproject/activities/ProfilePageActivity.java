package com.example.wooferproject.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.example.wooferproject.R;
import com.example.wooferproject.managers.ProfilePageManager;
import com.example.wooferproject.models.User;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.ByteArrayOutputStream;

public class ProfilePageActivity extends AppCompatActivity {

    EditText name, username, email;
    TextView password, resetPassword;
    Button editBtn, logoutBtn;
    ImageView profileImage;

    boolean isEditing = false;
    private ActivityResultLauncher<String> imagePickerLauncher;

    private Uri selectedImageUri = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_page);

        int userId = getIntent().getIntExtra("user_id", -1);

        // UI
        name = findViewById(R.id.profile_name);
        username = findViewById(R.id.profile_username);
        email = findViewById(R.id.profile_email);
        password = findViewById(R.id.profile_password);
        resetPassword = findViewById(R.id.profile_reset_password);

        editBtn = findViewById(R.id.editProfile);
        logoutBtn = findViewById(R.id.logout);
        profileImage = findViewById(R.id.imageView);

        SharedPreferences prefs = getSharedPreferences("profile", MODE_PRIVATE);
        String savedUri = prefs.getString("profile_image", null);

        resetPassword.setEnabled(false);
        resetPassword.setClickable(false);
        resetPassword.setAlpha(0.4f);

        // Loads the profile data
        ProfilePageManager.getProfile(userId, new ProfilePageManager.Callback() {
            @Override
            public void onSuccess(User user) {
                runOnUiThread(() -> {
                    name.setText(user.name);
                    username.setText(user.username);
                    email.setText(user.email);
                });
            }

            @Override
            public void onFailure(String error) {
                runOnUiThread(() -> email.setText("Failed to load profile"));
            }
        });

        // Loads the pic from local storage
        if (savedUri != null) {
            profileImage.setImageURI(Uri.parse(savedUri));
        }

        // picks the pic
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        selectedImageUri = uri;
                        profileImage.setImageURI(uri);
                    }
                }
        );

        profileImage.setOnClickListener(v -> {
            if (isEditing) {
                imagePickerLauncher.launch("image/*");
            }
        });

        profileImage.setClickable(false);
        profileImage.setAlpha(0.6f);

        // Edit and save button
        editBtn.setOnClickListener(v -> {

            if (!isEditing) {

                // edit profile
                name.setEnabled(true);
                username.setEnabled(true);
                email.setEnabled(true);

                profileImage.setClickable(true);
                profileImage.setAlpha(1.0f);

                resetPassword.setEnabled(true);
                resetPassword.setClickable(true);
                resetPassword.setAlpha(1.0f);

                editBtn.setText("Save");
                isEditing = true;

            } else {

                // Save profile
                ProfilePageManager.updateProfile(
                        userId,
                        name.getText().toString(),
                        username.getText().toString(),
                        email.getText().toString()
                );

                // Save the image if its changed
                if (selectedImageUri != null && profileImage.getDrawable() != null) {

                    profileImage.setDrawingCacheEnabled(true);
                    profileImage.buildDrawingCache();

                    Bitmap bitmap = ((BitmapDrawable) profileImage.getDrawable()).getBitmap();

                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);

                    byte[] imageBytes = stream.toByteArray();

                    ProfilePageManager.uploadProfileImage(userId, imageBytes);
                }

                // disable the edit mode
                name.setEnabled(false);
                username.setEnabled(false);
                email.setEnabled(false);

                profileImage.setClickable(false);
                profileImage.setAlpha(0.6f);

                resetPassword.setEnabled(false);
                resetPassword.setClickable(false);
                resetPassword.setAlpha(0.4f);

                editBtn.setText("Edit Profile");
                isEditing = false;
            }
        });

        // reset password
        resetPassword.setOnClickListener(v -> {
            startActivity(new Intent(this, ForgotPasswordActivity.class));
        });

        // logout
        logoutBtn.setOnClickListener(v -> {
            startActivity(new Intent(this, SignUpActivity.class));
            finish();
        });

        // Load the image from the database
        ProfilePageManager.getProfileImage(userId, new ProfilePageManager.ImageCallback() {
            @Override
            public void onSuccess(byte[] image) {
                runOnUiThread(() -> {
                    Bitmap bitmap = BitmapFactory.decodeByteArray(image, 0, image.length);
                    profileImage.setImageBitmap(bitmap);
                });
            }

            @Override
            public void onFailure(String error) {
                System.out.println(error);
            }
        });

        // Bottom navigation
        BottomNavigationView bottomNav = findViewById(R.id.navigationBar);
        setupBottomNav(bottomNav);
        bottomNav.setSelectedItemId(R.id.profile);
    }

    protected void setupBottomNav(BottomNavigationView bottomNav) {

        bottomNav.setOnItemSelectedListener(item -> {

            int id = item.getItemId();

            if (id == R.id.home) {
                startActivity(new Intent(this, HomeScreenActivity.class));
                return true;
            } else if (id == R.id.search) {
                startActivity(new Intent(this, SearchActivity.class));
                return true;
            } else if (id == R.id.add) {
                startActivity(new Intent(this, PostPageActivity.class));
                return true;
            } else if (id == R.id.profile) {
                return true;
            }

            return false;
        });
    }
}