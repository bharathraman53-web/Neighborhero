<?php
require_once __DIR__ . '/../config/database.php';

try {
    $cfg = require __DIR__ . '/../config/database.php';
    $dbConfig = $cfg['mysql'];
    $pdo = new PDO(
        "mysql:host={$dbConfig['host']};port={$dbConfig['port']};dbname={$dbConfig['database']};charset={$dbConfig['charset']}",
        $dbConfig['username'],
        $dbConfig['password'],
        [PDO::ATTR_ERRMODE => PDO::ERRMODE_EXCEPTION]
    );

    // 1. Update existing in_progress request to "Help with groceries"
    $stmt1 = $pdo->prepare("UPDATE help_requests SET title = :title, description = :desc, category = :cat, requested_time = :rtime, location = :loc, distance = :dist, status = :status, is_urgent = 0 WHERE id = 'req_8c36f455ca602645aba0'");
    $stmt1->execute([
        ':title' => 'Help with groceries',
        ':desc' => 'Buy some groceries from the store.',
        ':cat' => 'Groceries',
        ':rtime' => 'today 5pm',
        ':loc' => 'Maplewood District',
        ':dist' => '0.4 mi away',
        ':status' => 'IN_PROGRESS'
    ]);

    // 2. Ensure "Need groceries ASAP" exists
    $stmtCheck1 = $pdo->prepare("SELECT id FROM help_requests WHERE title = 'Need groceries ASAP'");
    $stmtCheck1->execute();
    if (!$stmtCheck1->fetch()) {
        $stmtIns1 = $pdo->prepare("INSERT INTO help_requests (id, requester_id, title, category, description, location, distance, requested_time, is_urgent, status) VALUES (:id, :rid, :title, :cat, :desc, :loc, :dist, :rtime, 1, 'OPEN')");
        $stmtIns1->execute([
            ':id' => 'req_groceries_asap_' . substr(md5(uniqid()), 0, 8),
            ':rid' => 'usr_leg_2_9e7cdd93',
            ':title' => 'Need groceries ASAP',
            ':cat' => 'Groceries',
            ':desc' => 'Need someone to buy groceries for my family.',
            ':loc' => 'Maplewood District',
            ':dist' => '2.5 km',
            ':rtime' => 'today 4pm'
        ]);
    } else {
        $pdo->exec("UPDATE help_requests SET category = 'Groceries', description = 'Need someone to buy groceries for my family.', distance = '2.5 km', requested_time = 'today 4pm', is_urgent = 1, status = 'OPEN' WHERE title = 'Need groceries ASAP'");
    }

    // 3. Ensure "Old Textbooks" exists
    $stmtCheck2 = $pdo->prepare("SELECT id FROM help_requests WHERE title = 'Old Textbooks'");
    $stmtCheck2->execute();
    if (!$stmtCheck2->fetch()) {
        $stmtIns2 = $pdo->prepare("INSERT INTO help_requests (id, requester_id, title, category, description, location, distance, requested_time, is_urgent, status) VALUES (:id, :rid, :title, :cat, :desc, :loc, :dist, :rtime, 0, 'OPEN')");
        $stmtIns2->execute([
            ':id' => 'req_old_textbooks_' . substr(md5(uniqid()), 0, 8),
            ':rid' => 'usr_leg_4_d246cd5d',
            ':title' => 'Old Textbooks',
            ':cat' => 'Books',
            ':desc' => 'Looking for 2nd year textbooks.',
            ':loc' => 'Oakridge Avenue',
            ':dist' => '3.1 km',
            ':rtime' => 'today 6pm'
        ]);
    } else {
        $pdo->exec("UPDATE help_requests SET category = 'Books', description = 'Looking for 2nd year textbooks.', distance = '3.1 km', requested_time = 'today 6pm', is_urgent = 0, status = 'OPEN' WHERE title = 'Old Textbooks'");
    }

    echo "SYNC_SUCCESS\n";
} catch (Exception $e) {
    echo "ERROR: " . $e->getMessage() . "\n";
}
