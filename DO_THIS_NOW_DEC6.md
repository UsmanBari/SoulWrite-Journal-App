# 🚀 QUICK START GUIDE - Fix Journals & Notifications

## DO THIS NOW (In Order):

### 1️⃣ BUILD THE ANDROID APP (2 minutes)

Open Android Studio and run these in order:

1. **File → Sync Project with Gradle Files** (wait to finish)
2. **Build → Clean Project** (wait to finish)
3. **Build → Rebuild Project** (wait to finish)

✅ Check Build window - should say "BUILD SUCCESSFUL"

---

### 2️⃣ UPLOAD TO SERVER (5 minutes)

**You need to upload ONE file that's CRITICAL for notifications:**

📤 **Upload `services_json.json`** to server:
- Location on your computer: `C:\Users\Usman Bari\AndroidStudioProjects\smdprojectsoulwrite\backend\services_json.json`
- Upload to server: `http://barisoulwrite.atwebpages.com/backend/services_json.json`

**The PHP files are probably already uploaded, but make sure these are there:**
- like_journal.php
- add_comment.php
- get_comments.php
- send_notification.php
- update_fcm_token.php
- follow_user.php

---

### 3️⃣ TEST IT (5 minutes)

#### Test 1: See Your Journals on Home
1. Login as User 1 (usman@gmail.com)
2. ✅ You should see ALL your journals on home screen

#### Test 2: Like a Journal
1. Stay logged in as User 1
2. Find a public journal from User 2 on home screen
3. Tap to open it
4. ✅ You should see a HEART icon and COMMENT icon at bottom
5. Tap the heart ❤
6. ✅ Heart should turn filled/red
7. ✅ User 2 should get a notification

#### Test 3: Comment on a Journal
1. Same journal from Test 2
2. Type a comment in the text box at bottom
3. Tap the send arrow button
4. ✅ Comment should appear in list
5. ✅ User 2 should get a notification

#### Test 4: Check Notifications Work
1. Logout, login as User 2 (saad@gmail.com)
2. Tap the Alerts/Notifications icon
3. ✅ You should see "User 1 liked your journal"
4. ✅ You should see "User 1 commented on your journal"
5. ✅ You should have received PUSH notifications

---

## 🐛 IF IT DOESN'T WORK:

### Problem: Build fails in Android Studio
**Fix**: Do this:
1. **File → Invalidate Caches → Invalidate and Restart**
2. Wait for restart
3. Try build again

### Problem: No like/comment buttons appear
**Check**: 
- Are you viewing a PUBLIC journal from ANOTHER user?
- Your own journals don't show like/comment (they show edit/delete)

### Problem: No push notifications
**Check**: 
1. Is `services_json.json` uploaded? (CRITICAL!)
2. Run this URL in browser: `http://barisoulwrite.atwebpages.com/backend/test_feed.php?user_id=1`
   - Should show your journals without errors
3. Check if users have FCM tokens in database:
   ```sql
   SELECT name, fcm_token FROM users;
   ```
   - Should NOT be NULL for logged-in users

### Problem: Journals not showing on home screen
**Fix**:
1. Clear app data: Settings → Apps → SoulWrite → Clear Data
2. Login again
3. Should load fresh from server

---

## 📱 WHAT YOU'LL SEE:

### Home Screen:
- Your own journals (all of them)
- Public journals from users you follow
- Each journal shows: thumbnail, title, preview, author name (if public)

### Detail Screen (Your Own Journal):
- Edit button (top right)
- Delete button (top right)
- NO like/comment buttons

### Detail Screen (Other User's Public Journal):
- NO edit/delete buttons
- LIKE button with count
- COMMENT button with count
- Comments list
- Text box to add comment

### Notifications (Alerts Tab):
- "User X liked your journal: Title"
- "User X commented on your journal: Title"  
- "User X started following you"

---

## ✅ SUCCESS CRITERIA:

You'll know it's working when:
- ✅ You see your journals on home screen
- ✅ You see public journals from users you follow
- ✅ You can like other users' public journals
- ✅ You can comment on other users' public journals
- ✅ Other users receive push notifications
- ✅ Notifications appear in Alerts tab
- ✅ Like count increases when you like
- ✅ Comments appear in comments list

---

**Time to Complete**: ~10 minutes total
**Difficulty**: Easy - just build, upload, test!

🎯 **Start with Step 1 (Build) NOW!**

