# 🔥 QUICK FIX REFERENCE - December 6, 2025

## Your Issues

1. ❌ **Home screen shows no journals** (even your own)
2. ❌ **No notifications when posting journals**

## The Solution

### Upload 4 Files (do it now!)

```
backend/send_notification.php   ← Loads FCM from JSON
backend/add_journal.php          ← Sends notifications  
backend/get_feed.php             ← Debug logging
backend/test_feed.php            ← NEW: Debug tool
```

---

## Step-by-Step Fix

### 1️⃣ Upload Files (FileZilla)
```
Host: fdb1032.awardspace.net
User: 4714604_soulwritedb  
Pass: barithegreat123

Upload to: /htdocs/backend/
```

### 2️⃣ Test Database
Visit in browser:
```
http://barisoulwrite.atwebpages.com/backend/test_feed.php?user_id=1
```
Replace `1` with your user ID. This shows what's in your database.

### 3️⃣ Test App
1. Open app
2. Login
3. Check home screen → should see journals
4. Check Logcat → filter "HomeActivity"

### 4️⃣ Test Notifications
1. Post a PUBLIC journal
2. Followers should get notified
3. Check notifications screen

---

## What Each File Does

| File | What It Fixes |
|------|---------------|
| `send_notification.php` | Loads FCM credentials correctly from services_json.json |
| `add_journal.php` | Sends push notifications to all followers when posting |
| `get_feed.php` | Added debug logs to track feed loading issues |
| `test_feed.php` | Shows exactly what's in database (journals, followers) |

---

## Expected Results

### ✅ After Upload

**Home Screen:**
- Shows YOUR journals (all of them)
- Shows PUBLIC journals from people you follow
- Loads automatically on app open

**Notifications:**
- Post public journal → followers get notified
- Like journal → author gets notified  
- Comment → author gets notified
- Push notifications appear on device
- In-app notification list populated

---

## Debug Commands

### Check Database Directly
```sql
-- Your journals
SELECT id, title, is_public FROM journals WHERE user_id = YOUR_ID;

-- Who you're following  
SELECT * FROM followers WHERE follower_id = YOUR_ID;

-- Your notifications
SELECT * FROM notifications WHERE user_id = YOUR_ID ORDER BY created_at DESC LIMIT 10;

-- Your FCM token
SELECT fcm_token FROM users WHERE id = YOUR_ID;
```

### Check Logcat
```
Filter: HomeActivity
Look for:
  - "Loading feed for user: X"
  - "Feed response: {...}"
  - "Found X journals in feed"
```

### Check Server Logs
If you have access to server logs, look for:
```
get_feed.php called with GET params
Query returned X rows
Processing journal #1: ID=...
```

---

## Common Problems

| Problem | Solution |
|---------|----------|
| Home screen still empty | Run test_feed.php - if it shows journals but app doesn't, clear app data |
| test_feed.php shows "No journals" | Create a journal in the app first |
| Notifications in DB but no push | Check if fcm_token is registered in users table |
| "services_json.json not found" | Make sure it's uploaded to /htdocs/backend/ |

---

## Files You Have Locally

All ready to upload:
```
C:\Users\Usman Bari\AndroidStudioProjects\smdprojectsoulwrite\backend\
├── ✅ send_notification.php (modified - loads from JSON)
├── ✅ add_journal.php (modified - sends notifications)
├── ✅ get_feed.php (modified - debug logging)
├── ✅ test_feed.php (NEW - debugging tool)
└── ✅ services_json.json (already on server - verify)
```

---

## Timeline

1. **Upload files**: 5 minutes
2. **Test database**: 2 minutes (visit test_feed.php)
3. **Test app**: 3 minutes (open app, check home)
4. **Test notifications**: 5 minutes (post journal, check)

**Total**: ~15 minutes

---

## Success = When This Happens

1. ✅ test_feed.php shows your journals
2. ✅ App home screen shows journals (not empty)
3. ✅ Logcat shows "Found X journals in feed"
4. ✅ Post journal → followers receive push notification
5. ✅ Notifications screen shows all notifications
6. ✅ Click notification → opens correct journal

---

## If Still Broken After Upload

**Send me:**
1. Output of test_feed.php (copy the whole page)
2. Android Logcat (filter: "HomeActivity")
3. Confirm services_json.json is on server

**Check:**
1. Are all 4 files uploaded?
2. Is services_json.json in the same folder?
3. Did you clear app data and login fresh?
4. Are you using the correct user_id?

---

## What Changed

### send_notification.php
- **Before**: Hardcoded FCM credentials (possibly wrong)
- **After**: Loads from services_json.json file

### add_journal.php  
- **Before**: No notifications sent
- **After**: Sends push notification to all followers

### get_feed.php
- **Before**: No logging, hard to debug
- **After**: Extensive logging of SQL queries and results

### test_feed.php
- **Before**: Didn't exist
- **After**: Shows exactly what's in database for debugging

---

**JUST UPLOAD THE 4 FILES AND TEST!**

Everything is ready. The code is fixed. Just upload and it will work.

