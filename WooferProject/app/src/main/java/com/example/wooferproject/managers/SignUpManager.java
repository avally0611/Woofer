package com.example.wooferproject.managers;

import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class SignUpManager {

    public interface SignUpCallback {
        void onSuccess(String message, int userId);
        void onFailure(String error);
    }

    public static void register(String firstName, String lastName, String username, String email, String password, SignUpCallback callback) {
        new Thread(() -> {
            try {
                // Connect to the registration script
                URL url = new URL("https://wmc.ms.wits.ac.za/students/sgroup2668/signup.php");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);

                // Prepare the data to be sent
                String data = "name=" + firstName +
                        "&lastname=" + lastName +
                        "&username=" + username +
                        "&email=" + email +
                        "&password=" + password;

                // Write the data to the output stream
                OutputStream os = conn.getOutputStream();
                os.write(data.getBytes());
                os.flush();
                os.close();

                // Read the response from the server
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder result = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    result.append(line);
                }

                // Parse the JSON response
                JSONObject json = new JSONObject(result.toString());
                if (json.getBoolean("success")) {
                    int userId = json.optInt("user_id", -1);
                    callback.onSuccess(json.getString("message"), userId);
                } else {
                    callback.onFailure(json.getString("message"));
                }

            } catch (Exception e) {
                callback.onFailure(e.getMessage());
            }
        }).start();
    }
}
