<?php
header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');
require_once 'config.php';

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    echo json_encode(['success' => false, 'message' => 'Invalid request method']);
    exit;
}

$user_id = $_POST['user_id'] ?? '';
$fcm_token = $_POST['fcm_token'] ?? '';

if (empty($user_id) || empty($fcm_token)) {
    echo json_encode(['success' => false, 'message' => 'Missing required fields']);
    exit;
}

$stmt = $conn->prepare("UPDATE users SET fcm_token = ? WHERE id = ?");
$stmt->bind_param("si", $fcm_token, $user_id);

if ($stmt->execute()) {
    echo json_encode(['success' => true, 'message' => 'FCM token updated']);
} else {
    echo json_encode(['success' => false, 'message' => 'Failed to update token']);
}

$conn->close();
?>

