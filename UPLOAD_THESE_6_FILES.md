# 📤 UPLOAD THESE 6 FILES TO BACKEND

## Location: 
Upload to: `barisoulwrite.atwebpages.com/backend/`

## Files to Upload:

### 1. send_notification.php ⭐ IMPORTANT
**Purpose**: Sends FCM push notifications
**Action After Upload**: Edit line 29 and add your FCM Server Key

### 2. update_fcm_token.php
**Purpose**: Saves device FCM tokens when users login

### 3. get_notifications.php
**Purpose**: Retrieves all notifications for a user

### 4. add_comment.php
**Purpose**: Adds comment + sends notification to journal author

### 5. like_journal.php
**Purpose**: Adds like + sends notification to journal author

### 6. follow_user.php
**Purpose**: Follows user + sends notification to followed user

---

## ⚠️ IMPORTANT AFTER UPLOAD:

Edit `send_notification.php` on line 29:

**Change this:**
```php
$server_key = 'YOUR_FCM_SERVER_KEY_HERE';
```

**To this:**
```php
$server_key = 'YOUR_ACTUAL_FCM_SERVER_KEY_FROM_FIREBASE';
```

---

## How to Get FCM Server Key:

1. Go to: https://console.firebase.google.com
2. Select: `smdprojectsoulwrite`
3. Click: ⚙️ Settings → Project settings → Cloud Messaging
4. Copy the **Server key** under "Cloud Messaging API (Legacy)"
5. Paste it in `send_notification.php` line 29

---

## That's It!

After uploading these 6 files and updating the Server Key, your backend is complete! 🎉

Then just rebuild the Android app and everything will work.

