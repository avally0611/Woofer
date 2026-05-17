package com.example.wooferproject.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.wooferproject.R;
import com.example.wooferproject.managers.LoginManager;

public class LoginActivity extends AppCompatActivity {

    private EditText usernameField, passwordField;
    private Button loginBtn, signUpBtn;
    private TextView forgotPasswordBtn;
    private LoginManager loginManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Before showing the login screen, we check if this user has already logged in 
        // recently. If they have, we just skip this page and send them straight home.
        SharedPreferences prefs = getSharedPreferences("WooferPrefs", MODE_PRIVATE);
        int savedUserId = prefs.getInt("user_id", -1);

        if (savedUserId != -1) {
            Intent intent = new Intent(LoginActivity.this, HomeScreenActivity.class);
            intent.putExtra("user_id", savedUserId);
            startActivity(intent);
            finish();
            return;
        }

        setContentView(R.layout.login_page);

        // Here we link our Java variables to the design elements in the XML layout
        // and initialize our background manager.
        usernameField = findViewById(R.id.enterUsername);
        passwordField = findViewById(R.id.password);
        loginBtn = findViewById(R.id.loginBtn);
        signUpBtn = findViewById(R.id.signUpBtn);
        forgotPasswordBtn = findViewById(R.id.forgot);
        loginManager = new LoginManager();

        // This section tells the app what to do when buttons are tapped. 
        // Each button either starts the login process or navigates to a new screen.
        loginBtn.setOnClickListener(v -> handleLogin());

        signUpBtn.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
            startActivity(intent);
        });

        forgotPasswordBtn.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
            startActivity(intent);
        });
    }

    // This block handles the actual login logic. It validates that the boxes aren't 
    // empty, locks the button to prevent double-taps, and talks to the database.
    private void handleLogin() {
        String username = usernameField.getText().toString().trim();
        String password = passwordField.getText().toString().trim();

        if (username.isEmpty()) {
            usernameField.setError("Username is required");
            usernameField.requestFocus();
            return;
        }
        if (password.isEmpty()) {
            passwordField.setError("Password is required");
            passwordField.requestFocus();
            return;
        }

        loginBtn.setEnabled(false);

        loginManager.login(username, password, new LoginManager.LoginCallback() {
            @Override
            public void onSuccess(int userId) {
                // If the login works, we save their user ID so the app remembers them
                // next time, then we switch them over to the home screen.
                SharedPreferences prefs = getSharedPreferences("WooferPrefs", MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putInt("user_id", userId);
                editor.apply();

                runOnUiThread(() -> {
                    if (isFinishing() || isDestroyed()) return;
                    Toast.makeText(LoginActivity.this, "Welcome back", Toast.LENGTH_SHORT).show();
                    
                    Intent intent = new Intent(LoginActivity.this, HomeScreenActivity.class);
                    intent.putExtra("user_id", userId);
                    startActivity(intent);
                    finish();
                });
            }

            @Override
            public void onFailure(String error) {
                // If login fails, we re-enable the button so they can try again and 
                // show a clear message explaining why it didn't work.
                runOnUiThread(() -> {
                    if (isFinishing() || isDestroyed()) return;
                    loginBtn.setEnabled(true);

                    if (error.toLowerCase().contains("not found") || error.toLowerCase().contains("doesn't exist")) {
                        Toast.makeText(LoginActivity.this, "We couldn't find that account.", Toast.LENGTH_LONG).show();
                        usernameField.setError("User not found");
                    } else {
                        Toast.makeText(LoginActivity.this, "Login Failed: " + error, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
}
