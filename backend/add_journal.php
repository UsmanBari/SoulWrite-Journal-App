<?php
header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: POST');

require_once 'config.php';

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $user_id = $_POST['user_id'] ?? '';
    $title = $_POST['title'] ?? '';
    $content = $_POST['content'] ?? '';
    $image_url = $_POST['image_url'] ?? '';
    $thumbnail_url = $_POST['thumbnail_url'] ?? '';
    $is_public = $_POST['is_public'] ?? 0;

    // Validate inputs
    if (empty($user_id) || empty($title) || empty($content)) {
        echo json_encode(array("success" => false, "message" => "Required fields are missing"));
        exit();
    }

    // Insert journal
    $query = "INSERT INTO journals (user_id, title, content, image_url, thumbnail_url, is_public, date, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, NOW())";
    $stmt = $conn->prepare($query);
    $date = time() * 1000; // Convert to milliseconds
    $stmt->bind_param("issssis", $user_id, $title, $content, $image_url, $thumbnail_url, $is_public, $date);

    if ($stmt->execute()) {
        $journal_id = $conn->insert_id;
        echo json_encode(array(
            "success" => true,
            "message" => "Journal added successfully",
            "journal_id" => $journal_id
        ));
    } else {
        echo json_encode(array("success" => false, "message" => "Failed to add journal"));
    }

    $stmt->close();
}

$conn->close();
?>

