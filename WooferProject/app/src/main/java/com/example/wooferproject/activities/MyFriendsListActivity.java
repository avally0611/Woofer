package com.example.wooferproject.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wooferproject.R;
import com.example.wooferproject.managers.FriendManager;
import com.example.wooferproject.models.User;

import java.util.ArrayList;
import java.util.List;

public class MyFriendsListActivity extends AppCompatActivity {

    private RecyclerView friendsRecyclerView;
    private MyFriendsAdapter friendsAdapter;
    private List<User> friendsList;
    private int targetUserId; // The person whose friends list we are viewing
    private int loggedInUserId; // The person currently logged in


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.friend_list);

        // Initialize Managers
        FriendManager.init(this);

        // Get IDs
        targetUserId = getIntent().getIntExtra("user_id", -1);
        SharedPreferences prefs = getSharedPreferences("WooferPrefs", MODE_PRIVATE);
        loggedInUserId = prefs.getInt("user_id", -1);

        if (targetUserId == -1 || loggedInUserId == -1) {
            Toast.makeText(this, "Error: User not found. Please log in.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        TextView friendsCountTextView = findViewById(R.id.profile_friends_count);
        friendsRecyclerView = findViewById(R.id.friendsRecyclerView);
        friendsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        friendsList = new ArrayList<>();

        // Setup RecyclerView
        friendsRecyclerView = findViewById(R.id.friendsRecyclerView);
        friendsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        boolean isOwner = (targetUserId == loggedInUserId);
        friendsList = new ArrayList<>();
        friendsAdapter = new MyFriendsAdapter(friendsList, loggedInUserId, isOwner, position -> {

            if (friendsCountTextView != null) {
                friendsCountTextView.setText(friendsList.size() + " Friends");
            }
        });


        friendsRecyclerView.setAdapter(friendsAdapter);

        loadFriends();


        // Back Button
        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());
    }

    private void loadFriends() {
        FriendManager.getInstance().getFriends(targetUserId, loggedInUserId, new FriendManager.FriendsCallback() {
            @Override
            public void onSuccess(List<User> friends) {
                runOnUiThread(() -> {
                    friendsList.clear();
                    friendsList.addAll(friends);
                    friendsAdapter.updateFriendList(friendsList);
                    if (friends.isEmpty()) {
                        Toast.makeText(MyFriendsListActivity.this, "No friends found.", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onFailure(String error) {
                runOnUiThread(() ->
                        Toast.makeText(MyFriendsListActivity.this, "Error loading friends: " + error, Toast.LENGTH_SHORT).show()
                );
            }
        });
    }
}