<?php
declare(strict_types=1);

namespace NeighborHero\Repositories;

use PDO;

class SafetyRepository {
    private PDO $pdo;

    public function __construct(PDO $pdo) {
        $this->pdo = $pdo;
    }

    public function triggerSos(string $userId, ?float $lat = null, ?float $lng = null, string $address = 'Maplewood District', string $alertType = 'EMERGENCY_SOS'): array {
        $id = 'sos_' . bin2hex(random_bytes(10));
        $now = gmdate('Y-m-d H:i:s');
        $stmt = $this->pdo->prepare(
            "INSERT INTO sos_alerts (id, user_id, latitude, longitude, address, alert_type, status, created_at)
             VALUES (:id, :user_id, :lat, :lng, :address, :alert_type, 'ACTIVE', :created_at)"
        );
        $stmt->execute([
            ':id' => $id,
            ':user_id' => $userId,
            ':lat' => $lat,
            ':lng' => $lng,
            ':address' => $address,
            ':alert_type' => $alertType,
            ':created_at' => $now,
        ]);

        return [
            'id' => $id,
            'user_id' => $userId,
            'alert_type' => $alertType,
            'status' => 'ACTIVE',
            'address' => $address,
            'created_at' => $now,
        ];
    }

    public function resolveSos(string $sosId, string $userId): bool {
        $stmt = $this->pdo->prepare(
            "UPDATE sos_alerts SET status = 'RESOLVED' WHERE id = :id AND user_id = :uid"
        );
        return $stmt->execute([':id' => $sosId, ':uid' => $userId]);
    }

    public function getTaskHistory(string $userId): array {
        // Retrieve past completed or cancelled tasks for the user (as requester or helper)
        $stmt = $this->pdo->prepare(
            "SELECT r.id, r.title, r.description AS subtitle,
                    r.created_at, r.status, r.category,
                    COALESCE(rt.rating, 0.0) AS rating
             FROM help_requests r
             LEFT JOIN ratings rt ON rt.request_id = r.id
             WHERE (r.requester_id = :uid OR r.assigned_helper_id = :uid)
               AND r.status IN ('COMPLETED', 'CANCELLED')
             ORDER BY r.created_at DESC"
        );
        $stmt->execute([':uid' => $userId]);
        $rows = $stmt->fetchAll();

        return array_map(function($r) {
            return [
                'id' => (string)$r['id'],
                'title' => (string)$r['title'],
                'subtitle' => (string)$r['subtitle'],
                'date' => date('M d, Y', strtotime($r['created_at'])),
                'rating' => (float)$r['rating'],
                'status' => ucfirst(strtolower((string)$r['status'])),
                'category' => strtolower((string)$r['status']), // 'completed' or 'cancelled' for filter chips
            ];
        }, $rows);
    }
}

