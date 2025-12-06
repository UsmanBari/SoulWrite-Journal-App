<?php
header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');

require_once 'config.php';

if ($_SERVER['REQUEST_METHOD'] === 'GET') {
    $user_id = isset($_GET['user_id']) ? $_GET['user_id'] : '';
    $current_user_id = isset($_GET['current_user_id']) ? $_GET['current_user_id'] : '';

    if (!empty($user_id)) {
        // If viewing own profile, show all journals; else show only public journals
        if ($user_id == $current_user_id) {
            $sql = "SELECT j.*, u.name as user_name
                    FROM journals j
                    LEFT JOIN users u ON j.user_id = u.id
                    WHERE j.user_id = ?
                    ORDER BY j.date DESC";
            $stmt = $conn->prepare($sql);
            $stmt->bind_param("s", $user_id);
        } else {
            $sql = "SELECT j.*, u.name as user_name
                    FROM journals j
                    LEFT JOIN users u ON j.user_id = u.id
                    WHERE j.user_id = ? AND j.is_public = 1
                    ORDER BY j.date DESC";
            $stmt = $conn->prepare($sql);
            $stmt->bind_param("s", $user_id);
        }

        $stmt->execute();
        $result = $stmt->get_result();

        $journals = array();
        while ($row = $result->fetch_assoc()) {
            $journals[] = array(
                "id" => $row['id'],
                "user_id" => $row['user_id'],
                "title" => $row['title'],
                "content" => $row['content'],
                "image_url" => $row['image_url'] ?: '',
                "thumbnail_url" => $row['thumbnail_url'] ?: '',
                "is_public" => (int)$row['is_public'],
                "date" => (int)$row['date'],
                "user_name" => $row['user_name']
            );
        }

        echo json_encode(array("success" => true, "journals" => $journals));
        $stmt->close();
    } else {
        echo json_encode(array("success" => false, "message" => "User ID required"));
    }
}

$conn->close();
?>

