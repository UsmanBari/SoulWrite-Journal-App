<?php
header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: POST');

require_once 'config.php';

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $journal_id = $_POST['journal_id'] ?? '';

    if (empty($journal_id)) {
        echo json_encode(array("success" => false, "message" => "Journal ID is required"));
        exit();
    }

    $query = "DELETE FROM journals WHERE id = ?";
    $stmt = $conn->prepare($query);
    $stmt->bind_param("i", $journal_id);

    if ($stmt->execute()) {
        echo json_encode(array("success" => true, "message" => "Journal deleted successfully"));
    } else {
        echo json_encode(array("success" => false, "message" => "Failed to delete journal"));
    }

    $stmt->close();
}

$conn->close();
?>

