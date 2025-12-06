<?php
/**
 * Send FCM Push Notification - FIXED VERSION
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

    if (!isset($service_account['private_key'])) {
        error_log("FCM: private_key not found in services_json.json");
        return null;
    }

    $now = time();

    // Create JWT header
    $header = [
        'alg' => 'RS256',
        'typ' => 'JWT'
    ];
    $jwt_header = rtrim(strtr(base64_encode(json_encode($header)), '+/', '-_'), '=');

    // Create JWT claim
    $claim = [
        'iss' => $service_account['client_email'],
        'scope' => 'https://www.googleapis.com/auth/firebase.messaging',
        'aud' => $service_account['token_uri'],
        'iat' => $now,
        'exp' => $now + 3600
    ];
    $jwt_claim = rtrim(strtr(base64_encode(json_encode($claim)), '+/', '-_'), '=');

    // Sign with private key
    $signature_input = $jwt_header . '.' . $jwt_claim;

    // Try to get the private key resource
    $private_key = openssl_pkey_get_private($service_account['private_key']);
    if (!$private_key) {
        error_log("FCM: Failed to parse private key: " . openssl_error_string());
        return null;
    }

    $signature = '';
    $sign_result = openssl_sign($signature_input, $signature, $private_key, OPENSSL_ALGO_SHA256);

    if (!$sign_result) {
        error_log("FCM: Failed to sign JWT: " . openssl_error_string());
        return null;
    }

    $jwt_signature = rtrim(strtr(base64_encode($signature), '+/', '-_'), '=');
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
    $http_code = curl_getinfo($ch, CURLINFO_HTTP_CODE);
    curl_close($ch);

    if ($http_code !== 200) {
        error_log("FCM: Token request failed (HTTP $http_code): $response");
        return null;
    }

    $token_data = json_decode($response, true);

    if (!isset($token_data['access_token'])) {
        error_log("FCM: No access_token in response: $response");
        return null;
    }

    return $token_data['access_token'];
}

function sendPushNotification($user_id, $title, $message, $data = []) {
    global $conn;

    // Get user's FCM token
    $stmt = $conn->prepare("SELECT fcm_token FROM users WHERE id = ?");
    $stmt->bind_param("i", $user_id);
    $stmt->execute();
    $result = $stmt->get_result();

    if ($result->num_rows === 0) {
        error_log("FCM: User $user_id not found");
        return false;
    }

    $user = $result->fetch_assoc();
    $fcm_token = $user['fcm_token'];

    if (empty($fcm_token)) {
        error_log("FCM: User $user_id has no FCM token");
        return false;
    }

    // Get OAuth2 access token
    $access_token = getAccessToken();
    if (!$access_token) {
        error_log("FCM: Failed to get access token for user $user_id");
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
            'data' => array_map('strval', $data), // Convert all to strings
            'android' => [
                'priority' => 'high',
                'notification' => [
                    'sound' => 'default',
                    'notification_priority' => 'PRIORITY_HIGH',
                    'default_sound' => true,
                    'default_vibrate_timings' => true
                ]
            ],
            'apns' => [
                'payload' => [
                    'aps' => [
                        'sound' => 'default',
                        'badge' => 1
                    ]
                ]
            ]
        ]
    ];

    // Send via FCM v1 API
    $url = 'https://fcm.googleapis.com/v1/projects/smdprojectsoulwrite/messages:send';
    $ch = curl_init();
    curl_setopt($ch, CURLOPT_URL, $url);
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
    error_log("FCM: Sent to user $user_id - HTTP $http_code - Response: $result");

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

