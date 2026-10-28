<?php
declare(strict_types=1);

return [
    'name' => 'NeighborHero API',
    'env' => getenv('APP_ENV') ?: 'development',
    'debug' => (getenv('APP_DEBUG') ?: 'true') === 'true',
    'timezone' => 'UTC',
    'token_ttl_seconds' => 30 * 24 * 3600, // 30 days
    'rate_limit_per_minute' => 120,
    'cors' => [
        'allowed_origins' => ['*'],
        'allowed_methods' => ['GET', 'POST', 'PUT', 'PATCH', 'DELETE', 'OPTIONS'],
        'allowed_headers' => ['Content-Type', 'Authorization', 'X-Requested-With', 'Accept'],
    ],
];

