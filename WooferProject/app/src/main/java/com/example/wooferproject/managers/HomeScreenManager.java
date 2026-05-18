package com.example.wooferproject.managers;

import com.example.wooferproject.interfaces.PostCallback;
import com.example.wooferproject.models.Post;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

//manager files deal with backend - hectic thinking and logic
public class HomeScreenManager {

    //okay so we gonna make the OkHttpClient object - this handles the internet connection
    private final OkHttpClient c = new OkHttpClient();

    //now we create an interface that returns or like signals the HomeActivity class when HomeScreenManager has finished getting data from db
    //if it was successful - returns the list of data othweise return error message

    //okay so now this is our main fucntion get all the posts from db- we have to have user id in input and a PostCallback to send user id to db to get posts from user friends and a postcallback to return success or error
    public void getPosts(int userId, PostCallback pc)
    {
        //
        RequestBody formBody = new FormBody.Builder()
                .add("my_user_id", String.valueOf(userId))
                .build();

        Request request = new Request.Builder()
                .url("https://wmc.ms.wits.ac.za/students/sgroup2668/get_posts.php")
                .post(formBody)
                .build();

        //enqueue tells okhttp to run this in backrgound - so app doesnt freeze
        c.newCall(request).enqueue(new Callback()
        {
            //if the server not working - gives error mesage
            @Override
            public void onFailure(Call call, IOException e)
            {
                pc.onFailure(e.getMessage());
            }
            //if we get php script this method runs - we then check if any error with php - otherwise we grab the JSON text
            @Override
            public void onResponse(Call call, Response response) throws IOException
            {
                if (!response.isSuccessful())
                {
                    pc.onFailure("Error " + response);
                    return;
                }

                try
                {
                    //we try to get JSON code - wrap in try catch so it catchs php error instead of pritning it out or giving weird results
                    String responseData = response.body().string(); //this is raw JSON text

                    //now convert raw text into JSON array
                    JSONArray jsonArr = new JSONArray(responseData);



                    //now we put this post objects into an arraylist:

                    ArrayList<Post> posts = new ArrayList<Post>();

                    for (int i = 0; i < jsonArr.length(); i++)
                    {
                        JSONObject jsonPost = jsonArr.getJSONObject(i);

                        Post newPost = new Post(jsonPost.getString("username"), jsonPost.optString("text", ""), jsonPost.optString("location", ""), jsonPost.optString("image", ""),jsonPost.getInt("post_id"), jsonPost.optInt("upvotes",0));
                        boolean userUpVoted = jsonPost.optBoolean("user_upvoted", false);
                        newPost.setUserUpvoted(userUpVoted);
                        posts.add(newPost);
                    }


                    //okay so we got our posts objects - so can signal actviity class and send arraylist for UI update
                    pc.onSuccess(posts);


                } catch (JSONException e) {
                    //now in case we get error with JSOn or php code
                    pc.onFailure("JSON Error: " + e.getMessage());
                }
            }
        });

    }

    //okay now we need a separate function to handle the upvotes
    // This fires silently in the background whenever a user taps the heart
    public void sendUpvote(int userID, int postId, String action) {

        RequestBody formBody = new FormBody.Builder()
                .add("my_user_id", String.valueOf(userID))
                .add("post_id", String.valueOf(postId))
                .add("action", action) //wheter to icnrease or decrease upvote
                .build();

        Request request = new Request.Builder()
                .url("https://wmc.ms.wits.ac.za/students/sgroup2668/update_upvote.php")
                .post(formBody)
                .build();

        //enqueue tells okhttp to run this in backrgound - so app doesnt freeze
        c.newCall(request).enqueue(new Callback()
        {
            @Override
            public void onFailure(Call call, IOException e) {
                android.util.Log.e("BondUpvote", "Failed: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    android.util.Log.d("BondUpvote", "Database updated successfully");
                }
            }
        });
    }
}