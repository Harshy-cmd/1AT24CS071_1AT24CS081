-- ============================================================
-- PROJECT  : Complaint Management System
-- FILE     : complaint_management.sql
-- VERSION  : 1.0.0
-- AUTHOR   : CMS Development Team
-- DATE     : 2024
--
-- DESCRIPTION:
--   Complete database schema for the Complaint Management
--   System desktop application. Creates all tables with
--   constraints, indexes, foreign keys, and sample data.
--
-- HOW TO RUN:
--   mysql -u root -p < complaint_management.sql
--
-- DEFAULT CREDENTIALS:
--   Admin    → username: admin      / password: Admin@123
--   Admin2   → username: rtaylor    / password: Admin@123
--   Employee → username: jsmith     / password: Emp@123
--              (and all other employees)
-- ============================================================

-- ============================================================
-- STEP 1: Create (or re-create) the database
-- ============================================================
DROP DATABASE IF EXISTS complaint_management;

CREATE DATABASE complaint_management
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE complaint_management;

-- ============================================================
-- STEP 2: Table — users
--
-- Stores all system users: Admins, Employees, and Citizens.
-- Roles: ADMIN | EMPLOYEE | CITIZEN
-- ============================================================
CREATE TABLE users (
    user_id         INT             NOT NULL AUTO_INCREMENT,
    full_name       VARCHAR(100)    NOT NULL,
    username        VARCHAR(50)     NOT NULL,
    password_hash   CHAR(64)        NOT NULL COMMENT 'SHA-256 hex digest',
    email           VARCHAR(150)    NOT NULL,
    phone           VARCHAR(20),
    role            ENUM('ADMIN','EMPLOYEE','CITIZEN') NOT NULL DEFAULT 'CITIZEN',
    department      VARCHAR(100),
    is_active       TINYINT(1)      NOT NULL DEFAULT 1,
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_login      DATETIME,

    CONSTRAINT pk_users         PRIMARY KEY (user_id),
    CONSTRAINT uq_users_uname   UNIQUE (username),
    CONSTRAINT uq_users_email   UNIQUE (email),
    CONSTRAINT chk_full_name    CHECK (CHAR_LENGTH(TRIM(full_name)) >= 2),
    CONSTRAINT chk_username     CHECK (CHAR_LENGTH(TRIM(username)) >= 3)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = 'System users — admins, employees, and citizens';

CREATE INDEX idx_users_role       ON users (role);
CREATE INDEX idx_users_department ON users (department);
CREATE INDEX idx_users_is_active  ON users (is_active);

-- ============================================================
-- STEP 3: Table — complaints
--
-- Core table: every registered complaint lives here.
-- ============================================================
CREATE TABLE complaints (
    complaint_id        INT             NOT NULL AUTO_INCREMENT,
    complaint_number    VARCHAR(20)     NOT NULL
                            COMMENT 'Human-readable ID, e.g. CMS-2024-0001',
    title               VARCHAR(200)    NOT NULL,
    description         TEXT            NOT NULL,
    category            ENUM('ELECTRICITY','WATER','ROAD','GARBAGE',
                              'SECURITY','INTERNET','HEALTH',
                              'EDUCATION','OTHERS')      NOT NULL,
    priority            ENUM('LOW','MEDIUM','HIGH','CRITICAL')
                                        NOT NULL DEFAULT 'MEDIUM',
    status              ENUM('PENDING','ASSIGNED','IN_PROGRESS',
                              'ON_HOLD','RESOLVED','CLOSED')
                                        NOT NULL DEFAULT 'PENDING',
    location            VARCHAR(200),
    department          VARCHAR(100),
    assigned_to         INT             COMMENT 'FK → users.user_id',
    created_by          INT             NOT NULL COMMENT 'FK → users.user_id',
    date_created        DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_updated        DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP
                            ON UPDATE CURRENT_TIMESTAMP,
    resolution_date     DATE,
    remarks             TEXT,

    CONSTRAINT pk_complaints
        PRIMARY KEY (complaint_id),
    CONSTRAINT uq_complaint_number
        UNIQUE (complaint_number),
    CONSTRAINT fk_complaints_assigned_to
        FOREIGN KEY (assigned_to)
        REFERENCES users (user_id)
        ON DELETE SET NULL
        ON UPDATE CASCADE,
    CONSTRAINT fk_complaints_created_by
        FOREIGN KEY (created_by)
        REFERENCES users (user_id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,
    CONSTRAINT chk_title_length
        CHECK (CHAR_LENGTH(TRIM(title)) >= 5),
    CONSTRAINT chk_desc_length
        CHECK (CHAR_LENGTH(TRIM(description)) >= 10)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = 'All citizen / staff complaints';

CREATE INDEX idx_cmp_status      ON complaints (status);
CREATE INDEX idx_cmp_priority    ON complaints (priority);
CREATE INDEX idx_cmp_category    ON complaints (category);
CREATE INDEX idx_cmp_created_by  ON complaints (created_by);
CREATE INDEX idx_cmp_assigned_to ON complaints (assigned_to);
CREATE INDEX idx_cmp_created_dt  ON complaints (date_created);
CREATE INDEX idx_cmp_department  ON complaints (department);

-- ============================================================
-- STEP 4: Table — complaint_history
--
-- Immutable audit trail: every status change is recorded.
-- ============================================================
CREATE TABLE complaint_history (
    history_id      INT     NOT NULL AUTO_INCREMENT,
    complaint_id    INT     NOT NULL,
    changed_by      INT     NOT NULL,
    old_status      ENUM('PENDING','ASSIGNED','IN_PROGRESS',
                          'ON_HOLD','RESOLVED','CLOSED'),
    new_status      ENUM('PENDING','ASSIGNED','IN_PROGRESS',
                          'ON_HOLD','RESOLVED','CLOSED') NOT NULL,
    change_date     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    remarks         TEXT,

    CONSTRAINT pk_history
        PRIMARY KEY (history_id),
    CONSTRAINT fk_hist_complaint
        FOREIGN KEY (complaint_id)
        REFERENCES complaints (complaint_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    CONSTRAINT fk_hist_changed_by
        FOREIGN KEY (changed_by)
        REFERENCES users (user_id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = 'Immutable status-change audit trail per complaint';

CREATE INDEX idx_hist_complaint_id ON complaint_history (complaint_id);
CREATE INDEX idx_hist_change_date  ON complaint_history (change_date);
CREATE INDEX idx_hist_changed_by   ON complaint_history (changed_by);

-- ============================================================
-- STEP 5: Table — activity_log
--
-- General audit log for all significant user actions.
-- ============================================================
CREATE TABLE activity_log (
    log_id          INT          NOT NULL AUTO_INCREMENT,
    user_id         INT          COMMENT 'NULL if user was deleted',
    action          VARCHAR(100) NOT NULL,
    description     TEXT,
    entity_type     VARCHAR(50)  COMMENT 'e.g. COMPLAINT, USER, REPORT',
    entity_id       INT          COMMENT 'PK of the affected entity',
    log_timestamp   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_activity_log
        PRIMARY KEY (log_id),
    CONSTRAINT fk_log_user
        FOREIGN KEY (user_id)
        REFERENCES users (user_id)
        ON DELETE SET NULL
        ON UPDATE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = 'General system-wide audit log';

CREATE INDEX idx_log_user_id   ON activity_log (user_id);
CREATE INDEX idx_log_timestamp ON activity_log (log_timestamp);
CREATE INDEX idx_log_action    ON activity_log (action);
CREATE INDEX idx_log_entity    ON activity_log (entity_type, entity_id);

-- ============================================================
-- STEP 6: Sample Data — users
--
-- Passwords are hashed by MySQL's SHA2() function.
-- Java side: MessageDigest("SHA-256") → hex string
-- ============================================================
INSERT INTO users
    (full_name, username, password_hash, email, phone, role, department, is_active)
VALUES
-- Admins
('System Administrator', 'admin',
    SHA2('Admin@123', 256),
    'admin@cms.local', '9000000001', 'ADMIN', 'IT Department', 1),

('Robert Taylor', 'rtaylor',
    SHA2('Admin@123', 256),
    'robert.taylor@cms.local', '9000000008', 'ADMIN', 'Administration', 1),

-- Employees
('John Smith', 'jsmith',
    SHA2('Emp@123', 256),
    'john.smith@cms.local', '9000000002', 'EMPLOYEE', 'Infrastructure', 1),

('Sarah Johnson', 'sjohnson',
    SHA2('Emp@123', 256),
    'sarah.johnson@cms.local', '9000000003', 'EMPLOYEE', 'Water Supply', 1),

('Michael Chen', 'mchen',
    SHA2('Emp@123', 256),
    'michael.chen@cms.local', '9000000004', 'EMPLOYEE', 'Electricity', 1),

('Emma Wilson', 'ewilson',
    SHA2('Emp@123', 256),
    'emma.wilson@cms.local', '9000000005', 'EMPLOYEE', 'Road Maintenance', 1),

('David Brown', 'dbrown',
    SHA2('Emp@123', 256),
    'david.brown@cms.local', '9000000006', 'EMPLOYEE', 'Sanitation', 1),

('Alice Martinez', 'amartinez',
    SHA2('Emp@123', 256),
    'alice.martinez@cms.local', '9000000007', 'EMPLOYEE', 'Security', 1),

-- Citizens
('Citizen User', 'citizen',
    SHA2('Citizen@123', 256),
    'citizen@cms.local', '9000000009', 'CITIZEN', NULL, 1);

-- ============================================================
-- STEP 7: Sample Data — complaints
-- ============================================================
INSERT INTO complaints
    (complaint_number, title, description, category, priority, status,
     location, department, assigned_to, created_by, date_created, remarks)
VALUES
('CMS-2024-0001',
 'Street Lights Not Working on Main Street',
 'Multiple street lights on Main Street Block A have been non-functional for 3 days causing safety concerns at night.',
 'ELECTRICITY', 'HIGH', 'RESOLVED',
 'Main Street, Block A', 'Electricity', 5, 1,
 DATE_SUB(NOW(), INTERVAL 15 DAY), 'All faulty lights replaced and tested.'),

('CMS-2024-0002',
 'Large Pothole on Highway 12 Near Junction',
 'A large and dangerous pothole has developed on Highway 12 near the KM 45 junction. Multiple vehicles have been damaged.',
 'ROAD', 'CRITICAL', 'IN_PROGRESS',
 'Highway 12, KM 45', 'Road Maintenance', 6, 1,
 DATE_SUB(NOW(), INTERVAL 12 DAY), NULL),

('CMS-2024-0003',
 'No Water Supply in Sector 4 for 2 Days',
 'Residents of Sector 4, Zone B have had no water supply for two consecutive days. Urgent resolution required.',
 'WATER', 'HIGH', 'ASSIGNED',
 'Sector 4, Zone B', 'Water Supply', 4, 2,
 DATE_SUB(NOW(), INTERVAL 10 DAY), NULL),

('CMS-2024-0004',
 'Garbage Not Collected Near Community Center',
 'The garbage bins near the Community Center in Zone A have not been collected for over a week. They are overflowing.',
 'GARBAGE', 'MEDIUM', 'PENDING',
 'Community Center, Zone A', 'Sanitation', NULL, 3,
 DATE_SUB(NOW(), INTERVAL 8 DAY), NULL),

('CMS-2024-0005',
 'Suspicious Individuals Near School After Hours',
 'Unidentified individuals have been observed loitering near City School premises after closing hours.',
 'SECURITY', 'HIGH', 'CLOSED',
 'City School, Block C', 'Security', 8, 2,
 DATE_SUB(NOW(), INTERVAL 7 DAY), 'Police patrol frequency increased. Issue resolved.'),

('CMS-2024-0006',
 'Complete Internet Outage in Office Block B',
 'The entire Office Block B on Floor 2 has had no internet connectivity since Monday morning affecting operations.',
 'INTERNET', 'MEDIUM', 'PENDING',
 'Office Block B, Floor 2', 'IT Department', NULL, 1,
 DATE_SUB(NOW(), INTERVAL 5 DAY), NULL),

('CMS-2024-0007',
 'Burst Water Pipeline in Residential Area 7',
 'A major water pipeline has burst near Residential Area 7 causing significant water wastage and road waterlogging.',
 'WATER', 'CRITICAL', 'IN_PROGRESS',
 'Residential Area 7, Main Pipeline', 'Water Supply', 4, 1,
 DATE_SUB(NOW(), INTERVAL 3 DAY), NULL),

('CMS-2024-0008',
 'Road Surface Damage After Heavy Rainfall',
 'Heavy rains have severely damaged the road surface in Zone C residential area making it dangerous for commuters.',
 'ROAD', 'HIGH', 'PENDING',
 'Zone C, Residential Area', 'Road Maintenance', NULL, 5,
 DATE_SUB(NOW(), INTERVAL 2 DAY), NULL),

('CMS-2024-0009',
 'Frequent Power Cuts and Fluctuations in Building 3',
 'Building 3 in Sector 2 has been experiencing frequent power cuts and dangerous electrical fluctuations daily.',
 'ELECTRICITY', 'HIGH', 'ASSIGNED',
 'Building 3, Sector 2', 'Electricity', 5, 3,
 DATE_SUB(NOW(), INTERVAL 1 DAY), NULL),

('CMS-2024-0010',
 'Community Health Center Severely Understaffed',
 'The community health center has been operating with only 1 doctor for 2 weeks. Patients are being turned away.',
 'HEALTH', 'CRITICAL', 'PENDING',
 'Community Health Center, Block D', 'Health', NULL, 1,
 NOW(), NULL);

-- ============================================================
-- STEP 8: Sample Data — complaint_history
-- ============================================================
INSERT INTO complaint_history
    (complaint_id, changed_by, old_status, new_status, change_date, remarks)
VALUES
(1, 1, 'PENDING',     'ASSIGNED',    DATE_SUB(NOW(), INTERVAL 14 DAY), 'Assigned to Michael Chen, Electricity dept.'),
(1, 5, 'ASSIGNED',    'IN_PROGRESS', DATE_SUB(NOW(), INTERVAL 13 DAY), 'Technician dispatched. Work started.'),
(1, 5, 'IN_PROGRESS', 'RESOLVED',    DATE_SUB(NOW(), INTERVAL 10 DAY), 'All 8 street lights replaced and tested successfully.'),

(2, 1, 'PENDING',     'ASSIGNED',    DATE_SUB(NOW(), INTERVAL 11 DAY), 'Assigned to Emma Wilson, Road Maintenance.'),
(2, 6, 'ASSIGNED',    'IN_PROGRESS', DATE_SUB(NOW(), INTERVAL 9 DAY),  'Road survey completed. Repair work in progress.'),

(3, 1, 'PENDING',     'ASSIGNED',    DATE_SUB(NOW(), INTERVAL 9 DAY),  'Assigned to Sarah Johnson, Water Supply.'),

(5, 1, 'PENDING',     'ASSIGNED',    DATE_SUB(NOW(), INTERVAL 6 DAY),  'Assigned to Alice Martinez, Security dept.'),
(5, 8, 'ASSIGNED',    'RESOLVED',    DATE_SUB(NOW(), INTERVAL 5 DAY),  'Police patrol increased. Area secured.'),
(5, 1, 'RESOLVED',    'CLOSED',      DATE_SUB(NOW(), INTERVAL 4 DAY),  'Verified resolved. Complaint closed.'),

(9, 1, 'PENDING',     'ASSIGNED',    DATE_SUB(NOW(), INTERVAL 1 DAY),  'Assigned to Michael Chen, Electricity dept.');

-- ============================================================
-- STEP 9: Sample Data — activity_log
-- ============================================================
INSERT INTO activity_log
    (user_id, action, description, entity_type, entity_id, log_timestamp)
VALUES
(1, 'LOGIN',            'System Administrator logged in.',           'USER',      1,    DATE_SUB(NOW(), INTERVAL 15 DAY)),
(1, 'CREATE_COMPLAINT', 'Created complaint CMS-2024-0001.',          'COMPLAINT', 1,    DATE_SUB(NOW(), INTERVAL 15 DAY)),
(1, 'ASSIGN_COMPLAINT', 'Assigned CMS-2024-0001 to Michael Chen.',   'COMPLAINT', 1,    DATE_SUB(NOW(), INTERVAL 14 DAY)),
(5, 'LOGIN',            'Michael Chen logged in.',                   'USER',      5,    DATE_SUB(NOW(), INTERVAL 13 DAY)),
(5, 'UPDATE_STATUS',    'Updated CMS-2024-0001 to IN_PROGRESS.',     'COMPLAINT', 1,    DATE_SUB(NOW(), INTERVAL 13 DAY)),
(5, 'UPDATE_STATUS',    'Updated CMS-2024-0001 to RESOLVED.',        'COMPLAINT', 1,    DATE_SUB(NOW(), INTERVAL 10 DAY)),
(1, 'CREATE_COMPLAINT', 'Created complaint CMS-2024-0002.',          'COMPLAINT', 2,    DATE_SUB(NOW(), INTERVAL 12 DAY)),
(1, 'ASSIGN_COMPLAINT', 'Assigned CMS-2024-0002 to Emma Wilson.',    'COMPLAINT', 2,    DATE_SUB(NOW(), INTERVAL 11 DAY)),
(1, 'CLOSE_COMPLAINT',  'Closed complaint CMS-2024-0005.',           'COMPLAINT', 5,    DATE_SUB(NOW(), INTERVAL 4 DAY)),
(1, 'EXPORT_CSV',       'Exported complaint report to CSV file.',    'REPORT',    NULL, DATE_SUB(NOW(), INTERVAL 2 DAY)),
(1, 'LOGIN',            'System Administrator logged in.',           'USER',      1,    NOW());

-- ============================================================
-- STEP 10: Useful Views (optional helpers)
-- ============================================================

-- View: All complaint details with assigned employee name
CREATE OR REPLACE VIEW v_complaint_details AS
SELECT
    c.complaint_id,
    c.complaint_number,
    c.title,
    c.category,
    c.priority,
    c.status,
    c.location,
    c.department,
    c.date_created,
    c.date_updated,
    c.resolution_date,
    c.remarks,
    creator.full_name  AS created_by_name,
    creator.username   AS created_by_username,
    assignee.full_name AS assigned_to_name,
    assignee.username  AS assigned_to_username
FROM complaints c
JOIN  users creator  ON c.created_by  = creator.user_id
LEFT JOIN users assignee ON c.assigned_to = assignee.user_id;

-- View: Dashboard statistics
CREATE OR REPLACE VIEW v_dashboard_stats AS
SELECT
    COUNT(*)                                         AS total_complaints,
    SUM(status = 'PENDING')                          AS pending,
    SUM(status = 'ASSIGNED')                         AS assigned,
    SUM(status = 'IN_PROGRESS')                      AS in_progress,
    SUM(status = 'ON_HOLD')                          AS on_hold,
    SUM(status = 'RESOLVED')                         AS resolved,
    SUM(status = 'CLOSED')                           AS closed,
    SUM(priority = 'CRITICAL')                       AS critical,
    SUM(DATE(date_created) = CURDATE())              AS today_total
FROM complaints;

-- ============================================================
-- STEP 11: Stored Procedures
-- ============================================================

DELIMITER //

-- Procedure to assign a complaint to an employee and record in history/activity
CREATE PROCEDURE sp_assign_complaint(
    IN p_complaint_id INT,
    IN p_employee_id INT,
    IN p_admin_id INT
)
BEGIN
    DECLARE v_old_status VARCHAR(20);
    DECLARE v_emp_name VARCHAR(100);
    
    START TRANSACTION;
    
    -- Get old status
    SELECT status INTO v_old_status FROM complaints WHERE complaint_id = p_complaint_id;
    
    -- Update complaint assignment
    UPDATE complaints 
    SET assigned_to = p_employee_id, status = 'ASSIGNED', date_updated = NOW() 
    WHERE complaint_id = p_complaint_id;
    
    -- Insert into history
    INSERT INTO complaint_history (complaint_id, changed_by, old_status, new_status, remarks)
    VALUES (p_complaint_id, p_admin_id, v_old_status, 'ASSIGNED', CONCAT('Assigned to employee ID: ', p_employee_id));
    
    -- Get employee name for log
    SELECT full_name INTO v_emp_name FROM users WHERE user_id = p_employee_id;
    
    -- Log activity
    INSERT INTO activity_log (user_id, action, description, entity_type, entity_id)
    VALUES (p_admin_id, 'ASSIGN_COMPLAINT', CONCAT('Assigned complaint ID ', p_complaint_id, ' to ', v_emp_name, '.'), 'COMPLAINT', p_complaint_id);
    
    COMMIT;
END //

-- Procedure to update complaint status with history and activity logging
CREATE PROCEDURE sp_update_complaint_status(
    IN p_complaint_id INT,
    IN p_user_id INT,
    IN p_new_status VARCHAR(20),
    IN p_remarks TEXT
)
BEGIN
    DECLARE v_old_status VARCHAR(20);
    DECLARE v_comp_num VARCHAR(20);
    
    START TRANSACTION;
    
    -- Get current status and number
    SELECT status, complaint_number INTO v_old_status, v_comp_num FROM complaints WHERE complaint_id = p_complaint_id;
    
    -- Update status, remarks and resolution date
    UPDATE complaints 
    SET status = p_new_status,
        remarks = IFNULL(p_remarks, remarks),
        resolution_date = CASE WHEN p_new_status IN ('RESOLVED','CLOSED') THEN CURDATE() ELSE resolution_date END,
        date_updated = NOW()
    WHERE complaint_id = p_complaint_id;
    
    -- Insert into history
    INSERT INTO complaint_history (complaint_id, changed_by, old_status, new_status, remarks)
    VALUES (p_complaint_id, p_user_id, v_old_status, p_new_status, p_remarks);
    
    -- Log activity
    INSERT INTO activity_log (user_id, action, description, entity_type, entity_id)
    VALUES (p_user_id, 'UPDATE_STATUS', CONCAT('Changed complaint ', v_comp_num, ' from ', v_old_status, ' to ', p_new_status, '.'), 'COMPLAINT', p_complaint_id);
    
    COMMIT;
END //

DELIMITER ;

-- ============================================================
-- STEP 12: Triggers
-- ============================================================

DELIMITER //

-- Trigger to automatically log complaint creation into activity_log
CREATE TRIGGER tr_after_complaint_insert
AFTER INSERT ON complaints
FOR EACH ROW
BEGIN
    INSERT INTO activity_log (user_id, action, description, entity_type, entity_id)
    VALUES (NEW.created_by, 'CREATE_COMPLAINT', CONCAT('Created complaint ', NEW.complaint_number, '.'), 'COMPLAINT', NEW.complaint_id);
END //

DELIMITER ;

-- ============================================================
-- END OF SCRIPT
-- ============================================================
SELECT 'Database complaint_management created successfully.' AS status;
