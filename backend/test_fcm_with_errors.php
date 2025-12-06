<?php
// Test FCM with visible error output
error_reporting(E_ALL);
ini_set('display_errors', 1);

echo "=== FCM SEND TEST WITH ERRORS ===\n\n";

require_once 'config.php';
require_once 'send_notification.php';

echo "1. Getting user 1 info...\n";
$stmt = $conn->prepare("SELECT id, name, fcm_token FROM users WHERE id = 1");
$stmt->execute();
$result = $stmt->get_result();

if ($result->num_rows === 0) {
    echo "  ❌ User not found\n";
    exit;
}

$user = $result->fetch_assoc();
$user_id = $user['id'];
$user_name = $user['name'];
$fcm_token = $user['fcm_token'];

echo "  ✅ User: $user_name (ID: $user_id)\n";
echo "  Token: " . substr($fcm_token, 0, 50) . "...\n\n";

echo "2. Getting access token...\n";
$access_token = getAccessToken();

if (!$access_token) {
    echo "  ❌ Failed to get access token\n";
    exit;
}

echo "  ✅ Got token: " . substr($access_token, 0, 30) . "...\n\n";

echo "3. Preparing FCM payload...\n";
$title = "Test Notification";
$message = "This is a test from error debug script";
$data = ['type' => 'test', 'timestamp' => time()];

$payload = [
    'message' => [
        'token' => $fcm_token,
        'notification' => [
            'title' => $title,
            'body' => $message
        ],
        'data' => array_map('strval', $data),
        'android' => [
            'priority' => 'high',
            'notification' => [
                'sound' => 'default'
            ]
        ]
    ]
];

echo "  Payload prepared\n\n";

echo "4. Sending to FCM...\n";
$url = 'https://fcm.googleapis.com/v1/projects/smdprojectsoulwrite/messages:send';
echo "  URL: $url\n";

$ch = curl_init($url);
curl_setopt_array($ch, [
    CURLOPT_POST => true,
    CURLOPT_RETURNTRANSFER => true,
    CURLOPT_SSL_VERIFYPEER => false,
    CURLOPT_VERBOSE => true,
    CURLOPT_HTTPHEADER => [
        'Authorization: Bearer ' . $access_token,
        'Content-Type: application/json'
    ],
    CURLOPT_POSTFIELDS => json_encode($payload)
]);

$response = curl_exec($ch);
$http_code = curl_getinfo($ch, CURLINFO_HTTP_CODE);
$curl_error = curl_error($ch);

echo "\n5. Response:\n";
echo "  HTTP Code: $http_code\n";

if ($curl_error) {
    echo "  ❌ cURL Error: $curl_error\n\n";
} else {
    echo "  Response Body:\n";
    echo "  " . str_replace("\n", "\n  ", $response) . "\n\n";
}

curl_close($ch);

if ($http_code === 200) {
    echo "✅ SUCCESS! Notification sent!\n";
    echo "Check device for notification.\n";
} else {
    echo "❌ FAILED!\n\n";
    echo "Common issues:\n";
    echo "1. If HTTP 400: Token format or payload issue\n";
    echo "2. If HTTP 401: Access token expired or invalid\n";
    echo "3. If HTTP 403: Firebase project issue\n";
    echo "4. If HTTP 404: Wrong project ID or URL\n";
    echo "5. If HTTP 503: FCM service temporarily down\n";
}

$conn->close();
?>

