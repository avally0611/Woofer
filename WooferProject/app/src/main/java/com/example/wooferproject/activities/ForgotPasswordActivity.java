package com.example.wooferproject.activities;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.wooferproject.R;
import com.example.wooferproject.managers.ForgotPasswordManager;
import java.util.regex.Pattern;

public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText emailField, otpField, newPasswordField;
    private Button sendCodeBtn, verifyCodeBtn, resetPasswordBtn;
    private TextView noCodeText;
    private ForgotPasswordManager manager;
    private boolean isResend = false;
    private long lastSendTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.forgot_password);

        // Initialize UI components
        emailField = findViewById(R.id.editEmail);
        otpField = findViewById(R.id.editOtp);
        newPasswordField = findViewById(R.id.editNewPassword);
        sendCodeBtn = findViewById(R.id.sendCode);
        verifyCodeBtn = findViewById(R.id.verifyCode);
        resetPasswordBtn = findViewById(R.id.resetPassword);
        noCodeText = findViewById(R.id.noCode);

        manager = new ForgotPasswordManager();

        // Step 1: Send the code
        sendCodeBtn.setOnClickListener(v -> handleSendCode());

        // Step 2: Verify the code
        verifyCodeBtn.setOnClickListener(v -> handleVerifyCode());

        // Step 3: Reset the password
        resetPasswordBtn.setOnClickListener(v -> handleResetPassword());

        // Resend logic
        noCodeText.setOnClickListener(v -> {
            if (System.currentTimeMillis() - lastSendTime < 60000) {
                long remaining = (60000 - (System.currentTimeMillis() - lastSendTime)) / 1000;
                Toast.makeText(this, "Please wait " + remaining + " seconds", Toast.LENGTH_SHORT).show();
            } else {
                handleSendCode();
            }
        });
    }

    private void handleSendCode() {
        String email = emailField.getText().toString().trim();
        if (email.isEmpty()) {
            emailField.setError("Email is required");
            return;
        }

        // Check if this is a resend
        isResend = (otpField.getVisibility() == View.VISIBLE);

        sendCodeBtn.setEnabled(false);
        sendCodeBtn.setText("Sending...");
        noCodeText.setEnabled(false);

        manager.sendOtp(email, new ForgotPasswordManager.ForgotPasswordCallback() {
            @Override
            public void onSuccess(String message) {
                runOnUiThread(() -> {
                    if (isFinishing()) return;
                    
                    lastSendTime = System.currentTimeMillis();
                    startResendTimer();

                    Toast.makeText(ForgotPasswordActivity.this, 
                        isResend ? "A new code has been sent" : "Code sent to your email", 
                        Toast.LENGTH_SHORT).show();
                    
                    // TRANSITION: Show OTP verification UI, hide Email UI
                    emailField.setVisibility(View.GONE);
                    sendCodeBtn.setVisibility(View.GONE);
                    
                    otpField.setVisibility(View.VISIBLE);
                    verifyCodeBtn.setVisibility(View.VISIBLE);
                    noCodeText.setVisibility(View.VISIBLE);
                });
            }

            @Override
            public void onFailure(String error) {
                runOnUiThread(() -> {
                    if (isFinishing()) return;
                    sendCodeBtn.setEnabled(true);
                    sendCodeBtn.setText(isResend ? "Resend Code" : "Send Code");
                    noCodeText.setEnabled(true);
                    Toast.makeText(ForgotPasswordActivity.this, "Error: " + error, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void startResendTimer() {
        noCodeText.setEnabled(false);
        new CountDownTimer(60000, 1000) {
            public void onTick(long millisUntilFinished) {
                noCodeText.setText("Resend code in " + millisUntilFinished / 1000 + "s");
            }

            public void onFinish() {
                noCodeText.setText("Didn't receive a code?");
                noCodeText.setEnabled(true);
            }
        }.start();
    }

    private void handleVerifyCode() {
        String email = emailField.getText().toString().trim();
        String otp = otpField.getText().toString().trim();

        if (otp.isEmpty()) {
            otpField.setError("Enter OTP");
            return;
        }

        verifyCodeBtn.setEnabled(false);
        verifyCodeBtn.setText("Verifying...");

        manager.verifyOtp(email, otp, new ForgotPasswordManager.ForgotPasswordCallback() {
            @Override
            public void onSuccess(String message) {
                runOnUiThread(() -> {
                    if (isFinishing()) return;
                    Toast.makeText(ForgotPasswordActivity.this, "Code Verified!", Toast.LENGTH_SHORT).show();

                    // TRANSITION: Show New Password UI, hide OTP UI
                    otpField.setVisibility(View.GONE);
                    verifyCodeBtn.setVisibility(View.GONE);
                    noCodeText.setVisibility(View.GONE);

                    newPasswordField.setVisibility(View.VISIBLE);
                    resetPasswordBtn.setVisibility(View.VISIBLE);
                });
            }

            @Override
            public void onFailure(String error) {
                runOnUiThread(() -> {
                    if (isFinishing()) return;
                    verifyCodeBtn.setEnabled(true);
                    verifyCodeBtn.setText("Verify Code");
                    otpField.setError("Invalid or expired code");
                    Toast.makeText(ForgotPasswordActivity.this, error, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void handleResetPassword() {
        String email = emailField.getText().toString().trim();
        String newPassword = newPasswordField.getText().toString().trim();

        // Applying the password guide used when signing up
        String passwordError = validatePassword(newPassword);
        if (passwordError != null) {
            newPasswordField.setError(passwordError);
            return;
        }

        resetPasswordBtn.setEnabled(false);
        resetPasswordBtn.setText("Updating...");

        manager.resetPassword(email, newPassword, new ForgotPasswordManager.ForgotPasswordCallback() {
            @Override
            public void onSuccess(String message) {
                runOnUiThread(() -> {
                    if (isFinishing()) return;
                    Toast.makeText(ForgotPasswordActivity.this, "Password reset successful!", Toast.LENGTH_SHORT).show();
                    finish(); // Return to Login
                });
            }

            @Override
            public void onFailure(String error) {
                runOnUiThread(() -> {
                    if (isFinishing()) return;
                    resetPasswordBtn.setEnabled(true);
                    resetPasswordBtn.setText("Reset Password");
                    Toast.makeText(ForgotPasswordActivity.this, "Reset failed: " + error, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    /**
     * Validates password based on requirements used in SignUpActivity:
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
}
