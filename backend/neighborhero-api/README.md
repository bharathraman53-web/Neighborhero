# NeighborHero PHP + MySQL API Backend (XAMPP)

Functional, secure REST API backend built for the NeighborHero Android application using PHP 8.2+ with PDO, prepared statements, database transactions, input validation, bearer token authentication, and MySQL/MariaDB.

---

## 1. Prerequisites
- **XAMPP** (Apache 2.4+ and MySQL 8.0+ / MariaDB 10.4+) installed on Windows (default path: `C:\xampp`).
- PHP 8.1 or higher (included in modern XAMPP distributions).
- Android Studio with Android Emulator or a physical device connected to the same LAN.

---

## 2. Quick Setup Instructions

### Step 1: Start Apache and MySQL in XAMPP
1. Open the **XAMPP Control Panel** (`C:\xampp\xampp-control.exe`).
2. Click **Start** next to **Apache**.
3. Click **Start** next to **MySQL**.
4. Verify both display green indicators.

### Step 2: Configure Virtual Host or Link into `htdocs`
To serve the API directly via Apache, choose either option:

#### Option A: Directory Symlink or Copy to `htdocs` (Recommended)
Open PowerShell as Administrator and run:
```powershell
New-Item -ItemType SymbolicLink -Path "C:\xampp\htdocs\neighborhero-api" -Target "C:\Users\SCAD LAB\AndroidStudioProjects\Neighborhero3\backend\neighborhero-api\public"
```
Or start the built-in PHP development server on all interfaces:
```powershell
& "C:\xampp\php\php.exe" -S 0.0.0.0:8000 -t "C:\Users\SCAD LAB\AndroidStudioProjects\Neighborhero3\backend\neighborhero-api\public"
```

### Step 3: Create the MySQL Database & Import Schema
1. Open **phpMyAdmin** in your browser at [http://localhost/phpmyadmin](http://localhost/phpmyadmin) OR use the MySQL command line:
```powershell
& "C:\xampp\mysql\bin\mysql.exe" -u root -e "CREATE DATABASE IF NOT EXISTS neighborhero_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
```
2. Import the schema:
```powershell
& "C:\xampp\mysql\bin\mysql.exe" -u root neighborhero_db < "C:\Users\SCAD LAB\AndroidStudioProjects\Neighborhero3\backend\neighborhero-api\database\schema.sql"
```
*(Optional)* Seed development sample data for initial exploration:
```powershell
& "C:\xampp\php\php.exe" "C:\Users\SCAD LAB\AndroidStudioProjects\Neighborhero3\backend\neighborhero-api\database\seed_demo_data.php"
```

### Step 4: Configure Database Credentials
A safe local configuration file `config/local.database.php` can be created (already excluded from Git):
```php
<?php
return [
    'driver' => 'mysql',
    'host' => '127.0.0.1',
    'port' => 3306,
    'database' => 'neighborhero_db',
    'username' => 'root',
    'password' => '', // Default for XAMPP is blank
];
```

---

## 3. Verify Health Endpoint
Open a terminal or browser and test the health endpoint:
```powershell
curl http://localhost:8000/api/health
```
Or via XAMPP Apache:
```powershell
curl http://localhost/neighborhero-api/api/health
```
Response:
```json
{
  "status": "healthy",
  "app": "NeighborHero API",
  "version": "1.0.0",
  "database": {
    "status": "connected",
    "driver": "mysql"
  },
  "server_time_utc": "2026-10-28 12:45:00"
}
```

---

## 4. Connecting Android to the Backend

### A. Android Emulator
The Android emulator accesses the host machine through `10.0.2.2`.
- When using PHP built-in server (`-S 0.0.0.0:8000`):
  `http://10.0.2.2:8000/api/`
- When using XAMPP Apache:
  `http://10.0.2.2/neighborhero-api/api/`

### B. Physical Android Device (Same Wi-Fi / LAN)
1. Find your development machine's local IP address:
```powershell
Get-NetIPAddress -AddressFamily IPv4 | Where-Object { $_.InterfaceAlias -like "*Wi-Fi*" -or $_.InterfaceAlias -like "*Ethernet*" }
```
2. Example LAN IP: `192.168.1.150`
3. Configure the base URL in the app's `ApiConfig.kt`:
  `http://192.168.1.150:8000/api/`
4. Ensure Windows Firewall permits incoming traffic on port 8000 or port 80.

### C. Production Deployment (HTTPS)
1. Host behind Apache/Nginx with a valid TLS certificate (e.g. Let's Encrypt).
2. Configure `ApiConfig.kt` to target `https://api.yourdomain.com/api/`.
3. Cleartext HTTP traffic is strictly restricted to debug builds via Android network security config.

---

## 5. Automated Backend Tests
Run the standalone in-memory automated test suite with PHP CLI:
```powershell
& "C:\xampp\php\php.exe" tests/run_tests.php
```
Runs 36 comprehensive tests covering:
- Database connectivity & health
- User registration, password hashing & login
- Input validation & malformed payload rejection
- Bearer token generation, hashing & revocation
- Help request creation, status filtering & pagination
- Task lifecycle transitions (`OPEN` -> `IN_PROGRESS` -> `PENDING_VERIFICATION` -> `COMPLETED`)
- Community points ledger (verifies duplicate completion points are strictly blocked)
- Chat persistence & sender/recipient authorization
- Notification queue & read states
- Emergency SOS dispatch & real task history

