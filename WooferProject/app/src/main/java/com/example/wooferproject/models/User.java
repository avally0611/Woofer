package com.example.wooferproject.models;

public class User {

    public int id;
    public String name;
    public String username;
    public String email;

    public User(String name, String username, String email) {
        this.name = name;
        this.username = username;
        this.email = email;
    }
    public User(int id, String username, String email) {
        this.id = id;
        this.username = username;
        this.email = email;
    }
}