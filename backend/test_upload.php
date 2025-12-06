<?php
// Test if PHP can write files to uploads/journals folder

header('Content-Type: text/html; charset=utf-8');

echo "<h2>Testing File Upload Permissions</h2>";

// Test 1: Check if folder exists
echo "<h3>Test 1: Folder Exists</h3>";
$folder = 'uploads/journals/';
if (file_exists($folder)) {
    echo "✓ Folder exists: " . realpath($folder) . "<br>";
    echo "Folder permissions: " . substr(sprintf('%o', fileperms($folder)), -4) . "<br>";
} else {
    echo "✗ Folder does not exist<br>";
    echo "Trying to create folder...<br>";
    if (mkdir($folder, 0777, true)) {
        echo "✓ Folder created successfully<br>";
    } else {
        echo "✗ Failed to create folder<br>";
    }
}

// Test 2: Try to write a file
echo "<h3>Test 2: Write File</h3>";
$test_file = $folder . 'test_' . time() . '.txt';
$test_content = 'This is a test file created at ' . date('Y-m-d H:i:s');
$bytes = file_put_contents($test_file, $test_content);

if ($bytes !== false) {
    echo "✓ File written successfully: $bytes bytes<br>";
    echo "File path: " . realpath($test_file) . "<br>";

    // Test 3: Check if file exists
    echo "<h3>Test 3: Verify File Exists</h3>";
    if (file_exists($test_file)) {
        echo "✓ File exists after write<br>";
        echo "File permissions: " . substr(sprintf('%o', fileperms($test_file)), -4) . "<br>";
        echo "File size: " . filesize($test_file) . " bytes<br>";

        // Test 4: Try to read the file
        echo "<h3>Test 4: Read File</h3>";
        $content = file_get_contents($test_file);
        if ($content !== false) {
            echo "✓ File read successfully<br>";
            echo "Content: " . htmlspecialchars($content) . "<br>";
        } else {
            echo "✗ Failed to read file<br>";
        }

        // Test 5: Check URL access
        echo "<h3>Test 5: HTTP Access</h3>";
        $protocol = (isset($_SERVER['HTTPS']) && $_SERVER['HTTPS'] === 'on') ? "https" : "http";
        $host = $_SERVER['HTTP_HOST'];
        $script_dir = dirname($_SERVER['SCRIPT_NAME']);
        $file_url = $protocol . "://" . $host . $script_dir . "/" . $test_file;
        echo "File URL: <a href='$file_url' target='_blank'>$file_url</a><br>";
        echo "Try clicking the link. If you see the file content, HTTP access works!<br>";

    } else {
        echo "✗ File does not exist after write!<br>";
        echo "This means write succeeded but file disappeared.<br>";
    }
} else {
    echo "✗ Failed to write file<br>";
    $error = error_get_last();
    echo "Error: " . print_r($error, true) . "<br>";
}

// Test 6: Image upload simulation
echo "<h3>Test 6: Image Upload Simulation</h3>";
$test_image = $folder . 'test_image_' . time() . '.jpg';
// Create a simple 1x1 pixel JPEG
$image_data = base64_decode('/9j/4AAQSkZJRgABAQEAYABgAAD/2wBDAAgGBgcGBQgHBwcJCQgKDBQNDAsLDBkSEw8UHRofHh0aHBwgJC4nICIsIxwcKDcpLDAxNDQ0Hyc5PTgyPC4zNDL/2wBDAQkJCQwLDBgNDRgyIRwhMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjL/wAARCAABAAEDASIAAhEBAxEB/8QAFQABAQAAAAAAAAAAAAAAAAAAAAv/xAAUEAEAAAAAAAAAAAAAAAAAAAAA/8QAFQEBAQAAAAAAAAAAAAAAAAAAAAX/xAAUEQEAAAAAAAAAAAAAAAAAAAAA/9oADAMBAAIRAxEAPwCwAA8A//Z');
$bytes = file_put_contents($test_image, $image_data);

if ($bytes !== false && file_exists($test_image)) {
    echo "✓ Test image created successfully<br>";
    $protocol = (isset($_SERVER['HTTPS']) && $_SERVER['HTTPS'] === 'on') ? "https" : "http";
    $host = $_SERVER['HTTP_HOST'];
    $script_dir = dirname($_SERVER['SCRIPT_NAME']);
    $image_url = $protocol . "://" . $host . $script_dir . "/" . $test_image;
    echo "Image URL: <a href='$image_url' target='_blank'>$image_url</a><br>";
    echo "<img src='$image_url' style='border:1px solid #000; width:50px; height:50px;'><br>";
} else {
    echo "✗ Failed to create test image<br>";
}

echo "<h3>Summary</h3>";
echo "If all tests passed, your server CAN save files and they ARE accessible via HTTP.<br>";
echo "If any test failed, check the error messages above.<br>";
?>

