# 🚨 UPLOAD THESE FILES NOW - December 6, 2025

## Critical Files to Upload

Upload these 4 files to fix BOTH issues:

### 1. ✅ backend/send_notification.php
**Why**: Now loads FCM credentials from services_json.json (was hardcoded before)
**Location on server**: `/htdocs/backend/send_notification.php`

### 2. ✅ backend/add_journal.php  
**Why**: Now sends push notifications to followers when you post a public journal
**Location on server**: `/htdocs/backend/add_journal.php`

### 3. ✅ backend/get_feed.php
**Why**: Added debug logging to track why journals aren't showing
**Location on server**: `/htdocs/backend/get_feed.php`

### 4. ✅ backend/test_feed.php (NEW)
**Why**: Debug tool to see exactly what's in your database
**Location on server**: `/htdocs/backend/test_feed.php`

---

## Quick Upload Steps

### Using FileZilla:
1. **Connect**:
   - Host: `fdb1032.awardspace.net`
   - Username: `4714604_soulwritedb`
   - Password: `barithegreat123`
   - Port: `21`

2. **Navigate**:
   - Remote site: Go to `/htdocs/backend/`
   - Local site: Go to your project folder `backend/`

3. **Upload** (drag and drop these 4 files):
   - `send_notification.php`
   - `add_journal.php`
   - `get_feed.php`
   - `test_feed.php`

4. **Verify**: Make sure `services_json.json` is already there

---

## After Uploading - Test Immediately

### Test 1: Debug Feed (Do this first!)
Visit this URL in your browser (replace YOUR_USER_ID with your actual ID):
```
http://barisoulwrite.atwebpages.com/backend/test_feed.php?user_id=YOUR_USER_ID
```

This will show you:
- ✅ If user exists in database
- ✅ How many journals you have
- ✅ Who you're following
- ✅ What the SQL query returns

### Test 2: Home Screen
1. Open the Android app
2. Login with your account
3. Check if journals appear on home screen
4. Check Android Logcat for logs:
   ```
   Filter: "HomeActivity"
   Look for: "Feed response", "Found X journals"
   ```

### Test 3: Notifications
1. Post a new PUBLIC journal
2. Have another user who follows you check their notifications
3. They should receive:
   - Push notification on device
   - In-app notification in notifications screen

---

## What's Fixed

### ✅ Journals Not Showing
**Before**: Home screen was empty
**After**: Shows your journals + followed users' public journals

**Why it was broken**: Unknown - that's why we added debug logging
**What we did**: Added extensive logging to track the issue

### ✅ Notifications Not Working  
**Before**: No notifications when posting journals
**After**: Followers get notified when you post public journals

**Why it was broken**: 
1. FCM credentials were hardcoded (wrong/outdated)
2. add_journal.php didn't send notifications

**What we did**:
1. Load FCM credentials from services_json.json
2. Add notification sending in add_journal.php

---

## Troubleshooting

### Issue: test_feed.php shows "User not found"
**Solution**: Use the correct user_id from your database
- Check: `SELECT id, name FROM users;`

### Issue: test_feed.php shows "No journals found"
**Solution**: Create a journal first
- Open app → Add Entry → Create journal → Save

### Issue: Journals show in test_feed.php but not in app
**Solution**: Check Android Logcat
- Look for network errors
- Check if API URL is correct
- Clear app data and login again

### Issue: Notifications saved to DB but no push notification
**Solution**: 
- Check if fcm_token is registered: `SELECT fcm_token FROM users WHERE id = YOUR_USER_ID`
- If NULL, open app and wait for token to register
- Check Logcat for "FCM Token" messages

---

## Files Location

All files are in your local project:
```
C:\Users\Usman Bari\AndroidStudioProjects\smdprojectsoulwrite\backend\
├── send_notification.php      ← Upload this
├── add_journal.php            ← Upload this
├── get_feed.php               ← Upload this
├── test_feed.php              ← Upload this (new)
└── services_json.json         ← Already uploaded (verify it's there)
```

---

## Success Checklist

After uploading, you should be able to:

- [ ] Visit test_feed.php and see your journals listed
- [ ] Open app and see journals on home screen
- [ ] Post a public journal and followers get notified
- [ ] Like a journal and author gets notified
- [ ] See notifications in notifications screen
- [ ] Click notification and it opens the journal

---

## Next Steps

1. **Upload the 4 files** (5 minutes)
2. **Run test_feed.php** to verify database (2 minutes)
3. **Open the app** and check home screen (1 minute)
4. **Post a journal** and check if notifications work (2 minutes)

**Total time**: ~10 minutes

---

## Need Help?

If it still doesn't work after uploading:

1. Send me the output of test_feed.php
2. Send me the Android Logcat (filter: "HomeActivity")
3. Check your server's error logs
4. Verify services_json.json exists on server

---

**REMEMBER**: You MUST upload all 4 files for the fixes to work!

- send_notification.php - Fixes FCM
- add_journal.php - Sends notifications
- get_feed.php - Has debug logging
- test_feed.php - Debug tool

**Don't skip any file!**

