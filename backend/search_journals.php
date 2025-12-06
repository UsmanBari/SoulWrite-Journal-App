<?php
header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: GET');

require_once 'config.php';

if ($_SERVER['REQUEST_METHOD'] === 'GET') {
    $query = $_GET['query'] ?? '';
    $user_id = $_GET['user_id'] ?? '';

    if (empty($query)) {
        echo json_encode(array("success" => false, "message" => "Search query is required"));
        exit();
    }

    // Search in public journals OR user's own journals (including private)
    if (!empty($user_id)) {
        $search_query = "SELECT j.*, u.name as user_name
                         FROM journals j
                         JOIN users u ON j.user_id = u.id
                         WHERE (j.title LIKE ? OR j.content LIKE ?)
                         AND (j.is_public = 1 OR j.user_id = ?)
                         ORDER BY j.date DESC
                         LIMIT 50";
        $stmt = $conn->prepare($search_query);
        $search_param = "%$query%";
        $stmt->bind_param("ssi", $search_param, $search_param, $user_id);
    } else {
        // If no user_id provided, only search public journals
        $search_query = "SELECT j.*, u.name as user_name
                         FROM journals j
                         JOIN users u ON j.user_id = u.id
                         WHERE (j.title LIKE ? OR j.content LIKE ?) AND j.is_public = 1
                         ORDER BY j.date DESC
                         LIMIT 50";
        $stmt = $conn->prepare($search_query);
        $search_param = "%$query%";
        $stmt->bind_param("ss", $search_param, $search_param);
    }

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

