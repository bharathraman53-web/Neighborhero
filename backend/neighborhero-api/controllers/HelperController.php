<?php
declare(strict_types=1);

namespace NeighborHero\Controllers;

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
        $skill = $_GET['skill'] ?? null;
        $limit = isset($_GET['limit']) ? (int)$_GET['limit'] : 20;

        $helpers = $this->helperRepository->getRecommendations($skill, $limit);
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

