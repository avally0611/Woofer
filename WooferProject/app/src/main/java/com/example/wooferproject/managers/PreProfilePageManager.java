package com.example.wooferproject.managers;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.util.Log;


import com.example.wooferproject.models.Post;
import com.example.wooferproject.models.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class PreProfilePageManager {
    private static final OkHttpClient c = new OkHttpClient();
    // thi is used to connect to the internet/ Volley can also be used instead

    private static final String TAG = "PreProfilePageManager";

    private static final String BASE_URL = "https://wmc.ms.wits.ac.za/students/sgroup2668/";
    private static final String GET_PROFILE_DETAILS_URL = BASE_URL + "get_profile.php";
    private static final String GET_PROFILE_IMAGE_URL = BASE_URL + "get_profile_pic.php";
    private static final String GET_COUNTS_URL = BASE_URL + "get_profile_counts.php";
    private static final String GET_POSTS_URL = BASE_URL + "get_post_pic.php";
    private static final String GET_FRIENDS_URL = BASE_URL + "get_friends.php";
    private static final String UNFRIEND_URL = BASE_URL + "unfriend.php";
    // this provides an easier way to access all the php files

    // gets the text details of the profile
    public interface ProfileDetailsCallback {
        void onSuccess(User user, int postCount, int friendCount);
        void onFailure(String error);
    }

    // gets the image details of the profile
    public interface ImageCallback {
        void onSuccess(byte[] imageBytes);
        void onFailure(String error);
    }

    // gets the profile image
    public interface ProfileImageCallback {
        void onSuccess(Bitmap imageBitmap);
        void onFailure(String error);
    }

    // gets the posts
    public interface PostsCallback {
        void onSuccess(List<Post> posts);
        void onFailure(String error);
    }

    // gets the friends
    public interface FriendsCallback {
        void onSuccess(List<User> friends);
        void onFailure(String error);
    }

    // unfriends
    public interface UnfriendCallback {
        void onSuccess(String message);
        void onFailure(String error);
    }

    public static synchronized void init(Context context) {
        // OkHttpClient is initialized and reused
    }

    public static void getProfileData(int userId, int viewerId, ProfileDetailsCallback callback) {
        String url = GET_PROFILE_DETAILS_URL + "?id=" + userId + "&viewer_id=" + viewerId;
        Request request = new Request.Builder().url(url).build();

        c.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onFailure("Network error fetching details: " + e.getMessage());
                // safety message if error occurs
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String responseData = response.body().string();
                        JSONObject jsonResponse = new JSONObject(responseData);
                        if (jsonResponse.getBoolean("success")) {
                            String username = jsonResponse.getString("username");
                            String name = jsonResponse.getString("name");
                            String email = jsonResponse.getString("email");
                            User user = new User(name, username, email);
                            user.id = userId; // ensure ID is set
                            user.isFriend = jsonResponse.optBoolean("is_friend", false);

                            fetchCounts(userId, user, callback);
                        } else {
                            callback.onFailure(jsonResponse.optString("message", "Unknown error"));
                        }
                    } catch (JSONException e) {
                        callback.onFailure("Failed to parse profile details");
                    }
                } else {
                    callback.onFailure("Server error: " + response.code());
                }
            }
        });
    }

    // gets the counts of the posts and the friends
    // tells you how many posts posted and how many friends of the user
    private static void fetchCounts(int userId, User user, ProfileDetailsCallback callback) {
        Request request = new Request.Builder()
                .url(GET_COUNTS_URL + "?id=" + userId)
                .build();

        c.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onFailure("Network error fetching counts: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String responseData = response.body().string();
                        JSONObject countsJson = new JSONObject(responseData);
                        if (countsJson.getBoolean("success")) {
                            int postCount = countsJson.getInt("post_count");
                            int friendCount = countsJson.getInt("friend_count");
                            new Handler(Looper.getMainLooper()).post(() -> callback.onSuccess(user, postCount, friendCount));
                        } else {
                            callback.onFailure(countsJson.optString("message", "Error fetching counts"));
                        }
                    } catch (JSONException e) {
                        callback.onFailure("Failed to parse counts");
                    }
                } else {
                    callback.onFailure("Server error: " + response.code());
                }
            }
        });
    }

    public static void getProfileImage(int userId, ProfileImageCallback callback) {
        Request request = new Request.Builder()
                .url(GET_PROFILE_IMAGE_URL + "?id=" + userId)
                .build();

        c.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onFailure("Network error fetching image: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String responseData = response.body().string();
                        if (responseData == null || responseData.trim().isEmpty() || responseData.equals("null")) {
                            new Handler(Looper.getMainLooper()).post(() -> callback.onSuccess(null));
                            return;
                        }
                        byte[] decodedString = Base64.decode(responseData.trim(), Base64.DEFAULT);
                        Bitmap bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                        new Handler(Looper.getMainLooper()).post(() -> callback.onSuccess(bitmap));
                    } catch (Exception e) {
                        callback.onFailure("Failed to decode image");
                    }
                } else {
                    callback.onFailure("Server error: " + response.code());
                }
            }
        });
    }

    public static void getPosts(int userId, PostsCallback callback) {
        String url = GET_POSTS_URL + "?id=" + userId;
        Request request = new Request.Builder().url(url).build();

        c.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onFailure("Network error: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String responseData = response.body().string();
                        JSONObject jsonResponse = new JSONObject(responseData);
                        if (!jsonResponse.getBoolean("success")) {
                            callback.onFailure(jsonResponse.optString("message", "Failed to load posts"));
                            return;
                        }

                        JSONArray jsonArray = jsonResponse.getJSONArray("posts");
                        List<Post> posts = new ArrayList<>();
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject obj = jsonArray.getJSONObject(i);
                            posts.add(new Post(
                                    obj.getString("username"),
                                    obj.getString("text"),
                                    obj.optString("location", ""),
                                    obj.optString("image", ""),
                                    obj.getInt("post_id"),
                                    obj.optInt("upvotes", 0)
                            ));
                        }
                        new Handler(Looper.getMainLooper()).post(() -> callback.onSuccess(posts));
                    } catch (Exception e) {
                        callback.onFailure("Parse error: " + e.getMessage());
                    }
                } else {
                    callback.onFailure("Server error: " + response.code());
                }
            }
        });
    }

    public static void getPostImage(int postId, ImageCallback callback) {
        Request request = new Request.Builder()
                .url(BASE_URL + "getpost.pic.php?id=" + postId)
                .build();

        c.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                new Handler(Looper.getMainLooper()).post(() -> callback.onFailure("Error: " + e.getMessage()));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String base64 = response.body().string().trim();
                        Handler mainHandler = new Handler(Looper.getMainLooper());
                        if (base64.isEmpty() || base64.equals("null")) {
                            mainHandler.post(() -> callback.onFailure("No image found"));
                            return;
                        }
                        byte[] decoded = Base64.decode(base64, Base64.DEFAULT);
                        if (decoded.length == 0) {
                            mainHandler.post(() -> callback.onFailure("Decode failed"));
                        } else {
                            mainHandler.post(() -> callback.onSuccess(decoded));
                        }
                    } catch (Exception e) {
                        new Handler(Looper.getMainLooper()).post(() -> callback.onFailure("Error: " + e.getMessage()));
                    }
                } else {
                    new Handler(Looper.getMainLooper()).post(() -> callback.onFailure("Server error: " + response.code()));
                }
            }
        });
    }

    public static void getFriends(int userId, FriendsCallback callback) {
        String url = GET_FRIENDS_URL + "?user_id=" + userId;
        Request request = new Request.Builder().url(url).build();

        c.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onFailure("Network error fetching friends: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String responseData = response.body().string();
                        JSONObject jsonResponse = new JSONObject(responseData);
                        if (!jsonResponse.getBoolean("success")) {
                            callback.onFailure(jsonResponse.optString("message", "Failed to load friends"));
                            return;
                        }
                        JSONArray jsonArray = jsonResponse.getJSONArray("friends");
                        List<User> friends = new ArrayList<>();
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject obj = jsonArray.getJSONObject(i);
                            friends.add(new User(
                                    obj.getInt("id"),
                                    obj.getString("username"),
                                    ""
                            ));
                        }
                        new Handler(Looper.getMainLooper()).post(() -> callback.onSuccess(friends));
                    } catch (JSONException e) {
                        callback.onFailure("Failed to parse friends list");
                    }
                } else {
                    callback.onFailure("Server error: " + response.code());
                }
            }
        });
    }

    public static void unfriend(int userId, int friendId, UnfriendCallback callback) {
        RequestBody formBody = new FormBody.Builder()
                .add("user_id", String.valueOf(userId))
                .add("friend_id", String.valueOf(friendId))
                .build();

        Request request = new Request.Builder()
                .url(UNFRIEND_URL)
                .post(formBody)
                .build();

        c.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onFailure("Network error unfriending: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String responseData = response.body().string();
                        JSONObject jsonResponse = new JSONObject(responseData);
                        if (jsonResponse.getBoolean("success")) {
                            new Handler(Looper.getMainLooper()).post(() -> callback.onSuccess(jsonResponse.optString("message", "Unfriended")));
                        } else {
                            callback.onFailure(jsonResponse.optString("message", "Failed to unfriend"));
                        }
                    } catch (JSONException e) {
                        callback.onFailure("Failed to parse unfriend response");
                    }
                } else {
                    callback.onFailure("Server error: " + response.code());
                }
            }
        });
    }
    public static void addFriend(int userId, int friendId, UnfriendCallback callback) {
        RequestBody formBody = new FormBody.Builder()
                .add("user_id", String.valueOf(userId))
                .add("friend_id", String.valueOf(friendId))
                .build();

        Request request = new Request.Builder()
                .url(BASE_URL + "add_friend.php")
                .post(formBody)
                .build();

        c.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onFailure("Network error: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String responseData = response.body().string();
                        JSONObject json = new JSONObject(responseData);
                        if (json.getBoolean("success")) {
                            new Handler(Looper.getMainLooper()).post(() -> callback.onSuccess(json.optString("message", "Friend added")));
                        } else {
                            callback.onFailure(json.optString("message", "Failed to add friend"));
                        }
                    } catch (JSONException e) {
                        callback.onFailure("Parse error");
                    }
                } else {
                    callback.onFailure("Server error: " + response.code());
                }
            }
        });
    }

}