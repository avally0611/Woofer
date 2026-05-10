package com.example.wooferproject.activities;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wooferproject.R;
import com.example.wooferproject.models.Post;

import java.util.ArrayList;
import java.util.List;

//so recycler view has a built in adapter but we have to make our own specific to the project so we make our own object that extends the recycler object's mrthods
public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder>
{
    private ArrayList<Post> posts;

    //this allows home actity to send the list posts to the adapter so it can start drawing the cards
    public PostAdapter(ArrayList<Post> posts)
    {
        this.posts = posts;
    }

    //okay so we first create a blank card so as soon as screen is made we can display on screen

    @NonNull
    @Override
    //this method goes to our template (post_template) and builds a real java object so we can populate it
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        //a view is jsut object on screen: e.g textview, button, image,etc
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.post_template, parent, false);
        return new PostViewHolder(view);
        //basically just returns a blank card tnat has fields accesible to populate
    }

    //this method is the REAL ONE - basically gets data from db and pputs it on blank card we created - populate card
    //so when user scrolls to post o next post n, it sends that number as pos so it gets that post as a card and displays on UI
    public void onBindViewHolder(@NonNull PostViewHolder holder, int pos)
    {
        Post currPost = posts.get(pos);
        holder.username.setText(currPost.getUsername());
        holder.location.setText(currPost.getLocation());
        holder.text.setText(currPost.getText());
    }

    //need this to tell recycler view how many cards exist in view
    public int getItemCount()
    {
        return posts.size();
    }


    //so we dont make this in its own file because its only used by post adapter
    //jsut like post adapter, recyelerview has a view holder that just get the empty attributes or boxes that we wanna populate
    //basically just points or keeps track of the fields we wanna populate
    public static class PostViewHolder extends RecyclerView.ViewHolder
    {
        TextView username, location, text;

        public PostViewHolder(@NonNull View itemView)
        {
            super(itemView);
            username = itemView.findViewById(R.id.postUser);
            location = itemView.findViewById(R.id.postLocation);
            text = itemView.findViewById(R.id.postText);
        }
    }
    public void updatePosts(List<Post> newPosts)
    {
        posts.clear();
        posts.addAll(newPosts);
        notifyDataSetChanged();
    }

}
