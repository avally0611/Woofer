package com.example.wooferproject.managers;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import android.os.Handler;
import android.os.Looper;
import org.json.JSONException;
import org.json.JSONObject;

public class PostPageManager {

    private static final OkHttpClient c = new OkHttpClient();
    // allows for internet connection
    private static final String BASE_URL = "https://wmc.ms.wits.ac.za/students/sgroup2668/";

    public interface PostCallback {
        void onSuccess(String message);
        void onFailure(String error);
    }

    // creates a post witht he text and image requirements
    public static void createPost(int userId,
                                  String text,
                                  String location,
                                  byte[] imageBytes,
                                  PostCallback callback) {

        if ((text == null || text.trim().isEmpty()) &&
                (location == null || location.trim().isEmpty()) &&
                (imageBytes == null || imageBytes.length == 0)) {
            callback.onFailure("Error: Please fill in text, location or image.");
            return;
        }

        MultipartBody.Builder multipartBodyBuilder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("user_id", String.valueOf(userId));

        if (text != null && !text.trim().isEmpty()) {
            multipartBodyBuilder.addFormDataPart("text", text);
        }
        if (location != null && !location.trim().isEmpty()) {
            multipartBodyBuilder.addFormDataPart("location", location);
        }
        if (imageBytes != null && imageBytes.length > 0) {
            multipartBodyBuilder.addFormDataPart("image", "post.jpg",
                    RequestBody.create(MediaType.parse("image/jpeg"), imageBytes));
        }

        RequestBody requestBody = multipartBodyBuilder.build();

        Request request = new Request.Builder()
                .url(BASE_URL + "create_post.php")
                .post(requestBody)
                .build();

        c.newCall(request).enqueue(new Callback() {
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
                        new Handler(Looper.getMainLooper()).post(() -> {
                            if (jsonResponse.optBoolean("success", false)) {
                                callback.onSuccess(jsonResponse.optString("message", "Post created successfully!"));
                            } else {
                                callback.onFailure(jsonResponse.optString("message", "Failed to create post."));
                            }
                        });
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