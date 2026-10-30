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

    public function uploadEvidencePhoto(string $requestId): void {
        $user = AuthMiddleware::authenticate(true);

        if (empty($_FILES['photo']) || !isset($_FILES['photo']['error'])) {
            Response::error('No evidence photo file uploaded.', 400);
        }

        $file = $_FILES['photo'];
        if ($file['error'] !== UPLOAD_ERR_OK) {
            Response::error('File upload failed with error code: ' . $file['error'], 400);
        }

        $maxBytes = 5 * 1024 * 1024; // 5 MB
        if ($file['size'] > $maxBytes) {
            Response::error('File size exceeds the 5 MB limit.', 422);
        }

        $finfo = new \finfo(FILEINFO_MIME_TYPE);
        $mime = $finfo->file($file['tmp_name']);

        $allowedMimes = [
            'image/jpeg' => 'jpg',
            'image/png'  => 'png',
            'image/webp' => 'webp',
        ];

        if (!isset($allowedMimes[$mime])) {
            Response::error('Invalid file type. Supported formats: JPEG, PNG, WebP.', 422);
        }

        $imageInfo = @getimagesize($file['tmp_name']);
        if ($imageInfo === false) {
            Response::error('Invalid or corrupt image file.', 422);
        }

        $extension = $allowedMimes[$mime];
        $safeName = 'evidence_' . bin2hex(random_bytes(16)) . '.' . $extension;

        $targetDir = realpath(__DIR__ . '/../public/uploads/evidence');
        if (!$targetDir) {
            $targetDir = __DIR__ . '/../public/uploads/evidence';
            if (!is_dir($targetDir)) {
                mkdir($targetDir, 0755, true);
            }
        }

        $targetPath = $targetDir . DIRECTORY_SEPARATOR . $safeName;

        if (!move_uploaded_file($file['tmp_name'], $targetPath)) {
            Response::error('Failed to save uploaded image.', 500);
        }

        $photoUrl = 'uploads/evidence/' . $safeName;
        Response::success(['photo_url' => $photoUrl], 'Evidence photo uploaded successfully');
    }

    public function getEvidence(string $requestId): void {
        $user = AuthMiddleware::authenticate(true);
        try {
            $evidence = $this->taskRepository->getEvidence($requestId, $user['id']);
            Response::success($evidence);
        } catch (Throwable $e) {
            Response::error($e->getMessage(), $e->getCode() >= 400 && $e->getCode() <= 499 ? (int)$e->getCode() : 400);
        }
    }

    public function streamEvidencePhoto(string $requestId): void {
        $user = AuthMiddleware::authenticate(true);
        try {
            $evidence = $this->taskRepository->getEvidence($requestId, $user['id']);
            if (empty($evidence['proof_photo_url'])) {
                Response::error('No evidence photo submitted for this task.', 404);
            }

            $rawPath = $evidence['proof_photo_url'];
            $filename = basename($rawPath);
            $filePath = __DIR__ . '/../public/uploads/evidence/' . $filename;

            if (!file_exists($filePath)) {
                Response::error('Evidence photo file not found on server.', 404);
            }

            $finfo = new \finfo(FILEINFO_MIME_TYPE);
            $mime = $finfo->file($filePath) ?: 'image/jpeg';

            header('Content-Type: ' . $mime);
            header('Content-Length: ' . filesize($filePath));
            header('Cache-Control: private, max-age=86400');
            readfile($filePath);
            exit;
        } catch (Throwable $e) {
            Response::error($e->getMessage(), $e->getCode() >= 400 && $e->getCode() <= 499 ? (int)$e->getCode() : 403);
        }
    }
}

