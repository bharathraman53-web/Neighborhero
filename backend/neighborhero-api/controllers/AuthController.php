<?php
declare(strict_types=1);

namespace NeighborHero\Controllers;

use NeighborHero\Middleware\AuthMiddleware;
use NeighborHero\Repositories\UserRepository;
use NeighborHero\Services\AuthService;
use NeighborHero\Services\Database;
use NeighborHero\Services\Response;
use NeighborHero\Validators\Validator;

class AuthController {
    private UserRepository $userRepository;
    private AuthService $authService;

    public function __construct() {
        $pdo = Database::getConnection();
        $this->userRepository = new UserRepository($pdo);
        $this->authService = new AuthService($pdo);
    }

    public function register(): void {
        $input = json_decode(file_get_contents('php://input'), true) ?: $_POST;

        $validator = Validator::make($input)
            ->required('full_name')
            ->minLength('full_name', 2)
            ->required('email')
            ->email('email')
            ->required('password')
            ->minLength('password', 6);

        if (!$validator->passes()) {
            Response::error($validator->firstError() ?: 'Validation failed', 422, $validator->errors());
        }

        $email = strtolower(trim($input['email']));
        if ($this->userRepository->findByEmail($email)) {
            Response::error('An account with this email address already exists.', 409);
        }

        if (!empty($input['phone'])) {
            $phone = trim($input['phone']);
            if ($this->userRepository->findByPhone($phone)) {
                Response::error('An account with this phone number already exists.', 409);
            }
        }

        $passwordHash = $this->authService->hashPassword((string)$input['password']);

        $user = $this->userRepository->create([
            'full_name' => trim($input['full_name']),
            'email' => $email,
            'phone' => !empty($input['phone']) ? trim($input['phone']) : null,
            'password_hash' => $passwordHash,
            'neighborhood' => $input['neighborhood'] ?? 'Maplewood District',
            'bio' => $input['bio'] ?? '',
            'avatar_url' => $input['avatar_url'] ?? '',
            'skills' => $input['skills'] ?? ['Groceries', 'Repairs'],
        ]);

        $token = $this->authService->createToken($user['id']);

        Response::success([
            'token' => $token,
            'user' => $user,
        ], 'Registration successful', 201);
    }

    public function login(): void {
        $input = json_decode(file_get_contents('php://input'), true) ?: $_POST;

        // Allow either 'email_or_phone' or 'email'
        if (empty($input['email_or_phone']) && !empty($input['email'])) {
            $input['email_or_phone'] = $input['email'];
        }

        $validator = Validator::make($input)
            ->required('email_or_phone', 'Email or Phone')
            ->required('password', 'Password');

        if (!$validator->passes()) {
            Response::error($validator->firstError() ?: 'Validation failed', 422, $validator->errors());
        }

        $identifier = trim((string)$input['email_or_phone']);
        $password = (string)$input['password'];

        $user = str_contains($identifier, '@')
            ? $this->userRepository->findByEmail($identifier)
            : $this->userRepository->findByPhone($identifier);

        if (!$user || !$this->authService->verifyPassword($password, $user['password_hash'])) {
            // Constant-time safe error message to prevent enumeration
            Response::error('Invalid email/phone or password.', 401);
        }

        $token = $this->authService->createToken($user['id']);
        $profile = $this->userRepository->findById($user['id']);

        Response::success([
            'token' => $token,
            'user' => $profile,
        ], 'Login successful');
    }

    public function logout(): void {
        $headers = getallheaders();
        $authHeader = $headers['Authorization'] ?? $headers['authorization'] ?? '';

        if (preg_match('/Bearer\s+(\S+)/i', $authHeader, $matches)) {
            $this->authService->revokeToken($matches[1]);
        }

        Response::success(null, 'Logged out successfully');
    }

    public function me(): void {
        $currentUser = AuthMiddleware::authenticate(true);
        $fullUser = $this->userRepository->findById($currentUser['id']);
        Response::success($fullUser);
    }

    public function updateProfile(): void {
        $currentUser = AuthMiddleware::authenticate(true);
        $input = json_decode(file_get_contents('php://input'), true) ?: $_POST;

        $this->userRepository->updateProfile($currentUser['id'], $input);
        $updated = $this->userRepository->findById($currentUser['id']);

        Response::success($updated, 'Profile updated successfully');
    }
}

