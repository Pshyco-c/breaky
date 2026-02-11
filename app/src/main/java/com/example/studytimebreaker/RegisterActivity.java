package com.example.studytimebreaker;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

/**
 * Registration screen - allows new users to create an account.
 * Validates all inputs and checks for duplicate username/email.
 * Passwords are hashed using SHA-256 before storing in the database.
 */
public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText etUsername, etEmail, etPassword, etConfirmPassword;
    private MaterialButton btnRegister;
    private TextView tvLogin;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialize views
        etUsername = findViewById(R.id.etUsername);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnRegister = findViewById(R.id.btnRegister);
        tvLogin = findViewById(R.id.tvLogin);
        dbHelper = new DatabaseHelper(this);

        // Register button click
        btnRegister.setOnClickListener(v -> registerUser());

        // "Login" text click - go back to login screen
        tvLogin.setOnClickListener(v -> finish());
    }

    /**
     * Validate all inputs and register the user.
     * Checks: empty fields, username length, email format,
     * password length, password match, and duplicate username/email.
     */
    private void registerUser() {
        String username = etUsername.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        // Input validation
        if (username.isEmpty()) {
            etUsername.setError("Username is required");
            etUsername.requestFocus();
            return;
        }
        if (username.length() < 3) {
            etUsername.setError("Username must be at least 3 characters");
            etUsername.requestFocus();
            return;
        }
        if (email.isEmpty()) {
            etEmail.setError("Email is required");
            etEmail.requestFocus();
            return;
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Enter a valid email address");
            etEmail.requestFocus();
            return;
        }
        if (password.isEmpty()) {
            etPassword.setError("Password is required");
            etPassword.requestFocus();
            return;
        }
        if (password.length() < 6) {
            etPassword.setError("Password must be at least 6 characters");
            etPassword.requestFocus();
            return;
        }
        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Passwords do not match");
            etConfirmPassword.requestFocus();
            return;
        }

        // Check for duplicate username
        if (dbHelper.isUsernameExists(username)) {
            etUsername.setError("Username already taken");
            etUsername.requestFocus();
            return;
        }

        // Check for duplicate email
        if (dbHelper.isEmailExists(email)) {
            etEmail.setError("Email already registered");
            etEmail.requestFocus();
            return;
        }

        // All validation passed - register the user
        long result = dbHelper.registerUser(username, email, password);
        if (result != -1) {
            Toast.makeText(this, "Registration successful! Please login.", Toast.LENGTH_SHORT).show();
            finish(); // Go back to login screen
        } else {
            Toast.makeText(this, "Registration failed. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }
}
