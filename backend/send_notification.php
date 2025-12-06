<?php
/**
 * Send FCM Push Notification
 * Uses Firebase Cloud Messaging v1 API with service account authentication
 */

/**
 * Get OAuth2 access token using service account
 */
function getAccessToken() {
    // Load service account credentials from JSON file
    $json_file = __DIR__ . '/services_json.json';

    if (!file_exists($json_file)) {
        error_log("FCM: services_json.json not found at: $json_file");
        return null;
    }

    $service_account = json_decode(file_get_contents($json_file), true);

    if (!$service_account) {
        error_log("FCM: Failed to parse services_json.json");
        return null;
    }

    $now = time();

    // Create JWT header
    $jwt_header = str_replace(['+', '/', '='], ['-', '_', ''], base64_encode(json_encode([
        'alg' => 'RS256',
        'typ' => 'JWT'
    ])));

    // Create JWT claim
    $jwt_claim = str_replace(['+', '/', '='], ['-', '_', ''], base64_encode(json_encode([
        'iss' => $service_account['client_email'],
        'scope' => 'https://www.googleapis.com/auth/firebase.messaging',
        'aud' => $service_account['token_uri'],
        'iat' => $now,
        'exp' => $now + 3600
    ])));

    // Sign with private key
    $signature_input = $jwt_header . '.' . $jwt_claim;
    openssl_sign($signature_input, $signature, $service_account['private_key'], 'SHA256');
    $jwt_signature = str_replace(['+', '/', '='], ['-', '_', ''], base64_encode($signature));

    $jwt = $signature_input . '.' . $jwt_signature;

    // Exchange JWT for access token
    $ch = curl_init($service_account['token_uri']);
    curl_setopt($ch, CURLOPT_POST, true);
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
    curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, false);
    curl_setopt($ch, CURLOPT_POSTFIELDS, http_build_query([
        'grant_type' => 'urn:ietf:params:oauth:grant-type:jwt-bearer',
        'assertion' => $jwt
    ]));

    $response = curl_exec($ch);
    curl_close($ch);

    $token_data = json_decode($response, true);
    return $token_data['access_token'] ?? null;
}

function sendPushNotification($user_id, $title, $message, $data = []) {
    global $conn;

    // Get user's FCM token
    $stmt = $conn->prepare("SELECT fcm_token FROM users WHERE id = ?");
    $stmt->bind_param("i", $user_id);
    $stmt->execute();
    $result = $stmt->get_result();

    if ($result->num_rows === 0) {
        return false;
    }

    $user = $result->fetch_assoc();
    $fcm_token = $user['fcm_token'];

    if (empty($fcm_token)) {
        return false; // User hasn't registered FCM token
    }

    // Get OAuth2 access token
    $access_token = getAccessToken();
    if (!$access_token) {
        error_log("Failed to get FCM access token");
        return false;
    }

    // Prepare notification payload for FCM v1 API
    $fcm_data = [
        'message' => [
            'token' => $fcm_token,
            'notification' => [
                'title' => $title,
                'body' => $message
            ],
            'data' => $data,
            'android' => [
                'priority' => 'high',
                'notification' => [
                    'sound' => 'default',
                    'notification_count' => 1
                ]
            ]
        ]
    ];

    // Send via FCM v1 API
    $ch = curl_init();
    curl_setopt($ch, CURLOPT_URL, 'https://fcm.googleapis.com/v1/projects/smdprojectsoulwrite/messages:send');
    curl_setopt($ch, CURLOPT_POST, true);
    curl_setopt($ch, CURLOPT_HTTPHEADER, [
        'Authorization: Bearer ' . $access_token,
        'Content-Type: application/json'
    ]);
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
    curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, false);
    curl_setopt($ch, CURLOPT_POSTFIELDS, json_encode($fcm_data));

    $result = curl_exec($ch);
    $http_code = curl_getinfo($ch, CURLINFO_HTTP_CODE);
    curl_close($ch);

    // Log the result
    error_log("FCM Response: " . $result . " (HTTP " . $http_code . ")");

    return $http_code === 200;
}

// Update user's FCM token
function updateFCMToken($user_id, $fcm_token) {
    global $conn;

    $stmt = $conn->prepare("UPDATE users SET fcm_token = ? WHERE id = ?");
    $stmt->bind_param("si", $fcm_token, $user_id);

    return $stmt->execute();
}
?>

