<?php
declare(strict_types=1);

namespace NeighborHero\Controllers;

use NeighborHero\Middleware\AuthMiddleware;
use NeighborHero\Repositories\TaskRepository;
use NeighborHero\Services\Database;
use NeighborHero\Services\Response;
use NeighborHero\Validators\Validator;
use Throwable;

class TaskController {
    private TaskRepository $taskRepository;

    public function __construct() {
        $pdo = Database::getConnection();
        $this->taskRepository = new TaskRepository($pdo);
    }

    public function assign(string $requestId): void {
        $user = AuthMiddleware::authenticate(true);
        $input = json_decode(file_get_contents('php://input'), true) ?: $_POST;

        // If helper_id not supplied, default to authenticated user offering help
        $helperId = !empty($input['helper_id']) ? (string)$input['helper_id'] : $user['id'];

        try {
            $updated = $this->taskRepository->assignHelper($requestId, $helperId, $user['id']);
            Response::success($updated, 'Helper assigned to request successfully');
        } catch (Throwable $e) {
            Response::error($e->getMessage(), $e->getCode() >= 400 && $e->getCode() <= 499 ? (int)$e->getCode() : 400);
        }
    }

    public function confirmStart(string $requestId): void {
        $user = AuthMiddleware::authenticate(true);
        try {
            $updated = $this->taskRepository->confirmStart($requestId, $user['id']);
            Response::success($updated, 'Task start confirmed');
        } catch (Throwable $e) {
            Response::error($e->getMessage(), $e->getCode() >= 400 && $e->getCode() <= 499 ? (int)$e->getCode() : 400);
        }
    }

    public function submitCompletion(string $requestId): void {
        $user = AuthMiddleware::authenticate(true);
        $input = json_decode(file_get_contents('php://input'), true) ?: $_POST;

        $validator = Validator::make($input)
            ->required('notes')
            ->minLength('notes', 5);

        if (!$validator->passes()) {
            Response::error($validator->firstError() ?: 'Validation failed', 422, $validator->errors());
        }

        $photoUrl = $input['proof_photo_url'] ?? null;

        try {
            $updated = $this->taskRepository->submitCompletion($requestId, $user['id'], trim($input['notes']), $photoUrl);
            Response::success($updated, 'Task completion submitted for verification');
        } catch (Throwable $e) {
            Response::error($e->getMessage(), $e->getCode() >= 400 && $e->getCode() <= 499 ? (int)$e->getCode() : 400);
        }
    }

    public function verifyCompletion(string $requestId): void {
        $user = AuthMiddleware::authenticate(true);
        $input = json_decode(file_get_contents('php://input'), true) ?: $_POST;

        $rating = isset($input['rating']) ? (int)$input['rating'] : 5;
        $feedback = isset($input['feedback']) ? (string)$input['feedback'] : '';
        $tags = isset($input['tags']) ? (string)$input['tags'] : '';

        try {
            $updated = $this->taskRepository->verifyCompletion($requestId, $user['id'], $rating, $feedback, $tags);
            Response::success($updated, 'Task verified and completed! Community points awarded.');
        } catch (Throwable $e) {
            Response::error($e->getMessage(), $e->getCode() >= 400 && $e->getCode() <= 499 ? (int)$e->getCode() : 400);
        }
    }
}

