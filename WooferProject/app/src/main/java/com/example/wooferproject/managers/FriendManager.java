package com.example.wooferproject.managers;

import android.content.Context;
import android.util.Log;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.io.IOException;
import com.example.wooferproject.models.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FriendManager {
    private static final String TAG = "FriendManager";
    private static FriendManager instance;
    private final OkHttpClient c = new OkHttpClient();

    private static final String BASE_URL = "https://wmc.ms.wits.ac.za/students/sgroup2668/";
    private static final String GET_FRIENDS_URL = BASE_URL + "get_friends.php";
    private static final String ADD_FRIEND_URL = BASE_URL + "add_friend.php";
    private static final String DELETE_FRIEND_URL = BASE_URL + "remove_friend.php";

    private FriendManager(Context context) {
        // OkHttpClient is initialized once and reused
    }

    public static synchronized void init(Context context) {
        if (instance == null) {
            instance = new FriendManager(context);
        }
    }

    public static synchronized FriendManager getInstance() {
        if (instance == null) {
            throw new IllegalStateException("FriendManager not initialized. Call init(Context) first.");
        }
        return instance;
    }

    public interface FriendsCallback {
        void onSuccess(List<User> friends);
        void onFailure(String error);
    }

    public interface ActionCallback {
        void onSuccess(String message);
        void onFailure(String error);
    }

    /**
     * Fetches the friends list for a target user.
     * @param targetUserId The user whose friends we want to see.
     * @param viewerId The currently logged-in user (to check friendship status).
     */
    public void getFriends(int targetUserId, int viewerId, FriendsCallback callback) {
        String url = GET_FRIENDS_URL + "?user_id=" + targetUserId + "&viewer_id=" + viewerId;

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
                        JSONObject json = new JSONObject(responseData);
                        if (json.getBoolean("success")) {
                            JSONArray array = json.getJSONArray("friends");
                            List<User> friends = new ArrayList<>();
                            for (int i = 0; i < array.length(); i++) {
                                JSONObject obj = array.getJSONObject(i);
                                User user = new User(obj.getInt("id"), obj.getString("username"), "");
                                user.isFriend = obj.optBoolean("is_friend", false);
                                friends.add(user);
                            }
                            callback.onSuccess(friends);
                        } else {
                            callback.onFailure(json.optString("message", "Error fetching friends"));
                        }
                    } catch (JSONException e) {
                        callback.onFailure("Parse error: " + e.getMessage());
                    }
                } else {
                    callback.onFailure("Server error: " + response.code());
                }
            }
        });
    }

    public void addFriend(int myUserId, int friendId, ActionCallback callback) {
        performAction(ADD_FRIEND_URL, myUserId, friendId, callback);
    }

    public void unfriend(int myUserId, int friendId, ActionCallback callback) {
        performAction(DELETE_FRIEND_URL, myUserId, friendId, callback);
    }

    public void checkFriendship(int myUserId, int targetUserId, ActionCallback callback) {
        String url = BASE_URL + "check_friendship.php?user_id=" + myUserId + "&friend_id=" + targetUserId;
        Request request = new Request.Builder().url(url).build();

        c.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onFailure(e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String data = response.body().string();
                        JSONObject json = new JSONObject(data);
                        if (json.getBoolean("is_friend")) {
                            callback.onSuccess("friend");
                        } else {
                            callback.onSuccess("not_friend");
                        }
                    } catch (JSONException e) {
                        callback.onFailure("Parse error");
                    }
                } else {
                    callback.onFailure("Server error");
                }
            }
        });
    }

    private void performAction(String url, int myUserId, int friendId, ActionCallback callback) {
        RequestBody formBody = new FormBody.Builder()
                .add("my_user_id", String.valueOf(myUserId))
                .add("friend_id", String.valueOf(friendId))
                .build();

        Request request = new Request.Builder()
                .url(url)
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
                            callback.onSuccess(json.getString("message"));
                        } else {
                            callback.onFailure(json.optString("message", "Unknown error"));
                        }
                    } catch (JSONException e) {
                        callback.onFailure("Parse error: " + e.getMessage());
                    }
                } else {
                    callback.onFailure("Server error: " + response.code());
                }
            }
        });
    }
}