package com.example.wooferproject.interfaces;


import com.example.wooferproject.models.Post;

import java.util.ArrayList;

//so we make thuis interface so that we can use in alll the manager classes since w eget data from db using okhttp
//so this interface essentially just signals activity classes when it got data back and it tells ativity whether it actually got a result or an error
public interface PostCallback
{
    //when backend gets results from db
    void onSuccess(ArrayList<Post> posts);
    //when there is an error getting data from db
    void onFailure(String error);
}
