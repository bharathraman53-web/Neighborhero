<?php
declare(strict_types=1);

namespace NeighborHero\Controllers;

use NeighborHero\Middleware\AuthMiddleware;
use NeighborHero\Repositories\NotificationRepository;
use NeighborHero\Services\Database;
use NeighborHero\Services\Response;

class NotificationController {
    private NotificationRepository $repository;

    public function __construct() {
        $pdo = Database::getConnection();
        $this->repository = new NotificationRepository($pdo);
    }

    public function index(): void {
        $user = AuthMiddleware::authenticate(true);
        $page = isset($_GET['page']) ? (int)$_GET['page'] : 1;
        $limit = isset($_GET['limit']) ? (int)$_GET['limit'] : 30;

        $notifications = $this->repository->getNotificationsForUser($user['id'], $page, $limit);
        Response::success($notifications);
    }

    public function markAsRead(string $id): void {
        $user = AuthMiddleware::authenticate(true);
        $this->repository->markAsRead($id, $user['id']);
        Response::success(['id' => $id, 'is_read' => true], 'Notification marked as read');
    }

    public function markAllRead(): void {
        $user = AuthMiddleware::authenticate(true);
        $this->repository->markAllAsRead($user['id']);
        Response::success(null, 'All notifications marked as read');
    }
}

