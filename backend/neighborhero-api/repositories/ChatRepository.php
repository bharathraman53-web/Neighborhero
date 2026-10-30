<?php
declare(strict_types=1);

namespace NeighborHero\Repositories;

use PDO;
use RuntimeException;
use Throwable;

class ChatRepository {
    private PDO $pdo;

    public function __construct(PDO $pdo) {
        $this->pdo = $pdo;
    }

    public function getOrCreateConversation(string $userId1, string $userId2, ?string $requestId = null): array {
        if ($userId1 === $userId2) {
            throw new RuntimeException("Cannot start a conversation with yourself.", 400);
        }

        // Verify both users exist
        $userCheck = $this->pdo->prepare("SELECT id FROM users WHERE id IN (:u1, :u2)");
        $userCheck->execute([':u1' => $userId1, ':u2' => $userId2]);
        $found = $userCheck->fetchAll(PDO::FETCH_COLUMN);
        if (count($found) < 2) {
            throw new RuntimeException("One or both conversation participants do not exist.", 404);
        }

        // Ensure consistent canonical ordering so (A, B) and (B, A) share the same conversation
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
            if (!empty($requestId) && empty($conv['request_id'])) {
                $upd = $this->pdo->prepare("UPDATE chat_conversations SET request_id = :req WHERE id = :id");
                $upd->execute([':req' => $requestId, ':id' => $conv['id']]);
                $conv['request_id'] = $requestId;
            }
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

    public function getConversation(string $conversationId, string $authenticatedUserId): array {
        $stmt = $this->pdo->prepare(
            "SELECT c.*,
                    u1.full_name AS p1_name, u1.avatar_url AS p1_avatar,
                    u2.full_name AS p2_name, u2.avatar_url AS p2_avatar
             FROM chat_conversations c
             JOIN users u1 ON u1.id = c.participant1_id
             JOIN users u2 ON u2.id = c.participant2_id
             WHERE c.id = :id
             LIMIT 1"
        );
        $stmt->execute([':id' => $conversationId]);
        $conv = $stmt->fetch();

        if (!$conv) {
            throw new RuntimeException("Conversation not found.", 404);
        }

        if ($conv['participant1_id'] !== $authenticatedUserId && $conv['participant2_id'] !== $authenticatedUserId) {
            throw new RuntimeException("You do not have access to this conversation.", 403);
        }

        $isP1 = ($conv['participant1_id'] === $authenticatedUserId);
        $conv['partner_id'] = $isP1 ? $conv['participant2_id'] : $conv['participant1_id'];
        $conv['partner_name'] = $isP1 ? $conv['p2_name'] : $conv['p1_name'];
        $conv['partner_avatar'] = $isP1 ? $conv['p2_avatar'] : $conv['p1_avatar'];

        return $conv;
    }

    public function getUserConversations(string $userId): array {
        $stmt = $this->pdo->prepare(
            "SELECT c.id, c.request_id, c.created_at, c.updated_at,
                    c.participant1_id, c.participant2_id,
                    u1.full_name AS p1_name, u1.avatar_url AS p1_avatar,
                    u2.full_name AS p2_name, u2.avatar_url AS p2_avatar
             FROM chat_conversations c
             JOIN users u1 ON u1.id = c.participant1_id
             JOIN users u2 ON u2.id = c.participant2_id
             WHERE c.participant1_id = :uid OR c.participant2_id = :uid
             ORDER BY c.updated_at DESC"
        );
        $stmt->execute([':uid' => $userId]);
        $rows = $stmt->fetchAll();

        $result = [];
        foreach ($rows as $r) {
            $isP1 = ($r['participant1_id'] === $userId);
            $partnerId = $isP1 ? $r['participant2_id'] : $r['participant1_id'];
            $partnerName = $isP1 ? $r['p2_name'] : $r['p1_name'];
            $partnerAvatar = $isP1 ? $r['p2_avatar'] : $r['p1_avatar'];

            $lastMsgStmt = $this->pdo->prepare(
                "SELECT message_text, created_at FROM chat_messages
                 WHERE conversation_id = :cid
                 ORDER BY created_at DESC, id DESC LIMIT 1"
            );
            $lastMsgStmt->execute([':cid' => $r['id']]);
            $lastMsg = $lastMsgStmt->fetch();

            $result[] = [
                'id' => (string)$r['id'],
                'request_id' => $r['request_id'],
                'partner_id' => (string)$partnerId,
                'partner_name' => (string)$partnerName,
                'partner_avatar' => (string)$partnerAvatar,
                'last_message' => $lastMsg ? (string)$lastMsg['message_text'] : '',
                'last_message_time' => (isset($lastMsg['created_at']) && $lastMsg['created_at']) ? gmdate('g:i A', strtotime($lastMsg['created_at'])) : '',
            ];
        }

        return $result;
    }

    public function getMessages(
        string $userId,
        ?string $conversationId = null,
        ?string $recipientId = null,
        ?string $sinceId = null,
        int $limit = 50
    ): array {
        if (empty($conversationId) && empty($recipientId)) {
            throw new RuntimeException("A valid conversation_id or recipient_id is required to view messages.", 400);
        }

        if (!empty($conversationId)) {
            // Strictly verifies existence and participant authorization
            $this->getConversation($conversationId, $userId);
        } else {
            // Resolve 1-on-1 conversation with recipient
            $conv = $this->getOrCreateConversation($userId, $recipientId);
            $conversationId = $conv['id'];
        }

        $params = [':conv_id' => $conversationId];
        $where = ["m.conversation_id = :conv_id"];

        if (!empty($sinceId)) {
            $where[] = "m.id > :since_id";
            $params[':since_id'] = $sinceId;
        }

        $whereSql = implode(' AND ', $where);

        $sql = "SELECT m.id, m.conversation_id, m.sender_id, m.client_message_id,
                       u.full_name AS sender_name,
                       m.message_text, m.attachment_title, m.attachment_code,
                       m.created_at
                FROM chat_messages m
                JOIN users u ON u.id = m.sender_id
                WHERE {$whereSql}
                ORDER BY m.created_at ASC, m.id ASC
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
            $names = explode(' ', trim((string)$r['sender_name']));
            $initials = '';
            foreach ($names as $n) {
                if (!empty($n)) $initials .= mb_substr($n, 0, 1);
            }
            if ($isUser) $initials = 'ME';

            $formatted[] = [
                'id' => (string)$r['id'],
                'conversation_id' => (string)$r['conversation_id'],
                'sender_id' => (string)$r['sender_id'],
                'client_message_id' => $r['client_message_id'] ?? null,
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

    public function sendMessage(
        string $senderId,
        string $messageText,
        ?string $conversationId = null,
        ?string $recipientId = null,
        ?string $clientMessageId = null,
        ?string $attachmentCode = null,
        ?string $attachmentTitle = null
    ): array {
        $trimmed = trim($messageText);
        if ($trimmed === '') {
            throw new RuntimeException("Message text cannot be empty.", 422);
        }

        if (empty($conversationId) && empty($recipientId)) {
            throw new RuntimeException("A valid conversation_id or recipient_id is required to send a message.", 422);
        }

        if (!empty($conversationId)) {
            $conv = $this->getConversation($conversationId, $senderId);
            $resolvedRecipientId = ($conv['participant1_id'] === $senderId) ? $conv['participant2_id'] : $conv['participant1_id'];
            if (!empty($recipientId) && $recipientId !== $resolvedRecipientId) {
                throw new RuntimeException("Recipient does not match conversation participants.", 400);
            }
            $recipientId = $resolvedRecipientId;
        } else {
            // Recipient provided, no conversation ID yet
            if ($recipientId === $senderId) {
                throw new RuntimeException("Cannot send a message to yourself.", 400);
            }
            $conv = $this->getOrCreateConversation($senderId, $recipientId);
            $conversationId = $conv['id'];
        }

        // --- IDEMPOTENCY & DUPLICATE CHECK ---
        // 1. Check by client_message_id if provided
        if (!empty($clientMessageId)) {
            $checkClient = $this->pdo->prepare(
                "SELECT m.*, u.full_name AS sender_name
                 FROM chat_messages m
                 JOIN users u ON u.id = m.sender_id
                 WHERE m.conversation_id = :cid AND m.client_message_id = :cmid
                 LIMIT 1"
            );
            $checkClient->execute([':cid' => $conversationId, ':cmid' => $clientMessageId]);
            $existing = $checkClient->fetch();
            if ($existing) {
                return $this->formatMessageRow($existing, $senderId);
            }
        }

        // 2. Double-submit guard: exact identical message from same sender in same conversation within 2 seconds
        $cutoff = gmdate('Y-m-d H:i:s', time() - 2);
        $checkDup = $this->pdo->prepare(
            "SELECT m.*, u.full_name AS sender_name
             FROM chat_messages m
             JOIN users u ON u.id = m.sender_id
             WHERE m.conversation_id = :cid AND m.sender_id = :sid AND m.message_text = :text AND m.created_at >= :cutoff
             ORDER BY m.created_at DESC LIMIT 1"
        );
        $checkDup->execute([
            ':cid' => $conversationId,
            ':sid' => $senderId,
            ':text' => $trimmed,
            ':cutoff' => $cutoff
        ]);
        $recentDup = $checkDup->fetch();
        if ($recentDup) {
            return $this->formatMessageRow($recentDup, $senderId);
        }

        // Insert new message
        $msgId = 'msg_' . bin2hex(random_bytes(10));
        $createdAt = gmdate('Y-m-d H:i:s');

        $stmt = $this->pdo->prepare(
            "INSERT INTO chat_messages (id, conversation_id, sender_id, client_message_id, message_text, attachment_code, attachment_title, created_at)
             VALUES (:id, :conv_id, :sender_id, :client_id, :text, :att_code, :att_title, :created_at)"
        );
        $stmt->execute([
            ':id' => $msgId,
            ':conv_id' => $conversationId,
            ':sender_id' => $senderId,
            ':client_id' => $clientMessageId,
            ':text' => $trimmed,
            ':att_code' => $attachmentCode,
            ':att_title' => $attachmentTitle,
            ':created_at' => $createdAt,
        ]);

        // Update conversation timestamp
        try {
            $upd = $this->pdo->prepare("UPDATE chat_conversations SET updated_at = :created WHERE id = :id");
            $upd->execute([':created' => $createdAt, ':id' => $conversationId]);
        } catch (Throwable $_) {}

        // Notification: created ONLY for the intended recipient
        try {
            $notifRepo = new NotificationRepository($this->pdo);
            $senderNameStmt = $this->pdo->prepare("SELECT full_name FROM users WHERE id = :id LIMIT 1");
            $senderNameStmt->execute([':id' => $senderId]);
            $senderName = $senderNameStmt->fetchColumn() ?: 'Neighbor';

            $notifRepo->createNotification(
                $recipientId,
                "New message from {$senderName}",
                mb_substr($trimmed, 0, 100),
                'NEW_MESSAGE',
                $conv['request_id'] ?? null
            );
        } catch (Throwable $_) {}

        return [
            'id' => $msgId,
            'conversation_id' => $conversationId,
            'sender_id' => $senderId,
            'client_message_id' => $clientMessageId,
            'sender_name' => 'You',
            'sender_initials' => 'ME',
            'message_text' => $trimmed,
            'timestamp' => 'Just now',
            'is_user' => true,
            'attachment_code' => $attachmentCode,
            'attachment_title' => $attachmentTitle,
        ];
    }

    private function formatMessageRow(array $r, string $currentUserId): array {
        $isUser = ($r['sender_id'] === $currentUserId);
        $names = explode(' ', trim((string)($r['sender_name'] ?? '')));
        $initials = '';
        foreach ($names as $n) {
            if (!empty($n)) $initials .= mb_substr($n, 0, 1);
        }
        if ($isUser) $initials = 'ME';

        return [
            'id' => (string)$r['id'],
            'conversation_id' => (string)$r['conversation_id'],
            'sender_id' => (string)$r['sender_id'],
            'client_message_id' => $r['client_message_id'] ?? null,
            'sender_name' => $isUser ? 'You' : (string)($r['sender_name'] ?? 'Neighbor'),
            'sender_initials' => $initials ?: 'NH',
            'message_text' => (string)$r['message_text'],
            'timestamp' => gmdate('g:i A', strtotime($r['created_at'])),
            'is_user' => $isUser,
            'attachment_code' => $r['attachment_code'] ?? null,
            'attachment_title' => $r['attachment_title'] ?? null,
        ];
    }
}
