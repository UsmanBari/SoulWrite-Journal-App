<?php
header('Content-Type: text/plain');
require_once 'config.php';

// Test user_id - replace with your actual user ID
$user_id = $_GET['user_id'] ?? 1;

echo "Testing Feed for User ID: $user_id\n\n";

// Check if user exists
$check = $conn->query("SELECT id, name, email FROM users WHERE id = $user_id");
if ($check && $check->num_rows > 0) {
    $user = $check->fetch_assoc();
    echo "✅ User found: " . $user['name'] . " (Email: " . $user['email'] . ")\n\n";
} else {
    echo "❌ User not found!\n";
    exit;
}

// Check user's own journals
echo "--- User's Own Journals ---\n";
$own_journals = $conn->query("SELECT id, title, is_public, date FROM journals WHERE user_id = $user_id ORDER BY date DESC");
if ($own_journals && $own_journals->num_rows > 0) {
    echo "Found " . $own_journals->num_rows . " journal(s):\n";
    while ($j = $own_journals->fetch_assoc()) {
        echo "  - ID: {$j['id']}, Title: {$j['title']}, Public: {$j['is_public']}, Date: {$j['date']}\n";
    }
} else {
    echo "No journals found for this user.\n";
}
echo "\n";

// Check who user is following
echo "--- Following ---\n";
$following = $conn->query("SELECT following_id, u.name FROM followers f JOIN users u ON f.following_id = u.id WHERE follower_id = $user_id");
if ($following && $following->num_rows > 0) {
    echo "Following " . $following->num_rows . " user(s):\n";
    while ($f = $following->fetch_assoc()) {
        echo "  - ID: {$f['following_id']}, Name: {$f['name']}\n";

        // Check their public journals
        $their_journals = $conn->query("SELECT id, title, is_public FROM journals WHERE user_id = {$f['following_id']} AND is_public = 1");
        if ($their_journals && $their_journals->num_rows > 0) {
            echo "    Has {$their_journals->num_rows} public journal(s)\n";
        } else {
            echo "    No public journals\n";
        }
    }
} else {
    echo "Not following anyone.\n";
}
echo "\n";

// Test the actual query
echo "--- Feed Query Test ---\n";
$sql = "SELECT j.*, u.name as author_name, u.name as user_name
        FROM journals j
        LEFT JOIN users u ON j.user_id = u.id
        WHERE (j.user_id = $user_id)
           OR (j.is_public = 1 AND j.user_id IN (
               SELECT following_id FROM followers WHERE follower_id = $user_id
           ))
        ORDER BY j.date DESC";

echo "SQL Query:\n$sql\n\n";

// Test if query is valid first
if ($conn->error) {
    echo "Connection Error: " . $conn->error . "\n";
}

$result = $conn->query($sql);
if ($result && $result->num_rows > 0) {
    echo "✅ Feed query returned " . $result->num_rows . " journal(s):\n";
    while ($j = $result->fetch_assoc()) {
        echo "  - ID: {$j['id']}, Title: {$j['title']}, Author: {$j['author_name']}, Public: {$j['is_public']}\n";
    }
} else {
    echo "❌ Feed query returned no results.\n";
    echo "Error: " . $conn->error . "\n";
}

$conn->close();
?>

