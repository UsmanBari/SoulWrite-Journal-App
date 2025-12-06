<!DOCTYPE html>
<html>
<head>
    <title>Image Access Test</title>
    <style>
        body { font-family: Arial; padding: 20px; }
        .test { margin: 20px 0; padding: 15px; border: 1px solid #ccc; }
        .success { background: #d4edda; }
        .error { background: #f8d7da; }
        img { max-width: 300px; border: 2px solid #000; }
    </style>
</head>
<body>
    <h1>Image Access Diagnostic</h1>

    <?php
    $journal_folder = 'uploads/journals/';

    // Get all image files
    $images = glob($journal_folder . '*.{jpg,jpeg,png,gif}', GLOB_BRACE);
    $thumbs = glob($journal_folder . 'thumb_*.{jpg,jpeg,png,gif}', GLOB_BRACE);

    echo "<h2>Found " . count($images) . " images</h2>";

    // Get base URL
    $protocol = (isset($_SERVER['HTTPS']) && $_SERVER['HTTPS'] === 'on') ? "https" : "http";
    $host = $_SERVER['HTTP_HOST'];
    $script_dir = dirname($_SERVER['SCRIPT_NAME']);
    $base_url = $protocol . "://" . $host . $script_dir;

    // Show last 5 uploaded images
    $recent_images = array_slice(array_reverse($images), 0, 5);

    foreach ($recent_images as $image_path) {
        $filename = basename($image_path);
        $url = $base_url . "/" . $journal_folder . $filename;

        echo "<div class='test'>";
        echo "<h3>$filename</h3>";
        echo "Full path: " . realpath($image_path) . "<br>";
        echo "File size: " . filesize($image_path) . " bytes<br>";
        echo "Permissions: " . substr(sprintf('%o', fileperms($image_path)), -4) . "<br>";
        echo "URL: <a href='$url' target='_blank'>$url</a><br><br>";

        // Try to load the image
        echo "<img src='$url' onerror=\"this.style.border='2px solid red'; this.alt='FAILED TO LOAD'\" onload=\"this.style.border='2px solid green'\">";

        echo "</div>";
    }

    // Show thumbnail info
    echo "<h2>Thumbnails</h2>";
    echo "Found " . count($thumbs) . " thumbnails<br>";

    $recent_thumbs = array_slice(array_reverse($thumbs), 0, 3);
    foreach ($recent_thumbs as $thumb_path) {
        $filename = basename($thumb_path);
        $url = $base_url . "/" . $journal_folder . $filename;

        echo "<div class='test'>";
        echo "<h3>$filename</h3>";
        echo "URL: <a href='$url' target='_blank'>$url</a><br><br>";
        echo "<img src='$url' onerror=\"this.style.border='2px solid red'\" onload=\"this.style.border='2px solid green'\">";
        echo "</div>";
    }

    // Check .htaccess
    echo "<h2>.htaccess Check</h2>";
    $htaccess_path = 'uploads/.htaccess';
    if (file_exists($htaccess_path)) {
        echo "<div class='test success'>";
        echo "✓ .htaccess exists in uploads/<br>";
        echo "Content:<br><pre>" . htmlspecialchars(file_get_contents($htaccess_path)) . "</pre>";
        echo "</div>";
    } else {
        echo "<div class='test error'>";
        echo "✗ .htaccess NOT found in uploads/<br>";
        echo "This is why images are blocked!";
        echo "</div>";
    }
    ?>

    <h2>Instructions</h2>
    <p>If images show with GREEN border = Access works!</p>
    <p>If images show with RED border = 403 Forbidden or access blocked</p>
    <p>If .htaccess is missing, upload it to backend/uploads/.htaccess</p>
</body>
</html>

