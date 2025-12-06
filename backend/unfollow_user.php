<?php
header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: POST');

require_once 'config.php';

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $follower_id = isset($_POST['follower_id']) ? intval($_POST['follower_id']) : 0;
    $following_id = isset($_POST['following_id']) ? intval($_POST['following_id']) : 0;

    if ($follower_id > 0 && $following_id > 0) {
        $sql = "DELETE FROM followers WHERE follower_id = ? AND following_id = ?";
        $stmt = $conn->prepare($sql);
        $stmt->bind_param("ii", $follower_id, $following_id);

        if ($stmt->execute()) {
            if ($stmt->affected_rows > 0) {
                echo json_encode(array("success" => true, "message" => "Successfully unfollowed user"));
            } else {
                echo json_encode(array("success" => false, "message" => "Not following this user"));
            }
        } else {
            echo json_encode(array("success" => false, "message" => "Failed to unfollow user"));
        }
        $stmt->close();
    } else {
        echo json_encode(array("success" => false, "message" => "Invalid user IDs"));
    }
}

$conn->close();
?>

