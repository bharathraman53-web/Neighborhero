<?php
declare(strict_types=1);

namespace NeighborHero\Repositories;

use PDO;

class HelpRequestRepository {
    private PDO $pdo;

    public function __construct(PDO $pdo) {
        $this->pdo = $pdo;
    }

    public function findById(string $id): ?array {
        $stmt = $this->pdo->prepare(
            "SELECT r.id, r.title, r.category, r.description,
                    u.full_name AS requester_name,
                    u.rating AS requester_rating,
                    u.completed_tasks_count AS requester_task_count,
                    u.phone AS requester_phone,
                    r.location, r.distance, r.requested_time,
                    r.is_urgent, r.status,
                    r.assigned_helper_id,
                    h.full_name AS assigned_helper_name,
                    r.requester_id,
                    r.created_at, r.updated_at
             FROM help_requests r
             JOIN users u ON u.id = r.requester_id
             LEFT JOIN users h ON h.id = r.assigned_helper_id
             WHERE r.id = :id
             LIMIT 1"
        );
        $stmt->execute([':id' => $id]);
        $row = $stmt->fetch();
        return $row ? $this->mapRow($row) : null;
    }

    public function listAll(?string $category = null, ?string $status = null, int $page = 1, int $limit = 20): array {
        $where = [];
        $params = [];

        if ($category && $category !== 'All') {
            $where[] = "r.category = :category";
            $params[':category'] = $category;
        }

        if ($status) {
            $where[] = "r.status = :status";
            $params[':status'] = $status;
        }

        $whereClause = !empty($where) ? "WHERE " . implode(' AND ', $where) : "";
        $offset = max(0, ($page - 1) * $limit);

        $sql = "SELECT r.id, r.title, r.category, r.description,
                       u.full_name AS requester_name,
                       u.rating AS requester_rating,
                       u.completed_tasks_count AS requester_task_count,
                       u.phone AS requester_phone,
                       r.location, r.distance, r.requested_time,
                       r.is_urgent, r.status,
                       r.assigned_helper_id,
                       h.full_name AS assigned_helper_name,
                       r.requester_id,
                       r.created_at, r.updated_at
                FROM help_requests r
                JOIN users u ON u.id = r.requester_id
                LEFT JOIN users h ON h.id = r.assigned_helper_id
                {$whereClause}
                ORDER BY r.is_urgent DESC, r.created_at DESC
                LIMIT :limit OFFSET :offset";

        $stmt = $this->pdo->prepare($sql);
        foreach ($params as $k => $v) {
            $stmt->bindValue($k, $v);
        }
        $stmt->bindValue(':limit', $limit, PDO::PARAM_INT);
        $stmt->bindValue(':offset', $offset, PDO::PARAM_INT);
        $stmt->execute();

        $rows = $stmt->fetchAll();
        return array_map([$this, 'mapRow'], $rows);
    }

    public function findByRequester(string $requesterId): array {
        $stmt = $this->pdo->prepare(
            "SELECT r.id, r.title, r.category, r.description,
                    u.full_name AS requester_name,
                    u.rating AS requester_rating,
                    u.completed_tasks_count AS requester_task_count,
                    u.phone AS requester_phone,
                    r.location, r.distance, r.requested_time,
                    r.is_urgent, r.status,
                    r.assigned_helper_id,
                    h.full_name AS assigned_helper_name,
                    r.requester_id,
                    r.created_at, r.updated_at
             FROM help_requests r
             JOIN users u ON u.id = r.requester_id
             LEFT JOIN users h ON h.id = r.assigned_helper_id
             WHERE r.requester_id = :req_id
             ORDER BY r.created_at DESC"
        );
        $stmt->execute([':req_id' => $requesterId]);
        return array_map([$this, 'mapRow'], $stmt->fetchAll());
    }

    public function findByHelper(string $helperId): array {
        $stmt = $this->pdo->prepare(
            "SELECT r.id, r.title, r.category, r.description,
                    u.full_name AS requester_name,
                    u.rating AS requester_rating,
                    u.completed_tasks_count AS requester_task_count,
                    u.phone AS requester_phone,
                    r.location, r.distance, r.requested_time,
                    r.is_urgent, r.status,
                    r.assigned_helper_id,
                    h.full_name AS assigned_helper_name,
                    r.requester_id,
                    r.created_at, r.updated_at
             FROM help_requests r
             JOIN users u ON u.id = r.requester_id
             LEFT JOIN users h ON h.id = r.assigned_helper_id
             WHERE r.assigned_helper_id = :helper_id
             ORDER BY r.created_at DESC"
        );
        $stmt->execute([':helper_id' => $helperId]);
        return array_map([$this, 'mapRow'], $stmt->fetchAll());
    }

    public function create(array $data): array {
        $id = $data['id'] ?? ('req_' . bin2hex(random_bytes(10)));
        $stmt = $this->pdo->prepare(
            "INSERT INTO help_requests (id, requester_id, title, category, description, location, distance, requested_time, is_urgent, status)
             VALUES (:id, :requester_id, :title, :category, :description, :location, :distance, :requested_time, :is_urgent, :status)"
        );
        $stmt->execute([
            ':id' => $id,
            ':requester_id' => $data['requester_id'],
            ':title' => $data['title'],
            ':category' => $data['category'],
            ':description' => $data['description'],
            ':location' => $data['location'],
            ':distance' => $data['distance'] ?? '0.4 mi away',
            ':requested_time' => $data['requested_time'],
            ':is_urgent' => !empty($data['is_urgent']) ? 1 : 0,
            ':status' => 'OPEN',
        ]);

        return $this->findById($id);
    }

    public function updateStatus(string $requestId, string $newStatus, ?string $helperId = null, ?string $changedByUserId = null): bool {
        $startedTransaction = false;
        if (!$this->pdo->inTransaction()) {
            $this->pdo->beginTransaction();
            $startedTransaction = true;
        }

        try {
            // Fetch current status
            $chk = $this->pdo->prepare("SELECT status, assigned_helper_id FROM help_requests WHERE id = :id");
            $chk->execute([':id' => $requestId]);
            $current = $chk->fetch();
            if (!$current) {
                if ($startedTransaction) {
                    $this->pdo->rollBack();
                }
                return false;
            }

            $params = [
                ':id' => $requestId,
                ':status' => $newStatus,
            ];

            $updateHelperSql = "";
            if ($helperId !== null) {
                $updateHelperSql = ", assigned_helper_id = :helper_id";
                $params[':helper_id'] = $helperId;
            }

            $stmt = $this->pdo->prepare(
                "UPDATE help_requests SET status = :status {$updateHelperSql} WHERE id = :id"
            );
            $stmt->execute($params);

            // Record status history audit
            $hStmt = $this->pdo->prepare(
                "INSERT INTO task_status_history (id, request_id, changed_by_user_id, old_status, new_status)
                 VALUES (:id, :req_id, :user_id, :old, :new)"
            );
            $hStmt->execute([
                ':id' => 'tsh_' . bin2hex(random_bytes(10)),
                ':req_id' => $requestId,
                ':user_id' => $changedByUserId ?? $helperId ?? 'system',
                ':old' => $current['status'],
                ':new' => $newStatus,
            ]);

            if ($startedTransaction) {
                $this->pdo->commit();
            }
            return true;
        } catch (\Exception $e) {
            if ($startedTransaction) {
                $this->pdo->rollBack();
            }
            throw $e;
        }
    }

    private function mapRow(array $row): array {
        return [
            'id' => (string)$row['id'],
            'title' => (string)$row['title'],
            'category' => (string)$row['category'],
            'description' => (string)$row['description'],
            'requester_name' => (string)$row['requester_name'],
            'requester_rating' => (float)$row['requester_rating'],
            'requester_task_count' => (int)$row['requester_task_count'],
            'requester_phone' => (string)($row['requester_phone'] ?? ''),
            'location' => (string)$row['location'],
            'distance' => (string)$row['distance'],
            'requested_time' => (string)$row['requested_time'],
            'is_urgent' => (bool)$row['is_urgent'],
            'status' => (string)$row['status'],
            'assigned_helper_id' => $row['assigned_helper_id'] ? (string)$row['assigned_helper_id'] : null,
            'assigned_helper_name' => $row['assigned_helper_name'] ? (string)$row['assigned_helper_name'] : null,
            'requester_id' => (string)$row['requester_id'],
            'created_at' => (string)$row['created_at'],
        ];
    }
}
