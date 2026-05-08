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

    // Declare UI components for input and interaction
    private EditText usernameField, passwordField;
    private Button loginBtn, signUpBtn;
    private TextView forgotPasswordBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check if user is already logged in
        SharedPreferences prefs = getSharedPreferences("WooferPrefs", MODE_PRIVATE);
        int savedUserId = prefs.getInt("user_id", -1);

        if (savedUserId != -1) {
            // User is already logged in, skip login page and go to Home Screen
            Intent intent = new Intent(LoginActivity.this, HomeScreenActivity.class);
            intent.putExtra("user_id", savedUserId);
            startActivity(intent);
            finish();
            return;
        }

        // Set the layout for this activity using the login_page XML
        setContentView(R.layout.login_page);

        // Link the Java UI components to their IDs defined in the XML layout
        usernameField = findViewById(R.id.enterUsername);
        passwordField = findViewById(R.id.password);
        loginBtn = findViewById(R.id.loginBtn);
        signUpBtn = findViewById(R.id.signUpBtn);
        forgotPasswordBtn = findViewById(R.id.forgot);

        // When login button is clicked, trigger the handleLogin logic
        loginBtn.setOnClickListener(v -> handleLogin());

        // When sign up button is clicked, open the SignUpActivity screen
        signUpBtn.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
            startActivity(intent);
        });

        // When forgot password is clicked, open the ForgotPasswordActivity screen
        forgotPasswordBtn.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
            startActivity(intent);
        });

    }

    /**
     * Captures user input and communicates with LoginManager to verify credentials against the database.
     */
    private void handleLogin() {
        // Extract text from the input fields and remove extra spaces
        String username = usernameField.getText().toString().trim();
        String password = passwordField.getText().toString().trim();

        // Better Field Validation UI
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

        // Prevent Double-Clicking
        loginBtn.setEnabled(false);

        // Send the credentials to the LoginManager to perform the database query
        LoginManager.login(username, password, new LoginManager.LoginCallback() {
            @Override
            public void onSuccess(int userId) {
                // Save login state in SharedPreferences
                SharedPreferences prefs = getSharedPreferences("WooferPrefs", MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putInt("user_id", userId);
                editor.apply();

                // Since network tasks run on background threads, update the UI on the main thread
                runOnUiThread(() -> {
                    //s Activity Lifecycle Safety Check
                    if (isFinishing() || isDestroyed()) return;

                    Toast.makeText(LoginActivity.this, "Login Successful!", Toast.LENGTH_SHORT).show();
                    
                    // Create an intent to navigate to the Home Screen
                    Intent intent = new Intent(LoginActivity.this, HomeScreenActivity.class);
                    // Pass the unique user_id to the next activity for context (e.g., loading their posts)
                    intent.putExtra("user_id", userId);
                    startActivity(intent);
                    
                    // finish() removes LoginActivity from the stack so the back button won't return here
                    finish(); 
                });
            }

            @Override
            public void onFailure(String error) {
                // Show the error message (e.g., "Invalid username or password") if the query fails
                runOnUiThread(() -> {
                    // Activity Lifecycle Safety Check
                    if (isFinishing() || isDestroyed()) return;

                    // Re-enable button on failure
                    loginBtn.setEnabled(true);

                    // Check if the error indicates that the login doesn't exist
                    if (error.toLowerCase().contains("not found") || error.toLowerCase().contains("doesn't exist") || error.toLowerCase().contains("invalid username")) {
                        Toast.makeText(LoginActivity.this, "This login does not exist in the database.", Toast.LENGTH_LONG).show();
                        usernameField.setError("Login not found");
                    } else {
                        Toast.makeText(LoginActivity.this, "Login Failed: " + error, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
}
