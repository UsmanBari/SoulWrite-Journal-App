<?php
/**
 * FCM Push Notifications - WORKING VERSION
 * Based on proven AwardSpace implementation
 */

function base64UrlEncode($data) {
    return rtrim(strtr(base64_encode($data), '+/', '-_'), '=');
}

function getAccessToken() {
    $json_file = __DIR__ . '/services_json.json';

    if (!file_exists($json_file)) {
        error_log("FCM: services_json.json not found");
        return null;
    }

    $serviceAccount = json_decode(file_get_contents($json_file), true);
    if (!$serviceAccount) {
        error_log("FCM: Failed to parse services_json.json");
        return null;
    }

    $now = time();

    // Create JWT header
    $header = array(
        'alg' => 'RS256',
        'typ' => 'JWT'
    );

    // Create JWT payload - CRITICAL: Use correct 'aud'
    $payload = array(
        'iss' => $serviceAccount['client_email'],
        'scope' => 'https://www.googleapis.com/auth/firebase.messaging',
        'aud' => 'https://oauth2.googleapis.com/token',  // ✅ CORRECT endpoint
        'iat' => $now,
        'exp' => $now + 3600
    );

    // Encode header and payload
    $base64UrlHeader = base64UrlEncode(json_encode($header));
    $base64UrlPayload = base64UrlEncode(json_encode($payload));

    // Create signature input
    $signatureInput = $base64UrlHeader . '.' . $base64UrlPayload;

    // Sign with private key
    $privateKey = openssl_pkey_get_private($serviceAccount['private_key']);
    if (!$privateKey) {
        error_log("FCM: Failed to parse private key");
        return null;
    }

    $signature = '';
    openssl_sign($signatureInput, $signature, $privateKey, OPENSSL_ALGO_SHA256);

    // Create JWT
    $jwt = $signatureInput . '.' . base64UrlEncode($signature);

    // Exchange JWT for access token
    $ch = curl_init('https://oauth2.googleapis.com/token');
    curl_setopt_array($ch, [
        CURLOPT_POST => true,
        CURLOPT_RETURNTRANSFER => true,
        CURLOPT_SSL_VERIFYPEER => false,  // Important for AwardSpace
        CURLOPT_POSTFIELDS => http_build_query([
            'grant_type' => 'urn:ietf:params:oauth:grant-type:jwt-bearer',
            'assertion' => $jwt
        ])
    ]);

    $response = curl_exec($ch);
    $httpCode = curl_getinfo($ch, CURLINFO_HTTP_CODE);

    if (curl_errno($ch)) {
        error_log("FCM: cURL error: " . curl_error($ch));
        curl_close($ch);
        return null;
    }

    curl_close($ch);

    if ($httpCode !== 200) {
        error_log("FCM: Token request failed (HTTP $httpCode): $response");
        return null;
    }

    $tokenData = json_decode($response, true);
    if (!isset($tokenData['access_token'])) {
        error_log("FCM: No access_token in response");
        return null;
    }

    error_log("FCM: Access token obtained successfully");
    return $tokenData['access_token'];
}

function sendFCMNotificationV1($fcmToken, $title, $body, $data = array()) {
    // Load service account to get project ID
    $json_file = __DIR__ . '/services_json.json';
    $serviceAccount = json_decode(file_get_contents($json_file), true);
    $projectId = $serviceAccount['project_id'];

    // Get access token
    $accessToken = getAccessToken();
    if (!$accessToken) {
        error_log("FCM: Failed to get access token");
        return false;
    }

    // Prepare message
    $message = array(
        'message' => array(
            'token' => $fcmToken,
            'notification' => array(
                'title' => $title,
                'body' => $body
            ),
            'data' => $data,
            'android' => array(
                'priority' => 'high',
                'notification' => array(
                    'sound' => 'default',
                    'channel_id' => 'default'
                )
            )
        )
    );

    // Send to FCM
    $url = "https://fcm.googleapis.com/v1/projects/$projectId/messages:send";

    $ch = curl_init($url);
    curl_setopt_array($ch, [
        CURLOPT_POST => true,
        CURLOPT_RETURNTRANSFER => true,
        CURLOPT_SSL_VERIFYPEER => false,  // Important for AwardSpace
        CURLOPT_HTTPHEADER => [
            'Authorization: Bearer ' . $accessToken,
            'Content-Type: application/json'
        ],
        CURLOPT_POSTFIELDS => json_encode($message)
    ]);

    $response = curl_exec($ch);
    $httpCode = curl_getinfo($ch, CURLINFO_HTTP_CODE);

    if (curl_errno($ch)) {
        $error = curl_error($ch);
        error_log("FCM: cURL error sending notification: $error");
        curl_close($ch);
        return false;
    }

    curl_close($ch);

    if ($httpCode === 200) {
        error_log("FCM: Notification sent successfully");
        return true;
    } else {
        error_log("FCM: Send failed (HTTP $httpCode): $response");
        return false;
    }
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
    $fcmToken = $user['fcm_token'];

    if (empty($fcmToken)) {
        error_log("FCM: User $user_id has no FCM token");
        return false;
    }

    // Convert all data values to strings
    $dataStrings = array();
    foreach ($data as $key => $value) {
        $dataStrings[$key] = strval($value);
    }

    return sendFCMNotificationV1($fcmToken, $title, $message, $dataStrings);
}

function updateFCMToken($user_id, $fcm_token) {
    global $conn;
    $stmt = $conn->prepare("UPDATE users SET fcm_token = ? WHERE id = ?");
    $stmt->bind_param("si", $fcm_token, $user_id);
    return $stmt->execute();
}
?>

