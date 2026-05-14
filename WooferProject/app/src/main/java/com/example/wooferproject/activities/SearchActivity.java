package com.example.wooferproject.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wooferproject.R;
import com.example.wooferproject.interfaces.SearchCallBack;
import com.example.wooferproject.managers.SearchManager;
import com.example.wooferproject.models.SearchUser;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;

public class SearchActivity extends AppCompatActivity {
    private int userId;
    private EditText searchBar;
    private RecyclerView mutualsRecyclerView;
    private RecyclerView searchResultsRecyclerView;

    private SearchUserAdapter searchAdapter;
    private SearchManager searchManager;
    private ArrayList<SearchUser> searchList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        userId = getIntent().getIntExtra("user_id", -1);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.search);

        searchBar = findViewById(R.id.searchBar);
        mutualsRecyclerView = findViewById(R.id.mutualsRecyclerView);
        searchResultsRecyclerView = findViewById(R.id.searchResultsRecyclerView);


        searchManager = new SearchManager();

        searchAdapter = new SearchUserAdapter(searchList);
        //setting layout inside recyler as linear layout so everythign is stacked vertically
        searchResultsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        //connect searchadapter file to our empty recycler view so that the adapter can add the users searched info to our recycler view
        searchResultsRecyclerView.setAdapter(searchAdapter);


        //now we add a listenting event that waits and listens for a keypress in search bar
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after)
            {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
                String currentTyping = s.toString().trim();

                //user hasnt type anything
                if (currentTyping.isEmpty())
                {
                    searchResultsRecyclerView.setVisibility(View.GONE);
                    mutualsRecyclerView.setVisibility(View.VISIBLE);
                    searchList.clear();
                    searchAdapter.notifyDataSetChanged();
                }
                else
                {
                    //first do changes in ui if they typing
                    mutualsRecyclerView.setVisibility(View.GONE);
                    searchResultsRecyclerView.setVisibility(View.VISIBLE);

                    //now we ask managet to get data from db
                    searchManager.searchUsers(userId, currentTyping, new SearchCallBack() {
                        @Override
                        public void onSuccess(ArrayList<SearchUser> fetchedUsers) {
                            runOnUiThread(new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    //we wanna update screen with new posts (first removes any old data)
                                    searchList.clear();
                                    searchList.addAll(fetchedUsers);
                                    searchAdapter.notifyDataSetChanged();
                                }

                            });
                        }

                        @Override
                        public void onFailure(String error)
                        {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(SearchActivity.this, error, Toast.LENGTH_SHORT).show();
                                }
                            });

                        }
                    });

                }

            }

            @Override
            public void afterTextChanged(Editable s)
            {

            }
        });


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
                Intent intent = new Intent(this, PreProfilePageActivity.class);
                intent.putExtra("user_id", userId);
                startActivity(intent);
                return true;
            }

            return false;
        });
    }
}