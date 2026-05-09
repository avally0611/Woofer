package com.example.wooferproject.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.wooferproject.R;
import com.example.wooferproject.managers.SignUpManager;

import java.util.regex.Pattern;

public class SignUpActivity extends AppCompatActivity {

    private EditText nameField, lastNameField, usernameField, emailField, passwordField;
    private TextView passwordCheckText, loginLink;
    private Button signUpBtn;
    private SignUpManager signUpManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_up);

        // Initialize UI components
        nameField = findViewById(R.id.Name);
        lastNameField = findViewById(R.id.LastName);
        usernameField = findViewById(R.id.username);
        emailField = findViewById(R.id.EmailAddress);
        passwordField = findViewById(R.id.Password);
        passwordCheckText = findViewById(R.id.PasswordCheck);
        signUpBtn = findViewById(R.id.SignUpBtn);
        loginLink = findViewById(R.id.loginLink);

        signUpManager = new SignUpManager();

        // Provide real-time feedback for password as the user types
        setupPasswordWatcher();

        // Set up button listener for Sign Up
        signUpBtn.setOnClickListener(v -> handleSignUp());

        // Redirect to Login page if they already have an account
        loginLink.setOnClickListener(v -> {
            Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
            startActivity(intent);
        });
    }

    /**
     * Listens for text changes in the password field to provide instant validation feedback.
     */
    private void setupPasswordWatcher() {
        passwordField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String password = s.toString();
                if (password.isEmpty()) {
                    passwordCheckText.setText("");
                    return;
                }
                String error = validatePassword(password);
                if (error != null) {
                    passwordCheckText.setText(error);
                    passwordCheckText.setTextColor(ContextCompat.getColor(SignUpActivity.this, android.R.color.holo_red_dark));
                } else {
                    passwordCheckText.setText("Password valid");
                    passwordCheckText.setTextColor(ContextCompat.getColor(SignUpActivity.this, android.R.color.holo_green_dark));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void handleSignUp() {
        String firstName = nameField.getText().toString().trim();
        String lastName = lastNameField.getText().toString().trim();
        String username = usernameField.getText().toString().trim();
        String email = emailField.getText().toString().trim();
        String password = passwordField.getText().toString().trim();

        // Check if any fields are empty
        if (firstName.isEmpty() || lastName.isEmpty() || username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate email format
        if (!isValidEmail(email)) {
            emailField.setError("Please enter a valid email address");
            emailField.requestFocus();
            return;
        }

        // Final check on password requirements before submission
        String passwordError = validatePassword(password);
        if (passwordError != null) {
            passwordCheckText.setText(passwordError);
            passwordCheckText.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark));
            return;
        }

        // Call the manager to register the user
        signUpManager.register(firstName, lastName, username, email, password, new SignUpManager.SignUpCallback() {
            @Override
            public void onSuccess(String message, int userId) {
                // Save login state in SharedPreferences for session management
                SharedPreferences prefs = getSharedPreferences("WooferPrefs", MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putInt("user_id", userId);
                editor.apply();

                runOnUiThread(() -> {
                    // Prevention of memory leaks and crashes if the activity was closed
                    if (isFinishing()) return;

                    Toast.makeText(SignUpActivity.this, "Registration Successful!", Toast.LENGTH_LONG).show();
                    
                    // After successful signup, navigate straight to the Home Screen
                    Intent intent = new Intent(SignUpActivity.this, HomeScreenActivity.class);
                    intent.putExtra("user_id", userId);
                    startActivity(intent);
                    finish();
                });
            }

            @Override
            public void onFailure(String error) {
                runOnUiThread(() -> {
                    if (isFinishing()) return;

                    // Specific handling for existing email/username errors
                    if (error.toLowerCase().contains("email already exists")) {
                        Toast.makeText(SignUpActivity.this, "This email already exists, please login", Toast.LENGTH_LONG).show();
                        emailField.setError("Email already in use");
                    } else {
                        Toast.makeText(SignUpActivity.this, "Registration Failed: " + error, Toast.LENGTH_LONG).show();
                        if (error.toLowerCase().contains("username")) {
                            usernameField.setError("Username already taken");
                        }
                    }
                });
            }
        });
    }

    /**
     * Validates password based on:
     * - Min 8 characters
     * - At least one uppercase letter
     * - At least one digit
     * - At least one special character
     */
    private String validatePassword(String password) {
        if (password.length() < 8) {
            return "Password must be at least 8 characters long";
        }
        if (!Pattern.compile("[A-Z]").matcher(password).find()) {
            return "Password must contain at least one uppercase letter";
        }
        if (!Pattern.compile("[0-9]").matcher(password).find()) {
            return "Password must contain at least one digit";
        }
        if (!Pattern.compile("[!@#$%^&*(),.?\":{}|<>]").matcher(password).find()) {
            return "Password must contain at least one special character";
        }
        return null; // No errors
    }

    /**
     * Helper method to check if an email string matches the standard email format
     */
    private boolean isValidEmail(String email) {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }
}
