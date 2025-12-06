<?php
/**
 * Image Proxy - Serves images to Android app
 * This bypasses 403 Forbidden errors caused by User-Agent blocking
 */

// Get the requested image filename
$file = $_GET['file'] ?? '';

// Sanitize the filename (prevent directory traversal attacks)
$file = basename($file);

// Construct the full path
$filepath = 'uploads/journals/' . $file;

// Check if file exists
if (file_exists($filepath) && is_file($filepath)) {
    // Get file extension
    $ext = strtolower(pathinfo($filepath, PATHINFO_EXTENSION));

    // Set appropriate MIME type
    $mime_types = [
        'jpg' => 'image/jpeg',
        'jpeg' => 'image/jpeg',
        'png' => 'image/png',
        'gif' => 'image/gif',
        'bmp' => 'image/bmp',
        'webp' => 'image/webp'
    ];

    $mime = $mime_types[$ext] ?? 'application/octet-stream';

    // Set headers for proper image serving
    header('Content-Type: ' . $mime);
    header('Content-Length: ' . filesize($filepath));
    header('Access-Control-Allow-Origin: *');
    header('Access-Control-Allow-Methods: GET, OPTIONS');
    header('Cache-Control: public, max-age=86400'); // Cache for 1 day
    header('Expires: ' . gmdate('D, d M Y H:i:s', time() + 86400) . ' GMT');

    // Output the file
    readfile($filepath);
    exit;
} else {
    // File not found
    header('HTTP/1.0 404 Not Found');
    header('Content-Type: text/plain');
    echo 'Image not found: ' . htmlspecialchars($file);
    exit;
}
?>

