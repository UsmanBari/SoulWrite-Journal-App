<?php
header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: POST');

require_once 'config.php';

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $name = $_POST['name'] ?? '';
    $email = $_POST['email'] ?? '';
    $phone = $_POST['phone'] ?? '';
    $password = $_POST['password'] ?? '';

    // Validate inputs
    if (empty($name) || empty($email) || empty($password)) {
        echo json_encode(array("success" => false, "message" => "All fields are required"));
        exit();
    }

    // Check if email already exists
    $check_query = "SELECT id FROM users WHERE email = ?";
    $stmt = $conn->prepare($check_query);
    $stmt->bind_param("s", $email);
    $stmt->execute();
    $result = $stmt->get_result();

    if ($result->num_rows > 0) {
        echo json_encode(array("success" => false, "message" => "Email already exists"));
        exit();
    }

    // Hash password
    $hashed_password = password_hash($password, PASSWORD_DEFAULT);

    // Insert user
    $insert_query = "INSERT INTO users (name, email, phone, password, created_at) VALUES (?, ?, ?, ?, NOW())";
    $stmt = $conn->prepare($insert_query);
    $stmt->bind_param("ssss", $name, $email, $phone, $hashed_password);

    if ($stmt->execute()) {
        $user_id = $conn->insert_id;
        echo json_encode(array(
            "success" => true,
            "message" => "Registration successful",
            "user_id" => $user_id
        ));
    } else {
        echo json_encode(array("success" => false, "message" => "Registration failed"));
    }

    $stmt->close();
}

$conn->close();
?>

