package com.example.wooferproject.activities;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
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

        String username = currPost.getUsername();
        //we have to first check if username empty so it doesnt show on screen
        if (username == null || username.equalsIgnoreCase("null") || username.trim().isEmpty())
        {
            holder.username.setVisibility(View.GONE);
        }
        else
        {
            holder.username.setVisibility(View.VISIBLE);
            holder.username.setText(username);
        }

        String text = currPost.getText();
        if (text == null || text.equalsIgnoreCase("null") || text.trim().isEmpty())
        {
            holder.text.setVisibility(View.GONE);
        }
        else
        {
            holder.text.setVisibility(View.VISIBLE);
            holder.text.setText(text.trim());
        }

        String location = currPost.getLocation();
        if (location == null || location.equalsIgnoreCase("null") || location.trim().isEmpty())
        {
            holder.location.setVisibility(View.GONE);
        }
        else
        {
            holder.location.setVisibility(View.VISIBLE);
            holder.location.setText(location);
        }

        //now gotta decode image from db
        String imgString = currPost.getImage();

        if (imgString != null && !imgString.equalsIgnoreCase("null") && !imgString.trim().isEmpty())
        {
            //so the image was sent in base 64 string format so dstabae could hold image nicely - decode back to byte array
            byte[] decodedString = Base64.decode(imgString, Base64.DEFAULT);
            //now we convert the byte array into a bitmap picture - now in prop pic format
            Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

            //now we can take image and set it to imageview attribute
            holder.image.setImageBitmap(decodedByte);
            holder.image.setVisibility(View.VISIBLE);

        }
        else
        {
            //if now image, rememebr image view doesnt get populated and we hdie it so it doesn tlook weird
            holder.image.setVisibility(View.GONE);
        }


        //now we do the upvaote logic ----
        holder.upvoteCount.setText(String.valueOf(currPost.getUpvotes()));


        //here is where we change colour if user clicked button or unvoted
        if (currPost.isUserUpvoted())
        {
            holder.upvoteButton.setColorFilter(android.graphics.Color.RED);
        }
        else
        {
            holder.upvoteButton.setColorFilter(android.graphics.Color.BLACK);
        }

        //now we change count and update count to database
        holder.upvoteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //so if its already clciked, then theyre unclicking
                if (currPost.isUserUpvoted())
                {
                    currPost.setUserUpvoted(false);
                    currPost.setUpvotes(currPost.getUpvotes() - 1);
                    holder.upvoteButton.setColorFilter(android.graphics.Color.BLACK);
                }

                //otherwise nit clciked so actually wanna upvoet
                else
                {
                    currPost.setUserUpvoted(true);
                    currPost.setUpvotes(currPost.getUpvotes() + 1);
                    holder.upvoteButton.setColorFilter(android.graphics.Color.RED);
                }

                // Update the number on the screen instantly
                holder.upvoteCount.setText(String.valueOf(currPost.getUpvotes()));
            }
        });
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
        ImageView image;
        ImageView upvoteButton;
        TextView upvoteCount;

        public PostViewHolder(@NonNull View itemView)
        {
            super(itemView);
            username = itemView.findViewById(R.id.postUser);
            location = itemView.findViewById(R.id.postLocation);
            text = itemView.findViewById(R.id.postText);
            image = itemView.findViewById(R.id.postImage);
            upvoteButton = itemView.findViewById(R.id.upvoteButton);
            upvoteCount = itemView.findViewById(R.id.upvoteCount);

        }
    }
    public void updatePosts(List<Post> newPosts)
    {
        posts.clear();
        posts.addAll(newPosts);
        notifyDataSetChanged();
    }

}
