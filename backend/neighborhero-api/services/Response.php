<?php
declare(strict_types=1);

namespace NeighborHero\Services;

class Response {
    public static function json(mixed $data, int $status = 200, array $headers = []): void {
        http_response_code($status);
        header('Content-Type: application/json; charset=utf-8');
        foreach ($headers as $key => $val) {
            header("{$key}: {$val}");
        }

        echo json_encode($data, JSON_UNESCAPED_SLASHES | JSON_UNESCAPED_UNICODE);
        exit;
    }

    public static function success(mixed $data = null, string $message = 'Success', int $status = 200): void {
        self::json([
            'success' => true,
            'message' => $message,
            'data' => $data,
        ], $status);
    }

    public static function error(string $message, int $status = 400, array $errors = []): void {
        self::json([
            'success' => false,
            'message' => $message,
            'errors' => $errors,
        ], $status);
    }

    public static function unauthorized(string $message = 'Authentication required'): void {
        self::error($message, 401);
    }

    public static function forbidden(string $message = 'Action forbidden'): void {
        self::error($message, 403);
    }

    public static function notFound(string $message = 'Resource not found'): void {
        self::error($message, 404);
    }
}

