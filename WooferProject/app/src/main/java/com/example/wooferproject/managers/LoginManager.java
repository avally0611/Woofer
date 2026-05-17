package com.example.wooferproject.managers;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * This class handles all the background communication for user login. 
 * It acts as the bridge between our login screen and the server database.
 */
public class LoginManager {

    // We use OkHttpClient to manage our connection to the internet.
    private final OkHttpClient client = new OkHttpClient();

    public interface LoginCallback {
        void onSuccess(int userId);
        void onFailure(String error);
    }

    // This section prepares the network request, sends the credentials to our 
    // login script, and processes the server's reply.
    public void login(String username, String password, LoginCallback callback) {
        RequestBody formBody = new FormBody.Builder()
                .add("username", username)
                .add("password", password)
                .build();

        Request request = new Request.Builder()
                .url("https://wmc.ms.wits.ac.za/students/sgroup2668/login.php")
                .post(formBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onFailure(e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    callback.onFailure("Error " + response);
                    return;
                }

                try {
                    String responseData = response.body().string();
                    JSONObject json = new JSONObject(responseData);

                    if (json.getBoolean("success")) {
                        int userId = json.getInt("user_id");
                        callback.onSuccess(userId);
                    } else {
                        callback.onFailure(json.getString("message"));
                    }
                } catch (JSONException e) {
                    callback.onFailure("JSON Error: " + e.getMessage());
                }
            }
        });
    }
}
