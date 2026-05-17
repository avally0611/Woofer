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
 * This class coordinates all the network requests needed to safely reset a user's password.
 * It manages three distinct phases: sending an OTP, verifying it, and updating the password.
 */
public class ForgotPasswordManager {

    private final OkHttpClient client = new OkHttpClient();
    private static final String BASE_URL = "https://wmc.ms.wits.ac.za/students/sgroup2668/";

    public interface ForgotPasswordCallback {
        void onSuccess(String message);
        void onFailure(String error);
    }

    // This section handles the request to generate and email a fresh reset code to the user.
    public void sendOtp(String email, ForgotPasswordCallback callback) {
        RequestBody formBody = new FormBody.Builder()
                .add("email", email)
                .build();

        Request request = new Request.Builder()
                .url(BASE_URL + "send_otp.php")
                .post(formBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onFailure(e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                handleResponse(response, callback);
            }
        });
    }

    // This block manages the second step of confirming that the 6-digit code matches 
    // the one currently stored in our reset table.
    public void verifyOtp(String email, String otp, ForgotPasswordCallback callback) {
        RequestBody formBody = new FormBody.Builder()
                .add("email", email)
                .add("otp", otp)
                .build();

        Request request = new Request.Builder()
                .url(BASE_URL + "verify_otp.php")
                .post(formBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onFailure(e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                handleResponse(response, callback);
            }
        });
    }

    // This section handles the final database update that overwrites the user's 
    // old password with their new chosen one.
    public void resetPassword(String email, String newPassword, ForgotPasswordCallback callback) {
        RequestBody formBody = new FormBody.Builder()
                .add("email", email)
                .add("newPassword", newPassword)
                .build();

        Request request = new Request.Builder()
                .url(BASE_URL + "reset_password.php")
                .post(formBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onFailure(e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                handleResponse(response, callback);
            }
        });
    }

    // This shared helper block processes all the server's JSON replies and checks for success.
    private void handleResponse(Response response, ForgotPasswordCallback callback) throws IOException {
        if (!response.isSuccessful()) {
            callback.onFailure("Error: " + response.code());
            return;
        }

        try {
            String responseData = response.body() != null ? response.body().string() : "";
            JSONObject json = new JSONObject(responseData);

            if (json.getString("status").equals("success")) {
                callback.onSuccess(json.optString("message", "Success"));
            } else {
                callback.onFailure(json.getString("message"));
            }
        } catch (JSONException e) {
            callback.onFailure("JSON Error: " + e.getMessage());
        }
    }
}
