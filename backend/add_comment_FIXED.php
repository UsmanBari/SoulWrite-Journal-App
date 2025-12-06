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
    ob_clean();
    echo json_encode(['success' => false, 'message' => 'Invalid request method']);
    exit;
}

$journal_id = $_POST['journal_id'] ?? '';
$user_id = $_POST['user_id'] ?? '';
$comment_text = $_POST['comment_text'] ?? '';

if (empty($journal_id) || empty($user_id) || empty($comment_text)) {
    ob_clean();
    echo json_encode(['success' => false, 'message' => 'Missing required fields']);
    exit;
}

// Check if journal exists and is public
$stmt = $conn->prepare("SELECT user_id, title FROM journals WHERE id = ? AND is_public = 1");
$stmt->bind_param("i", $journal_id);
$stmt->execute();
$result = $stmt->get_result();

if ($result->num_rows === 0) {
    ob_clean();
    echo json_encode(['success' => false, 'message' => 'Journal not found or not public']);
    exit;
}

$journal = $result->fetch_assoc();
$journal_owner_id = $journal['user_id'];
$journal_title = $journal['title'];

// Insert comment
$stmt = $conn->prepare("INSERT INTO journal_comments (journal_id, user_id, comment_text) VALUES (?, ?, ?)");
$stmt->bind_param("iis", $journal_id, $user_id, $comment_text);

if ($stmt->execute()) {
    $comment_id = $stmt->insert_id;

    // Get commenter's name
    $stmt = $conn->prepare("SELECT name FROM users WHERE id = ?");
    $stmt->bind_param("i", $user_id);
    $stmt->execute();
    $commenter = $stmt->get_result()->fetch_assoc();
    $commenter_name = $commenter['name'] ?? 'Someone';

    // Create notification for journal owner (if not self-comment)
    if ($journal_owner_id != $user_id) {
        try {
            $title = "New Comment";
            $message = "$commenter_name commented on your journal: $journal_title";
            $data = json_encode(['journal_id' => (string)$journal_id, 'comment_id' => (string)$comment_id, 'type' => 'comment']);

            // Insert notification into database
            $notif_stmt = $conn->prepare("INSERT INTO notifications (user_id, from_user_id, type, title, message, data) VALUES (?, ?, 'comment', ?, ?, ?)");
            $notif_stmt->bind_param("iisss", $journal_owner_id, $user_id, $title, $message, $data);

            if ($notif_stmt->execute()) {
                error_log("✅ Comment notification created for user $journal_owner_id from user $user_id");

                // Send FCM notification (but don't break if it fails)
                if (file_exists(__DIR__ . '/send_notification.php')) {
                    require_once __DIR__ . '/send_notification.php';
                    $fcm_result = @sendPushNotification($journal_owner_id, $title, $message, ['journal_id' => (string)$journal_id, 'type' => 'comment']);
                    error_log("FCM comment notification: " . ($fcm_result ? "✅ success" : "❌ failed"));
                } else {
                    error_log("❌ send_notification.php not found at: " . __DIR__ . '/send_notification.php');
                }
            } else {
                error_log("❌ Failed to insert comment notification: " . $conn->error);
            }
        } catch (Exception $e) {
            // Log error but don't break the response
            error_log("❌ Notification error: " . $e->getMessage());
        }
    } else {
        error_log("ℹ️ Not sending notification - user $user_id commented on own journal");
    }

    // Clean output buffer and send JSON
    ob_clean();
    echo json_encode([
        'success' => true,
        'comment_id' => $comment_id,
        'message' => 'Comment added'
    ]);
} else {
    ob_clean();
    echo json_encode(['success' => false, 'message' => 'Failed to add comment: ' . $conn->error]);
}

$conn->close();
?>

