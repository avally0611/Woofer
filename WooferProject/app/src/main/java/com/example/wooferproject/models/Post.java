package com.example.wooferproject;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class Post extends AppCompatActivity {
    private String username;
    private String text;
    private String location;
    private String image ref;
    private int upvotes;

    public Post(String username, String text, String location, String image ref, int upvotes) {
        this.username = username;
        this.text = text;
        this.location = location;
        this.image ref = image ref;
        this.upvotes = upvotes;

    }

    public String getUsername() {
        return username;
    }

    public String getText() {
        return text;
    }

    public String getLocation() {
        return location;
    }

    public String getImage ref() {
        return image ref;
    }

    public int getUpvotes() {
        return upvotes;
    }

    public void setUpvotes(int inUpvotes) {
        this.upvotes = inUpvotes;
    }



}


}