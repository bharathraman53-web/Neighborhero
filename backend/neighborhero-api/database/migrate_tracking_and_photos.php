<?php
declare(strict_types=1);

$cfg = require __DIR__ . '/../config/database.php';
$db = $cfg['mysql'];
$pdo = new PDO(
    "mysql:host={$db['host']};port={$db['port']};dbname={$db['database']}",
    $db['username'],
    $db['password'],
    [PDO::ATTR_ERRMODE => PDO::ERRMODE_EXCEPTION]
);

// 1. Add photo_url to help_requests if not exists
$cols = $pdo->query("SHOW COLUMNS FROM help_requests LIKE 'photo_url'")->fetchAll();
if (empty($cols)) {
    $pdo->exec("ALTER TABLE help_requests ADD COLUMN photo_url VARCHAR(255) NULL AFTER preferred_time_slot");
    echo "Added photo_url column to help_requests\n";
} else {
    echo "photo_url column already exists in help_requests\n";
}

// 2. Create task_live_locations table
$pdo->exec("CREATE TABLE IF NOT EXISTS task_live_locations (
    request_id VARCHAR(64) NOT NULL PRIMARY KEY,
    helper_id VARCHAR(64) NOT NULL,
    latitude DECIMAL(10, 7) NOT NULL,
    longitude DECIMAL(10, 7) NOT NULL,
    heading DECIMAL(6, 2) NULL,
    accuracy DECIMAL(6, 2) NULL,
    speed DECIMAL(6, 2) NULL,
    is_active TINYINT(1) NOT NULL DEFAULT 1,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_tll_helper (helper_id),
    CONSTRAINT fk_tll_request FOREIGN KEY (request_id) REFERENCES help_requests(id) ON DELETE CASCADE,
    CONSTRAINT fk_tll_helper FOREIGN KEY (helper_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci");
echo "task_live_locations table ready\n";
