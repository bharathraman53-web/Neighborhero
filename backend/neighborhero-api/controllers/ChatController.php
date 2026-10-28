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

    public function getMessages(): void {
        $user = AuthMiddleware::authenticate(true);
        $convId = $_GET['conversation_id'] ?? null;
        $sinceId = $_GET['since_id'] ?? null;
        $limit = isset($_GET['limit']) ? (int)$_GET['limit'] : 50;

        $messages = $this->chatRepository->getMessages($user['id'], $convId, $sinceId, $limit);
        Response::success($messages);
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
                $input['conversation_id'] ?? null,
                $input['recipient_id'] ?? null,
                $input['attachment_code'] ?? null,
                $input['attachment_title'] ?? null
            );
            Response::success($msg, 'Message sent', 201);
        } catch (Throwable $e) {
            Response::error($e->getMessage(), $e->getCode() >= 400 && $e->getCode() <= 499 ? (int)$e->getCode() : 400);
        }
    }
}

