package com.example.wooferproject.managers;

import androidx.annotation.NonNull;

import com.example.wooferproject.interfaces.SearchCallBack;
import com.example.wooferproject.models.SearchUser;

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

public class SearchManager {
    private final OkHttpClient c = new OkHttpClient();
    public void searchUsers(int userId, String searchQuery, SearchCallBack callback)
    {

        RequestBody formBody = new FormBody.Builder()
                .add("my_user_id", String.valueOf(userId))
                .add("search_query", searchQuery)
                .build();

        Request request = new Request.Builder()
                .url("https://wmc.ms.wits.ac.za/students/sgroup2668/search_users.php")
                .post(formBody)
                .build();

        c.newCall(request).enqueue(new Callback()
        {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onFailure("Network Error: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    callback.onFailure("Server Error: " + response.code());
                    return;
                }

                try {
                    //get the data from php file, put in JSON array and then we go through each object in json array nd break into 3 fields we wsnt
                    String responseData = response.body().string();
                    JSONArray jsonArray = new JSONArray(responseData);
                    ArrayList<SearchUser> users = new ArrayList<>();

                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject userObj = jsonArray.getJSONObject(i);

                        int id = userObj.getInt("user_id");
                        String username = userObj.getString("username");
                        boolean isFriend = userObj.getBoolean("is_friend");

                        users.add(new SearchUser(id, username, isFriend));
                    }

                    // this will give lsit to activity class
                    callback.onSuccess(users);

                } catch (JSONException e) {
                    callback.onFailure("JSON Error: " + e.getMessage());
                }
            }
        });
    }

    public void addOrRemoveFriend(int myUserID, int friendID, String action)
    {
        String url = "";
        if (action.equals("add"))
        {
            url = "https://wmc.ms.wits.ac.za/students/sgroup2668/add_friend.php";
        }
        else
        {
            url = "https://wmc.ms.wits.ac.za/students/sgroup2668/remove_friend.php";
        }

        RequestBody formbody = new FormBody.Builder()
                .add("my_user_id", String.valueOf(myUserID))
                .add("friend_id", String.valueOf(friendID))
                .build();

        Request request = new Request.Builder()
                .url(url)
                .post(formbody)
                .build();

        c.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                android.util.Log.e("Adding friend","Network failed: " + e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException
            {
                android.util.Log.e("Adding friend","Friend" + action +  "success!");

            }
        });

    }

    public void getMutuals(int userId, SearchCallBack callback) {

        RequestBody formBody = new FormBody.Builder()
                .add("my_user_id", String.valueOf(userId))
                .build();

        Request request = new Request.Builder()
                .url("https://wmc.ms.wits.ac.za/students/sgroup2668/get_mutuals.php")
                .post(formBody)
                .build();

        c.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(okhttp3.Call call, java.io.IOException e) {
                callback.onFailure("Network Error: " + e.getMessage());
            }

            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) throws java.io.IOException {
                if (!response.isSuccessful()) {
                    callback.onFailure("Server Error");
                    return;
                }
                try {
                    String responseData = response.body().string();
                    JSONArray jsonArray = new JSONArray(responseData);
                    ArrayList<SearchUser> mutualList = new ArrayList<>();

                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject userObj = jsonArray.getJSONObject(i);
                        mutualList.add(new SearchUser(
                                userObj.getInt("user_id"),
                                userObj.getString("username"),
                                userObj.getBoolean("is_friend")
                        ));
                    }
                    callback.onSuccess(mutualList);
                } catch (org.json.JSONException e) {
                    callback.onFailure("JSON Error");
                }
            }
        });
    }
}
