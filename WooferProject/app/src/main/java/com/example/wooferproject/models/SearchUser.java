package com.example.wooferproject.models;

//this is a model that helps with shwoing if a searched user is a friend or not
public class SearchUser {
    public int userID;
    public String username;
    public boolean isFriend;

    public SearchUser(int userID, String username, boolean isFriend)
    {
        this.userID = userID;
        this.username = username;
        this.isFriend = isFriend;

    }

    public int getUserID() {
        return userID;
    }

    public String getUsername() {
        return username;
    }

    public boolean isFriend() {
        return isFriend;
    }

    public void setFriend(boolean friend) {
        isFriend = friend;
    }
}
