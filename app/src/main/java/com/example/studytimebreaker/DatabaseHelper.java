package com.example.studytimebreaker;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

/**
 * SQLite Database Helper class.
 * Manages two tables: 'users' and 'study_sessions'.
 * Handles all CRUD operations for the app.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    // Database name and version
    private static final String DATABASE_NAME = "Breaky.db";
    private static final int DATABASE_VERSION = 1;

    // ==================== USERS TABLE ====================
    public static final String TABLE_USERS = "users";
    public static final String COL_USER_ID = "id";
    public static final String COL_USERNAME = "username";
    public static final String COL_EMAIL = "email";
    public static final String COL_PASSWORD = "password";
    public static final String COL_USER_CREATED = "created_at";

    // ==================== STUDY SESSIONS TABLE ====================
    public static final String TABLE_SESSIONS = "study_sessions";
    public static final String COL_SESSION_ID = "id";
    public static final String COL_SESSION_USER_ID = "user_id";
    public static final String COL_SUBJECT = "subject";
    public static final String COL_STUDY_DURATION = "study_duration";
    public static final String COL_BREAK_DURATION = "break_duration";
    public static final String COL_BREAKS_TAKEN = "breaks_taken";
    public static final String COL_DATE = "date";
    public static final String COL_SESSION_CREATED = "created_at";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create the users table
        String createUsersTable = "CREATE TABLE " + TABLE_USERS + " ("
                + COL_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_USERNAME + " TEXT NOT NULL UNIQUE, "
                + COL_EMAIL + " TEXT NOT NULL UNIQUE, "
                + COL_PASSWORD + " TEXT NOT NULL, "
                + COL_USER_CREATED + " DATETIME DEFAULT CURRENT_TIMESTAMP)";
        db.execSQL(createUsersTable);

        // Create the study_sessions table with a foreign key to users
        String createSessionsTable = "CREATE TABLE " + TABLE_SESSIONS + " ("
                + COL_SESSION_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_SESSION_USER_ID + " INTEGER NOT NULL, "
                + COL_SUBJECT + " TEXT NOT NULL, "
                + COL_STUDY_DURATION + " INTEGER NOT NULL, "
                + COL_BREAK_DURATION + " INTEGER DEFAULT 0, "
                + COL_BREAKS_TAKEN + " INTEGER DEFAULT 0, "
                + COL_DATE + " TEXT NOT NULL, "
                + COL_SESSION_CREATED + " DATETIME DEFAULT CURRENT_TIMESTAMP, "
                + "FOREIGN KEY (" + COL_SESSION_USER_ID + ") REFERENCES "
                + TABLE_USERS + "(" + COL_USER_ID + "))";
        db.execSQL(createSessionsTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SESSIONS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }

    // ==================== PASSWORD HASHING ====================

    /**
     * Hash a password using SHA-256 algorithm.
     * This ensures passwords are NOT stored in plain text.
     */
    public static String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return password;
        }
    }

    // ==================== USER OPERATIONS ====================

    /**
     * Register a new user. Password is hashed before storing.
     * Returns the new user's row ID, or -1 if registration failed.
     */
    public long registerUser(String username, String email, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_USERNAME, username);
        values.put(COL_EMAIL, email);
        values.put(COL_PASSWORD, hashPassword(password));
        long result = db.insert(TABLE_USERS, null, values);
        db.close();
        return result;
    }

    /**
     * Login a user by checking username and hashed password.
     * Returns the user's ID if credentials match, or -1 if login fails.
     */
    public int loginUser(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        String hashedPassword = hashPassword(password);
        Cursor cursor = db.rawQuery(
                "SELECT " + COL_USER_ID + " FROM " + TABLE_USERS
                        + " WHERE " + COL_USERNAME + " = ? AND " + COL_PASSWORD + " = ?",
                new String[]{username, hashedPassword});

        int userId = -1;
        if (cursor.moveToFirst()) {
            userId = cursor.getInt(0);
        }
        cursor.close();
        db.close();
        return userId;
    }

    /** Check if a username is already taken */
    public boolean isUsernameExists(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT " + COL_USER_ID + " FROM " + TABLE_USERS
                        + " WHERE " + COL_USERNAME + " = ?",
                new String[]{username});
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        db.close();
        return exists;
    }

    /** Check if an email is already registered */
    public boolean isEmailExists(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT " + COL_USER_ID + " FROM " + TABLE_USERS
                        + " WHERE " + COL_EMAIL + " = ?",
                new String[]{email});
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        db.close();
        return exists;
    }

    /** Get username by user ID */
    public String getUsername(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT " + COL_USERNAME + " FROM " + TABLE_USERS
                        + " WHERE " + COL_USER_ID + " = ?",
                new String[]{String.valueOf(userId)});
        String username = "";
        if (cursor.moveToFirst()) {
            username = cursor.getString(0);
        }
        cursor.close();
        db.close();
        return username;
    }

    // ==================== SESSION OPERATIONS (CRUD) ====================

    /**
     * CREATE - Add a new study session to the database.
     * Returns the new session's row ID, or -1 if insert failed.
     */
    public long addSession(StudySession session) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_SESSION_USER_ID, session.getUserId());
        values.put(COL_SUBJECT, session.getSubject());
        values.put(COL_STUDY_DURATION, session.getStudyDuration());
        values.put(COL_BREAK_DURATION, session.getBreakDuration());
        values.put(COL_BREAKS_TAKEN, session.getBreaksTaken());
        values.put(COL_DATE, session.getDate());
        long result = db.insert(TABLE_SESSIONS, null, values);
        db.close();
        return result;
    }

    /**
     * READ - Get all study sessions for a specific user.
     * Each user only sees their own sessions (user-based filtering).
     * Results are ordered by most recent first.
     */
    public List<StudySession> getUserSessions(int userId) {
        List<StudySession> sessions = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT * FROM " + TABLE_SESSIONS
                        + " WHERE " + COL_SESSION_USER_ID + " = ? ORDER BY "
                        + COL_SESSION_CREATED + " DESC",
                new String[]{String.valueOf(userId)});

        if (cursor.moveToFirst()) {
            do {
                StudySession session = new StudySession();
                session.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COL_SESSION_ID)));
                session.setUserId(cursor.getInt(cursor.getColumnIndexOrThrow(COL_SESSION_USER_ID)));
                session.setSubject(cursor.getString(cursor.getColumnIndexOrThrow(COL_SUBJECT)));
                session.setStudyDuration(cursor.getLong(cursor.getColumnIndexOrThrow(COL_STUDY_DURATION)));
                session.setBreakDuration(cursor.getLong(cursor.getColumnIndexOrThrow(COL_BREAK_DURATION)));
                session.setBreaksTaken(cursor.getInt(cursor.getColumnIndexOrThrow(COL_BREAKS_TAKEN)));
                session.setDate(cursor.getString(cursor.getColumnIndexOrThrow(COL_DATE)));
                session.setCreatedAt(cursor.getString(cursor.getColumnIndexOrThrow(COL_SESSION_CREATED)));
                sessions.add(session);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return sessions;
    }

    /**
     * UPDATE - Update a study session's subject name.
     * Returns the number of rows affected.
     */
    public int updateSession(int sessionId, String newSubject) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_SUBJECT, newSubject);
        int rowsAffected = db.update(TABLE_SESSIONS, values,
                COL_SESSION_ID + " = ?",
                new String[]{String.valueOf(sessionId)});
        db.close();
        return rowsAffected;
    }

    /**
     * DELETE - Delete a study session by its ID.
     * Returns the number of rows deleted.
     */
    public int deleteSession(int sessionId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rowsAffected = db.delete(TABLE_SESSIONS,
                COL_SESSION_ID + " = ?",
                new String[]{String.valueOf(sessionId)});
        db.close();
        return rowsAffected;
    }

    /**
     * Get total study time (in seconds) for a user.
     * Used to display statistics on the history screen.
     */
    public long getTotalStudyTime(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT SUM(" + COL_STUDY_DURATION + ") FROM " + TABLE_SESSIONS
                        + " WHERE " + COL_SESSION_USER_ID + " = ?",
                new String[]{String.valueOf(userId)});
        long total = 0;
        if (cursor.moveToFirst()) {
            total = cursor.getLong(0);
        }
        cursor.close();
        db.close();
        return total;
    }

    /**
     * Get total number of study sessions for a user.
     */
    public int getSessionCount(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT COUNT(*) FROM " + TABLE_SESSIONS
                        + " WHERE " + COL_SESSION_USER_ID + " = ?",
                new String[]{String.valueOf(userId)});
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        db.close();
        return count;
    }
}
