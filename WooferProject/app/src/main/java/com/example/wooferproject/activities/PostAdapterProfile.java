package com.example.wooferproject.activities;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wooferproject.R;
import com.example.wooferproject.models.Post;

import java.util.ArrayList;
import java.util.List;

public class PostAdapterProfile extends RecyclerView.Adapter<PostAdapterProfile.PostViewHolder> {

    private final ArrayList<Post> posts;
    private static final String TAG = "PostAdapterProfile";

    public PostAdapterProfile(ArrayList<Post> posts) {
        this.posts = posts;
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Ensure this layout file exists and contains the correct IDs
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.simple_post_template, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        Post currentPost = posts.get(position);

        // FIXED: Added null checks for each view before calling setText()
        if (holder.username != null) {
            holder.username.setText(currentPost.getUsername() != null ? currentPost.getUsername() : "Unknown");
        }

        if (holder.text != null) {
            holder.text.setText(currentPost.getText() != null ? currentPost.getText() : "");
        }

        if (holder.location != null) {
            holder.location.setText(currentPost.getLocation() != null ? currentPost.getLocation() : "");
        }

        if (holder.image != null) {
            String imageBase64 = currentPost.getImage();
            if (imageBase64 != null && !imageBase64.isEmpty() && !imageBase64.equals("null")) {
                try {
                    byte[] decodedBytes = Base64.decode(imageBase64, Base64.DEFAULT);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                    if (bitmap != null) {
                        holder.image.setImageBitmap(bitmap);
                        holder.image.setVisibility(View.VISIBLE);
                    } else {
                        holder.image.setVisibility(View.GONE);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Image decoding failed", e);
                    holder.image.setVisibility(View.GONE);
                }
            } else {
                holder.image.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    public void updatePosts(List<Post> newPosts) {
        posts.clear();
        if (newPosts != null) {
            posts.addAll(newPosts);
        }
        notifyDataSetChanged();
    }

    public static class PostViewHolder extends RecyclerView.ViewHolder {
        TextView username, location, text;
        ImageView image;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            // FIXED: Ensure these IDs match your post_template.xml exactly
            username = itemView.findViewById(R.id.post_user);
            location = itemView.findViewById(R.id.post_location);
            text = itemView.findViewById(R.id.post_text);
            image = itemView.findViewById(R.id.post_image);

            // Debugging: Log if any view is null
            if (username == null) Log.e(TAG, "TextView 'post_user' not found in layout!");
            if (location == null) Log.e(TAG, "TextView 'post_location' not found in layout!");
            if (text == null) Log.e(TAG, "TextView 'post_text' not found in layout!");
            if (image == null) Log.e(TAG, "ImageView 'post_image' not found in layout!");
        }
    }
}