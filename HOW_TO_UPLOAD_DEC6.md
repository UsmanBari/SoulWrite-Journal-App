# 📤 HOW TO UPLOAD services_json.json TO SERVER

## Option 1: Using FileZilla (Recommended)

### Download FileZilla:
https://filezilla-project.org/download.php?type=client

### Connect to AwardSpace:
1. Open FileZilla
2. Enter these details:
   - **Host**: `ftp.awardspace.net` or `ftp.awardspace.com`
   - **Username**: Your AwardSpace username (check your AwardSpace account)
   - **Password**: Your AwardSpace password
   - **Port**: 21
3. Click "Quickconnect"

### Upload the File:
1. On the **LEFT** side: Navigate to your computer folder:
   ```
   C:\Users\Usman Bari\AndroidStudioProjects\smdprojectsoulwrite\backend\
   ```

2. On the **RIGHT** side: Navigate to server folder:
   ```
   /www/barisoulwrite.atwebpages.com/backend/
   ```

3. Find `services_json.json` on left side

4. **Drag it** from left to right

5. ✅ File should appear on right side (server)

---

## Option 2: Using AwardSpace File Manager

1. Login to AwardSpace: https://www.awardspace.com/

2. Go to **File Manager**

3. Navigate to folder:
   ```
   www/barisoulwrite.atwebpages.com/backend/
   ```

4. Click **Upload** button

5. Select `services_json.json` from:
   ```
   C:\Users\Usman Bari\AndroidStudioProjects\smdprojectsoulwrite\backend\services_json.json
   ```

6. Wait for upload to complete

7. ✅ File should appear in list

---

## Option 3: Using WinSCP

### Download WinSCP:
https://winscp.net/eng/download.php

### Connect:
1. Open WinSCP
2. Choose "FTP" protocol
3. Enter:
   - **Host**: `ftp.awardspace.net`
   - **Username**: Your AwardSpace username
   - **Password**: Your AwardSpace password
4. Click "Login"

### Upload:
1. Navigate on right panel to:
   ```
   /www/barisoulwrite.atwebpages.com/backend/
   ```

2. Navigate on left panel to:
   ```
   C:\Users\Usman Bari\AndroidStudioProjects\smdprojectsoulwrite\backend\
   ```

3. Drag `services_json.json` from left to right

4. ✅ Done!

---

## ✅ VERIFY UPLOAD WORKED:

After uploading, check if file is accessible:

**Visit this URL in your browser:**
```
http://barisoulwrite.atwebpages.com/backend/services_json.json
```

**You should see:**
```json
{
  "type": "service_account",
  "project_id": "smdprojectsoulwrite",
  "private_key_id": "315491eb881f18d7e3bf6a4c79af35c269188618",
  ...
}
```

⚠️ **SECURITY NOTE**: This file contains private keys. Normally you should NOT expose it publicly, but for testing it's okay. In production, you should:
1. Move it outside the public www folder
2. Update PHP files to reference the new location
3. Set proper file permissions

---

## 🔒 FILE PERMISSIONS:

After uploading, make sure the file has correct permissions:

**Using FileZilla:**
1. Right-click `services_json.json` on server (right panel)
2. Click "File Permissions"
3. Set to: **644** (Owner: read+write, Others: read)
4. Click OK

**Using AwardSpace File Manager:**
1. Select the file
2. Click "Permissions"
3. Set to: **644**

---

## 🐛 TROUBLESHOOTING:

### Problem: "Connection refused"
- Check your AwardSpace username and password
- Try different FTP host: `ftp.awardspace.com` instead of `ftp.awardspace.net`

### Problem: "Permission denied"
- Your AwardSpace account may need FTP enabled
- Login to AwardSpace dashboard and check FTP settings

### Problem: File uploads but shows 0 bytes
- Try uploading in **Binary mode** (FileZilla: Transfer → Transfer Type → Binary)

### Problem: Can't see backend folder
- Navigate to: `/www/barisoulwrite.atwebpages.com/backend/`
- Make sure you're in the correct directory

---

## 📋 QUICK CHECKLIST:

Before you start:
- [ ] Know your AwardSpace username
- [ ] Know your AwardSpace password
- [ ] Have FileZilla (or WinSCP) installed
- [ ] Know where services_json.json is on your computer

After upload:
- [ ] File visible in server backend folder
- [ ] File size is NOT 0 bytes
- [ ] File accessible via browser URL
- [ ] File permissions set to 644

---

## ⏱️ TIME REQUIRED:
- Download FileZilla: 2 minutes
- Connect to server: 1 minute
- Upload file: 30 seconds
- **TOTAL: ~5 minutes**

---

**💡 TIP**: Keep FileZilla open after upload - you might need to upload other files later!

