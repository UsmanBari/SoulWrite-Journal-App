# 🚀 COMPLETE AWARDSPACE SETUP GUIDE

## Step-by-Step Instructions for Database Setup

---

## PART 1: CREATE AWARDSPACE ACCOUNT

### Step 1: Sign Up for AwardSpace

1. **Go to:** https://www.awardspace.com/
2. **Click:** "Sign Up" button (top right)
3. **Choose:** Free Hosting plan
4. **Fill in:**
   - Email address
   - Password
   - Domain name (e.g., `soulwrite-yourname`)
5. **Verify:** Check your email for verification link
6. **Click:** Verification link in email
7. **Done:** Account is now active ✅

---

## PART 2: CREATE MYSQL DATABASE

### Step 2: Login to Control Panel

1. **Go to:** https://www.awardspace.com/login
2. **Enter:** Your email and password
3. **Click:** "Login"
4. **You'll see:** Control Panel dashboard

### Step 3: Create Database

1. **In Control Panel, find:** "MySQL Databases" section
2. **Click:** "MySQL Databases"
3. **You'll see:** Database creation form

4. **Fill in the form:**
   ```
   Database Name: soulwrite_db
   ```
   - Or any name you prefer
   - AwardSpace will add a prefix automatically
   - Final name will be like: `4385921_soulwrite_db`

5. **Click:** "Create Database" button

6. **IMPORTANT - SAVE THESE DETAILS:**
   ```
   Database Host: mysqlXXX.awardspace.com
   Database Name: 4385921_soulwrite_db
   Username: 4385921_soulwrite
   Password: [auto-generated or set by you]
   ```

   ⚠️ **WRITE THESE DOWN - YOU'LL NEED THEM!**

---

## PART 3: ACCESS PHPMYADMIN

### Step 4: Open phpMyAdmin

1. **In Control Panel, find:** "phpMyAdmin" link
2. **Click:** "phpMyAdmin"
3. **New tab opens:** phpMyAdmin interface
4. **You'll be automatically logged in**

### Step 5: Select Your Database

1. **On the left sidebar:** See list of databases
2. **Click:** Your database name (e.g., `4385921_soulwrite_db`)
3. **You'll see:** Empty database (no tables yet)

---

## PART 4: CREATE DATABASE TABLES

### Step 6: Import SQL Schema

1. **In phpMyAdmin, click:** "SQL" tab (top menu)
2. **You'll see:** Large text area

3. **Copy this entire SQL code:**

```sql
-- SoulWrite Database Schema
-- Copy and paste this entire code into phpMyAdmin

-- Create users table
CREATE TABLE IF NOT EXISTS `users` (
  `id` INT(11) NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(255) NOT NULL,
  `email` VARCHAR(255) NOT NULL UNIQUE,
  `phone` VARCHAR(20) DEFAULT NULL,
  `password` VARCHAR(255) NOT NULL,
  `profile_image_url` VARCHAR(500) DEFAULT NULL,
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Create journals table
CREATE TABLE IF NOT EXISTS `journals` (
  `id` INT(11) NOT NULL AUTO_INCREMENT,
  `user_id` INT(11) NOT NULL,
  `title` VARCHAR(255) NOT NULL,
  `content` TEXT NOT NULL,
  `image_url` VARCHAR(500) DEFAULT NULL,
  `thumbnail_url` VARCHAR(500) DEFAULT NULL,
  `is_public` TINYINT(1) DEFAULT 0,
  `date` BIGINT(20) NOT NULL,
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_date` (`date`),
  KEY `idx_public` (`is_public`),
  CONSTRAINT `fk_journals_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Create followers table (for future use)
CREATE TABLE IF NOT EXISTS `followers` (
  `id` INT(11) NOT NULL AUTO_INCREMENT,
  `follower_id` INT(11) NOT NULL,
  `following_id` INT(11) NOT NULL,
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_follow` (`follower_id`, `following_id`),
  KEY `idx_follower` (`follower_id`),
  KEY `idx_following` (`following_id`),
  CONSTRAINT `fk_follower_user` FOREIGN KEY (`follower_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_following_user` FOREIGN KEY (`following_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

4. **Paste the code** into the text area
5. **Click:** "Go" button (bottom right)
6. **Wait:** 2-3 seconds
7. **You should see:** Green success message "3 queries executed successfully"

### Step 7: Verify Tables Created

1. **Click:** Your database name in left sidebar
2. **You should see 3 tables:**
   - ✅ `users`
   - ✅ `journals`
   - ✅ `followers`

3. **Click each table** to verify structure:
   - Click `users` → See columns: id, name, email, phone, password, etc.
   - Click `journals` → See columns: id, user_id, title, content, etc.
   - Click `followers` → See columns: id, follower_id, following_id, etc.

✅ **Database is now ready!**

---

## PART 5: UPLOAD PHP FILES

### Step 8: Access File Manager

1. **Go back to:** AwardSpace Control Panel
2. **Find:** "File Manager" or "FTP Manager"
3. **Click:** "File Manager"
4. **You'll see:** Your hosting file structure

### Step 9: Create Folder Structure

1. **Navigate to:** `public_html` folder
2. **Click:** "New Folder" button
3. **Name it:** `soulwrite`
4. **Click:** "Create"
5. **Open:** The `soulwrite` folder you just created

### Step 10: Upload PHP Files

**You need to upload these 9 files from your `backend/` folder:**

1. **Select:** "Upload Files" button
2. **Upload each file one by one:**
   - ✅ `config.php`
   - ✅ `register.php`
   - ✅ `login.php`
   - ✅ `add_journal.php`
   - ✅ `get_journals.php`
   - ✅ `search_journals.php`
   - ✅ `update_journal.php`
   - ✅ `delete_journal.php`
   - ✅ `upload_image.php`

3. **Wait for each upload** to complete

### Step 11: Create Uploads Folder

1. **Still in the `soulwrite` folder**
2. **Click:** "New Folder"
3. **Name:** `uploads`
4. **Click:** "Create"
5. **Right-click** the `uploads` folder
6. **Select:** "Change Permissions" or "chmod"
7. **Set permissions to:** `755`
   - Owner: Read, Write, Execute
   - Group: Read, Execute
   - Others: Read, Execute
8. **Click:** "Save" or "Apply"

✅ **Folder structure complete!**

```
public_html/
└── soulwrite/
    ├── config.php
    ├── register.php
    ├── login.php
    ├── add_journal.php
    ├── get_journals.php
    ├── search_journals.php
    ├── update_journal.php
    ├── delete_journal.php
    ├── upload_image.php
    └── uploads/  (chmod 755)
```

---

## PART 6: CONFIGURE DATABASE CONNECTION

### Step 12: Edit config.php

1. **In File Manager, find:** `config.php`
2. **Right-click:** `config.php`
3. **Select:** "Edit" or "Code Edit"
4. **You'll see the code:**

```php
<?php
$host = "your_host.awardspace.com";
$username = "your_username";
$password = "your_password";
$database = "your_database";
?>
```

5. **Replace with YOUR details** (from Step 3):

```php
<?php
$host = "mysql333.awardspace.com";        // Your actual host
$username = "4385921_soulwrite";          // Your actual username
$password = "YourPasswordHere";           // Your actual password
$database = "4385921_soulwrite_db";       // Your actual database name

// Create connection
$conn = new mysqli($host, $username, $password, $database);

// Check connection
if ($conn->connect_error) {
    die(json_encode(array("success" => false, "message" => "Connection failed: " . $conn->connect_error)));
}

// Set charset to utf8
$conn->set_charset("utf8");
?>
```

6. **Click:** "Save" button
7. **Close** the editor

✅ **Database connection configured!**

---

## PART 7: TEST THE SETUP

### Step 13: Test Database Connection

1. **Open your browser**
2. **Go to:** `http://yourdomain.awardspace.com/soulwrite/config.php`
   - Replace `yourdomain` with your actual AwardSpace domain
3. **You should see:** Blank page (no errors = good!)
4. **If you see errors:** Check your database credentials

### Step 14: Test Registration API

1. **Install Postman** (or use browser)
2. **Create POST request to:**
   ```
   http://yourdomain.awardspace.com/soulwrite/register.php
   ```

3. **Add these parameters:**
   ```
   name: Test User
   email: test@example.com
   phone: 1234567890
   password: test123
   ```

4. **Send the request**
5. **You should get:**
   ```json
   {
     "success": true,
     "message": "Registration successful",
     "user_id": "1"
   }
   ```

6. **Verify in database:**
   - Go to phpMyAdmin
   - Click `users` table
   - Click "Browse"
   - You should see your test user ✅

### Step 15: Test Login API

1. **Create POST request to:**
   ```
   http://yourdomain.awardspace.com/soulwrite/login.php
   ```

2. **Add parameters:**
   ```
   email: test@example.com
   password: test123
   ```

3. **Send the request**
4. **You should get:**
   ```json
   {
     "success": true,
     "message": "Login successful",
     "user": {
       "id": "1",
       "name": "Test User",
       "email": "test@example.com",
       "phone": "1234567890"
     }
   }
   ```

✅ **APIs are working!**

---

## PART 8: UPDATE ANDROID APP

### Step 16: Update API URLs

1. **Open Android Studio**
2. **Navigate to:** `app/src/main/java/com/uh/smdprojectsoulwrite/ApiHelper.kt`
3. **Find line 11:**
   ```kotlin
   private const val BASE_URL = "http://your-awardspace-url.com/soulwrite/"
   ```

4. **Replace with YOUR actual URL:**
   ```kotlin
   private const val BASE_URL = "http://yourdomain.awardspace.com/soulwrite/"
   ```
   
   Example:
   ```kotlin
   private const val BASE_URL = "http://soulwrite-usman.awardspace.com/soulwrite/"
   ```

5. **Save the file**

### Step 17: Sync and Build

1. **Click:** File → Sync Project with Gradle Files
2. **Wait** for sync to complete
3. **Click:** Build → Rebuild Project
4. **Wait** for build to complete
5. **No errors?** You're ready! ✅

---

## PART 9: TEST THE COMPLETE APP

### Step 18: Run on Device

1. **Connect phone** or start emulator
2. **Click:** Run button (green triangle)
3. **Wait** for app to install

### Step 19: Test Registration

1. **Open app** (Splash screen appears)
2. **Click:** "Sign Up"
3. **Fill in:**
   - Name: Your Name
   - Email: yourname@email.com
   - Phone: 1234567890
   - Password: password123
4. **Click:** "Signup"
5. **Wait 2-3 seconds**
6. **Success?** You're logged in! ✅

7. **Verify in database:**
   - Go to phpMyAdmin
   - Check `users` table
   - Your account should be there ✅

### Step 20: Test Journal Creation

1. **Click:** FAB (+) button
2. **Enter:**
   - Title: My First Journal
   - Content: This is a test journal entry
3. **Click:** "Select Image" (optional)
4. **Choose** an image from gallery
5. **Privacy Setting:**
   - ☐ Unchecked = Private (only you can see)
   - ☑ Checked = Public (everyone can see and search)
6. **Click:** "Save Entry"
7. **Success?** Journal appears in list! ✅

7. **Verify in database:**
   - Go to phpMyAdmin
   - Check `journals` table
   - Your journal should be there ✅

8. **Verify image upload:**
   - Go to File Manager
   - Navigate to: `soulwrite/uploads/journals/`
   - Your image should be there ✅
   - Thumbnail should also be there (thumb_*.jpg) ✅

### Step 21: Test Search

1. **Click:** Search icon
2. **Type:** "First"
3. **Results appear?** Working! ✅

### Step 22: Test Offline Mode

1. **Turn on:** Airplane mode
2. **Open app**
3. **Journals still visible?** ✅
4. **Turn off:** Airplane mode
5. **Pull to refresh**
6. **Data syncs?** ✅

---

## TROUBLESHOOTING

### Problem: Database connection failed

**Solution:**
1. Check `config.php` credentials are correct
2. Verify database exists in phpMyAdmin
3. Check database host is correct
4. Ensure no typos in credentials

### Problem: PHP files not found

**Solution:**
1. Verify files are in correct folder: `public_html/soulwrite/`
2. Check file names are exactly as listed
3. Ensure no extra spaces in filenames
4. Try re-uploading the files

### Problem: Image upload fails

**Solution:**
1. Check `uploads/` folder exists
2. Verify folder permissions are 755
3. Check PHP upload_max_filesize setting
4. Try with smaller image (< 2MB)

### Problem: App can't connect to server

**Solution:**
1. Check BASE_URL in ApiHelper.kt is correct
2. Verify domain is accessible in browser
3. Check internet connection
4. Try pinging your domain

### Problem: Registration works but login fails

**Solution:**
1. Check password in database is hashed
2. Verify login.php password_verify() is working
3. Test with the exact same credentials
4. Check for extra spaces in email/password

---

## SUMMARY CHECKLIST

Use this checklist to verify everything is set up:

### AwardSpace Setup:
- [ ] Account created
- [ ] MySQL database created
- [ ] Database credentials saved
- [ ] phpMyAdmin accessible
- [ ] Tables created (users, journals, followers)
- [ ] PHP files uploaded to soulwrite/ folder
- [ ] uploads/ folder created with 755 permissions
- [ ] config.php edited with correct credentials

### Testing:
- [ ] config.php loads without errors
- [ ] register.php works (tested with Postman)
- [ ] login.php works (tested with Postman)
- [ ] User appears in database after registration
- [ ] Android app BASE_URL updated
- [ ] App compiled without errors

### App Testing:
- [ ] App opens successfully
- [ ] Can register new user
- [ ] Can login
- [ ] Can create journal
- [ ] Can upload image
- [ ] Journal appears in list
- [ ] Can search journals
- [ ] Offline mode works
- [ ] Data syncs when online

---

## YOUR SETUP DETAILS

**Fill this in for your reference:**

```
AwardSpace Domain: _______________________________

MySQL Host: _______________________________

Database Name: _______________________________

Database Username: _______________________________

Database Password: _______________________________

API Base URL: http://_______________.awardspace.com/soulwrite/

File Manager Login: _______________________________
```

---

## 🎉 CONGRATULATIONS!

Your SoulWrite app is now fully connected to AwardSpace MySQL database!

**What you have:**
- ✅ MySQL database on cloud
- ✅ PHP REST APIs
- ✅ Image upload to server
- ✅ Local SQLite caching
- ✅ Automatic data sync
- ✅ FCM push notifications

**You can now:**
- Register and login users
- Create and manage journals
- Upload images
- Search journals
- Use offline
- Receive notifications

**Your app is PRODUCTION READY!** 🚀

