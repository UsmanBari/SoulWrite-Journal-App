<?php
/**
 * FINAL FCM Solution - With Full Logging
 * Since diagnostics show everything works, this adds detailed logging
 */

function getAccessToken() {
    error_log("FCM: getAccessToken() called");

    $json_file = __DIR__ . '/services_json.json';

    if (!file_exists($json_file)) {
        error_log("FCM ERROR: services_json.json not found at: $json_file");
        return null;
    }
    error_log("FCM: services_json.json found");

    $service_account = json_decode(file_get_contents($json_file), true);
    if (!$service_account) {
        error_log("FCM ERROR: Failed to parse services_json.json");
        return null;
    }
    error_log("FCM: services_json.json parsed successfully");

    if (!isset($service_account['private_key'])) {
        error_log("FCM ERROR: private_key not found");
        return null;
    }
    error_log("FCM: private_key found");

    $now = time();

    // Build JWT
    $header = json_encode(['alg' => 'RS256', 'typ' => 'JWT']);
    $claim = json_encode([
        'iss' => $service_account['client_email'],
        'scope' => 'https://www.googleapis.com/auth/firebase.messaging',
        'aud' => $service_account['token_uri'],
        'iat' => $now,
        'exp' => $now + 3600
    ]);

    $jwt_header = rtrim(strtr(base64_encode($header), '+/', '-_'), '=');
    $jwt_claim = rtrim(strtr(base64_encode($claim), '+/', '-_'), '=');
    $signature_input = "$jwt_header.$jwt_claim";

    error_log("FCM: JWT header and claim created");

    // Get private key
    $private_key = @openssl_pkey_get_private($service_account['private_key']);
    if (!$private_key) {
        $error = openssl_error_string();
        error_log("FCM ERROR: Cannot parse private key - $error");
        return null;
    }
    error_log("FCM: Private key parsed successfully");

    // Sign
    $signature = '';
    if (!openssl_sign($signature_input, $signature, $private_key, OPENSSL_ALGO_SHA256)) {
        $error = openssl_error_string();
        error_log("FCM ERROR: Cannot sign JWT - $error");
        return null;
    }
    error_log("FCM: JWT signed successfully");

    $jwt_signature = rtrim(strtr(base64_encode($signature), '+/', '-_'), '=');
    $jwt = "$signature_input.$jwt_signature";

    error_log("FCM: Complete JWT created, length: " . strlen($jwt));

    // Exchange for token
    $token_url = $service_account['token_uri'];
    error_log("FCM: Requesting access token from: $token_url");

    $ch = curl_init($token_url);
    curl_setopt_array($ch, [
        CURLOPT_POST => true,
        CURLOPT_RETURNTRANSFER => true,
        CURLOPT_SSL_VERIFYPEER => false,
        CURLOPT_HTTPHEADER => ['Content-Type: application/x-www-form-urlencoded'],
        CURLOPT_POSTFIELDS => http_build_query([
            'grant_type' => 'urn:ietf:params:oauth:grant-type:jwt-bearer',
            'assertion' => $jwt
        ])
    ]);

    $response = curl_exec($ch);
    $http_code = curl_getinfo($ch, CURLINFO_HTTP_CODE);
    $curl_error = curl_error($ch);
    curl_close($ch);

    if ($curl_error) {
        error_log("FCM ERROR: cURL error: $curl_error");
        return null;
    }

    error_log("FCM: Token request HTTP $http_code");
    error_log("FCM: Token response: $response");

    if ($http_code !== 200) {
        error_log("FCM ERROR: Token request failed (HTTP $http_code)");
        return null;
    }

    $token_data = json_decode($response, true);
    if (!isset($token_data['access_token'])) {
        error_log("FCM ERROR: No access_token in response");
        return null;
    }

    $token_preview = substr($token_data['access_token'], 0, 30);
    error_log("FCM SUCCESS: Got access token: $token_preview...");
    return $token_data['access_token'];
}

function sendPushNotification($user_id, $title, $message, $data = []) {
    global $conn;

    error_log("FCM: sendPushNotification called for user $user_id");

    // Get user's FCM token
    $stmt = $conn->prepare("SELECT fcm_token FROM users WHERE id = ?");
    $stmt->bind_param("i", $user_id);
    $stmt->execute();
    $result = $stmt->get_result();

    if ($result->num_rows === 0) {
        error_log("FCM ERROR: User $user_id not found");
        return false;
    }

    $user = $result->fetch_assoc();
    $fcm_token = $user['fcm_token'];

    if (empty($fcm_token)) {
        error_log("FCM ERROR: User $user_id has no FCM token");
        return false;
    }

    $token_preview = substr($fcm_token, 0, 30);
    error_log("FCM: User token: $token_preview...");

    // Get access token
    $access_token = getAccessToken();
    if (!$access_token) {
        error_log("FCM ERROR: Failed to get access token");
        return false;
    }
    error_log("FCM: Access token obtained");

    // Prepare payload
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
                    'sound' => 'default',
                    'notification_priority' => 'PRIORITY_HIGH',
                    'default_sound' => true
                ]
            ]
        ]
    ];

    $url = 'https://fcm.googleapis.com/v1/projects/smdprojectsoulwrite/messages:send';
    error_log("FCM: Sending to: $url");

    $ch = curl_init($url);
    curl_setopt_array($ch, [
        CURLOPT_POST => true,
        CURLOPT_RETURNTRANSFER => true,
        CURLOPT_SSL_VERIFYPEER => false,
        CURLOPT_HTTPHEADER => [
            'Authorization: Bearer ' . $access_token,
            'Content-Type: application/json'
        ],
        CURLOPT_POSTFIELDS => json_encode($payload)
    ]);

    $response = curl_exec($ch);
    $http_code = curl_getinfo($ch, CURLINFO_HTTP_CODE);
    $curl_error = curl_error($ch);
    curl_close($ch);

    if ($curl_error) {
        error_log("FCM ERROR: cURL error sending notification: $curl_error");
        return false;
    }

    error_log("FCM: Send HTTP $http_code");
    error_log("FCM: Send response: $response");

    if ($http_code === 200) {
        error_log("FCM SUCCESS: Notification sent to user $user_id!");
        return true;
    } else {
        error_log("FCM ERROR: Send failed (HTTP $http_code)");
        return false;
    }
}

function updateFCMToken($user_id, $fcm_token) {
    global $conn;
    $stmt = $conn->prepare("UPDATE users SET fcm_token = ? WHERE id = ?");
    $stmt->bind_param("si", $fcm_token, $user_id);
    return $stmt->execute();
}
?>

