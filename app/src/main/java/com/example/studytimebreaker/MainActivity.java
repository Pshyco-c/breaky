package com.example.studytimebreaker;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

/**
 * Splash/Redirect Activity - the app's entry point.
 * Checks if a user is already logged in:
 *   - If YES → go to TimerActivity (main screen)
 *   - If NO  → go to LoginActivity
 * Also applies the saved theme preference (light/dark mode).
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Apply saved theme preference before creating the activity
        SharedPreferences themePrefs = getSharedPreferences("theme_prefs", MODE_PRIVATE);
        boolean isDarkMode = themePrefs.getBoolean("dark_mode", false);
        AppCompatDelegate.setDefaultNightMode(
                isDarkMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);

        super.onCreate(savedInstanceState);

        // Check if user is already logged in
        SharedPreferences userPrefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        boolean isLoggedIn = userPrefs.getBoolean("is_logged_in", false);

        if (isLoggedIn) {
            // User is logged in → go to Timer screen
            startActivity(new Intent(this, TimerActivity.class));
        } else {
            // User is not logged in → go to Login screen
            startActivity(new Intent(this, LoginActivity.class));
        }

        finish(); // Close this activity so user can't go back to it
    }
}