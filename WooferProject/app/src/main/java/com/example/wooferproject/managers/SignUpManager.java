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
 * This class deals with the background networking for creating a new user account.
 * It packages the user's data and sends it to our signup script on the server.
 */
public class SignUpManager {

    private final OkHttpClient client = new OkHttpClient();

    public interface SignUpCallback {
        void onSuccess(String message, int userId);
        void onFailure(String error);
    }

    // This block handles the entire registration request flow, from building the 
    // form to handling the success or failure messages from the database.
    public void register(String firstName, String lastName, String username, String email, String password, SignUpCallback callback) {
        RequestBody formBody = new FormBody.Builder()
                .add("name", firstName)
                .add("lastname", lastName)
                .add("username", username)
                .add("email", email)
                .add("password", password)
                .build();

        Request request = new Request.Builder()
                .url("https://wmc.ms.wits.ac.za/students/sgroup2668/signup.php")
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
                        int userId = json.optInt("user_id", -1);
                        callback.onSuccess(json.getString("message"), userId);
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
