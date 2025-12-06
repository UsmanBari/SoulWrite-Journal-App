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

        // If journal is public, notify all followers
        if ($is_public == 1) {
            // Get user's info
            $user_stmt = $conn->prepare("SELECT name FROM users WHERE id = ?");
            $user_stmt->bind_param("i", $user_id);
            $user_stmt->execute();
            $user_result = $user_stmt->get_result();

            if ($user_result->num_rows > 0) {
                $user_data = $user_result->fetch_assoc();
                $author_name = $user_data['name'];

                // Get all followers
                $followers_stmt = $conn->prepare("SELECT follower_id FROM followers WHERE following_id = ?");
                $followers_stmt->bind_param("i", $user_id);
                $followers_stmt->execute();
                $followers_result = $followers_stmt->get_result();

                // Load notification functions
                require_once 'send_notification.php';

                $notification_title = "New Journal";
                $notification_message = "$author_name posted a new journal: $title";
                $notification_data = json_encode(['journal_id' => $journal_id, 'type' => 'new_journal']);

                // Send notification to each follower
                while ($follower = $followers_result->fetch_assoc()) {
                    $follower_id = $follower['follower_id'];

                    // Save notification to database
                    $notif_stmt = $conn->prepare("INSERT INTO notifications (user_id, from_user_id, type, title, message, data) VALUES (?, ?, 'new_journal', ?, ?, ?)");
                    $notif_stmt->bind_param("iisss", $follower_id, $user_id, $notification_title, $notification_message, $notification_data);
                    $notif_stmt->execute();

                    // Send FCM push notification
                    sendPushNotification($follower_id, $notification_title, $notification_message, ['journal_id' => (string)$journal_id, 'type' => 'new_journal']);
                }

                error_log("Sent new journal notifications to " . $followers_result->num_rows . " followers");
            }
        }

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

