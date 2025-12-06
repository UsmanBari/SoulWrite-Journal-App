<?php
header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');

require_once 'config.php';

// Enable error logging
error_log("get_feed.php called with GET params: " . json_encode($_GET));

if ($_SERVER['REQUEST_METHOD'] === 'GET') {
    $user_id = isset($_GET['user_id']) ? intval($_GET['user_id']) : 0;

    error_log("get_feed.php - User ID: $user_id");

    if ($user_id > 0) {
        // Get user's own journals (all) + public journals from followed users
        $sql = "SELECT j.*, u.name as author_name, u.name as user_name, u.fcm_token as user_fcm_token
                FROM journals j
                LEFT JOIN users u ON j.user_id = u.id
                WHERE (j.user_id = ?)
                   OR (j.is_public = 1 AND j.user_id IN (
                       SELECT following_id FROM followers WHERE follower_id = ?
                   ))
                ORDER BY j.date DESC";

        error_log("get_feed.php - Executing SQL query for user_id: $user_id");

        $stmt = $conn->prepare($sql);
        if (!$stmt) {
            error_log("get_feed.php - SQL prepare failed: " . $conn->error);
            echo json_encode(array("success" => false, "message" => "Database error: " . $conn->error));
            exit();
        }

        $stmt->bind_param("ii", $user_id, $user_id);

        if (!$stmt->execute()) {
            error_log("get_feed.php - SQL execute failed: " . $stmt->error);
            echo json_encode(array("success" => false, "message" => "Query error: " . $stmt->error));
            exit();
        }

        $result = $stmt->get_result();
        error_log("get_feed.php - Query returned " . $result->num_rows . " rows");

        $journals = array();
        $count = 0;
        while ($row = $result->fetch_assoc()) {
            $journal_id = $row['id'];
            $count++;
            error_log("get_feed.php - Processing journal #$count: ID={$journal_id}, Title={$row['title']}, Author={$row['author_name']}");

            // Get like count
            $like_stmt = $conn->prepare("SELECT COUNT(*) as like_count FROM journal_likes WHERE journal_id = ?");
            $like_stmt->bind_param("i", $journal_id);
            $like_stmt->execute();
            $like_count = $like_stmt->get_result()->fetch_assoc()['like_count'];

            // Check if current user liked this
            $liked_stmt = $conn->prepare("SELECT id FROM journal_likes WHERE journal_id = ? AND user_id = ?");
            $liked_stmt->bind_param("ii", $journal_id, $user_id);
            $liked_stmt->execute();
            $is_liked = $liked_stmt->get_result()->num_rows > 0;

            // Get comment count
            $comment_stmt = $conn->prepare("SELECT COUNT(*) as comment_count FROM journal_comments WHERE journal_id = ?");
            $comment_stmt->bind_param("i", $journal_id);
            $comment_stmt->execute();
            $comment_count = $comment_stmt->get_result()->fetch_assoc()['comment_count'];

            $journals[] = array(
                "id" => (int)$row['id'],
                "user_id" => (int)$row['user_id'],
                "title" => $row['title'] ?? '',
                "content" => $row['content'] ?? '',
                "image_url" => $row['image_url'] ?? '',
                "thumbnail_url" => $row['thumbnail_url'] ?? '',
                "is_public" => (int)$row['is_public'],
                "date" => (int)$row['date'],
                "user_name" => $row['user_name'] ?? '',
                "user_fcm_token" => $row['user_fcm_token'] ?? '',
                "author_name" => $row['author_name'] ?? '',
                "like_count" => (int)$like_count,
                "comment_count" => (int)$comment_count,
                "is_liked" => $is_liked
            );
        }

        error_log("get_feed.php - Returning " . count($journals) . " journals in response");
        echo json_encode(array("success" => true, "journals" => $journals));
        $stmt->close();
    } else {
        error_log("get_feed.php - Error: Invalid user_id");
        echo json_encode(array("success" => false, "message" => "User ID required"));
    }
}

$conn->close();
?>

