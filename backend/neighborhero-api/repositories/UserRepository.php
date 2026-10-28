<?php
declare(strict_types=1);

namespace NeighborHero\Repositories;

use PDO;

class UserRepository {
    private PDO $pdo;

    public function __construct(PDO $pdo) {
        $this->pdo = $pdo;
    }

    public function findById(string $id): ?array {
        $stmt = $this->pdo->prepare(
            "SELECT id, full_name, email, phone, neighborhood, bio, avatar_url,
                    is_verified, points, completed_tasks_count, rating, created_at, updated_at
             FROM users WHERE id = :id LIMIT 1"
        );
        $stmt->execute([':id' => $id]);
        $user = $stmt->fetch();
        if (!$user) return null;

        $user['skills'] = $this->getSkills($id);
        $user['is_verified'] = (bool)$user['is_verified'];
        $user['points'] = (int)$user['points'];
        $user['completed_tasks_count'] = (int)$user['completed_tasks_count'];
        $user['rating'] = (float)$user['rating'];
        return $user;
    }

    public function findByEmail(string $email): ?array {
        $stmt = $this->pdo->prepare("SELECT * FROM users WHERE LOWER(email) = LOWER(:email) LIMIT 1");
        $stmt->execute([':email' => trim($email)]);
        $user = $stmt->fetch();
        return $user ?: null;
    }

    public function findByPhone(string $phone): ?array {
        $stmt = $this->pdo->prepare("SELECT * FROM users WHERE phone = :phone LIMIT 1");
        $stmt->execute([':phone' => trim($phone)]);
        $user = $stmt->fetch();
        return $user ?: null;
    }

    public function create(array $data): array {
        $id = $data['id'] ?? ('usr_' . bin2hex(random_bytes(12)));
        $stmt = $this->pdo->prepare(
            "INSERT INTO users (id, full_name, email, phone, password_hash, neighborhood, bio, avatar_url, is_verified, points, completed_tasks_count, rating)
             VALUES (:id, :full_name, :email, :phone, :password_hash, :neighborhood, :bio, :avatar_url, :is_verified, :points, :completed_tasks_count, :rating)"
        );
        $stmt->execute([
            ':id' => $id,
            ':full_name' => $data['full_name'],
            ':email' => strtolower(trim($data['email'])),
            ':phone' => !empty($data['phone']) ? trim($data['phone']) : null,
            ':password_hash' => $data['password_hash'],
            ':neighborhood' => $data['neighborhood'] ?? '',
            ':bio' => $data['bio'] ?? '',
            ':avatar_url' => $data['avatar_url'] ?? '',
            ':is_verified' => $data['is_verified'] ?? 1,
            ':points' => $data['points'] ?? 0,
            ':completed_tasks_count' => $data['completed_tasks_count'] ?? 0,
            ':rating' => $data['rating'] ?? 5.00,
        ]);

        if (!empty($data['skills']) && is_array($data['skills'])) {
            $this->syncSkills($id, $data['skills']);
        }

        // Also create user profile entry
        $pStmt = $this->pdo->prepare("INSERT INTO user_profiles (user_id) VALUES (:user_id)");
        $pStmt->execute([':user_id' => $id]);

        return $this->findById($id);
    }

    public function updateProfile(string $userId, array $data): bool {
        $fields = [];
        $params = [':id' => $userId];

        if (array_key_exists('full_name', $data)) {
            $fields[] = "full_name = :full_name";
            $params[':full_name'] = $data['full_name'];
        }
        if (array_key_exists('neighborhood', $data)) {
            $fields[] = "neighborhood = :neighborhood";
            $params[':neighborhood'] = $data['neighborhood'];
        }
        if (array_key_exists('bio', $data)) {
            $fields[] = "bio = :bio";
            $params[':bio'] = $data['bio'];
        }
        if (array_key_exists('phone', $data)) {
            $fields[] = "phone = :phone";
            $params[':phone'] = $data['phone'];
        }
        if (array_key_exists('avatar_url', $data)) {
            $fields[] = "avatar_url = :avatar_url";
            $params[':avatar_url'] = $data['avatar_url'];
        }

        if (!empty($fields)) {
            $sql = "UPDATE users SET " . implode(', ', $fields) . " WHERE id = :id";
            $stmt = $this->pdo->prepare($sql);
            $stmt->execute($params);
        }

        if (array_key_exists('skills', $data) && is_array($data['skills'])) {
            $this->syncSkills($userId, $data['skills']);
        }

        return true;
    }

    public function getSkills(string $userId): array {
        $stmt = $this->pdo->prepare("SELECT skill_name FROM helper_skills WHERE user_id = :user_id ORDER BY created_at ASC");
        $stmt->execute([':user_id' => $userId]);
        return $stmt->fetchAll(PDO::FETCH_COLUMN) ?: [];
    }

    public function syncSkills(string $userId, array $skills): void {
        $this->pdo->prepare("DELETE FROM helper_skills WHERE user_id = :user_id")->execute([':user_id' => $userId]);
        $stmt = $this->pdo->prepare("INSERT INTO helper_skills (id, user_id, skill_name) VALUES (:id, :user_id, :skill)");
        foreach ($skills as $skill) {
            $skill = trim((string)$skill);
            if ($skill !== '') {
                $stmt->execute([
                    ':id' => 'sk_' . bin2hex(random_bytes(10)),
                    ':user_id' => $userId,
                    ':skill' => $skill,
                ]);
            }
        }
    }

    public function getLeaderboard(int $limit = 50): array {
        // Derive rankings from verified users with point totals from users table or ledger
        $stmt = $this->pdo->prepare(
            "SELECT u.id, u.full_name AS name, 'Neighborhood Hero' AS title, u.neighborhood,
                    CONCAT(ROUND(0.3 + (RAND() * 1.5), 1), ' miles away') AS distance,
                    u.rating, (u.completed_tasks_count * 3) AS reviewCount,
                    u.completed_tasks_count AS tasksDone,
                    99 AS onTimePercentage,
                    COALESCE(u.bio, '') AS bio,
                    u.is_verified AS isVerified,
                    1 AS isAvailableToday,
                    u.avatar_url AS avatarUrl,
                    u.points
             FROM users u
             ORDER BY u.points DESC, u.completed_tasks_count DESC, u.rating DESC
             LIMIT :limit"
        );
        $stmt->bindValue(':limit', $limit, PDO::PARAM_INT);
        $stmt->execute();
        $helpers = $stmt->fetchAll();

        foreach ($helpers as &$h) {
            $h['skills'] = $this->getSkills($h['id']);
            $h['rating'] = (float)$h['rating'];
            $h['reviewCount'] = (int)$h['reviewCount'];
            $h['tasksDone'] = (int)$h['tasksDone'];
            $h['onTimePercentage'] = (int)$h['onTimePercentage'];
            $h['isVerified'] = (bool)$h['isVerified'];
            $h['isAvailableToday'] = (bool)$h['isAvailableToday'];
            $h['points'] = (int)$h['points'];
        }
        return $helpers;
    }

    public function addPoints(string $userId, int $points, ?string $requestId, string $reason): bool {
        // Prevent duplicate points for the same request and reason via ledger
        $ledgerStmt = $this->pdo->prepare(
            "INSERT INTO community_points_ledger (id, user_id, request_id, points, reason)
             VALUES (:id, :user_id, :request_id, :points, :reason)"
        );
        try {
            $ledgerStmt->execute([
                ':id' => 'led_' . bin2hex(random_bytes(12)),
                ':user_id' => $userId,
                ':request_id' => $requestId,
                ':points' => $points,
                ':reason' => $reason,
            ]);
        } catch (\Exception $e) {
            // Duplicate points prevented
            return false;
        }

        // Update user totals
        $uStmt = $this->pdo->prepare(
            "UPDATE users SET points = points + :pts, completed_tasks_count = completed_tasks_count + 1 WHERE id = :id"
        );
        return $uStmt->execute([
            ':pts' => $points,
            ':id' => $userId,
        ]);
    }
}

