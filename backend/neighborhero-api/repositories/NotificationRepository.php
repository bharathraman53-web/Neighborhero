<?php
declare(strict_types=1);

namespace NeighborHero\Repositories;

use PDO;

class NotificationRepository {
    private PDO $pdo;

    public function __construct(PDO $pdo) {
        $this->pdo = $pdo;
    }

    public function getNotificationsForUser(string $userId, int $page = 1, int $limit = 30): array {
        $offset = max(0, ($page - 1) * $limit);
        $stmt = $this->pdo->prepare(
            "SELECT id, title, message, type, is_read, related_request_id, created_at
             FROM notifications
             WHERE user_id = :user_id
             ORDER BY created_at DESC
             LIMIT :limit OFFSET :offset"
        );
        $stmt->bindValue(':user_id', $userId);
        $stmt->bindValue(':limit', $limit, PDO::PARAM_INT);
        $stmt->bindValue(':offset', $offset, PDO::PARAM_INT);
        $stmt->execute();

        $rows = $stmt->fetchAll();
        return array_map(function($r) {
            return [
                'id' => (string)$r['id'],
                'title' => (string)$r['title'],
                'message' => (string)$r['message'],
                'type' => (string)$r['type'],
                'time_ago' => $this->timeAgo($r['created_at']),
                'is_read' => (bool)$r['is_read'],
                'related_request_id' => $r['related_request_id'],
            ];
        }, $rows);
    }

    public function markAsRead(string $notificationId, string $userId): bool {
        $stmt = $this->pdo->prepare(
            "UPDATE notifications SET is_read = 1 WHERE id = :id AND user_id = :uid"
        );
        return $stmt->execute([':id' => $notificationId, ':uid' => $userId]);
    }

    public function markAllAsRead(string $userId): bool {
        $stmt = $this->pdo->prepare(
            "UPDATE notifications SET is_read = 1 WHERE user_id = :uid AND is_read = 0"
        );
        return $stmt->execute([':uid' => $userId]);
    }

    public function createNotification(string $userId, string $title, string $message, string $type, ?string $relatedRequestId = null): array {
        $id = 'notif_' . bin2hex(random_bytes(10));
        $now = gmdate('Y-m-d H:i:s');
        $stmt = $this->pdo->prepare(
            "INSERT INTO notifications (id, user_id, title, message, type, related_request_id, created_at)
             VALUES (:id, :user_id, :title, :message, :type, :req_id, :created_at)"
        );
        $stmt->execute([
            ':id' => $id,
            ':user_id' => $userId,
            ':title' => $title,
            ':message' => $message,
            ':type' => $type,
            ':req_id' => $relatedRequestId,
            ':created_at' => $now,
        ]);

        return [
            'id' => $id,
            'title' => $title,
            'message' => $message,
            'type' => $type,
            'time_ago' => 'Just now',
            'is_read' => false,
            'related_request_id' => $relatedRequestId,
        ];
    }

    private function timeAgo(string $datetime): string {
        $time = strtotime($datetime);
        $diff = time() - $time;

        if ($diff < 60) return "Just now";
        if ($diff < 3600) return floor($diff / 60) . " mins ago";
        if ($diff < 86400) return floor($diff / 3600) . " hours ago";
        if ($diff < 604800) return floor($diff / 86400) . " days ago";
        return date('M d, Y', $time);
    }
}

