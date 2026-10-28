<?php
declare(strict_types=1);

namespace NeighborHero\Controllers;

use NeighborHero\Services\Database;
use NeighborHero\Services\Response;
use Throwable;

class HealthController {
    public function check(): void {
        $dbStatus = 'connected';
        $driver = 'unknown';

        try {
            $pdo = Database::getConnection();
            $driver = $pdo->getAttribute(\PDO::ATTR_DRIVER_NAME);
            $pdo->query('SELECT 1');
        } catch (Throwable $e) {
            $dbStatus = 'disconnected: ' . $e->getMessage();
        }

        Response::json([
            'status' => ($dbStatus === 'connected') ? 'healthy' : 'degraded',
            'app' => 'NeighborHero API',
            'version' => '1.0.0',
            'database' => [
                'status' => $dbStatus,
                'driver' => $driver,
            ],
            'server_time_utc' => gmdate('Y-m-d H:i:s'),
        ]);
    }
}

