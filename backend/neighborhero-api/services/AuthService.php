<?php
declare(strict_types=1);

namespace NeighborHero\Services;

use PDO;

class AuthService {
    private PDO $pdo;

    public function __construct(PDO $pdo) {
        $this->pdo = $pdo;
    }

    public function hashPassword(string $password): string {
        return password_hash($password, PASSWORD_DEFAULT);
    }

    public function verifyPassword(string $password, string $hash): bool {
        return password_verify($password, $hash);
    }

    public function createToken(string $userId, ?string $deviceInfo = null, int $ttlSeconds = 2592000): string {
        $plainToken = bin2hex(random_bytes(32));
        $tokenHash = hash('sha256', $plainToken);
        $tokenId = 'tok_' . bin2hex(random_bytes(16));
        $expiresAt = gmdate('Y-m-d H:i:s', time() + $ttlSeconds);

        $stmt = $this->pdo->prepare(
            "INSERT INTO auth_tokens (id, user_id, token_hash, device_info, expires_at)
             VALUES (:id, :user_id, :token_hash, :device_info, :expires_at)"
        );
        $stmt->execute([
            ':id' => $tokenId,
            ':user_id' => $userId,
            ':token_hash' => $tokenHash,
            ':device_info' => $deviceInfo,
            ':expires_at' => $expiresAt,
        ]);

        return $plainToken;
    }

    public function authenticateToken(string $plainToken): ?array {
        if (empty($plainToken)) {
            return null;
        }

        $tokenHash = hash('sha256', $plainToken);
        $stmt = $this->pdo->prepare(
            "SELECT t.id AS token_id, t.user_id, t.expires_at, t.revoked_at,
                    u.id, u.full_name, u.email, u.phone, u.neighborhood, u.bio,
                    u.avatar_url, u.is_verified, u.points, u.completed_tasks_count, u.rating
             FROM auth_tokens t
             JOIN users u ON u.id = t.user_id
             WHERE t.token_hash = :hash
             LIMIT 1"
        );
        $stmt->execute([':hash' => $tokenHash]);
        $row = $stmt->fetch();

        if (!$row) {
            return null;
        }

        if ($row['revoked_at'] !== null) {
            return null; // Token revoked
        }

        // Compare UTC timestamps
        $now = gmdate('Y-m-d H:i:s');
        if ($row['expires_at'] < $now) {
            return null; // Token expired
        }

        return $row;
    }

    public function revokeToken(string $plainToken): bool {
        $tokenHash = hash('sha256', $plainToken);
        $stmt = $this->pdo->prepare(
            "UPDATE auth_tokens SET revoked_at = :now WHERE token_hash = :hash AND revoked_at IS NULL"
        );
        return $stmt->execute([
            ':now' => gmdate('Y-m-d H:i:s'),
            ':hash' => $tokenHash,
        ]);
    }
}

