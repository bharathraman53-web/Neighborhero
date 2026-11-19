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

        // Requester cannot accept or be assigned to their own request
        if ($req['requester_id'] === $helperId || $req['requester_id'] === $actorUserId) {
            throw new RuntimeException("Requesters cannot accept or be assigned to their own requests", 403);
        }

        if ($req['status'] !== 'OPEN') {
            if ($req['assigned_helper_id'] === $helperId) {
                throw new RuntimeException("You have already accepted this task.", 409);
            }
            if ($req['status'] === 'CANCELLED' || $req['status'] === 'COMPLETED') {
                throw new RuntimeException("This task is unavailable or has been {$req['status']}.", 400);
            }
            throw new RuntimeException("This task has already been accepted by another helper.", 409);
        }

        $this->pdo->beginTransaction();
        try {
            // Atomic conditional update ensuring concurrency protection (prevents race condition)
            $isSqlite = ($this->pdo->getAttribute(PDO::ATTR_DRIVER_NAME) === 'sqlite');
            $nowExpr = $isSqlite ? "datetime('now')" : "NOW()";

            $atomicStmt = $this->pdo->prepare(
                "UPDATE help_requests 
                 SET status = 'IN_PROGRESS', assigned_helper_id = :helper_id, updated_at = {$nowExpr} 
                 WHERE id = :id AND status = 'OPEN'"
            );
            $atomicStmt->execute([
                ':id' => $requestId,
                ':helper_id' => $helperId
            ]);

            if ($atomicStmt->rowCount() === 0) {
                // Another helper already accepted or request status changed concurrently
                $this->pdo->rollBack();
                $latest = $this->requestRepository->findById($requestId);
                if ($latest) {
                    if ($latest['assigned_helper_id'] === $helperId) {
                        throw new RuntimeException("You have already accepted this task.", 409);
                    }
                    if ($latest['status'] === 'CANCELLED' || $latest['status'] === 'COMPLETED') {
                        throw new RuntimeException("This task is unavailable or has been {$latest['status']}.", 400);
                    }
                    if ($latest['status'] !== 'OPEN') {
                        throw new RuntimeException("This task has already been accepted by another helper.", 409);
                    }
                }
                throw new RuntimeException("Failed to accept task. Request is no longer available.", 400);
            }

            // Record status history audit
            $hStmt = $this->pdo->prepare(
                "INSERT INTO task_status_history (id, request_id, changed_by_user_id, old_status, new_status)
                 VALUES (:id, :req_id, :user_id, 'OPEN', 'IN_PROGRESS')"
            );
            $hStmt->execute([
                ':id' => 'tsh_' . bin2hex(random_bytes(10)),
                ':req_id' => $requestId,
                ':user_id' => $actorUserId,
            ]);

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
            if ($this->pdo->inTransaction()) {
                $this->pdo->rollBack();
            }
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

    public function validateEvidenceUpload(string $requestId, string $userId): array {
        $req = $this->requestRepository->findById($requestId);
        if (!$req) {
            throw new RuntimeException("Request not found", 404);
        }

        if ($req['requester_id'] === $userId) {
            throw new RuntimeException("Requesters cannot upload completion evidence for their own requests", 403);
        }

        if (empty($req['assigned_helper_id']) || $req['assigned_helper_id'] !== $userId) {
            throw new RuntimeException("Only the assigned helper can upload completion evidence", 403);
        }

        if ($req['status'] === 'PENDING_VERIFICATION') {
            throw new RuntimeException("Task completion has already been submitted and is pending verification", 409);
        }

        if ($req['status'] !== 'IN_PROGRESS') {
            throw new RuntimeException("Cannot upload completion evidence for a task not currently in progress", 400);
        }

        return $req;
    }

    public function submitCompletion(string $requestId, string $helperId, string $notes, ?string $photoUrl = null): array {
        $req = $this->requestRepository->findById($requestId);
        if (!$req) {
            throw new RuntimeException("Request not found", 404);
        }

        // Authorization checks
        if ($req['requester_id'] === $helperId) {
            throw new RuntimeException("Requesters cannot submit completion for their own requests", 403);
        }

        if (empty($req['assigned_helper_id']) || $req['assigned_helper_id'] !== $helperId) {
            throw new RuntimeException("Only the assigned helper can submit task completion", 403);
        }

        if ($req['status'] === 'PENDING_VERIFICATION') {
            throw new RuntimeException("Task completion has already been submitted and is pending verification", 409);
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

            // Automatically deactivate live tracking when completion is submitted
            $this->pdo->prepare("UPDATE task_live_locations SET is_active = 0 WHERE request_id = :id")->execute([':id' => $requestId]);

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

            // Automatically deactivate live tracking when task completes
            $this->pdo->prepare("UPDATE task_live_locations SET is_active = 0 WHERE request_id = :id")->execute([':id' => $requestId]);

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

    public function getEvidence(string $requestId, string $userId): array {
        $req = $this->requestRepository->findById($requestId);
        if (!$req) {
            throw new \RuntimeException("Request not found", 404);
        }

        $isRequester = ($req['requester_id'] === $userId);
        $isHelper = (!empty($req['assigned_helper_id']) && $req['assigned_helper_id'] === $userId);

        if (!$isRequester && !$isHelper) {
            throw new \RuntimeException("Forbidden: Only task participants can view completion evidence", 403);
        }

        $stmt = $this->pdo->prepare(
            "SELECT e.*, u.full_name AS helper_name, u.avatar_url AS helper_avatar
             FROM task_completion_evidence e
             JOIN users u ON e.helper_id = u.id
             WHERE e.request_id = :req_id
             ORDER BY e.submitted_at DESC
             LIMIT 1"
        );
        $stmt->execute([':req_id' => $requestId]);
        $evidence = $stmt->fetch(\PDO::FETCH_ASSOC);

        if (!$evidence) {
            return [
                'id' => '',
                'request_id' => $requestId,
                'helper_id' => $req['assigned_helper_id'] ?? '',
                'notes' => '',
                'proof_photo_url' => null,
                'submitted_at' => '',
                'helper_name' => '',
                'helper_avatar' => '',
            ];
        }

        return $evidence;
    }

    public function getTaskHistory(string $userId): array {
        // Retrieve past completed or cancelled tasks for the user (as requester or helper)
        $stmt = $this->pdo->prepare(
            "SELECT r.id, r.title, r.description AS subtitle,
                    r.created_at, r.status, r.category,
                    COALESCE(rt.rating, 0.0) AS rating
             FROM help_requests r
             LEFT JOIN ratings rt ON rt.request_id = r.id
             WHERE (r.requester_id = :uid OR r.assigned_helper_id = :uid)
               AND r.status IN ('COMPLETED', 'CANCELLED')
             ORDER BY r.created_at DESC"
        );
        $stmt->execute([':uid' => $userId]);
        $rows = $stmt->fetchAll();

        return array_map(function($r) {
            return [
                'id' => (string)$r['id'],
                'title' => (string)$r['title'],
                'subtitle' => (string)$r['subtitle'],
                'date' => date('M d, Y', strtotime($r['created_at'])),
                'rating' => (float)$r['rating'],
                'status' => $r['status'] === 'COMPLETED' ? 'Completed' : 'Cancelled',
                'category' => $r['status'] === 'COMPLETED' ? 'completed' : 'cancelled',
            ];
        }, $rows);
    }

    public function updateLiveLocation(string $requestId, string $helperId, float $lat, float $lng, ?float $heading = null, ?float $accuracy = null, ?float $speed = null): array {
        $req = $this->requestRepository->findById($requestId);
        if (!$req) {
            throw new RuntimeException("Request not found", 404);
        }
        // Only the assigned helper can update live location
        if ($req['assigned_helper_id'] !== $helperId) {
            throw new RuntimeException("Only the assigned helper can share location for this task", 403);
        }
        // Location sharing only allowed while task is IN_PROGRESS
        if ($req['status'] !== 'IN_PROGRESS') {
            throw new RuntimeException("Location sharing is only active while the task is IN_PROGRESS (Current: {$req['status']})", 400);
        }

        $isSqlite = ($this->pdo->getAttribute(PDO::ATTR_DRIVER_NAME) === 'sqlite');
        if ($isSqlite) {
            $stmt = $this->pdo->prepare(
                "INSERT INTO task_live_locations (request_id, helper_id, latitude, longitude, heading, accuracy, speed, is_active, updated_at)
                 VALUES (:req_id, :helper_id, :lat, :lng, :heading, :accuracy, :speed, 1, datetime('now'))
                 ON CONFLICT(request_id) DO UPDATE SET
                    latitude = excluded.latitude,
                    longitude = excluded.longitude,
                    heading = excluded.heading,
                    accuracy = excluded.accuracy,
                    speed = excluded.speed,
                    is_active = 1,
                    updated_at = datetime('now')"
            );
        } else {
            $stmt = $this->pdo->prepare(
                "INSERT INTO task_live_locations (request_id, helper_id, latitude, longitude, heading, accuracy, speed, is_active, updated_at)
                 VALUES (:req_id, :helper_id, :lat, :lng, :heading, :accuracy, :speed, 1, NOW())
                 ON DUPLICATE KEY UPDATE
                    latitude = VALUES(latitude),
                    longitude = VALUES(longitude),
                    heading = VALUES(heading),
                    accuracy = VALUES(accuracy),
                    speed = VALUES(speed),
                    is_active = 1,
                    updated_at = NOW()"
            );
        }
        $stmt->execute([
            ':req_id' => $requestId,
            ':helper_id' => $helperId,
            ':lat' => $lat,
            ':lng' => $lng,
            ':heading' => $heading,
            ':accuracy' => $accuracy,
            ':speed' => $speed,
        ]);

        return $this->getLiveLocation($requestId, $helperId);
    }

    public function getLiveLocation(string $requestId, string $viewerUserId): array {
        $req = $this->requestRepository->findById($requestId);
        if (!$req) {
            throw new RuntimeException("Request not found", 404);
        }

        // Privacy check: Only requester and assigned helper can see live tracking
        $isRequester = ($req['requester_id'] === $viewerUserId);
        $isHelper = ($req['assigned_helper_id'] === $viewerUserId);
        if (!$isRequester && !$isHelper) {
            throw new RuntimeException("Access denied. Helper location is only visible to the requester of this active task.", 403);
        }

        // If task is no longer IN_PROGRESS, deactivate live location
        if ($req['status'] !== 'IN_PROGRESS') {
            $this->pdo->prepare("UPDATE task_live_locations SET is_active = 0 WHERE request_id = :id")->execute([':id' => $requestId]);
            return [
                'request_id' => $requestId,
                'status' => $req['status'],
                'is_active' => false,
                'message' => 'Task is no longer active. Location tracking has ended.'
            ];
        }

        $stmt = $this->pdo->prepare(
            "SELECT l.*, u.full_name AS helper_name, u.avatar_url AS helper_avatar
             FROM task_live_locations l
             JOIN users u ON u.id = l.helper_id
             WHERE l.request_id = :req_id
             LIMIT 1"
        );
        $stmt->execute([':req_id' => $requestId]);
        $loc = $stmt->fetch(PDO::FETCH_ASSOC);

        if (!$loc || empty($loc['is_active'])) {
            return [
                'request_id' => $requestId,
                'status' => $req['status'],
                'is_active' => false,
                'helper_name' => $req['assigned_helper_name'],
                'message' => 'Helper has not shared live location yet.'
            ];
        }

        $helperLat = (float)$loc['latitude'];
        $helperLng = (float)$loc['longitude'];
        $reqLat = $req['latitude'] !== null ? (float)$req['latitude'] : null;
        $reqLng = $req['longitude'] !== null ? (float)$req['longitude'] : null;

        $distKm = null;
        $distMiles = null;
        $etaMinutes = null;

        if ($reqLat !== null && $reqLng !== null) {
            $earthRadiusKm = 6371.0;
            $dLat = deg2rad($reqLat - $helperLat);
            $dLng = deg2rad($reqLng - $helperLng);
            $a = sin($dLat / 2) * sin($dLat / 2) +
                 cos(deg2rad($helperLat)) * cos(deg2rad($reqLat)) *
                 sin($dLng / 2) * sin($dLng / 2);
            $c = 2 * atan2(sqrt($a), sqrt(1 - $a));
            $distKm = round($earthRadiusKm * $c, 2);
            $distMiles = round($distKm * 0.621371, 2);

            $speedKmh = ($loc['speed'] !== null && (float)$loc['speed'] > 2.0) ? ((float)$loc['speed'] * 3.6) : 20.0;
            if ($distKm > 0) {
                $etaMinutes = max(1, (int)round(($distKm / $speedKmh) * 60));
            } else {
                $etaMinutes = 0;
            }
        }

        return [
            'request_id' => $requestId,
            'status' => $req['status'],
            'is_active' => true,
            'helper_id' => $loc['helper_id'],
            'helper_name' => $loc['helper_name'] ?? $req['assigned_helper_name'],
            'helper_avatar' => $loc['helper_avatar'] ?? $req['assigned_helper_avatar'],
            'latitude' => $helperLat,
            'longitude' => $helperLng,
            'heading' => $loc['heading'] !== null ? (float)$loc['heading'] : null,
            'accuracy' => $loc['accuracy'] !== null ? (float)$loc['accuracy'] : null,
            'speed' => $loc['speed'] !== null ? (float)$loc['speed'] : null,
            'requester_latitude' => $reqLat,
            'requester_longitude' => $reqLng,
            'requester_location' => $req['location'],
            'distance_km' => $distKm,
            'distance_miles' => $distMiles,
            'eta_minutes' => $etaMinutes,
            'updated_at' => $loc['updated_at'],
        ];
    }

    public function stopLiveLocation(string $requestId, string $userId): bool {
        $req = $this->requestRepository->findById($requestId);
        if (!$req) {
            throw new RuntimeException("Request not found", 404);
        }
        if ($req['requester_id'] !== $userId && $req['assigned_helper_id'] !== $userId) {
            throw new RuntimeException("Access denied", 403);
        }
        $stmt = $this->pdo->prepare("UPDATE task_live_locations SET is_active = 0 WHERE request_id = :req_id");
        $stmt->execute([':req_id' => $requestId]);
        return true;
    }
}
