<?php
// Database configuration for AwardSpace
$host = "fdb1032.awardspace.net";
$username = "4714604_soulwritedb";
$password = "barithegreat123";
$database = "4714604_soulwritedb";

// Create connection
$conn = new mysqli($host, $username, $password, $database);

// Check connection
if ($conn->connect_error) {
    die(json_encode(array("success" => false, "message" => "Connection failed: " . $conn->connect_error)));
}

// Set charset to utf8
$conn->set_charset("utf8");
?>

