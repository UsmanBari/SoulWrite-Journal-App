# 📤 EXACT UPLOAD STEPS - Follow This!

## What You're Uploading

4 PHP files that fix:
1. ✅ Home screen not showing journals
2. ✅ FCM notifications not working

---

## Step-by-Step Upload Instructions

### 1. Open FileZilla

If you don't have it: Download from https://filezilla-project.org/

### 2. Connect to Your Server

Fill in these details at the top:

```
Host:       fdb1032.awardspace.net
Username:   4714604_soulwritedb
Password:   barithegreat123
Port:       21
```

Click **"Quickconnect"**

### 3. Navigate to Backend Folder

**On the server (right side)**:
- Navigate to: `/htdocs/backend/`
- You should see files like: config.php, login.php, etc.

**On your computer (left side)**:
- Navigate to: `C:\Users\Usman Bari\AndroidStudioProjects\smdprojectsoulwrite\backend\`

### 4. Upload These 4 Files

Drag these files from left to right:

1. ✅ `send_notification.php` (overwrite existing)
2. ✅ `add_journal.php` (overwrite existing)  
3. ✅ `get_feed.php` (overwrite existing)
4. ✅ `test_feed.php` (new file)

**Make sure all 4 transfers complete successfully!**

### 5. Verify Services JSON Exists

Check that this file is on the server:
- `/htdocs/backend/services_json.json`

If it's not there, upload it too!

---

## After Upload - Test Immediately!

### Test 1: Database Debug (2 minutes)

**Open this URL in your browser:**
```
http://barisoulwrite.atwebpages.com/backend/test_feed.php?user_id=1
```

**Change `1` to your actual user ID**

**What you should see:**
```
Testing Feed for User ID: 1

✅ User found: Your Name (@username)

--- User's Own Journals ---
Found 3 journal(s):
  - ID: 10, Title: My Journal, Public: 1, Date: 1733500000
  - ID: 9, Title: Private Journal, Public: 0, Date: 1733400000
  ...

--- Following ---
Following 2 user(s):
  - ID: 5, Name: Friend Name (@friend)
    Has 3 public journal(s)
  ...

--- Feed Query Test ---
✅ Feed query returned 5 journal(s):
  - ID: 10, Title: My Journal, Author: Your Name, Public: 1
  - ID: 9, Title: Private Journal, Author: Your Name, Public: 0
  ...
```

**If you see this, database is working correctly!**

### Test 2: App Home Screen (3 minutes)

1. **Open the Android app**
2. **Login** with your account
3. **Go to Home screen**
4. **Should see journals now!**

**Open Android Studio Logcat:**
- Filter by: `HomeActivity`
- Look for these messages:
  ```
  HomeActivity: Loading feed for user: 1
  HomeActivity: Feed response: {"success":true,...}
  HomeActivity: Found 5 journals in feed
  ```

### Test 3: Notifications (5 minutes)

**You need 2 users for this test:**

**User A (You):**
1. Post a new PUBLIC journal
2. Check Logcat for: "Journal added successfully"

**User B (Another account or friend):**
1. Should receive PUSH NOTIFICATION
2. Should see notification in notifications screen
3. Click it → should open the journal

---

## Troubleshooting

### ❌ test_feed.php shows "User not found"

**Solution**: You're using wrong user_id
- Login to app
- Check SharedPreferences or database
- Use correct user_id in URL

### ❌ test_feed.php shows "No journals found"

**Solution**: Create a journal first!
1. Open app
2. Click + button
3. Create a journal
4. Save it
5. Run test_feed.php again

### ❌ App home screen still empty

**Solution**: 
1. Check if test_feed.php shows journals → if yes, problem is in app
2. Clear app data: Settings → Apps → SoulWrite → Storage → Clear Data
3. Login again
4. Check Logcat for errors

### ❌ Notifications not working

**Solution**:
1. Check if FCM token is registered:
   - Query: `SELECT fcm_token FROM users WHERE id = YOUR_USER_ID`
   - If NULL, open app and wait 30 seconds
2. Make sure journal is PUBLIC (not private)
3. Make sure User B is following User A
4. Check server logs for FCM errors

---

## What Each File Does

### send_notification.php
**Before**: Hardcoded FCM credentials (wrong)
**After**: Loads from services_json.json (correct)
**Impact**: Push notifications will work now

### add_journal.php
**Before**: No notifications sent
**After**: Sends notification to all followers
**Impact**: Followers get notified when you post

### get_feed.php
**Before**: No logging, hard to debug
**After**: Extensive logging
**Impact**: Can track why journals don't show

### test_feed.php
**Before**: Didn't exist
**After**: Shows database contents
**Impact**: Can verify data directly

---

## Expected Timeline

| Task | Time |
|------|------|
| Connect FileZilla | 1 min |
| Navigate to folder | 1 min |
| Upload 4 files | 2 min |
| Run test_feed.php | 1 min |
| Open app and test | 2 min |
| Test notifications | 3 min |
| **TOTAL** | **~10 min** |

---

## Success Criteria

You know it worked when:

1. ✅ test_feed.php shows your journals
2. ✅ App home screen shows journals (not empty)
3. ✅ Logcat shows "Found X journals in feed"
4. ✅ Post journal → followers get notification
5. ✅ Push notification appears on device
6. ✅ Notification opens correct journal when clicked

---

## Files Location Summary

### Local (Your Computer)
```
C:\Users\Usman Bari\AndroidStudioProjects\smdprojectsoulwrite\backend\
├── send_notification.php   ← Upload
├── add_journal.php         ← Upload
├── get_feed.php            ← Upload
├── test_feed.php           ← Upload
└── services_json.json      ← Verify on server
```

### Server (Upload Destination)
```
/htdocs/backend/
├── send_notification.php   ← Overwrite
├── add_journal.php         ← Overwrite
├── get_feed.php            ← Overwrite
├── test_feed.php           ← New file
└── services_json.json      ← Must exist
```

---

## Quick Checklist

Before uploading:
- [ ] FileZilla installed
- [ ] Server credentials ready
- [ ] Files located on computer

During upload:
- [ ] Connected to server successfully
- [ ] Navigated to /htdocs/backend/
- [ ] Uploaded send_notification.php
- [ ] Uploaded add_journal.php
- [ ] Uploaded get_feed.php
- [ ] Uploaded test_feed.php
- [ ] Verified services_json.json exists

After upload:
- [ ] Ran test_feed.php in browser
- [ ] Opened app and checked home screen
- [ ] Checked Logcat for logs
- [ ] Tested notifications
- [ ] Confirmed everything works

---

## If You Get Stuck

**Send me these 3 things:**

1. **test_feed.php output** (copy the entire page)
2. **Android Logcat** (filter: "HomeActivity", copy recent logs)
3. **Screenshot** of what you see in the app

**Also verify:**
- All 4 files uploaded successfully
- services_json.json exists on server
- You're using correct user_id
- App is connected to internet

---

## Ready? Start Upload!

1. Open FileZilla
2. Connect to server
3. Upload 4 files
4. Test immediately
5. Done! 🎉

**Total time: ~10 minutes**

**YOU CAN DO THIS!** 💪

The code is fixed, files are ready, just upload and test!

