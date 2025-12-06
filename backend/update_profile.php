<?php
header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: POST');

require_once 'config.php';

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $user_id = isset($_POST['user_id']) ? $_POST['user_id'] : '';
    $name = isset($_POST['name']) ? $_POST['name'] : null;
    $phone = isset($_POST['phone']) ? $_POST['phone'] : null;
    $profile_image = isset($_POST['profile_image']) ? $_POST['profile_image'] : null;

    if (empty($user_id)) {
        echo json_encode(array("success" => false, "message" => "User ID is required"));
        exit();
    }

    // Build dynamic update query
    $updates = array();
    $params = array();
    $types = "";

    if ($name !== null) {
        $updates[] = "name = ?";
        $params[] = $name;
        $types .= "s";
    }

    if ($phone !== null) {
        $updates[] = "phone = ?";
        $params[] = $phone;
        $types .= "s";
    }

    if ($profile_image !== null) {
        $updates[] = "profile_image = ?";
        $params[] = $profile_image;
        $types .= "s";
    }

    if (empty($updates)) {
        echo json_encode(array("success" => false, "message" => "No fields to update"));
        exit();
    }

    // Add user_id to params
    $params[] = $user_id;
    $types .= "s";

    $sql = "UPDATE users SET " . implode(", ", $updates) . " WHERE id = ?";
    $stmt = $conn->prepare($sql);

    // Bind parameters dynamically
    $bind_params = array($types);
    foreach ($params as $key => $param) {
        $bind_params[] = &$params[$key];
    }
    call_user_func_array(array($stmt, 'bind_param'), $bind_params);

    if ($stmt->execute()) {
        // Get updated user data
        $sql_select = "SELECT id, name, email, phone, profile_image FROM users WHERE id = ?";
        $stmt_select = $conn->prepare($sql_select);
        $stmt_select->bind_param("s", $user_id);
        $stmt_select->execute();
        $result = $stmt_select->get_result();

        if ($row = $result->fetch_assoc()) {
            echo json_encode(array(
                "success" => true,
                "message" => "Profile updated successfully",
                "user" => array(
                    "id" => $row['id'],
                    "name" => $row['name'],
                    "email" => $row['email'],
                    "phone" => $row['phone'],
                    "profile_image" => $row['profile_image']
                )
            ));
        } else {
            echo json_encode(array("success" => true, "message" => "Profile updated"));
        }

        $stmt_select->close();
    } else {
        echo json_encode(array("success" => false, "message" => "Failed to update profile"));
    }

    $stmt->close();
} else {
    echo json_encode(array("success" => false, "message" => "Invalid request method"));
}

$conn->close();
?>

