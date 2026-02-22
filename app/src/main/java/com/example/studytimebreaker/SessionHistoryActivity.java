package com.example.studytimebreaker;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.List;

/**
 * Session History screen - displays a list of all past study sessions.
 * Users can only see their own sessions (filtered by userId).
 * Supports EDIT (update subject name) and DELETE operations.
 * Also shows aggregate statistics: total sessions and total study time.
 */
public class SessionHistoryActivity extends AppCompatActivity
        implements SessionAdapter.OnSessionActionListener {

    // UI elements
    private RecyclerView recyclerView;
    private TextView tvTotalSessions, tvTotalStudyTime;
    private LinearLayout tvEmptyMessage;
    private ImageView btnThemeToggle;
    private BottomNavigationView bottomNav;

    // Data
    private DatabaseHelper dbHelper;
    private SessionAdapter adapter;
    private int userId;
    private List<StudySession> sessionList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_session_history);

        // Get user ID from SharedPreferences
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        userId = prefs.getInt("user_id", -1);

        dbHelper = new DatabaseHelper(this);

        initViews();
        setupListeners();
        loadSessions();
    }

    /** Initialize all view references */
    private void initViews() {
        recyclerView = findViewById(R.id.recyclerSessions);
        tvTotalSessions = findViewById(R.id.tvTotalSessions);
        tvTotalStudyTime = findViewById(R.id.tvTotalStudyTime);
        tvEmptyMessage = findViewById(R.id.tvEmptyMessage);
        btnThemeToggle = findViewById(R.id.btnThemeToggle);
        bottomNav = findViewById(R.id.bottomNav);

        // Set up RecyclerView with a vertical list layout
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Update theme icon
        SharedPreferences themePrefs = getSharedPreferences("theme_prefs", MODE_PRIVATE);
        boolean isDarkMode = themePrefs.getBoolean("dark_mode", false);
        btnThemeToggle.setImageResource(isDarkMode ? R.drawable.ic_sun : R.drawable.ic_moon);
    }

    /** Set up click listeners */
    private void setupListeners() {
        // Theme toggle
        btnThemeToggle.setOnClickListener(v -> {
            SharedPreferences themePrefs = getSharedPreferences("theme_prefs", MODE_PRIVATE);
            boolean isDarkMode = themePrefs.getBoolean("dark_mode", false);
            themePrefs.edit().putBoolean("dark_mode", !isDarkMode).apply();
            AppCompatDelegate.setDefaultNightMode(
                    !isDarkMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
        });

        // Bottom navigation
        bottomNav.setSelectedItemId(R.id.nav_history);
        bottomNav.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_timer) {
                finish(); // Go back to timer screen
                return true;
            }
            return item.getItemId() == R.id.nav_history;
        });
    }

    /**
     * Load all study sessions for the current user from the database.
     * Updates the RecyclerView and statistics display.
     */
    private void loadSessions() {
        // Get sessions only for the logged-in user
        sessionList = dbHelper.getUserSessions(userId);

        // Show empty message if no sessions exist
        if (sessionList.isEmpty()) {
            tvEmptyMessage.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            tvEmptyMessage.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }

        // Set up the RecyclerView adapter
        adapter = new SessionAdapter(sessionList, this);
        recyclerView.setAdapter(adapter);

        // Update statistics
        int totalSessions = dbHelper.getSessionCount(userId);
        long totalStudyTime = dbHelper.getTotalStudyTime(userId);
        tvTotalSessions.setText("Total Sessions: " + totalSessions);
        tvTotalStudyTime.setText("Total Study Time: " + StudySession.formatDuration(totalStudyTime));
    }

    // ==================== EDIT & DELETE (from SessionAdapter callbacks) ====================

    /**
     * EDIT operation - update the subject name of a session.
     * Shows a dialog with an editable text field.
     */
    @Override
    public void onEdit(StudySession session, int position) {
        // Create an EditText for the dialog
        EditText editText = new EditText(this);
        editText.setText(session.getSubject());
        editText.setHint("Enter subject name");
        editText.setPadding(60, 40, 60, 20);

        new AlertDialog.Builder(this)
                .setTitle("Edit Session")
                .setMessage("Update the subject name:")
                .setView(editText)
                .setPositiveButton("Save", (dialog, which) -> {
                    String newSubject = editText.getText().toString().trim();
                    if (!newSubject.isEmpty()) {
                        // Update in the database
                        dbHelper.updateSession(session.getId(), newSubject);
                        // Update in the list and refresh the item
                        session.setSubject(newSubject);
                        adapter.notifyItemChanged(position);
                        Toast.makeText(this, "Session updated!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Subject cannot be empty", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    /**
     * DELETE operation - remove a session from the database.
     * Shows a confirmation dialog before deleting.
     */
    @Override
    public void onDelete(StudySession session, int position) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Session")
                .setMessage("Are you sure you want to delete this study session?\n\nSubject: "
                        + session.getSubject())
                .setPositiveButton("Delete", (dialog, which) -> {
                    // Delete from database
                    dbHelper.deleteSession(session.getId());
                    // Remove from the list and refresh
                    sessionList.remove(position);
                    adapter.notifyItemRemoved(position);
                    adapter.notifyItemRangeChanged(position, sessionList.size());

                    // Reload to update statistics and empty state
                    loadSessions();

                    Toast.makeText(this, "Session deleted", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        bottomNav.setSelectedItemId(R.id.nav_history);
        loadSessions(); // Refresh data when returning to this screen
    }
}
