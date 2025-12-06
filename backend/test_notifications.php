<?php
// Test notification system
header('Content-Type: text/plain; charset=utf-8');
require_once 'config.php';

echo "=== NOTIFICATION SYSTEM TEST ===\n\n";

// Test 1: Check if users have FCM tokens
echo "1. Checking FCM Tokens:\n";
$result = $conn->query("SELECT id, name, email,
    CASE WHEN fcm_token IS NOT NULL AND fcm_token != '' THEN 'YES' ELSE 'NO' END as has_token,
    LEFT(fcm_token, 40) as token_preview
FROM users ORDER BY id");

if ($result && $result->num_rows > 0) {
    while ($row = $result->fetch_assoc()) {
        echo "  User #{$row['id']}: {$row['name']} ({$row['email']})\n";
        echo "    Has FCM Token: {$row['has_token']}\n";
        if ($row['has_token'] == 'YES') {
            echo "    Token Preview: {$row['token_preview']}...\n";
        }
        echo "\n";
    }
} else {
    echo "  ❌ No users found\n\n";
}

// Test 2: Check recent notifications
echo "2. Recent Notifications:\n";
$result = $conn->query("
    SELECT n.id, n.type, n.title, n.message, n.is_read,
        u1.name as recipient,
        u2.name as sender,
        n.created_at,
        n.data
    FROM notifications n
    LEFT JOIN users u1 ON n.user_id = u1.id
    LEFT JOIN users u2 ON n.from_user_id = u2.id
    ORDER BY n.created_at DESC
    LIMIT 10
");

if ($result && $result->num_rows > 0) {
    while ($row = $result->fetch_assoc()) {
        echo "  Notification #{$row['id']}:\n";
        echo "    Type: {$row['type']}\n";
        echo "    To: {$row['recipient']}\n";
        echo "    From: {$row['sender']}\n";
        echo "    Title: {$row['title']}\n";
        echo "    Message: {$row['message']}\n";
        echo "    Read: " . ($row['is_read'] ? 'YES' : 'NO') . "\n";
        echo "    Created: {$row['created_at']}\n";
        echo "    Data: {$row['data']}\n";
        echo "\n";
    }
} else {
    echo "  ❌ No notifications found\n\n";
}

// Test 3: Check notification counts by type
echo "3. Notification Counts by Type:\n";
$result = $conn->query("
    SELECT type, COUNT(*) as count
    FROM notifications
    GROUP BY type
    ORDER BY count DESC
");

if ($result && $result->num_rows > 0) {
    while ($row = $result->fetch_assoc()) {
        echo "  {$row['type']}: {$row['count']}\n";
    }
} else {
    echo "  ❌ No notifications\n";
}
echo "\n";

// Test 4: Check comments
echo "4. Recent Comments:\n";
$result = $conn->query("
    SELECT c.id, c.comment_text,
        u.name as commenter,
        j.title as journal_title,
        j.user_id as journal_owner_id,
        c.created_at
    FROM journal_comments c
    LEFT JOIN users u ON c.user_id = u.id
    LEFT JOIN journals j ON c.journal_id = j.id
    ORDER BY c.created_at DESC
    LIMIT 10
");

if ($result && $result->num_rows > 0) {
    while ($row = $result->fetch_assoc()) {
        echo "  Comment #{$row['id']}:\n";
        echo "    By: {$row['commenter']}\n";
        echo "    On: {$row['journal_title']} (Owner ID: {$row['journal_owner_id']})\n";
        echo "    Text: {$row['comment_text']}\n";
        echo "    Created: {$row['created_at']}\n";
        echo "\n";
    }
} else {
    echo "  ❌ No comments found\n\n";
}

// Test 5: Check if services_json.json exists
echo "5. Checking FCM Configuration:\n";
$json_file = __DIR__ . '/services_json.json';
if (file_exists($json_file)) {
    echo "  ✅ services_json.json exists\n";
    $json = json_decode(file_get_contents($json_file), true);
    if ($json && isset($json['project_id'])) {
        echo "  ✅ Valid JSON format\n";
        echo "    Project ID: {$json['project_id']}\n";
        echo "    Client Email: {$json['client_email']}\n";
    } else {
        echo "  ❌ Invalid JSON format\n";
    }
} else {
    echo "  ❌ services_json.json NOT FOUND\n";
    echo "    This file is REQUIRED for push notifications!\n";
}
echo "\n";

// Test 6: Test notification creation
echo "6. Testing Notification Creation:\n";
echo "  (Will create a test notification for user 1)\n";

$test_user_id = 1;
$test_from_user = 2;
$test_title = "Test Notification";
$test_message = "This is a test notification from the test script";
$test_data = json_encode(['type' => 'test', 'test_id' => '123']);

$stmt = $conn->prepare("INSERT INTO notifications (user_id, from_user_id, type, title, message, data) VALUES (?, ?, 'test', ?, ?, ?)");
$stmt->bind_param("iisss", $test_user_id, $test_from_user, $test_title, $test_message, $test_data);

if ($stmt->execute()) {
    $test_notif_id = $stmt->insert_id;
    echo "  ✅ Test notification created (ID: $test_notif_id)\n";

    // Try to send FCM
    if (file_exists(__DIR__ . '/send_notification.php')) {
        require_once __DIR__ . '/send_notification.php';
        echo "  ✅ send_notification.php found\n";

        $fcm_result = @sendPushNotification($test_user_id, $test_title, $test_message, ['type' => 'test']);
        if ($fcm_result) {
            echo "  ✅ FCM notification sent successfully!\n";
        } else {
            echo "  ❌ FCM notification failed (check error logs)\n";
        }
    } else {
        echo "  ❌ send_notification.php NOT FOUND\n";
    }
} else {
    echo "  ❌ Failed to create test notification: " . $conn->error . "\n";
}

echo "\n=== TEST COMPLETE ===\n";

$conn->close();
?>

