<?php
declare(strict_types=1);

/**
 * Migrate legacy user records from userdb.users into neighborhero_db.users.
 * Backward compatibility script that securely preserves existing users and
 * hashes plain-text legacy passwords using bcrypt.
 */

require_once __DIR__ . '/../bootstrap.php';

use NeighborHero\Services\Database;

try {
    $pdo = Database::getConnection();
    echo "Connected to neighborhero_db.\n";

    // 1. Ensure 'role' column exists in neighborhero_db.users
    $cols = $pdo->query("SHOW COLUMNS FROM users")->fetchAll(PDO::FETCH_COLUMN);
    if (!in_array('role', $cols)) {
        $pdo->exec("ALTER TABLE users ADD COLUMN role VARCHAR(32) NOT NULL DEFAULT 'requester'");
        echo "Added 'role' column to users table.\n";
    }

    // 2. Fetch existing emails from neighborhero_db.users
    $existingEmails = $pdo->query("SELECT LOWER(email) FROM users")->fetchAll(PDO::FETCH_COLUMN);
    $existingEmailsMap = array_flip(array_map('strtolower', $existingEmails));

    // 3. Query legacy users from userdb.users
    $legacyUsers = $pdo->query("SELECT id, name, email, password, created_at FROM userdb.users")->fetchAll(PDO::FETCH_ASSOC);
    echo "Found " . count($legacyUsers) . " users in userdb.users.\n";

    $insertStmt = $pdo->prepare(
        "INSERT INTO users (id, full_name, email, phone, password_hash, role, neighborhood, bio, avatar_url, is_verified, points, completed_tasks_count, rating, created_at)
         VALUES (:id, :full_name, :email, :phone, :password_hash, :role, :neighborhood, :bio, :avatar_url, :is_verified, :points, :completed_tasks_count, :rating, :created_at)"
    );

    $profileStmt = $pdo->prepare(
        "INSERT IGNORE INTO user_profiles (user_id, created_at) VALUES (:user_id, :created_at)"
    );

    $skillStmt = $pdo->prepare(
        "INSERT IGNORE INTO helper_skills (id, user_id, skill_name) VALUES (:id, :user_id, :skill)"
    );

    $migrated = 0;
    foreach ($legacyUsers as $leg) {
        $email = strtolower(trim($leg['email']));
        if (isset($existingEmailsMap[$email])) {
            echo "Skipping {$email}: already exists in neighborhero_db.users.\n";
            continue;
        }

        $userId = 'usr_leg_' . $leg['id'] . '_' . bin2hex(random_bytes(4));
        // Securely hash the legacy plain-text password with bcrypt
        $passwordHash = password_hash((string)$leg['password'], PASSWORD_DEFAULT);

        $insertStmt->execute([
            ':id' => $userId,
            ':full_name' => trim($leg['name'] ?: 'Neighbor'),
            ':email' => $email,
            ':phone' => null,
            ':password_hash' => $passwordHash,
            ':role' => 'requester',
            ':neighborhood' => 'Maplewood District',
            ':bio' => 'Community neighbor.',
            ':avatar_url' => '',
            ':is_verified' => 1,
            ':points' => 100,
            ':completed_tasks_count' => 0,
            ':rating' => 5.0,
            ':created_at' => $leg['created_at'] ?: date('Y-m-d H:i:s'),
        ]);

        $profileStmt->execute([
            ':user_id' => $userId,
            ':created_at' => $leg['created_at'] ?: date('Y-m-d H:i:s'),
        ]);

        $skillStmt->execute([
            ':id' => 'sk_' . bin2hex(random_bytes(8)),
            ':user_id' => $userId,
            ':skill' => 'Groceries'
        ]);

        $migrated++;
        echo "Migrated {$email} ({$leg['name']}) -> ID: {$userId}\n";
    }

    echo "Migration completed! Successfully migrated {$migrated} legacy accounts.\n";
} catch (Throwable $e) {
    echo "Error during migration: " . $e->getMessage() . "\n";
    exit(1);
}
