package com.example.wooferproject.managers;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class ProfilePageManager {

    public static void updateProfile(int userId, String name, String username, String email) {

        new Thread(() -> {
            try {
                URL url = new URL("http://YOUR_IP/updateProfile.php");

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);

                String data = "id=" + userId +
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
}