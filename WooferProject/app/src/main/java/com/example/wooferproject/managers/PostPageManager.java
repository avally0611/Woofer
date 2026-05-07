package com.example.wooferproject.managers;

import android.net.Uri;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class PostPageManager {

    public static void createPost(int userId, String text, String location, byte[] imageBytes) {

        new Thread(() -> {
            try {
                URL url = new URL("https://wmc.ms.wits.ac.za/students/sgroup2668/create_post.php");

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);

                String image = "";

                if (imageBytes != null) {
                    image = android.util.Base64.encodeToString(imageBytes, android.util.Base64.NO_WRAP);
                }

                String data =
                        "user_id=" + userId +
                                "&text=" + text +
                                "&location=" + location +
                                "&image=" + image;

                OutputStream os = conn.getOutputStream();
                os.write(data.getBytes());
                os.flush();
                os.close();

                conn.getInputStream();
                //forces a response check
                int responseCode = conn.getResponseCode();
                System.out.println("Response code: " + responseCode);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}