package com.example.studytimebreaker;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * MAIN TIMER SCREEN - The core feature of Breaky.
 *
 * How the timer works:
 * 1. User enters a subject name (e.g., "Mathematics")
 * 2. Clicks "Start Studying" → study timer starts counting up
 * 3. User can "Take Break" → study timer pauses, break timer starts
 * 4. User clicks "Resume Study" → break ends, study timer resumes
 * 5. User clicks "Stop" → session is saved to database
 * 6. A summary dialog shows total study time, break time, and breaks taken
 *
 * Timer uses a Handler that runs a Runnable every 1 second.
 * State is preserved on screen rotation using onSaveInstanceState.
 */
public class TimerActivity extends AppCompatActivity {

    // ==================== UI ELEMENTS ====================
    private TextView tvWelcome, tvTimerDisplay, tvTimerLabel;
    private TextView tvBreakTime, tvBreaksTaken, tvStatus;
    private EditText etSubject;
    private MaterialButton btnStartPause, btnBreak, btnStop;
    private MaterialCardView timerCard;
    private ImageView btnThemeToggle, btnLogout;
    private BottomNavigationView bottomNav;

    // ==================== TIMER VARIABLES ====================
    private Handler timerHandler = new Handler(Looper.getMainLooper());
    private long studySeconds = 0;      // Total study time in seconds
    private long breakSeconds = 0;      // Total break time in seconds
    private int breaksTaken = 0;        // Number of breaks taken
    private boolean isRunning = false;  // Is timer currently running?
    private boolean isOnBreak = false;  // Is user currently on a break?
    private boolean hasStarted = false; // Has the session been started?

    // ==================== USER & DATABASE ====================
    private int userId;
    private String username;
    private DatabaseHelper dbHelper;

    /**
     * This Runnable runs every 1 second while the timer is active.
     * It increments either the study or break counter and updates the display.
     */
    private Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            if (isOnBreak) {
                breakSeconds++;
                tvBreakTime.setText("Break Time: " + StudySession.formatTimer(breakSeconds));
            } else {
                studySeconds++;
                tvTimerDisplay.setText(StudySession.formatTimer(studySeconds));
            }
            // Schedule next tick after 1 second (1000 milliseconds)
            timerHandler.postDelayed(this, 1000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timer);

        // Get logged-in user info from SharedPreferences
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        userId = prefs.getInt("user_id", -1);
        username = prefs.getString("username", "Student");

        dbHelper = new DatabaseHelper(this);

        // Initialize all UI elements and set up click listeners
        initViews();
        setupListeners();

        // Restore timer state if screen was rotated
        if (savedInstanceState != null) {
            restoreTimerState(savedInstanceState);
        }

        // Set initial button states
        updateUI();
    }

    /** Find and assign all UI element references */
    private void initViews() {
        tvWelcome = findViewById(R.id.tvWelcome);
        tvTimerDisplay = findViewById(R.id.tvTimerDisplay);
        tvTimerLabel = findViewById(R.id.tvTimerLabel);
        tvBreakTime = findViewById(R.id.tvBreakTime);
        tvBreaksTaken = findViewById(R.id.tvBreaksTaken);
        tvStatus = findViewById(R.id.tvStatus);
        etSubject = findViewById(R.id.etSubject);
        btnStartPause = findViewById(R.id.btnStartPause);
        btnBreak = findViewById(R.id.btnBreak);
        btnStop = findViewById(R.id.btnStop);
        timerCard = findViewById(R.id.timerCard);
        btnThemeToggle = findViewById(R.id.btnThemeToggle);
        btnLogout = findViewById(R.id.btnLogout);
        bottomNav = findViewById(R.id.bottomNav);

        // Display welcome message with user's name
        tvWelcome.setText("Hello, " + username + "!");

        // Set theme toggle icon based on current mode
        updateThemeIcon();
    }

    /** Set up click listeners for all interactive elements */
    private void setupListeners() {
        // Start/Pause button - starts new session, pauses, or resumes
        btnStartPause.setOnClickListener(v -> {
            if (!hasStarted) {
                startSession();
            } else if (isRunning) {
                pauseTimer();
            } else {
                resumeTimer();
            }
        });

        // Break button - toggles between taking a break and resuming study
        btnBreak.setOnClickListener(v -> {
            if (isRunning && !isOnBreak) {
                startBreak();
            } else if (isOnBreak) {
                endBreak();
            }
        });

        // Stop button - ends the session and saves to database
        btnStop.setOnClickListener(v -> stopSession());

        // Theme toggle - switches between light and dark mode
        btnThemeToggle.setOnClickListener(v -> toggleTheme());

        // Logout button
        btnLogout.setOnClickListener(v -> logout());

        // Bottom navigation - switch between Timer and History screens
        bottomNav.setSelectedItemId(R.id.nav_timer);
        bottomNav.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_history) {
                startActivity(new Intent(this, SessionHistoryActivity.class));
                return true;
            }
            return item.getItemId() == R.id.nav_timer;
        });
    }

    // ==================== TIMER CONTROL METHODS ====================

    /** Start a new study session */
    private void startSession() {
        String subject = etSubject.getText().toString().trim();

        // Validate subject input
        if (subject.isEmpty()) {
            etSubject.setError("Please enter a subject to study");
            etSubject.requestFocus();
            return;
        }

        // Reset all counters for a fresh session
        studySeconds = 0;
        breakSeconds = 0;
        breaksTaken = 0;
        hasStarted = true;
        isRunning = true;
        isOnBreak = false;

        // Lock the subject field while studying
        etSubject.setEnabled(false);

        // Start the timer
        timerHandler.post(timerRunnable);

        tvStatus.setText("Studying...");
        updateUI();
    }

    /** Pause the study timer */
    private void pauseTimer() {
        isRunning = false;
        timerHandler.removeCallbacks(timerRunnable);
        tvStatus.setText("Paused");
        updateUI();
    }

    /** Resume the study timer after a pause */
    private void resumeTimer() {
        isRunning = true;
        timerHandler.post(timerRunnable);
        tvStatus.setText("Studying...");
        updateUI();
    }

    /** Start a break - pause studying, begin counting break time */
    private void startBreak() {
        isOnBreak = true;
        breaksTaken++;
        tvBreaksTaken.setText("Breaks Taken: " + breaksTaken);
        tvStatus.setText("On Break");
        tvTimerLabel.setText("Study Time (paused)");
        updateUI();
    }

    /** End break - resume studying */
    private void endBreak() {
        isOnBreak = false;
        tvStatus.setText("Studying...");
        tvTimerLabel.setText("Study Time");
        updateUI();
    }

    /** Stop the session - save to database and show summary */
    private void stopSession() {
        if (!hasStarted) return;

        // Stop the timer
        timerHandler.removeCallbacks(timerRunnable);

        // Only save if the user actually studied for at least 1 second
        if (studySeconds > 0) {
            // Get current date and time for the session record
            String date = new SimpleDateFormat("MMM dd, yyyy - HH:mm",
                    Locale.getDefault()).format(new Date());

            // Create a StudySession object with all the session data
            StudySession session = new StudySession(
                    userId,
                    etSubject.getText().toString().trim(),
                    studySeconds,
                    breakSeconds,
                    breaksTaken,
                    date
            );

            // Save the session to the SQLite database
            long result = dbHelper.addSession(session);

            if (result != -1) {
                showSessionSummary(session);
            } else {
                Toast.makeText(this, "Failed to save session", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Session too short to save", Toast.LENGTH_SHORT).show();
        }

        // Reset the timer to initial state
        resetTimer();
    }

    /** Show a dialog summarizing the completed session */
    private void showSessionSummary(StudySession session) {
        new AlertDialog.Builder(this)
                .setTitle("Session Complete!")
                .setMessage("Subject: " + session.getSubject()
                        + "\n\nStudy Time: " + StudySession.formatDuration(session.getStudyDuration())
                        + "\nBreak Time: " + StudySession.formatDuration(session.getBreakDuration())
                        + "\nBreaks Taken: " + session.getBreaksTaken())
                .setPositiveButton("Great!", null)
                .setNeutralButton("View History", (d, w) -> {
                    startActivity(new Intent(this, SessionHistoryActivity.class));
                })
                .show();
    }

    /** Reset all timer variables and UI to initial state */
    private void resetTimer() {
        hasStarted = false;
        isRunning = false;
        isOnBreak = false;
        studySeconds = 0;
        breakSeconds = 0;
        breaksTaken = 0;

        tvTimerDisplay.setText("00:00:00");
        tvBreakTime.setText("Break Time: 00:00:00");
        tvBreaksTaken.setText("Breaks Taken: 0");
        tvStatus.setText("Ready to study?");
        tvTimerLabel.setText("Study Time");
        etSubject.setText("");
        etSubject.setEnabled(true);

        updateUI();
    }

    // ==================== UI UPDATE ====================

    /** Update button text and enabled state based on current timer state */
    private void updateUI() {
        if (!hasStarted) {
            // Initial state - show Start button
            btnStartPause.setText("Start Studying");
            btnStartPause.setEnabled(true);
            btnBreak.setText("Take Break");
            btnBreak.setEnabled(false);
            btnStop.setEnabled(false);
        } else if (isOnBreak) {
            // On break - show Resume Study on break button
            btnStartPause.setEnabled(false);
            btnBreak.setText("Resume Study");
            btnBreak.setEnabled(true);
            btnStop.setEnabled(true);
        } else if (isRunning) {
            // Timer running - show Pause
            btnStartPause.setText("Pause");
            btnStartPause.setEnabled(true);
            btnBreak.setText("Take Break");
            btnBreak.setEnabled(true);
            btnStop.setEnabled(true);
        } else {
            // Paused - show Resume
            btnStartPause.setText("Resume");
            btnStartPause.setEnabled(true);
            btnBreak.setText("Take Break");
            btnBreak.setEnabled(false);
            btnStop.setEnabled(true);
        }
    }

    // ==================== THEME TOGGLE ====================

    /** Toggle between light and dark mode */
    private void toggleTheme() {
        SharedPreferences themePrefs = getSharedPreferences("theme_prefs", MODE_PRIVATE);
        boolean isDarkMode = themePrefs.getBoolean("dark_mode", false);

        // Save the new preference
        themePrefs.edit().putBoolean("dark_mode", !isDarkMode).apply();

        // Apply the new theme (this will recreate the activity)
        AppCompatDelegate.setDefaultNightMode(
                !isDarkMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
    }

    /** Update the theme toggle icon (sun for dark mode, moon for light mode) */
    private void updateThemeIcon() {
        SharedPreferences themePrefs = getSharedPreferences("theme_prefs", MODE_PRIVATE);
        boolean isDarkMode = themePrefs.getBoolean("dark_mode", false);
        // In dark mode show sun (to switch to light), in light mode show moon (to switch to dark)
        btnThemeToggle.setImageResource(isDarkMode ? R.drawable.ic_sun : R.drawable.ic_moon);
    }

    // ==================== LOGOUT ====================

    /** Log out the user with a confirmation dialog */
    private void logout() {
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Logout", (d, w) -> {
                    // Stop timer if running
                    timerHandler.removeCallbacks(timerRunnable);

                    // Clear saved login state
                    SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
                    prefs.edit().clear().apply();

                    // Navigate to login screen and clear the back stack
                    Intent intent = new Intent(this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // ==================== STATE SAVING (for screen rotation) ====================

    /**
     * Save timer state when the screen is rotated.
     * This prevents losing the timer progress during configuration changes.
     */
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong("studySeconds", studySeconds);
        outState.putLong("breakSeconds", breakSeconds);
        outState.putInt("breaksTaken", breaksTaken);
        outState.putBoolean("isRunning", isRunning);
        outState.putBoolean("isOnBreak", isOnBreak);
        outState.putBoolean("hasStarted", hasStarted);
        outState.putString("subject", etSubject.getText().toString());
    }

    /** Restore timer state after screen rotation */
    private void restoreTimerState(Bundle savedInstanceState) {
        studySeconds = savedInstanceState.getLong("studySeconds", 0);
        breakSeconds = savedInstanceState.getLong("breakSeconds", 0);
        breaksTaken = savedInstanceState.getInt("breaksTaken", 0);
        isRunning = savedInstanceState.getBoolean("isRunning", false);
        isOnBreak = savedInstanceState.getBoolean("isOnBreak", false);
        hasStarted = savedInstanceState.getBoolean("hasStarted", false);

        // Restore subject text
        String subject = savedInstanceState.getString("subject", "");
        etSubject.setText(subject);
        if (hasStarted) etSubject.setEnabled(false);

        // Restore timer displays
        tvTimerDisplay.setText(StudySession.formatTimer(studySeconds));
        tvBreakTime.setText("Break Time: " + StudySession.formatTimer(breakSeconds));
        tvBreaksTaken.setText("Breaks Taken: " + breaksTaken);

        // Restore status text
        if (isOnBreak) {
            tvStatus.setText("On Break");
            tvTimerLabel.setText("Study Time (paused)");
        } else if (isRunning) {
            tvStatus.setText("Studying...");
        } else if (hasStarted) {
            tvStatus.setText("Paused");
        }

        // Restart the timer if it was running
        if (isRunning) {
            timerHandler.post(timerRunnable);
        }
    }

    // ==================== LIFECYCLE ====================

    @Override
    protected void onResume() {
        super.onResume();
        // Ensure the Timer tab is selected when returning to this screen
        bottomNav.setSelectedItemId(R.id.nav_timer);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Remove timer callbacks to prevent memory leaks
        timerHandler.removeCallbacks(timerRunnable);
    }
}
