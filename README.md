# Breaky - Study Timer & Break Manager

A simple and effective Android application that helps students manage their study sessions with timed breaks to boost productivity and prevent burnout.

## Description

**Breaky** is a study productivity app designed to help students maintain focus during study sessions by implementing structured study and break intervals. The app tracks study time per subject, reminds users to take breaks, and maintains a full session history so students can review their study habits over time.

The app follows the principle that taking regular breaks during study sessions improves concentration, reduces mental fatigue, and leads to better academic performance.

## Features

- **User Authentication** – Register and login system with secure password hashing using SQLite
- **Study Timer** – Countdown timer for study sessions with customizable duration per subject
- **Break Reminders** – Automatic break timer that activates after each study session
- **Session History** – View, edit, and delete past study sessions with detailed logs
- **Subject Tracking** – Enter subject names to track study time per subject
- **Dark/Light Theme** – Support for both light and dark mode with Material 3 design
- **Landscape Support** – Responsive layouts for both portrait and landscape orientations
- **Bottom Navigation** – Easy navigation between Timer and History screens
- **Local Storage** – All data stored locally using SQLite database

## Screenshots

<!-- Add screenshots here -->

## Tech Stack

| Component | Technology |
|-----------|-----------|
| Language | Java |
| Min SDK | 24 (Android 7.0) |
| Target SDK | 36 |
| UI Framework | Material Design 3 |
| Database | SQLite |
| Architecture | Activity-based |
| Build System | Gradle (Kotlin DSL) |

## Project Structure

```
app/src/main/
├── java/com/example/studytimebreaker/
│   ├── MainActivity.java            # Main screen with navigation
│   ├── LoginActivity.java           # User login screen
│   ├── RegisterActivity.java        # User registration screen
│   ├── TimerActivity.java           # Study/break countdown timer
│   ├── SessionHistoryActivity.java  # View past study sessions
│   ├── SessionAdapter.java          # RecyclerView adapter for sessions
│   ├── StudySession.java            # Data model for study sessions
│   └── DatabaseHelper.java          # SQLite database operations
├── res/
│   ├── layout/                      # Portrait layouts
│   ├── layout-land/                 # Landscape layouts
│   ├── drawable/                    # Vector icons
│   ├── menu/                        # Bottom navigation menu
│   └── values/                      # Colors, strings, themes
└── AndroidManifest.xml
```

## Getting Started

### Prerequisites

- Android Studio Hedgehog or later
- JDK 11 or higher
- Android device or emulator running Android 7.0+

### Installation

1. Clone the repository
   ```bash
   git clone https://github.com/Pshyco-c/breaky.git
   ```
2. Open the project in Android Studio
3. Sync Gradle files
4. Run on an emulator or physical device

## Team Members

| Name | Role |
|------|------|
| Chamikara | Project setup, UI layouts, main activity & themes |
| Sandul | Authentication logic, timer implementation & session management |
| Kosala | Database design, UI resources & final integration |

## License

This project is developed for educational purposes.
