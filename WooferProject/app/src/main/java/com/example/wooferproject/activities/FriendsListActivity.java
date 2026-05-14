package com.example.wooferproject.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wooferproject.R;
import com.example.wooferproject.activities.FriendsAdapter;
import com.example.wooferproject.managers.PreProfilePageManager;
import com.example.wooferproject.models.User;

import java.util.ArrayList;
import java.util.List;

public class FriendsListActivity extends AppCompatActivity implements FriendsAdapter.OnUnfriendClickListener {

    private RecyclerView friendsRecyclerView;
    private FriendsAdapter friendsAdapter;
    private List<User> friendsList;
    private int currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.friend_list);

        PreProfilePageManager.init(this);

        SharedPreferences prefs = getSharedPreferences("WooferPrefs", MODE_PRIVATE);
        currentUserId = prefs.getInt("user_id", -1);

        if (currentUserId == -1) {
            Toast.makeText(this, "User ID not found. Please log in.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        friendsRecyclerView = findViewById(R.id.friendsRecyclerView);
        friendsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        friendsList = new ArrayList<>();
        friendsAdapter = new FriendsAdapter(friendsList, this);
        friendsRecyclerView.setAdapter(friendsAdapter);

        loadFriends();
    }

    private void loadFriends() {
        PreProfilePageManager.getFriends(currentUserId, new PreProfilePageManager.FriendsCallback() {
            @Override
            public void onSuccess(List<User> friends) {
                runOnUiThread(() -> {
                    friendsList.clear();
                    friendsList.addAll(friends);
                    friendsAdapter.updateFriendList(friendsList);
                    if (friends.isEmpty()) {
                        Toast.makeText(FriendsListActivity.this, "No friends found.", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onFailure(String error) {
                runOnUiThread(() ->
                        Toast.makeText(FriendsListActivity.this, "Error loading friends: " + error, Toast.LENGTH_SHORT).show()
                );
            }
        });
    }

    @Override
    public void onUnfriendClick(int position) {
        User friendToUnfriend = friendsList.get(position);
        PreProfilePageManager.unfriend(currentUserId, friendToUnfriend.id, new PreProfilePageManager.UnfriendCallback() {
            @Override
            public void onSuccess(String message) {
                runOnUiThread(() -> {
                    Toast.makeText(FriendsListActivity.this, message, Toast.LENGTH_SHORT).show();
                    friendsList.remove(position);
                    friendsAdapter.notifyItemRemoved(position);
                });
            }

            @Override
            public void onFailure(String error) {
                runOnUiThread(() ->
                        Toast.makeText(FriendsListActivity.this, "Error unfriending: " + error, Toast.LENGTH_SHORT).show()
                );
            }
        });
    }
}
