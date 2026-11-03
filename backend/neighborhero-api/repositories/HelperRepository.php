<?php
declare(strict_types=1);

namespace NeighborHero\Repositories;

use NeighborHero\Services\HelperMatchingEngine;
use NeighborHero\Services\UserRepository;
use PDO;

class HelperRepository {
    private PDO $pdo;
    private UserRepository|\NeighborHero\Repositories\UserRepository $userRepository;
    private HelperMatchingEngine $matchingEngine;

    public function __construct(PDO $pdo) {
        $this->pdo = $pdo;
        $this->userRepository = new \NeighborHero\Repositories\UserRepository($pdo);
        $this->matchingEngine = new HelperMatchingEngine($pdo);
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
                    COALESCE(u.is_available_today, 1) AS isAvailableToday,
                    COALESCE(u.avatar_url, '') AS avatarUrl,
                    u.points
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
        $row['avatar_url'] = $row['avatarUrl'];
        $row['points'] = (int)($row['points'] ?? 0);
        return $row;
    }

    /**
     * Retrieves AI-ranked helper recommendations using the multi-parameter matching engine.
     */
    public function getRecommendations(
        ?string $skill = null,
        int $limit = 10,
        ?string $location = null,
        ?string $requestedTime = null,
        ?string $requesterId = null
    ): array {
        $context = [
            'category'       => $skill ?? 'Other',
            'location'       => $location ?? 'Maplewood',
            'requested_time' => $requestedTime ?? 'Today',
            'requester_id'   => $requesterId,
        ];

        $matched = $this->matchingEngine->matchHelpers($context, [], $limit);

        // Ensure backward compatibility fields (camelCase + snake_case)
        foreach ($matched as &$h) {
            $h['avatar_url'] = $h['avatarUrl'] ?? '';
            $h['isAvailableToday'] = (bool)($h['is_available_today'] ?? $h['isAvailableToday'] ?? true);
        }

        return $matched;
    }

    /**
     * Retrieves AI-ranked helpers for an existing help request.
     */
    public function getRecommendationsForRequest(string $requestId, array $preferences = [], int $limit = 10): array {
        $matched = $this->matchingEngine->matchForRequest($requestId, $preferences, $limit);
        foreach ($matched as &$h) {
            $h['avatar_url'] = $h['avatarUrl'] ?? '';
            $h['isAvailableToday'] = (bool)($h['is_available_today'] ?? $h['isAvailableToday'] ?? true);
        }
        return $matched;
    }
}
