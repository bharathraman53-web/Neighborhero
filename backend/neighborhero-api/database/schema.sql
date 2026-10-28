-- ==============================================================================
-- NEIGHBORHERO DATABASE SCHEMA (MySQL 8.0+ / MariaDB 10.4+)
-- Safe & Repeatable: Uses CREATE ... IF NOT EXISTS. Does not drop existing data.
-- Character Set: utf8mb4 (Full Unicode & Emoji support)
-- ==============================================================================

CREATE DATABASE IF NOT EXISTS neighborhero_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE neighborhero_db;

-- ------------------------------------------------------------------------------
-- 1. USERS TABLE
-- Core credentials, identity, and aggregated community metrics.
-- ------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS users (
    id VARCHAR(64) NOT NULL PRIMARY KEY,
    full_name VARCHAR(120) NOT NULL,
    email VARCHAR(191) NOT NULL UNIQUE,
    phone VARCHAR(32) NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    neighborhood VARCHAR(120) NOT NULL DEFAULT '',
    bio TEXT NULL,
    avatar_url VARCHAR(255) NOT NULL DEFAULT '',
    is_verified TINYINT(1) NOT NULL DEFAULT 1,
    points INT NOT NULL DEFAULT 0,
    completed_tasks_count INT NOT NULL DEFAULT 0,
    rating DECIMAL(3, 2) NOT NULL DEFAULT 5.00,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_users_email (email),
    INDEX idx_users_phone (phone),
    INDEX idx_users_points (points DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------------------------------------------
-- 2. USER PROFILES TABLE
-- Extended preferences, emergency contacts, and neighborhood settings.
-- ------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS user_profiles (
    user_id VARCHAR(64) NOT NULL PRIMARY KEY,
    emergency_contact_name VARCHAR(120) NULL,
    emergency_contact_phone VARCHAR(32) NULL,
    interests TEXT NULL, -- Stored as JSON or comma-separated strings
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_user_profiles_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------------------------------------------
-- 3. AUTH TOKENS TABLE
-- Secure mobile session bearer tokens.
-- Plaintext token is never stored server-side; only SHA-256 hash is persisted.
-- ------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS auth_tokens (
    id VARCHAR(64) NOT NULL PRIMARY KEY,
    user_id VARCHAR(64) NOT NULL,
    token_hash VARCHAR(64) NOT NULL UNIQUE,
    device_info VARCHAR(255) NULL,
    expires_at TIMESTAMP NOT NULL,
    revoked_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_tokens_hash (token_hash),
    INDEX idx_tokens_user (user_id),
    CONSTRAINT fk_tokens_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------------------------------------------
-- 4. HELPER SKILLS TABLE
-- Categorized skills claimed by users (e.g. Groceries, Repairs, Pet Care, Tutoring)
-- ------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS helper_skills (
    id VARCHAR(64) NOT NULL PRIMARY KEY,
    user_id VARCHAR(64) NOT NULL,
    skill_name VARCHAR(64) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uq_user_skill (user_id, skill_name),
    INDEX idx_skills_name (skill_name),
    CONSTRAINT fk_skills_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------------------------------------------
-- 5. HELP REQUESTS TABLE
-- Real community help postings created by requesters.
-- ------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS help_requests (
    id VARCHAR(64) NOT NULL PRIMARY KEY,
    requester_id VARCHAR(64) NOT NULL,
    title VARCHAR(255) NOT NULL,
    category VARCHAR(64) NOT NULL,
    description TEXT NOT NULL,
    location VARCHAR(255) NOT NULL,
    distance VARCHAR(64) NOT NULL DEFAULT 'Nearby',
    requested_time VARCHAR(128) NOT NULL,
    is_urgent TINYINT(1) NOT NULL DEFAULT 0,
    status ENUM('OPEN', 'IN_PROGRESS', 'PENDING_VERIFICATION', 'COMPLETED', 'CANCELLED') NOT NULL DEFAULT 'OPEN',
    assigned_helper_id VARCHAR(64) NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_requests_status (status),
    INDEX idx_requests_category (category),
    INDEX idx_requests_requester (requester_id),
    INDEX idx_requests_helper (assigned_helper_id),
    CONSTRAINT fk_requests_requester FOREIGN KEY (requester_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_requests_helper FOREIGN KEY (assigned_helper_id) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------------------------------------------
-- 6. REQUEST ASSIGNMENTS TABLE
-- Helper assignments with status tracking and lifecycle timestamps.
-- ------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS request_assignments (
    id VARCHAR(64) NOT NULL PRIMARY KEY,
    request_id VARCHAR(64) NOT NULL,
    helper_id VARCHAR(64) NOT NULL,
    status ENUM('ASSIGNED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED') NOT NULL DEFAULT 'ASSIGNED',
    assigned_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    started_at TIMESTAMP NULL,
    completed_at TIMESTAMP NULL,
    INDEX idx_assignments_request (request_id),
    INDEX idx_assignments_helper (helper_id),
    CONSTRAINT fk_assignments_request FOREIGN KEY (request_id) REFERENCES help_requests(id) ON DELETE CASCADE,
    CONSTRAINT fk_assignments_helper FOREIGN KEY (helper_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------------------------------------------
-- 7. TASK STATUS HISTORY (Audit Trail)
-- Immutable event log for lifecycle state changes.
-- ------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS task_status_history (
    id VARCHAR(64) NOT NULL PRIMARY KEY,
    request_id VARCHAR(64) NOT NULL,
    changed_by_user_id VARCHAR(64) NOT NULL,
    old_status VARCHAR(32) NOT NULL,
    new_status VARCHAR(32) NOT NULL,
    notes TEXT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_history_request (request_id),
    CONSTRAINT fk_history_request FOREIGN KEY (request_id) REFERENCES help_requests(id) ON DELETE CASCADE,
    CONSTRAINT fk_history_user FOREIGN KEY (changed_by_user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------------------------------------------
-- 8. TASK COMPLETION EVIDENCE TABLE
-- Notes and photo evidence submitted by helper upon task completion.
-- ------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS task_completion_evidence (
    id VARCHAR(64) NOT NULL PRIMARY KEY,
    request_id VARCHAR(64) NOT NULL,
    helper_id VARCHAR(64) NOT NULL,
    notes TEXT NOT NULL,
    proof_photo_url VARCHAR(255) NULL,
    submitted_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_evidence_request (request_id),
    CONSTRAINT fk_evidence_request FOREIGN KEY (request_id) REFERENCES help_requests(id) ON DELETE CASCADE,
    CONSTRAINT fk_evidence_helper FOREIGN KEY (helper_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------------------------------------------
-- 9. RATINGS & REVIEWS TABLE
-- Completed task reviews submitted by requesters.
-- UNIQUE constraint on (request_id, reviewer_id) prevents duplicate rating exploit.
-- ------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS ratings (
    id VARCHAR(64) NOT NULL PRIMARY KEY,
    request_id VARCHAR(64) NOT NULL,
    reviewer_id VARCHAR(64) NOT NULL,
    helper_id VARCHAR(64) NOT NULL,
    rating INT NOT NULL CHECK (rating BETWEEN 1 AND 5),
    review_text TEXT NULL,
    tags VARCHAR(255) NULL, -- Comma-separated praise tags (e.g. "Super fast, Very polite")
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uq_request_reviewer (request_id, reviewer_id),
    INDEX idx_ratings_helper (helper_id),
    CONSTRAINT fk_ratings_request FOREIGN KEY (request_id) REFERENCES help_requests(id) ON DELETE CASCADE,
    CONSTRAINT fk_ratings_reviewer FOREIGN KEY (reviewer_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_ratings_helper FOREIGN KEY (helper_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------------------------------------------
-- 10. COMMUNITY POINTS LEDGER TABLE
-- Single source of truth for point accrual.
-- UNIQUE constraint prevents awarding points repeatedly for the same task.
-- ------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS community_points_ledger (
    id VARCHAR(64) NOT NULL PRIMARY KEY,
    user_id VARCHAR(64) NOT NULL,
    request_id VARCHAR(64) NULL,
    points INT NOT NULL,
    reason VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uq_user_request_reason (user_id, request_id, reason),
    INDEX idx_ledger_user (user_id),
    CONSTRAINT fk_ledger_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_ledger_request FOREIGN KEY (request_id) REFERENCES help_requests(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------------------------------------------
-- 11. CHAT CONVERSATIONS TABLE
-- 1-on-1 dialogue channels between requesters and helpers.
-- ------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS chat_conversations (
    id VARCHAR(64) NOT NULL PRIMARY KEY,
    request_id VARCHAR(64) NULL,
    participant1_id VARCHAR(64) NOT NULL,
    participant2_id VARCHAR(64) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_conv_part1 (participant1_id),
    INDEX idx_conv_part2 (participant2_id),
    INDEX idx_conv_request (request_id),
    CONSTRAINT fk_conv_part1 FOREIGN KEY (participant1_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_conv_part2 FOREIGN KEY (participant2_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_conv_request FOREIGN KEY (request_id) REFERENCES help_requests(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------------------------------------------
-- 12. CHAT MESSAGES TABLE
-- Persistent conversation message history with stable IDs.
-- ------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS chat_messages (
    id VARCHAR(64) NOT NULL PRIMARY KEY,
    conversation_id VARCHAR(64) NOT NULL,
    sender_id VARCHAR(64) NOT NULL,
    message_text TEXT NOT NULL,
    attachment_code VARCHAR(64) NULL,
    attachment_title VARCHAR(128) NULL,
    is_read TINYINT(1) NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_messages_conv (conversation_id, created_at),
    CONSTRAINT fk_messages_conv FOREIGN KEY (conversation_id) REFERENCES chat_conversations(id) ON DELETE CASCADE,
    CONSTRAINT fk_messages_sender FOREIGN KEY (sender_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------------------------------------------
-- 13. NOTIFICATIONS TABLE
-- Real-time notification queue for user events.
-- ------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS notifications (
    id VARCHAR(64) NOT NULL PRIMARY KEY,
    user_id VARCHAR(64) NOT NULL,
    title VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    type VARCHAR(64) NOT NULL, -- REQUEST_ACCEPTED, NEW_MESSAGE, TASK_UPDATE, SAFETY_ALERT, COMMUNITY_NEWS
    is_read TINYINT(1) NOT NULL DEFAULT 0,
    related_request_id VARCHAR(64) NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_notif_user_unread (user_id, is_read, created_at DESC),
    CONSTRAINT fk_notif_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------------------------------------------
-- 14. SOS ALERTS TABLE
-- Emergency security alerts with geographic positioning.
-- ------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS sos_alerts (
    id VARCHAR(64) NOT NULL PRIMARY KEY,
    user_id VARCHAR(64) NOT NULL,
    latitude DECIMAL(10, 8) NULL,
    longitude DECIMAL(11, 8) NULL,
    address VARCHAR(255) NOT NULL DEFAULT 'Maplewood District',
    alert_type VARCHAR(64) NOT NULL DEFAULT 'EMERGENCY_SOS', -- EMERGENCY_SOS, SUSPICIOUS_ACTIVITY, HAZARD
    status ENUM('ACTIVE', 'RESOLVED', 'CANCELLED') NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_sos_status (status),
    CONSTRAINT fk_sos_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

