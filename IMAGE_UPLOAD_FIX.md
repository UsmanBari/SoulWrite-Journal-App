# FIX FOR IMAGE DISPLAY ISSUE - 403 Forbidden

## Problem:
Images upload successfully but show 403 Forbidden error when trying to display them.

## Root Causes:
1. **Wrong URL construction** - PHP was creating URLs like:
   ```
   http://barisoulwrite.atwebpages.com/backend/uploads/journals/image.jpg
   ```
   Should be:
   ```
   http://barisoulwrite.atwebpages.com/backend/uploads/journals/image.jpg
   ```
   (Actually this is correct, but server needs proper permissions)

2. **Server folder permissions** - The `uploads/journals/` folder needs:
   - Folder permissions: `755` (rwxr-xr-x)
   - File permissions: `644` (rw-r--r--)

## Files to Upload to Server:

### 1. Upload Fixed PHP File:
**File:** `backend/upload_image.php`
**Upload to:** `barisoulwrite.atwebpages.com/backend/upload_image.php`
**What changed:** Fixed URL construction to use correct paths

### 2. Upload .htaccess File:
**File:** `backend/uploads/.htaccess`
**Upload to:** `barisoulwrite.atwebpages.com/backend/uploads/.htaccess`
**What it does:** Ensures images can be accessed via HTTP

## Steps to Fix on Server:

### Via File Manager (AwardSpace Control Panel):

1. **Login to AwardSpace**
   - Go to: https://www.awardspace.com/
   - Login with your credentials

2. **Open File Manager**
   - Click "File Manager" in control panel

3. **Navigate to backend folder**
   - Go to: `public_html/backend/` (or wherever your backend is)

4. **Upload Fixed Files**
   - Upload `upload_image.php` (overwrite existing)
   - Navigate to `uploads/` folder
   - Upload `.htaccess` file

5. **Set Folder Permissions**
   - Right-click `uploads` folder → Properties/Permissions
   - Set to: `755` (or check: Owner: rwx, Group: r-x, Public: r-x)
   - Click "Apply to subfolders"

6. **Set File Permissions** (if images already exist)
   - Go into `uploads/journals/` folder
   - Select all image files
   - Right-click → Properties/Permissions
   - Set to: `644` (or check: Owner: rw-, Group: r--, Public: r--)

### Via FTP (Alternative Method):

1. **Connect via FTP Client** (FileZilla, etc.)
   - Host: Your FTP hostname from AwardSpace
   - Username: Your FTP username
   - Password: Your FTP password

2. **Navigate to backend folder**

3. **Upload Files**
   - Upload `upload_image.php` to `backend/`
   - Upload `.htaccess` to `backend/uploads/`

4. **Set Permissions**
   - Right-click `uploads` folder → File permissions → 755
   - Right-click `uploads/journals` folder → File permissions → 755
   - Right-click each image file → File permissions → 644

## Test After Uploading:

1. **Test direct image access:**
   Open in browser:
   ```
   http://barisoulwrite.atwebpages.com/backend/uploads/journals/
   ```
   Should show "Forbidden" (this is correct - prevents directory listing)

2. **Test specific image:**
   If you have an image like `journal_123456_abc.jpg`, try:
   ```
   http://barisoulwrite.atwebpages.com/backend/uploads/journals/journal_123456_abc.jpg
   ```
   Should show the image (not 403)

3. **Upload new journal with image in app:**
   - Add new journal with image
   - Save
   - Should show "Image uploaded successfully"
   - Open journal
   - Image should now display!

## Expected Results:

✅ Images upload successfully
✅ URLs are correctly constructed
✅ Images display in journal list (thumbnails)
✅ Images display in journal detail (full size)
✅ No more 403 Forbidden errors

## If Still Not Working:

### Check Server Error Logs:
- In AwardSpace control panel, check PHP error logs
- Look for permission denied errors

### Check Image URLs:
- Add a journal with image
- Check logcat for the URL returned
- Try opening that URL in a browser
- If 403, permissions are still wrong
- If 404, path is wrong

### Common Issues:

**403 Forbidden:**
- Folder permissions too restrictive
- .htaccess blocking access
- Check folders: `uploads/` and `uploads/journals/` both need 755

**404 Not Found:**
- Wrong path in URL
- Uploads folder doesn't exist
- Check the PHP file is creating the correct path

**Image uploads but doesn't save:**
- Check `uploads/journals/` folder exists
- Check write permissions (folder needs 755 or 777)

## Summary:

1. ✅ Fixed `upload_image.php` - correct URL paths
2. ✅ Created `.htaccess` - allow image access
3. 📤 **YOU NEED TO:** Upload these 2 files to server
4. 📤 **YOU NEED TO:** Set folder permissions to 755
5. 📤 **YOU NEED TO:** Set file permissions to 644

Once you upload these files and fix permissions, images will display correctly!

