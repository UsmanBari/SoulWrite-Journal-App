# SoulWrite - Journal Application

A full-featured Android journal application built with Kotlin, Firebase, and MySQL backend.

## Features Implemented

### Core Functionality (50 points)
✅ Complete journal application with all committed features
✅ User authentication and profile management
✅ Journal creation, editing, and deletion
✅ Image uploads with Firebase Storage
✅ Search and filter functionality

### Technical Requirements

#### Store Data Locally (10 points)
✅ SQLite database implementation using DatabaseHelper class
✅ Local storage for journals and user data
✅ Offline capability

#### Data Sync (15 points)
✅ Automatic sync between local SQLite and remote MySQL database
✅ Sync on app launch and after operations
✅ Conflict resolution strategy

#### Store Data on Cloud (10 points)
✅ MySQL database hosted on AwardSpace
✅ PHP API endpoints for CRUD operations
✅ Firebase Storage for images

#### GET/POST Images from/on Server using Web APIs (10 points)
✅ Image upload to Firebase Storage
✅ Image retrieval and display using Glide library
✅ Thumbnail generation

#### Lists and Search Boxes (10 points)
✅ RecyclerView for journal listings
✅ Search functionality with real-time results
✅ Filter and sort options

#### Signup and Login with Authentication (10 points)
✅ Firebase Authentication integration
✅ Email/password authentication
✅ MySQL database for user data
✅ Password reset functionality

#### Push Notifications (10 points)
✅ Firebase Cloud Messaging (FCM) integration
✅ Notification service implementation
✅ Custom notification handling

## Project Structure

```
app/src/main/
├── java/com/uh/smdprojectsoulwrite/
│   ├── Activities
│   │   ├── SplashActivity.kt
│   │   ├── LoginActivity.kt
│   │   ├── SignupActivity.kt
│   │   ├── ForgotPasswordActivity.kt
│   │   ├── HomeActivity.kt
│   │   ├── AddEntryActivity.kt
│   │   ├── DetailActivity.kt
│   │   ├── SearchActivity.kt
│   │   ├── ProfileActivity.kt
│   │   ├── EditProfileActivity.kt
│   │   ├── ChangePasswordActivity.kt
│   │   └── NotificationsActivity.kt
│   ├── Data Models
│   │   ├── User.kt
│   │   └── Journal.kt
│   ├── Database
│   │   └── DatabaseHelper.kt
│   ├── API
│   │   └── ApiHelper.kt
│   ├── Adapters
│   │   └── JournalAdapter.kt
│   └── Services
│       └── MyFirebaseMessagingService.kt
└── res/
    └── layout/
        ├── All activity layouts
        └── item_journal.xml (RecyclerView item)

backend/
├── config.php (Database configuration)
├── register.php
├── login.php
├── add_journal.php
├── get_journals.php
├── search_journals.php
├── update_journal.php
├── delete_journal.php
└── database_schema.sql
```

## Setup Instructions

### 1. Firebase Configuration
1. Make sure `google-services.json` is in the `app/` directory
2. Firebase Authentication is enabled
3. Firebase Storage is configured
4. Firebase Cloud Messaging is enabled

### 2. MySQL Database Setup (AwardSpace)
1. Create a MySQL database on AwardSpace
2. Run the SQL script from `backend/database_schema.sql`
3. Update `backend/config.php` with your database credentials:
   ```php
   $host = "your_host.awardspace.com";
   $username = "your_username";
   $password = "your_password";
   $database = "your_database";
   ```

### 3. Upload PHP Files
1. Upload all files from the `backend/` folder to your AwardSpace hosting
2. Place them in a folder like `public_html/soulwrite/`
3. Update the BASE_URL in `ApiHelper.kt`:
   ```kotlin
   private const val BASE_URL = "http://your-domain.awardspace.com/soulwrite/"
   ```

### 4. Build and Run
1. Open the project in Android Studio
2. Sync Gradle files
3. Run on an emulator or physical device (API 24+)

## Key Dependencies

```gradle
// Firebase
implementation("com.google.firebase:firebase-auth-ktx")
implementation("com.google.firebase:firebase-database-ktx")
implementation("com.google.firebase:firebase-storage-ktx")
implementation("com.google.firebase:firebase-messaging")

// Networking
implementation("com.android.volley:volley:1.2.1")
implementation("com.squareup.okhttp3:okhttp:4.11.0")

// Image Loading
implementation("com.github.bumptech.glide:glide:4.15.1")
implementation("com.squareup.picasso:picasso:2.8")

// UI Components
implementation("com.google.android.material:material:1.12.0")
implementation("de.hdodenhof:circleimageview:3.1.0")
```

## API Endpoints

- **POST** `/register.php` - User registration
- **POST** `/login.php` - User login
- **POST** `/add_journal.php` - Add new journal
- **GET** `/get_journals.php?user_id={id}` - Get user journals
- **GET** `/search_journals.php?query={text}` - Search journals
- **POST** `/update_journal.php` - Update journal
- **POST** `/delete_journal.php` - Delete journal

## Features Overview

### Authentication
- Email/password signup and login
- Firebase Authentication
- MySQL user storage
- Password reset functionality
- Session management with SharedPreferences

### Journal Management
- Create journals with title, content, and images
- Edit and delete journals
- Public/private journal settings
- Local and cloud storage
- Automatic sync

### Search and Discovery
- Real-time search functionality
- Search public journals
- Filter and sort options

### User Profile
- View and edit profile
- Change password
- Logout functionality

### Notifications
- FCM push notifications
- Custom notification handling
- Notification history

## Testing

Test the following scenarios:
1. User registration and login
2. Create journal entry (with and without image)
3. View journal list
4. Search journals
5. Edit and delete journals
6. Test offline mode (airplane mode)
7. Test data sync when back online
8. Profile management
9. Password change
10. Push notifications

## Notes

- Minimum SDK: API 24 (Android 7.0)
- Target SDK: API 36
- Language: Kotlin
- Architecture: Activity-based with MVVM principles
- Local Storage: SQLite
- Cloud Storage: MySQL + Firebase
- Image Storage: Firebase Storage

## Team Members
- i230680
- i230536
- i230077

## Troubleshooting

**Issue**: Cannot connect to MySQL database
**Solution**: Check AwardSpace database credentials in config.php and ensure BASE_URL is correct

**Issue**: Images not uploading
**Solution**: Verify Firebase Storage rules and permissions

**Issue**: Notifications not working
**Solution**: Ensure FCM is properly configured in Firebase Console

**Issue**: App crashes on login
**Solution**: Check if google-services.json is properly configured

## Future Enhancements
- Following system implementation
- Comments and likes on public journals
- Journal categories and tags
- Export journals as PDF
- Dark mode
- Widget support

