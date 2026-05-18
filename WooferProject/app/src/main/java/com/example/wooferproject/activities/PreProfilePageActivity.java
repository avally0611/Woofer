package com.example.wooferproject.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wooferproject.R;
import com.example.wooferproject.managers.PreProfilePageManager;
import com.example.wooferproject.models.Post;
import com.example.wooferproject.models.User;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

public class PreProfilePageActivity extends AppCompatActivity {

    private ImageView preProfileImage;
    private TextView preProfileUsername;
    private TextView preProfilePostsCount;
    private TextView preProfileFriendsCount;
    private Button preProfileManageButton;
    private RecyclerView preProfilePostsRecyclerView;

    private StaticPostAdapter postAdapter;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pre_profile_page);

        PreProfilePageManager.init(this);

        userId = getIntent().getIntExtra("user_id", -1);

        if (userId == -1) {
            SharedPreferences prefs = getSharedPreferences("WooferPrefs", MODE_PRIVATE);
            userId = prefs.getInt("user_id", -1);
        }

        if (userId == -1) {
            Toast.makeText(this, "User ID not found. Please log in.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        initViews();
        setupRecyclerView();
        setupManageButton();

        BottomNavigationView bottomNav = findViewById(R.id.navigationBar);
        setupBottomNav(bottomNav);
        bottomNav.setSelectedItemId(R.id.profile);

    }

    @Override
    protected void onResume() {
        super.onResume();
        // This runs every time you return to this screen (e.g., after unfriending)
        loadProfileDataAndCounts(userId);
        loadProfileImage(userId);
        loadUserPosts(userId);
    }


    private void initViews() {
        preProfileImage = findViewById(R.id.pre_profile_image);
        preProfileUsername = findViewById(R.id.pre_profile_username);
        preProfilePostsCount = findViewById(R.id.pre_profile_posts_count);
        preProfileFriendsCount = findViewById(R.id.pre_profile_friends_count);
        preProfileManageButton = findViewById(R.id.pre_profile_manage_button);
        preProfilePostsRecyclerView = findViewById(R.id.pre_profile_posts_recyclerview);

        preProfileFriendsCount.setOnClickListener(v -> {
            Intent intent = new Intent(PreProfilePageActivity.this, MyFriendsListActivity.class);
            intent.putExtra("user_id", userId);
            startActivity(intent);
        });
    }

    private void setupRecyclerView() {
        preProfilePostsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        postAdapter = new StaticPostAdapter(new ArrayList<>(),true);
        preProfilePostsRecyclerView.setAdapter(postAdapter);
    }

    private void setupManageButton() {
        preProfileManageButton.setOnClickListener(v -> {
            // This will show a message on screen so you know the click worked
            Toast.makeText(this, " Opening...", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(PreProfilePageActivity.this, ProfilePageActivity.class);
            intent.putExtra("user_id", userId);
            startActivity(intent);
        });
    }



    private void loadProfileDataAndCounts(int userId) {
        PreProfilePageManager.getProfileData(userId, userId, new PreProfilePageManager.ProfileDetailsCallback() {
            @Override
            public void onSuccess(User user, int postCount, int friendCount, boolean isFriend) {
                runOnUiThread(() -> {
                    if (user != null) {
                        preProfileUsername.setText(user.username);
                    } else {
                        preProfileUsername.setText("Unknown");
                    }
                    preProfilePostsCount.setText(postCount + " Posts");
                    preProfileFriendsCount.setText(friendCount + " Friends");
                });
            }

            @Override
            public void onFailure(String error) {
                runOnUiThread(() ->
                        Toast.makeText(PreProfilePageActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show()
                );
            }
        });
    }

    private void loadProfileImage(int userId) {
        PreProfilePageManager.getProfileImage(userId, new PreProfilePageManager.ProfileImageCallback() {
            @Override
            public void onSuccess(Bitmap imageBitmap) {
                runOnUiThread(() -> {
                    if (imageBitmap != null) {
                        preProfileImage.setImageBitmap(imageBitmap);
                    } else {
                        // Ensure R.drawable.profile exists or use a default
                        preProfileImage.setImageResource(android.R.drawable.ic_menu_gallery);
                    }
                });
            }

            @Override
            public void onFailure(String error) {
                runOnUiThread(() ->
                        preProfileImage.setImageResource(android.R.drawable.ic_menu_gallery)
                );
            }
        });
    }

    private void loadUserPosts(int userId) {
        PreProfilePageManager.getPosts(userId, new PreProfilePageManager.PostsCallback() {
            @Override
            public void onSuccess(List<Post> posts) {
                runOnUiThread(() -> {
                    if (posts != null && !posts.isEmpty()) {
                        postAdapter.updatePosts(posts);
                    } else {
                        postAdapter.updatePosts(new ArrayList<>());
                        Toast.makeText(PreProfilePageActivity.this, "No posts found", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onFailure(String error) {
                runOnUiThread(() ->
                        Toast.makeText(PreProfilePageActivity.this, "Posts Error: " + error, Toast.LENGTH_SHORT).show()
                );
            }
        });
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