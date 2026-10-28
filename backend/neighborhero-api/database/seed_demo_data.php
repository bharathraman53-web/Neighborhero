<?php
declare(strict_types=1);

/**
 * Optional Seed Script for Development & Testing Environments.
 * Usage: php database/seed_demo_data.php
 */

require_once __DIR__ . '/../bootstrap.php';

use NeighborHero\Services\Database;

$pdo = Database::getConnection();

echo "Seeding NeighborHero development database...\n";

// Password for demo users: 'Password123!'
$passwordHash = password_hash('Password123!', PASSWORD_DEFAULT);

// 1. Seed Demo Users
$users = [
    [
        'id' => 'usr_eleanor',
        'full_name' => 'Eleanor Vance',
        'email' => 'eleanor@example.com',
        'phone' => '(555) 234-5678',
        'password_hash' => $passwordHash,
        'neighborhood' => 'Maplewood & Oakridge',
        'bio' => 'Passionate gardener and retired teacher who loves helping out with local grocery runs, pet sitting, and weekend tutoring.',
        'points' => 350,
        'completed_tasks_count' => 24,
        'rating' => 4.90,
        'skills' => ['Groceries', 'Repairs', 'Pet Care', 'Tutoring'],
    ],
    [
        'id' => 'usr_vikram',
        'full_name' => 'Vikram Roy',
        'email' => 'vikram@example.com',
        'phone' => '(555) 345-6789',
        'password_hash' => $passwordHash,
        'neighborhood' => 'Greenwood',
        'bio' => 'Hey neighbor! I’m Vikram, a weekend DIY enthusiast and community handy-person. Always happy to lend a helping hand.',
        'points' => 420,
        'completed_tasks_count' => 42,
        'rating' => 4.90,
        'skills' => ['Heavy Lifting', 'Furniture Assembly', 'Yard Work', 'Home Repairs', 'Moving Help'],
    ],
    [
        'id' => 'usr_aarav',
        'full_name' => 'Aarav Sharma',
        'email' => 'aarav@example.com',
        'phone' => '(555) 456-7890',
        'password_hash' => $passwordHash,
        'neighborhood' => 'Maplewood St.',
        'bio' => 'Retired paramedic and garden volunteer. Happy to assist neighbors with grocery runs, medical drop-offs, and plant care.',
        'points' => 380,
        'completed_tasks_count' => 38,
        'rating' => 5.00,
        'skills' => ['Groceries', 'Pet Care', 'Senior Support', 'Plant Care'],
    ],
    [
        'id' => 'usr_priya',
        'full_name' => 'Priya Patel',
        'email' => 'priya@example.com',
        'phone' => '(555) 567-8901',
        'password_hash' => $passwordHash,
        'neighborhood' => 'Oakridge Park',
        'bio' => 'Computer science graduate student offering tech troubleshooting, Wi-Fi setup, and STEM tutoring for middle schoolers.',
        'points' => 290,
        'completed_tasks_count' => 29,
        'rating' => 4.90,
        'skills' => ['Tutoring', 'Tech Support', 'Wi-Fi Setup', 'Coding Help'],
    ],
];

$userStmt = $pdo->prepare(
    "INSERT INTO users (id, full_name, email, phone, password_hash, neighborhood, bio, is_verified, points, completed_tasks_count, rating)
     VALUES (:id, :full_name, :email, :phone, :password_hash, :neighborhood, :bio, 1, :points, :tasks, :rating)
     ON DUPLICATE KEY UPDATE full_name = VALUES(full_name), bio = VALUES(bio), points = VALUES(points)"
);

$skillStmt = $pdo->prepare(
    "INSERT INTO helper_skills (id, user_id, skill_name)
     VALUES (:id, :user_id, :skill)
     ON DUPLICATE KEY UPDATE skill_name = VALUES(skill_name)"
);

foreach ($users as $u) {
    $userStmt->execute([
        ':id' => $u['id'],
        ':full_name' => $u['full_name'],
        ':email' => $u['email'],
        ':phone' => $u['phone'],
        ':password_hash' => $u['password_hash'],
        ':neighborhood' => $u['neighborhood'],
        ':bio' => $u['bio'],
        ':points' => $u['points'],
        ':tasks' => $u['completed_tasks_count'],
        ':rating' => $u['rating'],
    ]);

    foreach ($u['skills'] as $s) {
        $skillStmt->execute([
            ':id' => 'sk_' . md5($u['id'] . $s),
            ':user_id' => $u['id'],
            ':skill' => $s,
        ]);
    }
}

// 2. Seed Sample Help Requests
$requests = [
    [
        'id' => 'req_demo_1',
        'requester_id' => 'usr_eleanor',
        'title' => 'Weekly Groceries Pickup',
        'category' => 'Groceries',
        'description' => 'Recovering from knee surgery and unable to drive. Need someone to pick up pre-paid groceries from Maplewood Co-op and drop them on front porch.',
        'location' => 'Maplewood District',
        'distance' => '0.3 mi away',
        'requested_time' => 'Due Today, 4:00 PM',
        'is_urgent' => 1,
        'status' => 'OPEN',
        'assigned_helper_id' => null,
    ],
    [
        'id' => 'req_demo_2',
        'requester_id' => 'usr_vikram',
        'title' => 'Garden Fence Repair & Mulch',
        'category' => 'Repairs',
        'description' => 'Storm damaged the backyard wooden fence panel. Need a handy neighbor to assist with securing 2 fence posts and laying fresh mulch.',
        'location' => 'Oakridge Avenue',
        'distance' => '0.6 mi away',
        'requested_time' => 'Tomorrow, 10:00 AM',
        'is_urgent' => 0,
        'status' => 'OPEN',
        'assigned_helper_id' => null,
    ],
    [
        'id' => 'req_demo_3',
        'requester_id' => 'usr_aarav',
        'title' => 'Golden Retriever Afternoon Walk',
        'category' => 'Pet Care',
        'description' => 'Looking for a dog lover to take Buster for a 30-minute stroll around Central Park while I am in afternoon meetings.',
        'location' => 'Pinecrest Ave',
        'distance' => '0.8 mi away',
        'requested_time' => 'Today, 2:30 PM',
        'is_urgent' => 0,
        'status' => 'IN_PROGRESS',
        'assigned_helper_id' => 'usr_eleanor',
    ],
];

$reqStmt = $pdo->prepare(
    "INSERT INTO help_requests (id, requester_id, title, category, description, location, distance, requested_time, is_urgent, status, assigned_helper_id)
     VALUES (:id, :requester_id, :title, :category, :description, :location, :distance, :requested_time, :is_urgent, :status, :assigned_helper_id)
     ON DUPLICATE KEY UPDATE title = VALUES(title), status = VALUES(status)"
);

foreach ($requests as $r) {
    $reqStmt->execute([
        ':id' => $r['id'],
        ':requester_id' => $r['requester_id'],
        ':title' => $r['title'],
        ':category' => $r['category'],
        ':description' => $r['description'],
        ':location' => $r['location'],
        ':distance' => $r['distance'],
        ':requested_time' => $r['requested_time'],
        ':is_urgent' => $r['is_urgent'],
        ':status' => $r['status'],
        ':assigned_helper_id' => $r['assigned_helper_id'],
    ]);
}

// 3. Seed Sample Notifications
$notifs = [
    [
        'id' => 'notif_demo_1',
        'user_id' => 'usr_eleanor',
        'title' => 'Welcome to NeighborHero!',
        'message' => 'Your neighborhood is active. Discover community requests around you.',
        'type' => 'COMMUNITY_NEWS',
    ],
    [
        'id' => 'notif_demo_2',
        'user_id' => 'usr_eleanor',
        'title' => 'Safety Alert: Greenwood Storm Watch',
        'message' => 'High winds advisory for tonight. Check on vulnerable neighbors.',
        'type' => 'SAFETY_ALERT',
    ],
];

$notifStmt = $pdo->prepare(
    "INSERT INTO notifications (id, user_id, title, message, type)
     VALUES (:id, :user_id, :title, :message, :type)
     ON DUPLICATE KEY UPDATE title = VALUES(title)"
);

foreach ($notifs as $n) {
    $notifStmt->execute([
        ':id' => $n['id'],
        ':user_id' => $n['user_id'],
        ':title' => $n['title'],
        ':message' => $n['message'],
        ':type' => $n['type'],
    ]);
}

echo "Database successfully seeded with demo records!\n";

