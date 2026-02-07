package com.example.studytimebreaker;

import java.util.Locale;

/**
 * Model class representing a single study session.
 * Stores subject name, study/break durations, and date info.
 */
public class StudySession {

    private int id;
    private int userId;
    private String subject;
    private long studyDuration;   // in seconds
    private long breakDuration;   // in seconds
    private int breaksTaken;
    private String date;          // formatted date string
    private String createdAt;     // timestamp from database

    // Empty constructor
    public StudySession() {}

    // Constructor with all fields (except id and createdAt which are auto-generated)
    public StudySession(int userId, String subject, long studyDuration,
                        long breakDuration, int breaksTaken, String date) {
        this.userId = userId;
        this.subject = subject;
        this.studyDuration = studyDuration;
        this.breakDuration = breakDuration;
        this.breaksTaken = breaksTaken;
        this.date = date;
    }

    // ==================== GETTERS & SETTERS ====================

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public long getStudyDuration() { return studyDuration; }
    public void setStudyDuration(long studyDuration) { this.studyDuration = studyDuration; }

    public long getBreakDuration() { return breakDuration; }
    public void setBreakDuration(long breakDuration) { this.breakDuration = breakDuration; }

    public int getBreaksTaken() { return breaksTaken; }
    public void setBreaksTaken(int breaksTaken) { this.breaksTaken = breaksTaken; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    // ==================== HELPER METHODS ====================

    /**
     * Format seconds into a readable duration like "1h 23m" or "45m 10s"
     * Used in session history to show how long the user studied
     */
    public static String formatDuration(long totalSeconds) {
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;

        if (hours > 0) {
            return hours + "h " + minutes + "m";
        } else if (minutes > 0) {
            return minutes + "m " + seconds + "s";
        } else {
            return seconds + "s";
        }
    }

    /**
     * Format seconds into HH:MM:SS for the timer display
     * Used on the main timer screen
     */
    public static String formatTimer(long totalSeconds) {
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;
        return String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds);
    }
}
