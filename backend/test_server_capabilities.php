<?php
// Diagnostic script to check server capabilities
header('Content-Type: text/plain');

echo "=== SERVER DIAGNOSTICS ===\n\n";

echo "1. PHP Version:\n";
echo "  " . phpversion() . "\n\n";

echo "2. OpenSSL Extension:\n";
if (extension_loaded('openssl')) {
    echo "  ✅ OpenSSL is loaded\n";
    echo "  Version: " . OPENSSL_VERSION_TEXT . "\n\n";
} else {
    echo "  ❌ OpenSSL NOT loaded\n";
    echo "  This is required for JWT signing!\n\n";
}

echo "3. cURL Extension:\n";
if (extension_loaded('curl')) {
    echo "  ✅ cURL is loaded\n";
    $version = curl_version();
    echo "  Version: " . $version['version'] . "\n";
    echo "  SSL Version: " . $version['ssl_version'] . "\n\n";
} else {
    echo "  ❌ cURL NOT loaded\n\n";
}

echo "4. JSON Extension:\n";
if (extension_loaded('json')) {
    echo "  ✅ JSON is loaded\n\n";
} else {
    echo "  ❌ JSON NOT loaded\n\n";
}

echo "5. Testing services_json.json:\n";
$json_file = __DIR__ . '/services_json.json';
if (file_exists($json_file)) {
    echo "  ✅ File exists\n";

    $content = file_get_contents($json_file);
    $json = json_decode($content, true);

    if ($json) {
        echo "  ✅ Valid JSON\n";
        echo "  Project ID: " . ($json['project_id'] ?? 'NOT FOUND') . "\n";
        echo "  Client Email: " . ($json['client_email'] ?? 'NOT FOUND') . "\n";

        if (isset($json['private_key'])) {
            echo "  ✅ private_key exists\n";
            $key_preview = substr($json['private_key'], 0, 50);
            echo "  Preview: " . $key_preview . "...\n";

            // Test if private key can be parsed
            if (extension_loaded('openssl')) {
                $private_key = @openssl_pkey_get_private($json['private_key']);
                if ($private_key) {
                    echo "  ✅ private_key can be parsed by OpenSSL\n";
                    openssl_pkey_free($private_key);
                } else {
                    echo "  ❌ private_key CANNOT be parsed\n";
                    echo "  Error: " . openssl_error_string() . "\n";
                }
            }
        } else {
            echo "  ❌ private_key NOT found\n";
        }
    } else {
        echo "  ❌ Invalid JSON format\n";
        echo "  Error: " . json_last_error_msg() . "\n";
    }
} else {
    echo "  ❌ File NOT found at: $json_file\n";
}
echo "\n";

echo "6. Test JWT Signing:\n";
if (extension_loaded('openssl') && file_exists($json_file)) {
    $json = json_decode(file_get_contents($json_file), true);
    if ($json && isset($json['private_key'])) {
        $test_data = "test_data_to_sign";
        $signature = '';
        $private_key = @openssl_pkey_get_private($json['private_key']);

        if ($private_key) {
            $result = @openssl_sign($test_data, $signature, $private_key, OPENSSL_ALGO_SHA256);
            if ($result) {
                echo "  ✅ Can sign data with private key\n";
            } else {
                echo "  ❌ Cannot sign data\n";
                echo "  Error: " . openssl_error_string() . "\n";
            }
        } else {
            echo "  ❌ Cannot get private key resource\n";
            echo "  Error: " . openssl_error_string() . "\n";
        }
    }
} else {
    echo "  ⚠️  Cannot test (OpenSSL not loaded or JSON not found)\n";
}
echo "\n";

echo "7. Alternative: Using firebase-php/firebase-php library?\n";
echo "  This would eliminate need for manual JWT signing.\n";
echo "  Server needs composer to install it.\n\n";

echo "=== RECOMMENDATIONS ===\n";
if (!extension_loaded('openssl')) {
    echo "❌ CRITICAL: OpenSSL extension is not loaded!\n";
    echo "   Contact your hosting provider to enable OpenSSL.\n";
    echo "   Or use alternative notification method.\n\n";
}

echo "=== END DIAGNOSTICS ===\n";
?>

