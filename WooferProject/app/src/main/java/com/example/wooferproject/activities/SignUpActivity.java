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

        // We start by finding all our UI components and setting up the manager 
        // that will handle the registration request in the background.
        nameField = findViewById(R.id.Name);
        lastNameField = findViewById(R.id.LastName);
        usernameField = findViewById(R.id.username);
        emailField = findViewById(R.id.EmailAddress);
        passwordField = findViewById(R.id.Password);
        passwordCheckText = findViewById(R.id.PasswordCheck);
        signUpBtn = findViewById(R.id.SignUpBtn);
        loginLink = findViewById(R.id.loginLink);
        signUpManager = new SignUpManager();

        // This section sets up interactive parts of the page, like the real-time 
        // password check and navigation back to the login screen.
        setupPasswordWatcher();

        signUpBtn.setOnClickListener(v -> handleSignUp());

        loginLink.setOnClickListener(v -> {
            Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
            startActivity(intent);
        });
    }

    // This block monitors the password field as the user types, giving them instant 
    // feedback if their password meets our safety standards.
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

    // This section gathers all the user's input, runs a final check on the data format, 
    // and then attempts to create the new account on our server.
    private void handleSignUp() {
        String firstName = nameField.getText().toString().trim();
        String lastName = lastNameField.getText().toString().trim();
        String username = usernameField.getText().toString().trim();
        String email = emailField.getText().toString().trim();
        String password = passwordField.getText().toString().trim();

        if (firstName.isEmpty() || lastName.isEmpty() || username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isValidEmail(email)) {
            emailField.setError("Please enter a valid email address");
            emailField.requestFocus();
            return;
        }

        String passwordError = validatePassword(password);
        if (passwordError != null) {
            passwordCheckText.setText(passwordError);
            passwordCheckText.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark));
            return;
        }

        signUpManager.register(firstName, lastName, username, email, password, new SignUpManager.SignUpCallback() {
            @Override
            public void onSuccess(String message, int userId) {
                // If the account is successfully created, we save their login session 
                // and welcome them to the home screen.
                SharedPreferences prefs = getSharedPreferences("WooferPrefs", MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putInt("user_id", userId);
                editor.apply();

                runOnUiThread(() -> {
                    if (isFinishing()) return;
                    Toast.makeText(SignUpActivity.this, "Welcome to Woofer", Toast.LENGTH_LONG).show();
                    
                    Intent intent = new Intent(SignUpActivity.this, HomeScreenActivity.class);
                    intent.putExtra("user_id", userId);
                    startActivity(intent);
                    finish();
                });
            }

            @Override
            public void onFailure(String error) {
                // If something goes wrong, we show a helpful error message so the user 
                // knows if they need to change their email or username.
                runOnUiThread(() -> {
                    if (isFinishing()) return;

                    if (error.toLowerCase().contains("email already exists")) {
                        Toast.makeText(SignUpActivity.this, "That email is already in use.", Toast.LENGTH_LONG).show();
                        emailField.setError("Email already in use");
                    } else {
                        Toast.makeText(SignUpActivity.this, "Error: " + error, Toast.LENGTH_LONG).show();
                        if (error.toLowerCase().contains("username")) {
                            usernameField.setError("That username is already taken");
                        }
                    }
                });
            }
        });
    }

    // These are the specific rules we use to keep passwords safe.
    private String validatePassword(String password) {
        if (password.length() < 8) {
            return "Password must be at least 8 characters long";
        }
        if (!Pattern.compile("[A-Z]").matcher(password).find()) {
            return "Needs at least one uppercase letter";
        }
        if (!Pattern.compile("[0-9]").matcher(password).find()) {
            return "Needs at least one digit";
        }
        if (!Pattern.compile("[!@#$%^&*(),.?\":{}|<>]").matcher(password).find()) {
            return "Needs at least one special character";
        }
        return null;
    }

    private boolean isValidEmail(String email) {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }
}
