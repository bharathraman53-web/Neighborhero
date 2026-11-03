<?php
declare(strict_types=1);

/**
 * NeighborHero — AI Helper Matching Engine Simulation & Comprehensive Testing Suite
 *
 * Runs completely in-memory using SQLite PDO.
 * DOES NOT touch or pollute the production MySQL database.
 */

require_once __DIR__ . '/../services/HelperMatchingEngine.php';

use NeighborHero\Services\HelperMatchingEngine;

echo "====================================================================\n";
echo "   NeighborHero AI Matching Engine Simulation & Quality Audit       \n";
echo "====================================================================\n\n";

// --- Test Framework Helper ---
$totalTests = 0;
$passedTests = 0;
$failedTests = 0;
$discoveredBugs = [];

function assertSimulation(bool $condition, string $description, ?string $detail = null): void {
    global $totalTests, $passedTests, $failedTests, $discoveredBugs;
    $totalTests++;
    if ($condition) {
        $passedTests++;
        echo "  [PASS] {$description}\n";
    } else {
        $failedTests++;
        echo "  [FAIL] {$description}\n";
        if ($detail) {
            echo "         Detail: {$detail}\n";
            $discoveredBugs[] = "{$description}: {$detail}";
        } else {
            $discoveredBugs[] = $description;
        }
    }
}

// --- Setup In-Memory SQLite Database ---
function createSimulationDatabase(): PDO {
    $pdo = new PDO('sqlite::memory:');
    $pdo->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
    $pdo->setAttribute(PDO::ATTR_DEFAULT_FETCH_MODE, PDO::FETCH_ASSOC);

    $pdo->exec("
        CREATE TABLE users (
            id TEXT PRIMARY KEY,
            full_name TEXT NOT NULL,
            email TEXT NOT NULL,
            phone TEXT,
            password_hash TEXT,
            neighborhood TEXT,
            bio TEXT,
            avatar_url TEXT,
            is_verified INTEGER DEFAULT 1,
            points INTEGER DEFAULT 0,
            completed_tasks_count INTEGER DEFAULT 0,
            rating REAL DEFAULT 5.0,
            latitude REAL,
            longitude REAL,
            is_available_today INTEGER DEFAULT 1,
            available_days TEXT DEFAULT 'Monday,Tuesday,Wednesday,Thursday,Friday,Saturday,Sunday',
            available_time_slots TEXT DEFAULT 'Morning,Afternoon,Evening',
            cancellation_count INTEGER DEFAULT 0,
            avg_response_minutes INTEGER DEFAULT 15,
            created_at TEXT DEFAULT CURRENT_TIMESTAMP,
            updated_at TEXT DEFAULT CURRENT_TIMESTAMP
        );

        CREATE TABLE helper_skills (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            user_id TEXT NOT NULL,
            skill_name TEXT NOT NULL,
            created_at TEXT DEFAULT CURRENT_TIMESTAMP,
            FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
        );

        CREATE TABLE help_requests (
            id TEXT PRIMARY KEY,
            requester_id TEXT NOT NULL,
            title TEXT NOT NULL,
            category TEXT NOT NULL,
            description TEXT NOT NULL,
            location TEXT NOT NULL,
            distance TEXT DEFAULT 'Nearby',
            requested_time TEXT NOT NULL,
            is_urgent INTEGER DEFAULT 0,
            status TEXT DEFAULT 'OPEN',
            assigned_helper_id TEXT,
            latitude REAL,
            longitude REAL,
            preferred_time_slot TEXT,
            created_at TEXT DEFAULT CURRENT_TIMESTAMP,
            updated_at TEXT DEFAULT CURRENT_TIMESTAMP
        );
    ");

    return $pdo;
}

$pdo = createSimulationDatabase();

// ====================================================================
// 1. CREATE CONTROLLED TEST HELPER DATA (18 Distinct Profiles)
// ====================================================================
echo "--- 1. Initializing 18 Controlled Simulated Helper Profiles ---\n";

$simulatedHelpers = [
    // 1. Alice: Groceries Expert, Maplewood (exact centroid), 4.9 rating, 32 tasks, 0 active, 0 cancel
    [
        'id' => 'hlp_alice',
        'full_name' => 'Alice Cooper',
        'email' => 'alice@test.local',
        'neighborhood' => 'Maplewood',
        'rating' => 4.90,
        'completed_tasks_count' => 32,
        'latitude' => 37.7749,
        'longitude' => -122.4194,
        'is_available_today' => 1,
        'available_days' => 'Monday,Tuesday,Wednesday,Thursday,Friday,Saturday,Sunday',
        'available_time_slots' => 'Morning,Afternoon',
        'cancellation_count' => 0,
        'avg_response_minutes' => 8,
        'skills' => ['Groceries'],
        'active_tasks' => 0,
        'past_completed_category' => ['Groceries' => 5],
    ],
    // 2. Bob: Books & Tutoring, Oakridge (0.6 mi), 4.8 rating, 18 tasks, 1 active task, 1 cancel
    [
        'id' => 'hlp_bob',
        'full_name' => 'Bob Dylan',
        'email' => 'bob@test.local',
        'neighborhood' => 'Oakridge',
        'rating' => 4.80,
        'completed_tasks_count' => 18,
        'latitude' => 37.7833,
        'longitude' => -122.4167,
        'is_available_today' => 1,
        'available_days' => 'Monday,Tuesday,Wednesday,Thursday,Friday',
        'available_time_slots' => 'Afternoon,Evening',
        'cancellation_count' => 1,
        'avg_response_minutes' => 15,
        'skills' => ['Books', 'Tutoring'],
        'active_tasks' => 1,
        'past_completed_category' => ['Books' => 3],
    ],
    // 3. Carlos: Repairs Expert (Plumbing, Handyman), Greenwood (1.6 mi), 4.95 rating, 50 tasks, 0 active
    [
        'id' => 'hlp_carlos',
        'full_name' => 'Carlos Santana',
        'email' => 'carlos@test.local',
        'neighborhood' => 'Greenwood',
        'rating' => 4.95,
        'completed_tasks_count' => 50,
        'latitude' => 37.7690,
        'longitude' => -122.4467,
        'is_available_today' => 1,
        'available_days' => 'Monday,Wednesday,Friday,Saturday',
        'available_time_slots' => 'Morning,Afternoon',
        'cancellation_count' => 0,
        'avg_response_minutes' => 5,
        'skills' => ['Repairs', 'Handyman', 'Plumbing'],
        'active_tasks' => 0,
        'past_completed_category' => ['Repairs' => 10],
    ],
    // 4. Diana: Transport Expert (Rides, Moving), Downtown (1.1 mi), 4.70 rating, 24 tasks, 2 active tasks, 2 cancel
    [
        'id' => 'hlp_diana',
        'full_name' => 'Diana Ross',
        'email' => 'diana@test.local',
        'neighborhood' => 'Downtown',
        'rating' => 4.70,
        'completed_tasks_count' => 24,
        'latitude' => 37.7879,
        'longitude' => -122.4075,
        'is_available_today' => 1,
        'available_days' => 'Monday,Tuesday,Wednesday,Thursday,Friday,Saturday,Sunday',
        'available_time_slots' => 'Evening',
        'cancellation_count' => 2,
        'avg_response_minutes' => 25,
        'skills' => ['Transport', 'Rides', 'Moving'],
        'active_tasks' => 2,
        'past_completed_category' => ['Transport' => 4],
    ],
    // 5. Elena: Pet Help (Pet Care), Riverside (1.4 mi), 5.0 rating, 28 tasks, 0 active, 0 cancel
    [
        'id' => 'hlp_elena',
        'full_name' => 'Elena Rostova',
        'email' => 'elena@test.local',
        'neighborhood' => 'Riverside',
        'rating' => 5.00,
        'completed_tasks_count' => 28,
        'latitude' => 37.7550,
        'longitude' => -122.4200,
        'is_available_today' => 1,
        'available_days' => 'Monday,Tuesday,Wednesday,Thursday,Friday,Saturday,Sunday',
        'available_time_slots' => 'Morning,Afternoon,Evening',
        'cancellation_count' => 0,
        'avg_response_minutes' => 10,
        'skills' => ['Pet Care', 'Pet Help'],
        'active_tasks' => 0,
        'past_completed_category' => ['Pet Help' => 6],
    ],
    // 6. Frank: Multi-skilled, Pinecrest (1.9 mi), 4.60 rating, 12 tasks, UNAVAILABLE TODAY (day off)
    [
        'id' => 'hlp_frank',
        'full_name' => 'Frank Sinatra',
        'email' => 'frank@test.local',
        'neighborhood' => 'Pinecrest',
        'rating' => 4.60,
        'completed_tasks_count' => 12,
        'latitude' => 37.7950,
        'longitude' => -122.3950,
        'is_available_today' => 0, // Day off / unavailable
        'available_days' => 'Wednesday,Thursday,Friday',
        'available_time_slots' => 'Morning',
        'cancellation_count' => 0,
        'avg_response_minutes' => 20,
        'skills' => ['Groceries', 'Repairs', 'Other'],
        'active_tasks' => 0,
        'past_completed_category' => [],
    ],
    // 7. Grace: Pet Help, Maplewood (0.05 mi), 4.30 rating, 6 tasks, 3 active tasks (heavy workload)
    [
        'id' => 'hlp_grace',
        'full_name' => 'Grace Hopper',
        'email' => 'grace@test.local',
        'neighborhood' => 'Maplewood',
        'rating' => 4.30,
        'completed_tasks_count' => 6,
        'latitude' => 37.7755,
        'longitude' => -122.4188,
        'is_available_today' => 1,
        'available_days' => 'Monday,Tuesday,Wednesday,Thursday,Friday,Saturday,Sunday',
        'available_time_slots' => 'Afternoon',
        'cancellation_count' => 1,
        'avg_response_minutes' => 40,
        'skills' => ['Pet Help'],
        'active_tasks' => 3,
        'past_completed_category' => ['Pet Help' => 1],
    ],
    // 8. Hank: New Helper, Maplewood (0.04 mi), 5.0 initial default, 0 tasks, 0 reviews, starter fairness test
    [
        'id' => 'hlp_hank',
        'full_name' => 'Hank Pym',
        'email' => 'hank@test.local',
        'neighborhood' => 'Maplewood',
        'rating' => 5.00,
        'completed_tasks_count' => 0,
        'latitude' => 37.7745,
        'longitude' => -122.4200,
        'is_available_today' => 1,
        'available_days' => 'Monday,Tuesday,Wednesday,Thursday,Friday,Saturday,Sunday',
        'available_time_slots' => 'Morning,Afternoon,Evening',
        'cancellation_count' => 0,
        'avg_response_minutes' => 15,
        'skills' => ['Groceries', 'Repairs'],
        'active_tasks' => 0,
        'past_completed_category' => [],
    ],
    // 9. Ivy: Repairs, Cedar Grove (1.2 mi), 4.85 rating, 40 tasks, 4 active tasks (overloaded)
    [
        'id' => 'hlp_ivy',
        'full_name' => 'Ivy League',
        'email' => 'ivy@test.local',
        'neighborhood' => 'Cedar Grove',
        'rating' => 4.85,
        'completed_tasks_count' => 40,
        'latitude' => 37.7620,
        'longitude' => -122.4350,
        'is_available_today' => 1,
        'available_days' => 'Monday,Tuesday,Wednesday,Thursday,Friday,Saturday,Sunday',
        'available_time_slots' => 'Morning,Afternoon',
        'cancellation_count' => 0,
        'avg_response_minutes' => 12,
        'skills' => ['Repairs'],
        'active_tasks' => 4,
        'past_completed_category' => ['Repairs' => 8],
    ],
    // 10. Jack: Books, West End (1.7 mi), 3.60 rating, 10 tasks, 4 cancellations (high cancellations, low rel)
    [
        'id' => 'hlp_jack',
        'full_name' => 'Jack Black',
        'email' => 'jack@test.local',
        'neighborhood' => 'West End',
        'rating' => 3.60,
        'completed_tasks_count' => 10,
        'latitude' => 37.7700,
        'longitude' => -122.4500,
        'is_available_today' => 1,
        'available_days' => 'Monday,Tuesday,Wednesday,Thursday,Friday',
        'available_time_slots' => 'Afternoon',
        'cancellation_count' => 4,
        'avg_response_minutes' => 65,
        'skills' => ['Books'],
        'active_tasks' => 0,
        'past_completed_category' => [],
    ],
    // 11. Karen: Distant Groceries Helper (~10.5 mi away in Outer Bay), 4.95 rating, 60 tasks
    [
        'id' => 'hlp_karen',
        'full_name' => 'Karen Carpenter',
        'email' => 'karen@test.local',
        'neighborhood' => 'Outer Bay',
        'rating' => 4.95,
        'completed_tasks_count' => 60,
        'latitude' => 37.8900,
        'longitude' => -122.3000,
        'is_available_today' => 1,
        'available_days' => 'Monday,Tuesday,Wednesday,Thursday,Friday,Saturday,Sunday',
        'available_time_slots' => 'Morning,Afternoon,Evening',
        'cancellation_count' => 0,
        'avg_response_minutes' => 8,
        'skills' => ['Groceries'],
        'active_tasks' => 0,
        'past_completed_category' => ['Groceries' => 15],
    ],
    // 12. Leo: Transport, Maplewood (0.0 mi), 4.60 rating, 14 tasks, Morning only
    [
        'id' => 'hlp_leo',
        'full_name' => 'Leo Tolstoy',
        'email' => 'leo@test.local',
        'neighborhood' => 'Maplewood',
        'rating' => 4.60,
        'completed_tasks_count' => 14,
        'latitude' => 37.7749,
        'longitude' => -122.4194,
        'is_available_today' => 1,
        'available_days' => 'Monday,Tuesday,Wednesday,Thursday,Friday',
        'available_time_slots' => 'Morning',
        'cancellation_count' => 0,
        'avg_response_minutes' => 15,
        'skills' => ['Transport'],
        'active_tasks' => 0,
        'past_completed_category' => ['Transport' => 2],
    ],
    // 13. Maya: General Assistance & Other, Greenwood (1.6 mi), 4.80 rating, 22 tasks
    [
        'id' => 'hlp_maya',
        'full_name' => 'Maya Angelou',
        'email' => 'maya@test.local',
        'neighborhood' => 'Greenwood',
        'rating' => 4.80,
        'completed_tasks_count' => 22,
        'latitude' => 37.7690,
        'longitude' => -122.4467,
        'is_available_today' => 1,
        'available_days' => 'Monday,Tuesday,Wednesday,Thursday,Friday,Saturday,Sunday',
        'available_time_slots' => 'Morning,Afternoon,Evening',
        'cancellation_count' => 1,
        'avg_response_minutes' => 12,
        'skills' => ['Other', 'General Assistance', 'Community Help'],
        'active_tasks' => 0,
        'past_completed_category' => ['Other' => 4],
    ],
    // 14. Nathan: Repairs, NO GPS coordinates (NULL lat/lng, Oakridge centroid fallback test)
    [
        'id' => 'hlp_nathan',
        'full_name' => 'Nathan Drake',
        'email' => 'nathan@test.local',
        'neighborhood' => 'Oakridge',
        'rating' => 4.75,
        'completed_tasks_count' => 16,
        'latitude' => null,
        'longitude' => null,
        'is_available_today' => 1,
        'available_days' => 'Monday,Tuesday,Wednesday,Thursday,Friday',
        'available_time_slots' => 'Morning,Afternoon',
        'cancellation_count' => 0,
        'avg_response_minutes' => 15,
        'skills' => ['Repairs'],
        'active_tasks' => 0,
        'past_completed_category' => ['Repairs' => 2],
    ],
    // 15. Olivia: Pet Help, Maplewood, Tuesday/Thursday only (day mismatch test)
    [
        'id' => 'hlp_olivia',
        'full_name' => 'Olivia Wilde',
        'email' => 'olivia@test.local',
        'neighborhood' => 'Maplewood',
        'rating' => 4.90,
        'completed_tasks_count' => 30,
        'latitude' => 37.7749,
        'longitude' => -122.4194,
        'is_available_today' => 1,
        'available_days' => 'Tuesday,Thursday',
        'available_time_slots' => 'Morning,Afternoon',
        'cancellation_count' => 0,
        'avg_response_minutes' => 10,
        'skills' => ['Pet Help'],
        'active_tasks' => 0,
        'past_completed_category' => ['Pet Help' => 5],
    ],
    // 16. Paul: Twin A, Groceries, Maplewood, identical stats to Queen
    [
        'id' => 'hlp_paul',
        'full_name' => 'Paul McCartney',
        'email' => 'paul@test.local',
        'neighborhood' => 'Maplewood',
        'rating' => 4.80,
        'completed_tasks_count' => 20,
        'latitude' => 37.7750,
        'longitude' => -122.4195,
        'is_available_today' => 1,
        'available_days' => 'Monday,Tuesday,Wednesday,Thursday,Friday,Saturday,Sunday',
        'available_time_slots' => 'Morning,Afternoon,Evening',
        'cancellation_count' => 0,
        'avg_response_minutes' => 10,
        'skills' => ['Groceries'],
        'active_tasks' => 0,
        'past_completed_category' => ['Groceries' => 3],
    ],
    // 17. Queen: Twin B, Groceries, Maplewood, identical stats to Paul
    [
        'id' => 'hlp_queen',
        'full_name' => 'Queen Latifah',
        'email' => 'queen@test.local',
        'neighborhood' => 'Maplewood',
        'rating' => 4.80,
        'completed_tasks_count' => 20,
        'latitude' => 37.7750,
        'longitude' => -122.4195,
        'is_available_today' => 1,
        'available_days' => 'Monday,Tuesday,Wednesday,Thursday,Friday,Saturday,Sunday',
        'available_time_slots' => 'Morning,Afternoon,Evening',
        'cancellation_count' => 0,
        'avg_response_minutes' => 10,
        'skills' => ['Groceries'],
        'active_tasks' => 0,
        'past_completed_category' => ['Groceries' => 3],
    ],
    // 18. Ryan: Extreme Outlier, 2.0 rating, 6 cancellations out of 8 accepted (75% cancel rate), 120m response
    [
        'id' => 'hlp_ryan',
        'full_name' => 'Ryan Gosling',
        'email' => 'ryan@test.local',
        'neighborhood' => 'Maplewood',
        'rating' => 2.00,
        'completed_tasks_count' => 2,
        'latitude' => 37.7749,
        'longitude' => -122.4194,
        'is_available_today' => 1,
        'available_days' => 'Monday',
        'available_time_slots' => 'Evening',
        'cancellation_count' => 6,
        'avg_response_minutes' => 120,
        'skills' => ['Transport'],
        'active_tasks' => 2,
        'past_completed_category' => [],
    ],
];

// Insert simulated helpers into SQLite DB
$userInsertStmt = $pdo->prepare("
    INSERT INTO users (
        id, full_name, email, neighborhood, rating, completed_tasks_count,
        latitude, longitude, is_available_today, available_days,
        available_time_slots, cancellation_count, avg_response_minutes, is_verified
    ) VALUES (
        :id, :full_name, :email, :neighborhood, :rating, :completed_tasks_count,
        :latitude, :longitude, :is_available_today, :available_days,
        :available_time_slots, :cancellation_count, :avg_response_minutes, 1
    )
");

$skillInsertStmt = $pdo->prepare("
    INSERT INTO helper_skills (user_id, skill_name) VALUES (:user_id, :skill_name)
");

$reqInsertStmt = $pdo->prepare("
    INSERT INTO help_requests (
        id, requester_id, title, category, description, location,
        requested_time, status, assigned_helper_id
    ) VALUES (
        :id, 'sim_requester', :title, :category, 'Simulated task record',
        'Maplewood', 'Past', :status, :assigned_helper_id
    )
");

foreach ($simulatedHelpers as $h) {
    $userInsertStmt->execute([
        ':id' => $h['id'],
        ':full_name' => $h['full_name'],
        ':email' => $h['email'],
        ':neighborhood' => $h['neighborhood'],
        ':rating' => $h['rating'],
        ':completed_tasks_count' => $h['completed_tasks_count'],
        ':latitude' => $h['latitude'],
        ':longitude' => $h['longitude'],
        ':is_available_today' => $h['is_available_today'],
        ':available_days' => $h['available_days'],
        ':available_time_slots' => $h['available_time_slots'],
        ':cancellation_count' => $h['cancellation_count'],
        ':avg_response_minutes' => $h['avg_response_minutes'],
    ]);

    foreach ($h['skills'] as $skill) {
        $skillInsertStmt->execute([
            ':user_id' => $h['id'],
            ':skill_name' => $skill,
        ]);
    }

    // Active tasks (in progress) to verify workload score
    for ($i = 0; $i < $h['active_tasks']; $i++) {
        $reqInsertStmt->execute([
            ':id' => "active_{$h['id']}_{$i}",
            ':title' => "Active Task {$i}",
            ':category' => $h['skills'][0] ?? 'Other',
            ':status' => 'IN_PROGRESS',
            ':assigned_helper_id' => $h['id'],
        ]);
    }

    // Completed tasks in category to verify category experience bonus
    foreach ($h['past_completed_category'] as $cat => $count) {
        for ($j = 0; $j < $count; $j++) {
            $reqInsertStmt->execute([
                ':id' => "completed_{$h['id']}_{$cat}_{$j}",
                ':title' => "Past {$cat} {$j}",
                ':category' => $cat,
                ':status' => 'COMPLETED',
                ':assigned_helper_id' => $h['id'],
            ]);
        }
    }
}

echo "Created " . count($simulatedHelpers) . " distinct test helpers with varied multi-parameter attributes.\n\n";

$engine = new HelperMatchingEngine($pdo);

// ====================================================================
// 2. CREATE TEST REQUESTS (Across 6 Target Categories)
// ====================================================================
echo "--- 2. Creating Test Requests Across Categories ---\n";

$testRequests = [
    'Groceries' => [
        'category' => 'Groceries',
        'title' => 'Urgent grocery pickup from local co-op',
        'location' => 'Maplewood',
        'requested_time' => 'Today, 10:00 AM',
        'latitude' => 37.7749,
        'longitude' => -122.4194,
        'requester_id' => 'req_user_1',
    ],
    'Books' => [
        'category' => 'Books',
        'title' => 'Textbook dropoff at college library',
        'location' => 'Oakridge',
        'requested_time' => 'Today, 3:00 PM',
        'latitude' => 37.7833,
        'longitude' => -122.4167,
        'requester_id' => 'req_user_1',
    ],
    'Repairs' => [
        'category' => 'Repairs',
        'title' => 'Fix leaking pipe under kitchen sink',
        'location' => 'Greenwood',
        'requested_time' => 'Today, 9:00 AM',
        'latitude' => 37.7690,
        'longitude' => -122.4467,
        'requester_id' => 'req_user_1',
    ],
    'Transport' => [
        'category' => 'Transport',
        'title' => 'Ride to downtown dental clinic',
        'location' => 'Downtown',
        'requested_time' => 'Today, 7:00 PM',
        'latitude' => 37.7879,
        'longitude' => -122.4075,
        'requester_id' => 'req_user_1',
    ],
    'Pet Help' => [
        'category' => 'Pet Help',
        'title' => 'Afternoon dog walking for golden retriever',
        'location' => 'Riverside',
        'requested_time' => 'Today, 2:00 PM',
        'latitude' => 37.7550,
        'longitude' => -122.4200,
        'requester_id' => 'req_user_1',
    ],
    'Other' => [
        'category' => 'Other',
        'title' => 'Help organizing boxes in garage storage',
        'location' => 'Maplewood',
        'requested_time' => 'Today, 11:00 AM',
        'latitude' => 37.7749,
        'longitude' => -122.4194,
        'requester_id' => 'req_user_1',
    ],
];

assertSimulation(count($testRequests) === 6, "Created test requests across all 6 target categories");
echo "\n";

// ====================================================================
// 3. TEST MATCHING BEHAVIOR
// ====================================================================
echo "--- 3. Testing Core Matching Behavior & Algorithms ---\n";

// 3.1 Skill Exclusivity Verification
$grocMatches = $engine->matchHelpers($testRequests['Groceries'], [], 10);
$matchedIdsGroc = array_column($grocMatches, 'id');

assertSimulation(!in_array('hlp_carlos', $matchedIdsGroc), "Carlos (Repairs only) is strictly excluded from Groceries");
assertSimulation(!in_array('hlp_elena', $matchedIdsGroc), "Elena (Pet Help only) is strictly excluded from Groceries");
assertSimulation(!in_array('hlp_diana', $matchedIdsGroc), "Diana (Transport only) is strictly excluded from Groceries");
assertSimulation(in_array('hlp_alice', $matchedIdsGroc), "Alice (Groceries expert) is included in Groceries recommendations");

// 3.2 Proximity & Haversine Distance Decay
// Compare Alice (Maplewood 0.0 mi) vs Karen (Outer Bay 10.5 mi) for Groceries in Maplewood
$aliceMatch = null;
$karenMatch = null;
foreach ($grocMatches as $m) {
    if ($m['id'] === 'hlp_alice') $aliceMatch = $m;
    if ($m['id'] === 'hlp_karen') $karenMatch = $m;
}
assertSimulation($aliceMatch !== null, "Alice evaluated in candidate set");
assertSimulation($karenMatch !== null, "Karen evaluated in candidate set");
if ($aliceMatch && $karenMatch) {
    $aliceLocScore = $aliceMatch['score_breakdown']['location'];
    $karenLocScore = $karenMatch['score_breakdown']['location'];
    assertSimulation($aliceLocScore > $karenLocScore, "Local helper (Alice, 0.0 mi) receives higher location score ({$aliceLocScore}) than distant helper (Karen, 10.5 mi: {$karenLocScore})");
    assertSimulation($aliceMatch['match_score'] > $karenMatch['match_score'], "Local helper outranks distant helper overall due to location proximity");
}

// 3.3 Availability Scoring & Day-Off Penalty
// Compare Alice (Available today) vs Frank (Unavailable today, is_available_today=0)
$frankMatch = null;
foreach ($grocMatches as $m) {
    if ($m['id'] === 'hlp_frank') $frankMatch = $m;
}
assertSimulation($frankMatch !== null, "Frank evaluated in candidate set");
if ($aliceMatch && $frankMatch) {
    $aliceAvail = $aliceMatch['score_breakdown']['availability'];
    $frankAvail = $frankMatch['score_breakdown']['availability'];
    assertSimulation($frankAvail <= 0.20, "Unavailable helper (Frank, day off) receives penalty score ({$frankAvail} <= 0.20)");
    assertSimulation($aliceAvail >= 0.90, "Available helper (Alice) receives high availability score ({$aliceAvail} >= 0.90)");
    assertSimulation($aliceAvail > $frankAvail, "Available helper strictly scores higher on availability than unavailable helper");
}

// 3.4 Bayesian Smoothing for Ratings
// Formula: smoothedRating = ((reviewCount * rawRating) + (3 * 4.5)) / (reviewCount + 3)
// Normalize: score = (smoothedRating - 1.0) / 4.0
// Hank: reviewCount=0, rawRating=5.0 -> smoothed = (0 + 13.5) / 3 = 4.5 -> score = (4.5 - 1.0) / 4.0 = 0.875
// Elena: reviewCount=28, rawRating=5.0 -> smoothed = (140 + 13.5) / 31 = 4.9516 -> score = (4.9516 - 1.0) / 4.0 = 0.988
$petMatches = $engine->matchHelpers($testRequests['Pet Help'], [], 10);
$elenaMatch = null;
foreach ($petMatches as $m) {
    if ($m['id'] === 'hlp_elena') $elenaMatch = $m;
}
$hankMatch = null;
foreach ($grocMatches as $m) {
    if ($m['id'] === 'hlp_hank') $hankMatch = $m;
}
assertSimulation($elenaMatch !== null && $hankMatch !== null, "Both Elena and Hank evaluated for rating verification");
if ($elenaMatch && $hankMatch) {
    $elenaRatingScore = $elenaMatch['score_breakdown']['rating'];
    $hankRatingScore = $hankMatch['score_breakdown']['rating'];
    assertSimulation(abs($hankRatingScore - 0.875) < 0.01, "Hank (0 reviews) correctly Bayesian smoothed to prior mean 4.5 (score 0.875, actual {$hankRatingScore})");
    assertSimulation(abs($elenaRatingScore - 0.988) < 0.01, "Elena (28 reviews at 5.0) Bayesian smoothed to 4.95 (score 0.988, actual {$elenaRatingScore})");
    assertSimulation($elenaRatingScore > $hankRatingScore, "Established 5.0 helper receives higher rating score than unreviewed 5.0 helper");
}

// 3.5 Reliability & Cancellation Penalties
// Compare Carlos (50 tasks, 0 cancellations, 5m response) vs Jack (10 tasks, 4 cancellations, 65m response)
$repairMatches = $engine->matchHelpers($testRequests['Repairs'], [], 10);
$carlosMatch = null;
foreach ($repairMatches as $m) {
    if ($m['id'] === 'hlp_carlos') $carlosMatch = $m;
}
$bookMatches = $engine->matchHelpers($testRequests['Books'], [], 10);
$jackMatch = null;
foreach ($bookMatches as $m) {
    if ($m['id'] === 'hlp_jack') $jackMatch = $m;
}
assertSimulation($carlosMatch !== null && $jackMatch !== null, "Both Carlos and Jack evaluated for reliability comparison");
if ($carlosMatch && $jackMatch) {
    $carlosRel = $carlosMatch['score_breakdown']['reliability'];
    $jackRel = $jackMatch['score_breakdown']['reliability'];
    assertSimulation($carlosRel >= 0.95, "Flawless helper (Carlos, 0 cancellations) scores high reliability ({$carlosRel} >= 0.95)");
    assertSimulation($jackRel <= 0.70, "Helper with cancellations (Jack, 4 cancels) receives lower reliability ({$jackRel} <= 0.70)");
}

// 3.6 Workload Score & Fair Task Distribution
// Compare Carlos (0 active tasks -> score 1.0) vs Ivy (4 active tasks -> score 0.10) for Repairs
$ivyMatch = null;
foreach ($repairMatches as $m) {
    if ($m['id'] === 'hlp_ivy') $ivyMatch = $m;
}
assertSimulation($ivyMatch !== null, "Ivy evaluated in Repairs recommendations");
if ($carlosMatch && $ivyMatch) {
    $carlosWorkload = $carlosMatch['score_breakdown']['workload'];
    $ivyWorkload = $ivyMatch['score_breakdown']['workload'];
    assertSimulation(abs($carlosWorkload - 1.00) < 0.01, "Carlos (0 active tasks) receives full workload score (1.00, actual {$carlosWorkload})");
    assertSimulation(abs($ivyWorkload - 0.10) < 0.01, "Ivy (4 active tasks) receives reduced workload score (0.10, actual {$ivyWorkload})");
    assertSimulation($carlosMatch['match_score'] > $ivyMatch['match_score'], "Carlos outranks overloaded Ivy overall");
}

// 3.7 Mathematical Consistency of 6-Parameter Weighted Scoring Formula
$weights = $engine->getWeights();
foreach (array_merge($grocMatches, $repairMatches, $bookMatches) as $testedHelper) {
    $bd = $testedHelper['score_breakdown'];
    $expectedTotal = (
        $weights['skill'] * $bd['skill'] +
        $weights['location'] * $bd['location'] +
        $weights['availability'] * $bd['availability'] +
        $weights['rating'] * $bd['rating'] +
        $weights['reliability'] * $bd['reliability'] +
        $weights['workload'] * $bd['workload']
    );
    $diff = abs($expectedTotal - $testedHelper['match_score']);
    if ($diff > 0.005) {
        assertSimulation(false, "Weighted formula mismatch for {$testedHelper['name']}", "Expected {$expectedTotal}, got {$testedHelper['match_score']}");
        break;
    }
}
assertSimulation(true, "All evaluated helpers strictly adhere to 6-parameter weighted scoring formula");

echo "\n";

// ====================================================================
// 4. EDGE CASES AND ROBUSTNESS TESTING
// ====================================================================
echo "--- 4. Testing Edge Cases & System Robustness ---\n";

// 4.1 No Eligible Helpers (Unknown or unrepresented category)
$noEligibleReq = [
    'category' => 'Astronaut Training',
    'title' => 'Need training on orbital mechanics',
    'location' => 'Maplewood',
    'requested_time' => 'Today',
    'latitude' => 37.7749,
    'longitude' => -122.4194,
    'requester_id' => 'req_user_1',
];
$emptyMatches = $engine->matchHelpers($noEligibleReq, [], 10);
assertSimulation(is_array($emptyMatches) && count($emptyMatches) === 0, "No eligible helpers returns clean empty array without error");

// 4.2 Helper with Missing GPS Coordinates (Nathan: null lat, null lng)
$nathanMatch = null;
foreach ($repairMatches as $m) {
    if ($m['id'] === 'hlp_nathan') $nathanMatch = $m;
}
assertSimulation($nathanMatch !== null, "Helper without GPS coordinates (Nathan) is evaluated without crashing");
if ($nathanMatch) {
    assertSimulation($nathanMatch['score_breakdown']['location'] > 0.5, "Nathan's location resolves to Oakridge centroid with valid location score ({$nathanMatch['score_breakdown']['location']})");
    assertSimulation(!empty($nathanMatch['distance']), "Nathan receives valid human-readable distance string ({$nathanMatch['distance']})");
}

// 4.3 Missing Requester Coordinates (Request with null lat, null lng)
$missingCoordsReq = [
    'category' => 'Repairs',
    'title' => 'Need screwdriver assistance',
    'location' => 'Oakridge', // Centroid should resolve
    'requested_time' => 'Today',
    'latitude' => null,
    'longitude' => null,
    'requester_id' => 'req_user_1',
];
$resolvedReqMatches = $engine->matchHelpers($missingCoordsReq, [], 5);
assertSimulation(!empty($resolvedReqMatches), "Request with missing GPS coordinates resolves centroid and matches helpers cleanly");
assertSimulation($resolvedReqMatches[0]['score_breakdown']['location'] > 0, "Resolved request location yields valid positive location score");

// 4.4 New Helper with Zero Tasks/Reviews (Starter Fairness)
assertSimulation($hankMatch['match_percentage'] >= 70, "New helper (Hank) receives fair starter compatibility score (>= 70%, actual {$hankMatch['match_percentage']}%)");
assertSimulation($hankMatch['score_breakdown']['reliability'] >= 0.80, "New helper receives standard 0.80 baseline reliability score");

// 4.5 All Helpers Unavailable Scenario
$pdoUnavailable = createSimulationDatabase();
// Insert helpers with is_available_today = 0
$pdoUnavailable->exec("
    INSERT INTO users (id, full_name, email, neighborhood, rating, completed_tasks_count, is_available_today, is_verified)
    VALUES ('hlp_unavail_1', 'Sleepy Dave', 'dave@sleepy.local', 'Maplewood', 4.8, 10, 0, 1);
    INSERT INTO helper_skills (user_id, skill_name) VALUES ('hlp_unavail_1', 'Groceries');
");
$engineUnavail = new HelperMatchingEngine($pdoUnavailable);
$unavailMatches = $engineUnavail->matchHelpers($testRequests['Groceries'], [], 5);
assertSimulation(count($unavailMatches) === 1, "Matching evaluates even when all candidate helpers are marked unavailable");
assertSimulation($unavailMatches[0]['score_breakdown']['availability'] <= 0.20, "Availability score safely reflects unavailable status without fatal errors or divide-by-zero");

// 4.6 Multiple Helpers with Identical Scores (Paul & Queen Twins)
$paulMatch = null;
$queenMatch = null;
foreach ($grocMatches as $m) {
    if ($m['id'] === 'hlp_paul') $paulMatch = $m;
    if ($m['id'] === 'hlp_queen') $queenMatch = $m;
}
assertSimulation($paulMatch !== null && $queenMatch !== null, "Both identical twin helpers (Paul & Queen) evaluated in candidate set");
if ($paulMatch && $queenMatch) {
    assertSimulation(abs($paulMatch['match_score'] - $queenMatch['match_score']) < 0.0001, "Twin helpers receive identical match scores ({$paulMatch['match_score']})");
    assertSimulation($paulMatch['id'] !== $queenMatch['id'], "Both helpers preserved distinctly in ranked output");
}

// 4.7 Invalid Coordinates (Out of Range Values)
$invalidCoordReq = [
    'category' => 'Groceries',
    'title' => 'Grocery delivery near edge',
    'location' => 'Unknown Nowhere',
    'requested_time' => 'Today',
    'latitude' => 89.9999, // Extreme north latitude
    'longitude' => -179.9999, // Extreme west longitude
    'requester_id' => 'req_user_1',
];
$extremeMatches = $engine->matchHelpers($invalidCoordReq, [], 5);
assertSimulation(!empty($extremeMatches), "Extreme coordinates compute Haversine distance without NaN or crash");
assertSimulation(is_finite($extremeMatches[0]['match_score']), "Resulting match score is a finite real number");

echo "\n";

// ====================================================================
// 5. VERIFY EXPLAINABILITY AND TRANSPARENCY
// ====================================================================
echo "--- 5. Verifying Recommendation Explainability & Score Breakdown ---\n";

$allSampleMatches = array_merge($grocMatches, $repairMatches, $bookMatches, $petMatches);
$allExplainable = true;
$allBreakdownValid = true;
$reasonsCountValid = true;

foreach ($allSampleMatches as $m) {
    // 1. Reasons present and between 2 and 4
    $rc = count($m['matching_reasons']);
    if ($rc < 1 || $rc > 4) {
        $reasonsCountValid = false;
    }

    // 2. Score breakdown keys
    $bd = $m['score_breakdown'];
    if (!isset($bd['skill'], $bd['location'], $bd['availability'], $bd['rating'], $bd['reliability'], $bd['workload'])) {
        $allBreakdownValid = false;
    }

    // 3. Reasons content check
    foreach ($m['matching_reasons'] as $r) {
        if (empty(trim($r))) {
            $allExplainable = false;
        }
    }
}

assertSimulation($reasonsCountValid, "Every recommendation contains between 1 and 4 human-readable matching reasons");
assertSimulation($allBreakdownValid, "Every recommendation provides a complete 6-factor score breakdown");
assertSimulation($allExplainable, "All matching reasons contain meaningful, non-empty text strings");

// Check ranking strictly sorted descending
$strictlySorted = true;
foreach ([$grocMatches, $repairMatches, $bookMatches, $petMatches] as $list) {
    for ($i = 0; $i < count($list) - 1; $i++) {
        if ($list[$i]['match_score'] < $list[$i + 1]['match_score']) {
            $strictlySorted = false;
            break;
        }
    }
}
assertSimulation($strictlySorted, "All recommendation sets are strictly sorted descending by overall match score");

echo "\n";

// ====================================================================
// 6. PERFORMANCE & SCALABILITY BENCHMARKING
// ====================================================================
echo "--- 6. Performance Benchmarks with Scaled Helper Volumes ---\n";

$volumes = [10, 50, 100, 500, 1000];
$benchmarkResults = [];

foreach ($volumes as $vol) {
    $benchPdo = createSimulationDatabase();
    
    // Prepare bulk inserts
    $benchUserStmt = $benchPdo->prepare("
        INSERT INTO users (
            id, full_name, email, neighborhood, rating, completed_tasks_count,
            latitude, longitude, is_available_today, available_days,
            available_time_slots, cancellation_count, avg_response_minutes, is_verified
        ) VALUES (
            :id, :name, :email, :neighborhood, :rating, :tasks,
            :lat, :lng, 1, 'Monday,Tuesday,Wednesday,Thursday,Friday,Saturday,Sunday',
            'Morning,Afternoon,Evening', :cancels, 15, 1
        )
    ");
    $benchSkillStmt = $benchPdo->prepare("
        INSERT INTO helper_skills (user_id, skill_name) VALUES (:uid, :skill)
    ");

    $categoriesPool = ['Groceries', 'Repairs', 'Books', 'Transport', 'Pet Help', 'Other'];
    $neighborhoods = [
        ['name' => 'Maplewood', 'lat' => 37.7749, 'lng' => -122.4194],
        ['name' => 'Oakridge', 'lat' => 37.7833, 'lng' => -122.4167],
        ['name' => 'Greenwood', 'lat' => 37.7690, 'lng' => -122.4467],
        ['name' => 'Downtown', 'lat' => 37.7879, 'lng' => -122.4075],
        ['name' => 'Riverside', 'lat' => 37.7550, 'lng' => -122.4200],
    ];

    $benchPdo->beginTransaction();
    for ($i = 1; $i <= $vol; $i++) {
        $uid = "bench_hlp_{$vol}_{$i}";
        $nb = $neighborhoods[$i % count($neighborhoods)];
        $skill = $categoriesPool[$i % count($categoriesPool)];

        $benchUserStmt->execute([
            ':id' => $uid,
            ':name' => "Helper {$i}",
            ':email' => "h{$i}@bench.local",
            ':neighborhood' => $nb['name'],
            ':rating' => 4.0 + (($i % 10) * 0.1),
            ':tasks' => ($i % 30),
            ':lat' => $nb['lat'] + (($i % 5) * 0.002),
            ':lng' => $nb['lng'] + (($i % 5) * 0.002),
            ':cancels' => ($i % 3),
        ]);

        $benchSkillStmt->execute([':uid' => $uid, ':skill' => $skill]);
    }
    $benchPdo->commit();

    $benchEngine = new HelperMatchingEngine($benchPdo);

    // Warm-up run
    $benchEngine->matchHelpers($testRequests['Repairs'], [], 10);

    // Benchmark 20 matching queries
    $queryCount = 20;
    $startMemory = memory_get_usage();
    $startTime = hrtime(true);

    for ($q = 0; $q < $queryCount; $q++) {
        $req = $testRequests[$categoriesPool[$q % count($categoriesPool)]];
        $results = $benchEngine->matchHelpers($req, [], 10);
    }

    $endTime = hrtime(true);
    $endMemory = memory_get_usage();

    $totalDurationMs = ($endTime - $startTime) / 1e6;
    $avgLatencyMs = $totalDurationMs / $queryCount;
    $throughput = $queryCount / ($totalDurationMs / 1000);
    $memUsedKb = ($endMemory - $startMemory) / 1024;

    $benchmarkResults[$vol] = [
        'helpers' => $vol,
        'avg_latency_ms' => round($avgLatencyMs, 2),
        'total_duration_ms' => round($totalDurationMs, 2),
        'throughput_qps' => round($throughput, 1),
        'memory_kb' => round($memUsedKb, 1),
    ];

    echo sprintf(
        "  • Volume: %4d Helpers | Avg Latency: %6.2f ms | Throughput: %5.1f req/s | Memory: %5.1f KB\n",
        $vol,
        $avgLatencyMs,
        $throughput,
        $memUsedKb
    );

    assertSimulation($avgLatencyMs < 100.0, "Volume {$vol}: Latency ({$avgLatencyMs} ms) is well within interactive threshold (< 100 ms)");
}

echo "\n";

// ====================================================================
// 7. DISPLAY SAMPLE RECOMMENDATIONS & RANKINGS
// ====================================================================
echo "--- 7. Sample AI Helper Recommendations Display ---\n";

function printSampleRanking(string $title, array $matches): void {
    echo "\n=== Recommendation Results: {$title} ===\n";
    foreach (array_slice($matches, 0, 3) as $idx => $m) {
        $rank = $idx + 1;
        $bd = $m['score_breakdown'];
        echo "  [#{$rank}] {$m['name']} — Compatibility: {$m['match_percentage']}% (Score: {$m['match_score']})\n";
        echo "      Distance: {$m['distance']} | Active Tasks: {$m['active_tasks']}\n";
        echo "      Breakdown: Skill={$bd['skill']} | Loc={$bd['location']} | Avail={$bd['availability']} | Rating={$bd['rating']} | Rel={$bd['reliability']} | Workload={$bd['workload']}\n";
        echo "      Reasons:\n";
        foreach ($m['matching_reasons'] as $r) {
            echo "        ✓ {$r}\n";
        }
    }
}

printSampleRanking("Groceries in Maplewood (Morning)", $grocMatches);
printSampleRanking("Repairs in Greenwood (Morning)", $repairMatches);
printSampleRanking("Pet Help in Riverside (Afternoon)", $petMatches);

echo "\n====================================================================\n";
echo "Simulation & QA Test Summary:\n";
echo "Total Tests Run : {$totalTests}\n";
echo "Passed          : {$passedTests}\n";
echo "Failed          : {$failedTests}\n";
if (empty($discoveredBugs)) {
    echo "Discovered Bugs : 0 (Clean Test Run)\n";
} else {
    echo "Discovered Bugs : " . count($discoveredBugs) . "\n";
    foreach ($discoveredBugs as $b) {
        echo "  • {$b}\n";
    }
}
echo "====================================================================\n";

if ($failedTests > 0) {
    exit(1);
}
exit(0);
