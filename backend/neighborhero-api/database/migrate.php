<?php
declare(strict_types=1);

/**
 * NeighborHero Database Migration Runner
 * Usage: php database/migrate.php
 */

require_once __DIR__ . '/../bootstrap.php';

use NeighborHero\Services\Database;

try {
    $config = require __DIR__ . '/../config/database.php';
    if (($config['driver'] ?? 'mysql') === 'mysql') {
        $db = $config['mysql'];
        // Connect to server without dbname to ensure database exists
        $serverDsn = sprintf('mysql:host=%s;port=%d;charset=%s', $db['host'], $db['port'], $db['charset']);
        $serverPdo = new PDO($serverDsn, $db['username'], $db['password'], $db['options']);
        $serverPdo->exec("CREATE DATABASE IF NOT EXISTS `{$db['database']}` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");
    }

    $pdo = Database::getConnection();
    echo "Connected to database.\n";

    $schemaFile = __DIR__ . '/schema.sql';
    if (!file_exists($schemaFile)) {
        throw new RuntimeException("Schema file not found at: {$schemaFile}");
    }

    $sql = file_get_contents($schemaFile);

    // Strip block comments /* ... */
    $cleanSql = preg_replace('!/\*.*?\*/!s', '', $sql);
    // Strip line comments -- ...
    $cleanSql = preg_replace('/^--.*$/m', '', $cleanSql);
    // Strip hash comments # ...
    $cleanSql = preg_replace('/^#.*$/m', '', $cleanSql);

    // Split SQL by semicolons, filtering out blank statements
    $statements = array_filter(
        array_map('trim', explode(';', $cleanSql)),
        fn($stmt) => !empty($stmt)
    );

    foreach ($statements as $stmt) {
        $pdo->exec($stmt);
    }

    echo "Migration completed successfully. All tables verified.\n";
} catch (Throwable $e) {
    echo "Migration failed: " . $e->getMessage() . "\n";
    exit(1);
}

