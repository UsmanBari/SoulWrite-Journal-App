# Bottom Navigation Implementation Guide

## Overview
The bottom navigation bar is consistently implemented across all main screens (Home, Search, Notifications, Profile) with proper navigation handling.

## Bottom Navigation Layout Structure

```xml
<LinearLayout
    android:id="@+id/bottom_bar"
    android:layout_width="match_parent"
    android:layout_height="70dp"
    android:layout_alignParentBottom="true"
    android:orientation="horizontal"
    android:background="#FFFFFF"
    android:elevation="8dp"
    android:weightSum="4">

    <!-- 4 navigation items: Home, Search, Alerts, Profile -->
</LinearLayout>
```

## Navigation Items

### 1. Home Icon
- **ID:** `home_icon`
- **Navigates to:** `HomeActivity`
- **Tint Color:** Blue when active, Gray when inactive

### 2. Search Icon
- **ID:** Not needed (embedded in Home's search icon)
- **Navigates to:** `SearchActivity`
- **Tint Color:** Gray

### 3. Alerts Icon
- **ID:** `alerts_icon`
- **Navigates to:** `NotificationsActivity`
- **Icon:** `alertsicon.png`

### 4. Profile Icon
- **ID:** `profile_icon`
- **Navigates to:** `ProfileActivity`
- **Icon:** `profileicon.png`

## Implementation in Activities

### HomeActivity
```kotlin
searchIcon.setOnClickListener {
    startActivity(Intent(this, SearchActivity::class.java))
}

alertsIcon.setOnClickListener {
    startActivity(Intent(this, NotificationsActivity::class.java))
}

profileIcon.setOnClickListener {
    startActivity(Intent(this, ProfileActivity::class.java))
}
```

### SearchActivity
```kotlin
homeIcon.setOnClickListener {
    startActivity(Intent(this, HomeActivity::class.java))
    finish()
}

alertsIcon.setOnClickListener {
    startActivity(Intent(this, NotificationsActivity::class.java))
}

profileIcon.setOnClickListener {
    startActivity(Intent(this, ProfileActivity::class.java))
}
```

### NotificationsActivity
```kotlin
homeIcon.setOnClickListener {
    startActivity(Intent(this, HomeActivity::class.java))
    finish()
}

profileIcon.setOnClickListener {
    startActivity(Intent(this, ProfileActivity::class.java))
}
```

### ProfileActivity
```kotlin
homeIcon.setOnClickListener {
    startActivity(Intent(this, HomeActivity::class.java))
    finish()
}

alertsIcon.setOnClickListener {
    startActivity(Intent(this, NotificationsActivity::class.java))
}
```

## Active State Indicators

The active screen is indicated by:
1. Blue tint color for the icon
2. Blue text color for the label
3. Other screens remain gray

### Example (Home is Active)
```xml
<ImageView
    android:src="@drawable/homeicon"
    app:tint="@color/blue" />  <!-- Blue when active -->

<TextView
    android:text="Home"
    android:textColor="@color/blue" />  <!-- Blue when active -->
```

## Floating Action Button (FAB)

On the Home screen, there's a FAB for adding new journals:

```kotlin
fab.setOnClickListener {
    startActivity(Intent(this, AddEntryActivity::class.java))
}
```

Position: Bottom-right, above the bottom navigation bar

## Navigation Flow

```
Splash Screen
    ↓
Login/Signup
    ↓
Home (with bottom nav)
    ├→ Search (with bottom nav)
    ├→ Notifications (with bottom nav)
    ├→ Profile (with bottom nav)
    │   ├→ Edit Profile
    │   └→ Change Password
    ├→ Add Entry (via FAB)
    └→ Detail View
```

## Important Notes

1. **Back Button Behavior:**
   - Detail, Add Entry, Edit Profile, Change Password: Use back button to return
   - Main screens (Home, Search, Notifications, Profile): Don't use finish() when navigating between them

2. **Current Screen Highlighting:**
   - Each activity should highlight its own icon in the bottom nav
   - Update XML layouts to reflect the active state

3. **Consistency:**
   - All main screens should have the same bottom navigation structure
   - Same click listeners across all screens
   - Same styling and dimensions

## Testing Navigation

Test these flows:
1. Home → Search → Home → Notifications → Profile → Home
2. Home → FAB → Add Entry → Back to Home
3. Home → Journal Item → Detail → Back to Home
4. Profile → Edit Profile → Save → Back to Profile → Home
5. Any Screen → Back Button (should work correctly)

## Common Issues

### Issue: Bottom nav not visible
**Solution:** Check that the layout uses RelativeLayout and `layout_above="@id/bottom_bar"`

### Issue: Icons not clickable
**Solution:** Ensure `android:clickable="true"` and `android:focusable="true"` are set

### Issue: Navigation not working
**Solution:** Check that IDs match between XML and Kotlin code

### Issue: Back button closes app from Home
**Solution:** Override onBackPressed() in HomeActivity to show exit confirmation

## Color References

- **Blue (Active):** `#413DB6`
- **Gray (Inactive):** `#888888`
- **White (Background):** `#FFFFFF`
- **Black (Text):** `#000000`

