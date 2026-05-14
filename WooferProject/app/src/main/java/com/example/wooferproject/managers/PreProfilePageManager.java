package com.example.wooferproject.managers;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.wooferproject.models.Post;
import com.example.wooferproject.models.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class PreProfilePageManager {

    private static final String TAG = "PreProfilePageManager";
    private static RequestQueue requestQueue;

    private static final String BASE_URL = "https://wmc.ms.wits.ac.za/students/sgroup2668/";
    private static final String GET_PROFILE_DETAILS_URL = BASE_URL + "get_profile.php";
    private static final String GET_PROFILE_IMAGE_URL = BASE_URL + "get_profile_pic.php";
    private static final String GET_COUNTS_URL = BASE_URL + "get_profile_counts.php";
    // FIXED: Changed URL to match the PHP filename
    private static final String GET_POSTS_URL = BASE_URL + "get_post_pic.php";

    public interface ProfileDetailsCallback {
        void onSuccess(User user, int postCount, int friendCount);
        void onFailure(String error);
    }

    public interface ImageCallback {
        void onSuccess(byte[] imageBytes);
        void onFailure(String error);
    }

    public interface ProfileImageCallback {
        void onSuccess(Bitmap imageBitmap);
        void onFailure(String error);
    }

    public interface PostsCallback {
        void onSuccess(List<Post> posts);
        void onFailure(String error);
    }

    public static synchronized void init(Context context) {
        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(context.getApplicationContext());
        }
    }

    public static void getProfileData(int userId, ProfileDetailsCallback callback) {
        if (requestQueue == null) {
            callback.onFailure("RequestQueue not initialized.");
            return;
        }

        StringRequest profileDetailsRequest = new StringRequest(
                Request.Method.GET,
                GET_PROFILE_DETAILS_URL + "?id=" + userId,
                response -> {
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        if (jsonResponse.getBoolean("success")) {
                            String username = jsonResponse.getString("username");
                            String name = jsonResponse.getString("name");
                            String email = jsonResponse.getString("email");
                            User user = new User(name, username, email);

                            // Fetch counts
                            fetchCounts(userId, user, callback);
                        } else {
                            callback.onFailure(jsonResponse.optString("message", "Unknown error"));
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "Profile parse error", e);
                        callback.onFailure("Failed to parse profile details");
                    }
                },
                error -> callback.onFailure("Network error fetching details")
        );
        requestQueue.add(profileDetailsRequest);
    }

    private static void fetchCounts(int userId, User user, ProfileDetailsCallback callback) {
        StringRequest countsRequest = new StringRequest(
                Request.Method.GET,
                GET_COUNTS_URL + "?id=" + userId,
                countsResponse -> {
                    try {
                        JSONObject countsJson = new JSONObject(countsResponse);
                        if (countsJson.getBoolean("success")) {
                            int postCount = countsJson.getInt("post_count");
                            int friendCount = countsJson.getInt("friend_count");
                            callback.onSuccess(user, postCount, friendCount);
                        } else {
                            callback.onFailure(countsJson.optString("message", "Error fetching counts"));
                        }
                    } catch (JSONException e) {
                        callback.onFailure("Failed to parse counts");
                    }
                },
                error -> callback.onFailure("Network error fetching counts")
        );
        requestQueue.add(countsRequest);
    }

    public static void getProfileImage(int userId, ProfileImageCallback callback) {
        if (requestQueue == null) {
            callback.onFailure("RequestQueue not initialized.");
            return;
        }

        StringRequest stringRequest = new StringRequest(
                Request.Method.GET,
                GET_PROFILE_IMAGE_URL + "?id=" + userId,
                response -> {
                    if (response == null || response.trim().isEmpty() || response.equals("null")) {
                        callback.onSuccess(null);
                        return;
                    }
                    try {
                        byte[] decodedString = Base64.decode(response.trim(), Base64.DEFAULT);
                        Bitmap bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                        callback.onSuccess(bitmap);
                    } catch (Exception e) {
                        Log.e(TAG, "Image decode error", e);
                        callback.onFailure("Failed to decode image");
                    }
                },
                error -> callback.onFailure("Network error fetching image")
        );
        requestQueue.add(stringRequest);
    }

    public static void getPosts(int userId, PostsCallback callback) {
        if (requestQueue == null) {
            callback.onFailure("RequestQueue not initialized.");
            return;
        }

        String url = GET_POSTS_URL + "?id=" + userId;
        StringRequest stringRequest = new StringRequest(
                Request.Method.GET,
                url,
                response -> {
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
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
                        callback.onSuccess(posts);
                    } catch (Exception e) {
                        Log.e(TAG, "Posts parse error", e);
                        callback.onFailure("Parse error: " + e.getMessage());
                    }
                },
                error -> callback.onFailure("Network error: " + error.getMessage())
        );
        requestQueue.add(stringRequest);
    }

    public static void getPostImage(int postId, ImageCallback callback) {
        new Thread(() -> {
            try {
                URL url = new URL(BASE_URL + "getpost.pic.php?id=" + postId);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder result = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    result.append(line);
                }
                br.close();

                String base64 = result.toString().trim();
                // FIXED: Use Handler to return result on Main Thread to avoid crashes
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
        }).start();
    }
}