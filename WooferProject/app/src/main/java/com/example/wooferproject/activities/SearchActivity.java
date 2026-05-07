package com.example.wooferproject.activities;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.wooferproject.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class SearchActivity extends AppCompatActivity {
    private int userId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        userId = 1; //getIntent().getIntExtra("user_id", -1);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search);

        // this allows the bottom navigation menu to be used
        BottomNavigationView bottomNav = findViewById(R.id.navigationBar);
        setupBottomNav(bottomNav);

        // highlight current tab
        bottomNav.setSelectedItemId(R.id.search);
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
                return true;

            } else if (id == R.id.add) {

                Intent intent = new Intent(this, PostPageActivity.class);
                intent.putExtra("user_id", userId);
                startActivity(intent);

                return true;

            } else if (id == R.id.profile) {
                Intent intent = new Intent(this, ProfilePageActivity.class);
                intent.putExtra("user_id", userId);
                startActivity(intent);
                return true;
            }

            return false;
        });
    }
}