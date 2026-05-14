
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

public class UserProfileActivity extends AppCompatActivity {

    private ImageView profileImage;
    private TextView profileUsername, postsCount, friendsCount;

    private RecyclerView postsRecyclerView;
    private PostAdapter postAdapter;
    private RecyclerView userRecyclerView;
    private UserAdapter userAdapter;


    private int targetUserId; // The person we are viewing # your friend
    private int currentUserId; // your id

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_profile_page);

        PreProfilePageManager.init(this);

        // 1. This gets the id's of the users
        targetUserId = getIntent().getIntExtra("target_user_id", -1);
        SharedPreferences prefs = getSharedPreferences("WooferPrefs", MODE_PRIVATE);
        currentUserId = prefs.getInt("user_id", -1);

        if (targetUserId == -1) {
            Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        //this is the bottom navigation
        BottomNavigationView bottomNav = findViewById(R.id.navigationBar);
        setupBottomNav(bottomNav);

        initViews();
        setupRecyclerView();

        // 2. load all the data
        loadProfileDataAndCounts(targetUserId);
        loadProfileImage(targetUserId);
        loadUserPosts(targetUserId);
    }

    private void initViews() {
        profileImage = findViewById(R.id.profile_image);
        profileUsername = findViewById(R.id.profile_username);
        postsCount = findViewById(R.id.profile_posts_count);
        friendsCount = findViewById(R.id.profile_friends_count);
        postsRecyclerView = findViewById(R.id.profile_posts_recyclerview);

        // Clicking friends count shows THEIR friends
        friendsCount.setOnClickListener(v -> {
            Intent intent = new Intent(this, MyFriendsListActivity.class);
            intent.putExtra("user_id", targetUserId);
            startActivity(intent);
        });
    }

    private void setupRecyclerView() {
        postsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        postAdapter = new PostAdapter(new ArrayList<>());
        postsRecyclerView.setAdapter(postAdapter);
    }

    private void loadProfileDataAndCounts(int id) {
        PreProfilePageManager.getProfileData(id, new PreProfilePageManager.ProfileDetailsCallback() {
            @Override
            public void onSuccess(User user, int pCount, int fCount) {
                runOnUiThread(() -> {
                    profileUsername.setText(user != null ? user.username : "Unknown");
                    postsCount.setText(pCount + " Posts");
                    friendsCount.setText(fCount + " Friends");
                });
            }
            @Override
            public void onFailure(String error) { /* handle error */ }
        });
    }

    private void loadProfileImage(int id) {
        PreProfilePageManager.getProfileImage(id, new PreProfilePageManager.ProfileImageCallback() {
            @Override
            public void onSuccess(Bitmap bitmap) {
                runOnUiThread(() -> {
                    if (bitmap != null) profileImage.setImageBitmap(bitmap);
                    else profileImage.setImageResource(android.R.drawable.ic_menu_gallery);
                });
            }
            @Override
            public void onFailure(String error) { /* handle error */ }
        });
    }

    private void loadUserPosts(int id) {
        PreProfilePageManager.getPosts(id, new PreProfilePageManager.PostsCallback() {
            @Override
            public void onSuccess(List<Post> posts) {
                runOnUiThread(() -> postAdapter.updatePosts(posts));
            }
            @Override
            public void onFailure(String error) { /* handle error */ }
        });
    }
    protected void setupBottomNav(BottomNavigationView bottomNav) {

        bottomNav.setOnItemSelectedListener(item -> {

            int id = item.getItemId();

            if (id == R.id.home) {
                Intent intent = new Intent(this, HomeScreenActivity.class);
                intent.putExtra("user_id", currentUserId);
                startActivity(intent);

                return true;

            } else if (id == R.id.search) {

                Intent intent = new Intent(this, SearchActivity.class);
                intent.putExtra("user_id", currentUserId);
                startActivity(intent);

                return true;

            } else if (id == R.id.add) {

                Intent intent = new Intent(this, PostPageActivity.class);
                intent.putExtra("user_id", currentUserId);
                startActivity(intent);

                return true;

            } else if (id == R.id.profile) {
                Intent intent = new Intent(this, PreProfilePageActivity.class);
                intent.putExtra("user_id", currentUserId);
                startActivity(intent);

                return true;
            }

            return false;
        });
    }
}
