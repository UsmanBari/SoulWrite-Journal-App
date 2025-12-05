<?php
header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: POST');

require_once 'config.php';

if ($_SERVER['REQUEST_METHOD'] === 'POST') {

    // Check if file was uploaded
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
            // Generate thumbnail
            $thumbnail_path = $upload_dir . 'thumb_' . $filename;
            createThumbnail($filepath, $thumbnail_path, 200, 200);

            // Return URLs
            $base_url = (isset($_SERVER['HTTPS']) && $_SERVER['HTTPS'] === 'on' ? "https" : "http") . "://$_SERVER[HTTP_HOST]";
            $image_url = $base_url . '/' . $filepath;
            $thumbnail_url = $base_url . '/' . $thumbnail_path;

            echo json_encode(array(
                "success" => true,
                "message" => "Image uploaded successfully",
                "image_url" => $image_url,
                "thumbnail_url" => $thumbnail_url
            ));
        } else {
            echo json_encode(array("success" => false, "message" => "Failed to upload image"));
        }
    } else {
        echo json_encode(array("success" => false, "message" => "No image file received"));
    }
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

