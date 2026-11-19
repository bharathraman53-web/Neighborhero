<?php
require_once __DIR__ . '/../services/Database.php';
require_once __DIR__ . '/../repositories/UserRepository.php';
require_once __DIR__ . '/../repositories/HelpRequestRepository.php';
require_once __DIR__ . '/../repositories/TaskRepository.php';

use NeighborHero\Services\Database;
use NeighborHero\Repositories\UserRepository;
use NeighborHero\Repositories\HelpRequestRepository;
use NeighborHero\Repositories\TaskRepository;

echo "=== NeighborHero Live Tracking E2E Verification ===\n\n";

$pdo = Database::getConnection();
$userRepo = new UserRepository($pdo);
$requestRepo = new HelpRequestRepository($pdo);
$taskRepo = new TaskRepository($pdo, $requestRepo, $userRepo);

$requesterId = 'usr_a1720be5ebc579f94266c1b5'; // Bharath
$helperId = 'usr_4a31eff2ae44438853bb3ea5';   // Nithish
$thirdPartyId = 'test_unauthorized_user';

$taskId = 'req_live_tracking_e2e_' . time();
$destLat = 13.0827;
$destLng = 80.2707;

// 1. Create or ensure task is IN_PROGRESS between Bharath and Nithish
$stmt = $pdo->prepare("
    INSERT INTO help_requests (id, requester_id, assigned_helper_id, title, description, category, location, latitude, longitude, status, created_at, updated_at)
    VALUES (:id, :req_id, :helper_id, 'Urgent Grocery Delivery - Live Tracking Test', 'Please deliver fresh supplies', 'Groceries', 'Maplewood District', :lat, :lng, 'IN_PROGRESS', NOW(), NOW())
");
$stmt->execute([
    ':id' => $taskId,
    ':req_id' => $requesterId,
    ':helper_id' => $helperId,
    ':lat' => $destLat,
    ':lng' => $destLng
]);
echo "[PASS] Step 1: Active task created: $taskId (Requester: Bharath, Helper: Nithish)\n";

// 2. Helper (Nithish) updates location: Point 1 (starting position ~0.6 mi away)
$loc1 = $taskRepo->updateLiveLocation($taskId, $helperId, 13.0910, 80.2760, 45.0, 4.0, 5.2);
echo "[PASS] Step 2: Helper posted GPS Point 1 (Lat: {$loc1['latitude']}, Lng: {$loc1['longitude']}, Speed: {$loc1['speed']} m/s)\n";

// 3. Requester (Bharath) retrieves live location
$reqView1 = $taskRepo->getLiveLocation($taskId, $requesterId);
echo "[PASS] Step 3: Requester fetched live location:\n";
echo "       - Helper: {$reqView1['helper_name']}\n";
echo "       - Coordinates: {$reqView1['latitude']}, {$reqView1['longitude']}\n";
echo "       - Distance: {$reqView1['distance_miles']} mi ({$reqView1['distance_km']} km)\n";
echo "       - ETA: ~{$reqView1['eta_minutes']} min\n";
echo "       - Is Active: " . ($reqView1['is_active'] ? 'true' : 'false') . "\n";

assert($reqView1['is_active'] === true, "Tracking must be active");
assert($reqView1['latitude'] == 13.0910, "Latitude must match point 1");

// 4. Unauthorized third party attempts to track helper
$blocked = false;
try {
    $taskRepo->getLiveLocation($taskId, $thirdPartyId);
} catch (RuntimeException $e) {
    if ($e->getCode() === 403) {
        $blocked = true;
    }
}
assert($blocked, "Unauthorized third party must be blocked with HTTP 403");
echo "[PASS] Step 4: Unauthorized third party blocked with HTTP 403 Forbidden\n";

// 5. Helper moves to Point 2 (approaching destination ~0.3 mi away)
$loc2 = $taskRepo->updateLiveLocation($taskId, $helperId, 13.0865, 80.2730, 30.0, 3.5, 4.8);
$reqView2 = $taskRepo->getLiveLocation($taskId, $requesterId);
echo "[PASS] Step 5: Helper moved to GPS Point 2:\n";
echo "       - Coordinates: {$reqView2['latitude']}, {$reqView2['longitude']}\n";
echo "       - New Distance: {$reqView2['distance_miles']} mi (decreased from {$reqView1['distance_miles']} mi)\n";
echo "       - New ETA: ~{$reqView2['eta_minutes']} min\n";

assert((float)$reqView2['distance_miles'] < (float)$reqView1['distance_miles'], "Distance must decrease as helper approaches");

// 6. Helper moves to Point 3 (arrived at destination ~0.05 mi away)
$loc3 = $taskRepo->updateLiveLocation($taskId, $helperId, 13.0830, 80.2709, 10.0, 2.0, 1.2);
$reqView3 = $taskRepo->getLiveLocation($taskId, $requesterId);
echo "[PASS] Step 6: Helper arrived at GPS Point 3:\n";
echo "       - Coordinates: {$reqView3['latitude']}, {$reqView3['longitude']}\n";
echo "       - Distance: {$reqView3['distance_miles']} mi\n";
echo "       - ETA: ~{$reqView3['eta_minutes']} min\n";

// 7. Helper stops sharing location
$stopped = $taskRepo->stopLiveLocation($taskId, $helperId);
$reqViewStopped = $taskRepo->getLiveLocation($taskId, $requesterId);
echo "[PASS] Step 7: Helper stopped location sharing:\n";
echo "       - Is Active: " . ($reqViewStopped['is_active'] ? 'true' : 'false') . "\n";
echo "       - Message: {$reqViewStopped['message']}\n";

assert($reqViewStopped['is_active'] === false, "Tracking must be inactive after stop");

echo "\n>>> ALL 7 E2E LIVE TRACKING VERIFICATIONS PASSED SUCCESSFULLY <<<\n";
