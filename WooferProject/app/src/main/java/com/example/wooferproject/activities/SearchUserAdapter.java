package com.example.wooferproject.activities;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wooferproject.R;
import com.example.wooferproject.managers.HomeScreenManager;
import com.example.wooferproject.models.Post;
import com.example.wooferproject.models.SearchUser;

import java.util.ArrayList;
import java.util.List;

public class SearchUserAdapter extends RecyclerView.Adapter<SearchUserAdapter.UserViewHolder>
{
    private ArrayList<SearchUser> users;

        //this allows search actity to send the list users to the adapter so it can start
    public SearchUserAdapter(ArrayList<SearchUser> users)
    {
        this.users = users;
    }

    //okay so we first create a blank card so as soon as screen is made we can display on screen

    @NonNull
    @Override
    //this method goes to our template (post_template) and builds a real java object so we can populate it
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        //a view is jsut object on screen: e.g textview, button, image,etc
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_template, parent, false);
        return new UserViewHolder(view);
        //basically just returns a blank card tnat has fields accesible to populate
    }

    //this method is the REAL ONE - basically gets data from db and pputs it on blank card we created - populate card
    //so when user scrolls to post o next post n, it sends that number as pos so it gets that post as a card and displays on UI
    public void onBindViewHolder(@NonNull UserViewHolder holder, int pos)
    {
        SearchUser currUser = users.get(pos);

        //now set username on template to real username
        holder.username.setText(currUser.getUsername());

        //now we check if they friend or not friend to determine which icon
        if (currUser.isFriend())
        {
            //show the unfriend icon bcz already friends
            holder.friendButton.setImageResource(R.drawable.ic_friend_remove);
            holder.friendButton.setColorFilter(android.graphics.Color.RED);
        }
        else
        {
            holder.friendButton.setImageResource(R.drawable.ic_friend_add);
            holder.friendButton.setColorFilter(android.graphics.Color.BLACK);
        }

    }

    //need this to tell recycler view how many cards exist in view
    public int getItemCount()
    {
        return users.size();
    }


    //so we dont make this in its own file because its only used by post adapter
    //jsut like post adapter, recyelerview has a view holder that just get the empty attributes or boxes that we wanna populate
    //basically just points or keeps track of the fields we wanna populate
    public static class UserViewHolder extends RecyclerView.ViewHolder
    {
        TextView username;
        ImageView friendButton;

        public UserViewHolder(@NonNull View itemView)
        {
            super(itemView);
            friendButton = itemView.findViewById(R.id.friendButton);

        }
    }

    //now we need to make a helper methid so every time use types letter the screen is refreshed/updated withnew results
    public void updateUserList(ArrayList<SearchUser> newList)
    {
        //updates old to nrw list after we typed another letter in
        this.users = newList;
        notifyDataSetChanged();
    }


}

