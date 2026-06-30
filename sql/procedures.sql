USE complaint_management;

DELIMITER //

DROP PROCEDURE IF EXISTS sp_assign_complaint //
CREATE PROCEDURE sp_assign_complaint(
    IN p_complaint_id INT,
    IN p_employee_id INT,
    IN p_admin_id INT
)
BEGIN
    DECLARE v_old_status VARCHAR(20);
    DECLARE v_emp_name VARCHAR(100);
    
    START TRANSACTION;
    
    SELECT status INTO v_old_status FROM complaints WHERE complaint_id = p_complaint_id;
    
    UPDATE complaints 
    SET assigned_to = p_employee_id, status = 'ASSIGNED', date_updated = NOW() 
    WHERE complaint_id = p_complaint_id;
    
    INSERT INTO complaint_history (complaint_id, changed_by, old_status, new_status, remarks)
    VALUES (p_complaint_id, p_admin_id, v_old_status, 'ASSIGNED', CONCAT('Assigned to employee ID: ', p_employee_id));
    
    SELECT full_name INTO v_emp_name FROM users WHERE user_id = p_employee_id;
    
    INSERT INTO activity_log (user_id, action, description, entity_type, entity_id)
    VALUES (p_admin_id, 'ASSIGN_COMPLAINT', CONCAT('Assigned complaint ID ', p_complaint_id, ' to ', v_emp_name, '.'), 'COMPLAINT', p_complaint_id);
    
    COMMIT;
END //

DROP PROCEDURE IF EXISTS sp_update_complaint_status //
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
    
    SELECT status, complaint_number INTO v_old_status, v_comp_num FROM complaints WHERE complaint_id = p_complaint_id;
    
    UPDATE complaints 
    SET status = p_new_status,
        remarks = IFNULL(p_remarks, remarks),
        resolution_date = CASE WHEN p_new_status IN ('RESOLVED','CLOSED') THEN CURDATE() ELSE resolution_date END,
        date_updated = NOW()
    WHERE complaint_id = p_complaint_id;
    
    INSERT INTO complaint_history (complaint_id, changed_by, old_status, new_status, remarks)
    VALUES (p_complaint_id, p_user_id, v_old_status, p_new_status, p_remarks);
    
    INSERT INTO activity_log (user_id, action, description, entity_type, entity_id)
    VALUES (p_user_id, 'UPDATE_STATUS', CONCAT('Changed complaint ', v_comp_num, ' from ', v_old_status, ' to ', p_new_status, '.'), 'COMPLAINT', p_complaint_id);
    
    COMMIT;
END //

DELIMITER ;
