<?php
declare(strict_types=1);

require_once __DIR__ . '/../bootstrap.php';

use NeighborHero\Middleware\CorsMiddleware;
use NeighborHero\Routes\Router;
use NeighborHero\Services\Response;

// Convert PHP errors to ErrorExceptions
set_error_handler(function (int $errno, string $errstr, string $errfile, int $errline): bool {
    if (!(error_reporting() & $errno)) {
        return false;
    }
    throw new ErrorException($errstr, 0, $errno, $errfile, $errline);
});

// Centralized fatal exception handler
set_exception_handler(function (Throwable $e): void {
    $status = ($e->getCode() >= 400 && $e->getCode() <= 599) ? (int)$e->getCode() : 500;
    Response::error($e->getMessage(), $status);
});

// Load configuration
$appConfig = require __DIR__ . '/../config/app.php';

// Handle CORS & Preflight OPTIONS
CorsMiddleware::handle($appConfig['cors'] ?? []);

// Initialize Router and define API endpoints
$router = new Router();
$routesDefinition = require __DIR__ . '/../routes/api.php';
$routesDefinition($router);

// Dispatch incoming request
$method = $_SERVER['REQUEST_METHOD'] ?? 'GET';
$uri = $_SERVER['REQUEST_URI'] ?? '/';
$router->dispatch($method, $uri);

