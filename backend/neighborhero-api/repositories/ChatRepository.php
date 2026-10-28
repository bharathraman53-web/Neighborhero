<?php
declare(strict_types=1);

namespace NeighborHero\Repositories;

use PDO;
use RuntimeException;

class ChatRepository {
    private PDO $pdo;

    public function __construct(PDO $pdo) {
        $this->pdo = $pdo;
    }

    public function getOrCreateConversation(string $userId1, string $userId2, ?string $requestId = null): array {
        // Ensure consistent ordering so conversation between A & B is uniquely identified
        $p1 = min($userId1, $userId2);
        $p2 = max($userId1, $userId2);

        $stmt = $this->pdo->prepare(
            "SELECT * FROM chat_conversations
             WHERE participant1_id = :p1 AND participant2_id = :p2
             LIMIT 1"
        );
        $stmt->execute([':p1' => $p1, ':p2' => $p2]);
        $conv = $stmt->fetch();

        if ($conv) {
            return $conv;
        }

        $id = 'conv_' . bin2hex(random_bytes(10));
        $ins = $this->pdo->prepare(
            "INSERT INTO chat_conversations (id, request_id, participant1_id, participant2_id)
             VALUES (:id, :req_id, :p1, :p2)"
        );
        $ins->execute([
            ':id' => $id,
            ':req_id' => $requestId,
            ':p1' => $p1,
            ':p2' => $p2,
        ]);

        return [
            'id' => $id,
            'request_id' => $requestId,
            'participant1_id' => $p1,
            'participant2_id' => $p2,
        ];
    }

    public function getMessages(string $userId, ?string $conversationId = null, ?string $sinceId = null, int $limit = 50): array {
        $params = [':user_id' => $userId];
        $where = ["(c.participant1_id = :user_id OR c.participant2_id = :user_id)"];

        if (!empty($conversationId)) {
            $where[] = "m.conversation_id = :conv_id";
            $params[':conv_id'] = $conversationId;
        }

        if (!empty($sinceId)) {
            $where[] = "m.id > :since_id";
            $params[':since_id'] = $sinceId;
        }

        $whereSql = implode(' AND ', $where);

        $sql = "SELECT m.id, m.conversation_id, m.sender_id,
                       u.full_name AS sender_name,
                       m.message_text, m.attachment_title, m.attachment_code,
                       m.created_at
                FROM chat_messages m
                JOIN chat_conversations c ON c.id = m.conversation_id
                JOIN users u ON u.id = m.sender_id
                WHERE {$whereSql}
                ORDER BY m.created_at ASC
                LIMIT :limit";

        $stmt = $this->pdo->prepare($sql);
        foreach ($params as $k => $v) {
            $stmt->bindValue($k, $v);
        }
        $stmt->bindValue(':limit', $limit, PDO::PARAM_INT);
        $stmt->execute();

        $rows = $stmt->fetchAll();
        $formatted = [];
        foreach ($rows as $r) {
            $isUser = ($r['sender_id'] === $userId);
            $names = explode(' ', trim($r['sender_name']));
            $initials = '';
            foreach ($names as $n) {
                if (!empty($n)) $initials .= mb_substr($n, 0, 1);
            }
            if ($isUser) $initials = 'ME';

            $formatted[] = [
                'id' => (string)$r['id'],
                'sender_id' => (string)$r['sender_id'],
                'sender_name' => $isUser ? 'You' : (string)$r['sender_name'],
                'sender_initials' => $initials ?: 'NH',
                'message_text' => (string)$r['message_text'],
                'timestamp' => gmdate('g:i A', strtotime($r['created_at'])),
                'is_user' => $isUser,
                'attachment_code' => $r['attachment_code'],
                'attachment_title' => $r['attachment_title'],
            ];
        }

        return $formatted;
    }

    public function sendMessage(string $senderId, string $messageText, ?string $conversationId = null, ?string $recipientId = null, ?string $attachmentCode = null, ?string $attachmentTitle = null): array {
        if (empty($conversationId)) {
            if (empty($recipientId)) {
                // If neither given, check if active conversation exists or find the default active task partner
                $taskStmt = $this->pdo->prepare(
                    "SELECT requester_id, assigned_helper_id FROM help_requests
                     WHERE (requester_id = :uid OR assigned_helper_id = :uid) AND status = 'IN_PROGRESS'
                     LIMIT 1"
                );
                $taskStmt->execute([':uid' => $senderId]);
                $task = $taskStmt->fetch();
                if ($task) {
                    $recipientId = ($task['requester_id'] === $senderId) ? $task['assigned_helper_id'] : $task['requester_id'];
                }
            }

            if (empty($recipientId)) {
                // Check if any other user exists
                $otherStmt = $this->pdo->prepare("SELECT id FROM users WHERE id != :uid LIMIT 1");
                $otherStmt->execute([':uid' => $senderId]);
                $recipientId = $otherStmt->fetchColumn() ?: null;
            }

            if (empty($recipientId)) {
                throw new RuntimeException("No recipient available to receive message", 400);
            }

            $conv = $this->getOrCreateConversation($senderId, $recipientId);
            $conversationId = $conv['id'];
        }

        // Verify sender is participant
        $cStmt = $this->pdo->prepare(
            "SELECT participant1_id, participant2_id FROM chat_conversations WHERE id = :id LIMIT 1"
        );
        $cStmt->execute([':id' => $conversationId]);
        $conv = $cStmt->fetch();
        if (!$conv || ($conv['participant1_id'] !== $senderId && $conv['participant2_id'] !== $senderId)) {
            throw new RuntimeException("You do not have access to this conversation", 403);
        }

        $msgId = 'msg_' . bin2hex(random_bytes(10));
        $createdAt = gmdate('Y-m-d H:i:s');

        $stmt = $this->pdo->prepare(
            "INSERT INTO chat_messages (id, conversation_id, sender_id, message_text, attachment_code, attachment_title, created_at)
             VALUES (:id, :conv_id, :sender_id, :text, :att_code, :att_title, :created_at)"
        );
        $stmt->execute([
            ':id' => $msgId,
            ':conv_id' => $conversationId,
            ':sender_id' => $senderId,
            ':text' => trim($messageText),
            ':att_code' => $attachmentCode,
            ':att_title' => $attachmentTitle,
            ':created_at' => $createdAt,
        ]);

        return [
            'id' => $msgId,
            'sender_id' => $senderId,
            'sender_name' => 'You',
            'sender_initials' => 'ME',
            'message_text' => trim($messageText),
            'timestamp' => 'Just now',
            'is_user' => true,
            'attachment_code' => $attachmentCode,
            'attachment_title' => $attachmentTitle,
        ];
    }
}

