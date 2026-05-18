package com.example.wooferproject.activities;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wooferproject.R;
import com.example.wooferproject.managers.FriendManager;
import com.example.wooferproject.models.User;

import java.util.List;

public class MyFriendsAdapter extends RecyclerView.Adapter<MyFriendsAdapter.FriendViewHolder> {

    private List<User> friendList;
    private boolean isMyOwnList;
    private int currentUserId; // The person viewing the list
    private OnFriendshipChangedListener friendshipChangedListener;

    public interface OnFriendshipChangedListener {
        void onFriendshipChanged(int position);
    }

    public MyFriendsAdapter(List<User> friendList, int currentUserId, boolean isMyOwnList, OnFriendshipChangedListener listener) {
        this.friendList = friendList;
        this.currentUserId = currentUserId;
        this.isMyOwnList = isMyOwnList; // Now it will correctly set to true or false
        this.friendshipChangedListener = listener;
    }


    @NonNull
    @Override
    public FriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Uses a single template and changing elements dynamically
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.friend_template, parent, false);
        return new FriendViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FriendViewHolder holder, int position) {
        User user = friendList.get(position);
        holder.friendUsername.setText(user.username);

        // Dynamically set the icon and color based on friendship status
        if (user.isFriend) {
            // They are already friends - show Unfriend option (Red Delete Icon)
            holder.actionButton.setImageResource(R.drawable.ic_friend_remove);
            holder.actionButton.setColorFilter(0xFFFF0000); // Red

            holder.actionButton.setOnClickListener(v -> {
                FriendManager.getInstance().unfriend(currentUserId, user.id, new FriendManager.ActionCallback() {

                    @Override
                    public void onSuccess(String message) {
                        ((android.app.Activity) v.getContext()).runOnUiThread(() -> {
                            if (isMyOwnList) {
                                //  It's my list, remove friends entirely
                                int currentPos = holder.getAdapterPosition();
                                if (currentPos != RecyclerView.NO_POSITION) {
                                    friendList.remove(currentPos);
                                    notifyItemRemoved(currentPos);
                                    notifyItemRangeChanged(currentPos, friendList.size());
                                }
                            } else {
                                //  It's someone else's list, just change the icon to "Add"
                                user.isFriend = false;
                                notifyItemChanged(holder.getAdapterPosition());
                            }
                            Toast.makeText(v.getContext(), message, Toast.LENGTH_SHORT).show();
                        });
                    }


                    @Override
                    public void onFailure(String error) {
                        ((android.app.Activity) v.getContext()).runOnUiThread(() -> {
                            Toast.makeText(v.getContext(), "Error: " + error, Toast.LENGTH_SHORT).show();
                        });
                    }
                });
            });
        } else {
            // They are NOT friends hense show Add Friend option (Black Add Icon)
            holder.actionButton.setImageResource(R.drawable.ic_friend_add);
            holder.actionButton.setColorFilter(0xFF000000); // Black

            holder.actionButton.setOnClickListener(v -> {
                FriendManager.getInstance().addFriend(currentUserId, user.id, new FriendManager.ActionCallback() {

                    @Override
                    public void onSuccess(String message) {

                        user.isFriend = true;

                        ((android.app.Activity) v.getContext()).runOnUiThread(() -> {

                            notifyItemChanged(position);

                            if (friendshipChangedListener != null) {
                                friendshipChangedListener.onFriendshipChanged(position);
                            }

                            Toast.makeText(v.getContext(), message, Toast.LENGTH_SHORT).show();
                        });
                    }

                    @Override
                    public void onFailure(String error) {

                        ((android.app.Activity) v.getContext()).runOnUiThread(() -> {
                            Toast.makeText(v.getContext(), "Error: " + error, Toast.LENGTH_SHORT).show();
                        });
                    }
                });
            });
        }

        holder.friendUsername.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), UserProfileActivity.class);
            intent.putExtra("target_user_id", user.id);
            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return friendList.size();
    }

    public void updateFriendList(List<User> newFriendList) {
        this.friendList = newFriendList;
        notifyDataSetChanged();
    }

    public static class FriendViewHolder extends RecyclerView.ViewHolder {
        TextView friendUsername;
        ImageView actionButton;

        public FriendViewHolder(@NonNull View itemView) {
            super(itemView);
            // Map to the IDs in your templates
            friendUsername = itemView.findViewById(R.id.friendUsername);
            if (friendUsername == null) friendUsername = itemView.findViewById(R.id.username);

            actionButton = itemView.findViewById(R.id.unfriendButton);
            if (actionButton == null) actionButton = itemView.findViewById(R.id.friendButton);
        }
    }
}