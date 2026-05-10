package com.example.wooferproject.models;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class Post {
    private String username;
    private String text;
    private String location;
    private String image;


    public Post(String username, String text, String location, String image) {
        this.username = username;
        this.text = text;
        this.location = location;
        this.image = image;

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

    public String getImage() {return image;}
}
