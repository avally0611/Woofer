package com.example.wooferproject.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResult;


import androidx.appcompat.app.AppCompatActivity;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.example.wooferproject.R;
import com.example.wooferproject.managers.ProfilePageManager;
import com.example.wooferproject.models.User;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class ProfilePageActivity extends AppCompatActivity {

    EditText name, username, email;
    TextView password, resetPassword;
    Button editBtn, logoutBtn;
    ImageView profileImage;
    private int userId;

    boolean isEditing = false;
    private ActivityResultLauncher<Intent> imagePickerLauncher;

    private Uri selectedImageUri = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_page);

        userId = getIntent().getIntExtra("user_id", -1);
        if (userId == -1) {
            SharedPreferences loginPrefs = getSharedPreferences("WooferPrefs", MODE_PRIVATE);
            userId = loginPrefs.getInt("user_id", -1);
        }
        // UI
        name = findViewById(R.id.profile_name);
        username = findViewById(R.id.profile_username);
        email = findViewById(R.id.profile_email);
        password = findViewById(R.id.profile_password);
        resetPassword = findViewById(R.id.profile_reset_password);

        editBtn = findViewById(R.id.editProfile);
        logoutBtn = findViewById(R.id.logout);
        profileImage = findViewById(R.id.imageView);

        // Read only;
        setEditMode(false);
        if (userId != -1) {
            loadProfileData();
            loadProfileImage();
        } else {
            Toast.makeText(this, "User session expired. Please login again.", Toast.LENGTH_LONG).show();
            // Optional: Redirect to login
        }


        // picks the pic
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        selectedImageUri = result.getData().getData();
                        try {
                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImageUri);
                            profileImage.setImageBitmap(bitmap);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
        );

        profileImage.setOnClickListener(v -> {
            if (isEditing) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                imagePickerLauncher.launch(intent);
            }
        });


        // Edit and save button
        editBtn.setOnClickListener(v -> {

                    if (!isEditing) {
                        setEditMode(true);
                    } else {
                        saveProfile();
                    }
                });

        // reset password
        resetPassword.setOnClickListener(v -> {
            startActivity(new Intent(this, ForgotPasswordActivity.class));
        });

        // logout
        logoutBtn.setOnClickListener(v -> {
            // Clear the saved login session from SharedPreferences
            SharedPreferences loginPrefs = getSharedPreferences("WooferPrefs", MODE_PRIVATE);
            loginPrefs.edit().clear().apply();

            // Redirect to Login Page and clear the activity stack
            Intent intent = new Intent(ProfilePageActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        // Bottom navigation
        BottomNavigationView bottomNav = findViewById(R.id.navigationBar);
        setupBottomNav(bottomNav);
        bottomNav.setSelectedItemId(R.id.profile);
    }

    private void loadProfileData() {
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
                runOnUiThread(() -> Toast.makeText(ProfilePageActivity.this, "Failed to load profile", Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void loadProfileImage() {
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
                runOnUiThread(() -> profileImage.setImageResource(R.drawable.profile));
            }
        });
    }

    private void setEditMode(boolean enable) {
        isEditing = enable;
        name.setEnabled(enable);
        username.setEnabled(enable);
        email.setEnabled(enable);
        profileImage.setClickable(enable);
        profileImage.setAlpha(enable ? 1.0f : 0.6f);
        resetPassword.setEnabled(enable);
        resetPassword.setClickable(enable);
        resetPassword.setAlpha(enable ? 1.0f : 0.4f);
        editBtn.setText(enable ? "Save" : "Edit Profile");
    }

    private void saveProfile() {

        // Save text details
        ProfilePageManager.updateProfile(
                userId,
                name.getText().toString(),
                username.getText().toString(),
                email.getText().toString()
        );

        // Save image
        if (profileImage.getDrawable() != null) {

            Bitmap bitmap = ((BitmapDrawable) profileImage.getDrawable()).getBitmap();

            // Resize image
            Bitmap resized = Bitmap.createScaledBitmap(bitmap, 300, 300, true);

            ByteArrayOutputStream stream = new ByteArrayOutputStream();

            resized.compress(Bitmap.CompressFormat.JPEG, 80, stream);

            byte[] imageBytes = stream.toByteArray();
            Toast.makeText(this,
                    "Image size: " + imageBytes.length,
                    Toast.LENGTH_LONG).show();

            // Upload image
            ProfilePageManager.uploadProfileImage(userId, imageBytes);
        }

        Toast.makeText(this, "Profile Updated", Toast.LENGTH_SHORT).show();

        setEditMode(false);
    }

    protected void setupBottomNav(BottomNavigationView bottomNav) {

        bottomNav.setOnItemSelectedListener(item -> {

            int id = item.getItemId();

            if (id == R.id.home) {

                Intent intent = new Intent(this, HomeScreenActivity.class);
                intent.putExtra("user_id", userId);
                startActivity(intent);

                return true;

            } else if (id == R.id.search) {

                Intent intent = new Intent(this, SearchActivity.class);
                intent.putExtra("user_id", userId);
                startActivity(intent);

                return true;

            } else if (id == R.id.add) {

                Intent intent = new Intent(this, PostPageActivity.class);
                intent.putExtra("user_id", userId);
                startActivity(intent);

                return true;

            } else if (id == R.id.profile) {

                return true;
            }

            return false;
        });
    }
}