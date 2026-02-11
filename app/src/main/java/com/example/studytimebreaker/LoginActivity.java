package com.example.studytimebreaker;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

/**
 * Login screen - allows existing users to log in.
 * After successful login, user info is saved in SharedPreferences
 * and the user is redirected to the Timer screen.
 */
public class LoginActivity extends AppCompatActivity {

    private TextInputEditText etUsername, etPassword;
    private MaterialButton btnLogin;
    private TextView tvRegister;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize views
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvRegister = findViewById(R.id.tvRegister);
        dbHelper = new DatabaseHelper(this);

        // Login button click
        btnLogin.setOnClickListener(v -> loginUser());

        // "Register" text click - go to registration screen
        tvRegister.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class)));
    }

    /**
     * Validate inputs and authenticate the user.
     * On success: save login state and go to TimerActivity.
     * On failure: show error message.
     */
    private void loginUser() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // Basic input validation
        if (username.isEmpty()) {
            etUsername.setError("Username is required");
            etUsername.requestFocus();
            return;
        }
        if (password.isEmpty()) {
            etPassword.setError("Password is required");
            etPassword.requestFocus();
            return;
        }

        // Check credentials against database
        int userId = dbHelper.loginUser(username, password);

        if (userId != -1) {
            // Login successful - save user info in SharedPreferences
            SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
            prefs.edit()
                    .putBoolean("is_logged_in", true)
                    .putInt("user_id", userId)
                    .putString("username", username)
                    .apply();

            Toast.makeText(this, "Welcome, " + username + "!", Toast.LENGTH_SHORT).show();

            // Go to timer screen
            Intent intent = new Intent(this, TimerActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Invalid username or password", Toast.LENGTH_SHORT).show();
        }
    }
}
