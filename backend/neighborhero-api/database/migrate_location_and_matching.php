<?php
/**
 * Migration script to add latitude, longitude, and matching engine fields
 * to live MySQL database.
 */

$configFile = __DIR__ . '/../config/Database.php';
$config = require $configFile;
$mysql = $config['mysql'];

$dsn = "mysql:host={$mysql['host']};port={$mysql['port']};dbname={$mysql['database']};charset={$mysql['charset']}";
$pdo = new PDO($dsn, $mysql['username'], $mysql['password'], $mysql['options']);

echo "Connected to MySQL database: {$mysql['database']}\n";

// 1. Check and update users table
$colsUsers = $pdo->query("SHOW COLUMNS FROM users")->fetchAll(PDO::FETCH_COLUMN);

$userAlters = [];
if (!in_array('latitude', $colsUsers)) {
    $userAlters[] = "ADD COLUMN latitude DECIMAL(10, 7) NULL";
}
if (!in_array('longitude', $colsUsers)) {
    $userAlters[] = "ADD COLUMN longitude DECIMAL(10, 7) NULL";
}
if (!in_array('is_available_today', $colsUsers)) {
    $userAlters[] = "ADD COLUMN is_available_today TINYINT(1) NOT NULL DEFAULT 1";
}
if (!in_array('available_days', $colsUsers)) {
    $userAlters[] = "ADD COLUMN available_days VARCHAR(255) NOT NULL DEFAULT 'Monday,Tuesday,Wednesday,Thursday,Friday,Saturday,Sunday'";
}
if (!in_array('available_time_slots', $colsUsers)) {
    $userAlters[] = "ADD COLUMN available_time_slots VARCHAR(255) NOT NULL DEFAULT 'Morning,Afternoon,Evening'";
}
if (!in_array('cancellation_count', $colsUsers)) {
    $userAlters[] = "ADD COLUMN cancellation_count INT NOT NULL DEFAULT 0";
}
if (!in_array('avg_response_minutes', $colsUsers)) {
    $userAlters[] = "ADD COLUMN avg_response_minutes INT NOT NULL DEFAULT 15";
}

if (!empty($userAlters)) {
    $sql = "ALTER TABLE users " . implode(', ', $userAlters);
    $pdo->exec($sql);
    echo "Added " . count($userAlters) . " columns to users table.\n";
} else {
    echo "Users table already up to date.\n";
}

// 2. Check and update help_requests table
$colsReq = $pdo->query("SHOW COLUMNS FROM help_requests")->fetchAll(PDO::FETCH_COLUMN);

$reqAlters = [];
if (!in_array('latitude', $colsReq)) {
    $reqAlters[] = "ADD COLUMN latitude DECIMAL(10, 7) NULL";
}
if (!in_array('longitude', $colsReq)) {
    $reqAlters[] = "ADD COLUMN longitude DECIMAL(10, 7) NULL";
}
if (!in_array('preferred_time_slot', $colsReq)) {
    $reqAlters[] = "ADD COLUMN preferred_time_slot VARCHAR(64) NULL";
}

if (!empty($reqAlters)) {
    $sql = "ALTER TABLE help_requests " . implode(', ', $reqAlters);
    $pdo->exec($sql);
    echo "Added " . count($reqAlters) . " columns to help_requests table.\n";
} else {
    echo "help_requests table already up to date.\n";
}

echo "Migration completed successfully!\n";
