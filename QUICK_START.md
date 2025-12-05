# SoulWrite - Quick Start Guide

## 🚀 Getting Started in 5 Minutes

### Prerequisites
- Android Studio (Latest version recommended)
- Android device or emulator (API 24+)
- Firebase account
- AwardSpace account (for MySQL hosting)

---

## Step 1: Open Project (30 seconds)

1. Open Android Studio
2. File → Open → Select the `smdprojectsoulwrite` folder
3. Wait for Gradle sync to complete

---

## Step 2: Firebase Setup (2 minutes)

### If `google-services.json` is already present:
✅ Skip to Step 3

### If not:
1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Create/Select project
3. Add Android app
4. Package name: `com.uh.smdprojectsoulwrite`
5. Download `google-services.json`
6. Place it in `app/` folder
7. Enable:
   - Authentication (Email/Password)
   - Storage
   - Cloud Messaging

---

## Step 3: Database Setup (2 minutes)

### Quick Option (For Testing):
You can test the app WITHOUT setting up MySQL initially. It will work with:
- Firebase Authentication ✅
- Local SQLite storage ✅
- Firebase Storage for images ✅

The MySQL sync is optional for initial testing.

### Full Option (For Production):
1. Login to [AwardSpace](https://www.awardspace.com/)
2. Create MySQL database
3. Open phpMyAdmin
4. Import `backend/database_schema.sql`
5. Note your database credentials
6. Upload `backend/` PHP files to your hosting
7. Edit `backend/config.php` with your credentials
8. Update `ApiHelper.kt` BASE_URL with your domain

---

## Step 4: Build and Run (30 seconds)

1. Click the green ▶️ Play button
2. Select your device/emulator
3. Wait for app to install

---

## Step 5: Test the App (1 minute)

### First Launch:
1. **Splash Screen** appears for 2 seconds
2. **Login Screen** appears

### Create Account:
1. Click "Sign Up"
2. Enter:
   - Name: Test User
   - Email: test@example.com
   - Phone: 1234567890
   - Password: test123
3. Click "Signup"

### You're In! 🎉
- Home screen with empty journal list
- Tap the blue ➕ button to add your first journal

---

## Quick Feature Tour

### 🏠 Home Screen
- View all your journals
- Tap ➕ (FAB) to add new journal
- Tap 🔍 to search
- Use bottom navigation

### ➕ Add Journal
- Enter title and content
- Optionally add an image
- Check "Make journal public" to share
- Click "Save Entry"

### 🔍 Search
- Type to search journals
- Results appear in real-time

### 🔔 Notifications
- View app notifications
- Currently shows empty state

### 👤 Profile
- View your profile
- Edit profile information
- Change password
- Logout

---

## Common First-Time Issues

### ❌ Build fails
**Solution:** 
```
File → Invalidate Caches → Invalidate and Restart
```

### ❌ App crashes on startup
**Check:**
- `google-services.json` is in `app/` folder
- Minimum SDK is 24
- Internet permission is in AndroidManifest

### ❌ Cannot login
**Check:**
- Firebase Authentication is enabled
- Email/Password provider is enabled in Firebase
- Internet connection is active

### ❌ Images not uploading
**Check:**
- Firebase Storage is enabled
- Storage permissions in AndroidManifest
- Storage rules in Firebase allow write

---

## Testing Checklist

Quick tests to verify everything works:

- [ ] App launches successfully
- [ ] Can signup new user
- [ ] Can login with created account
- [ ] Can add journal without image
- [ ] Can add journal with image
- [ ] Can view journal list
- [ ] Can open journal details
- [ ] Can search journals
- [ ] Can view profile
- [ ] Can logout

---

## Development Mode

### Enable Debug Logging
Already enabled in the code. Check Android Studio Logcat:
- Filter: `com.uh.smdprojectsoulwrite`
- Look for authentication, database, and sync logs

### Test Data
The app creates test data automatically in SQLite when you add journals.

---

## Next Steps

Once basic testing works:

1. **Setup MySQL** (for production)
   - Follow detailed instructions in `CONFIGURATION_CHECKLIST.md`
   - Update API URLs in `ApiHelper.kt`

2. **Customize**
   - Update app name in `strings.xml`
   - Change app icon in `res/mipmap/`
   - Modify color scheme in `colors.xml`

3. **Deploy**
   - Build signed APK
   - Test on physical devices
   - Upload to Play Store (optional)

---

## File Structure Overview

```
app/src/main/
├── java/com/uh/smdprojectsoulwrite/
│   ├── SplashActivity.kt         → Entry point
│   ├── LoginActivity.kt          → Authentication
│   ├── SignupActivity.kt         → Registration
│   ├── HomeActivity.kt           → Main screen
│   ├── AddEntryActivity.kt       → Create journal
│   ├── DatabaseHelper.kt         → SQLite operations
│   ├── ApiHelper.kt              → Network calls
│   └── [Other activities]
└── res/
    ├── layout/                    → XML layouts
    ├── drawable/                  → Images & icons
    └── values/                    → Colors, strings

backend/
├── config.php                     → Database config
├── register.php                   → User registration API
├── login.php                      → User login API
├── add_journal.php                → Create journal API
├── get_journals.php               → Fetch journals API
└── [Other PHP files]
```

---

## Support

For detailed information, check:
- `README.md` - Complete project documentation
- `CONFIGURATION_CHECKLIST.md` - Detailed setup instructions
- `NAVIGATION_GUIDE.md` - Navigation implementation details

---

## Important URLs

- **Firebase Console:** https://console.firebase.google.com/
- **AwardSpace:** https://www.awardspace.com/
- **Project GitHub:** [Your repository URL]

---

## Success! 🎉

If you can:
1. ✅ Open the app
2. ✅ Create an account
3. ✅ Add a journal
4. ✅ See it in the list

**You're all set!** The app is working correctly.

---

## Need Help?

Check the logs:
1. Open Android Studio
2. View → Tool Windows → Logcat
3. Filter by: `com.uh.smdprojectsoulwrite`
4. Look for error messages

Common solutions:
- Clean Project: Build → Clean Project
- Rebuild: Build → Rebuild Project
- Sync: File → Sync Project with Gradle Files
- Restart: File → Invalidate Caches → Invalidate and Restart

---

**Happy Journaling! 📝✨**

