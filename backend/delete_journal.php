<?php
header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: POST');

require_once 'config.php';

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $journal_id = $_POST['journal_id'] ?? '';
    $user_id = $_POST['user_id'] ?? '';

    if (empty($journal_id) || empty($user_id)) {
        echo json_encode(array("success" => false, "message" => "Journal ID and User ID are required"));
        exit();
    }

    // Get journal details including image URLs
    $check_query = "SELECT id, image_url, thumbnail_url FROM journals WHERE id = ? AND user_id = ?";
    $stmt = $conn->prepare($check_query);
    $stmt->bind_param("ii", $journal_id, $user_id);
    $stmt->execute();
    $result = $stmt->get_result();

    if ($result->num_rows === 0) {
        echo json_encode(array("success" => false, "message" => "Journal not found or you don't have permission to delete it"));
        $stmt->close();
        exit();
    }

    $journal = $result->fetch_assoc();
    $stmt->close();

    // Delete the journal from database
    $query = "DELETE FROM journals WHERE id = ? AND user_id = ?";
    $stmt = $conn->prepare($query);
    $stmt->bind_param("ii", $journal_id, $user_id);

    if ($stmt->execute()) {
        // Try to delete image files if they exist
        if (!empty($journal['image_url'])) {
            $image_path = str_replace((isset($_SERVER['HTTPS']) && $_SERVER['HTTPS'] === 'on' ? "https" : "http") . "://$_SERVER[HTTP_HOST]/", '', $journal['image_url']);
            if (file_exists($image_path)) {
                @unlink($image_path);
            }
        }
        if (!empty($journal['thumbnail_url'])) {
            $thumb_path = str_replace((isset($_SERVER['HTTPS']) && $_SERVER['HTTPS'] === 'on' ? "https" : "http") . "://$_SERVER[HTTP_HOST]/", '', $journal['thumbnail_url']);
            if (file_exists($thumb_path)) {
                @unlink($thumb_path);
            }
        }

        echo json_encode(array("success" => true, "message" => "Journal deleted successfully"));
    } else {
        echo json_encode(array("success" => false, "message" => "Failed to delete journal"));
    }

    $stmt->close();
}

$conn->close();
?>

