-- ============================================
-- PARKCONTROL S.A. - SCRIPT DDL (ESTRUCTURA)
-- ============================================

DROP DATABASE IF EXISTS parkcontrol_db;
CREATE DATABASE parkcontrol_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE parkcontrol_db;

CREATE TABLE role_types (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL UNIQUE,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE subscription_plan_types (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL UNIQUE,
    code VARCHAR(20) NOT NULL UNIQUE,
    sort_order INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE vehicle_types (
    id INT PRIMARY KEY AUTO_INCREMENT,
    code VARCHAR(10) NOT NULL UNIQUE,
    name VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE ticket_status_types (
    id INT PRIMARY KEY AUTO_INCREMENT,
    code VARCHAR(20) NOT NULL UNIQUE,
    name VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE subscription_status_types (
    id INT PRIMARY KEY AUTO_INCREMENT,
    code VARCHAR(20) NOT NULL UNIQUE,
    name VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE change_request_status_types (
    id INT PRIMARY KEY AUTO_INCREMENT,
    code VARCHAR(20) NOT NULL UNIQUE,
    name VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE temporal_permit_status_types (
    id INT PRIMARY KEY AUTO_INCREMENT,
    code VARCHAR(20) NOT NULL UNIQUE,
    name VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE benefit_types (
    id INT PRIMARY KEY AUTO_INCREMENT,
    code VARCHAR(30) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE settlement_period_types (
    id INT PRIMARY KEY AUTO_INCREMENT,
    code VARCHAR(20) NOT NULL UNIQUE,
    name VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE operation_types (
    id INT PRIMARY KEY AUTO_INCREMENT,
    code VARCHAR(20) NOT NULL UNIQUE,
    name VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE incident_types (
    id INT PRIMARY KEY AUTO_INCREMENT,
    code VARCHAR(30) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    requires_documentation BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE plate_change_reasons (
    id INT PRIMARY KEY AUTO_INCREMENT,
    code VARCHAR(30) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    requires_evidence BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE document_types (
    id INT PRIMARY KEY AUTO_INCREMENT,
    code VARCHAR(30) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    phone VARCHAR(20),
    role_type_id INT NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    requires_password_change BOOLEAN DEFAULT TRUE,
    has_2fa_enabled BOOLEAN DEFAULT FALSE,
    failed_login_attempts INT DEFAULT 0,
    locked_until DATETIME NULL,
    last_login DATETIME NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_users_role FOREIGN KEY (role_type_id) REFERENCES role_types(id) ON DELETE RESTRICT ON UPDATE CASCADE,
    INDEX idx_email (email),
    INDEX idx_role (role_type_id),
    INDEX idx_active (is_active),
    INDEX idx_role_active (role_type_id, is_active),
    CONSTRAINT chk_email_format CHECK (email REGEXP '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$'),
    CONSTRAINT chk_phone_format CHECK (phone IS NULL OR phone REGEXP '^[0-9]{8,15}$'),
    CONSTRAINT chk_failed_attempts CHECK (failed_login_attempts >= 0 AND failed_login_attempts <= 10)
) ENGINE=InnoDB;

CREATE TABLE password_history (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_password_history_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE ON UPDATE CASCADE,
    INDEX idx_user (user_id),
    INDEX idx_user_created (user_id, created_at DESC)
) ENGINE=InnoDB;

CREATE TABLE password_reset_tokens (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    token VARCHAR(6) NOT NULL UNIQUE,
    expires_at DATETIME NOT NULL,
    is_used BOOLEAN NOT NULL DEFAULT FALSE,
    used_at DATETIME NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_password_reset_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE ON UPDATE CASCADE,
    INDEX idx_user_token (user_id, token),
    INDEX idx_expires_at (expires_at),
    INDEX idx_token (token)
) ENGINE=InnoDB;

CREATE TABLE rate_base_history (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    amount_per_hour DECIMAL(10,2) NOT NULL,
    start_date DATETIME NOT NULL,
    end_date DATETIME NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_by BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_rate_base_created_by FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE RESTRICT ON UPDATE CASCADE,
    INDEX idx_active (is_active),
    INDEX idx_dates (start_date, end_date),
    INDEX idx_created_by (created_by),
    CONSTRAINT chk_positive_amount CHECK (amount_per_hour > 0),
    CONSTRAINT chk_date_range CHECK (end_date IS NULL OR end_date > start_date)
) ENGINE=InnoDB;

CREATE TABLE branches (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(200) NOT NULL UNIQUE,
    address TEXT NOT NULL,
    opening_time TIME NOT NULL,
    closing_time TIME NOT NULL,
    capacity_2r INT NOT NULL,
    capacity_4r INT NOT NULL,
    rate_per_hour DECIMAL(10,2) NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_name (name),
    INDEX idx_active (is_active),
    CONSTRAINT chk_capacity_2r CHECK (capacity_2r > 0),
    CONSTRAINT chk_capacity_4r CHECK (capacity_4r > 0),
    CONSTRAINT chk_rate CHECK (rate_per_hour IS NULL OR rate_per_hour > 0),
    CONSTRAINT chk_hours CHECK (closing_time > opening_time)
) ENGINE=InnoDB;

CREATE TABLE subscription_plans (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    plan_type_id INT NOT NULL,
    monthly_hours INT NOT NULL,
    monthly_discount_percentage DECIMAL(5,2) NOT NULL,
    annual_additional_discount_percentage DECIMAL(5,2) NOT NULL,
    description TEXT,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_subscription_plans_type FOREIGN KEY (plan_type_id) REFERENCES subscription_plan_types(id) ON DELETE RESTRICT ON UPDATE CASCADE,
    UNIQUE KEY uk_plan_type (plan_type_id),
    INDEX idx_active (is_active),
    CONSTRAINT chk_monthly_hours CHECK (monthly_hours >= 0),
    CONSTRAINT chk_monthly_discount CHECK (monthly_discount_percentage >= 0 AND monthly_discount_percentage <= 100),
    CONSTRAINT chk_annual_discount CHECK (annual_additional_discount_percentage >= 0 AND annual_additional_discount_percentage <= 100)
) ENGINE=InnoDB;

CREATE TABLE vehicles (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    license_plate VARCHAR(20) NOT NULL,
    vehicle_type_id INT NOT NULL,
    brand VARCHAR(100),
    model VARCHAR(100),
    color VARCHAR(50),
    year INT,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_vehicles_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_vehicles_vehicle_type FOREIGN KEY (vehicle_type_id) REFERENCES vehicle_types(id) ON DELETE RESTRICT ON UPDATE CASCADE,
    UNIQUE KEY uk_user_plate (user_id, license_plate),
    INDEX idx_user (user_id),
    INDEX idx_plate (license_plate),
    INDEX idx_active (is_active),
    INDEX idx_user_active (user_id, is_active),
    CONSTRAINT chk_vehicle_plate_format CHECK (license_plate REGEXP '^[A-Z]{1,3}-?[0-9]{3,4}$|^[A-Z]{1,3}[0-9]{3,4}$|^P-[0-9]{5,6}$'),
    CONSTRAINT chk_vehicle_year CHECK (year IS NULL OR (year >= 1900 AND year <= 2050))
) ENGINE=InnoDB;

CREATE TABLE subscriptions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    plan_id BIGINT NOT NULL,
    license_plate VARCHAR(20) NOT NULL,
    frozen_rate_base DECIMAL(10,2) NOT NULL,
    purchase_date DATETIME NOT NULL,
    start_date DATETIME NOT NULL,
    end_date DATETIME NOT NULL,
    consumed_hours DECIMAL(10,2) DEFAULT 0,
    status_type_id INT NOT NULL,
    is_annual BOOLEAN DEFAULT FALSE,
    notified_80_percent BOOLEAN DEFAULT FALSE,
    auto_renew_enabled BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_subscriptions_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_subscriptions_plan FOREIGN KEY (plan_id) REFERENCES subscription_plans(id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_subscriptions_status FOREIGN KEY (status_type_id) REFERENCES subscription_status_types(id) ON DELETE RESTRICT ON UPDATE CASCADE,
    INDEX idx_user (user_id),
    INDEX idx_plate (license_plate),
    INDEX idx_status (status_type_id),
    INDEX idx_dates (start_date, end_date),
    INDEX idx_plate_status_dates (license_plate, status_type_id, end_date),
    INDEX idx_user_status (user_id, status_type_id),
    CONSTRAINT chk_frozen_rate CHECK (frozen_rate_base > 0),
    CONSTRAINT chk_consumed_hours CHECK (consumed_hours >= 0),
    CONSTRAINT chk_date_range_sub CHECK (end_date > start_date),
    CONSTRAINT chk_purchase_before_start CHECK (purchase_date <= start_date),
    CONSTRAINT chk_plate_format CHECK (license_plate REGEXP '^[A-Z]{1,3}-?[0-9]{3,4}$|^[A-Z]{1,3}[0-9]{3,4}$|^P-[0-9]{5,6}$')
) ENGINE=InnoDB;

CREATE TABLE affiliated_businesses (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(200) NOT NULL,
    tax_id VARCHAR(50) NOT NULL UNIQUE,
    contact_name VARCHAR(200),
    email VARCHAR(255),
    phone VARCHAR(20),
    rate_per_hour DECIMAL(10,2) NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_name (name),
    INDEX idx_active (is_active),
    INDEX idx_tax_id (tax_id),
    CONSTRAINT chk_business_rate CHECK (rate_per_hour > 0),
    CONSTRAINT chk_business_email CHECK (email IS NULL OR email REGEXP '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$')
) ENGINE=InnoDB;

CREATE TABLE branch_businesses (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    business_id BIGINT NOT NULL,
    branch_id BIGINT NOT NULL,
    benefit_type_id INT NOT NULL,
    settlement_period_type_id INT NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_branch_businesses_business FOREIGN KEY (business_id) REFERENCES affiliated_businesses(id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_branch_businesses_branch FOREIGN KEY (branch_id) REFERENCES branches(id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_branch_businesses_benefit FOREIGN KEY (benefit_type_id) REFERENCES benefit_types(id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_branch_businesses_period FOREIGN KEY (settlement_period_type_id) REFERENCES settlement_period_types(id) ON DELETE RESTRICT ON UPDATE CASCADE,
    UNIQUE KEY uk_business_branch (business_id, branch_id),
    INDEX idx_branch (branch_id),
    INDEX idx_active (is_active),
    INDEX idx_branch_active (branch_id, is_active),
    INDEX idx_business_active (business_id, is_active)
) ENGINE=InnoDB;

CREATE TABLE tickets (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    branch_id BIGINT NOT NULL,
    folio VARCHAR(50) NOT NULL,
    license_plate VARCHAR(20) NOT NULL,
    vehicle_type_id INT NOT NULL,
    entry_time DATETIME NOT NULL,
    exit_time DATETIME NULL,
    subscription_id BIGINT NULL,
    is_subscriber BOOLEAN DEFAULT FALSE,
    has_incident BOOLEAN DEFAULT FALSE,
    status_type_id INT NOT NULL,
    qr_code VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_tickets_branch FOREIGN KEY (branch_id) REFERENCES branches(id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_tickets_vehicle_type FOREIGN KEY (vehicle_type_id) REFERENCES vehicle_types(id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_tickets_subscription FOREIGN KEY (subscription_id) REFERENCES subscriptions(id) ON DELETE SET NULL ON UPDATE CASCADE,
    CONSTRAINT fk_tickets_status FOREIGN KEY (status_type_id) REFERENCES ticket_status_types(id) ON DELETE RESTRICT ON UPDATE CASCADE,
    UNIQUE KEY uk_branch_folio (branch_id, folio),
    INDEX idx_plate (license_plate),
    INDEX idx_entry (entry_time),
    INDEX idx_status (status_type_id),
    INDEX idx_branch_status_entry (branch_id, status_type_id, entry_time),
    INDEX idx_plate_status (license_plate, status_type_id),
    INDEX idx_subscription (subscription_id),
    INDEX idx_branch_entry (branch_id, entry_time),
    CONSTRAINT chk_exit_after_entry CHECK (exit_time IS NULL OR exit_time > entry_time),
    CONSTRAINT chk_ticket_plate_format CHECK (license_plate REGEXP '^[A-Z]{1,3}-?[0-9]{3,4}$|^[A-Z]{1,3}[0-9]{3,4}$|^P-[0-9]{5,6}$')
) ENGINE=InnoDB;

CREATE TABLE ticket_charges (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    ticket_id BIGINT NOT NULL,
    total_hours DECIMAL(10,2) NOT NULL,
    free_hours_granted DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    billable_hours DECIMAL(10,2) NOT NULL,
    rate_applied DECIMAL(10,2) NOT NULL,
    subtotal DECIMAL(10,2) NOT NULL,
    subscription_hours_consumed DECIMAL(10,2) DEFAULT 0.00,
    subscription_overage_hours DECIMAL(10,2) DEFAULT 0.00,
    subscription_overage_charge DECIMAL(10,2) DEFAULT 0.00,
    total_amount DECIMAL(10,2) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_ticket_charges_ticket FOREIGN KEY (ticket_id) REFERENCES tickets(id) ON DELETE CASCADE ON UPDATE CASCADE,
    UNIQUE KEY uk_ticket_charge (ticket_id),
    INDEX idx_created_at (created_at),
    CONSTRAINT chk_total_hours CHECK (total_hours >= 0),
    CONSTRAINT chk_free_hours CHECK (free_hours_granted >= 0),
    CONSTRAINT chk_billable_hours CHECK (billable_hours >= 0),
    CONSTRAINT chk_rate CHECK (rate_applied >= 0),
    CONSTRAINT chk_subtotal CHECK (subtotal >= 0),
    CONSTRAINT chk_total_amount CHECK (total_amount >= 0)
) ENGINE=InnoDB;

CREATE TABLE business_free_hours (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    ticket_id BIGINT NOT NULL,
    business_id BIGINT NOT NULL,
    branch_id BIGINT NOT NULL,
    granted_hours DECIMAL(10,2) NOT NULL,
    granted_at DATETIME NOT NULL,
    is_settled BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_business_free_hours_ticket FOREIGN KEY (ticket_id) REFERENCES tickets(id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_business_free_hours_business FOREIGN KEY (business_id) REFERENCES affiliated_businesses(id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_business_free_hours_branch FOREIGN KEY (branch_id) REFERENCES branches(id) ON DELETE RESTRICT ON UPDATE CASCADE,
    INDEX idx_ticket (ticket_id),
    INDEX idx_business (business_id),
    INDEX idx_settled (is_settled),
    INDEX idx_granted_at (granted_at),
    INDEX idx_business_settled_date (business_id, is_settled, granted_at),
    INDEX idx_branch_settled_date (branch_id, is_settled, granted_at),
    CONSTRAINT chk_granted_hours CHECK (granted_hours > 0)
) ENGINE=InnoDB;

CREATE TABLE business_settlement_history (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    business_id BIGINT NOT NULL,
    branch_id BIGINT NOT NULL,
    period_start DATETIME NOT NULL,
    period_end DATETIME NOT NULL,
    total_hours DECIMAL(10,2) NOT NULL,
    total_amount DECIMAL(10,2) NOT NULL,
    ticket_count INT NOT NULL,
    settled_at DATETIME NOT NULL,
    settled_by BIGINT NOT NULL,
    observations TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_settlement_business FOREIGN KEY (business_id) REFERENCES affiliated_businesses(id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_settlement_branch FOREIGN KEY (branch_id) REFERENCES branches(id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_settlement_settled_by FOREIGN KEY (settled_by) REFERENCES users(id) ON DELETE RESTRICT ON UPDATE CASCADE,
    INDEX idx_business (business_id),
    INDEX idx_branch (branch_id),
    INDEX idx_settled_at (settled_at),
    INDEX idx_business_period (business_id, period_start, period_end),
    INDEX idx_branch_period (branch_id, period_start, period_end),
    CONSTRAINT chk_settlement_total_hours CHECK (total_hours >= 0),
    CONSTRAINT chk_settlement_total_amount CHECK (total_amount >= 0),
    CONSTRAINT chk_settlement_ticket_count CHECK (ticket_count >= 0),
    CONSTRAINT chk_settlement_period CHECK (period_end > period_start)
) ENGINE=InnoDB;

CREATE TABLE settlement_tickets (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    settlement_id BIGINT NOT NULL,
    ticket_id BIGINT NOT NULL,
    free_hours_granted DECIMAL(10,2) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_settlement_tickets_settlement FOREIGN KEY (settlement_id) REFERENCES business_settlement_history(id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_settlement_tickets_ticket FOREIGN KEY (ticket_id) REFERENCES tickets(id) ON DELETE RESTRICT ON UPDATE CASCADE,
    UNIQUE KEY uk_settlement_ticket (settlement_id, ticket_id),
    INDEX idx_settlement (settlement_id),
    INDEX idx_ticket (ticket_id),
    CONSTRAINT chk_settlement_ticket_hours CHECK (free_hours_granted > 0)
) ENGINE=InnoDB;

CREATE TABLE subscription_overages (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    subscription_id BIGINT NOT NULL,
    ticket_id BIGINT NOT NULL,
    overage_hours DECIMAL(10,2) NOT NULL,
    charged_amount DECIMAL(10,2) NOT NULL,
    applied_rate DECIMAL(10,2) NOT NULL,
    charged_at DATETIME NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_overages_subscription FOREIGN KEY (subscription_id) REFERENCES subscriptions(id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_overages_ticket FOREIGN KEY (ticket_id) REFERENCES tickets(id) ON DELETE CASCADE ON UPDATE CASCADE,
    INDEX idx_subscription (subscription_id),
    INDEX idx_ticket (ticket_id),
    INDEX idx_charged_at (charged_at),
    INDEX idx_subscription_charged (subscription_id, charged_at),
    CONSTRAINT chk_overage_hours CHECK (overage_hours > 0),
    CONSTRAINT chk_charged_amount CHECK (charged_amount >= 0),
    CONSTRAINT chk_applied_rate CHECK (applied_rate > 0)
) ENGINE=InnoDB;

CREATE TABLE stored_files (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    document_type_id INT NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    file_url TEXT NOT NULL,
    container_name VARCHAR(100) NOT NULL,
    blob_name VARCHAR(255) NOT NULL,
    file_size_bytes BIGINT,
    mime_type VARCHAR(100),
    uploaded_by BIGINT,
    uploaded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_stored_files_document_type FOREIGN KEY (document_type_id) REFERENCES document_types(id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_stored_files_uploaded_by FOREIGN KEY (uploaded_by) REFERENCES users(id) ON DELETE SET NULL ON UPDATE CASCADE,
    INDEX idx_document_type (document_type_id),
    INDEX idx_uploaded_by (uploaded_by),
    INDEX idx_uploaded_at (uploaded_at),
    INDEX idx_container_blob (container_name(50), blob_name(100)),
    CONSTRAINT chk_file_size CHECK (file_size_bytes IS NULL OR file_size_bytes > 0)
) ENGINE=InnoDB;

CREATE TABLE incidents (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    ticket_id BIGINT NULL,
    branch_id BIGINT NOT NULL,
    incident_type_id INT NOT NULL,
    reported_by_user_id BIGINT NOT NULL,
    license_plate VARCHAR(20) NOT NULL,
    description TEXT,
    resolution_notes TEXT,
    is_resolved BOOLEAN NOT NULL DEFAULT FALSE,
    resolved_by_user_id BIGINT NULL,
    resolved_at DATETIME NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_incidents_ticket FOREIGN KEY (ticket_id) REFERENCES tickets(id) ON DELETE SET NULL ON UPDATE CASCADE,
    CONSTRAINT fk_incidents_branch FOREIGN KEY (branch_id) REFERENCES branches(id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_incidents_type FOREIGN KEY (incident_type_id) REFERENCES incident_types(id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_incidents_reported_by FOREIGN KEY (reported_by_user_id) REFERENCES users(id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_incidents_resolved_by FOREIGN KEY (resolved_by_user_id) REFERENCES users(id) ON DELETE SET NULL ON UPDATE CASCADE,
    INDEX idx_ticket (ticket_id),
    INDEX idx_branch (branch_id),
    INDEX idx_license_plate (license_plate),
    INDEX idx_created_at (created_at),
    INDEX idx_branch_created (branch_id, created_at),
    INDEX idx_reported_by (reported_by_user_id),
    INDEX idx_resolved (is_resolved)
) ENGINE=InnoDB;

CREATE TABLE incident_files (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    incident_id BIGINT NOT NULL,
    stored_file_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_incident_files_incident FOREIGN KEY (incident_id) REFERENCES incidents(id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_incident_files_file FOREIGN KEY (stored_file_id) REFERENCES stored_files(id) ON DELETE CASCADE ON UPDATE CASCADE,
    UNIQUE KEY uk_incident_file (incident_id, stored_file_id),
    INDEX idx_incident (incident_id),
    INDEX idx_file (stored_file_id)
) ENGINE=InnoDB;

CREATE TABLE plate_change_requests (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    subscription_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    old_license_plate VARCHAR(20) NOT NULL,
    new_license_plate VARCHAR(20) NOT NULL,
    reason_id INT NOT NULL,
    notes VARCHAR(500),
    status_id INT NOT NULL,
    reviewed_by BIGINT NULL,
    reviewed_at DATETIME NULL,
    review_notes VARCHAR(500),
    has_administrative_charge BOOLEAN DEFAULT FALSE,
    administrative_charge_amount DECIMAL(10,2) NULL,
    administrative_charge_reason VARCHAR(500) NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_plate_change_subscription FOREIGN KEY (subscription_id) REFERENCES subscriptions(id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_plate_change_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_plate_change_reason FOREIGN KEY (reason_id) REFERENCES plate_change_reasons(id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_plate_change_status FOREIGN KEY (status_id) REFERENCES change_request_status_types(id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_plate_change_reviewed_by FOREIGN KEY (reviewed_by) REFERENCES users(id) ON DELETE SET NULL ON UPDATE CASCADE,
    INDEX idx_subscription (subscription_id),
    INDEX idx_status (status_id),
    INDEX idx_created_at (created_at),
    INDEX idx_subscription_status (subscription_id, status_id),
    CONSTRAINT chk_old_plate_format CHECK (old_license_plate REGEXP '^[A-Z]{1,3}-?[0-9]{3,4}$|^[A-Z]{1,3}[0-9]{3,4}$|^P-[0-9]{5,6}$'),
    CONSTRAINT chk_new_plate_format CHECK (new_license_plate REGEXP '^[A-Z]{1,3}-?[0-9]{3,4}$|^[A-Z]{1,3}[0-9]{3,4}$|^P-[0-9]{5,6}$'),
    CONSTRAINT chk_different_plates CHECK (old_license_plate != new_license_plate),
    CONSTRAINT chk_administrative_charge_amount CHECK (administrative_charge_amount IS NULL OR administrative_charge_amount >= 0)
) ENGINE=InnoDB;

CREATE TABLE plate_change_files (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    plate_change_request_id BIGINT NOT NULL,
    stored_file_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_plate_change_files_request FOREIGN KEY (plate_change_request_id) REFERENCES plate_change_requests(id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_plate_change_files_file FOREIGN KEY (stored_file_id) REFERENCES stored_files(id) ON DELETE CASCADE ON UPDATE CASCADE,
    UNIQUE KEY uk_change_file (plate_change_request_id, stored_file_id),
    INDEX idx_change_request (plate_change_request_id),
    INDEX idx_file (stored_file_id)
) ENGINE=InnoDB;

CREATE TABLE change_request_evidences (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    change_request_id BIGINT NOT NULL,
    document_type_id INT NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    file_url VARCHAR(500) NOT NULL,
    file_size BIGINT,
    uploaded_by VARCHAR(255) NOT NULL,
    uploaded_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_change_evidence_request FOREIGN KEY (change_request_id) REFERENCES plate_change_requests(id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_change_evidence_document_type FOREIGN KEY (document_type_id) REFERENCES document_types(id) ON DELETE RESTRICT ON UPDATE CASCADE,
    INDEX idx_change_request_id (change_request_id),
    INDEX idx_document_type_id (document_type_id),
    INDEX idx_uploaded_at (uploaded_at),
    CONSTRAINT chk_file_size_positive CHECK (file_size IS NULL OR file_size > 0)
) ENGINE=InnoDB;

CREATE TABLE administrative_charge_config (
    id INT PRIMARY KEY AUTO_INCREMENT,
    reason_code VARCHAR(30) NOT NULL UNIQUE,
    description VARCHAR(255) NOT NULL,
    charge_amount DECIMAL(10,2) NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT chk_charge_positive CHECK (charge_amount >= 0),
    INDEX idx_reason_code (reason_code),
    INDEX idx_is_active (is_active)
) ENGINE=InnoDB;

CREATE TABLE temporal_permits (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    subscription_id BIGINT NOT NULL,
    temporal_plate VARCHAR(20) NOT NULL,
    start_date DATETIME NOT NULL,
    end_date DATETIME NOT NULL,
    max_uses INT NOT NULL,
    current_uses INT DEFAULT 0,
    allowed_branches JSON NULL,
    vehicle_type_id INT NOT NULL,
    status_type_id INT NOT NULL,
    approved_by BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_temporal_permits_subscription FOREIGN KEY (subscription_id) REFERENCES subscriptions(id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_temporal_permits_vehicle_type FOREIGN KEY (vehicle_type_id) REFERENCES vehicle_types(id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_temporal_permits_status FOREIGN KEY (status_type_id) REFERENCES temporal_permit_status_types(id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_temporal_permits_approved_by FOREIGN KEY (approved_by) REFERENCES users(id) ON DELETE RESTRICT ON UPDATE CASCADE,
    INDEX idx_subscription (subscription_id),
    INDEX idx_temporal_plate (temporal_plate),
    INDEX idx_status (status_type_id),
    INDEX idx_dates (start_date, end_date),
    INDEX idx_plate_status_dates (temporal_plate, status_type_id, end_date),
    CONSTRAINT chk_max_uses CHECK (max_uses > 0 AND max_uses <= 20),
    CONSTRAINT chk_current_uses CHECK (current_uses >= 0 AND current_uses <= max_uses),
    CONSTRAINT chk_permit_dates CHECK (end_date > start_date),
    CONSTRAINT chk_permit_duration CHECK (DATEDIFF(end_date, start_date) <= 30),
    CONSTRAINT chk_temporal_plate_format CHECK (temporal_plate REGEXP '^[A-Z]{1,3}-?[0-9]{3,4}$|^[A-Z]{1,3}[0-9]{3,4}$|^P-[0-9]{5,6}$')
) ENGINE=InnoDB;

CREATE TABLE fleet_companies (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(200) NOT NULL,
    tax_id VARCHAR(50) NOT NULL UNIQUE,
    contact_name VARCHAR(200),
    corporate_email VARCHAR(255) NOT NULL,
    phone VARCHAR(20),
    corporate_discount_percentage DECIMAL(5,2) NOT NULL,
    plate_limit INT NOT NULL,
    billing_period VARCHAR(20) NOT NULL,
    months_unpaid INT DEFAULT 0,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    admin_user_id BIGINT NULL COMMENT 'ID del usuario administrador de la flotilla',
    INDEX idx_name (name),
    INDEX idx_active (is_active),
    CONSTRAINT chk_corporate_discount CHECK (corporate_discount_percentage >= 0 AND corporate_discount_percentage <= 10),
    CONSTRAINT chk_plate_limit CHECK (plate_limit > 0 AND plate_limit <= 50),
    CONSTRAINT chk_months_unpaid CHECK (months_unpaid >= 0),
    CONSTRAINT chk_fleet_email CHECK (corporate_email REGEXP '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}'),
    CONSTRAINT fk_fleet_company_admin_user FOREIGN KEY (admin_user_id) REFERENCES users(id) ON DELETE SET NULL ON UPDATE CASCADE,
    INDEX idx_fleet_companies_admin_user (admin_user_id)
) ENGINE=InnoDB;

CREATE TABLE fleet_vehicles (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    company_id BIGINT NOT NULL,
    license_plate VARCHAR(20) NOT NULL,
    plan_id BIGINT NOT NULL,
    vehicle_type_id INT NOT NULL,
    assigned_employee VARCHAR(200),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_fleet_vehicles_company FOREIGN KEY (company_id) REFERENCES fleet_companies(id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_fleet_vehicles_plan FOREIGN KEY (plan_id) REFERENCES subscription_plans(id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_fleet_vehicles_vehicle_type FOREIGN KEY (vehicle_type_id) REFERENCES vehicle_types(id) ON DELETE RESTRICT ON UPDATE CASCADE,
    UNIQUE KEY uk_company_plate (company_id, license_plate),
    INDEX idx_company (company_id),
    INDEX idx_plate (license_plate),
    INDEX idx_active (is_active),
    INDEX idx_company_active (company_id, is_active),
    CONSTRAINT chk_fleet_plate_format CHECK (license_plate REGEXP '^[A-Z]{1,3}-?[0-9]{3,4}$|^[A-Z]{1,3}[0-9]{3,4}$|^P-[0-9]{5,6}$')
) ENGINE=InnoDB;

CREATE TABLE audit_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NULL,
    module VARCHAR(100) NOT NULL,
    entity VARCHAR(100) NOT NULL,
    operation_type_id INT NOT NULL,
    description TEXT NOT NULL,
    previous_values JSON,
    new_values JSON,
    client_ip VARCHAR(45),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_audit_log_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL ON UPDATE CASCADE,
    CONSTRAINT fk_audit_log_operation FOREIGN KEY (operation_type_id) REFERENCES operation_types(id) ON DELETE RESTRICT ON UPDATE CASCADE,
    INDEX idx_user (user_id),
    INDEX idx_module (module),
    INDEX idx_created_at (created_at),
    INDEX idx_module_created (module, created_at),
    INDEX idx_entity_created (entity, created_at)
) ENGINE=InnoDB;

SELECT '============================================' AS '';
SELECT 'SCRIPT DDL EJECUTADO EXITOSAMENTE' AS status;
SELECT '============================================' AS '';
SELECT 'Tablas creadas: 35' AS info;
SELECT '============================================' AS '';