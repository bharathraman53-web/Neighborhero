<?php
declare(strict_types=1);

/**
 * End-to-end authentication test suite for NeighborHero.
 * Tests:
 * 1. Older existing account login (e.g., bharath@gmail.com, nithish@gmail.com, madesh@gmail.com, bharathraman53@gmail.com)
 * 2. Newly created account login
 * 3. Correct Gmail + incorrect password -> 401 error
 * 4. Unregistered Gmail -> 401 error
 * 5. Email with accidental leading/trailing spaces -> Login succeeds
 * 6. Requester login -> role = requester
 * 7. Helper login -> role = helper
 * 8. Token authentication -> resolves user correctly
 * 9. Token logout / revocation -> token becomes invalid
 */

require_once __DIR__ . '/../bootstrap.php';

use NeighborHero\Services\Database;
use NeighborHero\Services\AuthService;
use NeighborHero\Repositories\UserRepository;

$pdo = Database::getConnection();
$authService = new AuthService($pdo);
$userRepo = new UserRepository($pdo);

$passed = 0;
$failed = 0;

function assertCondition(bool $cond, string $desc): void {
    global $passed, $failed;
    if ($cond) {
        $passed++;
        echo "  [PASS] {$desc}\n";
    } else {
        $failed++;
        echo "  [FAIL] {$desc}\n";
    }
}

function postJson(string $path, array $data, ?string $token = null): array {
    $url = "http://127.0.0.1/neighborhero-api/public/api" . $path;
    $ch = curl_init($url);
    $payload = json_encode($data);
    $headers = [
        'Content-Type: application/json',
        'Accept: application/json',
    ];
    if ($token) {
        $headers[] = "Authorization: Bearer {$token}";
    }
    curl_setopt($ch, CURLOPT_POSTFIELDS, $payload);
    curl_setopt($ch, CURLOPT_HTTPHEADER, $headers);
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
    $raw = curl_exec($ch);
    $httpCode = curl_getinfo($ch, CURLINFO_HTTP_CODE);
    curl_close($ch);
    $decoded = json_decode((string)$raw, true) ?: [];
    return ['status' => $httpCode, 'body' => $decoded, 'raw' => $raw];
}

function getJson(string $path, string $token): array {
    $url = "http://127.0.0.1/neighborhero-api/public/api" . $path;
    $ch = curl_init($url);
    $headers = [
        'Accept: application/json',
        "Authorization: Bearer {$token}",
    ];
    curl_setopt($ch, CURLOPT_HTTPHEADER, $headers);
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
    $raw = curl_exec($ch);
    $httpCode = curl_getinfo($ch, CURLINFO_HTTP_CODE);
    curl_close($ch);
    $decoded = json_decode((string)$raw, true) ?: [];
    return ['status' => $httpCode, 'body' => $decoded];
}

echo "=== NeighborHero End-to-End Authentication Test Suite ===\n\n";

// Scenario 1: Older existing account from userdb (bharath@gmail.com with bharath@123)
echo "--- Scenario 1: Older Existing Account Login ---\n";
$res1 = postJson('/auth/login', [
    'email_or_phone' => 'bharath@gmail.com',
    'password' => 'bharath@123'
]);
assertCondition($res1['status'] === 200, "Older account (bharath@gmail.com) login returns HTTP 200");
assertCondition($res1['body']['success'] === true, "Older account login response indicates success");
assertCondition(!empty($res1['body']['data']['token']), "Older account login generates valid session token");
assertCondition(($res1['body']['data']['user']['email'] ?? '') === 'bharath@gmail.com', "Older account resolves correct email");

// Scenario 1b: Existing account (bharathraman53@gmail.com with Bharath@123)
$res1b = postJson('/auth/login', [
    'email_or_phone' => 'bharathraman53@gmail.com',
    'password' => 'Bharath@123'
]);
assertCondition($res1b['status'] === 200, "Account (bharathraman53@gmail.com) login returns HTTP 200");
assertCondition($res1b['body']['success'] === true, "bharathraman53@gmail.com login succeeds");
assertCondition(($res1b['body']['data']['user']['role'] ?? '') === 'requester', "bharathraman53 has requester role");

// Scenario 1c: Existing account (madesh@gmail.com with madesh@123)
$res1c = postJson('/auth/login', [
    'email_or_phone' => 'madesh@gmail.com',
    'password' => 'madesh@123'
]);
assertCondition($res1c['status'] === 200, "Account (madesh@gmail.com) login returns HTTP 200");
assertCondition($res1c['body']['success'] === true, "madesh@gmail.com login succeeds");

// Scenario 2: Newly created account registration and login
echo "\n--- Scenario 2: Newly Created Account Registration & Login ---\n";
$uniqueEmail = 'newuser_' . time() . '@gmail.com';
$uniquePhone = '9' . str_pad((string)mt_rand(100000000, 999999999), 9, '0', STR_PAD_LEFT);
$regRes = postJson('/auth/register', [
    'full_name' => 'Test New User',
    'email' => $uniqueEmail,
    'phone' => $uniquePhone,
    'password' => 'SecurePass123!',
    'role' => 'requester'
]);
assertCondition($regRes['status'] === 201, "New account registration returns HTTP 201");
assertCondition($regRes['body']['success'] === true, "New account registration succeeds");

$newLoginRes = postJson('/auth/login', [
    'email_or_phone' => $uniqueEmail,
    'password' => 'SecurePass123!'
]);
assertCondition($newLoginRes['status'] === 200, "Newly created account logs in successfully (HTTP 200)");
assertCondition($newLoginRes['body']['success'] === true, "New account login response indicates success");
assertCondition(!empty($newLoginRes['body']['data']['token']), "New account receives valid bearer token");

// Scenario 3: Correct Gmail + incorrect password -> proper error
echo "\n--- Scenario 3: Correct Email + Incorrect Password ---\n";
$badPassRes = postJson('/auth/login', [
    'email_or_phone' => 'bharathraman53@gmail.com',
    'password' => 'WrongPassword!'
]);
assertCondition($badPassRes['status'] === 401, "Incorrect password returns HTTP 401");
assertCondition($badPassRes['body']['success'] === false, "Incorrect password response indicates failure");
assertCondition(str_contains($badPassRes['body']['message'] ?? '', 'Incorrect email or phone number, or password'), "Appropriate error message returned without user enumeration");

// Scenario 4: Unregistered Gmail -> proper error
echo "\n--- Scenario 4: Unregistered Email ---\n";
$unregRes = postJson('/auth/login', [
    'email_or_phone' => 'nonexistent_user_99999@gmail.com',
    'password' => 'AnyPassword123!'
]);
assertCondition($unregRes['status'] === 401, "Unregistered email returns HTTP 401");
assertCondition($unregRes['body']['success'] === false, "Unregistered email response indicates failure");
assertCondition(str_contains($unregRes['body']['message'] ?? '', 'Incorrect email or phone number, or password'), "Safe error message returned");

// Scenario 5: Accidental leading/trailing spaces in email
echo "\n--- Scenario 5: Email with Accidental Spaces ---\n";
$spacedRes = postJson('/auth/login', [
    'email_or_phone' => '   bharathraman53@gmail.com   ',
    'password' => 'Bharath@123'
]);
assertCondition($spacedRes['status'] === 200, "Email with whitespace is properly trimmed and returns HTTP 200");
assertCondition($spacedRes['body']['success'] === true, "Trimmed email login succeeds");

// Scenario 6: Requester login -> role is requester
echo "\n--- Scenario 6: Requester Dashboard Role ---\n";
assertCondition(($res1b['body']['data']['user']['role'] ?? '') === 'requester', "Requester user has role 'requester' (routes to Home dashboard)");

// Scenario 7: Helper login -> role is helper
echo "\n--- Scenario 7: Helper Dashboard Role ---\n";
// Update nithish to helper role
$userRepo->updateRole('usr_4a31eff2ae44438853bb3ea5', 'helper');
$helperLoginRes = postJson('/auth/login', [
    'email_or_phone' => 'nithish@gmail.com',
    'password' => 'password123'
]);
assertCondition($helperLoginRes['status'] === 200, "Helper (nithish@gmail.com) login returns HTTP 200");
assertCondition(($helperLoginRes['body']['data']['user']['role'] ?? '') === 'helper', "Helper user has role 'helper' (routes to Nearby Requests dashboard)");

// Scenario 8: Token persistence and authentication
echo "\n--- Scenario 8: Token Persistence & Authentication ---\n";
$token = $res1b['body']['data']['token'];
$meRes = getJson('/auth/me', $token);
assertCondition($meRes['status'] === 200, "Authenticated /auth/me with bearer token returns HTTP 200");
assertCondition(($meRes['body']['data']['email'] ?? '') === 'bharathraman53@gmail.com', "Bearer token resolves authenticated user identity");

// Scenario 9: Logout & token revocation
echo "\n--- Scenario 9: Logout & Token Revocation ---\n";
$logoutRes = postJson('/auth/logout', [], $token);
assertCondition($logoutRes['status'] === 200, "Logout returns HTTP 200");
$meAfterLogout = getJson('/auth/me', $token);
assertCondition($meAfterLogout['status'] === 401, "Accessing /auth/me with revoked token returns HTTP 401 Unauthorized");

echo "\n==================================================\n";
echo "Total Tests Run: " . ($passed + $failed) . "\n";
echo "Passed: {$passed} | Failed: {$failed}\n";
echo "==================================================\n";

if ($failed > 0) {
    exit(1);
}
