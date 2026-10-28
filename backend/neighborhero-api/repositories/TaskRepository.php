<?php
declare(strict_types=1);

namespace NeighborHero\Repositories;

use PDO;
use RuntimeException;

class TaskRepository {
    private PDO $pdo;
    private UserRepository $userRepository;
    private HelpRequestRepository $requestRepository;

    public function __construct(PDO $pdo) {
        $this->pdo = $pdo;
        $this->userRepository = new UserRepository($pdo);
        $this->requestRepository = new HelpRequestRepository($pdo);
    }

    public function assignHelper(string $requestId, string $helperId, string $actorUserId): array {
        $req = $this->requestRepository->findById($requestId);
        if (!$req) {
            throw new RuntimeException("Request not found", 404);
        }

        // Requester or helper themselves can accept/assign depending on flow
        if ($req['status'] !== 'OPEN') {
            throw new RuntimeException("Request is not open for assignment (Current status: {$req['status']})", 400);
        }

        $this->pdo->beginTransaction();
        try {
            // Assign helper and transition status to IN_PROGRESS
            $this->requestRepository->updateStatus($requestId, 'IN_PROGRESS', $helperId, $actorUserId);

            // Create assignment record
            $assignStmt = $this->pdo->prepare(
                "INSERT INTO request_assignments (id, request_id, helper_id, status, assigned_at)
                 VALUES (:id, :req_id, :helper_id, 'IN_PROGRESS', :assigned_at)"
            );
            $assignStmt->execute([
                ':id' => 'asg_' . bin2hex(random_bytes(10)),
                ':req_id' => $requestId,
                ':helper_id' => $helperId,
                ':assigned_at' => gmdate('Y-m-d H:i:s'),
            ]);

            // Notify requester
            $nStmt = $this->pdo->prepare(
                "INSERT INTO notifications (id, user_id, title, message, type, related_request_id)
                 VALUES (:id, :user_id, :title, :message, 'REQUEST_ACCEPTED', :req_id)"
            );
            $helper = $this->userRepository->findById($helperId);
            $helperName = $helper['full_name'] ?? 'A neighbor';
            $nStmt->execute([
                ':id' => 'notif_' . bin2hex(random_bytes(10)),
                ':user_id' => $req['requester_id'],
                ':title' => 'Offer Accepted!',
                ':message' => "{$helperName} accepted your help offer for \"{$req['title']}\".",
                ':req_id' => $requestId,
            ]);

            $this->pdo->commit();
            return $this->requestRepository->findById($requestId);
        } catch (\Exception $e) {
            $this->pdo->rollBack();
            throw $e;
        }
    }

    public function confirmStart(string $requestId, string $actorUserId): array {
        $req = $this->requestRepository->findById($requestId);
        if (!$req) {
            throw new RuntimeException("Request not found", 404);
        }

        if ($req['status'] !== 'IN_PROGRESS') {
            // If already open, transition to in progress
            $this->requestRepository->updateStatus($requestId, 'IN_PROGRESS', $actorUserId, $actorUserId);
        }

        // Update started_at on assignment
        $stmt = $this->pdo->prepare(
            "UPDATE request_assignments SET started_at = :now WHERE request_id = :req_id"
        );
        $stmt->execute([
            ':now' => gmdate('Y-m-d H:i:s'),
            ':req_id' => $requestId,
        ]);

        return $this->requestRepository->findById($requestId);
    }

    public function submitCompletion(string $requestId, string $helperId, string $notes, ?string $photoUrl = null): array {
        $req = $this->requestRepository->findById($requestId);
        if (!$req) {
            throw new RuntimeException("Request not found", 404);
        }

        // Authorization check: only assigned helper can submit completion
        if (!empty($req['assigned_helper_id']) && $req['assigned_helper_id'] !== $helperId) {
            throw new RuntimeException("Only the assigned helper can submit task completion", 403);
        }

        if ($req['status'] !== 'IN_PROGRESS') {
            throw new RuntimeException("Cannot submit completion for a task not currently in progress", 400);
        }

        $this->pdo->beginTransaction();
        try {
            // Save evidence
            $evidenceStmt = $this->pdo->prepare(
                "INSERT INTO task_completion_evidence (id, request_id, helper_id, notes, proof_photo_url)
                 VALUES (:id, :req_id, :helper_id, :notes, :photo)"
            );
            $evidenceStmt->execute([
                ':id' => 'ev_' . bin2hex(random_bytes(10)),
                ':req_id' => $requestId,
                ':helper_id' => $helperId,
                ':notes' => $notes,
                ':photo' => $photoUrl,
            ]);

            // Transition status to PENDING_VERIFICATION
            $this->requestRepository->updateStatus($requestId, 'PENDING_VERIFICATION', $helperId, $helperId);

            // Notify requester that completion was submitted
            $nStmt = $this->pdo->prepare(
                "INSERT INTO notifications (id, user_id, title, message, type, related_request_id)
                 VALUES (:id, :user_id, :title, :message, 'TASK_UPDATE', :req_id)"
            );
            $nStmt->execute([
                ':id' => 'notif_' . bin2hex(random_bytes(10)),
                ':user_id' => $req['requester_id'],
                ':title' => 'Task Completion Submitted',
                ':message' => "Your helper has finished \"{$req['title']}\". Please verify and rate the completion.",
                ':req_id' => $requestId,
            ]);

            $this->pdo->commit();
            return $this->requestRepository->findById($requestId);
        } catch (\Exception $e) {
            $this->pdo->rollBack();
            throw $e;
        }
    }

    public function verifyCompletion(string $requestId, string $requesterId, int $rating = 5, string $reviewText = '', string $tags = ''): array {
        $req = $this->requestRepository->findById($requestId);
        if (!$req) {
            throw new RuntimeException("Request not found", 404);
        }

        // Authorization check: only the requester can verify completion
        if ($req['requester_id'] !== $requesterId) {
            throw new RuntimeException("Only the requester who posted the task can verify completion", 403);
        }

        if ($req['status'] === 'COMPLETED') {
            throw new RuntimeException("This task has already been completed and verified", 400);
        }

        $this->pdo->beginTransaction();
        try {
            // Transition status to COMPLETED
            $this->requestRepository->updateStatus($requestId, 'COMPLETED', null, $requesterId);

            $helperId = $req['assigned_helper_id'] ?? $requesterId;

            // Prevent duplicate ratings: check if exists
            $existStmt = $this->pdo->prepare("SELECT id FROM ratings WHERE request_id = :req_id AND reviewer_id = :rev_id");
            $existStmt->execute([':req_id' => $requestId, ':rev_id' => $requesterId]);
            $existingRatingId = $existStmt->fetchColumn();

            if ($existingRatingId) {
                $ratingStmt = $this->pdo->prepare(
                    "UPDATE ratings SET rating = :rating, review_text = :review_text, tags = :tags WHERE id = :id"
                );
                $ratingStmt->execute([
                    ':id' => $existingRatingId,
                    ':rating' => max(1, min(5, $rating)),
                    ':review_text' => $reviewText,
                    ':tags' => $tags,
                ]);
            } else {
                $ratingStmt = $this->pdo->prepare(
                    "INSERT INTO ratings (id, request_id, reviewer_id, helper_id, rating, review_text, tags)
                     VALUES (:id, :req_id, :reviewer_id, :helper_id, :rating, :review_text, :tags)"
                );
                $ratingStmt->execute([
                    ':id' => 'rat_' . bin2hex(random_bytes(10)),
                    ':req_id' => $requestId,
                    ':reviewer_id' => $requesterId,
                    ':helper_id' => $helperId,
                    ':rating' => max(1, min(5, $rating)),
                    ':review_text' => $reviewText,
                    ':tags' => $tags,
                ]);
            }

            // Recalculate helper average rating
            $avgStmt = $this->pdo->prepare("SELECT AVG(rating) FROM ratings WHERE helper_id = :h_id");
            $avgStmt->execute([':h_id' => $helperId]);
            $newAvg = (float)$avgStmt->fetchColumn();
            if ($newAvg > 0) {
                $uStmt = $this->pdo->prepare("UPDATE users SET rating = :rating WHERE id = :id");
                $uStmt->execute([':rating' => round($newAvg, 2), ':id' => $helperId]);
            }

            // Award points (150 community points) exactly once via points ledger
            $this->userRepository->addPoints($helperId, 150, $requestId, 'Task completion award');

            // Notify helper
            $nStmt = $this->pdo->prepare(
                "INSERT INTO notifications (id, user_id, title, message, type, related_request_id)
                 VALUES (:id, :user_id, :title, :message, 'TASK_UPDATE', :req_id)"
            );
            $nStmt->execute([
                ':id' => 'notif_' . bin2hex(random_bytes(10)),
                ':user_id' => $helperId,
                ':title' => 'Completion Verified! +150 Points',
                ':message' => "The requester verified \"{$req['title']}\" and awarded you 150 community points!",
                ':req_id' => $requestId,
            ]);

            $this->pdo->commit();
            return $this->requestRepository->findById($requestId);
        } catch (\Exception $e) {
            $this->pdo->rollBack();
            throw $e;
        }
    }
}
