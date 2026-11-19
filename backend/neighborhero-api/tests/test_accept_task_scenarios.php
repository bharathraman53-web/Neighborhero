<?php
declare(strict_types=1);

require_once __DIR__ . '/../bootstrap.php';

use NeighborHero\Repositories\HelpRequestRepository;
use NeighborHero\Repositories\TaskRepository;
use NeighborHero\Repositories\UserRepository;
use NeighborHero\Services\Database;

$pdo = Database::getConnection();
$userRepo = new UserRepository($pdo);
$requestRepo = new HelpRequestRepository($pdo);
$taskRepo = new TaskRepository($pdo);

echo "=== NeighborHero Accept Task & Multi-Account End-to-End Test Suite ===\n\n";

$passed = 0;
$failed = 0;

function assertCondition(bool $condition, string $testName): void {
    global $passed, $failed;
    if ($condition) {
        echo "  [PASS] {$testName}\n";
        $passed++;
    } else {
        echo "  [FAIL] {$testName}\n";
        $failed++;
    }
}

// 0. Set up test accounts
$requesterId = 'test_req_user_' . time();
$helper1Id = 'test_helper1_' . time();
$helper2Id = 'test_helper2_' . time();

$pdo->prepare("INSERT INTO users (id, email, password_hash, full_name, role, neighborhood) 
               VALUES (:id, :email, :pwd, :name, 'requester', 'Maplewood District')")
    ->execute([
        ':id' => $requesterId,
        ':email' => "requester_{$requesterId}@example.com",
        ':pwd' => password_hash('password123', PASSWORD_BCRYPT),
        ':name' => 'Alice Requester'
    ]);

$pdo->prepare("INSERT INTO users (id, email, password_hash, full_name, role, neighborhood) 
               VALUES (:id, :email, :pwd, :name, 'helper', 'Maplewood District')")
    ->execute([
        ':id' => $helper1Id,
        ':email' => "helper1_{$helper1Id}@example.com",
        ':pwd' => password_hash('password123', PASSWORD_BCRYPT),
        ':name' => 'Bob Helper 1'
    ]);

$pdo->prepare("INSERT INTO users (id, email, password_hash, full_name, role, neighborhood) 
               VALUES (:id, :email, :pwd, :name, 'helper', 'Maplewood District')")
    ->execute([
        ':id' => $helper2Id,
        ':email' => "helper2_{$helper2Id}@example.com",
        ':pwd' => password_hash('password123', PASSWORD_BCRYPT),
        ':name' => 'Charlie Helper 2'
    ]);

echo "--- Scenario 1: Groceries Task End-to-End Workflow ---\n";
$groceriesTaskId = 'task_groceries_' . time();
$createdGroceries = $requestRepo->create([
    'id' => $groceriesTaskId,
    'requester_id' => $requesterId,
    'title' => 'Need groceries delivered ASAP',
    'category' => 'Groceries',
    'description' => 'Fresh vegetables and milk from local market',
    'location' => 'Maplewood District',
    'requested_time' => 'Today 4pm'
]);
assertCondition($createdGroceries['status'] === 'OPEN', "1.1 Groceries task saved in MySQL with status OPEN");
assertCondition($createdGroceries['assigned_helper_id'] === null, "1.2 Groceries task initially has null assigned helper");

// Check visibility in helper available tasks
$availableTasks = $requestRepo->listAll(null, 'OPEN');
$foundInOpen = array_filter($availableTasks, fn($t) => $t['id'] === $groceriesTaskId);
assertCondition(!empty($foundInOpen), "1.3 Groceries task appears in helper available task list");

// Helper 1 accepts task
$acceptedGroceries = $taskRepo->assignHelper($groceriesTaskId, $helper1Id, $helper1Id);
assertCondition($acceptedGroceries['status'] === 'IN_PROGRESS', "1.4 Backend assigns Groceries task to Helper 1 (status IN_PROGRESS)");
assertCondition($acceptedGroceries['assigned_helper_id'] === $helper1Id, "1.5 Assigned helper ID matches Helper 1");

// Requester views task: sees Helper 1 assigned
$requesterView = $requestRepo->findById($groceriesTaskId);
assertCondition($requesterView['assigned_helper_id'] === $helper1Id, "1.6 Requester sees Helper 1 assigned");
assertCondition($requesterView['assigned_helper_name'] === 'Bob Helper 1', "1.7 Requester sees Helper 1 name ('Bob Helper 1')");

// Helper 1 views accepted tasks: sees Groceries task
$helper1Tasks = $requestRepo->findByHelper($helper1Id);
$foundInH1 = array_filter($helper1Tasks, fn($t) => $t['id'] === $groceriesTaskId);
assertCondition(!empty($foundInH1), "1.8 Helper 1 sees accepted task in their accepted task list");

// Other helper (Helper 2) attempts to accept same Groceries task
$helper2Blocked = false;
$helper2Code = 0;
$helper2Msg = '';
try {
    $taskRepo->assignHelper($groceriesTaskId, $helper2Id, $helper2Id);
} catch (\Exception $e) {
    $helper2Blocked = true;
    $helper2Code = $e->getCode();
    $helper2Msg = $e->getMessage();
}
assertCondition($helper2Blocked, "1.9 Helper 2 cannot accept already-accepted Groceries task");
assertCondition($helper2Code === 409, "1.10 Helper 2 receives HTTP 409 Conflict");
assertCondition($helper2Msg === 'This task has already been accepted by another helper.', "1.11 Response states 'This task has already been accepted by another helper.'");

// Confirm Helper 2 did not overwrite Helper 1
$recheckGroceries = $requestRepo->findById($groceriesTaskId);
assertCondition($recheckGroceries['assigned_helper_id'] === $helper1Id, "1.12 Helper 1 remains the assigned helper (Helper 2 did not overwrite)");

// Helper 1 taps Accept again (repeated button tap)
$duplicateBlocked = false;
$duplicateCode = 0;
$duplicateMsg = '';
try {
    $taskRepo->assignHelper($groceriesTaskId, $helper1Id, $helper1Id);
} catch (\Exception $e) {
    $duplicateBlocked = true;
    $duplicateCode = $e->getCode();
    $duplicateMsg = $e->getMessage();
}
assertCondition($duplicateBlocked, "1.13 Repeated acceptance tap by Helper 1 is handled gracefully");
assertCondition($duplicateCode === 409, "1.14 Repeated tap returns HTTP 409 Conflict");
assertCondition($duplicateMsg === 'You have already accepted this task.', "1.15 Accurately distinguishes 'You have already accepted this task.'");

echo "\n--- Scenario 2: Books Task Workflow (Helper 2 Assignment) ---\n";
$booksTaskId = 'task_books_' . time();
$createdBooks = $requestRepo->create([
    'id' => $booksTaskId,
    'requester_id' => $requesterId,
    'title' => 'Need 2nd year organic chemistry textbook',
    'category' => 'Books',
    'description' => 'Borrowing for semester exam revision',
    'location' => 'Maplewood Library',
    'requested_time' => 'Tomorrow 2pm'
]);
assertCondition($createdBooks['status'] === 'OPEN', "2.1 Books task created in MySQL in OPEN status");

// Helper 2 accepts Books task
$acceptedBooks = $taskRepo->assignHelper($booksTaskId, $helper2Id, $helper2Id);
assertCondition($acceptedBooks['status'] === 'IN_PROGRESS', "2.2 Books task assigned to Helper 2 in IN_PROGRESS status");
assertCondition($acceptedBooks['assigned_helper_id'] === $helper2Id, "2.3 Books task assigned_helper_id matches Helper 2");

// Helper 1 attempts to accept Books task
$h1BlockedOnBooks = false;
$h1MsgOnBooks = '';
try {
    $taskRepo->assignHelper($booksTaskId, $helper1Id, $helper1Id);
} catch (\Exception $e) {
    $h1BlockedOnBooks = true;
    $h1MsgOnBooks = $e->getMessage();
}
assertCondition($h1BlockedOnBooks, "2.4 Helper 1 blocked from accepting task assigned to Helper 2");
assertCondition($h1MsgOnBooks === 'This task has already been accepted by another helper.', "2.5 Helper 1 receives 'This task has already been accepted by another helper.'");

echo "\n--- Scenario 3: Repairs Task Workflow ---\n";
$repairsTaskId = 'task_repairs_' . time();
$createdRepairs = $requestRepo->create([
    'id' => $repairsTaskId,
    'requester_id' => $requesterId,
    'title' => 'Fix leaking garden faucet',
    'category' => 'Repairs',
    'description' => 'Outdoor faucet dripping constantly, need washer replacement',
    'location' => 'Oakridge Lane 14',
    'requested_time' => 'Friday 10am'
]);
assertCondition($createdRepairs['status'] === 'OPEN', "3.1 Repairs task created successfully in MySQL");

$acceptedRepairs = $taskRepo->assignHelper($repairsTaskId, $helper1Id, $helper1Id);
assertCondition($acceptedRepairs['assigned_helper_id'] === $helper1Id, "3.2 Repairs task accepted by Helper 1");
assertCondition($acceptedRepairs['status'] === 'IN_PROGRESS', "3.3 Repairs task status transitioned to IN_PROGRESS");

echo "\n--- Scenario 4: Security, Integrity & Edge Cases ---\n";
// Requester cannot accept own task
$ownTask = 'task_own_' . time();
$requestRepo->create([
    'id' => $ownTask,
    'requester_id' => $requesterId,
    'title' => 'Requester self task',
    'category' => 'Other',
    'description' => 'Test requester self-acceptance block',
    'location' => 'Maplewood',
    'requested_time' => 'Today'
]);
$selfBlocked = false;
$selfCode = 0;
try {
    $taskRepo->assignHelper($ownTask, $requesterId, $requesterId);
} catch (\Exception $e) {
    $selfBlocked = true;
    $selfCode = $e->getCode();
}
assertCondition($selfBlocked, "4.1 Requester is blocked from accepting their own task");
assertCondition($selfCode === 403, "4.2 Requester self-acceptance returns HTTP 403 Forbidden");

// Nonexistent task returns 404
$notFoundBlocked = false;
$notFoundCode = 0;
try {
    $taskRepo->assignHelper('nonexistent_task_999999', $helper1Id, $helper1Id);
} catch (\Exception $e) {
    $notFoundBlocked = true;
    $notFoundCode = $e->getCode();
}
assertCondition($notFoundBlocked, "4.3 Acceptance of nonexistent task ID is rejected");
assertCondition($notFoundCode === 404, "4.4 Nonexistent task returns HTTP 404 Not Found");

// Cancelled task returns 400
$cancelledTaskId = 'task_cancelled_' . time();
$requestRepo->create([
    'id' => $cancelledTaskId,
    'requester_id' => $requesterId,
    'title' => 'Cancelled test task',
    'category' => 'Other',
    'description' => 'Task that will be cancelled',
    'location' => 'Maplewood',
    'requested_time' => 'Today',
    'status' => 'CANCELLED'
]);
$cancelBlocked = false;
$cancelCode = 0;
$cancelMsg = '';
try {
    $taskRepo->assignHelper($cancelledTaskId, $helper1Id, $helper1Id);
} catch (\Exception $e) {
    $cancelBlocked = true;
    $cancelCode = $e->getCode();
    $cancelMsg = $e->getMessage();
}
assertCondition($cancelBlocked, "4.5 Acceptance of CANCELLED task is rejected");
assertCondition($cancelCode === 400, "4.6 Cancelled task returns HTTP 400 Bad Request");
assertCondition(strpos($cancelMsg, 'CANCELLED') !== false, "4.7 Cancelled error message states task is unavailable/cancelled");

echo "\n==================================================\n";
echo "Total Tests: " . ($passed + $failed) . "\n";
echo "Passed: {$passed} | Failed: {$failed}\n";
echo "==================================================\n";

if ($failed > 0) {
    exit(1);
}
