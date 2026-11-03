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
$pdo->sqliteCreateFunction('CONCAT', function(...$args) {
    return implode('', $args);
});
$pdo->sqliteCreateFunction('RAND', function() {
    return mt_rand() / mt_getrandmax();
});
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
        latitude REAL NULL,
        longitude REAL NULL,
        is_available_today INT NOT NULL DEFAULT 1,
        available_days VARCHAR(255) NOT NULL DEFAULT 'Monday,Tuesday,Wednesday,Thursday,Friday,Saturday,Sunday',
        available_time_slots VARCHAR(255) NOT NULL DEFAULT 'Morning,Afternoon,Evening',
        cancellation_count INT NOT NULL DEFAULT 0,
        avg_response_minutes INT NOT NULL DEFAULT 15,
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

    CREATE TABLE helper_availability (
        id VARCHAR(64) PRIMARY KEY,
        user_id VARCHAR(64) NOT NULL,
        day_of_week VARCHAR(16) NOT NULL,
        start_time VARCHAR(8) NOT NULL DEFAULT '08:00',
        end_time VARCHAR(8) NOT NULL DEFAULT '20:00',
        is_active INT NOT NULL DEFAULT 1,
        created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
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
        latitude REAL NULL,
        longitude REAL NULL,
        preferred_time_slot VARCHAR(64) NULL,
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
        client_message_id VARCHAR(64) NULL,
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
");

// Initialize Repositories and Services
$authService = new AuthService($pdo);
$userRepo = new UserRepository($pdo);
$requestRepo = new HelpRequestRepository($pdo);
$helperRepo = new HelperRepository($pdo);
$taskRepo = new TaskRepository($pdo);
$chatRepo = new ChatRepository($pdo);
$notifRepo = new NotificationRepository($pdo);

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

// Phone number strict 10-digit validation tests
$vPhoneGood = Validator::make(['phone' => '5551234567'])->phone10('phone');
assertTest($vPhoneGood->passes(), "Validator accepts valid 10-digit mobile number");

$vPhone9 = Validator::make(['phone' => '555123456'])->phone10('phone');
assertTest(!$vPhone9->passes(), "Validator rejects 9-digit mobile number");

$vPhone11 = Validator::make(['phone' => '55512345678'])->phone10('phone');
assertTest(!$vPhone11->passes(), "Validator rejects 11-digit mobile number");

$vPhoneSymbols = Validator::make(['phone' => '(555) 123-4567'])->phone10('phone');
assertTest(!$vPhoneSymbols->passes(), "Validator rejects phone with punctuation/formatting");

$vPhoneAlpha = Validator::make(['phone' => '555abc4567'])->phone10('phone');
assertTest(!$vPhoneAlpha->passes(), "Validator rejects phone containing alphabet letters");

$vPhoneEmpty = Validator::make(['phone' => ''])->phone10('phone');
assertTest(!$vPhoneEmpty->passes(), "Validator rejects empty phone input");

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

// Profile photo upload persistence
$userRepo->updateProfile('usr_1', ['avatar_url' => 'uploads/avatars/avatar_test123.jpg']);
$u1WithAvatar = $userRepo->findById('usr_1');
assertTest($u1WithAvatar['avatar_url'] === 'uploads/avatars/avatar_test123.jpg', "Profile photo URL persists and is retrievable from user profile");

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

$openPetHelpRequests = $requestRepo->listAll('Pet Help', 'OPEN');
assertTest(count($openPetHelpRequests) === 1, "Requests filterable by canonical 'Pet Help' returns requests created under 'Pet Care'");

// Test canonical categories creation
$reqBooks = $requestRepo->create([
    'id' => 'req_books_1',
    'requester_id' => 'usr_1',
    'title' => 'Need chemistry textbook for semester',
    'category' => 'Books',
    'description' => 'Borrowing 2nd edition college chemistry book',
    'location' => 'Maplewood',
    'distance' => '0.5 mi',
    'requested_time' => 'Tomorrow 10am',
    'is_urgent' => false,
]);
assertTest($reqBooks['category'] === 'Books', "Help request created with canonical category 'Books'");

$reqTransport = $requestRepo->create([
    'id' => 'req_trans_1',
    'requester_id' => 'usr_1',
    'title' => 'Ride to medical clinic',
    'category' => 'Transport',
    'description' => 'Need a ride 3 miles to clinic and back',
    'location' => 'Maplewood',
    'distance' => '0.2 mi',
    'requested_time' => 'Friday 2pm',
    'is_urgent' => false,
]);
assertTest($reqTransport['category'] === 'Transport', "Help request created with canonical category 'Transport'");

// Helper matching alias and distinction tests
$pdo->exec("UPDATE users SET is_verified = 1 WHERE id IN ('usr_1', 'usr_2')");
$petHelpers = $helperRepo->getRecommendations('Pet Help');
assertTest(count($petHelpers) >= 1 && $petHelpers[0]['id'] === 'usr_1', "Helper recommendation: 'Pet Help' query matches helper with 'Pet Care' skill");

$bookHelpers = $helperRepo->getRecommendations('Books');
assertTest(count($bookHelpers) === 0, "Helper recommendation: 'Books' does not conflate with 'Tutoring'");

$tutoringHelpers = $helperRepo->getRecommendations('Tutoring');
assertTest(count($tutoringHelpers) >= 1 && $tutoringHelpers[0]['id'] === 'usr_2', "Helper recommendation: 'Tutoring' returns helper with distinct Tutoring skill");

// Avatar Exposure and Direct Assignment Tests
$userRepo->updateProfile('usr_2', ['avatar_url' => 'uploads/avatars/helper_bob.jpg']);
$reqDirect = $requestRepo->create([
    'id' => 'req_direct_1',
    'requester_id' => 'usr_1',
    'title' => 'Direct tutoring session for science',
    'category' => 'Books',
    'description' => 'Direct assignment request',
    'location' => 'Maplewood',
    'distance' => '0.1 mi',
    'requested_time' => 'Saturday 11am',
    'is_urgent' => false,
    'assigned_helper_id' => 'usr_2',
]);
assertTest($reqDirect['assigned_helper_id'] === 'usr_2', "Direct help request assigned helper set to target helper ID");
assertTest($reqDirect['status'] === 'IN_PROGRESS', "Direct help request initialized immediately to IN_PROGRESS");

// Query direct request through findById to verify avatars mapped
$fetchedDirect = $requestRepo->findById('req_direct_1');
assertTest($fetchedDirect !== null, "Direct help request retrievable by ID");
assertTest($fetchedDirect['requester_avatar'] === 'uploads/avatars/avatar_test123.jpg', "Direct help request includes requester avatar URL");
assertTest($fetchedDirect['assigned_helper_avatar'] === 'uploads/avatars/helper_bob.jpg', "Direct help request includes assigned helper avatar URL");

// Verify listAll also exposes avatars
$allDirectList = $requestRepo->listAll('Books', 'IN_PROGRESS');
assertTest(count($allDirectList) >= 1 && !empty($allDirectList[0]['requester_avatar']), "HelpRequestRepository::listAll exposes requester avatar URL");
assertTest(count($allDirectList) >= 1 && !empty($allDirectList[0]['assigned_helper_avatar']), "HelpRequestRepository::listAll exposes assigned helper avatar URL");

// Verify HelperRepository exposes avatar_url and avatarUrl
$helperWithAvatar = $helperRepo->findById('usr_2');
assertTest($helperWithAvatar !== null && $helperWithAvatar['avatar_url'] === 'uploads/avatars/helper_bob.jpg', "HelperRepository::findById exposes avatar_url");
assertTest($helperWithAvatar['avatarUrl'] === 'uploads/avatars/helper_bob.jpg', "HelperRepository::findById exposes camelCase avatarUrl");

$recHelpers = $helperRepo->getRecommendations('Tutoring');
assertTest(count($recHelpers) >= 1 && $recHelpers[0]['avatar_url'] === 'uploads/avatars/helper_bob.jpg', "HelperRepository::getRecommendations exposes avatar_url");
assertTest(count($recHelpers) >= 1 && $recHelpers[0]['avatarUrl'] === 'uploads/avatars/helper_bob.jpg', "HelperRepository::getRecommendations exposes camelCase avatarUrl");

// GPS Coordinates Persistence Tests
$reqGps = $requestRepo->create([
    'id' => 'req_gps_test',
    'requester_id' => 'usr_1',
    'title' => 'GPS tagged request in Maplewood',
    'category' => 'Repairs',
    'description' => 'Accurate location repair',
    'location' => 'Maplewood District',
    'distance' => '0.2 mi',
    'requested_time' => 'Today 3pm',
    'is_urgent' => false,
    'latitude' => 37.774921,
    'longitude' => -122.419415,
]);
assertTest($reqGps['latitude'] !== null && abs($reqGps['latitude'] - 37.774921) < 0.0001, "Help request stores and returns accurate latitude");
assertTest($reqGps['longitude'] !== null && abs($reqGps['longitude'] - -122.419415) < 0.0001, "Help request stores and returns accurate longitude");

// Profile coordinates update test
$userRepo->updateProfile('usr_1', ['latitude' => 37.775010, 'longitude' => -122.419520]);
$u1WithGps = $userRepo->findById('usr_1');
assertTest($u1WithGps['latitude'] !== null && abs($u1WithGps['latitude'] - 37.775010) < 0.0001, "User profile stores and returns accurate latitude");
assertTest($u1WithGps['longitude'] !== null && abs($u1WithGps['longitude'] - -122.419520) < 0.0001, "User profile stores and returns accurate longitude");

// Direct request notification creation
$notifRepo->create([
    'user_id' => 'usr_2',
    'title' => 'New Direct Help Request',
    'message' => 'Sarah Jenkins requested your help for: Direct tutoring session for science',
    'type' => 'REQUEST_ASSIGNED',
    'related_request_id' => 'req_direct_1',
]);
$helperNotifs = $notifRepo->getNotificationsForUser('usr_2');
$directNotifFound = false;
foreach ($helperNotifs as $n) {
    if ($n['type'] === 'REQUEST_ASSIGNED' && $n['related_request_id'] === 'req_direct_1') {
        $directNotifFound = true;
        break;
    }
}
assertTest($directNotifFound, "Assigned helper received REQUEST_ASSIGNED in-app notification for direct request");

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

// Test: Evidence photo persistence and participant access control
$req2 = $requestRepo->create([
    'id' => 'req_102',
    'requester_id' => 'usr_1',
    'title' => 'Fix wooden fence latch',
    'category' => 'Repairs',
    'description' => 'Back gate latch broke.',
    'location' => 'Maplewood District',
    'distance' => '0.2 mi away',
    'requested_time' => 'Tomorrow 10am',
    'is_urgent' => false,
]);
$taskRepo->assignHelper('req_102', 'usr_2', 'usr_2');
$taskRepo->confirmStart('req_102', 'usr_2');
$taskRepo->submitCompletion('req_102', 'usr_2', 'Replaced latch screws and tested alignment.', 'uploads/evidence/evidence_fence123.jpg');

// Requester can view evidence
$evForRequester = $taskRepo->getEvidence('req_102', 'usr_1');
assertTest($evForRequester['proof_photo_url'] === 'uploads/evidence/evidence_fence123.jpg', "Evidence proof photo persisted and retrieved by requester");
assertTest(str_contains($evForRequester['notes'], 'Replaced latch'), "Evidence notes retrieved correctly");

// Helper can view evidence
$evForHelper = $taskRepo->getEvidence('req_102', 'usr_2');
assertTest($evForHelper['proof_photo_url'] === 'uploads/evidence/evidence_fence123.jpg', "Evidence proof photo accessible to assigned helper");

// Unauthorized user (usr_charlie) blocked with 403 Forbidden
$unauthorizedEvidenceBlocked = false;
try {
    $taskRepo->getEvidence('req_102', 'usr_3');
} catch (\Exception $e) {
    if ($e->getCode() === 403) {
        $unauthorizedEvidenceBlocked = true;
    }
}
assertTest($unauthorizedEvidenceBlocked, "Unauthorized third party blocked from viewing task evidence (HTTP 403)");

// Role Permissions, Anti-Self-Assignment, and Duplicate Submission Prevention Tests
$req3 = $requestRepo->create([
    'id' => 'req_103',
    'requester_id' => 'usr_1',
    'title' => 'Grocery delivery assistance',
    'category' => 'Groceries',
    'description' => 'Need help picking up groceries from supermarket.',
    'location' => 'Maplewood District',
    'distance' => '0.5 mi away',
    'requested_time' => 'Today 5pm',
    'is_urgent' => false,
]);

// 1. Requester cannot accept / assign self to their own request
$selfAssignBlocked = false;
try {
    $taskRepo->assignHelper('req_103', 'usr_1', 'usr_1');
} catch (\Exception $e) {
    if ($e->getCode() === 403 && str_contains($e->getMessage(), 'Requesters cannot accept')) {
        $selfAssignBlocked = true;
    }
}
assertTest($selfAssignBlocked, "Requester cannot accept or assign themselves to their own request (HTTP 403)");

// Properly assign usr_2 as helper
$taskRepo->assignHelper('req_103', 'usr_2', 'usr_2');

// 2. Requester cannot submit completion for their own request
$requesterSubmitBlocked = false;
try {
    $taskRepo->submitCompletion('req_103', 'usr_1', 'Trying to submit my own request');
} catch (\Exception $e) {
    if ($e->getCode() === 403 && str_contains($e->getMessage(), 'Requesters cannot submit completion')) {
        $requesterSubmitBlocked = true;
    }
}
assertTest($requesterSubmitBlocked, "Requester blocked from submitting task completion for own request (HTTP 403)");

// 3. Unassigned third-party cannot submit completion
$thirdPartySubmitBlocked = false;
try {
    $taskRepo->submitCompletion('req_103', 'usr_3', 'Third party trying to submit');
} catch (\Exception $e) {
    if ($e->getCode() === 403 && str_contains($e->getMessage(), 'Only the assigned helper')) {
        $thirdPartySubmitBlocked = true;
    }
}
assertTest($thirdPartySubmitBlocked, "Unassigned third party blocked from submitting task completion (HTTP 403)");

// 4. Evidence upload authorization checks
$requesterEvidenceUploadBlocked = false;
try {
    $taskRepo->validateEvidenceUpload('req_103', 'usr_1');
} catch (\Exception $e) {
    if ($e->getCode() === 403 && str_contains($e->getMessage(), 'Requesters cannot upload')) {
        $requesterEvidenceUploadBlocked = true;
    }
}
assertTest($requesterEvidenceUploadBlocked, "Requester blocked from uploading completion evidence (HTTP 403)");

$thirdPartyEvidenceUploadBlocked = false;
try {
    $taskRepo->validateEvidenceUpload('req_103', 'usr_3');
} catch (\Exception $e) {
    if ($e->getCode() === 403 && str_contains($e->getMessage(), 'Only the assigned helper')) {
        $thirdPartyEvidenceUploadBlocked = true;
    }
}
assertTest($thirdPartyEvidenceUploadBlocked, "Unassigned third party blocked from uploading evidence (HTTP 403)");

// Helper can validate evidence upload
$helperUploadAllowed = true;
try {
    $taskRepo->validateEvidenceUpload('req_103', 'usr_2');
} catch (\Exception $e) {
    $helperUploadAllowed = false;
}
assertTest($helperUploadAllowed, "Assigned helper authorized to upload completion evidence");

// 5. Assigned helper submits completion successfully
$req3Submitted = $taskRepo->submitCompletion('req_103', 'usr_2', 'Picked up and delivered groceries.');
assertTest($req3Submitted['status'] === 'PENDING_VERIFICATION', "Assigned helper submits completion successfully (PENDING_VERIFICATION)");

// 6. Duplicate submission blocked with HTTP 409
$duplicateSubmitBlocked = false;
try {
    $taskRepo->submitCompletion('req_103', 'usr_2', 'Submitting again right away');
} catch (\Exception $e) {
    if ($e->getCode() === 409 && str_contains($e->getMessage(), 'already been submitted')) {
        $duplicateSubmitBlocked = true;
    }
}
assertTest($duplicateSubmitBlocked, "Duplicate completion submission blocked with HTTP 409 Conflict");

// 7. Duplicate evidence upload blocked with HTTP 409
$duplicateUploadBlocked = false;
try {
    $taskRepo->validateEvidenceUpload('req_103', 'usr_2');
} catch (\Exception $e) {
    if ($e->getCode() === 409) {
        $duplicateUploadBlocked = true;
    }
}
assertTest($duplicateUploadBlocked, "Uploading evidence after completion already submitted blocked with HTTP 409");

// 8. Helper cannot verify own task
$helperVerifyBlocked = false;
try {
    $taskRepo->verifyCompletion('req_103', 'usr_2', 5, 'Self verification');
} catch (\Exception $e) {
    if ($e->getCode() === 403) {
        $helperVerifyBlocked = true;
    }
}
assertTest($helperVerifyBlocked, "Helper blocked from verifying task completion (HTTP 403)");

// 9. Requester successfully verifies completion and awards points
$req3Verified = $taskRepo->verifyCompletion('req_103', 'usr_1', 5, 'Thank you so much!');
assertTest($req3Verified['status'] === 'COMPLETED', "Requester verifies completion successfully (COMPLETED)");

echo "\n--- 6. Chat Privacy, Recipient Isolation & Security Tests ---\n";
// Create distinct test users: Alice, Bob, and Charlie
$userRepo->create([
    'id' => 'usr_alice',
    'full_name' => 'Alice Allison',
    'email' => 'alice@example.com',
    'phone' => '1112223334',
    'password_hash' => $passHash,
]);
$userRepo->create([
    'id' => 'usr_bob',
    'full_name' => 'Bob Builder',
    'email' => 'bob@example.com',
    'phone' => '2223334445',
    'password_hash' => $passHash,
]);
$userRepo->create([
    'id' => 'usr_charlie',
    'full_name' => 'Charlie Snoop',
    'email' => 'charlie@example.com',
    'phone' => '3334445556',
    'password_hash' => $passHash,
]);

// 1. Alice sends a private message to Bob
$aliceMsg = $chatRepo->sendMessage('usr_alice', 'Confidential: gate code is 9876', null, 'usr_bob', 'client_token_001');
assertTest($aliceMsg['is_user'] === true, "Alice sender message flagged as is_user");
assertTest(!empty($aliceMsg['id']), "Chat message assigned persistent stable ID");
assertTest(!empty($aliceMsg['conversation_id']), "Message assigned to private conversation");

// 2. Bob retrieves the message
$bobMsgs = $chatRepo->getMessages('usr_bob', $aliceMsg['conversation_id']);
assertTest(count($bobMsgs) === 1, "Bob can retrieve Alice's private message");
assertTest($bobMsgs[0]['message_text'] === 'Confidential: gate code is 9876', "Message content intact for recipient");
assertTest($bobMsgs[0]['is_user'] === false, "Message identified as incoming for Bob");

// 3. Charlie attempts to retrieve Alice and Bob's private conversation
$charlieReadBlocked = false;
try {
    $chatRepo->getMessages('usr_charlie', $aliceMsg['conversation_id']);
} catch (\Throwable $t) {
    $charlieReadBlocked = ($t->getCode() === 403);
}
assertTest($charlieReadBlocked, "Charlie cannot read Alice and Bob's conversation (HTTP 403 Forbidden)");

// 4. Charlie attempts to send into Alice and Bob's conversation
$charlieSendBlocked = false;
try {
    $chatRepo->sendMessage('usr_charlie', 'Hacking in', $aliceMsg['conversation_id']);
} catch (\Throwable $t) {
    $charlieSendBlocked = ($t->getCode() === 403);
}
assertTest($charlieSendBlocked, "Charlie cannot inject messages into Alice and Bob's conversation (HTTP 403 Forbidden)");

// 5. General chat request without conversation or recipient rejected
$generalBlocked = false;
try {
    $chatRepo->getMessages('usr_alice');
} catch (\Throwable $t) {
    $generalBlocked = ($t->getCode() === 400);
}
assertTest($generalBlocked, "General chat request without conversation_id or recipient_id rejected (HTTP 400)");

// 6. Cannot send to nonexistent recipient
$nonexistentBlocked = false;
try {
    $chatRepo->sendMessage('usr_alice', 'Ghost message', null, 'non_existent_usr_999');
} catch (\Throwable $t) {
    $nonexistentBlocked = ($t->getCode() === 404);
}
assertTest($nonexistentBlocked, "Cannot send message to nonexistent recipient (HTTP 404)");

// 7. Cannot message oneself
$selfBlocked = false;
try {
    $chatRepo->sendMessage('usr_alice', 'Note to self', null, 'usr_alice');
} catch (\Throwable $t) {
    $selfBlocked = ($t->getCode() === 400);
}
assertTest($selfBlocked, "Cannot send message to self (HTTP 400)");

// 8. Cannot mismatch recipient with conversation participants
$mismatchBlocked = false;
try {
    $chatRepo->sendMessage('usr_alice', 'Redirect attempt', $aliceMsg['conversation_id'], 'usr_charlie');
} catch (\Throwable $t) {
    $mismatchBlocked = ($t->getCode() === 400);
}
assertTest($mismatchBlocked, "Cannot redirect message to arbitrary recipient outside conversation");

// 9. Repeated send with same client_message_id returns existing message idempotently
$idempotentMsg = $chatRepo->sendMessage('usr_alice', 'Confidential: gate code is 9876', $aliceMsg['conversation_id'], null, 'client_token_001');
assertTest($idempotentMsg['id'] === $aliceMsg['id'], "Repeated send with same client_message_id is idempotent (same ID)");

// 10. Rapid double submission (within 2 seconds) prevented
$rapidDup = $chatRepo->sendMessage('usr_alice', 'Confidential: gate code is 9876', $aliceMsg['conversation_id']);
assertTest($rapidDup['id'] === $aliceMsg['id'], "Rapid double submission prevented server-side");

// Verify database message count for this conversation is exactly 1
$msgCount = (int)$pdo->query("SELECT COUNT(*) FROM chat_messages WHERE conversation_id = '{$aliceMsg['conversation_id']}'")->fetchColumn();
assertTest($msgCount === 1, "Exactly one row persisted in database (0 duplicates)");

// 11. Notification isolation: only Bob received a notification, Charlie did not
$bobNotifs = $notifRepo->getNotificationsForUser('usr_bob');
$charlieNotifs = $notifRepo->getNotificationsForUser('usr_charlie');
assertTest(count($bobNotifs) >= 1, "Bob received in-app notification for the private message");
assertTest(count($charlieNotifs) === 0, "Charlie received 0 notifications for Alice and Bob's private message");

echo "\n--- 7. Notifications & Read Status Tests ---\n";
$notifRepo->createNotification('usr_1', 'Task Update', 'Your task was updated.', 'TASK_UPDATE');
$notifs = $notifRepo->getNotificationsForUser('usr_1');
assertTest(count($notifs) >= 1, "User can retrieve their notifications");
assertTest($notifs[0]['is_read'] === false, "New notification starts unread");

$notifRepo->markAsRead($notifs[0]['id'], 'usr_1');
$notifsUpdated = $notifRepo->getNotificationsForUser('usr_1');
assertTest($notifsUpdated[0]['is_read'] === true, "Notification successfully marked as read");

$notifRepo->clearAllForUser('usr_1');
$notifsAfterClear = $notifRepo->getNotificationsForUser('usr_1');
assertTest(count($notifsAfterClear) === 0, "All notifications cleared for user");

echo "\n--- 8. Real Task History Tests ---\n";
$history = $taskRepo->getTaskHistory('usr_1');
assertTest(count($history) >= 1, "Completed task visible in user real task history");
assertTest($history[0]['status'] === 'Completed', "Task history derived from real database status");

echo "\n--- 9. Intelligent AI Helper Matching Engine Tests ---\n";
$matchingEngine = new \NeighborHero\Services\HelperMatchingEngine($pdo);

// Setup distinct test helpers with known profiles for matching verification
// Helper 10 (Dave): Repairs expert, Maplewood (near), available today, 5.0 rating, 0 active tasks
$userRepo->create([
    'id' => 'hlp_dave',
    'full_name' => 'Dave Miller',
    'email' => 'dave@example.com',
    'phone' => '5551112233',
    'password_hash' => $passHash,
    'neighborhood' => 'Maplewood',
    'skills' => ['Repairs'],
]);
$pdo->exec("UPDATE users SET is_verified = 1, rating = 5.0, completed_tasks_count = 12, is_available_today = 1 WHERE id = 'hlp_dave'");

// Helper 11 (Elena): Repairs expert, Riverside (further away), available today, 5.0 rating, 0 active tasks
$userRepo->create([
    'id' => 'hlp_elena',
    'full_name' => 'Elena Rostova',
    'email' => 'elena@example.com',
    'phone' => '5552223344',
    'password_hash' => $passHash,
    'neighborhood' => 'Riverside',
    'skills' => ['Repairs'],
]);
$pdo->exec("UPDATE users SET is_verified = 1, rating = 5.0, completed_tasks_count = 12, is_available_today = 1 WHERE id = 'hlp_elena'");

// Helper 12 (Frank): Repairs expert, Maplewood (near), but UNAVAILABLE today (day off)
$userRepo->create([
    'id' => 'hlp_frank',
    'full_name' => 'Frank Wright',
    'email' => 'frank@example.com',
    'phone' => '5553334455',
    'password_hash' => $passHash,
    'neighborhood' => 'Maplewood',
    'skills' => ['Repairs'],
]);
$pdo->exec("UPDATE users SET is_verified = 1, rating = 5.0, completed_tasks_count = 10, is_available_today = 0 WHERE id = 'hlp_frank'");

// Helper 13 (Grace): Nearby in Maplewood, but ONLY has 'Groceries' skill (lacks Repairs)
$userRepo->create([
    'id' => 'hlp_grace',
    'full_name' => 'Grace Hopper',
    'email' => 'grace@example.com',
    'phone' => '5554445566',
    'password_hash' => $passHash,
    'neighborhood' => 'Maplewood',
    'skills' => ['Groceries'],
]);
$pdo->exec("UPDATE users SET is_verified = 1, rating = 5.0, completed_tasks_count = 15, is_available_today = 1 WHERE id = 'hlp_grace'");

// Helper 14 (Hank): New helper, 0 reviews, 0 completed tasks, Repairs skill in Maplewood
$userRepo->create([
    'id' => 'hlp_hank',
    'full_name' => 'Hank Newbie',
    'email' => 'hank@example.com',
    'phone' => '5555556677',
    'password_hash' => $passHash,
    'neighborhood' => 'Maplewood',
    'skills' => ['Repairs'],
]);
$pdo->exec("UPDATE users SET is_verified = 1, rating = 5.0, completed_tasks_count = 0, is_available_today = 1 WHERE id = 'hlp_hank'");

// Helper 15 (Ivy): Overloaded helper with 3 active tasks currently
$userRepo->create([
    'id' => 'hlp_ivy',
    'full_name' => 'Ivy Busy',
    'email' => 'ivy@example.com',
    'phone' => '5556667788',
    'password_hash' => $passHash,
    'neighborhood' => 'Maplewood',
    'skills' => ['Repairs'],
]);
$pdo->exec("UPDATE users SET is_verified = 1, rating = 5.0, completed_tasks_count = 15, is_available_today = 1 WHERE id = 'hlp_ivy'");
// Assign 3 active in-progress tasks to Ivy
for ($i = 1; $i <= 3; $i++) {
    $pdo->exec("INSERT INTO help_requests (id, requester_id, title, category, description, location, requested_time, status, assigned_helper_id)
                VALUES ('req_busy_{$i}', 'usr_1', 'Busy task {$i}', 'Repairs', 'Work in progress', 'Maplewood', 'Today', 'IN_PROGRESS', 'hlp_ivy')");
}

// 1. Skill Filtering Test: Nearby helper lacking the required skill must be excluded
$repairsContext = [
    'category' => 'Repairs',
    'location' => 'Maplewood',
    'requested_time' => 'Today 2pm',
    'requester_id' => 'usr_1',
];
$repairsMatches = $matchingEngine->matchHelpers($repairsContext, [], 10);
$graceFound = false;
foreach ($repairsMatches as $m) {
    if ($m['id'] === 'hlp_grace') $graceFound = true;
}
assertTest(!$graceFound, "Nearby helper lacking required skill (Grace) is excluded from Repairs recommendations");

// 2. Distance Ranking Test: Equally skilled Dave (Maplewood ~0.4mi) ranks higher than Elena (Riverside ~2.8mi)
$daveRank = null;
$elenaRank = null;
foreach ($repairsMatches as $idx => $m) {
    if ($m['id'] === 'hlp_dave') $daveRank = $idx;
    if ($m['id'] === 'hlp_elena') $elenaRank = $idx;
}
assertTest($daveRank !== null && $elenaRank !== null && $daveRank < $elenaRank, "Closer helper (Dave, Maplewood) ranks higher than distant helper (Elena, Riverside)");

// 3. Availability Test: Unavailable helper (Frank, day off) ranks lower than available helper (Dave)
$frankRank = null;
foreach ($repairsMatches as $idx => $m) {
    if ($m['id'] === 'hlp_frank') $frankRank = $idx;
}
assertTest($frankRank === null || $daveRank < $frankRank, "Available helper (Dave) ranks higher than unavailable helper (Frank, day off)");

// 4. Workload Balancing Test: Overloaded helper with 3 active tasks (Ivy) has lower workload score than free helper (Dave)
$daveMatch = null;
$ivyMatch = null;
foreach ($repairsMatches as $m) {
    if ($m['id'] === 'hlp_dave') $daveMatch = $m;
    if ($m['id'] === 'hlp_ivy') $ivyMatch = $m;
}
assertTest($daveMatch !== null && $ivyMatch !== null, "Both Dave and Ivy evaluated in candidate set");
assertTest($daveMatch['score_breakdown']['workload'] > $ivyMatch['score_breakdown']['workload'], "Free helper (Dave, 0 tasks) receives higher workload score than overloaded helper (Ivy, 3 tasks)");
assertTest($daveMatch['match_score'] > $ivyMatch['match_score'], "Workload penalty appropriately lowers overloaded helper overall rank");

// 5. New Helper Inclusivity Test: New helper with 0 reviews (Hank) receives fair Bayesian baseline and is recommended
$hankMatch = null;
foreach ($repairsMatches as $m) {
    if ($m['id'] === 'hlp_hank') $hankMatch = $m;
}
assertTest($hankMatch !== null, "New helper with no ratings (Hank) is included in recommendations (not excluded)");
assertTest($hankMatch['match_percentage'] >= 70, "New helper receives fair starter compatibility score (>= 70%)");

// 6. Explainable Reasons Test: Matching engine provides human-readable reasons for recommendations
assertTest(!empty($daveMatch['matching_reasons']), "Matching engine generates explainable reasons");
$hasSkillReason = false;
$hasLocReason = false;
foreach ($daveMatch['matching_reasons'] as $r) {
    if (str_contains($r, 'Skills match') || str_contains($r, 'Repairs')) $hasSkillReason = true;
    if (str_contains($r, 'Located') || str_contains($r, 'Maplewood')) $hasLocReason = true;
}
assertTest($hasSkillReason, "Generated reasons include skill match explanation");
assertTest($hasLocReason, "Generated reasons include location proximity explanation");

// 7. Request-Specific Recommendations Test via HelperRepository & Controller
$reqMatch = $requestRepo->create([
    'id' => 'req_match_test',
    'requester_id' => 'usr_1',
    'title' => 'Broken kitchen cabinet door latch',
    'category' => 'Repairs',
    'description' => 'Screws came loose from wood frame.',
    'location' => 'Maplewood',
    'requested_time' => 'Today 3pm',
]);
$reqRecommendations = $helperRepo->getRecommendationsForRequest('req_match_test', [], 5);
assertTest(count($reqRecommendations) >= 1, "HelperRepository::getRecommendationsForRequest returns ranked helpers for request");
assertTest($reqRecommendations[0]['id'] === 'hlp_dave', "Top recommended helper for kitchen repair in Maplewood is Dave Miller");
assertTest($reqRecommendations[0]['match_percentage'] >= 90, "Top recommended helper achieves strong compatibility score (>= 90%)");

// 8. GPS Coordinates-Based Intelligent Matching & Centroid Fallback
$reqWithGps = $requestRepo->create([
    'id' => 'req_gps_match_test',
    'requester_id' => 'usr_1',
    'title' => 'Emergency water leak repair under sink',
    'category' => 'Repairs',
    'description' => 'Need wrench assistance with p-trap leak.',
    'location' => 'Maplewood',
    'requested_time' => 'Today ASAP',
    'latitude' => 37.7752,
    'longitude' => -122.4190,
]);
$gpsRecs = $helperRepo->getRecommendationsForRequest('req_gps_match_test', [], 5);
assertTest(count($gpsRecs) >= 1, "Matching engine provides recommendations for GPS-located request");
assertTest(isset($gpsRecs[0]['distance_miles']), "GPS matching calculates distance_miles using Haversine formula");
assertTest(isset($gpsRecs[0]['distance']), "GPS matching formats human-readable distance (e.g. '0.2 mi away')");

// Test missing coordinates fallback without error
$reqNoGps = $requestRepo->create([
    'id' => 'req_nogps_match_test',
    'requester_id' => 'usr_1',
    'title' => 'Help fixing garden fence gate',
    'category' => 'Repairs',
    'description' => 'Fence latch needs realignment.',
    'location' => 'Oakridge',
    'requested_time' => 'Weekend',
    'latitude' => null,
    'longitude' => null,
]);
$noGpsRecs = $helperRepo->getRecommendationsForRequest('req_nogps_match_test', [], 5);
assertTest(!empty($noGpsRecs), "Matching engine safely falls back to neighborhood centroid when request coordinates are null");

// Verify exact coordinates are not leaked in public helper output
$firstRec = $gpsRecs[0];
assertTest(!isset($firstRec['latitude']) && !isset($firstRec['longitude']), "Public helper recommendation output does not expose exact helper coordinates");


echo "\n==================================================\n";
echo "Total Tests Run: " . ($passed + $failed) . "\n";
echo "Passed: {$passed} | Failed: {$failed}\n";
echo "==================================================\n";

if ($failed > 0) {
    exit(1);
}

