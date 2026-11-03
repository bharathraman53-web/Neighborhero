<?php
declare(strict_types=1);

namespace NeighborHero\Controllers;

use NeighborHero\Middleware\AuthMiddleware;
use NeighborHero\Repositories\HelpRequestRepository;
use NeighborHero\Services\Database;
use NeighborHero\Services\Response;
use NeighborHero\Validators\Validator;

class HelpRequestController {
    private HelpRequestRepository $repository;

    public function __construct() {
        $pdo = Database::getConnection();
        $this->repository = new HelpRequestRepository($pdo);
    }

    public function index(): void {
        $category = $_GET['category'] ?? null;
        $status = $_GET['status'] ?? null;
        $page = isset($_GET['page']) ? (int)$_GET['page'] : 1;
        $limit = isset($_GET['limit']) ? (int)$_GET['limit'] : 20;

        $requests = $this->repository->listAll($category, $status, $page, $limit);
        Response::success($requests);
    }

    public function show(string $id): void {
        $request = $this->repository->findById($id);
        if (!$request) {
            Response::notFound("Help request '{$id}' not found.");
        }
        Response::success($request);
    }

    public function store(): void {
        $user = AuthMiddleware::authenticate(true);
        $input = json_decode(file_get_contents('php://input'), true) ?: $_POST;

        $validator = Validator::make($input)
            ->required('title')
            ->minLength('title', 3)
            ->required('category')
            ->required('description')
            ->minLength('description', 5)
            ->required('location');

        if (!$validator->passes()) {
            Response::error($validator->firstError() ?: 'Validation failed', 422, $validator->errors());
        }

        $canonicalCategories = ['Groceries', 'Books', 'Repairs', 'Transport', 'Pet Help', 'Other'];
        $legacyCategories = ['Pet Care', 'Tutoring', 'Yardwork', 'Moving'];
        $allowedCategories = array_merge($canonicalCategories, $legacyCategories);
        $category = trim($input['category']);
        if (!in_array($category, $allowedCategories, true)) {
            $category = 'Other';
        }

        $assignedHelperId = !empty($input['assigned_helper_id']) ? trim((string)$input['assigned_helper_id']) : (!empty($input['target_helper_id']) ? trim((string)$input['target_helper_id']) : null);
        $latitude = isset($input['latitude']) && is_numeric($input['latitude']) ? (float)$input['latitude'] : null;
        $longitude = isset($input['longitude']) && is_numeric($input['longitude']) ? (float)$input['longitude'] : null;

        $created = $this->repository->create([
            'requester_id' => $user['id'],
            'title' => trim($input['title']),
            'category' => $category,
            'description' => trim($input['description']),
            'location' => trim($input['location']),
            'distance' => $input['distance'] ?? '0.4 mi away',
            'requested_time' => !empty($input['requested_time']) ? trim($input['requested_time']) : 'Due Today, 4:00 PM',
            'is_urgent' => !empty($input['is_urgent']),
            'assigned_helper_id' => $assignedHelperId,
            'latitude' => $latitude,
            'longitude' => $longitude,
        ]);

        if ($assignedHelperId) {
            $notifRepo = new \NeighborHero\Repositories\NotificationRepository(\NeighborHero\Services\Database::getConnection());
            $requesterName = $user['full_name'] ?? 'A neighbor';
            $notifRepo->create([
                'user_id' => $assignedHelperId,
                'title' => 'New Direct Help Request',
                'message' => "{$requesterName} requested your help for: {$created['title']}",
                'type' => 'REQUEST_ASSIGNED',
                'related_request_id' => $created['id'],
            ]);
        }

        Response::success($created, 'Help request created successfully', 201);
    }

    public function cancel(string $id): void {
        $user = AuthMiddleware::authenticate(true);
        $req = $this->repository->findById($id);

        if (!$req) {
            Response::notFound("Help request '{$id}' not found.");
        }

        // Server-enforced authorization: only the requester can cancel
        if ($req['requester_id'] !== $user['id']) {
            Response::forbidden("Only the original requester can cancel this request.");
        }

        if ($req['status'] === 'COMPLETED') {
            Response::error("Cannot cancel a completed request.", 400);
        }

        $this->repository->updateStatus($id, 'CANCELLED', null, $user['id']);
        $updated = $this->repository->findById($id);

        Response::success($updated, 'Request cancelled successfully');
    }

    public function myPosted(): void {
        $user = AuthMiddleware::authenticate(true);
        $requests = $this->repository->findByRequester($user['id']);
        Response::success($requests);
    }

    public function myAccepted(): void {
        $user = AuthMiddleware::authenticate(true);
        $requests = $this->repository->findByHelper($user['id']);
        Response::success($requests);
    }
}

