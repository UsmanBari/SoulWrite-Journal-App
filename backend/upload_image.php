<?php
// Clean output buffer to prevent any HTML
if (ob_get_level()) ob_clean();

header('Content-Type: application/json; charset=utf-8');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: POST');

// Suppress any PHP warnings/notices
error_reporting(E_ERROR | E_PARSE);

// Log for debugging
error_log("Upload request received");
error_log("FILES: " . print_r($_FILES, true));

if ($_SERVER['REQUEST_METHOD'] === 'POST') {

    // Check if file was uploaded via standard form
    if (isset($_FILES['image']) && $_FILES['image']['error'] === UPLOAD_ERR_OK) {
        $file = $_FILES['image'];

        // Validate file type
        $allowed_types = ['image/jpeg', 'image/jpg', 'image/png', 'image/gif'];
        $file_type = $file['type'];

        if (!in_array($file_type, $allowed_types)) {
            echo json_encode(array("success" => false, "message" => "Invalid file type. Only JPG, PNG, and GIF allowed."));
            exit();
        }

        // Validate file size (max 5MB)
        if ($file['size'] > 5 * 1024 * 1024) {
            echo json_encode(array("success" => false, "message" => "File too large. Maximum size is 5MB."));
            exit();
        }

        // Create uploads directory if it doesn't exist
        $upload_dir = 'uploads/journals/';
        if (!file_exists($upload_dir)) {
            mkdir($upload_dir, 0755, true);
        }

        // Generate unique filename
        $extension = pathinfo($file['name'], PATHINFO_EXTENSION);
        $filename = 'journal_' . time() . '_' . uniqid() . '.' . $extension;
        $filepath = $upload_dir . $filename;

        // Move uploaded file
        if (move_uploaded_file($file['tmp_name'], $filepath)) {

            // VERIFY file actually exists after upload
            if (!file_exists($filepath)) {
                error_log("File upload reported success but file doesn't exist: " . $filepath);
                echo json_encode(array("success" => false, "message" => "File upload failed - file not found after save"));
                exit();
            }

            // Get actual file permissions
            $perms = substr(sprintf('%o', fileperms($filepath)), -4);
            error_log("File uploaded successfully: " . $filepath . " with permissions: " . $perms);

            // Generate thumbnail
            $thumbnail_path = $upload_dir . 'thumb_' . $filename;
            $thumbnail_created = createThumbnail($filepath, $thumbnail_path, 200, 200);

            // Verify thumbnail was created
            if ($thumbnail_created && !file_exists($thumbnail_path)) {
                error_log("Thumbnail creation reported success but file doesn't exist: " . $thumbnail_path);
                $thumbnail_created = false;
            }

            // Get the proper base URL
            $protocol = (isset($_SERVER['HTTPS']) && $_SERVER['HTTPS'] === 'on') ? "https" : "http";
            $host = $_SERVER['HTTP_HOST'];
            $script_dir = dirname($_SERVER['SCRIPT_NAME']);
            $base_url = $protocol . "://" . $host . $script_dir;

            // Use PHP proxy to serve images (bypasses 403 Forbidden errors)
            $image_url = $base_url . "/image.php?file=" . $filename;
            $thumbnail_url = $thumbnail_created ? $base_url . "/image.php?file=thumb_" . $filename : $image_url;

            error_log("Returning URLs - Image: " . $image_url . " Thumbnail: " . $thumbnail_url);

            // Return clean JSON
            $response = array(
                "success" => true,
                "message" => "Image uploaded successfully",
                "image_url" => $image_url,
                "thumbnail_url" => $thumbnail_url,
                "debug_file_exists" => file_exists($filepath),
                "debug_thumb_exists" => file_exists($thumbnail_path)
            );

            echo json_encode($response);
            exit();
        } else {
            $error = error_get_last();
            error_log("Failed to move uploaded file. Error: " . print_r($error, true));
            echo json_encode(array("success" => false, "message" => "Failed to move uploaded file. Check folder permissions."));
            exit();
        }
    }
    // Handle raw multipart data from Android
    else if (isset($_SERVER['CONTENT_TYPE']) && strpos($_SERVER['CONTENT_TYPE'], 'multipart/form-data') !== false) {
        error_log("Attempting to parse raw multipart data");

        // Get raw POST data
        $raw_data = file_get_contents('php://input');

        // Parse multipart data
        $boundary = substr($raw_data, 0, strpos($raw_data, "\r\n"));
        $parts = array_slice(explode($boundary, $raw_data), 1);

        foreach ($parts as $part) {
            if (empty($part)) continue;

            // Check if this part contains image data
            if (strpos($part, 'Content-Disposition') !== false && strpos($part, 'filename') !== false) {
                // Extract filename
                preg_match('/filename="(.+?)"/', $part, $filename_matches);
                if (!isset($filename_matches[1])) continue;

                $original_filename = $filename_matches[1];

                // Extract content type
                preg_match('/Content-Type: (.+?)\r\n/', $part, $type_matches);
                $file_type = isset($type_matches[1]) ? $type_matches[1] : 'image/jpeg';

                // Extract binary data
                $data_start = strpos($part, "\r\n\r\n") + 4;
                $data_end = strrpos($part, "\r\n");
                $image_data = substr($part, $data_start, $data_end - $data_start);

                // Validate file type
                $allowed_types = ['image/jpeg', 'image/jpg', 'image/png', 'image/gif'];
                if (!in_array($file_type, $allowed_types)) {
                    echo json_encode(array("success" => false, "message" => "Invalid file type"));
                    exit();
                }

                // Create uploads directory
                $upload_dir = 'uploads/journals/';
                if (!file_exists($upload_dir)) {
                    mkdir($upload_dir, 0755, true);
                }

                // Generate unique filename
                $extension = pathinfo($original_filename, PATHINFO_EXTENSION);
                $filename = 'journal_' . time() . '_' . uniqid() . '.' . $extension;
                $filepath = $upload_dir . $filename;

                // Save file
                $bytes_written = file_put_contents($filepath, $image_data);
                if ($bytes_written !== false) {

                    // VERIFY file actually exists after save
                    if (!file_exists($filepath)) {
                        error_log("Multipart: File save reported success but file doesn't exist: " . $filepath);
                        echo json_encode(array("success" => false, "message" => "File save failed - file not found after save"));
                        exit();
                    }

                    error_log("Multipart: File saved successfully: " . $filepath . " (" . $bytes_written . " bytes)");

                    // Generate thumbnail
                    $thumbnail_path = $upload_dir . 'thumb_' . $filename;
                    $thumbnail_created = createThumbnail($filepath, $thumbnail_path, 200, 200);

                    // Verify thumbnail
                    if ($thumbnail_created && !file_exists($thumbnail_path)) {
                        error_log("Multipart: Thumbnail creation reported success but file doesn't exist: " . $thumbnail_path);
                        $thumbnail_created = false;
                    }

                    // Get the proper base URL
                    $protocol = (isset($_SERVER['HTTPS']) && $_SERVER['HTTPS'] === 'on') ? "https" : "http";
                    $host = $_SERVER['HTTP_HOST'];
                    $script_dir = dirname($_SERVER['SCRIPT_NAME']);
                    $base_url = $protocol . "://" . $host . $script_dir;

                    // Use PHP proxy to serve images (bypasses 403 Forbidden errors)
                    $image_url = $base_url . "/image.php?file=" . $filename;
                    $thumbnail_url = $thumbnail_created ? $base_url . "/image.php?file=thumb_" . $filename : $image_url;

                    error_log("Multipart: Returning URLs - Image: " . $image_url . " Thumbnail: " . $thumbnail_url);

                    $response = array(
                        "success" => true,
                        "message" => "Image uploaded successfully",
                        "image_url" => $image_url,
                        "thumbnail_url" => $thumbnail_url,
                        "debug_file_exists" => file_exists($filepath),
                        "debug_thumb_exists" => file_exists($thumbnail_path),
                        "debug_bytes_written" => $bytes_written
                    );

                    echo json_encode($response);
                    exit();
                } else {
                    $error = error_get_last();
                    error_log("Multipart: Failed to save file. Error: " . print_r($error, true));
                    echo json_encode(array("success" => false, "message" => "Failed to save file. Check folder permissions."));
                    exit();
                }
            }
        }

        echo json_encode(array("success" => false, "message" => "No valid image data found in request"));
        exit();
    }
    else {
        $error_msg = "No image file received";
        if (isset($_FILES['image']['error'])) {
            $error_msg .= " (Error code: " . $_FILES['image']['error'] . ")";
        }
        echo json_encode(array("success" => false, "message" => $error_msg));
        exit();
    }
} else {
    echo json_encode(array("success" => false, "message" => "Invalid request method"));
    exit();
}

// Function to create thumbnail
function createThumbnail($source, $destination, $width, $height) {
    list($orig_width, $orig_height, $type) = getimagesize($source);

    // Calculate aspect ratio
    $ratio = min($width / $orig_width, $height / $orig_height);
    $new_width = $orig_width * $ratio;
    $new_height = $orig_height * $ratio;

    // Create image based on type
    switch ($type) {
        case IMAGETYPE_JPEG:
            $image = imagecreatefromjpeg($source);
            break;
        case IMAGETYPE_PNG:
            $image = imagecreatefrompng($source);
            break;
        case IMAGETYPE_GIF:
            $image = imagecreatefromgif($source);
            break;
        default:
            return false;
    }

    // Create thumbnail
    $thumbnail = imagecreatetruecolor($new_width, $new_height);
    imagecopyresampled($thumbnail, $image, 0, 0, 0, 0, $new_width, $new_height, $orig_width, $orig_height);

    // Save thumbnail
    imagejpeg($thumbnail, $destination, 85);

    // Free memory
    imagedestroy($image);
    imagedestroy($thumbnail);

    return true;
}
?>

