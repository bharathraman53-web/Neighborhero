<?php
declare(strict_types=1);

namespace NeighborHero\Controllers;

use NeighborHero\Middleware\AuthMiddleware;
use NeighborHero\Repositories\SafetyRepository;
use NeighborHero\Services\Database;
use NeighborHero\Services\Response;

class SafetyController {
    private SafetyRepository $repository;

    public function __construct() {
        $pdo = Database::getConnection();
        $this->repository = new SafetyRepository($pdo);
    }

    public function triggerSos(): void {
        $user = AuthMiddleware::authenticate(true);
        $input = json_decode(file_get_contents('php://input'), true) ?: $_POST;

        $lat = isset($input['latitude']) ? (float)$input['latitude'] : null;
        $lng = isset($input['longitude']) ? (float)$input['longitude'] : null;
        $address = !empty($input['address']) ? trim((string)$input['address']) : 'Maplewood District';
        $alertType = !empty($input['alert_type']) ? trim((string)$input['alert_type']) : 'EMERGENCY_SOS';

        $alert = $this->repository->triggerSos($user['id'], $lat, $lng, $address, $alertType);
        Response::success($alert, 'Emergency SOS signal dispatched to trusted circle and emergency responders', 201);
    }

    public function resolveSos(string $id): void {
        $user = AuthMiddleware::authenticate(true);
        $this->repository->resolveSos($id, $user['id']);
        Response::success(['id' => $id, 'status' => 'RESOLVED'], 'SOS signal resolved');
    }

    public function taskHistory(): void {
        $user = AuthMiddleware::authenticate(true);
        $history = $this->repository->getTaskHistory($user['id']);
        Response::success($history);
    }
}

