# ✅ QUICK SETUP CHECKLIST - After Database Creation

## Current Status: ✅ MySQL Database Created

---

## TODO LIST (Complete in Order):

### ☐ STEP 1: Create Database Tables (5 minutes)
- [ ] Open phpMyAdmin from AwardSpace control panel
- [ ] Click your database name on left sidebar
- [ ] Click "SQL" tab
- [ ] Copy SQL from `backend/database_schema.sql`
- [ ] Paste into text box
- [ ] Click "Go"
- [ ] Verify success message: "3 queries executed"
- [ ] Check left sidebar shows: users, journals, followers tables

**Result:** 3 tables created ✅

---

### ☐ STEP 2: Upload PHP Files (10 minutes)
- [ ] Open File Manager in AwardSpace
- [ ] Navigate to `public_html/`
- [ ] Create new folder: `soulwrite`
- [ ] Open `soulwrite` folder
- [ ] Upload these 9 files from `backend/` folder:
  - [ ] config.php
  - [ ] register.php
  - [ ] login.php
  - [ ] add_journal.php
  - [ ] get_journals.php
  - [ ] search_journals.php
  - [ ] update_journal.php
  - [ ] delete_journal.php
  - [ ] upload_image.php
- [ ] Create folder: `uploads`
- [ ] Set uploads folder permissions to 755

**Result:** All PHP files uploaded ✅

---

### ☐ STEP 3: Configure Database Connection (2 minutes)
- [ ] In File Manager, open `config.php` for editing
- [ ] Replace these values with YOUR credentials:
  - [ ] `$host = "your actual mysql host";`
  - [ ] `$username = "your actual username";`
  - [ ] `$password = "your actual password";`
  - [ ] `$database = "your actual database name";`
- [ ] Save file
- [ ] Test by opening: `http://yourdomain.com/soulwrite/config.php`
- [ ] Should see blank page (no errors)

**Result:** Database connected ✅

---

### ☐ STEP 4: Update Android App (2 minutes)
- [ ] Open Android Studio
- [ ] Open: `app/src/main/java/com/uh/smdprojectsoulwrite/ApiHelper.kt`
- [ ] Find line: `private const val BASE_URL =`
- [ ] Change to: `"http://yourdomain.awardspace.com/soulwrite/"`
- [ ] Save file
- [ ] Click: File → Sync Project with Gradle Files

**Result:** App configured ✅

---

### ☐ STEP 5: Test the App (5 minutes)
- [ ] Click Run button (green ▶️)
- [ ] Wait for app to install
- [ ] Click "Sign Up"
- [ ] Fill in test user details
- [ ] Click Signup
- [ ] Should see home screen ✅

**Result:** App working ✅

---

### ☐ STEP 6: Verify Database (2 minutes)
- [ ] Go to phpMyAdmin
- [ ] Click `users` table
- [ ] Click "Browse"
- [ ] See your test user ✅
- [ ] Create a journal in app
- [ ] Check `journals` table
- [ ] See your journal ✅

**Result:** Data saving to MySQL ✅

---

## 🎯 YOUR CREDENTIALS (Fill In):

```
AwardSpace Domain: _________________________________

MySQL Host: _________________________________

Database Name: _________________________________

Username: _________________________________

Password: _________________________________

API Base URL: http://_____________.awardspace.com/soulwrite/
```

---

## 📋 VERIFICATION CHECKLIST:

After completing all steps, verify:

- [ ] 3 tables exist in phpMyAdmin (users, journals, followers)
- [ ] 9 PHP files are in public_html/soulwrite/
- [ ] uploads/ folder exists with 755 permissions
- [ ] config.php has correct database credentials
- [ ] ApiHelper.kt has correct BASE_URL
- [ ] App builds without errors
- [ ] Can register new user
- [ ] User appears in MySQL database
- [ ] Can create journal
- [ ] Journal appears in MySQL database
- [ ] Can upload image
- [ ] Image appears in uploads/journals/ folder

---

## 🚀 WHEN ALL CHECKED:

**YOUR APP IS PRODUCTION READY!** ✅

You now have:
- ✅ Full MySQL backend
- ✅ User authentication
- ✅ Journal management
- ✅ Image uploads
- ✅ Privacy controls
- ✅ Search functionality
- ✅ Offline support
- ✅ Push notifications

---

## 📞 NEED HELP?

If stuck on any step:
1. Check AWARDSPACE_SETUP_GUIDE.md (detailed instructions)
2. Check PRIVACY_FEATURE.md (privacy system explained)
3. Check Android Logcat for errors
4. Check PHP error logs on AwardSpace

---

**START WITH STEP 1 - Create Database Tables!** 🎯

