<?php
header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');
require_once 'config.php';

if ($_SERVER['REQUEST_METHOD'] !== 'GET') {
    echo json_encode(['success' => false, 'message' => 'Invalid request method']);
    exit;
}

$user_id = $_GET['user_id'] ?? '';

if (empty($user_id)) {
    echo json_encode(['success' => false, 'message' => 'Missing user_id']);
    exit;
}

// Get notifications with sender info
$stmt = $conn->prepare("
    SELECT n.*, u.name as from_user_name
    FROM notifications n
    LEFT JOIN users u ON n.from_user_id = u.id
    WHERE n.user_id = ?
    ORDER BY n.created_at DESC
    LIMIT 50
");
$stmt->bind_param("i", $user_id);
$stmt->execute();
$result = $stmt->get_result();

$notifications = array();
while ($row = $result->fetch_assoc()) {
    // Properly handle data field - keep as string for Android to parse as JSONObject
    $dataString = $row['data'];
    if (empty($dataString)) {
        $dataString = "{}";
    }

    $notifications[] = array(
        "id" => (int)$row['id'],
        "type" => $row['type'] ?? '',
        "title" => $row['title'] ?? '',
        "message" => $row['message'] ?? '',
        "data" => $dataString,  // Keep as JSON string
        "from_user_name" => $row['from_user_name'] ?? 'Someone',
        "is_read" => (int)$row['is_read'],
        "created_at" => $row['created_at'] ?? ''
    );
}

// Mark all as read
$stmt = $conn->prepare("UPDATE notifications SET is_read = 1 WHERE user_id = ?");
$stmt->bind_param("i", $user_id);
$stmt->execute();

echo json_encode([
    'success' => true,
    'notifications' => $notifications
]);

$conn->close();
?>

