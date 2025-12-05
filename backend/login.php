<?php
header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: POST');

require_once 'config.php';

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $email = $_POST['email'] ?? '';
    $password = $_POST['password'] ?? '';

    // Validate inputs
    if (empty($email) || empty($password)) {
        echo json_encode(array("success" => false, "message" => "All fields are required"));
        exit();
    }

    // Get user from database
    $query = "SELECT id, name, email, phone, password FROM users WHERE email = ?";
    $stmt = $conn->prepare($query);
    $stmt->bind_param("s", $email);
    $stmt->execute();
    $result = $stmt->get_result();

    if ($result->num_rows === 0) {
        echo json_encode(array("success" => false, "message" => "Invalid email or password"));
        exit();
    }

    $user = $result->fetch_assoc();

    // Verify password
    if (password_verify($password, $user['password'])) {
        unset($user['password']); // Remove password from response
        echo json_encode(array(
            "success" => true,
            "message" => "Login successful",
            "user" => $user
        ));
    } else {
        echo json_encode(array("success" => false, "message" => "Invalid email or password"));
    }

    $stmt->close();
}

$conn->close();
?>

