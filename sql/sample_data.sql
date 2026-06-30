-- ============================================================
-- PROJECT  : Complaint Management System
-- FILE     : sample_data.sql
-- DESCRIPTION: Database DML Seed/Sample Data.
-- ============================================================

USE complaint_management;

-- Clear tables before seeding (respecting constraints)
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE activity_log;
TRUNCATE TABLE complaint_history;
TRUNCATE TABLE complaints;
TRUNCATE TABLE users;
SET FOREIGN_KEY_CHECKS = 1;

-- Seed Users
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

-- Seed Complaints
INSERT INTO complaints
    (complaint_number, title, description, category, priority, status,
     location, department, assigned_to, created_by, date_created, remarks)
VALUES
('CMS-2024-0001',
 'Street Lights Not Working on Main Street',
 'Multiple street lights on Main Street Block A have been non-functional for 3 days causing safety concerns at night.',
 'ELECTRICITY', 'HIGH', 'RESOLVED',
 'Main Street, Block A', 'Electricity', 5, 9,
 DATE_SUB(NOW(), INTERVAL 15 DAY), 'All faulty lights replaced and tested.'),

('CMS-2024-0002',
 'Large Pothole on Highway 12 Near Junction',
 'A large and dangerous pothole has developed on Highway 12 near the KM 45 junction. Multiple vehicles have been damaged.',
 'ROAD', 'CRITICAL', 'IN_PROGRESS',
 'Highway 12, KM 45', 'Road Maintenance', 6, 9,
 DATE_SUB(NOW(), INTERVAL 12 DAY), NULL),

('CMS-2024-0003',
 'No Water Supply in Sector 4 for 2 Days',
 'Residents of Sector 4, Zone B have had no water supply for two consecutive days. Urgent resolution required.',
 'WATER', 'HIGH', 'ASSIGNED',
 'Sector 4, Zone B', 'Water Supply', 4, 9,
 DATE_SUB(NOW(), INTERVAL 10 DAY), NULL),

('CMS-2024-0004',
 'Garbage Not Collected Near Community Center',
 'The garbage bins near the Community Center in Zone A have not been collected for over a week. They are overflowing.',
 'GARBAGE', 'MEDIUM', 'PENDING',
 'Community Center, Zone A', 'Sanitation', NULL, 9,
 DATE_SUB(NOW(), INTERVAL 8 DAY), NULL),

('CMS-2024-0005',
 'Suspicious Individuals Near School After Hours',
 'Unidentified individuals have been observed loitering near City School premises after closing hours.',
 'SECURITY', 'HIGH', 'CLOSED',
 'City School, Block C', 'Security', 8, 9,
 DATE_SUB(NOW(), INTERVAL 7 DAY), 'Police patrol frequency increased. Issue resolved.'),

('CMS-2024-0006',
 'Complete Internet Outage in Office Block B',
 'The entire Office Block B on Floor 2 has had no internet connectivity since Monday morning affecting operations.',
 'INTERNET', 'MEDIUM', 'PENDING',
 'Office Block B, Floor 2', 'IT Department', NULL, 9,
 DATE_SUB(NOW(), INTERVAL 5 DAY), NULL),

('CMS-2024-0007',
 'Burst Water Pipeline in Residential Area 7',
 'A major water pipeline has burst near Residential Area 7 causing significant water wastage and road waterlogging.',
 'WATER', 'CRITICAL', 'IN_PROGRESS',
 'Residential Area 7, Main Pipeline', 'Water Supply', 4, 9,
 DATE_SUB(NOW(), INTERVAL 3 DAY), NULL),

('CMS-2024-0008',
 'Road Surface Damage After Heavy Rainfall',
 'Heavy rains have severely damaged the road surface in Zone C residential area making it dangerous for commuters.',
 'ROAD', 'HIGH', 'PENDING',
 'Zone C, Residential Area', 'Road Maintenance', NULL, 9,
 DATE_SUB(NOW(), INTERVAL 2 DAY), NULL),

('CMS-2024-0009',
 'Frequent Power Cuts and Fluctuations in Building 3',
 'Building 3 in Sector 2 has been experiencing frequent power cuts and dangerous electrical fluctuations daily.',
 'ELECTRICITY', 'HIGH', 'ASSIGNED',
 'Building 3, Sector 2', 'Electricity', 5, 9,
 DATE_SUB(NOW(), INTERVAL 1 DAY), NULL),

('CMS-2024-0010',
 'Community Health Center Severely Understaffed',
 'The community health center has been operating with only 1 doctor for 2 weeks. Patients are being turned away.',
 'HEALTH', 'CRITICAL', 'PENDING',
 'Community Health Center, Block D', 'Health', NULL, 9,
 NOW(), NULL);

-- Seed Complaint History
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

-- Seed Activity Logs
INSERT INTO activity_log
    (user_id, action, description, entity_type, entity_id, log_timestamp)
VALUES
(1, 'LOGIN',            'System Administrator logged in.',           'USER',      1,    DATE_SUB(NOW(), INTERVAL 15 DAY)),
(9, 'CREATE_COMPLAINT', 'Created complaint CMS-2024-0001.',          'COMPLAINT', 1,    DATE_SUB(NOW(), INTERVAL 15 DAY)),
(1, 'ASSIGN_COMPLAINT', 'Assigned CMS-2024-0001 to Michael Chen.',   'COMPLAINT', 1,    DATE_SUB(NOW(), INTERVAL 14 DAY)),
(5, 'LOGIN',            'Michael Chen logged in.',                   'USER',      5,    DATE_SUB(NOW(), INTERVAL 13 DAY)),
(5, 'UPDATE_STATUS',    'Updated CMS-2024-0001 to IN_PROGRESS.',     'COMPLAINT', 1,    DATE_SUB(NOW(), INTERVAL 13 DAY)),
(5, 'UPDATE_STATUS',    'Updated CMS-2024-0001 to RESOLVED.',        'COMPLAINT', 1,    DATE_SUB(NOW(), INTERVAL 10 DAY)),
(9, 'CREATE_COMPLAINT', 'Created complaint CMS-2024-0002.',          'COMPLAINT', 2,    DATE_SUB(NOW(), INTERVAL 12 DAY)),
(1, 'ASSIGN_COMPLAINT', 'Assigned CMS-2024-0002 to Emma Wilson.',    'COMPLAINT', 2,    DATE_SUB(NOW(), INTERVAL 11 DAY)),
(1, 'CLOSE_COMPLAINT',  'Closed complaint CMS-2024-0005.',           'COMPLAINT', 5,    DATE_SUB(NOW(), INTERVAL 4 DAY)),
(1, 'EXPORT_CSV',       'Exported complaint report to CSV file.',    'REPORT',    NULL, DATE_SUB(NOW(), INTERVAL 2 DAY)),
(1, 'LOGIN',            'System Administrator logged in.',           'USER',      1,    NOW());
