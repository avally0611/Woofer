package com.example.wooferproject.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.wooferproject.R;
import com.example.wooferproject.managers.ProfilePageManager;

public class ProfilePageActivity extends AppCompatActivity {

    EditText name, username, email;
    TextView password, resetPassword;
    Button editBtn, logoutBtn;
    ImageView profileImage;

    boolean isEditing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_page);
        int userId = getIntent().getIntExtra("user_id", -1);

        // this connects to the ui
        name = findViewById(R.id.profile_name);
        username = findViewById(R.id.profile_username);
        email = findViewById(R.id.profile_email);
        password = findViewById(R.id.profile_password);
        resetPassword = findViewById(R.id.profile_reset_password);

        editBtn = findViewById(R.id.editProfile);
        logoutBtn = findViewById(R.id.logout);
        profileImage = findViewById(R.id.imageView);

        // this is the edit button to edit profile
        editBtn.setOnClickListener(v -> {
            if (!isEditing) {
                name.setEnabled(true);
                username.setEnabled(true);
                email.setEnabled(true);

                editBtn.setText("Save");
                isEditing = true;

            } else {
                ProfilePageManager.updateProfile(
                        userId,
                        name.getText().toString(),
                        username.getText().toString(),
                        email.getText().toString()
                );

                name.setEnabled(false);
                username.setEnabled(false);
                email.setEnabled(false);

                editBtn.setText("Edit Profile");
                isEditing = false;
            }
        });

        // RESET PASSWORD
        resetPassword.setOnClickListener(v -> {
            Intent intent = new Intent(ProfilePageActivity.this, ForgotPasswordActivity.class);
            startActivity(intent);
        });

        // LOGOUT
        logoutBtn.setOnClickListener(v -> {
            Intent intent = new Intent(ProfilePageActivity.this, SignUpActivity.class);
            startActivity(intent);
            finish();
        });

        // IMAGE CLICK
        profileImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, 1);
        });
    }

    // HANDLE IMAGE RESULT
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            profileImage.setImageURI(imageUri);

            // TODO: upload to server
        }
    }

    // mask the email so that it cannot be fully seen
    private String maskEmail(String email) {
        if (email.length() < 5) return "***";
        return email.substring(0, 3) + "***@***";
    }

}