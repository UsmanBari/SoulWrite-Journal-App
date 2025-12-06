<?php
// Suppress all errors to prevent HTML output
error_reporting(0);
ini_set('display_errors', 0);

// Disable any output buffering that might add HTML
if (ob_get_level()) ob_end_clean();
ob_start();

header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');


require_once 'config.php';

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    echo json_encode(['success' => false, 'message' => 'Invalid request method']);
    exit;
}

$journal_id = $_POST['journal_id'] ?? '';
$user_id = $_POST['user_id'] ?? '';

if (empty($journal_id) || empty($user_id)) {
    echo json_encode(['success' => false, 'message' => 'Missing required fields']);
    exit;
}

// Check if journal exists and is public
$stmt = $conn->prepare("SELECT user_id, title FROM journals WHERE id = ? AND is_public = 1");
$stmt->bind_param("i", $journal_id);
$stmt->execute();
$result = $stmt->get_result();

if ($result->num_rows === 0) {
    echo json_encode(['success' => false, 'message' => 'Journal not found or not public']);
    exit;
}

$journal = $result->fetch_assoc();
$journal_owner_id = $journal['user_id'];
$journal_title = $journal['title'];

// Check if already liked
$stmt = $conn->prepare("SELECT id FROM journal_likes WHERE journal_id = ? AND user_id = ?");
$stmt->bind_param("ii", $journal_id, $user_id);
$stmt->execute();
$existing = $stmt->get_result();

if ($existing->num_rows > 0) {
    // Unlike
    $stmt = $conn->prepare("DELETE FROM journal_likes WHERE journal_id = ? AND user_id = ?");
    $stmt->bind_param("ii", $journal_id, $user_id);

    if ($stmt->execute()) {
        ob_clean();
        echo json_encode(['success' => true, 'liked' => false, 'message' => 'Unliked']);
    } else {
        ob_clean();
        echo json_encode(['success' => false, 'message' => 'Failed to unlike']);
    }
} else {
    // Like
    $stmt = $conn->prepare("INSERT INTO journal_likes (journal_id, user_id) VALUES (?, ?)");
    $stmt->bind_param("ii", $journal_id, $user_id);

    if ($stmt->execute()) {
        // Get liker's name
        $stmt = $conn->prepare("SELECT name FROM users WHERE id = ?");
        $stmt->bind_param("i", $user_id);
        $stmt->execute();
        $liker = $stmt->get_result()->fetch_assoc();
        $liker_name = $liker['name'];

        // Create notification for journal owner (if not self-like)
        if ($journal_owner_id != $user_id) {
            try {
                $title = "New Like";
                $message = "$liker_name liked your journal: $journal_title";
                $data = json_encode(['journal_id' => (string)$journal_id, 'type' => 'like']);

                // Insert notification into database
                $notif_stmt = $conn->prepare("INSERT INTO notifications (user_id, from_user_id, type, title, message, data) VALUES (?, ?, 'like', ?, ?, ?)");
                $notif_stmt->bind_param("iisss", $journal_owner_id, $user_id, $title, $message, $data);

                if ($notif_stmt->execute()) {
                    error_log("Like notification created for user $journal_owner_id");

                    // Send FCM notification (but don't break if it fails)
                    if (file_exists(__DIR__ . '/send_notification.php')) {
                        require_once __DIR__ . '/send_notification.php';
                        $fcm_result = @sendPushNotification($journal_owner_id, $title, $message, ['journal_id' => (string)$journal_id, 'type' => 'like']);
                        error_log("FCM notification sent: " . ($fcm_result ? "success" : "failed"));
                    }
                } else {
                    error_log("Failed to insert like notification: " . $conn->error);
                }
            } catch (Exception $e) {
                // Log error but don't break the response
                error_log("Notification error: " . $e->getMessage());
            }
        }

        ob_clean();
        echo json_encode(['success' => true, 'liked' => true, 'message' => 'Liked']);
    } else {
        ob_clean();
        echo json_encode(['success' => false, 'message' => 'Failed to like']);
    }
}

$conn->close();
?>

