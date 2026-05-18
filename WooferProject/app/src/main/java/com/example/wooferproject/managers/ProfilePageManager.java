package com.example.wooferproject.managers;

import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.util.Log;

import com.example.wooferproject.models.User;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ProfilePageManager {

    private static final String TAG = "ProfilePageManager";
    private static final OkHttpClient c = new OkHttpClient();
    // this is used to connect to the internet/ Volley can also be used instead
    private static final String BASE_URL = "https://wmc.ms.wits.ac.za/students/sgroup2668/";

    // Update the profile
    public static void updateProfile(int userId, String name, String username, String email) {
        RequestBody formBody = new FormBody.Builder()
                .add("id", String.valueOf(userId))
                .add("name", name)
                .add("username", username)
                .add("email", email)
                .build();

        Request request = new Request.Builder()
                .url(BASE_URL + "update_profile.php")
                .post(formBody)
                .build();

        c.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "Update failed: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    Log.d(TAG, "Profile updated successfully");
                }
            }
        });
    }

    // Get the profile
    public static void getProfile(int userId, Callback callback) {
        Request request = new Request.Builder()
                .url(BASE_URL + "get_profile.php?id=" + userId)
                .build();

        c.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onFailure(e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String responseData = response.body().string();
                        JSONObject json = new JSONObject(responseData);
                        if (!json.getBoolean("success")) {
                            callback.onFailure(json.getString("message"));
                            return;
                        }
                        User user = new User(
                                json.getString("name"),
                                json.getString("username"),
                                json.getString("email")
                        );
                        new Handler(Looper.getMainLooper()).post(() -> callback.onSuccess(user));
                    } catch (JSONException e) {
                        callback.onFailure("Parse error");
                    }
                } else {
                    callback.onFailure("Server error: " + response.code());
                }
            }
        });
    }

    // callback interface, helps with the getprofile method
    public interface Callback {
        void onSuccess(User user);
        void onFailure(String error);
    }
    // this is for deleting account
    public interface DeleteCallback {
        void onSuccess();
        void onFailure(String error);
    }
    // this is for profile image
    public static void uploadProfileImage(int userId, byte[] imageBytes) {
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("id", String.valueOf(userId))
                .addFormDataPart("image", "profile.jpg",
                        RequestBody.create(MediaType.parse("image/jpeg"), imageBytes))
                .build();

        Request request = new Request.Builder()
                .url(BASE_URL + "upload_profile_pic.php")
                .post(requestBody)
                .build();

        c.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "Image upload failed: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    Log.d(TAG, "Image upload successful");
                } else {
                    Log.e(TAG, "Image upload failed with code: " + response.code());
                }
            }
        });
    }

    // this gets the image from the database
    public static void getProfileImage(int userId, ImageCallback callback) {
        long timestamp = System.currentTimeMillis();
        Request request = new Request.Builder()
                .url(BASE_URL + "get_profile_pic.php?id=" + userId + "&t=" + timestamp)
                .build();

        c.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                new Handler(Looper.getMainLooper()).post(() -> callback.onFailure(e.getMessage()));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String base64 = response.body().string().trim();
                    Handler mainHandler = new Handler(Looper.getMainLooper());
                    if (base64.isEmpty() || base64.equals("null")) {
                        mainHandler.post(() -> callback.onFailure("No image found"));
                    } else {
                        byte[] decoded = Base64.decode(base64, Base64.DEFAULT);
                        mainHandler.post(() -> callback.onSuccess(decoded));
                    }
                } else {
                    new Handler(Looper.getMainLooper()).post(() -> callback.onFailure("Server error: " + response.code()));
                }
            }
        });
    }
    // this allows for account deletion
    public static void deleteAccount(int userId, DeleteCallback callback) {
        Request request = new Request.Builder()
                .url(BASE_URL + "delete_account.php?id=" + userId)
                .build();

        c.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                new Handler(Looper.getMainLooper()).post(() -> callback.onFailure("Network error: " + e.getMessage()));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String responseData = response.body().string();
                        JSONObject jsonResponse = new JSONObject(responseData);
                        Handler mainHandler = new Handler(Looper.getMainLooper());
                        if (jsonResponse.getBoolean("success")) {
                            mainHandler.post(callback::onSuccess);
                        } else {
                            mainHandler.post(() -> callback.onFailure(jsonResponse.optString("message", "Failed to delete account")));
                        }
                    } catch (JSONException e) {
                        new Handler(Looper.getMainLooper()).post(() -> callback.onFailure("Parse error during deletion"));
                    }
                } else {
                    new Handler(Looper.getMainLooper()).post(() -> callback.onFailure("Server error: " + response.code()));
                }
            }
        });
    }

    public interface ImageCallback {
        void onSuccess(byte[] image);
        void onFailure(String error);
    }

    public interface CheckCallback {
        void onSuccess(boolean exists);
        void onFailure(String error);
    }

    public static void checkUsernameExists(String username, CheckCallback callback) {
        Request request = new Request.Builder()
                .url(BASE_URL + "check_username.php?username=" + username)
                .build();

        c.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                new Handler(Looper.getMainLooper()).post(() -> callback.onFailure("Network error: " + e.getMessage()));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String responseData = response.body().string();
                        JSONObject jsonResponse = new JSONObject(responseData);
                        Handler mainHandler = new Handler(Looper.getMainLooper());
                        if (jsonResponse.getBoolean("exists")) {
                            mainHandler.post(() -> callback.onSuccess(true));
                        } else {
                            mainHandler.post(() -> callback.onSuccess(false));
                        }
                    } catch (JSONException e) {
                        new Handler(Looper.getMainLooper()).post(() -> callback.onFailure("Parse error: " + e.getMessage()));
                    }
                } else {
                    new Handler(Looper.getMainLooper()).post(() -> callback.onFailure("Server error: " + response.code()));
                }
            }
        });
    }

    public static void checkEmailExists(String email, CheckCallback callback) {
        Request request = new Request.Builder()
                .url(BASE_URL + "check_email.php?email=" + email)
                .build();

        c.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                new Handler(Looper.getMainLooper()).post(() -> callback.onFailure("Network error: " + e.getMessage()));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String responseData = response.body().string();
                        JSONObject jsonResponse = new JSONObject(responseData);
                        Handler mainHandler = new Handler(Looper.getMainLooper());
                        if (jsonResponse.getBoolean("exists")) {
                            mainHandler.post(() -> callback.onSuccess(true));
                        } else {
                            mainHandler.post(() -> callback.onSuccess(false));
                        }
                    } catch (JSONException e) {
                        new Handler(Looper.getMainLooper()).post(() -> callback.onFailure("Parse error: " + e.getMessage()));
                    }
                } else {
                    new Handler(Looper.getMainLooper()).post(() -> callback.onFailure("Server error: " + response.code()));
                }
            }
        });
    }
}