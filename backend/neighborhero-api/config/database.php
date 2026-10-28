<?php
declare(strict_types=1);

/**
 * Database configuration for NeighborHero API.
 * Supports XAMPP MySQL / MariaDB and fallback SQLite for automated testing.
 */

$localConfigFile = __DIR__ . '/local.database.php';
$localConfig = file_exists($localConfigFile) ? require $localConfigFile : [];

$defaultDriver = getenv('DB_DRIVER') ?: ($localConfig['driver'] ?? 'mysql');

return [
    'driver' => $defaultDriver,
    'mysql' => [
        'host' => getenv('DB_HOST') ?: ($localConfig['host'] ?? '127.0.0.1'),
        'port' => (int)(getenv('DB_PORT') ?: ($localConfig['port'] ?? 3306)),
        'database' => getenv('DB_NAME') ?: ($localConfig['database'] ?? 'neighborhero_db'),
        'username' => getenv('DB_USER') ?: ($localConfig['username'] ?? 'root'),
        'password' => getenv('DB_PASS') ?: ($localConfig['password'] ?? ''),
        'charset' => 'utf8mb4',
        'collation' => 'utf8mb4_unicode_ci',
        'options' => [
            PDO::ATTR_ERRMODE => PDO::ERRMODE_EXCEPTION,
            PDO::ATTR_DEFAULT_FETCH_MODE => PDO::FETCH_ASSOC,
            PDO::ATTR_EMULATE_PREPARES => false,
            PDO::MYSQL_ATTR_INIT_COMMAND => "SET NAMES utf8mb4 COLLATE utf8mb4_unicode_ci",
        ],
    ],
    'sqlite' => [
        'database' => getenv('SQLITE_PATH') ?: ($localConfig['sqlite_path'] ?? ':memory:'),
        'options' => [
            PDO::ATTR_ERRMODE => PDO::ERRMODE_EXCEPTION,
            PDO::ATTR_DEFAULT_FETCH_MODE => PDO::FETCH_ASSOC,
        ],
    ],
];

