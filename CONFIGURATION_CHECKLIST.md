# SoulWrite App - Configuration Checklist

## Pre-Launch Configuration Steps

### 1. Firebase Setup
- [ ] Ensure `google-services.json` is present in `app/` directory
- [ ] Firebase Authentication is enabled in Firebase Console
- [ ] Email/Password provider is enabled
- [ ] Firebase Storage is configured with proper rules
- [ ] Firebase Cloud Messaging (FCM) is enabled
- [ ] Get your FCM Server Key from Firebase Console

### 2. Backend Configuration

#### Step 1: AwardSpace Database Setup
1. Log in to AwardSpace Control Panel
2. Create a new MySQL database
3. Note down these credentials:
   - Database Host
   - Database Name
   - Username
   - Password

#### Step 2: Run Database Schema
1. Go to phpMyAdmin in AwardSpace
2. Select your database
3. Import the `backend/database_schema.sql` file
4. Verify tables are created: `users`, `journals`, `followers`

#### Step 3: Upload PHP Files
1. Connect via FTP or File Manager
2. Create folder: `public_html/soulwrite/`
3. Upload all files from `backend/` folder
4. Edit `config.php` with your database credentials

#### Step 4: Update API URLs in App
Open `ApiHelper.kt` and update:
```kotlin
private const val BASE_URL = "http://YOUR-DOMAIN.awardspace.com/soulwrite/"
```

### 3. App Configuration

#### Update AndroidManifest.xml
Already configured, but verify:
- [ ] All activities are declared
- [ ] Internet permissions are added
- [ ] FCM service is registered

#### Update build.gradle.kts
Already configured with:
- [ ] ViewBinding enabled
- [ ] All dependencies added
- [ ] Google services plugin applied

### 4. API Helper Configuration
Update the BASE_URL in `ApiHelper.kt`:
```kotlin
private const val BASE_URL = "http://your-actual-domain.awardspace.com/soulwrite/"
```

Replace the following endpoints:
- REGISTER_URL
- LOGIN_URL
- ADD_JOURNAL_URL
- GET_JOURNALS_URL
- etc.

### 5. Testing Checklist

#### Before First Run
- [ ] Sync Gradle files
- [ ] Build project successfully
- [ ] No compilation errors
- [ ] All layouts are present

#### Authentication Testing
- [ ] Signup with new email
- [ ] Login with created account
- [ ] Logout functionality
- [ ] Password reset email
- [ ] Invalid credentials handling

#### Journal Operations Testing
- [ ] Create journal without image
- [ ] Create journal with image
- [ ] View journal list
- [ ] Open journal details
- [ ] Edit existing journal
- [ ] Delete journal
- [ ] Search journals

#### Offline Testing
- [ ] Turn on airplane mode
- [ ] Create journal (stored locally)
- [ ] View cached journals
- [ ] Turn off airplane mode
- [ ] Verify auto-sync works

#### Navigation Testing
- [ ] Bottom navigation works on all screens
- [ ] Back button navigation
- [ ] FAB button for adding journal
- [ ] Search icon navigation
- [ ] Profile navigation

#### Profile Testing
- [ ] View profile
- [ ] Edit profile (name, phone)
- [ ] Change password
- [ ] Logout

#### Notification Testing
- [ ] Send test FCM notification
- [ ] Verify notification appears
- [ ] Tap notification opens app
- [ ] Notifications page displays

### 6. Common Issues and Solutions

#### Issue: Cannot connect to database
**Check:**
- Database credentials in config.php
- AwardSpace database is active
- BASE_URL is correct
- PHP files are uploaded correctly

#### Issue: Images not uploading
**Check:**
- Firebase Storage rules allow write
- Storage permissions in AndroidManifest
- Internet connection

#### Issue: App crashes on launch
**Check:**
- google-services.json is present
- All dependencies are synced
- Minimum SDK is 24

#### Issue: Sync Gradle fails
**Check:**
- Internet connection
- Gradle version compatibility
- Dependencies are available

### 7. Important Notes

1. **AwardSpace URL Format:**
   - Usually: `http://yourusername.awardspace.com/soulwrite/`
   - Or custom domain if you have one

2. **Testing Environment:**
   - Test on Android 7.0 (API 24) or higher
   - Test on both emulator and physical device
   - Test with different screen sizes

3. **Security:**
   - Never commit config.php with real credentials
   - Use environment variables for production
   - Enable HTTPS in production

4. **Performance:**
   - Test with slow internet connection
   - Test with large images
   - Test with many journals (100+)

### 8. Deployment Steps

1. **Build Release APK:**
   ```
   Build > Generate Signed Bundle / APK
   ```

2. **Test Release Build:**
   - Install on physical device
   - Test all features
   - Verify no debug logs

3. **Update Backend:**
   - Ensure all PHP files are uploaded
   - Test API endpoints
   - Enable error logging

4. **Monitor:**
   - Check Firebase Console for errors
   - Monitor database for issues
   - Track user registrations

### 9. Final Verification

Run through this complete user flow:
1. Open app (Splash screen appears)
2. Sign up with new account
3. Login with credentials
4. View empty home screen
5. Add new journal with image
6. View journal in list
7. Open journal detail
8. Search for journal
9. Edit journal
10. View profile
11. Edit profile details
12. Change password
13. Logout
14. Login again
15. Verify all data persists

### 10. Success Criteria

✅ All authentication flows work
✅ Journals can be created, read, updated, deleted
✅ Images upload successfully
✅ Search returns correct results
✅ Data syncs between local and cloud
✅ Offline mode works
✅ Notifications can be received
✅ Navigation works smoothly
✅ No crashes or ANRs
✅ UI matches design requirements

## Contact for Issues

If you encounter issues:
1. Check error logs in Android Studio Logcat
2. Verify all configuration steps above
3. Test API endpoints directly using Postman
4. Check Firebase Console for authentication errors

## Project Status: ✅ COMPLETE

All features implemented as per rubric:
- ✅ Store data locally (SQLite)
- ✅ Data sync (Local ↔ Cloud)
- ✅ Store data on cloud (MySQL)
- ✅ GET/POST images (Firebase Storage)
- ✅ Lists and search boxes (RecyclerView)
- ✅ Signup and Login (Firebase Auth + MySQL)
- ✅ Push Notifications (FCM)

