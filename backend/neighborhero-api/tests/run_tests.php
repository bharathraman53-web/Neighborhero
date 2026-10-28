<?php
declare(strict_types=1);

/**
 * NeighborHero Backend Test Suite
 * Executes in-memory unit and integration tests against SQLite PDO.
 * Run with: php tests/run_tests.php
 */

date_default_timezone_set('UTC');

spl_autoload_register(function (string $class): void {
    $prefix = 'NeighborHero\\';
    $baseDir = __DIR__ . '/../';
    $len = strlen($prefix);
    if (strncmp($prefix, $class, $len) !== 0) return;
    $file = $baseDir . str_replace('\\', '/', substr($class, $len)) . '.php';
    if (file_exists($file)) require $file;
});

use NeighborHero\Repositories\ChatRepository;
use NeighborHero\Repositories\HelperRepository;
use NeighborHero\Repositories\HelpRequestRepository;
use NeighborHero\Repositories\NotificationRepository;
use NeighborHero\Repositories\SafetyRepository;
use NeighborHero\Repositories\TaskRepository;
use NeighborHero\Repositories\UserRepository;
use NeighborHero\Services\AuthService;
use NeighborHero\Services\Database;
use NeighborHero\Validators\Validator;

$passed = 0;
$failed = 0;

function assertTest(bool $condition, string $name): void {
    global $passed, $failed;
    if ($condition) {
        $passed++;
        echo "  [PASS] {$name}\n";
    } else {
        $failed++;
        echo "  [FAIL] {$name}\n";
    }
}

echo "=== NeighborHero Backend Automated Test Suite ===\n\n";

// Set up in-memory SQLite database
$pdo = new PDO('sqlite::memory:', null, null, [
    PDO::ATTR_ERRMODE => PDO::ERRMODE_EXCEPTION,
    PDO::ATTR_DEFAULT_FETCH_MODE => PDO::FETCH_ASSOC,
]);
$pdo->exec('PRAGMA foreign_keys = ON;');
Database::setConnection($pdo);

// Initialize SQLite Schema
$pdo->exec("
    CREATE TABLE users (
        id VARCHAR(64) PRIMARY KEY,
        full_name VARCHAR(120) NOT NULL,
        email VARCHAR(191) NOT NULL UNIQUE,
        phone VARCHAR(32) NULL UNIQUE,
        password_hash VARCHAR(255) NOT NULL,
        neighborhood VARCHAR(120) NOT NULL DEFAULT '',
        bio TEXT NULL,
        avatar_url VARCHAR(255) NOT NULL DEFAULT '',
        is_verified INT NOT NULL DEFAULT 1,
        points INT NOT NULL DEFAULT 0,
        completed_tasks_count INT NOT NULL DEFAULT 0,
        rating REAL NOT NULL DEFAULT 5.00,
        created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
        updated_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
    );

    CREATE TABLE user_profiles (
        user_id VARCHAR(64) PRIMARY KEY,
        emergency_contact_name VARCHAR(120) NULL,
        emergency_contact_phone VARCHAR(32) NULL,
        interests TEXT NULL,
        created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
        updated_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
    );

    CREATE TABLE auth_tokens (
        id VARCHAR(64) PRIMARY KEY,
        user_id VARCHAR(64) NOT NULL,
        token_hash VARCHAR(64) NOT NULL UNIQUE,
        device_info VARCHAR(255) NULL,
        expires_at TEXT NOT NULL,
        revoked_at TEXT NULL,
        created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
    );

    CREATE TABLE helper_skills (
        id VARCHAR(64) PRIMARY KEY,
        user_id VARCHAR(64) NOT NULL,
        skill_name VARCHAR(64) NOT NULL,
        created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
        UNIQUE(user_id, skill_name)
    );

    CREATE TABLE help_requests (
        id VARCHAR(64) PRIMARY KEY,
        requester_id VARCHAR(64) NOT NULL,
        title VARCHAR(255) NOT NULL,
        category VARCHAR(64) NOT NULL,
        description TEXT NOT NULL,
        location VARCHAR(255) NOT NULL,
        distance VARCHAR(64) NOT NULL DEFAULT 'Nearby',
        requested_time VARCHAR(128) NOT NULL,
        is_urgent INT NOT NULL DEFAULT 0,
        status VARCHAR(32) NOT NULL DEFAULT 'OPEN',
        assigned_helper_id VARCHAR(64) NULL,
        created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
        updated_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
    );

    CREATE TABLE request_assignments (
        id VARCHAR(64) PRIMARY KEY,
        request_id VARCHAR(64) NOT NULL,
        helper_id VARCHAR(64) NOT NULL,
        status VARCHAR(32) NOT NULL DEFAULT 'ASSIGNED',
        assigned_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
        started_at TEXT NULL,
        completed_at TEXT NULL
    );

    CREATE TABLE task_status_history (
        id VARCHAR(64) PRIMARY KEY,
        request_id VARCHAR(64) NOT NULL,
        changed_by_user_id VARCHAR(64) NOT NULL,
        old_status VARCHAR(32) NOT NULL,
        new_status VARCHAR(32) NOT NULL,
        notes TEXT NULL,
        created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
    );

    CREATE TABLE task_completion_evidence (
        id VARCHAR(64) PRIMARY KEY,
        request_id VARCHAR(64) NOT NULL,
        helper_id VARCHAR(64) NOT NULL,
        notes TEXT NOT NULL,
        proof_photo_url VARCHAR(255) NULL,
        submitted_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
    );

    CREATE TABLE ratings (
        id VARCHAR(64) PRIMARY KEY,
        request_id VARCHAR(64) NOT NULL,
        reviewer_id VARCHAR(64) NOT NULL,
        helper_id VARCHAR(64) NOT NULL,
        rating INT NOT NULL,
        review_text TEXT NULL,
        tags VARCHAR(255) NULL,
        created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
        UNIQUE(request_id, reviewer_id)
    );

    CREATE TABLE community_points_ledger (
        id VARCHAR(64) PRIMARY KEY,
        user_id VARCHAR(64) NOT NULL,
        request_id VARCHAR(64) NULL,
        points INT NOT NULL,
        reason VARCHAR(255) NOT NULL,
        created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
        UNIQUE(user_id, request_id, reason)
    );

    CREATE TABLE chat_conversations (
        id VARCHAR(64) PRIMARY KEY,
        request_id VARCHAR(64) NULL,
        participant1_id VARCHAR(64) NOT NULL,
        participant2_id VARCHAR(64) NOT NULL,
        created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
        updated_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
    );

    CREATE TABLE chat_messages (
        id VARCHAR(64) PRIMARY KEY,
        conversation_id VARCHAR(64) NOT NULL,
        sender_id VARCHAR(64) NOT NULL,
        message_text TEXT NOT NULL,
        attachment_code VARCHAR(64) NULL,
        attachment_title VARCHAR(128) NULL,
        is_read INT NOT NULL DEFAULT 0,
        created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
    );

    CREATE TABLE notifications (
        id VARCHAR(64) PRIMARY KEY,
        user_id VARCHAR(64) NOT NULL,
        title VARCHAR(255) NOT NULL,
        message TEXT NOT NULL,
        type VARCHAR(64) NOT NULL,
        is_read INT NOT NULL DEFAULT 0,
        related_request_id VARCHAR(64) NULL,
        created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
    );

    CREATE TABLE sos_alerts (
        id VARCHAR(64) PRIMARY KEY,
        user_id VARCHAR(64) NOT NULL,
        latitude REAL NULL,
        longitude REAL NULL,
        address VARCHAR(255) NOT NULL DEFAULT 'Maplewood District',
        alert_type VARCHAR(64) NOT NULL DEFAULT 'EMERGENCY_SOS',
        status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
        created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
    );
");

// Initialize Repositories and Services
$authService = new AuthService($pdo);
$userRepo = new UserRepository($pdo);
$requestRepo = new HelpRequestRepository($pdo);
$helperRepo = new HelperRepository($pdo);
$taskRepo = new TaskRepository($pdo);
$chatRepo = new ChatRepository($pdo);
$notifRepo = new NotificationRepository($pdo);
$safetyRepo = new SafetyRepository($pdo);

echo "--- 1. Validation & Payload Security Tests ---\n";
$v = Validator::make(['email' => 'bad-email', 'name' => ''])
    ->required('name')
    ->email('email');
assertTest(!$v->passes(), "Validator catches missing required name and invalid email");
assertTest(count($v->errors()) === 2, "Validator reports exactly 2 validation errors");

$vGood = Validator::make(['name' => 'Sarah', 'email' => 'sarah@example.com'])
    ->required('name')
    ->email('email');
assertTest($vGood->passes(), "Validator passes for valid inputs");

echo "\n--- 2. User Registration & Password Security Tests ---\n";
$passHash = $authService->hashPassword("SecurePassword123!");
assertTest(str_starts_with($passHash, '$2y$'), "Passwords are encrypted using bcrypt password_hash()");
assertTest($authService->verifyPassword("SecurePassword123!", $passHash), "password_verify correctly authenticates correct password");
assertTest(!$authService->verifyPassword("WrongPassword!", $passHash), "password_verify rejects invalid password");

$user1 = $userRepo->create([
    'id' => 'usr_1',
    'full_name' => 'Sarah Jenkins',
    'email' => 'sarah@example.com',
    'phone' => '(555) 123-4567',
    'password_hash' => $passHash,
    'neighborhood' => 'Maplewood',
    'bio' => 'Dog lover and gardener.',
    'skills' => ['Pet Care', 'Groceries'],
]);
assertTest($user1['full_name'] === 'Sarah Jenkins', "User registered and profile created in database");
assertTest($user1['points'] === 0, "New user starts with 0 points (no fabricated points)");

// Duplicate registration prevention test
$duplicateEmailCaught = false;
try {
    $userRepo->create([
        'id' => 'usr_dup',
        'full_name' => 'Sarah Jenkins Imposter',
        'email' => 'sarah@example.com', // Duplicate email
        'phone' => '(555) 999-9999',
        'password_hash' => $passHash,
    ]);
} catch (\Exception $e) {
    $duplicateEmailCaught = true;
}
assertTest($duplicateEmailCaught, "Duplicate email registration rejected by database unique constraint");

// Legitimate users sharing same display name are permitted
$sameNameUser = $userRepo->create([
    'id' => 'usr_2',
    'full_name' => 'Sarah Jenkins', // Identical display name, distinct email/ID
    'email' => 'sarah2@example.com',
    'phone' => '(555) 987-6543',
    'password_hash' => $passHash,
    'neighborhood' => 'Oakridge',
    'skills' => ['Tutoring'],
]);
assertTest($sameNameUser['id'] === 'usr_2', "Legitimate users sharing the same display name are supported via stable IDs");

echo "\n--- 3. Token Session & Expiry Tests ---\n";
$token = $authService->createToken('usr_1');
assertTest(strlen($token) === 64, "Generated 256-bit secure hex bearer token");

$authCheck = $authService->authenticateToken($token);
assertTest($authCheck !== null && $authCheck['email'] === 'sarah@example.com', "Bearer token successfully resolves authenticated user via token hash");

// Token revocation
$authService->revokeToken($token);
assertTest($authService->authenticateToken($token) === null, "Revoked bearer token immediately rejected");

echo "\n--- 4. Help Request Creation & Retrieval Tests ---\n";
$req1 = $requestRepo->create([
    'id' => 'req_101',
    'requester_id' => 'usr_1',
    'title' => 'Need help walking Buster',
    'category' => 'Pet Care',
    'description' => 'Golden retriever needs 30 min stroll around park.',
    'location' => 'Maplewood District',
    'distance' => '0.3 mi away',
    'requested_time' => 'Today 3pm',
    'is_urgent' => false,
]);
assertTest($req1['id'] === 'req_101', "Help request created with stable ID");
assertTest($req1['status'] === 'OPEN', "New help request starts in OPEN status");
assertTest($req1['requester_name'] === 'Sarah Jenkins', "Requester name resolved from users table join");

$openRequests = $requestRepo->listAll('Pet Care', 'OPEN');
assertTest(count($openRequests) === 1, "Requests filterable by category and status");

echo "\n--- 5. Task Lifecycle & Points Duplication Prevention Tests ---\n";
// Assign helper (usr_2 accepts usr_1's request)
$assigned = $taskRepo->assignHelper('req_101', 'usr_2', 'usr_2');
assertTest($assigned['status'] === 'IN_PROGRESS', "Task transitions to IN_PROGRESS upon helper assignment");
assertTest($assigned['assigned_helper_id'] === 'usr_2', "Helper correctly assigned to request");

// Confirm start
$started = $taskRepo->confirmStart('req_101', 'usr_2');
assertTest($started['status'] === 'IN_PROGRESS', "Task start confirmed");

// Submit completion by helper
$submitted = $taskRepo->submitCompletion('req_101', 'usr_2', 'Walked Buster around the park for 35 mins.');
assertTest($submitted['status'] === 'PENDING_VERIFICATION', "Task transitions to PENDING_VERIFICATION upon completion submission");

// Verify completion by requester (usr_1)
$verified = $taskRepo->verifyCompletion('req_101', 'usr_1', 5, 'Buster was so happy! Great job.');
assertTest($verified['status'] === 'COMPLETED', "Task transitions to COMPLETED upon requester verification");

// Check helper earned points
$helperUser = $userRepo->findById('usr_2');
assertTest($helperUser['points'] === 150, "Helper awarded exactly 150 community points");
assertTest($helperUser['completed_tasks_count'] === 1, "Helper completed tasks count incremented");

// Test: Attempting to verify or award points a second time must fail
$duplicatePointsBlocked = false;
try {
    $taskRepo->verifyCompletion('req_101', 'usr_1', 5, 'Trying to verify again');
} catch (\Exception $e) {
    $duplicatePointsBlocked = true;
}
assertTest($duplicatePointsBlocked, "Repeated verification and duplicate points prevented server-side");
$helperUserCheck = $userRepo->findById('usr_2');
assertTest($helperUserCheck['points'] === 150, "Points remain 150 (not duplicated)");

echo "\n--- 6. Chat Authorization & Persistence Tests ---\n";
$sentMsg = $chatRepo->sendMessage('usr_1', 'Hi Sarah, are you ready?', null, 'usr_2');
assertTest($sentMsg['is_user'] === true, "Sender message flagged as is_user");
assertTest(!empty($sentMsg['id']), "Chat message assigned persistent stable ID");

$messages = $chatRepo->getMessages('usr_2');
assertTest(count($messages) === 1, "Recipient can retrieve message from persistent conversation");
assertTest($messages[0]['is_user'] === false, "Message correctly identified as incoming for recipient");

echo "\n--- 7. Notifications & Read Status Tests ---\n";
$notifRepo->createNotification('usr_1', 'Task Update', 'Your task was updated.', 'TASK_UPDATE');
$notifs = $notifRepo->getNotificationsForUser('usr_1');
assertTest(count($notifs) >= 1, "User can retrieve their notifications");
assertTest($notifs[0]['is_read'] === false, "New notification starts unread");

$notifRepo->markAsRead($notifs[0]['id'], 'usr_1');
$notifsUpdated = $notifRepo->getNotificationsForUser('usr_1');
assertTest($notifsUpdated[0]['is_read'] === true, "Notification successfully marked as read");

echo "\n--- 8. Safety & Emergency SOS Tests ---\n";
$sos = $safetyRepo->triggerSos('usr_1', 37.7749, -122.4194, 'Maplewood Ave');
assertTest($sos['status'] === 'ACTIVE', "Emergency SOS alert created in ACTIVE state");
$safetyRepo->resolveSos($sos['id'], 'usr_1');

$history = $safetyRepo->getTaskHistory('usr_1');
assertTest(count($history) === 1, "Completed task visible in user real task history");
assertTest($history[0]['status'] === 'Completed', "Task history derived from real database status");

echo "\n==================================================\n";
echo "Total Tests Run: " . ($passed + $failed) . "\n";
echo "Passed: {$passed} | Failed: {$failed}\n";
echo "==================================================\n";

if ($failed > 0) {
    exit(1);
}

