package com.example.wooferproject.managers;

import android.util.Base64;

import com.example.wooferproject.models.User;

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

    // UPDATE PROFILE
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

    // GET PROFILE
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

                OutputStream os = conn.getOutputStream();
                DataOutputStream dos = new DataOutputStream(os);

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

                int code = conn.getResponseCode();
                System.out.println("HTTP CODE: " + code);

                BufferedReader br = new BufferedReader(
                        new InputStreamReader(conn.getInputStream())
                );

                String line;
                StringBuilder response = new StringBuilder();

                while ((line = br.readLine()) != null) {
                    response.append(line);
                }

                System.out.println(response.toString());

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
    // this geta the image from the database
    public static void getProfileImage(int userId, ImageCallback callback) {

        new Thread(() -> {
            try {
                URL url = new URL("https://wmc.ms.wits.ac.za/students/sgroup2668/get_profile_pic.php?id=" + userId);

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                BufferedReader br = new BufferedReader(
                        new InputStreamReader(conn.getInputStream())
                );

                String line;
                StringBuilder result = new StringBuilder();

                while ((line = br.readLine()) != null) {
                    result.append(line);
                }

                String base64 = result.toString().trim();

                if (base64.isEmpty()) {
                    callback.onFailure("No image found");
                    return;
                }

                byte[] decoded = Base64.decode(base64, Base64.DEFAULT);

                callback.onSuccess(decoded);

            } catch (Exception e) {
                callback.onFailure(e.getMessage());
            }
        }).start();
    }

    public interface ImageCallback {
        void onSuccess(byte[] image);
        void onFailure(String error);
    }
}