package com.example.wooferproject.managers;

import static android.content.ContentValues.TAG;

import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.example.wooferproject.models.User;

import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class ProfilePageManager {

    private static final String TAG = "ProfilePageManager";
    private static RequestQueue requestQueue;
    private static final String BASE_URL = "https://wmc.ms.wits.ac.za/students/sgroup2668/";

    // Update the profile
    public static void updateProfile(int userId, String name, String username, String email) {

        new Thread(() -> {
            try {
                URL url = new URL("https://wmc.ms.wits.ac.za/students/sgroup2668/update_profile.php");

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);

                String data =
                        "id=" + userId +
                                "&name=" + name +
                                "&username=" + username +
                                "&email=" + email;

                OutputStream os = conn.getOutputStream();
                os.write(data.getBytes());
                os.flush();
                os.close();
                conn.getInputStream();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    // Get the profile
    public static void getProfile(int userId, Callback callback) {

        new Thread(() -> {
            try {
                URL url = new URL("https://wmc.ms.wits.ac.za/students/sgroup2668/get_profile.php?id=" + userId);

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                BufferedReader br = new BufferedReader(
                        new InputStreamReader(conn.getInputStream())
                );

                StringBuilder result = new StringBuilder();
                String line;

                while ((line = br.readLine()) != null) {
                    result.append(line);
                }

                JSONObject json = new JSONObject(result.toString());

                if (!json.getBoolean("success")) {
                    callback.onFailure(json.getString("message"));
                    return;
                }

                User user = new User(
                        json.getString("name"),
                        json.getString("username"),
                        json.getString("email")
                );

                callback.onSuccess(user);

            } catch (Exception e) {
                callback.onFailure(e.getMessage());
            }
        }).start();
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

        new Thread(() -> {
            try {

                String boundary = "----WebKitFormBoundary7MA4YWxkTrZu0gW";
                URL url = new URL("https://wmc.ms.wits.ac.za/students/sgroup2668/upload_profile_pic.php");

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

                DataOutputStream dos = new DataOutputStream(conn.getOutputStream());

                // user id
                dos.writeBytes("--" + boundary + "\r\n");
                dos.writeBytes("Content-Disposition: form-data; name=\"id\"\r\n\r\n");
                dos.writeBytes(String.valueOf(userId) + "\r\n");

                // image
                dos.writeBytes("--" + boundary + "\r\n");
                dos.writeBytes("Content-Disposition: form-data; name=\"image\"; filename=\"profile.jpg\"\r\n");
                dos.writeBytes("Content-Type: image/jpeg\r\n\r\n");
                dos.write(imageBytes);
                dos.writeBytes("\r\n");

                dos.writeBytes("--" + boundary + "--\r\n");
                dos.flush();
                dos.close();

                conn.getResponseCode(); // Trigger request
                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    Log.d(TAG, "Image upload successful");
                } else {
                    Log.e(TAG, "Image upload failed with code: " + responseCode);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    // this geta the image from the database
    public static void getProfileImage(int userId, ImageCallback callback) {
        new Thread(() -> {
            try {
                long timestamp = System.currentTimeMillis();
                URL url = new URL(BASE_URL + "get_profile_pic.php?id=" + userId + "&t=" + timestamp);

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setUseCaches(false); // Disable caching at the connection level

                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder result = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    result.append(line);
                }
                br.close();

                String base64 = result.toString().trim();
                Handler mainHandler = new Handler(Looper.getMainLooper());

                if (base64.isEmpty() || base64.equals("null")) {
                    mainHandler.post(() -> callback.onFailure("No image found"));
                } else {
                    byte[] decoded = Base64.decode(base64, Base64.DEFAULT);
                    mainHandler.post(() -> callback.onSuccess(decoded));
                }
            } catch (Exception e) {
                new Handler(Looper.getMainLooper()).post(() -> callback.onFailure(e.getMessage()));
            }
        }).start();
    }
    public static void deleteAccount(int userId, DeleteCallback callback) {
        if (requestQueue == null) {
            callback.onFailure("RequestQueue not initialized.");
            return;
        }

        String url = BASE_URL + "delete_account.php?id=" + userId;

        StringRequest deleteRequest = new StringRequest(
                Request.Method.GET,
                url,
                response -> {
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        if (jsonResponse.getBoolean("success")) {
                            callback.onSuccess();
                        } else {
                            callback.onFailure(jsonResponse.optString("message", "Failed to delete account"));
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "Delete parse error", e);
                        callback.onFailure("Parse error during deletion");
                    }
                },
                error -> {
                    Log.e(TAG, "Delete network error", error);
                    callback.onFailure("Network error: " + error.getMessage());
                }
        );

        requestQueue.add(deleteRequest);
    }

    public interface ImageCallback {
        void onSuccess(byte[] image);
        void onFailure(String error);
    }
}