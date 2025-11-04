-- ============================================
-- PARKCONTROL S.A. - SCRIPT (Triggers, Funciones, Procedimientos)
-- ============================================

USE parkcontrol_db;

DELIMITER //

-- ============================================
-- FUNCIONES
-- ============================================

CREATE FUNCTION calculate_effective_discount(
    p_monthly_discount DECIMAL(5,2),
    p_annual_additional DECIMAL(5,2)
)
RETURNS DECIMAL(5,2)
DETERMINISTIC
BEGIN
    RETURN p_monthly_discount + (p_annual_additional / 12);
END//

CREATE FUNCTION has_active_subscription(
    p_license_plate VARCHAR(20)
)
RETURNS BOOLEAN
DETERMINISTIC
READS SQL DATA
BEGIN
    DECLARE v_count INT;
    SELECT COUNT(*) INTO v_count
    FROM subscriptions s
    JOIN subscription_status_types sst ON s.status_type_id = sst.id
    WHERE s.license_plate = p_license_plate
    AND sst.code = 'ACTIVE'
    AND s.end_date > NOW();
    RETURN v_count > 0;
END//

CREATE FUNCTION is_temporal_permit_valid(
    p_permit_id BIGINT
)
RETURNS BOOLEAN
DETERMINISTIC
READS SQL DATA
BEGIN
    DECLARE v_count INT;
    SELECT COUNT(*) INTO v_count
    FROM temporal_permits tp
    JOIN temporal_permit_status_types tpst ON tp.status_type_id = tpst.id
    WHERE tp.id = p_permit_id
    AND tpst.code = 'ACTIVE'
    AND tp.end_date > NOW()
    AND tp.current_uses < tp.max_uses;
    RETURN v_count > 0;
END//

CREATE FUNCTION get_available_capacity(
    p_branch_id BIGINT,
    p_vehicle_type VARCHAR(10)
)
RETURNS INT
DETERMINISTIC
READS SQL DATA
BEGIN
    DECLARE v_capacity INT;
    DECLARE v_current_occupancy INT;
    
    IF p_vehicle_type = '2R' THEN
        SELECT capacity_2r INTO v_capacity FROM branches WHERE id = p_branch_id;
        SELECT COUNT(*) INTO v_current_occupancy
        FROM tickets t
        JOIN vehicle_types vt ON t.vehicle_type_id = vt.id
        JOIN ticket_status_types tst ON t.status_type_id = tst.id
        WHERE t.branch_id = p_branch_id AND vt.code = '2R' AND tst.code = 'IN_PROGRESS';
    ELSE
        SELECT capacity_4r INTO v_capacity FROM branches WHERE id = p_branch_id;
        SELECT COUNT(*) INTO v_current_occupancy
        FROM tickets t
        JOIN vehicle_types vt ON t.vehicle_type_id = vt.id
        JOIN ticket_status_types tst ON t.status_type_id = tst.id
        WHERE t.branch_id = p_branch_id AND vt.code = '4R' AND tst.code = 'IN_PROGRESS';
    END IF;
    
    RETURN v_capacity - v_current_occupancy;
END//

-- ============================================
-- PROCEDIMIENTOS ALMACENADOS
-- ============================================

CREATE PROCEDURE reorder_subscription_plans()
BEGIN
    DECLARE done INT DEFAULT FALSE;
    DECLARE v_plan_type_id INT;
    DECLARE v_new_order INT DEFAULT 1;
    DECLARE plan_cursor CURSOR FOR
        SELECT spt.id FROM subscription_plan_types spt
        JOIN subscription_plans sp ON spt.id = sp.plan_type_id
        WHERE sp.is_active = TRUE
        ORDER BY (sp.monthly_discount_percentage + (sp.annual_additional_discount_percentage / 12)) DESC;
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;
    
    OPEN plan_cursor;
    read_loop: LOOP
        FETCH plan_cursor INTO v_plan_type_id;
        IF done THEN LEAVE read_loop; END IF;
        UPDATE subscription_plan_types SET sort_order = v_new_order WHERE id = v_plan_type_id;
        SET v_new_order = v_new_order + 1;
    END LOOP;
    CLOSE plan_cursor;
END//

CREATE PROCEDURE calculate_ticket_rate(
    IN p_branch_id BIGINT,
    OUT p_rate DECIMAL(10,2)
)
BEGIN
    DECLARE v_branch_rate DECIMAL(10,2);
    DECLARE v_base_rate DECIMAL(10,2);
    
    SELECT rate_per_hour INTO v_branch_rate FROM branches WHERE id = p_branch_id;
    
    IF v_branch_rate IS NOT NULL THEN
        SET p_rate = v_branch_rate;
    ELSE
        SELECT amount_per_hour INTO v_base_rate FROM rate_base_history WHERE is_active = TRUE LIMIT 1;
        SET p_rate = v_base_rate;
    END IF;
END//

CREATE PROCEDURE validate_night_plan_exit(
    IN p_ticket_id BIGINT,
    IN p_exit_time DATETIME,
    OUT p_is_valid BOOLEAN,
    OUT p_excess_hours DECIMAL(10,2),
    OUT p_message VARCHAR(500)
)
BEGIN
    DECLARE v_entry_time DATETIME;
    DECLARE v_branch_id BIGINT;
    DECLARE v_closing_time TIME;
    DECLARE v_opening_time TIME;
    DECLARE v_limit_time DATETIME;
    DECLARE v_plan_code VARCHAR(20);
    DECLARE v_subscription_id BIGINT;
    
    SELECT t.entry_time, t.branch_id, t.subscription_id
    INTO v_entry_time, v_branch_id, v_subscription_id
    FROM tickets t WHERE t.id = p_ticket_id;
    
    SELECT spt.code INTO v_plan_code
    FROM subscriptions s
    JOIN subscription_plans sp ON s.plan_id = sp.id
    JOIN subscription_plan_types spt ON sp.plan_type_id = spt.id
    WHERE s.id = v_subscription_id;
    
    IF v_plan_code != 'NIGHT' THEN
        SET p_is_valid = TRUE;
        SET p_excess_hours = 0;
        SET p_message = 'No es Plan Nocturno';
    ELSE
        SELECT closing_time, opening_time INTO v_closing_time, v_opening_time
        FROM branches WHERE id = v_branch_id;
        
        SET v_limit_time = DATE_ADD(DATE(p_exit_time), INTERVAL HOUR(v_opening_time) - 1 HOUR);
        SET v_limit_time = DATE_ADD(v_limit_time, INTERVAL MINUTE(v_opening_time) MINUTE);
        
        IF p_exit_time <= v_limit_time THEN
            SET p_is_valid = TRUE;
            SET p_excess_hours = 0;
            SET p_message = 'Salida dentro del horario nocturno permitido';
        ELSE
            SET p_is_valid = FALSE;
            SET p_excess_hours = TIMESTAMPDIFF(MINUTE, v_limit_time, p_exit_time) / 60.0;
            SET p_message = CONCAT('Excedio horario nocturno por ', ROUND(p_excess_hours, 2), ' horas');
        END IF;
    END IF;
END//

CREATE PROCEDURE process_vehicle_exit(
    IN p_ticket_id BIGINT,
    IN p_exit_time DATETIME,
    OUT p_amount_to_charge DECIMAL(10,2),
    OUT p_message VARCHAR(500)
)
BEGIN
    DECLARE v_entry_time DATETIME;
    DECLARE v_is_subscriber BOOLEAN;
    DECLARE v_subscription_id BIGINT;
    DECLARE v_branch_id BIGINT;
    DECLARE v_total_hours DECIMAL(10,2);
    DECLARE v_free_hours DECIMAL(10,2);
    DECLARE v_plan_remaining_hours DECIMAL(10,2);
    DECLARE v_plan_monthly_hours INT;
    DECLARE v_consumed_hours DECIMAL(10,2);
    DECLARE v_rate DECIMAL(10,2);
    DECLARE v_overage_hours DECIMAL(10,2);
    DECLARE v_hours_to_consume DECIMAL(10,2);
    DECLARE v_billable_hours DECIMAL(10,2);
    DECLARE v_night_valid BOOLEAN;
    DECLARE v_night_excess DECIMAL(10,2);
    DECLARE v_night_message VARCHAR(500);
    DECLARE v_benefit_type VARCHAR(30);
    DECLARE v_temporal_permit_id BIGINT;
    
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        SET p_amount_to_charge = 0;
        SET p_message = 'Error en transaccion';
    END;
    
    START TRANSACTION;
    
    SELECT entry_time, is_subscriber, subscription_id, branch_id
    INTO v_entry_time, v_is_subscriber, v_subscription_id, v_branch_id
    FROM tickets WHERE id = p_ticket_id;
    
    SET v_total_hours = TIMESTAMPDIFF(MINUTE, v_entry_time, p_exit_time) / 60.0;
    IF v_total_hours < 0.25 THEN SET v_total_hours = 0.25; END IF;
    
    SELECT COALESCE(SUM(granted_hours), 0) INTO v_free_hours
    FROM business_free_hours WHERE ticket_id = p_ticket_id;
    
    CALL calculate_ticket_rate(v_branch_id, v_rate);
    
    IF v_is_subscriber = TRUE AND v_subscription_id IS NOT NULL THEN
        CALL validate_night_plan_exit(p_ticket_id, p_exit_time, v_night_valid, v_night_excess, v_night_message);
        
        SELECT sp.monthly_hours, s.consumed_hours, (sp.monthly_hours - s.consumed_hours)
        INTO v_plan_monthly_hours, v_consumed_hours, v_plan_remaining_hours
        FROM subscriptions s
        JOIN subscription_plans sp ON s.plan_id = sp.id
        WHERE s.id = v_subscription_id;
        
        SELECT bt.code INTO v_benefit_type
        FROM business_free_hours bfh
        JOIN branch_businesses bb ON bfh.business_id = bb.business_id AND bfh.branch_id = bb.branch_id
        JOIN benefit_types bt ON bb.benefit_type_id = bt.id
        WHERE bfh.ticket_id = p_ticket_id LIMIT 1;
        
        IF v_benefit_type = 'NO_CONSUME_HOURS' THEN
            SET v_hours_to_consume = v_total_hours;
        ELSE
            SET v_hours_to_consume = v_total_hours - v_free_hours;
        END IF;
        
        IF v_night_valid = FALSE AND v_night_excess > 0 THEN
            SET p_amount_to_charge = v_night_excess * v_rate;
            SET p_message = v_night_message;
            INSERT INTO subscription_overages (subscription_id, ticket_id, overage_hours, charged_amount, applied_rate, charged_at)
            VALUES (v_subscription_id, p_ticket_id, v_night_excess, p_amount_to_charge, v_rate, p_exit_time);
        ELSEIF v_hours_to_consume <= v_plan_remaining_hours THEN
            UPDATE subscriptions SET consumed_hours = consumed_hours + v_hours_to_consume WHERE id = v_subscription_id;
            SET p_amount_to_charge = 0;
            SET p_message = 'Consumo cubierto por suscripcion';
        ELSE
            SET v_overage_hours = v_hours_to_consume - v_plan_remaining_hours;
            SET p_amount_to_charge = v_overage_hours * v_rate;
            UPDATE subscriptions SET consumed_hours = v_plan_monthly_hours WHERE id = v_subscription_id;
            INSERT INTO subscription_overages (subscription_id, ticket_id, overage_hours, charged_amount, applied_rate, charged_at)
            VALUES (v_subscription_id, p_ticket_id, v_overage_hours, p_amount_to_charge, v_rate, p_exit_time);
            SET p_message = CONCAT('Excedente: ', ROUND(v_overage_hours, 2), ' horas cobradas');
        END IF;
        
        IF (v_consumed_hours + v_hours_to_consume) / v_plan_monthly_hours >= 0.8 THEN
            UPDATE subscriptions SET notified_80_percent = TRUE
            WHERE id = v_subscription_id AND notified_80_percent = FALSE;
        END IF;
    ELSE
        SET v_billable_hours = v_total_hours - v_free_hours;
        IF v_billable_hours < 0 THEN SET v_billable_hours = 0; END IF;
        SET p_amount_to_charge = v_billable_hours * v_rate;
        SET p_message = 'Cliente sin suscripcion';
    END IF;
    
    SELECT id INTO v_temporal_permit_id
    FROM temporal_permits
    WHERE temporal_plate = (SELECT license_plate FROM tickets WHERE id = p_ticket_id)
    AND status_type_id = (SELECT id FROM temporal_permit_status_types WHERE code = 'ACTIVE')
    LIMIT 1;
    
    IF v_temporal_permit_id IS NOT NULL THEN
        UPDATE temporal_permits SET current_uses = current_uses + 1 WHERE id = v_temporal_permit_id;
    END IF;
    
    UPDATE tickets
    SET exit_time = p_exit_time,
        status_type_id = (SELECT id FROM ticket_status_types WHERE code = 'COMPLETED'),
        updated_at = NOW()
    WHERE id = p_ticket_id;
    
    COMMIT;
END//

CREATE PROCEDURE validate_password_reuse(
    IN p_user_id BIGINT,
    IN p_new_password_hash VARCHAR(255),
    OUT p_is_valid BOOLEAN,
    OUT p_message VARCHAR(200)
)
BEGIN
    DECLARE v_count INT;
    SELECT COUNT(*) INTO v_count
    FROM (SELECT password_hash FROM password_history WHERE user_id = p_user_id ORDER BY created_at DESC LIMIT 5) AS recent
    WHERE password_hash = p_new_password_hash;
    
    IF v_count > 0 THEN
        SET p_is_valid = FALSE;
        SET p_message = 'No puede reutilizar las ultimas 5 contrasenas';
    ELSE
        SET p_is_valid = TRUE;
        SET p_message = 'Contrasena valida';
    END IF;
END//

CREATE PROCEDURE check_user_lockout(
    IN p_user_id BIGINT,
    OUT p_is_locked BOOLEAN,
    OUT p_minutes_remaining INT
)
BEGIN
    DECLARE v_locked_until DATETIME;
    DECLARE v_failed_attempts INT;
    
    SELECT locked_until, failed_login_attempts INTO v_locked_until, v_failed_attempts
    FROM users WHERE id = p_user_id;
    
    IF v_locked_until IS NOT NULL AND v_locked_until > NOW() THEN
        SET p_is_locked = TRUE;
        SET p_minutes_remaining = TIMESTAMPDIFF(MINUTE, NOW(), v_locked_until);
    ELSE
        SET p_is_locked = FALSE;
        SET p_minutes_remaining = 0;
        IF v_locked_until IS NOT NULL AND v_locked_until <= NOW() THEN
            UPDATE users SET locked_until = NULL, failed_login_attempts = 0 WHERE id = p_user_id;
        END IF;
    END IF;
END//

CREATE PROCEDURE register_failed_login(IN p_user_id BIGINT)
BEGIN
    DECLARE v_failed_attempts INT;
    UPDATE users SET failed_login_attempts = failed_login_attempts + 1 WHERE id = p_user_id;
    SELECT failed_login_attempts INTO v_failed_attempts FROM users WHERE id = p_user_id;
    IF v_failed_attempts >= 5 THEN
        UPDATE users SET locked_until = DATE_ADD(NOW(), INTERVAL 30 MINUTE) WHERE id = p_user_id;
    END IF;
END//

CREATE PROCEDURE generate_business_settlement(
    IN p_business_id BIGINT,
    IN p_branch_id BIGINT,
    IN p_start_date DATETIME,
    IN p_end_date DATETIME,
    IN p_settled_by BIGINT,
    OUT p_total_hours DECIMAL(10,2),
    OUT p_total_amount DECIMAL(10,2),
    OUT p_ticket_count INT,
    OUT p_settlement_id BIGINT
)
BEGIN
    DECLARE v_rate DECIMAL(10,2);
    
    SELECT rate_per_hour INTO v_rate FROM affiliated_businesses WHERE id = p_business_id;
    
    SELECT COALESCE(SUM(granted_hours), 0), COUNT(DISTINCT ticket_id)
    INTO p_total_hours, p_ticket_count
    FROM business_free_hours
    WHERE business_id = p_business_id AND branch_id = p_branch_id
    AND granted_at BETWEEN p_start_date AND p_end_date AND is_settled = FALSE;
    
    SET p_total_amount = p_total_hours * v_rate;
    
    INSERT INTO business_settlement_history (business_id, branch_id, period_start, period_end, total_hours, total_amount, ticket_count, settled_at, settled_by)
    VALUES (p_business_id, p_branch_id, p_start_date, p_end_date, p_total_hours, p_total_amount, p_ticket_count, NOW(), p_settled_by);
    
    SET p_settlement_id = LAST_INSERT_ID();
    
    INSERT INTO settlement_tickets (settlement_id, ticket_id, free_hours_granted)
    SELECT p_settlement_id, ticket_id, granted_hours
    FROM business_free_hours
    WHERE business_id = p_business_id AND branch_id = p_branch_id
    AND granted_at BETWEEN p_start_date AND p_end_date AND is_settled = FALSE;
    
    UPDATE business_free_hours SET is_settled = TRUE
    WHERE business_id = p_business_id AND branch_id = p_branch_id
    AND granted_at BETWEEN p_start_date AND p_end_date AND is_settled = FALSE;
END//

CREATE PROCEDURE cleanup_expired_password_reset_tokens()
BEGIN
    DECLARE deleted_count INT;
    
    DELETE FROM password_reset_tokens 
    WHERE expires_at < NOW() 
    OR (is_used = TRUE AND used_at < DATE_SUB(NOW(), INTERVAL 7 DAY));
    
    SET deleted_count = ROW_COUNT();
    
    IF deleted_count > 0 THEN
        INSERT INTO audit_log (user_id, module, entity, operation_type_id, description)
        VALUES (NULL, 'Sistema', 'password_reset_tokens', (SELECT id FROM operation_types WHERE code = 'DELETE'),
                CONCAT('Limpieza automatica: ', deleted_count, ' tokens eliminados'));
    END IF;
END//

CREATE PROCEDURE validate_plan_hierarchy(
    IN p_plan_type_id INT,
    IN p_monthly_discount DECIMAL(5,2),
    IN p_annual_additional DECIMAL(5,2),
    IN p_plan_id BIGINT
)
BEGIN
    DECLARE v_plan_code VARCHAR(20);
    DECLARE v_effective_discount DECIMAL(5,2);
    DECLARE v_full_access_discount DECIMAL(5,2);
    DECLARE v_workweek_discount DECIMAL(5,2);
    DECLARE v_office_light_discount DECIMAL(5,2);
    DECLARE v_daily_flexible_discount DECIMAL(5,2);
    DECLARE v_night_discount DECIMAL(5,2);
    DECLARE v_duplicate_count INT;
    
    SET v_effective_discount = p_monthly_discount + (p_annual_additional / 12);
    SELECT code INTO v_plan_code FROM subscription_plan_types WHERE id = p_plan_type_id;
    
    SELECT COUNT(*) INTO v_duplicate_count FROM subscription_plans sp
    WHERE sp.is_active = TRUE AND (sp.id IS NULL OR sp.id != IFNULL(p_plan_id, 0))
    AND (sp.monthly_discount_percentage = p_monthly_discount
        OR sp.annual_additional_discount_percentage = p_annual_additional
        OR (sp.monthly_discount_percentage + (sp.annual_additional_discount_percentage / 12)) = v_effective_discount);
    
    IF v_duplicate_count > 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Todos los porcentajes de descuento deben ser distintos';
    END IF;
    
    SELECT 
        MAX(CASE WHEN spt.code = 'FULL_ACCESS' THEN sp.monthly_discount_percentage + (sp.annual_additional_discount_percentage / 12) END),
        MAX(CASE WHEN spt.code = 'WORKWEEK' THEN sp.monthly_discount_percentage + (sp.annual_additional_discount_percentage / 12) END),
        MAX(CASE WHEN spt.code = 'OFFICE_LIGHT' THEN sp.monthly_discount_percentage + (sp.annual_additional_discount_percentage / 12) END),
        MAX(CASE WHEN spt.code = 'DAILY_FLEXIBLE' THEN sp.monthly_discount_percentage + (sp.annual_additional_discount_percentage / 12) END),
        MAX(CASE WHEN spt.code = 'NIGHT' THEN sp.monthly_discount_percentage + (sp.annual_additional_discount_percentage / 12) END)
    INTO v_full_access_discount, v_workweek_discount, v_office_light_discount, v_daily_flexible_discount, v_night_discount
    FROM subscription_plans sp
    JOIN subscription_plan_types spt ON sp.plan_type_id = spt.id
    WHERE sp.is_active = TRUE AND spt.id != p_plan_type_id AND (sp.id IS NULL OR sp.id != IFNULL(p_plan_id, 0));
    
    IF v_plan_code = 'FULL_ACCESS' THEN
        IF (v_workweek_discount IS NOT NULL AND v_effective_discount <= v_workweek_discount)
           OR (v_office_light_discount IS NOT NULL AND v_effective_discount <= v_office_light_discount)
           OR (v_daily_flexible_discount IS NOT NULL AND v_effective_discount <= v_daily_flexible_discount)
           OR (v_night_discount IS NOT NULL AND v_effective_discount <= v_night_discount) THEN
            SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Full Access debe tener MAYOR descuento que todos los demas';
        END IF;
    ELSEIF v_plan_code = 'WORKWEEK' THEN
        IF (v_full_access_discount IS NOT NULL AND v_effective_discount >= v_full_access_discount)
           OR (v_office_light_discount IS NOT NULL AND v_effective_discount <= v_office_light_discount)
           OR (v_daily_flexible_discount IS NOT NULL AND v_effective_discount <= v_daily_flexible_discount)
           OR (v_night_discount IS NOT NULL AND v_effective_discount <= v_night_discount) THEN
            SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Workweek debe estar entre Full Access y Office Light';
        END IF;
    ELSEIF v_plan_code = 'OFFICE_LIGHT' THEN
        IF (v_full_access_discount IS NOT NULL AND v_effective_discount >= v_full_access_discount)
           OR (v_workweek_discount IS NOT NULL AND v_effective_discount >= v_workweek_discount)
           OR (v_daily_flexible_discount IS NOT NULL AND v_effective_discount <= v_daily_flexible_discount)
           OR (v_night_discount IS NOT NULL AND v_effective_discount <= v_night_discount) THEN
            SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Office Light debe estar entre Workweek y Diario Flexible';
        END IF;
    ELSEIF v_plan_code = 'DAILY_FLEXIBLE' THEN
        IF (v_full_access_discount IS NOT NULL AND v_effective_discount >= v_full_access_discount)
           OR (v_workweek_discount IS NOT NULL AND v_effective_discount >= v_workweek_discount)
           OR (v_office_light_discount IS NOT NULL AND v_effective_discount >= v_office_light_discount)
           OR (v_night_discount IS NOT NULL AND v_effective_discount <= v_night_discount) THEN
            SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Diario Flexible debe estar entre Office Light y Nocturno';
        END IF;
    ELSEIF v_plan_code = 'NIGHT' THEN
        IF (v_full_access_discount IS NOT NULL AND v_effective_discount >= v_full_access_discount)
           OR (v_workweek_discount IS NOT NULL AND v_effective_discount >= v_workweek_discount)
           OR (v_office_light_discount IS NOT NULL AND v_effective_discount >= v_office_light_discount)
           OR (v_daily_flexible_discount IS NOT NULL AND v_effective_discount >= v_daily_flexible_discount) THEN
            SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Nocturno debe tener MENOR descuento que todos los demas';
        END IF;
    END IF;
END//

DELIMITER ;

-- ============================================
-- TRIGGERS
-- ============================================

DELIMITER //

CREATE TRIGGER before_rate_base_insert
BEFORE INSERT ON rate_base_history
FOR EACH ROW
BEGIN
    DECLARE active_count INT;
    IF NEW.is_active = TRUE THEN
        SELECT COUNT(*) INTO active_count FROM rate_base_history WHERE is_active = TRUE;
        IF active_count > 0 THEN
            SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Solo puede existir una tarifa base activa';
        END IF;
    END IF;
END//

CREATE TRIGGER before_rate_base_update
BEFORE UPDATE ON rate_base_history
FOR EACH ROW
BEGIN
    DECLARE active_count INT;
    IF NEW.is_active = TRUE AND OLD.is_active = FALSE THEN
        SELECT COUNT(*) INTO active_count FROM rate_base_history WHERE is_active = TRUE AND id != NEW.id;
        IF active_count > 0 THEN
            SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Solo puede existir una tarifa base activa';
        END IF;
    END IF;
END//

CREATE TRIGGER before_subscription_insert
BEFORE INSERT ON subscriptions
FOR EACH ROW
BEGIN
    DECLARE active_count INT;
    DECLARE v_status_code VARCHAR(20);
    SELECT code INTO v_status_code FROM subscription_status_types WHERE id = NEW.status_type_id;
    
    IF v_status_code = 'ACTIVE' THEN
        SELECT COUNT(*) INTO active_count
        FROM subscriptions s
        JOIN subscription_status_types sst ON s.status_type_id = sst.id
        WHERE s.license_plate = NEW.license_plate AND sst.code = 'ACTIVE'
        AND s.end_date > NOW() AND s.id != NEW.id;
        
        IF active_count > 0 THEN
            SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Esta placa ya tiene una suscripcion activa vigente';
        END IF;
    END IF;
END//

CREATE TRIGGER before_subscription_update
BEFORE UPDATE ON subscriptions
FOR EACH ROW
BEGIN
    DECLARE active_count INT;
    DECLARE v_new_status_code VARCHAR(20);
    DECLARE v_old_status_code VARCHAR(20);
    
    SELECT code INTO v_new_status_code FROM subscription_status_types WHERE id = NEW.status_type_id;
    SELECT code INTO v_old_status_code FROM subscription_status_types WHERE id = OLD.status_type_id;
    
    IF (v_new_status_code = 'ACTIVE' AND v_old_status_code != 'ACTIVE') 
       OR (v_new_status_code = 'ACTIVE' AND NEW.license_plate != OLD.license_plate) THEN
        SELECT COUNT(*) INTO active_count
        FROM subscriptions s
        JOIN subscription_status_types sst ON s.status_type_id = sst.id
        WHERE s.license_plate = NEW.license_plate AND sst.code = 'ACTIVE'
        AND s.end_date > NOW() AND s.id != NEW.id;
        
        IF active_count > 0 THEN
            SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Esta placa ya tiene una suscripcion activa vigente';
        END IF;
    END IF;
END//

CREATE TRIGGER before_subscription_plan_insert
BEFORE INSERT ON subscription_plans
FOR EACH ROW
BEGIN
    CALL validate_plan_hierarchy(NEW.plan_type_id, NEW.monthly_discount_percentage, NEW.annual_additional_discount_percentage, NULL);
END//

CREATE TRIGGER before_subscription_plan_update
BEFORE UPDATE ON subscription_plans
FOR EACH ROW
BEGIN
    CALL validate_plan_hierarchy(NEW.plan_type_id, NEW.monthly_discount_percentage, NEW.annual_additional_discount_percentage, NEW.id);
END//

CREATE TRIGGER after_subscription_plan_insert
AFTER INSERT ON subscription_plans
FOR EACH ROW
BEGIN
    IF NEW.is_active = TRUE THEN
        CALL reorder_subscription_plans();
    END IF;
END//

CREATE TRIGGER after_subscription_plan_update
AFTER UPDATE ON subscription_plans
FOR EACH ROW
BEGIN
    IF NEW.is_active = TRUE OR OLD.is_active != NEW.is_active THEN
        CALL reorder_subscription_plans();
    END IF;
END//

CREATE TRIGGER before_ticket_insert
BEFORE INSERT ON tickets
FOR EACH ROW
BEGIN
    DECLARE v_subscription_id BIGINT;
    DECLARE v_permit_id BIGINT;
    DECLARE v_capacity INT;
    DECLARE v_vehicle_code VARCHAR(10);
    DECLARE v_opening_time TIME;
    DECLARE v_closing_time TIME;
    DECLARE v_entry_time TIME;
    DECLARE v_plan_code VARCHAR(20);
    DECLARE v_night_start TIME;
    DECLARE v_night_end TIME;
    DECLARE v_is_night_time BOOLEAN;
    
    SELECT code INTO v_vehicle_code FROM vehicle_types WHERE id = NEW.vehicle_type_id;
    
    SELECT opening_time, closing_time INTO v_opening_time, v_closing_time FROM branches WHERE id = NEW.branch_id;
    SET v_entry_time = TIME(NEW.entry_time);
    
    IF v_entry_time < v_opening_time OR v_entry_time > v_closing_time THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Fuera del horario de operacion';
    END IF;
    
    IF v_vehicle_code = '2R' THEN
        SELECT capacity_2r INTO v_capacity FROM branches WHERE id = NEW.branch_id;
        IF (SELECT COUNT(*) FROM tickets t
            JOIN vehicle_types vt ON t.vehicle_type_id = vt.id
            JOIN ticket_status_types tst ON t.status_type_id = tst.id
            WHERE t.branch_id = NEW.branch_id AND vt.code = '2R' AND tst.code = 'IN_PROGRESS') >= v_capacity THEN
            SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Capacidad completa para 2R';
        END IF;
    ELSE
        SELECT capacity_4r INTO v_capacity FROM branches WHERE id = NEW.branch_id;
        IF (SELECT COUNT(*) FROM tickets t
            JOIN vehicle_types vt ON t.vehicle_type_id = vt.id
            JOIN ticket_status_types tst ON t.status_type_id = tst.id
            WHERE t.branch_id = NEW.branch_id AND vt.code = '4R' AND tst.code = 'IN_PROGRESS') >= v_capacity THEN
            SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Capacidad completa para 4R';
        END IF;
    END IF;
    
    SELECT s.id INTO v_subscription_id
    FROM subscriptions s
    JOIN subscription_status_types sst ON s.status_type_id = sst.id
    WHERE s.license_plate = NEW.license_plate AND sst.code = 'ACTIVE' AND s.end_date > NOW()
    LIMIT 1;
    
    IF v_subscription_id IS NULL THEN
        SELECT tp.id INTO v_permit_id
        FROM temporal_permits tp
        JOIN temporal_permit_status_types tpst ON tp.status_type_id = tpst.id
        WHERE tp.temporal_plate = NEW.license_plate AND tpst.code = 'ACTIVE'
        AND tp.end_date > NOW() AND tp.current_uses < tp.max_uses
        LIMIT 1;
        
        IF v_permit_id IS NOT NULL THEN
            SELECT subscription_id INTO v_subscription_id FROM temporal_permits WHERE id = v_permit_id;
        END IF;
    END IF;
    
    IF v_subscription_id IS NOT NULL THEN
        SET NEW.is_subscriber = TRUE;
        SET NEW.subscription_id = v_subscription_id;
        
        SELECT spt.code INTO v_plan_code
        FROM subscriptions s
        JOIN subscription_plans sp ON s.plan_id = sp.id
        JOIN subscription_plan_types spt ON sp.plan_type_id = spt.id
        WHERE s.id = v_subscription_id;
        
        IF v_plan_code = 'NIGHT' THEN
            SET v_night_start = v_closing_time;
            SET v_night_end = SUBTIME(v_opening_time, '01:00:00');
            
            IF v_night_end < v_night_start THEN
                SET v_is_night_time = (v_entry_time >= v_night_start OR v_entry_time <= v_night_end);
            ELSE
                SET v_is_night_time = (v_entry_time >= v_night_start AND v_entry_time <= v_night_end);
            END IF;
            
            IF NOT v_is_night_time THEN
                SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Plan Nocturno solo valido entre cierre y 1h antes de apertura';
            END IF;
        END IF;
    ELSE
        SET NEW.is_subscriber = FALSE;
        SET NEW.subscription_id = NULL;
    END IF;
END//

CREATE TRIGGER before_plate_change_request_insert
BEFORE INSERT ON plate_change_requests
FOR EACH ROW
BEGIN
    DECLARE recent_changes INT;
    SELECT COUNT(*) INTO recent_changes
    FROM plate_change_requests pcr
    JOIN change_request_status_types crst ON pcr.status_type_id = crst.id
    WHERE pcr.subscription_id = NEW.subscription_id AND crst.code = 'APPROVED'
    AND pcr.reviewed_at >= DATE_SUB(NEW.requested_at, INTERVAL 6 MONTH);
    
    IF recent_changes > 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Solo se permite 1 cambio de placa cada 6 meses';
    END IF;
END//

CREATE TRIGGER before_plate_change_approve
BEFORE UPDATE ON plate_change_requests
FOR EACH ROW
BEGIN
    DECLARE v_new_status VARCHAR(20);
    DECLARE v_old_status VARCHAR(20);
    DECLARE plate_in_use INT;
    
    SELECT code INTO v_new_status FROM change_request_status_types WHERE id = NEW.status_type_id;
    SELECT code INTO v_old_status FROM change_request_status_types WHERE id = OLD.status_type_id;
    
    IF v_new_status = 'APPROVED' AND v_old_status = 'PENDING' THEN
        SELECT COUNT(*) INTO plate_in_use
        FROM subscriptions s
        JOIN subscription_status_types sst ON s.status_type_id = sst.id
        WHERE s.license_plate = NEW.new_plate AND sst.code = 'ACTIVE' AND s.end_date > NOW();
        
        IF plate_in_use > 0 THEN
            SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Nueva placa ya esta en otra suscripcion activa';
        END IF;
    END IF;
END//

CREATE TRIGGER after_plate_change_approved
AFTER UPDATE ON plate_change_requests
FOR EACH ROW
BEGIN
    DECLARE v_new_status VARCHAR(20);
    DECLARE v_old_status VARCHAR(20);
    
    SELECT code INTO v_new_status FROM change_request_status_types WHERE id = NEW.status_type_id;
    SELECT code INTO v_old_status FROM change_request_status_types WHERE id = OLD.status_type_id;
    
    IF v_new_status = 'APPROVED' AND v_old_status = 'PENDING' THEN
        UPDATE subscriptions SET license_plate = NEW.new_plate, updated_at = NOW() WHERE id = NEW.subscription_id;
        
        INSERT INTO audit_log (user_id, module, entity, operation_type_id, description, previous_values, new_values)
        VALUES (NEW.reviewed_by, 'Back Office', 'subscriptions', (SELECT id FROM operation_types WHERE code = 'UPDATE'),
                CONCAT('Cambio placa: ', NEW.old_plate, ' -> ', NEW.new_plate),
                JSON_OBJECT('license_plate', NEW.old_plate), JSON_OBJECT('license_plate', NEW.new_plate));
    END IF;
END//

CREATE TRIGGER before_business_free_hours_insert
BEFORE INSERT ON business_free_hours
FOR EACH ROW
BEGIN
    DECLARE is_affiliated INT;
    SELECT COUNT(*) INTO is_affiliated
    FROM branch_businesses
    WHERE business_id = NEW.business_id AND branch_id = NEW.branch_id AND is_active = TRUE;
    
    IF is_affiliated = 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Comercio no afiliado a esta sucursal';
    END IF;
END//

CREATE TRIGGER before_fleet_vehicle_insert
BEFORE INSERT ON fleet_vehicles
FOR EACH ROW
BEGIN
    DECLARE current_count INT;
    DECLARE plate_limit INT;
    DECLARE v_corporate_discount DECIMAL(5,2);
    DECLARE v_plan_discount DECIMAL(5,2);
    DECLARE v_plan_annual_additional DECIMAL(5,2);
    DECLARE v_total_discount DECIMAL(5,2);
    
    SELECT COUNT(*) INTO current_count FROM fleet_vehicles WHERE company_id = NEW.company_id AND is_active = TRUE;
    SELECT fc.plate_limit INTO plate_limit FROM fleet_companies fc WHERE fc.id = NEW.company_id;
    
    IF current_count >= plate_limit THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Limite de placas alcanzado';
    END IF;
    
    SELECT corporate_discount_percentage INTO v_corporate_discount FROM fleet_companies WHERE id = NEW.company_id;
    SELECT monthly_discount_percentage, annual_additional_discount_percentage
    INTO v_plan_discount, v_plan_annual_additional FROM subscription_plans WHERE id = NEW.plan_id;
    
    SET v_total_discount = v_corporate_discount + v_plan_discount + (v_plan_annual_additional / 12);
    
    IF v_total_discount > 35 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Descuento total no puede exceder 35%';
    END IF;
END//

CREATE TRIGGER before_fleet_vehicle_update
BEFORE UPDATE ON fleet_vehicles
FOR EACH ROW
BEGIN
    DECLARE v_corporate_discount DECIMAL(5,2);
    DECLARE v_plan_discount DECIMAL(5,2);
    DECLARE v_plan_annual_additional DECIMAL(5,2);
    DECLARE v_total_discount DECIMAL(5,2);
    
    IF NEW.plan_id != OLD.plan_id THEN
        SELECT corporate_discount_percentage INTO v_corporate_discount FROM fleet_companies WHERE id = NEW.company_id;
        SELECT monthly_discount_percentage, annual_additional_discount_percentage
        INTO v_plan_discount, v_plan_annual_additional FROM subscription_plans WHERE id = NEW.plan_id;
        
        SET v_total_discount = v_corporate_discount + v_plan_discount + (v_plan_annual_additional / 12);
        
        IF v_total_discount > 35 THEN
            SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Descuento total no puede exceder 35%';
        END IF;
    END IF;
END//

CREATE TRIGGER before_fleet_ticket_validation
BEFORE INSERT ON tickets
FOR EACH ROW
BEGIN
    DECLARE v_company_id BIGINT;
    DECLARE v_months_unpaid INT;
    
    SELECT fv.company_id, fc.months_unpaid INTO v_company_id, v_months_unpaid
    FROM fleet_vehicles fv
    JOIN fleet_companies fc ON fv.company_id = fc.id
    WHERE fv.license_plate = NEW.license_plate AND fv.is_active = TRUE LIMIT 1;
    
    IF v_company_id IS NOT NULL AND v_months_unpaid >= 2 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Acceso bloqueado: flota con 2+ meses impagos';
    END IF;
END//

CREATE TRIGGER before_temporal_permit_insert
BEFORE INSERT ON temporal_permits
FOR EACH ROW
BEGIN
    DECLARE v_branch_count INT;
    DECLARE v_branch_id BIGINT;
    DECLARE v_invalid_branches TEXT;
    
    IF NEW.allowed_branches IS NOT NULL THEN
        SET v_branch_count = JSON_LENGTH(NEW.allowed_branches);
        
        IF v_branch_count > 0 THEN
            SET @i = 0;
            WHILE @i < v_branch_count DO
                SET v_branch_id = JSON_EXTRACT(NEW.allowed_branches, CONCAT('$[', @i, ']'));
                
                IF NOT EXISTS (SELECT 1 FROM branches WHERE id = v_branch_id) THEN
                    SET v_invalid_branches = CONCAT(IFNULL(v_invalid_branches, ''), v_branch_id, ',');
                END IF;
                
                SET @i = @i + 1;
            END WHILE;
            
            IF v_invalid_branches IS NOT NULL THEN
                SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'IDs de sucursales invalidos en allowed_branches';
            END IF;
        END IF;
    END IF;
END//

CREATE TRIGGER before_temporal_permit_update
BEFORE UPDATE ON temporal_permits
FOR EACH ROW
BEGIN
    DECLARE v_branch_count INT;
    DECLARE v_branch_id BIGINT;
    DECLARE v_invalid_branches TEXT;
    
    IF NEW.allowed_branches IS NOT NULL AND NEW.allowed_branches != OLD.allowed_branches THEN
        SET v_branch_count = JSON_LENGTH(NEW.allowed_branches);
        
        IF v_branch_count > 0 THEN
            SET @i = 0;
            WHILE @i < v_branch_count DO
                SET v_branch_id = JSON_EXTRACT(NEW.allowed_branches, CONCAT('$[', @i, ']'));
                
                IF NOT EXISTS (SELECT 1 FROM branches WHERE id = v_branch_id) THEN
                    SET v_invalid_branches = CONCAT(IFNULL(v_invalid_branches, ''), v_branch_id, ',');
                END IF;
                
                SET @i = @i + 1;
            END WHILE;
            
            IF v_invalid_branches IS NOT NULL THEN
                SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'IDs de sucursales invalidos en allowed_branches';
            END IF;
        END IF;
    END IF;
END//

CREATE TRIGGER after_rate_base_insert
AFTER INSERT ON rate_base_history
FOR EACH ROW
BEGIN
    INSERT INTO audit_log (user_id, module, entity, operation_type_id, description, new_values)
    SELECT NEW.created_by, 'Administracion', 'rate_base_history', (SELECT id FROM operation_types WHERE code = 'INSERT'),
           CONCAT('Nueva tarifa base: ', NEW.amount_per_hour, ' por hora'),
           JSON_OBJECT('amount_per_hour', NEW.amount_per_hour, 'start_date', NEW.start_date);
END//

CREATE TRIGGER after_rate_base_update
AFTER UPDATE ON rate_base_history
FOR EACH ROW
BEGIN
    INSERT INTO audit_log (user_id, module, entity, operation_type_id, description, previous_values, new_values)
    SELECT NEW.created_by, 'Administracion', 'rate_base_history', (SELECT id FROM operation_types WHERE code = 'UPDATE'),
           CONCAT('Tarifa base modificada de ', OLD.amount_per_hour, ' a ', NEW.amount_per_hour),
           JSON_OBJECT('amount_per_hour', OLD.amount_per_hour, 'is_active', OLD.is_active),
           JSON_OBJECT('amount_per_hour', NEW.amount_per_hour, 'is_active', NEW.is_active);
END//

DELIMITER ;

-- ============================================
-- VISTAS
-- ============================================

CREATE VIEW v_active_subscriptions AS
SELECT 
    s.id,
    s.user_id,
    CONCAT(u.first_name, ' ', u.last_name) AS customer_name,
    u.email AS customer_email,
    s.license_plate,
    spt.name AS plan_name,
    spt.code AS plan_code,
    sp.monthly_hours,
    s.consumed_hours,
    (sp.monthly_hours - s.consumed_hours) AS remaining_hours,
    ROUND((s.consumed_hours / sp.monthly_hours) * 100, 2) AS consumption_percentage,
    s.frozen_rate_base,
    sp.monthly_discount_percentage,
    sp.annual_additional_discount_percentage,
    s.is_annual,
    s.start_date,
    s.end_date,
    DATEDIFF(s.end_date, NOW()) AS days_until_expiration,
    sst.name AS status_name,
    s.notified_80_percent
FROM subscriptions s
JOIN users u ON s.user_id = u.id
JOIN subscription_plans sp ON s.plan_id = sp.id
JOIN subscription_plan_types spt ON sp.plan_type_id = spt.id
JOIN subscription_status_types sst ON s.status_type_id = sst.id
WHERE sst.code = 'ACTIVE';

CREATE VIEW v_active_tickets AS
SELECT 
    t.id,
    t.folio,
    t.license_plate,
    t.entry_time,
    b.name AS branch_name,
    b.address AS branch_address,
    vt.name AS vehicle_type_name,
    vt.code AS vehicle_type_code,
    t.is_subscriber,
    CASE 
        WHEN t.subscription_id IS NOT NULL THEN CONCAT(u.first_name, ' ', u.last_name)
        ELSE 'Cliente General'
    END AS customer_name,
    TIMESTAMPDIFF(MINUTE, t.entry_time, NOW()) AS minutes_parked,
    ROUND(TIMESTAMPDIFF(MINUTE, t.entry_time, NOW()) / 60.0, 2) AS hours_parked,
    tst.name AS status_name,
    t.has_incident
FROM tickets t
JOIN branches b ON t.branch_id = b.id
JOIN vehicle_types vt ON t.vehicle_type_id = vt.id
JOIN ticket_status_types tst ON t.status_type_id = tst.id
LEFT JOIN subscriptions s ON t.subscription_id = s.id
LEFT JOIN users u ON s.user_id = u.id
WHERE tst.code = 'IN_PROGRESS';

CREATE VIEW v_branch_occupancy AS
SELECT 
    b.id AS branch_id,
    b.name AS branch_name,
    b.capacity_2r,
    b.capacity_4r,
    COUNT(CASE WHEN vt.code = '2R' THEN 1 END) AS current_2r,
    COUNT(CASE WHEN vt.code = '4R' THEN 1 END) AS current_4r,
    b.capacity_2r - COUNT(CASE WHEN vt.code = '2R' THEN 1 END) AS available_2r,
    b.capacity_4r - COUNT(CASE WHEN vt.code = '4R' THEN 1 END) AS available_4r,
    ROUND((COUNT(CASE WHEN vt.code = '2R' THEN 1 END) * 100.0 / b.capacity_2r), 1) AS occupancy_2r_percentage,
    ROUND((COUNT(CASE WHEN vt.code = '4R' THEN 1 END) * 100.0 / b.capacity_4r), 1) AS occupancy_4r_percentage
FROM branches b
LEFT JOIN tickets t ON b.id = t.branch_id 
JOIN ticket_status_types tst ON t.status_type_id = tst.id
LEFT JOIN vehicle_types vt ON t.vehicle_type_id = vt.id
WHERE b.is_active = TRUE AND (t.id IS NULL OR tst.code = 'IN_PROGRESS')
GROUP BY b.id, b.name, b.capacity_2r, b.capacity_4r;

CREATE VIEW v_pending_business_settlements AS
SELECT 
    ab.id AS business_id,
    ab.name AS business_name,
    b.id AS branch_id,
    b.name AS branch_name,
    SUM(bfh.granted_hours) AS total_hours,
    COUNT(DISTINCT bfh.ticket_id) AS tickets_count,
    ab.rate_per_hour,
    SUM(bfh.granted_hours * ab.rate_per_hour) AS amount_to_charge,
    MIN(bfh.granted_at) AS oldest_grant_date,
    MAX(bfh.granted_at) AS latest_grant_date
FROM business_free_hours bfh
JOIN affiliated_businesses ab ON bfh.business_id = ab.id
JOIN branches b ON bfh.branch_id = b.id
WHERE bfh.is_settled = FALSE
GROUP BY ab.id, ab.name, b.id, b.name, ab.rate_per_hour;

CREATE VIEW v_fleet_statistics AS
SELECT 
    fc.id AS company_id,
    fc.name AS company_name,
    fc.tax_id,
    fc.corporate_discount_percentage,
    fc.plate_limit,
    fc.months_unpaid,
    COUNT(fv.id) AS total_vehicles,
    COUNT(CASE WHEN fv.is_active = TRUE THEN 1 END) AS active_vehicles,
    fc.plate_limit - COUNT(CASE WHEN fv.is_active = TRUE THEN 1 END) AS available_slots,
    CASE 
        WHEN fc.months_unpaid >= 2 THEN 'BLOQUEADO'
        WHEN fc.months_unpaid = 1 THEN 'ADVERTENCIA'
        ELSE 'ACTIVO'
    END AS payment_status
FROM fleet_companies fc
LEFT JOIN fleet_vehicles fv ON fc.id = fv.company_id
WHERE fc.is_active = TRUE
GROUP BY fc.id, fc.name, fc.tax_id, fc.corporate_discount_percentage, fc.plate_limit, fc.months_unpaid;

CREATE VIEW v_subscription_plans_ordered AS
SELECT 
    spt.id,
    spt.code,
    spt.name,
    spt.sort_order,
    sp.monthly_hours,
    sp.monthly_discount_percentage,
    sp.annual_additional_discount_percentage,
    ROUND(sp.monthly_discount_percentage + (sp.annual_additional_discount_percentage / 12), 2) AS effective_discount,
    sp.description,
    sp.is_active,
    sp.created_at,
    sp.updated_at
FROM subscription_plan_types spt
LEFT JOIN subscription_plans sp ON spt.id = sp.plan_type_id
ORDER BY 
    CASE WHEN sp.is_active = TRUE 
    THEN (sp.monthly_discount_percentage + (sp.annual_additional_discount_percentage / 12))
    ELSE 0 END DESC,
    spt.sort_order ASC;

SELECT '============================================' AS '';
SELECT 'SCRIPT DE LOGICA EJECUTADO EXITOSAMENTE' AS status;
SELECT '============================================' AS '';
SELECT 'Funciones: 4' AS info;
SELECT 'Procedimientos: 10' AS '';
SELECT 'Triggers: 20' AS '';
SELECT 'Vistas: 6' AS '';
SELECT '============================================' AS '';