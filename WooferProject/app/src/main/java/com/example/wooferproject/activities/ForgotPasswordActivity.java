package com.example.wooferproject.activities;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.wooferproject.R;
import com.example.wooferproject.managers.ForgotPasswordManager;
import com.google.android.material.textfield.TextInputLayout;
import java.util.regex.Pattern;

public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText emailField, otpField, newPasswordField;
    private TextInputLayout passwordInputLayout;
    private Button sendCodeBtn, verifyCodeBtn, resetPasswordBtn;
    private TextView noCodeText;
    private ImageButton returnBtn;
    private ForgotPasswordManager manager;
    private boolean isResend = false;
    private long lastSendTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.forgot_password);

        // We initialize all our screen components and the manager that handles the
        // backend logic for sending codes and resetting passwords.
        emailField = findViewById(R.id.editEmail);
        otpField = findViewById(R.id.editOtp);
        passwordInputLayout = findViewById(R.id.passwordInputLayout);
        newPasswordField = findViewById(R.id.editNewPassword);
        sendCodeBtn = findViewById(R.id.sendCode);
        verifyCodeBtn = findViewById(R.id.verifyCode);
        resetPasswordBtn = findViewById(R.id.resetPassword);
        noCodeText = findViewById(R.id.noCode);
        manager = new ForgotPasswordManager();

        returnBtn = findViewById(R.id.return_btn);

        // this is the return btn (onBackPressed is an android studio function) and takes you to the previous page
        returnBtn.setOnClickListener(v -> onBackPressed());

        // This section links the buttons to their respective logic for sending, 
        // verifying, and finally resetting the password.
        sendCodeBtn.setOnClickListener(v -> handleSendCode());
        verifyCodeBtn.setOnClickListener(v -> handleVerifyCode());
        resetPasswordBtn.setOnClickListener(v -> handleResetPassword());

        noCodeText.setOnClickListener(v -> {
            if (System.currentTimeMillis() - lastSendTime < 60000) {
                long remaining = (60000 - (System.currentTimeMillis() - lastSendTime)) / 1000;
                Toast.makeText(this, "Please wait " + remaining + " seconds", Toast.LENGTH_SHORT).show();
            } else {
                handleSendCode();
            }
        });
    }

    // Phase 1: Handles sending the initial reset code to the user's email.
    private void handleSendCode() {
        String email = emailField.getText().toString().trim();
        if (email.isEmpty()) {
            emailField.setError("Email is required");
            return;
        }

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

    // This block handles the visual countdown for the resend code feature.
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

    // Phase 2: Handles the verification of the 6-digit code received via email.
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
                    Toast.makeText(ForgotPasswordActivity.this, "Code Verified", Toast.LENGTH_SHORT).show();

                    otpField.setVisibility(View.GONE);
                    verifyCodeBtn.setVisibility(View.GONE);
                    noCodeText.setVisibility(View.GONE);

                    passwordInputLayout.setVisibility(View.VISIBLE);
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

    // Phase 3: Handles the final step of updating the user's password in the database.
    private void handleResetPassword() {
        String email = emailField.getText().toString().trim();
        String newPassword = newPasswordField.getText().toString().trim();

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
                    Toast.makeText(ForgotPasswordActivity.this, "Password reset successful", Toast.LENGTH_SHORT).show();
                    finish(); 
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

    // These rules ensure that any new password meets our safety requirements.
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
}
