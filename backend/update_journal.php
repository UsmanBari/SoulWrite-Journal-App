<?php
header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: POST');

require_once 'config.php';

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $journal_id = $_POST['journal_id'] ?? '';
    $user_id = $_POST['user_id'] ?? '';
    $title = $_POST['title'] ?? '';
    $content = $_POST['content'] ?? '';
    $image_url = $_POST['image_url'] ?? '';
    $thumbnail_url = $_POST['thumbnail_url'] ?? '';
    $is_public = $_POST['is_public'] ?? '0';

    if (empty($journal_id) || empty($user_id) || empty($title) || empty($content)) {
        echo json_encode(array("success" => false, "message" => "Journal ID, user ID, title and content are required"));
        exit();
    }

    // Verify ownership
    $check_query = "SELECT id FROM journals WHERE id = ? AND user_id = ?";
    $stmt = $conn->prepare($check_query);
    $stmt->bind_param("ii", $journal_id, $user_id);
    $stmt->execute();
    $result = $stmt->get_result();

    if ($result->num_rows === 0) {
        echo json_encode(array("success" => false, "message" => "Journal not found or you don't have permission to update it"));
        $stmt->close();
        exit();
    }
    $stmt->close();

    // Update the journal
    $query = "UPDATE journals SET title = ?, content = ?, image_url = ?, thumbnail_url = ?, is_public = ? WHERE id = ? AND user_id = ?";
    $stmt = $conn->prepare($query);
    $stmt->bind_param("sssssii", $title, $content, $image_url, $thumbnail_url, $is_public, $journal_id, $user_id);

    if ($stmt->execute()) {
        echo json_encode(array("success" => true, "message" => "Journal updated successfully"));
    } else {
        echo json_encode(array("success" => false, "message" => "Failed to update journal"));
    }

    $stmt->close();
}

$conn->close();
?>

