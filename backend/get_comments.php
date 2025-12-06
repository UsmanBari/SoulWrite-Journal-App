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

if ($_SERVER['REQUEST_METHOD'] !== 'GET') {
    echo json_encode(['success' => false, 'message' => 'Invalid request method']);
    exit;
}

$journal_id = $_GET['journal_id'] ?? '';

if (empty($journal_id)) {
    echo json_encode(['success' => false, 'message' => 'Missing journal_id']);
    exit;
}

// Get comments with user info
$stmt = $conn->prepare("
    SELECT c.*, u.name as user_name
    FROM journal_comments c
    LEFT JOIN users u ON c.user_id = u.id
    WHERE c.journal_id = ?
    ORDER BY c.created_at DESC
");
$stmt->bind_param("i", $journal_id);
$stmt->execute();
$result = $stmt->get_result();

$comments = array();
while ($row = $result->fetch_assoc()) {
    $comments[] = array(
        "id" => $row['id'],
        "journal_id" => $row['journal_id'],
        "user_id" => $row['user_id'],
        "user_name" => $row['user_name'],
        "comment_text" => $row['comment_text'],
        "created_at" => $row['created_at']
    );
}

ob_clean();
echo json_encode([
    'success' => true,
    'comments' => $comments
]);

$conn->close();
?>

