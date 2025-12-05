<?php
header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: GET');

require_once 'config.php';

if ($_SERVER['REQUEST_METHOD'] === 'GET') {
    $user_id = $_GET['user_id'] ?? '';

    if (empty($user_id)) {
        echo json_encode(array("success" => false, "message" => "User ID is required"));
        exit();
    }

    // Get journals for user:
    // 1. All journals created by this user (both private and public)
    // 2. Public journals from other users
    $query = "SELECT j.*, u.name as user_name
              FROM journals j
              JOIN users u ON j.user_id = u.id
              WHERE (j.user_id = ? OR (j.is_public = 1 AND j.user_id != ?))
              ORDER BY j.date DESC";
    $stmt = $conn->prepare($query);
    $stmt->bind_param("ii", $user_id, $user_id);
    $stmt->execute();
    $result = $stmt->get_result();

    $journals = array();
    while ($row = $result->fetch_assoc()) {
        $journals[] = $row;
    }

    echo json_encode(array(
        "success" => true,
        "journals" => $journals
    ));

    $stmt->close();
}

$conn->close();
?>

