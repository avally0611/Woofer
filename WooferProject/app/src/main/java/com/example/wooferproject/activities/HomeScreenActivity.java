package com.example.wooferproject.activities;

//so acitivty files just deal with frontend - basically communicate with screen and user input and stuff
//okay so after some research i have decided ot use a recycler view - basically it like shows a limited amount of posts from the many we get from db and as you scroll down it populates with newwer posts

//okay also wwith recycle view comes a recycler adaptor that gets dsta from arraylist and puts in card

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wooferproject.R;
import com.example.wooferproject.interfaces.PostCallback;
import com.example.wooferproject.managers.HomeScreenManager;
import com.example.wooferproject.models.Post;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;

public class HomeScreenActivity extends AppCompatActivity
{
    //just declasre varibales we gonna use
    private RecyclerView recyclerView;
    private PostAdapter adapter;
    private ArrayList<Post> posts = new ArrayList<Post>();
    private HomeScreenManager homeScreenManager;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        userId = getIntent().getIntExtra("user_id", -1);


        //sets the screen desgin - uses home xml screen code
        setContentView(R.layout.home_screen);

        //setup the recycler view
        recyclerView = findViewById(R.id.recyclerView);
        //setting layout inside recyler as linear layout so everythign is stacked vertically
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        //connect postadapter file to our empty recycler view so that the adapter can add the posts info to our recycler view
        adapter = new PostAdapter(posts);
        recyclerView.setAdapter(adapter);


        //now we initalise the homescreen manager object so we can actually get the posts list from backend
        homeScreenManager = new HomeScreenManager();
        //just like in homescreen manager we just pop the two methods from interface for returning message when getting result from db
        homeScreenManager.getPosts(2, new PostCallback()
        {
            @Override
            public void onSuccess(ArrayList<Post> fetchedPosts)
            {
                //main thread/UI thread - only job is to draw screen, listen for buttons tapping, and keep app looking smooth
                //the fecth posts is done on a background thread to noit crash main thread and app screen
                //runOnUIThread  takes downloaded posts from back thrad and puts on main thread
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //we wanna update screen with new posts (first removes any old data)
                        posts.clear();
                        posts.addAll(fetchedPosts);
                        adapter.notifyDataSetChanged();
                    }
                });
            }

            @Override
            public void onFailure(String error) {
                //if server offline, go back to main thread and output error on UI using a toast/pop  up
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // a Toast is a like a pop up message - we  use to show an error message
                        Toast.makeText(HomeScreenActivity.this, "Error loading posts: " + error, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        // this allows the bottom navigation menu to be used
        BottomNavigationView bottomNav = findViewById(R.id.navigationBar);
        setupBottomNav(bottomNav);

        // highlight current tab
        bottomNav.setSelectedItemId(R.id.home);

    }
    // this allows the switch between home screen and other screens using the bottom navigation menu
    protected void setupBottomNav(BottomNavigationView bottomNav) {

        bottomNav.setOnItemSelectedListener(item -> {

            int id = item.getItemId();

            if (id == R.id.home) {

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
                Intent intent = new Intent(this, PreProfilePageActivity.class);
                intent.putExtra("user_id", userId);
                startActivity(intent);

                return true;
            }

            return false;
        });
    }

}