<?php
declare(strict_types=1);

namespace NeighborHero\Controllers;

use NeighborHero\Middleware\AuthMiddleware;
use NeighborHero\Repositories\ChatRepository;
use NeighborHero\Services\Database;
use NeighborHero\Services\Response;
use NeighborHero\Validators\Validator;
use Throwable;

class ChatController {
    private ChatRepository $chatRepository;

    public function __construct() {
        $pdo = Database::getConnection();
        $this->chatRepository = new ChatRepository($pdo);
    }

    public function getConversations(): void {
        $user = AuthMiddleware::authenticate(true);
        $convs = $this->chatRepository->getUserConversations($user['id']);
        Response::success($convs);
    }

    public function getMessages(): void {
        $user = AuthMiddleware::authenticate(true);
        $convId = isset($_GET['conversation_id']) && trim((string)$_GET['conversation_id']) !== ''
            ? trim((string)$_GET['conversation_id'])
            : null;
        $recipientId = isset($_GET['recipient_id']) && trim((string)$_GET['recipient_id']) !== ''
            ? trim((string)$_GET['recipient_id'])
            : null;
        $sinceId = isset($_GET['since_id']) && trim((string)$_GET['since_id']) !== ''
            ? trim((string)$_GET['since_id'])
            : null;
        $limit = isset($_GET['limit']) ? (int)$_GET['limit'] : 50;

        try {
            $messages = $this->chatRepository->getMessages($user['id'], $convId, $recipientId, $sinceId, $limit);
            Response::success($messages);
        } catch (Throwable $e) {
            $code = ($e->getCode() >= 400 && $e->getCode() <= 499) ? (int)$e->getCode() : 400;
            Response::error($e->getMessage(), $code);
        }
    }

    public function sendMessage(): void {
        $user = AuthMiddleware::authenticate(true);
        $input = json_decode(file_get_contents('php://input'), true) ?: $_POST;

        $validator = Validator::make($input)
            ->required('message_text', 'Message');

        if (!$validator->passes()) {
            Response::error($validator->firstError() ?: 'Validation failed', 422, $validator->errors());
        }

        try {
            $msg = $this->chatRepository->sendMessage(
                $user['id'],
                (string)$input['message_text'],
                !empty($input['conversation_id']) ? (string)$input['conversation_id'] : null,
                !empty($input['recipient_id']) ? (string)$input['recipient_id'] : null,
                !empty($input['client_message_id']) ? (string)$input['client_message_id'] : null,
                !empty($input['attachment_code']) ? (string)$input['attachment_code'] : null,
                !empty($input['attachment_title']) ? (string)$input['attachment_title'] : null
            );
            Response::success($msg, 'Message sent', 201);
        } catch (Throwable $e) {
            $code = ($e->getCode() >= 400 && $e->getCode() <= 499) ? (int)$e->getCode() : 400;
            Response::error($e->getMessage(), $code);
        }
    }
}
