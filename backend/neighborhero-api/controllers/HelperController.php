<?php
declare(strict_types=1);

namespace NeighborHero\Controllers;

use NeighborHero\Middleware\AuthMiddleware;
use NeighborHero\Repositories\HelperRepository;
use NeighborHero\Repositories\UserRepository;
use NeighborHero\Services\Database;
use NeighborHero\Services\Response;

class HelperController {
    private HelperRepository $helperRepository;
    private UserRepository $userRepository;

    public function __construct() {
        $pdo = Database::getConnection();
        $this->helperRepository = new HelperRepository($pdo);
        $this->userRepository = new UserRepository($pdo);
    }

    public function index(): void {
        $skill = $_GET['skill'] ?? $_GET['category'] ?? null;
        $location = $_GET['location'] ?? null;
        $requestedTime = $_GET['requested_time'] ?? null;
        $limit = isset($_GET['limit']) ? (int)$_GET['limit'] : 20;

        $user = AuthMiddleware::authenticate(false);
        $requesterId = $user ? $user['id'] : null;

        $helpers = $this->helperRepository->getRecommendations($skill, $limit, $location, $requestedTime, $requesterId);
        Response::success($helpers);
    }

    public function recommendations(): void {
        $requestId = $_GET['request_id'] ?? null;
        $limit = isset($_GET['limit']) ? (int)$_GET['limit'] : 10;

        if (!empty($requestId)) {
            $helpers = $this->helperRepository->getRecommendationsForRequest($requestId, [], $limit);
            Response::success($helpers);
            return;
        }

        $skill = $_GET['category'] ?? $_GET['skill'] ?? 'Other';
        $location = $_GET['location'] ?? 'Maplewood';
        $requestedTime = $_GET['requested_time'] ?? 'Today';

        $user = AuthMiddleware::authenticate(false);
        $requesterId = $user ? $user['id'] : null;

        $helpers = $this->helperRepository->getRecommendations($skill, $limit, $location, $requestedTime, $requesterId);
        Response::success($helpers);
    }

    public function recommendationsForRequest(string $requestId): void {
        $limit = isset($_GET['limit']) ? (int)$_GET['limit'] : 10;
        $helpers = $this->helperRepository->getRecommendationsForRequest($requestId, [], $limit);
        Response::success($helpers);
    }

    public function show(string $id): void {
        $helper = $this->helperRepository->findById($id);
        if (!$helper) {
            Response::notFound("Helper '{$id}' not found.");
        }
        Response::success($helper);
    }

    public function leaderboard(): void {
        $limit = isset($_GET['limit']) ? (int)$_GET['limit'] : 50;
        $champions = $this->userRepository->getLeaderboard($limit);
        Response::success($champions);
    }
}
