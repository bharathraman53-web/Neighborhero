<?php
declare(strict_types=1);

namespace NeighborHero\Services;

use PDO;

/**
 * Intelligent AI-Based Helper Matching Engine
 * Evaluates multi-parameter compatibility to recommend the most suitable helper for a request.
 *
 * Configurable weights:
 * - Skill & Category Match: 30%
 * - Location & Distance: 20%
 * - Availability: 20%
 * - Ratings & Feedback: 15%
 * - Reliability & Completed Tasks: 10%
 * - Workload & Fair Distribution: 5%
 */
class HelperMatchingEngine {
    private PDO $pdo;

    /** @var array<string, float> */
    private array $weights = [
        'skill' => 0.30,
        'location' => 0.20,
        'availability' => 0.20,
        'rating' => 0.15,
        'reliability' => 0.10,
        'workload' => 0.05,
    ];

    /**
     * Approximate centroid coordinates for known neighborhoods (San Francisco metro area)
     * Used when explicit GPS coordinates are omitted.
     */
    private const NEIGHBORHOOD_CENTROIDS = [
        'maplewood'   => ['lat' => 37.7749, 'lng' => -122.4194],
        'oakridge'    => ['lat' => 37.7833, 'lng' => -122.4167],
        'greenwood'   => ['lat' => 37.7690, 'lng' => -122.4467],
        'downtown'    => ['lat' => 37.7879, 'lng' => -122.4075],
        'riverside'   => ['lat' => 37.7550, 'lng' => -122.4200],
        'pinecrest'   => ['lat' => 37.7950, 'lng' => -122.3950],
        'cedar grove' => ['lat' => 37.7620, 'lng' => -122.4350],
        'west end'    => ['lat' => 37.7700, 'lng' => -122.4500],
    ];

    public function __construct(PDO $pdo, ?array $customWeights = null) {
        $this->pdo = $pdo;
        if ($customWeights !== null) {
            $this->setWeights($customWeights);
        }
    }

    public function setWeights(array $weights): void {
        $sum = array_sum($weights);
        if ($sum > 0) {
            foreach ($weights as $k => $w) {
                if (isset($this->weights[$k])) {
                    $this->weights[$k] = (float)$w / $sum;
                }
            }
        }
    }

    public function getWeights(): array {
        return $this->weights;
    }

    /**
     * Recommends ranked helpers for a specific help request.
     *
     * @param string $requestId
     * @param array $preferences Optional requester preferences (e.g. max_distance, required_skills)
     * @param int $limit
     * @return array Ranked helpers with match scores and explainable reasons
     */
    public function matchForRequest(string $requestId, array $preferences = [], int $limit = 10): array {
        $stmt = $this->pdo->prepare("SELECT * FROM help_requests WHERE id = :id LIMIT 1");
        $stmt->execute([':id' => $requestId]);
        $request = $stmt->fetch();

        if (!$request) {
            return [];
        }

        $context = [
            'request_id'     => $request['id'],
            'requester_id'   => $request['requester_id'],
            'category'       => $request['category'],
            'title'          => $request['title'],
            'description'    => $request['description'],
            'location'       => $request['location'],
            'requested_time' => $request['requested_time'],
            'latitude'       => !empty($request['latitude']) ? (float)$request['latitude'] : null,
            'longitude'      => !empty($request['longitude']) ? (float)$request['longitude'] : null,
        ];

        return $this->matchHelpers($context, $preferences, $limit);
    }

    /**
     * Core matching entry point: matches eligible helpers for a given request context.
     * Adaptively switches between small-volume in-memory evaluation and large-volume SQL pre-filtering.
     *
     * @param array $context Request context (category, location, requested_time, lat, lng, requester_id)
     * @param array $preferences Requester preferences
     * @param int $limit
     * @return array
     */
    public function matchHelpers(array $context, array $preferences = [], int $limit = 10): array {
        $requesterId = $context['requester_id'] ?? null;
        $category = trim($context['category'] ?? 'Other');
        $location = trim($context['location'] ?? 'Maplewood');
        $requestedTime = trim($context['requested_time'] ?? 'Today');
        $reqLat = $context['latitude'] ?? null;
        $reqLng = $context['longitude'] ?? null;

        // Resolve request coordinates if not provided
        if ($reqLat === null || $reqLng === null) {
            $coords = $this->resolveCoordinates($location);
            $reqLat = $coords['lat'];
            $reqLng = $coords['lng'];
        }

        // Count total active verified helpers to select matching mode
        $countStmt = $this->pdo->query("SELECT COUNT(*) FROM users WHERE is_verified = 1");
        $totalHelpers = (int)($countStmt ? $countStmt->fetchColumn() : 0);

        // Fetch candidate helpers adaptively
        $candidates = ($totalHelpers <= 10)
            ? $this->fetchCandidatesSmallVolume($requesterId)
            : $this->fetchCandidatesLargeVolume($requesterId, $category, $reqLat, $reqLng);

        if (empty($candidates)) {
            return [];
        }

        $scoredHelpers = [];
        foreach ($candidates as $helper) {
            $scoreData = $this->evaluateHelper($helper, $category, $location, $requestedTime, $reqLat, $reqLng, $preferences);
            
            // Minimum eligibility: If a specific category is requested, require skill match
            if (strcasecmp($category, 'Other') !== 0 && empty($scoreData['has_skill_match'])) {
                continue;
            }

            // Respect optional preference filters
            if (!empty($preferences['max_distance']) && $scoreData['raw_distance'] > (float)$preferences['max_distance']) {
                continue;
            }

            $scoredHelpers[] = array_merge($helper, $scoreData);
        }

        // Sort descending by overall match score
        usort($scoredHelpers, fn($a, $b) => $b['match_score'] <=> $a['match_score']);

        $cleaned = [];
        foreach (array_slice($scoredHelpers, 0, $limit) as $item) {
            unset($item['latitude'], $item['longitude']);
            $cleaned[] = $item;
        }

        return $cleaned;
    }

    /**
     * Evaluates a single helper across all 6 parameters and computes the weighted score.
     */
    public function evaluateHelper(
        array $helper,
        string $category,
        string $location,
        string $requestedTime,
        float $reqLat,
        float $reqLng,
        array $preferences = []
    ): array {
        $helperId = $helper['id'];

        // 1. Skill & Category Score (30%)
        $skillResult = $this->calculateSkillScore($helperId, $category);
        $skillScore = $skillResult['score'];

        // 2. Location & Distance Score (20%)
        $locResult = $this->calculateLocationScore($helper, $reqLat, $reqLng);
        $locScore = $locResult['score'];
        $distanceMiles = $locResult['distance_miles'];
        $distanceText = $locResult['distance_text'];

        // 3. Availability Score (20%)
        $availResult = $this->calculateAvailabilityScore($helper, $requestedTime);
        $availScore = $availResult['score'];

        // 4. Ratings & Feedback Score (15%)
        $ratingResult = $this->calculateRatingScore($helper);
        $ratingScore = $ratingResult['score'];

        // 5. Reliability & Completed Tasks Score (10%)
        $relResult = $this->calculateReliabilityScore($helper);
        $relScore = $relResult['score'];

        // 6. Workload & Fair Distribution Score (5%)
        $workloadResult = $this->calculateWorkloadScore($helperId);
        $workloadScore = $workloadResult['score'];
        $activeTaskCount = $workloadResult['active_tasks'];

        // Weighted total
        $totalScore = (
            ($this->weights['skill'] * $skillScore) +
            ($this->weights['location'] * $locScore) +
            ($this->weights['availability'] * $availScore) +
            ($this->weights['rating'] * $ratingScore) +
            ($this->weights['reliability'] * $relScore) +
            ($this->weights['workload'] * $workloadScore)
        );

        $totalScore = max(0.0, min(1.0, $totalScore));
        $matchPercentage = (int)round($totalScore * 100);

        // Generate explainable matching reasons
        $reasons = $this->generateMatchingReasons([
            'skill' => $skillResult,
            'location' => $locResult,
            'availability' => $availResult,
            'rating' => $ratingResult,
            'reliability' => $relResult,
            'workload' => $workloadResult,
            'category' => $category,
            'neighborhood' => $helper['neighborhood'] ?? '',
        ]);

        return [
            'match_score'       => round($totalScore, 4),
            'match_percentage'  => $matchPercentage,
            'matching_reasons'  => $reasons,
            'has_skill_match'   => $skillResult['has_match'],
            'distance'          => $distanceText,
            'distance_miles'    => $distanceMiles,
            'raw_distance'      => $distanceMiles,
            'active_tasks'      => $activeTaskCount,
            'score_breakdown'   => [
                'skill'         => round($skillScore, 3),
                'location'      => round($locScore, 3),
                'availability'  => round($availScore, 3),
                'rating'        => round($ratingScore, 3),
                'reliability'   => round($relScore, 3),
                'workload'      => round($workloadScore, 3),
            ],
        ];
    }

    /**
     * Skill matching logic: checks skills table and past completed tasks in category.
     */
    private function calculateSkillScore(string $helperId, string $category): array {
        $canonical = $this->canonicalCategory($category);
        $aliases = $this->getCategoryAliases($canonical);

        // Fetch helper skills
        $stmt = $this->pdo->prepare("SELECT skill_name FROM helper_skills WHERE user_id = :uid");
        $stmt->execute([':uid' => $helperId]);
        $skills = $stmt->fetchAll(PDO::FETCH_COLUMN) ?: [];

        $hasExactSkill = false;
        $hasAliasSkill = false;

        foreach ($skills as $s) {
            $sClean = trim($s);
            if (strcasecmp($sClean, $canonical) === 0) {
                $hasExactSkill = true;
                break;
            }
            foreach ($aliases as $alias) {
                if (strcasecmp($sClean, $alias) === 0) {
                    $hasAliasSkill = true;
                    break;
                }
            }
        }

        // Check history of completed tasks in this category
        $stmtHist = $this->pdo->prepare(
            "SELECT COUNT(*) FROM help_requests 
             WHERE assigned_helper_id = :uid AND status = 'COMPLETED' AND (category = :cat1 OR category = :cat2)"
        );
        $catAlias = !empty($aliases[0]) ? $aliases[0] : $canonical;
        $stmtHist->execute([':uid' => $helperId, ':cat1' => $canonical, ':cat2' => $catAlias]);
        $completedInCategory = (int)$stmtHist->fetchColumn();

        $score = 0.0;
        $matchedSkill = null;

        if ($hasExactSkill) {
            $score = 0.90;
            $matchedSkill = $canonical;
        } elseif ($hasAliasSkill) {
            $score = 0.85;
            $matchedSkill = $catAlias;
        } elseif (strcasecmp($canonical, 'Other') === 0 && count($skills) > 0) {
            $score = 0.70;
            $matchedSkill = 'General Assistance';
        }

        // Add experience bonus for completed tasks in category
        if ($completedInCategory > 0) {
            $bonus = min(0.10, $completedInCategory * 0.03);
            $score = min(1.0, $score + $bonus);
        }

        return [
            'score'                 => $score,
            'has_match'             => ($score > 0.3),
            'matched_skill'         => $matchedSkill,
            'completed_in_category' => $completedInCategory,
        ];
    }

    /**
     * Location & Distance calculation using the Haversine formula.
     */
    private function calculateLocationScore(array $helper, float $reqLat, float $reqLng): array {
        $helperLat = !empty($helper['latitude']) ? (float)$helper['latitude'] : null;
        $helperLng = !empty($helper['longitude']) ? (float)$helper['longitude'] : null;

        if ($helperLat === null || $helperLng === null) {
            $coords = $this->resolveCoordinates($helper['neighborhood'] ?? '');
            $helperLat = $coords['lat'];
            $helperLng = $coords['lng'];
        }

        $distanceMiles = $this->haversineDistance($reqLat, $reqLng, $helperLat, $helperLng);
        $distanceMiles = round($distanceMiles, 1);

        // Smooth distance decay
        if ($distanceMiles <= 0.5) {
            $score = 1.00;
        } elseif ($distanceMiles <= 1.0) {
            $score = 0.95;
        } elseif ($distanceMiles <= 2.0) {
            $score = 0.85;
        } elseif ($distanceMiles <= 5.0) {
            $score = max(0.50, 0.85 - (0.10 * ($distanceMiles - 2.0)));
        } elseif ($distanceMiles <= 10.0) {
            $score = max(0.20, 0.50 - (0.06 * ($distanceMiles - 5.0)));
        } else {
            $score = max(0.05, 0.20 - (0.015 * ($distanceMiles - 10.0)));
        }

        $distanceText = ($distanceMiles < 0.1) ? '0.1 miles away' : "{$distanceMiles} miles away";

        return [
            'score'          => round($score, 3),
            'distance_miles' => $distanceMiles,
            'distance_text'  => $distanceText,
        ];
    }

    /**
     * Availability calculation: parses requested time and matches against helper availability.
     */
    private function calculateAvailabilityScore(array $helper, string $requestedTime): array {
        $isAvailableToday = isset($helper['is_available_today']) ? (bool)$helper['is_available_today'] : true;
        $availableDaysStr = $helper['available_days'] ?? 'Monday,Tuesday,Wednesday,Thursday,Friday,Saturday,Sunday';
        $availableDays = array_map('trim', explode(',', strtolower($availableDaysStr)));
        $availableSlotsStr = $helper['available_time_slots'] ?? 'Morning,Afternoon,Evening';
        $availableSlots = array_map('trim', explode(',', strtolower($availableSlotsStr)));

        $timeLower = strtolower($requestedTime);
        $isTodayRequest = (str_contains($timeLower, 'today') || str_contains($timeLower, 'now') || str_contains($timeLower, 'urgent'));
        
        // Check days of week mentioned
        $daysOfWeek = ['monday', 'tuesday', 'wednesday', 'thursday', 'friday', 'saturday', 'sunday'];
        $requestedDay = null;
        foreach ($daysOfWeek as $d) {
            if (str_contains($timeLower, $d)) {
                $requestedDay = $d;
                break;
            }
        }

        // Check time slots mentioned
        $requestedSlot = null;
        if (str_contains($timeLower, 'morning') || preg_match('/\b(8|9|10|11)\s*am\b/', $timeLower)) {
            $requestedSlot = 'morning';
        } elseif (str_contains($timeLower, 'afternoon') || preg_match('/\b(12|1|2|3|4|5)\s*pm\b/', $timeLower)) {
            $requestedSlot = 'afternoon';
        } elseif (str_contains($timeLower, 'evening') || preg_match('/\b(6|7|8|9)\s*pm\b/', $timeLower)) {
            $requestedSlot = 'evening';
        }

        $score = 0.80; // Baseline for flexible availability
        $isAvailableForTime = true;

        if ($isTodayRequest) {
            if (!$isAvailableToday) {
                $score = 0.10; // Explicitly marked unavailable today
                $isAvailableForTime = false;
            } else {
                $score = 0.95;
            }
        }

        if ($requestedDay !== null) {
            if (!in_array($requestedDay, $availableDays, true)) {
                $score = min($score, 0.15); // Day off
                $isAvailableForTime = false;
            } else {
                $score = max($score, 0.90);
            }
        }

        if ($requestedSlot !== null && $isAvailableForTime) {
            if (in_array($requestedSlot, $availableSlots, true)) {
                $score = min(1.0, $score + 0.05);
            } else {
                $score = max(0.30, $score - 0.35); // Slot mismatch
            }
        }

        return [
            'score'                 => round($score, 3),
            'is_available_today'    => $isAvailableToday,
            'is_available_for_time' => $isAvailableForTime,
        ];
    }

    /**
     * Ratings & Feedback scoring with Bayesian smoothing.
     * Prevents small sample sizes from unfairly dominating while rewarding consistent quality.
     */
    private function calculateRatingScore(array $helper): array {
        $rawRating = isset($helper['rating']) ? (float)$helper['rating'] : 5.0;
        $reviewCount = isset($helper['reviewCount']) ? (int)$helper['reviewCount'] : (int)($helper['completed_tasks_count'] ?? 0);

        // Bayesian smoothed rating: prior mean = 4.5, prior weight = 3
        $priorMean = 4.5;
        $priorWeight = 3;
        $smoothedRating = (($reviewCount * $rawRating) + ($priorWeight * $priorMean)) / ($reviewCount + $priorWeight);

        // Normalize [1.0, 5.0] to [0.0, 1.0]
        $score = max(0.0, min(1.0, ($smoothedRating - 1.0) / 4.0));

        return [
            'score'           => round($score, 3),
            'raw_rating'      => $rawRating,
            'review_count'    => $reviewCount,
            'smoothed_rating' => round($smoothedRating, 2),
        ];
    }

    /**
     * Reliability calculation: completed vs accepted tasks, cancellations, and response time.
     */
    private function calculateReliabilityScore(array $helper): array {
        $completed = isset($helper['tasksDone']) ? (int)$helper['tasksDone'] : (int)($helper['completed_tasks_count'] ?? 0);
        $cancellations = isset($helper['cancellation_count']) ? (int)$helper['cancellation_count'] : 0;
        $responseMins = isset($helper['avg_response_minutes']) ? (int)$helper['avg_response_minutes'] : 15;

        $accepted = $completed + $cancellations;

        if ($accepted === 0) {
            // New helper baseline
            $score = 0.80;
            $completionRate = 1.0;
        } else {
            $completionRate = (float)$completed / max(1, $accepted);
            $cancellationPenalty = min(0.50, ((float)$cancellations / max(1, $accepted)) * 0.50);
            $responseScore = max(0.50, 1.0 - ($responseMins / 120.0));

            $score = ($completionRate * (1.0 - $cancellationPenalty) * 0.70) + ($responseScore * 0.30);
            $score = max(0.10, min(1.0, $score));
        }

        return [
            'score'           => round($score, 3),
            'completed_count' => $completed,
            'completion_rate' => round($completionRate * 100),
        ];
    }

    /**
     * Workload calculation: prevents overloaded helpers from monopolizing tasks.
     */
    private function calculateWorkloadScore(string $helperId): array {
        $stmt = $this->pdo->prepare(
            "SELECT COUNT(*) FROM help_requests 
             WHERE assigned_helper_id = :uid AND status IN ('IN_PROGRESS', 'PENDING_VERIFICATION')"
        );
        $stmt->execute([':uid' => $helperId]);
        $activeTasks = (int)$stmt->fetchColumn();

        if ($activeTasks === 0) {
            $score = 1.00;
        } elseif ($activeTasks === 1) {
            $score = 0.70;
        } elseif ($activeTasks === 2) {
            $score = 0.40;
        } else {
            $score = 0.10;
        }

        return [
            'score'        => $score,
            'active_tasks' => $activeTasks,
        ];
    }

    /**
     * Generates clean, explainable matching reasons for the requester UI.
     */
    private function generateMatchingReasons(array $data): array {
        $reasons = [];

        // 1. Skill Reason
        $skill = $data['skill'];
        if ($skill['has_match'] && !empty($skill['matched_skill'])) {
            if ($skill['completed_in_category'] >= 3) {
                $reasons[] = "Has successfully completed {$skill['completed_in_category']} {$data['category']} tasks";
            } else {
                $reasons[] = "Skills match your request ({$skill['matched_skill']})";
            }
        }

        // 2. Location Reason
        $loc = $data['location'];
        if ($loc['distance_miles'] <= 1.0) {
            $neighborhood = !empty($data['neighborhood']) ? " in {$data['neighborhood']}" : '';
            $reasons[] = "Located near your task ({$loc['distance_text']}{$neighborhood})";
        } elseif ($loc['distance_miles'] <= 3.0) {
            $reasons[] = "Located in your local area ({$loc['distance_text']})";
        }

        // 3. Availability Reason
        $avail = $data['availability'];
        if ($avail['score'] >= 0.85) {
            $reasons[] = "Available at your preferred time";
        }

        // 4. Rating Reason
        $rating = $data['rating'];
        if ($rating['raw_rating'] >= 4.7 && $rating['review_count'] >= 1) {
            $reasons[] = "Highly rated by previous requesters ({$rating['raw_rating']} ★)";
        } elseif ($rating['raw_rating'] >= 4.5) {
            $reasons[] = "Strong community feedback ({$rating['raw_rating']} ★)";
        }

        // 5. Reliability Reason
        $rel = $data['reliability'];
        if ($rel['completion_rate'] >= 95 && $rel['completed_count'] >= 2) {
            $reasons[] = "High reliability ({$rel['completion_rate']}% task completion rate)";
        } elseif ($rel['completed_count'] >= 1) {
            $reasons[] = "Proven neighbor ({$rel['completed_count']} tasks completed)";
        }

        // 6. Workload Reason
        $workload = $data['workload'];
        if ($workload['active_tasks'] === 0) {
            $reasons[] = "Ready to assist (No active tasks currently)";
        }

        // Ensure at least 2 distinct reasons
        if (empty($reasons)) {
            $reasons[] = "Verified community member ready to help";
        }

        return array_slice($reasons, 0, 4);
    }

    /**
     * Situation A: Small number of users (fast, comprehensive in-memory fetch).
     */
    private function fetchCandidatesSmallVolume(?string $excludeUserId): array {
        $sql = "SELECT u.id, u.full_name AS name, 'Neighborhood Hero' AS title,
                       COALESCE(u.neighborhood, 'Greenwood') AS neighborhood,
                       u.rating,
                       (u.completed_tasks_count * 3) AS reviewCount,
                       u.completed_tasks_count AS tasksDone,
                       99 AS onTimePercentage,
                       COALESCE(u.bio, '') AS bio,
                       u.is_verified AS isVerified,
                       u.avatar_url AS avatarUrl,
                       u.avatar_url AS avatar_url,
                       COALESCE(u.latitude, NULL) AS latitude,
                       COALESCE(u.longitude, NULL) AS longitude,
                       COALESCE(u.is_available_today, 1) AS is_available_today,
                       COALESCE(u.available_days, 'Monday,Tuesday,Wednesday,Thursday,Friday,Saturday,Sunday') AS available_days,
                       COALESCE(u.available_time_slots, 'Morning,Afternoon,Evening') AS available_time_slots,
                       COALESCE(u.cancellation_count, 0) AS cancellation_count,
                       COALESCE(u.avg_response_minutes, 15) AS avg_response_minutes
                FROM users u
                WHERE u.is_verified = 1";

        if (!empty($excludeUserId)) {
            $sql .= " AND u.id != :exclude_id";
            $stmt = $this->pdo->prepare($sql);
            $stmt->execute([':exclude_id' => $excludeUserId]);
        } else {
            $stmt = $this->pdo->query($sql);
        }

        $helpers = $stmt ? $stmt->fetchAll() : [];
        $this->attachSkills($helpers);
        return $helpers;
    }

    /**
     * Situation B: Large number of users (efficient database filtering, pre-ranking, anti-fatigue).
     */
    private function fetchCandidatesLargeVolume(?string $excludeUserId, string $category, float $reqLat, float $reqLng): array {
        $canonical = $this->canonicalCategory($category);
        $aliases = $this->getCategoryAliases($canonical);
        $skillCandidates = array_merge([$canonical], $aliases);

        $params = [];
        $where = ["u.is_verified = 1"];

        if (!empty($excludeUserId)) {
            $where[] = "u.id != :exclude_id";
            $params[':exclude_id'] = $excludeUserId;
        }

        // Skill filtering: prioritize users with relevant skill, completed task in category, or general
        $placeholders = [];
        foreach ($skillCandidates as $i => $sc) {
            $paramName = ":sc_{$i}";
            $placeholders[] = "LOWER(s.skill_name) = LOWER({$paramName})";
            $params[$paramName] = $sc;
        }
        $skillCheck = "EXISTS (SELECT 1 FROM helper_skills s WHERE s.user_id = u.id AND (" . implode(' OR ', $placeholders) . "))";

        // Workload filtering: exclude overloaded helpers with >= 5 active tasks
        $workloadCheck = "(SELECT COUNT(*) FROM help_requests hr WHERE hr.assigned_helper_id = u.id AND hr.status IN ('IN_PROGRESS', 'PENDING_VERIFICATION')) < 5";

        $where[] = "({$skillCheck} OR {$workloadCheck})";

        $whereSql = implode(' AND ', $where);

        $sql = "SELECT u.id, u.full_name AS name, 'Neighborhood Hero' AS title,
                       COALESCE(u.neighborhood, 'Greenwood') AS neighborhood,
                       u.rating,
                       (u.completed_tasks_count * 3) AS reviewCount,
                       u.completed_tasks_count AS tasksDone,
                       99 AS onTimePercentage,
                       COALESCE(u.bio, '') AS bio,
                       u.is_verified AS isVerified,
                       u.avatar_url AS avatarUrl,
                       u.avatar_url AS avatar_url,
                       COALESCE(u.latitude, NULL) AS latitude,
                       COALESCE(u.longitude, NULL) AS longitude,
                       COALESCE(u.is_available_today, 1) AS is_available_today,
                       COALESCE(u.available_days, 'Monday,Tuesday,Wednesday,Thursday,Friday,Saturday,Sunday') AS available_days,
                       COALESCE(u.available_time_slots, 'Morning,Afternoon,Evening') AS available_time_slots,
                       COALESCE(u.cancellation_count, 0) AS cancellation_count,
                       COALESCE(u.avg_response_minutes, 15) AS avg_response_minutes
                FROM users u
                WHERE {$whereSql}
                ORDER BY u.rating DESC, u.completed_tasks_count DESC
                LIMIT 50";

        $stmt = $this->pdo->prepare($sql);
        $stmt->execute($params);
        $helpers = $stmt->fetchAll();
        $this->attachSkills($helpers);
        return $helpers;
    }

    private function attachSkills(array &$helpers): void {
        if (empty($helpers)) return;

        $userIds = array_column($helpers, 'id');
        $inClause = implode(',', array_fill(0, count($userIds), '?'));
        $stmt = $this->pdo->prepare("SELECT user_id, skill_name FROM helper_skills WHERE user_id IN ({$inClause})");
        $stmt->execute($userIds);
        $rows = $stmt->fetchAll();

        $skillsMap = [];
        foreach ($rows as $r) {
            $skillsMap[$r['user_id']][] = $r['skill_name'];
        }

        foreach ($helpers as &$h) {
            $h['skills'] = $skillsMap[$h['id']] ?? [];
            $h['rating'] = (float)$h['rating'];
            $h['reviewCount'] = (int)$h['reviewCount'];
            $h['tasksDone'] = (int)$h['tasksDone'];
            $h['onTimePercentage'] = (int)$h['onTimePercentage'];
            $h['isVerified'] = (bool)$h['isVerified'];
            $h['isAvailableToday'] = isset($h['is_available_today']) ? (bool)$h['is_available_today'] : true;
        }
    }

    private function resolveCoordinates(string $neighborhood): array {
        $nClean = strtolower(trim($neighborhood));
        foreach (self::NEIGHBORHOOD_CENTROIDS as $k => $coords) {
            if (str_contains($nClean, $k) || str_contains($k, $nClean)) {
                return $coords;
            }
        }
        return ['lat' => 37.7749, 'lng' => -122.4194]; // Default Maplewood
    }

    private function haversineDistance(float $lat1, float $lon1, float $lat2, float $lon2): float {
        $earthRadius = 3958.8; // Earth's radius in miles

        $dLat = deg2rad($lat2 - $lat1);
        $dLon = deg2rad($lon2 - $lon1);

        $a = sin($dLat / 2) * sin($dLat / 2) +
             cos(deg2rad($lat1)) * cos(deg2rad($lat2)) *
             sin($dLon / 2) * sin($dLon / 2);

        $c = 2 * atan2(sqrt($a), sqrt(1 - $a));
        return $earthRadius * $c;
    }

    private function canonicalCategory(string $category): string {
        $c = trim($category);
        if (strcasecmp($c, 'Pet Care') === 0 || strcasecmp($c, 'Pet Help') === 0) return 'Pet Help';
        if (strcasecmp($c, 'Groceries') === 0) return 'Groceries';
        if (strcasecmp($c, 'Books') === 0) return 'Books';
        if (strcasecmp($c, 'Repairs') === 0) return 'Repairs';
        if (strcasecmp($c, 'Transport') === 0) return 'Transport';
        if (strcasecmp($c, 'Tutoring') === 0) return 'Tutoring';
        if (strcasecmp($c, 'Other') === 0) return 'Other';
        return ucwords(strtolower($c));
    }

    private function getCategoryAliases(string $canonical): array {
        $lower = strtolower(trim($canonical));
        return match ($lower) {
            'pet help', 'pet care' => ['Pet Care', 'Pet Help'],
            'groceries' => ['Food Delivery', 'Shopping'],
            'repairs' => ['Fixing', 'Maintenance', 'Handyman'],
            'transport' => ['Rides', 'Moving'],
            default => [],
        };
    }
}
