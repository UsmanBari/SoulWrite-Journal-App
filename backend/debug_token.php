<?php
// Debug getAccessToken() to see exact error
error_reporting(E_ALL);
ini_set('display_errors', 1);

echo "=== DEBUG getAccessToken() ===\n\n";

function base64UrlEncode($data) {
    return rtrim(strtr(base64_encode($data), '+/', '-_'), '=');
}

echo "1. Loading services_json.json...\n";
$json_file = __DIR__ . '/services_json.json';

if (!file_exists($json_file)) {
    echo "  ❌ File not found\n";
    exit;
}
echo "  ✅ File exists\n\n";

echo "2. Parsing JSON...\n";
$serviceAccount = json_decode(file_get_contents($json_file), true);

if (!$serviceAccount) {
    echo "  ❌ JSON parse failed\n";
    exit;
}
echo "  ✅ JSON parsed\n";
echo "  Project ID: " . $serviceAccount['project_id'] . "\n";
echo "  Client Email: " . $serviceAccount['client_email'] . "\n\n";

echo "3. Building JWT...\n";
$now = time();

$header = array(
    'alg' => 'RS256',
    'typ' => 'JWT'
);

$payload = array(
    'iss' => $serviceAccount['client_email'],
    'scope' => 'https://www.googleapis.com/auth/firebase.messaging',
    'aud' => 'https://oauth2.googleapis.com/token',
    'iat' => $now,
    'exp' => $now + 3600
);

echo "  Header: " . json_encode($header) . "\n";
echo "  Payload: " . json_encode($payload) . "\n\n";

echo "4. Encoding header and payload...\n";
$base64UrlHeader = base64UrlEncode(json_encode($header));
$base64UrlPayload = base64UrlEncode(json_encode($payload));

echo "  Encoded header: " . substr($base64UrlHeader, 0, 50) . "...\n";
echo "  Encoded payload: " . substr($base64UrlPayload, 0, 50) . "...\n\n";

echo "5. Creating signature...\n";
$signatureInput = $base64UrlHeader . '.' . $base64UrlPayload;
echo "  Signature input length: " . strlen($signatureInput) . "\n";

echo "6. Parsing private key...\n";
$privateKey = openssl_pkey_get_private($serviceAccount['private_key']);

if (!$privateKey) {
    echo "  ❌ Failed to parse private key\n";
    $errors = [];
    while ($error = openssl_error_string()) {
        $errors[] = $error;
    }
    if ($errors) {
        echo "  OpenSSL Errors:\n";
        foreach ($errors as $error) {
            echo "    - $error\n";
        }
    }
    exit;
}
echo "  ✅ Private key parsed\n\n";

echo "7. Signing data...\n";
$signature = '';
$result = openssl_sign($signatureInput, $signature, $privateKey, OPENSSL_ALGO_SHA256);

if (!$result) {
    echo "  ❌ Signing failed\n";
    exit;
}
echo "  ✅ Signature created\n";
echo "  Signature length: " . strlen($signature) . "\n\n";

echo "8. Building final JWT...\n";
$jwt = $signatureInput . '.' . base64UrlEncode($signature);
echo "  JWT length: " . strlen($jwt) . "\n";
echo "  JWT preview: " . substr($jwt, 0, 100) . "...\n\n";

echo "9. Exchanging JWT for access token...\n";
$tokenUrl = 'https://oauth2.googleapis.com/token';
echo "  URL: $tokenUrl\n";

$postData = http_build_query([
    'grant_type' => 'urn:ietf:params:oauth:grant-type:jwt-bearer',
    'assertion' => $jwt
]);

echo "  POST data length: " . strlen($postData) . "\n\n";

$ch = curl_init($tokenUrl);
curl_setopt_array($ch, [
    CURLOPT_POST => true,
    CURLOPT_RETURNTRANSFER => true,
    CURLOPT_SSL_VERIFYPEER => false,
    CURLOPT_VERBOSE => false,
    CURLOPT_POSTFIELDS => $postData
]);

echo "10. Sending request...\n";
$response = curl_exec($ch);
$httpCode = curl_getinfo($ch, CURLINFO_HTTP_CODE);
$curlError = curl_error($ch);

echo "  HTTP Code: $httpCode\n";

if ($curlError) {
    echo "  ❌ cURL Error: $curlError\n\n";
    curl_close($ch);
    exit;
}

curl_close($ch);

echo "  Response: $response\n\n";

if ($httpCode !== 200) {
    echo "❌ Token request failed (HTTP $httpCode)\n";
    echo "\nResponse details:\n";
    $responseData = json_decode($response, true);
    if ($responseData) {
        print_r($responseData);
    }
    exit;
}

echo "11. Parsing token response...\n";
$tokenData = json_decode($response, true);

if (!isset($tokenData['access_token'])) {
    echo "  ❌ No access_token in response\n";
    echo "  Response: $response\n";
    exit;
}

$accessToken = $tokenData['access_token'];
echo "  ✅ Access token: " . substr($accessToken, 0, 50) . "...\n\n";

echo "✅ SUCCESS! Access token obtained!\n";
echo "\nFull token:\n$accessToken\n";
?>

