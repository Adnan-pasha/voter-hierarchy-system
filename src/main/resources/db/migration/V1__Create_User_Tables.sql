-- ============================================================================
-- VOTER HIERARCHY SYSTEM - COMPLETE DATABASE SCHEMA
-- ============================================================================

-- Drop tables if exists (for clean setup)
DROP TABLE IF EXISTS user_roles;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS blo_details;
DROP TABLE IF EXISTS voter_details_current;
DROP TABLE IF EXISTS voter_details_2002;
DROP TABLE IF EXISTS person;
DROP TABLE IF EXISTS family;

-- ============================================================================
-- USER MANAGEMENT TABLES
-- ============================================================================

-- Users table
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(100),
    email VARCHAR(100),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME,
    updated_at DATETIME,
    last_login DATETIME,
    created_by VARCHAR(50),
    INDEX idx_username (username),
    INDEX idx_active (active),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- User roles table (many-to-many relationship)
CREATE TABLE user_roles (
    user_id BIGINT NOT NULL,
    role VARCHAR(20) NOT NULL,
    PRIMARY KEY (user_id, role),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_role (role)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- FAMILY AND PERSON TABLES
-- ============================================================================

-- Family table
CREATE TABLE family (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    family_code VARCHAR(50) UNIQUE NOT NULL,
    contact_person VARCHAR(100),
    contact_number VARCHAR(20),
    created_by VARCHAR(50),
    created_at DATETIME,
    updated_at DATETIME,
    INDEX idx_family_code (family_code),
    INDEX idx_contact_number (contact_number),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Person table (both family heads and members)
CREATE TABLE person (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    family_id BIGINT NOT NULL,
    is_family_head BOOLEAN DEFAULT FALSE,
    relation_type VARCHAR(20),
    age INT,
    status VARCHAR(20) NOT NULL,
    created_at DATETIME,
    updated_at DATETIME,
    FOREIGN KEY (family_id) REFERENCES family(id) ON DELETE CASCADE,
    INDEX idx_family_id (family_id),
    INDEX idx_is_family_head (is_family_head),
    INDEX idx_status (status),
    INDEX idx_relation_type (relation_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- VOTER DETAILS TABLES
-- ============================================================================

-- 2002 Voter Details table
CREATE TABLE voter_details_2002 (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    person_id BIGINT UNIQUE NOT NULL,
    name VARCHAR(100) NOT NULL,
    parent_spouse_name VARCHAR(100) NOT NULL,
    epic_no VARCHAR(50) NOT NULL,
    ac_no VARCHAR(20) NOT NULL,
    part_no VARCHAR(20) NOT NULL,
    serial_no VARCHAR(20) NOT NULL,
    FOREIGN KEY (person_id) REFERENCES person(id) ON DELETE CASCADE,
    INDEX idx_name (name),
    INDEX idx_epic_no (epic_no),
    INDEX idx_ac_part_serial (ac_no, part_no, serial_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Current Voter Details table
CREATE TABLE voter_details_current (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    person_id BIGINT UNIQUE NOT NULL,
    name VARCHAR(100) NOT NULL,
    parent_spouse_name VARCHAR(100) NOT NULL,
    epic_no VARCHAR(50) NOT NULL,
    ac_no VARCHAR(20) NOT NULL,
    part_no VARCHAR(20) NOT NULL,
    serial_no VARCHAR(20) NOT NULL,
    FOREIGN KEY (person_id) REFERENCES person(id) ON DELETE CASCADE,
    INDEX idx_name (name),
    INDEX idx_parent_spouse_name (parent_spouse_name),
    INDEX idx_epic_no (epic_no),
    INDEX idx_ac_part_serial (ac_no, part_no, serial_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- BLO Details table
CREATE TABLE blo_details (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    voter_details_current_id BIGINT UNIQUE NOT NULL,
    blo_name VARCHAR(100),
    blo_mobile VARCHAR(15),
    FOREIGN KEY (voter_details_current_id) REFERENCES voter_details_current(id) ON DELETE CASCADE,
    INDEX idx_blo_name (blo_name),
    INDEX idx_blo_mobile (blo_mobile)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- INITIAL DATA (Optional - Default Users)
-- ============================================================================

-- Insert default admin user (password: admin123)
-- BCrypt hash of "admin123"
INSERT INTO users (username, password, full_name, email, active, created_at, updated_at, created_by)
VALUES (
    'admin',
    '$2a$10$N9qo8uLOickgx2ZMRZoMye7I62LAMqrnVR1HJcM/jEqkKUb0H2OHC',
    'System Administrator',
    'admin@voterhierarchy.com',
    TRUE,
    NOW(),
    NOW(),
    'SYSTEM'
);

-- Assign ADMIN role to admin user
INSERT INTO user_roles (user_id, role)
VALUES (LAST_INSERT_ID(), 'ADMIN');

-- Insert default operator user (password: operator123)
-- BCrypt hash of "operator123"
INSERT INTO users (username, password, full_name, email, active, created_at, updated_at, created_by)
VALUES (
    'operator',
    '$2a$10$xvZKUU5U2Oi0CkpQ5LJQQeE5QqPLPx7XqKpX5vqCKqJEb8Y2QbCE2',
    'Data Entry Operator',
    'operator@voterhierarchy.com',
    TRUE,
    NOW(),
    NOW(),
    'SYSTEM'
);

-- Assign OPERATOR role to operator user
INSERT INTO user_roles (user_id, role)
VALUES (LAST_INSERT_ID(), 'OPERATOR');

-- Insert default viewer user (password: viewer123)
-- BCrypt hash of "viewer123"
INSERT INTO users (username, password, full_name, email, active, created_at, updated_at, created_by)
VALUES (
    'viewer',
    '$2a$10$rLJQKU5U2Oi0CkpQ5LJQQeE5QqPLPx7XqKpX5vqCKqJEb8Y2QbCF3',
    'Read-Only Viewer',
    'viewer@voterhierarchy.com',
    TRUE,
    NOW(),
    NOW(),
    'SYSTEM'
);

-- Assign VIEWER role to viewer user
INSERT INTO user_roles (user_id, role)
VALUES (LAST_INSERT_ID(), 'VIEWER');

-- ============================================================================
-- USEFUL QUERIES FOR VERIFICATION
-- ============================================================================

-- Check all users and their roles
-- SELECT u.id, u.username, u.full_name, GROUP_CONCAT(ur.role) as roles, u.active
-- FROM users u
-- LEFT JOIN user_roles ur ON u.id = ur.user_id
-- GROUP BY u.id;

-- Check family count by status
-- SELECT p.status, COUNT(DISTINCT f.id) as family_count
-- FROM family f
-- JOIN person p ON p.family_id = f.id AND p.is_family_head = TRUE
-- GROUP BY p.status;

-- Check total members across all families
-- SELECT COUNT(*) as total_members FROM person;

-- ============================================================================
-- PERFORMANCE OPTIMIZATION NOTES
-- ============================================================================

/*
Index Strategy:
1. Primary Keys: Auto-indexed by MySQL
2. Foreign Keys: Indexed for JOIN performance
3. Search Fields: name, epic_no, family_code, username
4. Filter Fields: status, active, is_family_head
5. Composite: Multi-column WHERE clauses

Query Optimization:
- Use EXPLAIN to analyze slow queries
- Composite indexes for common multi-field searches
- Covering indexes for frequently selected columns
- Regular ANALYZE TABLE to update statistics

Maintenance:
- Weekly: OPTIMIZE TABLE for fragmentation
- Monthly: Review slow query log
- Quarterly: Review and update indexes based on usage patterns
*/