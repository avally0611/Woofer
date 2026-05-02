package com.example.wooferproject.interfaces;

public interface UpdateCallback {
    void onSuccess(String message);
    void onFailure(String error);
}