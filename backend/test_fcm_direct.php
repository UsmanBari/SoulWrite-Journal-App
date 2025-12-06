<?php
// Test FCM directly
error_reporting(E_ALL);
ini_set('display_errors', 1);

echo "=== FCM DIRECT TEST ===\n\n";

// Load config
require_once 'config.php';

echo "1. Loading send_notification.php...\n";
if (file_exists(__DIR__ . '/send_notification.php')) {
    require_once __DIR__ . '/send_notification.php';
    echo "  ✅ Loaded\n\n";
} else {
    echo "  ❌ NOT FOUND\n";
    exit;
}

echo "2. Testing getAccessToken()...\n";
$token = getAccessToken();
if ($token) {
    echo "  ✅ Got access token: " . substr($token, 0, 50) . "...\n\n";
} else {
    echo "  ❌ Failed to get access token\n";
    echo "  Check if services_json.json has valid private_key\n\n";
}

echo "3. Getting user 1 FCM token...\n";
$stmt = $conn->prepare("SELECT fcm_token FROM users WHERE id = 1");
$stmt->execute();
$result = $stmt->get_result();
if ($result->num_rows > 0) {
    $user = $result->fetch_assoc();
    $fcm_token = $user['fcm_token'];
    echo "  ✅ User 1 token: " . substr($fcm_token, 0, 50) . "...\n\n";
} else {
    echo "  ❌ User 1 not found\n";
    exit;
}

echo "4. Sending test FCM notification...\n";
$title = "Direct FCM Test";
$message = "This is a direct test of FCM push notifications";
$data = ['type' => 'direct_test', 'timestamp' => time()];

// Call sendPushNotification
$result = sendPushNotification(1, $title, $message, $data);

if ($result) {
    echo "  ✅ FCM sent successfully!\n";
    echo "  Check your device for notification.\n\n";
} else {
    echo "  ❌ FCM failed\n";
    echo "  Check error logs above for details.\n\n";
}

echo "5. Checking recent error logs...\n";
echo "  (Look for 'FCM Response' messages)\n\n";

echo "=== TEST COMPLETE ===\n";
echo "\nIf FCM failed, common issues:\n";
echo "1. services_json.json private_key is invalid\n";
echo "2. Firebase project doesn't have FCM enabled\n";
echo "3. Server doesn't have curl/openssl\n";
echo "4. User's FCM token is expired/invalid\n";

$conn->close();
?>

