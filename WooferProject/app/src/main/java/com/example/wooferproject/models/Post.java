package com.example.wooferproject.models;

public class Post {
    private int postid;
    private String username;
    private String text;
    private String location;
    private String image;
    private int upvotes;
    private boolean userUpvoted;


    public Post(String username, String text, String location, String image,int postid,  int upvotes) {
        this.postid = postid;
        this.username = username;
        this.text = text;
        this.location = location;
        this.image = image;
        this.upvotes = upvotes;
        this.userUpvoted = false;

    }

    public int getPostid() {
        return postid;
    }

    public String getUsername() {
        return username;
    }

    public String getText() {
        return text;
    }

    public String getLocation() {
        return location;
    }

    public String getImage() {return image;}

    public int getUpvotes() {
        return upvotes;
    }

    public void setUpvotes(int upvotes) {
        this.upvotes = upvotes;
    }

    public boolean isUserUpvoted() {
        return userUpvoted;
    }

    public void setUserUpvoted(boolean userUpvoted) {
        this.userUpvoted = userUpvoted;
    }
}
