<?php
header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: POST');

require_once 'config.php';

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $follower_id = isset($_POST['follower_id']) ? intval($_POST['follower_id']) : 0;
    $following_id = isset($_POST['following_id']) ? intval($_POST['following_id']) : 0;

    if ($follower_id > 0 && $following_id > 0 && $follower_id != $following_id) {
        // Check if already following
        $check_sql = "SELECT id FROM followers WHERE follower_id = ? AND following_id = ?";
        $check_stmt = $conn->prepare($check_sql);
        $check_stmt->bind_param("ii", $follower_id, $following_id);
        $check_stmt->execute();
        $check_result = $check_stmt->get_result();

        if ($check_result->num_rows > 0) {
            echo json_encode(array("success" => false, "message" => "Already following this user"));
        } else {
            // Insert follow relationship
            $sql = "INSERT INTO followers (follower_id, following_id) VALUES (?, ?)";
            $stmt = $conn->prepare($sql);
            $stmt->bind_param("ii", $follower_id, $following_id);

            if ($stmt->execute()) {
                // Get follower's name
                $name_stmt = $conn->prepare("SELECT name FROM users WHERE id = ?");
                $name_stmt->bind_param("i", $follower_id);
                $name_stmt->execute();
                $follower = $name_stmt->get_result()->fetch_assoc();
                $follower_name = $follower['name'];

                // Create notification
                $title = "New Follower";
                $message = "$follower_name started following you";
                $data = json_encode(['user_id' => $follower_id, 'type' => 'follow']);

                $notif_stmt = $conn->prepare("INSERT INTO notifications (user_id, from_user_id, type, title, message, data) VALUES (?, ?, 'follow', ?, ?, ?)");
                $notif_stmt->bind_param("iisss", $following_id, $follower_id, $title, $message, $data);
                $notif_stmt->execute();

                // Send FCM notification
                require_once 'send_notification.php';
                sendPushNotification($following_id, $title, $message, ['user_id' => $follower_id, 'type' => 'follow']);

                echo json_encode(array("success" => true, "message" => "Successfully followed user"));
            } else {
                echo json_encode(array("success" => false, "message" => "Failed to follow user"));
            }
            $stmt->close();
        }
        $check_stmt->close();
    } else {
        echo json_encode(array("success" => false, "message" => "Invalid user IDs"));
    }
}

$conn->close();
?>

