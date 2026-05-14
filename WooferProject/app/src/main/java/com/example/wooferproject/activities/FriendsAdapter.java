package com.example.wooferproject.activities;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wooferproject.R;
import com.example.wooferproject.models.User;

import java.util.List;

public class FriendsAdapter extends RecyclerView.Adapter<FriendsAdapter.FriendViewHolder> {

    private List<User> friendList;
    private OnUnfriendClickListener unfriendClickListener;

    public interface OnUnfriendClickListener {
        void onUnfriendClick(int position);
    }

    public FriendsAdapter(List<User> friendList, OnUnfriendClickListener listener) {
        this.friendList = friendList;
        this.unfriendClickListener = listener;
    }

    @NonNull
    @Override
    public FriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.friend_template, parent, false);
        return new FriendViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FriendViewHolder holder, int position) {
        User friend = friendList.get(position);
        holder.friendUsername.setText(friend.username);

        holder.unfriendButton.setOnClickListener(v -> {
            if (unfriendClickListener != null) {
                unfriendClickListener.onUnfriendClick(position);
            }
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
        ImageView unfriendButton;

        public FriendViewHolder(@NonNull View itemView) {
            super(itemView);
            friendUsername = itemView.findViewById(R.id.friendUsername);
            unfriendButton = itemView.findViewById(R.id.unfriendButton);
        }
    }
}
