<?php
declare(strict_types=1);

namespace NeighborHero\Repositories;

use PDO;

class HelperRepository {
    private PDO $pdo;
    private UserRepository $userRepository;

    public function __construct(PDO $pdo) {
        $this->pdo = $pdo;
        $this->userRepository = new UserRepository($pdo);
    }

    public function findById(string $helperId): ?array {
        $stmt = $this->pdo->prepare(
            "SELECT u.id, u.full_name AS name, 'Neighborhood Hero' AS title,
                    COALESCE(u.neighborhood, 'Greenwood') AS neighborhood,
                    '0.4 miles away' AS distance,
                    u.rating,
                    (u.completed_tasks_count * 3) AS reviewCount,
                    u.completed_tasks_count AS tasksDone,
                    99 AS onTimePercentage,
                    COALESCE(u.bio, '') AS bio,
                    u.is_verified AS isVerified,
                    1 AS isAvailableToday,
                    COALESCE(u.avatar_url, '') AS avatarUrl
             FROM users u
             WHERE u.id = :id
             LIMIT 1"
        );
        $stmt->execute([':id' => $helperId]);
        $row = $stmt->fetch();
        if (!$row) return null;

        $row['skills'] = $this->userRepository->getSkills($row['id']);
        $row['rating'] = (float)$row['rating'];
        $row['reviewCount'] = (int)$row['reviewCount'];
        $row['tasksDone'] = (int)$row['tasksDone'];
        $row['onTimePercentage'] = (int)$row['onTimePercentage'];
        $row['isVerified'] = (bool)$row['isVerified'];
        $row['isAvailableToday'] = (bool)$row['isAvailableToday'];
        return $row;
    }

    public function getRecommendations(?string $skill = null, int $limit = 10): array {
        $where = ["u.is_verified = 1"];
        $params = [];

        if (!empty($skill)) {
            $where[] = "EXISTS (SELECT 1 FROM helper_skills s WHERE s.user_id = u.id AND LOWER(s.skill_name) = LOWER(:skill))";
            $params[':skill'] = trim($skill);
        }

        $whereSql = implode(' AND ', $where);
        $sql = "SELECT u.id, u.full_name AS name, 'Neighborhood Hero' AS title,
                       COALESCE(u.neighborhood, 'Greenwood') AS neighborhood,
                       CONCAT(ROUND(0.3 + (RAND() * 1.2), 1), ' miles away') AS distance,
                       u.rating,
                       (u.completed_tasks_count * 3) AS reviewCount,
                       u.completed_tasks_count AS tasksDone,
                       99 AS onTimePercentage,
                       COALESCE(u.bio, '') AS bio,
                       u.is_verified AS isVerified,
                       1 AS isAvailableToday,
                       COALESCE(u.avatar_url, '') AS avatarUrl
                FROM users u
                WHERE {$whereSql}
                ORDER BY u.rating DESC, u.completed_tasks_count DESC
                LIMIT :limit";

        $stmt = $this->pdo->prepare($sql);
        foreach ($params as $k => $v) {
            $stmt->bindValue($k, $v);
        }
        $stmt->bindValue(':limit', $limit, PDO::PARAM_INT);
        $stmt->execute();
        $helpers = $stmt->fetchAll();

        foreach ($helpers as &$h) {
            $h['skills'] = $this->userRepository->getSkills($h['id']);
            $h['rating'] = (float)$h['rating'];
            $h['reviewCount'] = (int)$h['reviewCount'];
            $h['tasksDone'] = (int)$h['tasksDone'];
            $h['onTimePercentage'] = (int)$h['onTimePercentage'];
            $h['isVerified'] = (bool)$h['isVerified'];
            $h['isAvailableToday'] = (bool)$h['isAvailableToday'];
        }
        return $helpers;
    }
}

