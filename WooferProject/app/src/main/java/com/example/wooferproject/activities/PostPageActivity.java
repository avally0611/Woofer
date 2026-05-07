package com.example.wooferproject.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import com.example.wooferproject.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.example.wooferproject.managers.PostPageManager;

import java.io.ByteArrayOutputStream;

public class PostPageActivity extends AppCompatActivity {
    private int userId;
    EditText caption;
    TextView locationText;
    Button imageBtn, postBtn;
    ImageView imageView;

    Uri selectedImageUri;
    ActivityResultLauncher<String> imagePicker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        userId = 1;//getIntent().getIntExtra("user_id", -1);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.post_page);

        // almost like initialising the items on screen
        caption = findViewById(R.id.addText);
        locationText = findViewById(R.id.location);
        imageBtn = findViewById(R.id.imageButt);
        imageView = findViewById(R.id.imageView);
        postBtn = findViewById(R.id.postButton);

        // this picks the picture
        imagePicker = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        selectedImageUri = uri;
                        imageView.setImageURI(uri);
                    }
                }
        );
        imageBtn.setOnClickListener(v -> {
            imagePicker.launch("image/*");
        });

        // this posts the posts;
        postBtn.setOnClickListener(v -> {

            // gets the input text
            String text = caption.getText().toString().trim();
            String location = locationText.getText().toString().trim();

            // null values changed to ""
            String textValue = text.isEmpty() ? "" : text;

            String locationValue =
                    (location.isEmpty() || location.equals("No Location Selected"))
                            ? ""
                            : location;
            // handles pictures
            byte[] imageBytes = null;

            if (selectedImageUri != null) {
                imageBytes = getImageBytes(selectedImageUri);
            }

            // updates database
            PostPageManager.createPost(
                    userId,
                    textValue,
                    locationValue,
                    imageBytes
            );

            // returns success to user after posting
            Toast.makeText(this, "Post successful!", Toast.LENGTH_SHORT).show();

            // clears all fields
            caption.setText("");
            locationText.setText("No Location Selected");
            locationText.setEnabled(false);

            imageView.setImageDrawable(null);
            selectedImageUri = null;
        });

        // the following controls the location button and allows fro location infill;
        Button locationBtn = findViewById(R.id.locationButt);

        locationBtn.setOnClickListener(v -> {

            if (!locationText.isEnabled()) {

                // turn editing ON
                locationText.setEnabled(true);
                locationText.setText("");
                locationText.requestFocus();

            } else {

                // turn editing OFF (lock it)
                locationText.setEnabled(false);

                if (locationText.getText().toString().trim().isEmpty()) {
                    locationText.setText("No Location Selected");
                }
            }
        });

        // this allows the bottom navigation menu to be used
        BottomNavigationView bottomNav = findViewById(R.id.navigationBar);
        setupBottomNav(bottomNav);

        // highlight current tab
        bottomNav.setSelectedItemId(R.id.add);
    }
    // this function turns the picture into bytes;
    private byte[] getImageBytes(Uri uri) {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(
                    this.getContentResolver(), uri);

            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);

            return stream.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
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
                return true;

            } else if (id == R.id.profile) {
                Intent intent = new Intent(this, ProfilePageActivity.class);
                intent.putExtra("user_id", userId);
                startActivity(intent);

                return true;
            }

            return false;
        });
    }
}