<?php
declare(strict_types=1);

namespace NeighborHero\Middleware;

use NeighborHero\Services\AuthService;
use NeighborHero\Services\Database;
use NeighborHero\Services\Response;

class AuthMiddleware {
    private static ?array $authenticatedUser = null;

    public static function authenticate(bool $required = true): ?array {
        if (self::$authenticatedUser !== null) {
            return self::$authenticatedUser;
        }

        $headers = getallheaders();
        $authHeader = $headers['Authorization'] ?? $headers['authorization'] ?? '';

        if (empty($authHeader) && isset($_SERVER['HTTP_AUTHORIZATION'])) {
            $authHeader = $_SERVER['HTTP_AUTHORIZATION'];
        }

        if (preg_match('/Bearer\s+(\S+)/i', $authHeader, $matches)) {
            $token = $matches[1];
            $authService = new AuthService(Database::getConnection());
            $user = $authService->authenticateToken($token);

            if ($user !== null) {
                self::$authenticatedUser = $user;
                return $user;
            }
        }

        if ($required) {
            Response::unauthorized('Invalid, expired, or missing bearer token.');
        }

        return null;
    }

    public static function user(): ?array {
        return self::$authenticatedUser;
    }

    public static function setUser(?array $user): void {
        self::$authenticatedUser = $user;
    }
}

