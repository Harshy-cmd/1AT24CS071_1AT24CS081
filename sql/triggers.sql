USE complaint_management;

DELIMITER //

DROP TRIGGER IF EXISTS tr_after_complaint_insert //
CREATE TRIGGER tr_after_complaint_insert
AFTER INSERT ON complaints
FOR EACH ROW
BEGIN
    INSERT INTO activity_log (user_id, action, description, entity_type, entity_id)
    VALUES (NEW.created_by, 'CREATE_COMPLAINT', CONCAT('Created complaint ', NEW.complaint_number, '.'), 'COMPLAINT', NEW.complaint_id);
END //

DELIMITER ;
