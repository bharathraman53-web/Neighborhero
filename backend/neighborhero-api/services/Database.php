<?php
declare(strict_types=1);

namespace NeighborHero\Services;

use PDO;
use PDOException;
use RuntimeException;

class Database {
    private static ?PDO $instance = null;

    public static function getConnection(?array $customConfig = null): PDO {
        if (self::$instance !== null && $customConfig === null) {
            return self::$instance;
        }

        $config = $customConfig ?? require __DIR__ . '/../config/database.php';
        $driver = $config['driver'] ?? 'mysql';

        try {
            if ($driver === 'sqlite') {
                $path = $config['sqlite']['database'] ?? ':memory:';
                $pdo = new PDO("sqlite:{$path}", null, null, $config['sqlite']['options'] ?? []);
                $pdo->exec('PRAGMA foreign_keys = ON;');
            } else {
                $db = $config['mysql'];
                $dsn = sprintf(
                    'mysql:host=%s;port=%d;dbname=%s;charset=%s',
                    $db['host'],
                    $db['port'],
                    $db['database'],
                    $db['charset']
                );
                $pdo = new PDO($dsn, $db['username'], $db['password'], $db['options']);
            }

            if ($customConfig === null) {
                self::$instance = $pdo;
            }

            return $pdo;
        } catch (PDOException $e) {
            throw new RuntimeException("Database connection failure: " . $e->getMessage(), (int)$e->getCode(), $e);
        }
    }

    public static function setConnection(?PDO $pdo): void {
        self::$instance = $pdo;
    }
}

