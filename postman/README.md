# NeighborHero REST API — Postman Collection & Testing Guide

This directory contains the official Postman collection and environment definitions for the **NeighborHero** PHP + MySQL backend (running under XAMPP Apache / MariaDB).

---

## 1. Files Included

- **Collection:** `postman/collections/NeighborHero REST API.postman_collection.json`
  - Compliant with **Postman Collection Format v2.1.0**
  - **12 Folders**, **38 API Requests**
  - Comprehensive automated test scripts and pre-request logic
  - Automatic Bearer token extraction and ID propagation
- **Environment:** `postman/environments/NeighborHero Local.postman_environment.json`
  - Configured for local PC development LAN IP `http://172.23.48.169/neighborhero-api/public/api`
  - Safe placeholders for credentials and dynamic variables

---

## 2. Importing into Postman

1. Open Postman on your desktop or browser.
2. Click the **Import** button in the top-left corner of the workspace.
3. Select or drag-and-drop both files:
   - `postman/collections/NeighborHero REST API.postman_collection.json`
   - `postman/environments/NeighborHero Local.postman_environment.json`
4. In the top-right environment dropdown, select **NeighborHero Local**.

---

## 3. Environment Variables Reference

| Variable | Default / Placeholder | Description |
|---|---|---|
| `baseUrl` | `http://172.23.48.169/neighborhero-api/public/api` | Base URL of the NeighborHero API (no trailing slash). |
| `token` | *(empty)* | Active 64-character Bearer token. Auto-populated on login/registration. |
| `testEmail` | `testuser@example.com` | Test email for authentication. Replace if desired. |
| `testPassword` | `Password123!` | Test password (encrypted using bcrypt on server). |
| `testPhone` | `5551234567` | Test 10-digit phone number (digits only, strictly validated). |
| `requestId` | *(empty)* | Active help request ID. Auto-set when creating or listing requests. |
| `taskId` | *(empty)* | Active task ID. Synced with `requestId` for lifecycle tests. |
| `helperId` | *(empty)* | Active helper user ID. Auto-populated from Helper Discovery. |
| `conversationId` | *(empty)* | Active conversation ID. Auto-populated from Chat requests. |
| `notificationId` | *(empty)* | Notification ID. Auto-populated from Notifications folder. |
| `sosAlertId` | *(empty)* | Emergency SOS alert ID. Auto-populated when triggering SOS. |

---

## 4. Authentication Automation Flow

The collection uses **Collection-level Bearer Token Authorization** (`{{token}}`). All authenticated requests inherit this token automatically.

### Public Endpoints (No Token Required)
- `1. Health Check / API Health & DB Connectivity`
- `2. Authentication / Register New User`
- `2. Authentication / Register - Validation Error: 9-Digit Phone`
- `2. Authentication / Login - Valid Credentials`
- `2. Authentication / Login - Invalid Credentials (401)`
- `3. User Profile / Get Profile - Missing Token (401)`

### Automatic Token Capture
When you execute `Login - Valid Credentials` (or `Register New User` for a new account), Postman's test script automatically extracts the Bearer token from the response:
```javascript
var jsonData = pm.response.json();
pm.environment.set("token", jsonData.data.token);
```
All subsequent authenticated requests in folders 3 through 12 will immediately use this token.

---

## 5. End-to-End Workflow & Folder Guide

### 1. Health Check
- Run **API Health & DB Connectivity** (`GET {{baseUrl}}/health`).
- Confirms Apache web server is running and MySQL database `neighborhero_db` is connected (`"status": "healthy"`).

### 2. Authentication
- Run **Login - Valid Credentials** (`POST {{baseUrl}}/auth/login`).
  - Uses `{{testEmail}}` and `{{testPassword}}`.
  - Captures `token` into environment.
- Test **Register - Validation Error: 9-Digit Phone** (`POST {{baseUrl}}/auth/register`) to verify HTTP 422 validation.
- Test **Login - Invalid Credentials** (`POST {{baseUrl}}/auth/login`) to verify HTTP 401 constant-time security handling.

### 3. User Profile
- Run **Get Current User Profile** (`GET {{baseUrl}}/auth/me`).
  - Verifies user identity, verified badge, points, and rating.
- Run **Update User Profile Information** (`PUT {{baseUrl}}/auth/profile`).
  - Updates bio, neighborhood, and skills array.

### 4. Profile Photo Upload
- Select **Upload Profile Photo** (`POST {{baseUrl}}/auth/profile/photo`).
- **How to attach a test photo in Postman:**
  1. Click on the **Body** tab.
  2. Notice the `photo` key is configured as `File`.
  3. Hover over the **Value** cell for `photo` and click **Select Files**.
  4. Choose any local `.jpg`, `.png`, or `.webp` file under 5 MB.
  5. Click **Send**.
  6. The test script verifies `200 OK` and saves `avatar_url` into the user record.

### 5. Help Requests
- Run **Create New Help Request** (`POST {{baseUrl}}/requests`).
  - Creates a new request in `OPEN` status.
  - Automatically captures the newly created ID into `{{requestId}}` and `{{taskId}}`.
- Run **List Help Requests** (`GET {{baseUrl}}/requests`).
  - Supports optional query parameters: `category` (e.g. `Groceries`, `Repairs`), `status` (e.g. `OPEN`), `page`, `limit`.
- Run **Get Help Request Details by ID** (`GET {{baseUrl}}/requests/{{requestId}}`).
- Run **List My Posted Requests** & **List My Accepted Requests**.

### 6. Helper Discovery
- Run **Discover Recommended Helpers** (`GET {{baseUrl}}/helpers`).
  - Lists community helpers with rating, tasks done, and skills. Automatically populates `{{helperId}}`.
- Run **Get Community Helper Leaderboard** (`GET {{baseUrl}}/helpers/leaderboard`).
  - Returns top champions sorted by verified points and tasks completed.
- Run **Get Helper Profile Details by ID** (`GET {{baseUrl}}/helpers/{{helperId}}`).

### 7. Task Management
Follow the real community task lifecycle:
1. **Assign Helper to Task** (`POST {{baseUrl}}/tasks/{{taskId}}/assign`):
   - Transitions task status from `OPEN` to `IN_PROGRESS`.
2. **Confirm Task Start** (`POST {{baseUrl}}/tasks/{{taskId}}/confirm-start`):
   - Confirms the helper has arrived and begun work.
3. **Submit Task Completion** (`POST {{baseUrl}}/tasks/{{taskId}}/submit-completion`):
   - Helper submits completion notes and optional proof photo reference.
   - Transitions status to `PENDING_VERIFICATION`.
4. **Verify Task Completion & Award Points** (`POST {{baseUrl}}/tasks/{{taskId}}/verify-completion`):
   - Requester verifies completion, awards 1-5 star rating and feedback.
   - Transitions status to `COMPLETED` and awards 150 points to the helper. Server prevents duplicate point awards.

### 8. Task Completion Evidence
- Run **Upload Task Evidence Photo** (`POST {{baseUrl}}/tasks/{{taskId}}/evidence/photo`):
  - In the **Body** tab, select a test proof photo for the `photo` field.
  - Uploaded images are stored in `uploads/evidence/` where direct web access is blocked by `.htaccess` (HTTP 403).
- Run **Get Task Evidence Metadata & Notes** (`GET {{baseUrl}}/tasks/{{taskId}}/evidence`):
  - Accessible strictly by the task requester and assigned helper. Non-participants receive HTTP 403 Forbidden.
- Run **Stream Task Evidence Photo** (`GET {{baseUrl}}/tasks/{{taskId}}/evidence/photo`):
  - Returns raw image stream with `Content-Type: image/jpeg` after validating participant authorization.
- Run **Evidence Access - Unauthorized Third Party Blocked** (`GET {{baseUrl}}/tasks/{{taskId}}/evidence`):
  - Verifies access control security for non-participants.

### 9. Chat and Conversations
- Run **List User Active Conversations** (`GET {{baseUrl}}/chat/conversations`):
  - Returns active 1-to-1 conversation threads with unread counters. Populates `{{conversationId}}`.
- Run **Get Conversation Messages** (`GET {{baseUrl}}/chat/messages`):
  - Supports `conversation_id`, `recipient_id`, `since_id` (polling), and `limit`.
- Run **Send Chat Message** (`POST {{baseUrl}}/chat/messages`):
  - Sends a private message and automatically triggers in-app notification to the recipient.
- Run **Send Duplicate Message - Idempotency Check**:
  - Uses fixed `client_message_id`. Verifies server deduplication (no duplicate rows in MySQL).
- Run **Send Message Without Recipient - Rejection (400)**:
  - Verifies that broadcast spam is blocked (every message must specify a recipient or conversation).

### 10. Notifications
- Run **Get User Notifications** (`GET {{baseUrl}}/notifications`):
  - Fetches in-app alerts (task updates, chat notifications, safety alerts). Populates `{{notificationId}}`.
- Run **Mark Single Notification as Read** (`POST {{baseUrl}}/notifications/{{notificationId}}/read`).
- Run **Mark All Notifications as Read** (`POST {{baseUrl}}/notifications/read-all`).

### 11. Safety and SOS
- Run **Trigger Emergency SOS Signal** (`POST {{baseUrl}}/safety/sos`):
  - Dispatches emergency alert with GPS coordinates and address. Populates `{{sosAlertId}}`.
- Run **Resolve Emergency SOS Signal** (`POST {{baseUrl}}/safety/sos/{{sosAlertId}}/resolve`):
  - Marks the emergency alert as `RESOLVED`.

### 12. Task History
- Run **Get User Task History & Service Records** (`GET {{baseUrl}}/safety/task-history`):
  - Returns chronological history of completed and cancelled tasks for the user.

---

## 6. Running with Postman Collection Runner or Newman

### Using Postman Collection Runner:
1. Select the **NeighborHero REST API** collection.
2. Click **Run collection** at the top.
3. Select the **NeighborHero Local** environment.
4. Uncheck photo upload requests if running automated batch tests without files attached, or keep them to verify their 400 validation checks.
5. Click **Run NeighborHero REST API**.

### Using Newman (CLI):
```bash
newman run "postman/collections/NeighborHero REST API.postman_collection.json" \
  --environment "postman/environments/NeighborHero Local.postman_environment.json" \
  --reporters cli,json
```

