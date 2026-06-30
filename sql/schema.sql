-- ============================================================
-- PROJECT  : Complaint Management System
-- FILE     : schema.sql
-- DESCRIPTION: Database DDL Schema definition.
-- ============================================================

DROP DATABASE IF EXISTS complaint_management;

CREATE DATABASE complaint_management
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE complaint_management;

-- Table: users
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
    address         VARCHAR(255),

    CONSTRAINT pk_users         PRIMARY KEY (user_id),
    CONSTRAINT uq_users_uname   UNIQUE (username),
    CONSTRAINT uq_users_email   UNIQUE (email),
    CONSTRAINT chk_full_name    CHECK (CHAR_LENGTH(TRIM(full_name)) >= 2),
    CONSTRAINT chk_username     CHECK (CHAR_LENGTH(TRIM(username)) >= 3)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE INDEX idx_users_role       ON users (role);
CREATE INDEX idx_users_department ON users (department);
CREATE INDEX idx_users_is_active  ON users (is_active);

-- Table: complaints
CREATE TABLE complaints (
    complaint_id        INT             NOT NULL AUTO_INCREMENT,
    complaint_number    VARCHAR(20)     NOT NULL,
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
  COLLATE = utf8mb4_unicode_ci;

CREATE INDEX idx_cmp_status      ON complaints (status);
CREATE INDEX idx_cmp_priority    ON complaints (priority);
CREATE INDEX idx_cmp_category    ON complaints (category);
CREATE INDEX idx_cmp_created_by  ON complaints (created_by);
CREATE INDEX idx_cmp_assigned_to ON complaints (assigned_to);
CREATE INDEX idx_cmp_created_dt  ON complaints (date_created);
CREATE INDEX idx_cmp_department  ON complaints (department);

-- Table: complaint_history
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
  COLLATE = utf8mb4_unicode_ci;

CREATE INDEX idx_hist_complaint_id ON complaint_history (complaint_id);
CREATE INDEX idx_hist_change_date  ON complaint_history (change_date);
CREATE INDEX idx_hist_changed_by   ON complaint_history (changed_by);

-- Table: activity_log
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
  COLLATE = utf8mb4_unicode_ci;

CREATE INDEX idx_log_user_id   ON activity_log (user_id);
CREATE INDEX idx_log_timestamp ON activity_log (log_timestamp);
CREATE INDEX idx_log_action    ON activity_log (action);
CREATE INDEX idx_log_entity    ON activity_log (entity_type, entity_id);

-- View: All complaint details
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
