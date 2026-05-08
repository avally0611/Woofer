package com.example.wooferproject.managers;

import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class LoginManager {

    public interface LoginCallback {
        void onSuccess(int userId);
        void onFailure(String error);
    }

    public static void login(String username, String password, LoginCallback callback) {
        new Thread(() -> {
            try {
                URL url = new URL("https://wmc.ms.wits.ac.za/students/sgroup2668/login.php");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);

                String data = "username=" + username + "&password=" + password;

                OutputStream os = conn.getOutputStream();
                os.write(data.getBytes());
                os.flush();
                os.close();

                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder result = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    result.append(line);
                }

                JSONObject json = new JSONObject(result.toString());
                if (json.getBoolean("success")) {
                    int userId = json.getInt("user_id");
                    callback.onSuccess(userId);
                } else {
                    callback.onFailure(json.getString("message"));
                }

            } catch (Exception e) {
                callback.onFailure(e.getMessage());
            }
        }).start();
    }
}
