# SoulWrite - MySQL Only Implementation

## Important Change: Firebase Only for FCM

This version of the app uses **MySQL for ALL data operations** (authentication, storage, images). Firebase is **ONLY used for Push Notifications (FCM)**.

## Architecture

- **Authentication:** MySQL database with PHP APIs
- **User Data:** MySQL database
- **Journal Data:** MySQL database  
- **Images:** Uploaded to your web server (AwardSpace)
- **Local Storage:** SQLite
- **Push Notifications:** Firebase Cloud Messaging (FCM) ONLY

## Setup Instructions

### 1. MySQL Database Setup

1. **Create database on AwardSpace**
2. **Run the SQL schema:**
   ```sql
   -- Use database_schema.sql
   ```

3. **Update config.php:**
   ```php
   $host = "your_host.awardspace.com";
   $username = "your_db_username";
   $password = "your_db_password";
   $database = "your_db_name";
   ```

### 2. Upload PHP Files

Upload ALL files from `backend/` folder to your AwardSpace hosting:
```
public_html/soulwrite/
├── config.php
├── register.php
├── login.php
├── add_journal.php
├── get_journals.php
├── search_journals.php
├── update_journal.php
├── delete_journal.php
├── upload_image.php
└── uploads/              (create this folder, chmod 755)
    └── journals/         (auto-created by upload script)
```

**Important:** Create `uploads` folder and set permissions to 755:
```bash
mkdir uploads
chmod 755 uploads
```

### 3. Update API URLs in App

Open `ApiHelper.kt` and update BASE_URL:
```kotlin
private const val BASE_URL = "http://your-actual-domain.awardspace.com/soulwrite/"
```

### 4. Firebase Setup (FCM Only)

1. **Download google-services.json** from Firebase Console
2. **Place in app/ folder**
3. **Enable Cloud Messaging only** (no need for Auth or Storage)
4. **Get Server Key** for sending notifications

### 5. Image Upload

Images are now uploaded to YOUR server:
- Upload path: `uploads/journals/`
- Thumbnails auto-generated: `uploads/journals/thumb_*.jpg`
- Max size: 5MB
- Supported: JPG, PNG, GIF

## Features Using MySQL

### ✅ Authentication
- **Signup:** PHP registers user in MySQL
- **Login:** PHP validates credentials from MySQL
- **Password:** Stored as bcrypt hash in MySQL
- **Session:** Managed via SharedPreferences

### ✅ Journal Operations
- **Create:** Saved to MySQL via `add_journal.php`
- **Read:** Fetched from MySQL via `get_journals.php`
- **Update:** Modified in MySQL via `update_journal.php`
- **Delete:** Removed from MySQL via `delete_journal.php`

### ✅ Image Handling
- **Upload:** `upload_image.php` saves to server
- **Thumbnail:** Auto-generated on server
- **Storage:** Your web server filesystem
- **URL:** Returned to app for database storage

### ✅ Search
- **Search:** MySQL LIKE query via `search_journals.php`
- **Public journals:** Searchable by all users
- **Private journals:** Only owner can see

### ✅ Local Storage
- **SQLite:** All data cached locally
- **Offline:** App works without internet
- **Sync:** Auto-sync when online

### ✅ Push Notifications (FCM ONLY)
- **Firebase:** Only for FCM service
- **No Auth:** Not using Firebase Authentication
- **No Storage:** Not using Firebase Storage
- **Just Notifications:** FCM for push notifications

## API Endpoints

All endpoints are PHP files on your server:

| Endpoint | Method | Purpose |
|----------|--------|---------|
| register.php | POST | User registration |
| login.php | POST | User login |
| add_journal.php | POST | Create journal |
| get_journals.php | GET | Fetch journals |
| search_journals.php | GET | Search journals |
| update_journal.php | POST | Update journal |
| delete_journal.php | POST | Delete journal |
| upload_image.php | POST | Upload image |

## Database Schema

```sql
-- Users table
CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    phone VARCHAR(20),
    password VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Journals table
CREATE TABLE journals (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    image_url VARCHAR(500),
    thumbnail_url VARCHAR(500),
    is_public TINYINT(1) DEFAULT 0,
    date BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);
```

## Testing Checklist

- [ ] User can register (data saved to MySQL)
- [ ] User can login (validated from MySQL)
- [ ] Create journal without image
- [ ] Create journal with image (uploaded to server)
- [ ] View journal list (from MySQL)
- [ ] Search journals (MySQL LIKE query)
- [ ] Edit journal (updated in MySQL)
- [ ] Delete journal (removed from MySQL)
- [ ] Offline mode (uses SQLite)
- [ ] Data sync when back online
- [ ] Receive FCM notification

## File Structure

```
app/src/main/java/com/uh/smdprojectsoulwrite/
├── Activities (NO Firebase Auth calls)
│   ├── LoginActivity.kt       - MySQL login only
│   ├── SignupActivity.kt      - MySQL registration only
│   ├── HomeActivity.kt         - MySQL data fetch
│   └── AddEntryActivity.kt     - Server image upload
├── DatabaseHelper.kt           - SQLite operations
├── ApiHelper.kt               - MySQL API calls
└── MyFirebaseMessagingService.kt - FCM ONLY

backend/
├── config.php                 - MySQL connection
├── register.php               - User registration
├── login.php                  - User authentication
├── add_journal.php            - Create journal
├── get_journals.php           - Fetch journals
├── search_journals.php        - Search journals
├── upload_image.php           - Image upload & thumbnail
└── database_schema.sql        - Database structure
```

## Important Notes

### Firebase Usage
- **ONLY for FCM (Push Notifications)**
- No Firebase Authentication
- No Firebase Storage  
- No Firebase Realtime Database
- Just Cloud Messaging

### Why MySQL?
- Full control over data
- No Firebase quotas/limits
- Standard SQL operations
- Easier to backup
- Free hosting on AwardSpace

### Image Storage
- Stored on YOUR server
- Path: uploads/journals/
- Thumbnails auto-generated
- Direct URLs in database
- No Firebase Storage costs

## Troubleshooting

### Cannot login
- Check MySQL connection in config.php
- Verify user exists in database
- Test login.php directly in browser

### Images not uploading
- Check uploads/ folder exists
- Verify folder permissions (755)
- Check PHP max_upload_filesize
- Test upload_image.php directly

### Data not syncing
- Verify BASE_URL in ApiHelper.kt
- Check PHP files are uploaded
- Test API endpoints in browser
- Check MySQL connection

## Development vs Production

### Development (Local Testing)
- Test without MySQL initially
- Use SQLite only
- Mock API responses

### Production (Full Setup)
- Setup MySQL on AwardSpace
- Upload PHP files
- Configure API URLs
- Test end-to-end

## Security Considerations

- ✅ Passwords hashed with bcrypt
- ✅ SQL injection prevention (prepared statements)
- ✅ File upload validation
- ✅ Image type verification
- ✅ File size limits
- ⚠️ Use HTTPS in production
- ⚠️ Add API authentication tokens

## Next Steps

1. Setup MySQL database
2. Upload PHP files
3. Create uploads folder
4. Update API URLs
5. Test registration
6. Test login
7. Test journal creation
8. Test image upload
9. Test search
10. Setup FCM for notifications

## Support

For issues:
1. Check PHP error logs on AwardSpace
2. Check Android Logcat
3. Test API endpoints directly
4. Verify MySQL connection

---

**Remember: Firebase is ONLY for FCM notifications. Everything else is MySQL!**

