# 🔒 JOURNAL PRIVACY FEATURE DOCUMENTATION

## Overview

The SoulWrite app includes a **complete privacy system** that allows users to control who can see their journals.

---

## 📊 How It Works

### Database Structure

The `journals` table includes:
```sql
`is_public` TINYINT(1) DEFAULT 0
```

- **0 = Private** (default) - Only the author can see
- **1 = Public** - Everyone can see and search

---

## 🎯 Feature Implementation

### 1. ✅ **Creating a Journal**

#### In the App:
When adding a new journal, users see:

```
┌─────────────────────────────────┐
│  New Entry                      │
├─────────────────────────────────┤
│  Title: [.....................]  │
│                                 │
│  Content:                       │
│  [...........................]  │
│  [...........................]  │
│                                 │
│  [Select Image]                 │
│                                 │
│  ☐ Make journal public          │ ← Privacy Checkbox
│                                 │
│  [Save Entry]                   │
└─────────────────────────────────┘
```

**Checkbox Options:**
- **Unchecked (☐)** = Private journal (is_public = 0)
- **Checked (☑)** = Public journal (is_public = 1)

#### In the Code:
```kotlin
// AddEntryActivity.kt
val isPublic = publicCheckBox.isChecked  // Gets checkbox state
```

#### In the Database:
```sql
INSERT INTO journals (..., is_public) VALUES (..., 0 or 1)
```

---

### 2. ✅ **Viewing Journals (Home Screen)**

#### Privacy Rules:
1. **User's Own Journals:**
   - ✅ Can see ALL their journals (private + public)
   
2. **Other Users' Journals:**
   - ✅ Can see ONLY public journals (is_public = 1)
   - ❌ Cannot see private journals (is_public = 0)

#### SQL Query:
```sql
-- get_journals.php
SELECT j.*, u.name as user_name
FROM journals j
JOIN users u ON j.user_id = u.id
WHERE (
    j.user_id = ?              -- All journals by this user
    OR 
    (j.is_public = 1 AND j.user_id != ?)  -- Public journals by others
)
ORDER BY j.date DESC
```

#### Example Scenario:

**User A creates:**
- Journal 1: "My Secret Diary" - Private ☐
- Journal 2: "Trip to Mountains" - Public ☑
- Journal 3: "Personal Thoughts" - Private ☐

**User B creates:**
- Journal 4: "Cooking Tips" - Public ☑
- Journal 5: "Private Notes" - Private ☐

**What User A sees on Home:**
- ✅ Journal 1 (own, private)
- ✅ Journal 2 (own, public)
- ✅ Journal 3 (own, private)
- ✅ Journal 4 (other user, public)
- ❌ Journal 5 (other user, private) ← Hidden

**What User B sees on Home:**
- ✅ Journal 2 (other user, public)
- ✅ Journal 4 (own, public)
- ✅ Journal 5 (own, private)
- ❌ Journal 1 (other user, private) ← Hidden
- ❌ Journal 3 (other user, private) ← Hidden

---

### 3. ✅ **Searching Journals**

#### Privacy Rules:
- **Search ONLY shows PUBLIC journals**
- Private journals are NEVER included in search results
- Even if the search query matches, private journals stay hidden

#### SQL Query:
```sql
-- search_journals.php
SELECT j.*, u.name as user_name
FROM journals j
JOIN users u ON j.user_id = u.id
WHERE (j.title LIKE ? OR j.content LIKE ?)
  AND j.is_public = 1  -- ← Only public journals
ORDER BY j.date DESC
LIMIT 50
```

#### Example:

**Database contains:**
- "Secret Recipe" - Private ☐
- "Public Recipe" - Public ☑
- "Secret Tips" - Private ☐
- "Travel Secrets" - Public ☑

**User searches for "secret":**

**Results shown:**
- ✅ "Travel Secrets" (public, contains "secret")
- ❌ "Secret Recipe" (private, hidden)
- ❌ "Secret Tips" (private, hidden)

---

## 🔐 Security & Privacy

### What's Protected:

1. **Private Journals:**
   - ✅ Not visible to other users
   - ✅ Not searchable by others
   - ✅ Not listed in any public feed
   - ✅ Only accessible by the author

2. **Public Journals:**
   - ✅ Visible to all users
   - ✅ Searchable by everyone
   - ✅ Listed in home feed
   - ✅ Shareable (future feature)

### Database Level Security:

```sql
-- SQL prevents access to private journals
WHERE (
    j.user_id = ?              -- User's own journals
    OR 
    j.is_public = 1            -- Only public from others
)
```

---

## 📱 User Experience

### Creating a Journal:

1. User clicks FAB (+) button
2. Fills in title and content
3. **Sees checkbox: "Make journal public"**
4. **Decides privacy:**
   - Leave unchecked → Private (only I can see)
   - Check the box → Public (everyone can see)
5. Saves journal

### Viewing Journals:

1. **Home Screen:**
   - Shows all my journals (private + public)
   - Shows public journals from others
   - Private journals from others are invisible

2. **Search:**
   - Only public journals appear
   - My private journals don't appear even in my own search
   - Others' private journals never appear

---

## 🎨 Visual Indicators (Optional Enhancement)

You can add visual indicators to show privacy status:

### In Journal List:
```
┌─────────────────────────────┐
│ 🔒 My Secret Diary         │ ← Private (lock icon)
│ Today I felt...            │
│ Oct 5, 2025                │
└─────────────────────────────┘

┌─────────────────────────────┐
│ 🌍 Trip to Mountains       │ ← Public (globe icon)
│ The views were amazing...  │
│ Oct 4, 2025                │
└─────────────────────────────┘
```

### Implementation (Future):
```kotlin
// In JournalAdapter.kt
if (journal.isPublic) {
    iconImageView.setImageResource(R.drawable.ic_public)
} else {
    iconImageView.setImageResource(R.drawable.ic_private)
}
```

---

## 🧪 Testing the Privacy Feature

### Test Case 1: Private Journal
```
1. User A logs in
2. Creates journal "Private Test"
3. UNCHECKS "Make journal public"
4. Saves journal
5. User A sees it on home ✅
6. User B logs in
7. User B does NOT see "Private Test" ✅
8. User B searches "Private" → Not found ✅
```

### Test Case 2: Public Journal
```
1. User A logs in
2. Creates journal "Public Test"
3. CHECKS "Make journal public"
4. Saves journal
5. User A sees it on home ✅
6. User B logs in
7. User B sees "Public Test" on home ✅
8. User B searches "Public" → Found ✅
```

### Test Case 3: Mixed Journals
```
1. User A creates:
   - Journal 1: Private
   - Journal 2: Public
   - Journal 3: Private

2. User A's home shows: All 3 journals ✅

3. User B's home shows: Only Journal 2 ✅

4. User B searches → Only Journal 2 appears ✅
```

---

## 📊 Database Verification

### Check Privacy Status:
```sql
-- In phpMyAdmin
SELECT id, title, user_id, is_public
FROM journals
ORDER BY id DESC;
```

### Expected Results:
```
+----+------------------+---------+-----------+
| id | title            | user_id | is_public |
+----+------------------+---------+-----------+
| 1  | Private Diary    | 1       | 0         | ← Private
| 2  | Public Recipe    | 1       | 1         | ← Public
| 3  | Secret Notes     | 2       | 0         | ← Private
| 4  | Travel Blog      | 2       | 1         | ← Public
+----+------------------+---------+-----------+
```

### Test Queries:

**Get User 1's journals:**
```sql
SELECT * FROM journals WHERE user_id = 1;
-- Returns: Journal 1 & 2 (both private and public)
```

**Get public journals:**
```sql
SELECT * FROM journals WHERE is_public = 1;
-- Returns: Journal 2 & 4 only
```

**Search public journals:**
```sql
SELECT * FROM journals 
WHERE (title LIKE '%recipe%' OR content LIKE '%recipe%')
  AND is_public = 1;
-- Returns: Journal 2 only (if it contains "recipe")
```

---

## 🔄 Summary

### ✅ **What You Have:**

1. **Database Table Ready:**
   - `is_public` column exists
   - Default is 0 (private)
   - Can be set to 1 (public)

2. **UI Checkbox:**
   - "Make journal public" checkbox in add entry screen
   - Easy for users to understand
   - Clear visual indication

3. **Backend Logic:**
   - `get_journals.php` - Filters by privacy
   - `search_journals.php` - Only shows public
   - `add_journal.php` - Saves privacy preference

4. **Privacy Rules:**
   - Users see all their own journals
   - Users see only public journals from others
   - Search only shows public journals
   - Private journals are completely hidden

### 🎯 **Privacy Guarantee:**

- ✅ Private journals = Truly private
- ✅ Public journals = Visible to all
- ✅ Search respects privacy
- ✅ No accidental exposure
- ✅ User controls their privacy

---

## 🚀 **It's Already Working!**

The privacy feature is **fully implemented** in your app:

1. ✅ Database structure
2. ✅ UI checkbox
3. ✅ Backend filtering
4. ✅ Search restrictions
5. ✅ Privacy logic

**Just use it:**
- Create a journal
- Check/uncheck "Make journal public"
- Save
- Test with another user account

**Privacy is automatic!** 🔒

