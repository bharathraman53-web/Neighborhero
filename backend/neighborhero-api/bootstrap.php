<?php
declare(strict_types=1);

// Set default timezone to UTC
date_default_timezone_set('UTC');

// PSR-4 Autoloader for NeighborHero namespace
spl_autoload_register(function (string $class): void {
    $prefix = 'NeighborHero\\';
    $baseDir = __DIR__ . '/';

    $len = strlen($prefix);
    if (strncmp($prefix, $class, $len) !== 0) {
        return;
    }

    $relativeClass = substr($class, $len);
    $file = $baseDir . str_replace('\\', '/', $relativeClass) . '.php';

    if (file_exists($file)) {
        require_once $file;
    }
});

