<?php
header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');

require_once 'config.php';

if ($_SERVER['REQUEST_METHOD'] === 'GET') {
    $query = isset($_GET['query']) ? $_GET['query'] : '';
    $current_user_id = isset($_GET['current_user_id']) ? intval($_GET['current_user_id']) : 0;

    if (!empty($query)) {
        $search_term = "%{$query}%";

        $sql = "SELECT u.id, u.name, u.email, u.phone, u.profile_image_url,
                CASE WHEN f.id IS NOT NULL THEN 1 ELSE 0 END as is_following
                FROM users u
                LEFT JOIN followers f ON f.following_id = u.id AND f.follower_id = ?
                WHERE u.name LIKE ? OR u.email LIKE ?
                ORDER BY u.name ASC";

        $stmt = $conn->prepare($sql);
        $stmt->bind_param("iss", $current_user_id, $search_term, $search_term);
        $stmt->execute();
        $result = $stmt->get_result();

        $users = array();
        while ($row = $result->fetch_assoc()) {
            $users[] = array(
                "id" => $row['id'],
                "name" => $row['name'],
                "email" => $row['email'],
                "phone" => $row['phone'],
                "profile_image_url" => $row['profile_image_url'] ?: '',
                "is_following" => (bool)$row['is_following']
            );
        }

        echo json_encode(array("success" => true, "users" => $users));
        $stmt->close();
    } else {
        echo json_encode(array("success" => false, "message" => "Query parameter required"));
    }
}

$conn->close();
?>

