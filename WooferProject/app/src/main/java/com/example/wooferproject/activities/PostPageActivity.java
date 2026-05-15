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
import java.io.IOException;
import android.media.ExifInterface;
import android.graphics.Matrix;

public class PostPageActivity extends AppCompatActivity {
    private int userId;
    private int postID;
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private Uri selectedImageUri = null;
    private ImageView postImage;
    EditText caption;
    TextView locationText;
    Button imageBtn, postBtn;
    ImageView imageView;
    ActivityResultLauncher<String> imagePicker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        userId = getIntent().getIntExtra("user_id", -1);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.post_page);

        // almost like initialising the items on screen
        caption = findViewById(R.id.addText);
        locationText = findViewById(R.id.location);
        imageBtn = findViewById(R.id.imageButt);
        imageView = findViewById(R.id.imageView);
        postBtn = findViewById(R.id.postButton);

        imageBtn.setOnClickListener(v -> {

            Intent intent = new Intent(
                    Intent.ACTION_PICK,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            );

            imagePickerLauncher.launch(intent);
        });

        // this picks the picture
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        selectedImageUri = result.getData().getData();
                        try {
                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImageUri);
                            imageView.setImageBitmap(bitmap);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
        );


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
                    imageBytes,
                    new PostPageManager.PostCallback() {
                        @Override
                        public void onSuccess(String message) {
                            Toast.makeText(PostPageActivity.this, message, Toast.LENGTH_SHORT).show();
                            // clears all fields on success
                            caption.setText("");
                            locationText.setText("No Location Selected");
                            locationText.setEnabled(false);
                            imageView.setImageDrawable(null);
                            selectedImageUri = null;
                        }

                        @Override
                        public void onFailure(String error) {
                            Toast.makeText(PostPageActivity.this, error, Toast.LENGTH_LONG).show();
                        }
                    }
            );
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

        if (uri == null) return null;

        try {

            Bitmap bitmap = MediaStore.Images.Media.getBitmap(
                    this.getContentResolver(),
                    uri
            );

            // Calculate new dimensions to maintain aspect ratio while fitting within a max size
            int originalWidth = bitmap.getWidth();
            int originalHeight = bitmap.getHeight();
            int maxSize = 1024; // Max size for either width or height

            int newWidth = originalWidth;
            int newHeight = originalHeight;

            if (originalWidth > maxSize || originalHeight > maxSize) {
                if (originalWidth > originalHeight) {
                    newWidth = maxSize;
                    newHeight = (int) (originalHeight * ((float) maxSize / originalWidth));
                } else {
                    newHeight = maxSize;
                    newWidth = (int) (originalWidth * ((float) maxSize / originalHeight));
                }
            }
            Bitmap resized = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);

            // Handle image rotation based on EXIF data
            try {
                ExifInterface exifInterface = new ExifInterface(this.getContentResolver().openInputStream(uri));
                int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
                Matrix matrix = new Matrix();
                switch (orientation) {
                    case ExifInterface.ORIENTATION_ROTATE_90:
                        matrix.postRotate(90);
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_180:
                        matrix.postRotate(180);
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_270:
                        matrix.postRotate(270);
                        break;
                }
                resized = Bitmap.createBitmap(resized, 0, 0, resized.getWidth(), resized.getHeight(), matrix, true);
            } catch (IOException e) {
                e.printStackTrace();
            }

            ByteArrayOutputStream stream = new ByteArrayOutputStream();

            resized.compress(Bitmap.CompressFormat.JPEG, 80, stream);

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
                Intent intent = new Intent(this, PreProfilePageActivity.class);
                intent.putExtra("user_id", userId);
                startActivity(intent);

                return true;
            }

            return false;
        });
    }
}