package com.example.wooferproject.managers;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class PostPageManager {

    public static void createPost(int userId,
                                  String text,
                                  String location,
                                  byte[] imageBytes) {

        new Thread(() -> {
            try {
                String boundary = "----WebKitFormBoundary7MA4YWxkTrZu0gW";

                URL url = new URL(
                        "https://wmc.ms.wits.ac.za/students/sgroup2668/create_post.php"
                );
                HttpURLConnection conn =
                        (HttpURLConnection) url.openConnection();

                conn.setRequestMethod("POST");
                conn.setDoOutput(true);

                conn.setRequestProperty(
                        "Content-Type",
                        "multipart/form-data; boundary=" + boundary
                );

                DataOutputStream dos =
                        new DataOutputStream(conn.getOutputStream());

                // user_id
                dos.writeBytes("--" + boundary + "\r\n");
                dos.writeBytes(
                        "Content-Disposition: form-data; name=\"user_id\"\r\n\r\n"
                );
                dos.writeBytes(userId + "\r\n");

                // text
                dos.writeBytes("--" + boundary + "\r\n");
                dos.writeBytes(
                        "Content-Disposition: form-data; name=\"text\"\r\n\r\n"
                );
                dos.writeBytes(text + "\r\n");

                // location
                dos.writeBytes("--" + boundary + "\r\n");
                dos.writeBytes(
                        "Content-Disposition: form-data; name=\"location\"\r\n\r\n"
                );
                dos.writeBytes(location + "\r\n");

                // image
                if (imageBytes != null) {
                    dos.writeBytes("--" + boundary + "\r\n");
                    dos.writeBytes(
                            "Content-Disposition: form-data; " +
                                    "name=\"image\"; filename=\"post.jpg\"\r\n"
                    );

                    dos.writeBytes(
                            "Content-Type: image/jpeg\r\n\r\n"
                    );
                    dos.write(imageBytes);
                    dos.writeBytes("\r\n");
                }
                dos.writeBytes("--" + boundary + "--\r\n");
                dos.flush();
                dos.close();

                int responseCode = conn.getResponseCode();
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(conn.getInputStream())
                );

                String line;
                StringBuilder response = new StringBuilder();
                while ((line = reader.readLine()) != null) {response.append(line);
                }

                System.out.println("SERVER RESPONSE: " + response.toString());

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}