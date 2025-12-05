<?php
header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: POST');

require_once 'config.php';

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $journal_id = $_POST['journal_id'] ?? '';
    $title = $_POST['title'] ?? '';
    $content = $_POST['content'] ?? '';
    $image_url = $_POST['image_url'] ?? '';

    if (empty($journal_id)) {
        echo json_encode(array("success" => false, "message" => "Journal ID is required"));
        exit();
    }

    $query = "UPDATE journals SET title = ?, content = ?, image_url = ? WHERE id = ?";
    $stmt = $conn->prepare($query);
    $stmt->bind_param("sssi", $title, $content, $image_url, $journal_id);

    if ($stmt->execute()) {
        echo json_encode(array("success" => true, "message" => "Journal updated successfully"));
    } else {
        echo json_encode(array("success" => false, "message" => "Failed to update journal"));
    }

    $stmt->close();
}

$conn->close();
?>

