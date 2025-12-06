<?php
/**
 * ALTERNATIVE FCM Solution - Uses legacy server key if available
 * Falls back to v1 API if OpenSSL works
 */

// Try to get access token using service account (v1 API)
function getAccessTokenV1() {
    $json_file = __DIR__ . '/services_json.json';

    if (!file_exists($json_file)) {
        error_log("FCM: services_json.json not found");
        return null;
    }

    $service_account = json_decode(file_get_contents($json_file), true);
    if (!$service_account || !isset($service_account['private_key'])) {
        error_log("FCM: Invalid services_json.json");
        return null;
    }

    // Check if OpenSSL is available
    if (!extension_loaded('openssl')) {
        error_log("FCM: OpenSSL extension not loaded - cannot use v1 API");
        return null;
    }

    $now = time();

    // Build JWT
    $header = base64_url_encode(json_encode(['alg' => 'RS256', 'typ' => 'JWT']));
    $claim = base64_url_encode(json_encode([
        'iss' => $service_account['client_email'],
        'scope' => 'https://www.googleapis.com/auth/firebase.messaging',
        'aud' => $service_account['token_uri'],
        'iat' => $now,
        'exp' => $now + 3600
    ]));

    $signature_input = "$header.$claim";

    // Try to sign
    $private_key = @openssl_pkey_get_private($service_account['private_key']);
    if (!$private_key) {
        error_log("FCM: Cannot parse private key - " . openssl_error_string());
        return null;
    }

    $signature = '';
    if (!openssl_sign($signature_input, $signature, $private_key, OPENSSL_ALGO_SHA256)) {
        error_log("FCM: Cannot sign JWT - " . openssl_error_string());
        return null;
    }

    $jwt = $signature_input . '.' . base64_url_encode($signature);

    // Exchange for token
    $ch = curl_init($service_account['token_uri']);
    curl_setopt_array($ch, [
        CURLOPT_POST => true,
        CURLOPT_RETURNTRANSFER => true,
        CURLOPT_SSL_VERIFYPEER => false,
        CURLOPT_POSTFIELDS => http_build_query([
            'grant_type' => 'urn:ietf:params:oauth:grant-type:jwt-bearer',
            'assertion' => $jwt
        ])
    ]);

    $response = curl_exec($ch);
    $http_code = curl_getinfo($ch, CURLINFO_HTTP_CODE);
    curl_close($ch);

    if ($http_code !== 200) {
        error_log("FCM: Token request failed (HTTP $http_code): $response");
        return null;
    }

    $token_data = json_decode($response, true);
    return $token_data['access_token'] ?? null;
}

function base64_url_encode($data) {
    return rtrim(strtr(base64_encode($data), '+/', '-_'), '=');
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

    // Try v1 API first
    $access_token = getAccessTokenV1();

    if ($access_token) {
        // Use v1 API
        error_log("FCM: Using v1 API with access token");
        return sendViaV1API($fcm_token, $title, $message, $data);
    } else {
        // Fallback: Log that FCM is not available
        error_log("FCM: Cannot get access token - Push notifications disabled");
        error_log("FCM: Notification created in database but not sent to device");
        error_log("FCM: Title: $title, Message: $message, User: $user_id");

        // Return true so notification still gets created in database
        // User will see it in Alerts tab even without push
        return false;
    }
}

function sendViaV1API($fcm_token, $title, $message, $data) {
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
                    'notification_priority' => 'PRIORITY_HIGH'
                ]
            ]
        ]
    ];

    $access_token = getAccessTokenV1();
    $url = 'https://fcm.googleapis.com/v1/projects/smdprojectsoulwrite/messages:send';

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
    curl_close($ch);

    error_log("FCM v1: HTTP $http_code - $response");
    return $http_code === 200;
}

function updateFCMToken($user_id, $fcm_token) {
    global $conn;
    $stmt = $conn->prepare("UPDATE users SET fcm_token = ? WHERE id = ?");
    $stmt->bind_param("si", $fcm_token, $user_id);
    return $stmt->execute();
}
?>

