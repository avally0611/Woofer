package com.example.wooferproject.interfaces;

import com.example.wooferproject.models.SearchUser;

import java.util.ArrayList;

public interface SearchCallBack {
    void onSuccess(ArrayList<SearchUser> fetchedUsers);

    void onFailure(String error);
}
