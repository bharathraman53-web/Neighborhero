<?php
declare(strict_types=1);

use NeighborHero\Controllers\AuthController;
use NeighborHero\Controllers\ChatController;
use NeighborHero\Controllers\HealthController;
use NeighborHero\Controllers\HelperController;
use NeighborHero\Controllers\HelpRequestController;
use NeighborHero\Controllers\NotificationController;
use NeighborHero\Controllers\SafetyController;
use NeighborHero\Controllers\TaskController;
use NeighborHero\Routes\Router;

return function(Router $router): void {
    // Health & Status
    $router->get('/api/health', [HealthController::class, 'check']);
    $router->get('/', [HealthController::class, 'check']);

    // Authentication
    $router->post('/api/auth/register', [AuthController::class, 'register']);
    $router->post('/api/auth/login', [AuthController::class, 'login']);
    $router->post('/api/auth/logout', [AuthController::class, 'logout']);
    $router->get('/api/auth/me', [AuthController::class, 'me']);
    $router->put('/api/auth/profile', [AuthController::class, 'updateProfile']);
    $router->post('/api/auth/profile/photo', [AuthController::class, 'uploadPhoto']);

    // Help Requests
    $router->get('/api/requests', [HelpRequestController::class, 'index']);
    $router->get('/api/requests/my/posted', [HelpRequestController::class, 'myPosted']);
    $router->get('/api/requests/my/accepted', [HelpRequestController::class, 'myAccepted']);
    $router->get('/api/requests/{id}', [HelpRequestController::class, 'show']);
    $router->post('/api/requests', [HelpRequestController::class, 'store']);
    $router->post('/api/requests/{id}/cancel', [HelpRequestController::class, 'cancel']);

    // Helper Discovery & Leaderboard
    $router->get('/api/helpers/leaderboard', [HelperController::class, 'leaderboard']);
    $router->get('/api/helpers', [HelperController::class, 'index']);
    $router->get('/api/helpers/{id}', [HelperController::class, 'show']);

    // Task Lifecycle
    $router->post('/api/tasks/{id}/assign', [TaskController::class, 'assign']);
    $router->post('/api/tasks/{id}/confirm-start', [TaskController::class, 'confirmStart']);
    $router->post('/api/tasks/{id}/evidence/photo', [TaskController::class, 'uploadEvidencePhoto']);
    $router->get('/api/tasks/{id}/evidence', [TaskController::class, 'getEvidence']);
    $router->get('/api/tasks/{id}/evidence/photo', [TaskController::class, 'streamEvidencePhoto']);
    $router->post('/api/tasks/{id}/submit-completion', [TaskController::class, 'submitCompletion']);
    $router->post('/api/tasks/{id}/verify-completion', [TaskController::class, 'verifyCompletion']);

    // Chat
    $router->get('/api/chat/conversations', [ChatController::class, 'getConversations']);
    $router->get('/api/chat/messages', [ChatController::class, 'getMessages']);
    $router->post('/api/chat/messages', [ChatController::class, 'sendMessage']);

    // Notifications
    $router->get('/api/notifications', [NotificationController::class, 'index']);
    $router->post('/api/notifications/read-all', [NotificationController::class, 'markAllRead']);
    $router->post('/api/notifications/{id}/read', [NotificationController::class, 'markAsRead']);

    // Safety & SOS
    $router->post('/api/safety/sos', [SafetyController::class, 'triggerSos']);
    $router->post('/api/safety/sos/{id}/resolve', [SafetyController::class, 'resolveSos']);
    $router->get('/api/safety/task-history', [SafetyController::class, 'taskHistory']);
};

