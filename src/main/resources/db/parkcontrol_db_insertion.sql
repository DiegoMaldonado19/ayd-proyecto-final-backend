-- ============================================
-- PARKCONTROL S.A. - SCRIPT DATOS (INSERCIÓN)
-- ============================================

USE parkcontrol_db;

-- ============================================
-- DATOS DE CATÁLOGOS
-- ============================================

INSERT INTO role_types (name, description) VALUES
('Administrador', 'Administrador del sistema con acceso total'),
('Operador Sucursal', 'Operador de sucursal para control de entradas/salidas'),
('Operador Back Office', 'Operador para validaciones especiales'),
('Cliente', 'Cliente del sistema'),
('Administrador Flotilla', 'Administrador de flota empresarial');

INSERT INTO subscription_plan_types (name, code, sort_order) VALUES
('Full Access', 'FULL_ACCESS', 1),
('Workweek', 'WORKWEEK', 2),
('Office Light', 'OFFICE_LIGHT', 3),
('Diario Flexible', 'DAILY_FLEXIBLE', 4),
('Nocturno', 'NIGHT', 5);

INSERT INTO vehicle_types (code, name) VALUES
('2R', 'Dos Ruedas'),
('4R', 'Cuatro Ruedas');

INSERT INTO ticket_status_types (code, name) VALUES
('IN_PROGRESS', 'En Curso'),
('COMPLETED', 'Finalizado'),
('CANCELLED', 'Cancelado');

INSERT INTO subscription_status_types (code, name) VALUES
('ACTIVE', 'Activa'),
('EXPIRED', 'Vencida'),
('CANCELLED', 'Cancelada'),
('SUSPENDED', 'Suspendida');

INSERT INTO change_request_status_types (code, name) VALUES
('PENDING', 'Pendiente'),
('APPROVED', 'Aprobado'),
('REJECTED', 'Rechazado');

INSERT INTO temporal_permit_status_types (code, name) VALUES
('ACTIVE', 'Activo'),
('EXPIRED', 'Vencido'),
('REVOKED', 'Revocado');

INSERT INTO benefit_types (code, name, description) VALUES
('DIRECT_DISCOUNT', 'Descuento Directo', 'Reduce tiempo facturable a clientes sin suscripcion'),
('NO_CONSUME_HOURS', 'No Descontar Horas', 'No consume bolson de clientes con suscripcion');

INSERT INTO settlement_period_types (code, name) VALUES
('DAILY', 'Diario'),
('WEEKLY', 'Semanal'),
('MONTHLY', 'Mensual'),
('ANNUAL', 'Anual');

INSERT INTO operation_types (code, name) VALUES
('INSERT', 'Insercion'),
('UPDATE', 'Actualizacion'),
('DELETE', 'Eliminacion'),
('LOGIN', 'Inicio de Sesion'),
('LOGOUT', 'Cierre de Sesion');

INSERT INTO incident_types (code, name, requires_documentation) VALUES
('LOST_TICKET', 'Ticket Extraviado', TRUE),
('FRAUD_DETECTED', 'Fraude Detectado', TRUE),
('DAMAGED_VEHICLE', 'Vehiculo Danado', TRUE),
('OTHER', 'Otro', FALSE);

INSERT INTO plate_change_reasons (code, name, requires_evidence) VALUES
('THEFT', 'Robo', TRUE),
('SALE', 'Venta', TRUE),
('ACCIDENT', 'Siniestro', TRUE),
('OTHER', 'Otro', TRUE);

INSERT INTO document_types (code, name, description) VALUES
('IDENTIFICATION', 'Identificacion Personal', 'DPI, Pasaporte, Licencia'),
('VEHICLE_CARD', 'Tarjeta de Circulacion', 'Documento del vehiculo'),
('POLICE_REPORT', 'Denuncia Policial', 'Reporte oficial de robo'),
('TRANSFER_DOCUMENT', 'Documento de Traspaso', 'Compra-venta de vehiculo'),
('INSURANCE_REPORT', 'Reporte de Seguro', 'Documento de siniestro'),
('VEHICLE_PHOTO', 'Foto de Vehiculo', 'Fotografia de placa o vehiculo'),
('OTHER', 'Otro Documento', 'Documentacion adicional');

-- ============================================
-- USUARIOS DEL SISTEMA
-- Contraseña para todos: Password123$
-- ============================================

INSERT INTO users (email, password_hash, first_name, last_name, phone, role_type_id, is_active, requires_password_change, has_2fa_enabled) VALUES
('admin@parkcontrol.com', '$2a$12$bJVP7ttQby16rb6CEGlYq.lhAe1XXDxB/ryDHAuuZf9oDjZv4wgKq', 'Carlos', 'Rodriguez', '50245678901', 1, TRUE, FALSE, TRUE),
('op.centro@parkcontrol.com', '$2a$12$bJVP7ttQby16rb6CEGlYq.lhAe1XXDxB/ryDHAuuZf9oDjZv4wgKq', 'Ana', 'Lopez', '50245678902', 2, TRUE, FALSE, FALSE),
('op.plaza@parkcontrol.com', '$2a$12$bJVP7ttQby16rb6CEGlYq.lhAe1XXDxB/ryDHAuuZf9oDjZv4wgKq', 'Luis', 'Martinez', '50245678903', 2, TRUE, FALSE, FALSE),
('op.norte@parkcontrol.com', '$2a$12$bJVP7ttQby16rb6CEGlYq.lhAe1XXDxB/ryDHAuuZf9oDjZv4wgKq', 'Sofia', 'Garcia', '50245678904', 2, TRUE, FALSE, FALSE),
('backoffice@parkcontrol.com', '$2a$12$bJVP7ttQby16rb6CEGlYq.lhAe1XXDxB/ryDHAuuZf9oDjZv4wgKq', 'Pedro', 'Morales', '50245678905', 3, TRUE, FALSE, TRUE),
('maria.gonzalez@email.com', '$2a$12$bJVP7ttQby16rb6CEGlYq.lhAe1XXDxB/ryDHAuuZf9oDjZv4wgKq', 'Maria', 'Gonzalez', '50212345678', 4, TRUE, FALSE, TRUE),
('juan.perez@email.com', '$2a$12$bJVP7ttQby16rb6CEGlYq.lhAe1XXDxB/ryDHAuuZf9oDjZv4wgKq', 'Juan', 'Perez', '50212345679', 4, TRUE, FALSE, FALSE),
('carmen.ramirez@email.com', '$2a$12$bJVP7ttQby16rb6CEGlYq.lhAe1XXDxB/ryDHAuuZf9oDjZv4wgKq', 'Carmen', 'Ramirez', '50212345680', 4, TRUE, FALSE, FALSE),
('roberto.castro@email.com', '$2a$12$bJVP7ttQby16rb6CEGlYq.lhAe1XXDxB/ryDHAuuZf9oDjZv4wgKq', 'Roberto', 'Castro', '50212345681', 4, TRUE, FALSE, TRUE),
('lucia.fernandez@email.com', '$2a$12$bJVP7ttQby16rb6CEGlYq.lhAe1XXDxB/ryDHAuuZf9oDjZv4wgKq', 'Lucia', 'Fernandez', '50212345682', 4, TRUE, FALSE, FALSE),
('diego.sanchez@email.com', '$2a$12$bJVP7ttQby16rb6CEGlYq.lhAe1XXDxB/ryDHAuuZf9oDjZv4wgKq', 'Diego', 'Sanchez', '50212345683', 4, TRUE, FALSE, FALSE),
('patricia.mendez@email.com', '$2a$12$bJVP7ttQby16rb6CEGlYq.lhAe1XXDxB/ryDHAuuZf9oDjZv4wgKq', 'Patricia', 'Mendez', '50212345684', 4, TRUE, FALSE, FALSE),
('fernando.diaz@email.com', '$2a$12$bJVP7ttQby16rb6CEGlYq.lhAe1XXDxB/ryDHAuuZf9oDjZv4wgKq', 'Fernando', 'Diaz', '50212345685', 4, TRUE, FALSE, FALSE),
('andrea.torres@email.com', '$2a$12$bJVP7ttQby16rb6CEGlYq.lhAe1XXDxB/ryDHAuuZf9oDjZv4wgKq', 'Andrea', 'Torres', '50212345686', 4, TRUE, FALSE, FALSE),
('miguel.herrera@email.com', '$2a$12$bJVP7ttQby16rb6CEGlYq.lhAe1XXDxB/ryDHAuuZf9oDjZv4wgKq', 'Miguel', 'Herrera', '50212345687', 4, TRUE, FALSE, FALSE),
('visitante1@email.com', '$2a$12$bJVP7ttQby16rb6CEGlYq.lhAe1XXDxB/ryDHAuuZf9oDjZv4wgKq', 'Jorge', 'Mejia', '50298765432', 4, TRUE, FALSE, FALSE),
('visitante2@email.com', '$2a$12$bJVP7ttQby16rb6CEGlYq.lhAe1XXDxB/ryDHAuuZf9oDjZv4wgKq', 'Isabel', 'Ortiz', '50298765433', 4, TRUE, FALSE, FALSE),
('visitante3@email.com', '$2a$12$bJVP7ttQby16rb6CEGlYq.lhAe1XXDxB/ryDHAuuZf9oDjZv4wgKq', 'Raul', 'Vargas', '50298765434', 4, TRUE, FALSE, FALSE),
('visitante4@email.com', '$2a$12$bJVP7ttQby16rb6CEGlYq.lhAe1XXDxB/ryDHAuuZf9oDjZv4wgKq', 'Elena', 'Ruiz', '50298765435', 4, TRUE, FALSE, FALSE),
('visitante5@email.com', '$2a$12$bJVP7ttQby16rb6CEGlYq.lhAe1XXDxB/ryDHAuuZf9oDjZv4wgKq', 'Oscar', 'Navarro', '50298765436', 4, TRUE, FALSE, FALSE),
('admin.transportes@email.com', '$2a$12$bJVP7ttQby16rb6CEGlYq.lhAe1XXDxB/ryDHAuuZf9oDjZv4wgKq', 'Ricardo', 'Flores', '50234567890', 5, TRUE, FALSE, TRUE),
('admin.logistica@email.com', '$2a$12$bJVP7ttQby16rb6CEGlYq.lhAe1XXDxB/ryDHAuuZf9oDjZv4wgKq', 'Daniela', 'Vega', '50234567891', 5, TRUE, FALSE, FALSE);

INSERT INTO password_history (user_id, password_hash) VALUES
(1, '$2a$12$bJVP7ttQby16rb6CEGlYq.lhAe1XXDxB/ryDHAuuZf9oDjZv4wgKq'),
(2, '$2a$12$bJVP7ttQby16rb6CEGlYq.lhAe1XXDxB/ryDHAuuZf9oDjZv4wgKq'),
(3, '$2a$12$bJVP7ttQby16rb6CEGlYq.lhAe1XXDxB/ryDHAuuZf9oDjZv4wgKq'),
(4, '$2a$12$bJVP7ttQby16rb6CEGlYq.lhAe1XXDxB/ryDHAuuZf9oDjZv4wgKq'),
(5, '$2a$12$bJVP7ttQby16rb6CEGlYq.lhAe1XXDxB/ryDHAuuZf9oDjZv4wgKq');

INSERT INTO rate_base_history (amount_per_hour, start_date, end_date, is_active, created_by) VALUES
(12.00, '2025-01-01 00:00:00', NULL, TRUE, 1);

INSERT INTO branches (name, address, opening_time, closing_time, capacity_2r, capacity_4r, rate_per_hour, is_active) VALUES
('Sucursal Centro', '5ta Avenida 10-20 Zona 1, Ciudad de Guatemala', '06:00:00', '22:00:00', 50, 100, 15.00, TRUE),
('Sucursal Plaza', 'Avenida Las Americas 15-30 Zona 13, Ciudad de Guatemala', '07:00:00', '21:00:00', 30, 80, NULL, TRUE),
('Sucursal Norte', 'Calzada San Juan 8-50 Zona 7, Mixco', '06:00:00', '23:00:00', 40, 120, 12.00, TRUE);

INSERT INTO subscription_plans (plan_type_id, monthly_hours, monthly_discount_percentage, annual_additional_discount_percentage, description, is_active) VALUES
(1, 224, 20.00, 12.00, 'Acceso completo 7 dias a la semana, 8 horas diarias', TRUE),
(2, 160, 15.00, 6.00, 'Lunes a viernes, 8 horas diarias', TRUE),
(3, 80, 10.00, 5.00, 'Lunes a viernes, 4 horas diarias', TRUE),
(4, 112, 7.50, 4.00, 'Todos los dias, 4 horas diarias', TRUE),
(5, 0, 4.50, 3.00, 'Cobertura nocturna desde cierre hasta 1 hora antes de apertura', TRUE);

INSERT INTO subscriptions (user_id, plan_id, license_plate, frozen_rate_base, purchase_date, start_date, end_date, consumed_hours, status_type_id, is_annual) VALUES
(6, 1, 'ABC-123', 12.00, '2025-10-01 10:00:00', '2025-10-01 10:00:00', '2025-11-01 10:00:00', 45.50, 1, FALSE),
(7, 2, 'DEF-456', 12.00, '2025-10-02 11:30:00', '2025-10-02 11:30:00', '2025-11-02 11:30:00', 28.75, 1, FALSE),
(8, 3, 'GHI789', 12.00, '2025-10-03 09:15:00', '2025-10-03 09:15:00', '2025-11-03 09:15:00', 15.25, 1, FALSE),
(9, 4, 'JKL-012', 12.00, '2025-10-04 14:20:00', '2025-10-04 14:20:00', '2025-11-04 14:20:00', 32.00, 1, FALSE),
(10, 5, 'MNO345', 12.00, '2025-10-05 16:45:00', '2025-10-05 16:45:00', '2025-11-05 16:45:00', 0.00, 1, FALSE),
(11, 1, 'PQR-678', 12.00, '2025-01-15 10:00:00', '2025-01-15 10:00:00', '2026-01-15 10:00:00', 189.50, 1, TRUE),
(12, 2, 'STU901', 12.00, '2025-02-20 11:30:00', '2025-02-20 11:30:00', '2026-02-20 11:30:00', 95.25, 1, TRUE),
(13, 3, 'VWX-234', 12.00, '2025-03-10 09:15:00', '2025-03-10 09:15:00', '2026-03-10 09:15:00', 42.75, 1, TRUE),
(14, 4, 'YZA567', 12.00, '2025-04-05 14:20:00', '2025-04-05 14:20:00', '2026-04-05 14:20:00', 78.00, 1, TRUE),
(15, 1, 'BCD-890', 12.00, '2025-05-12 16:45:00', '2025-05-12 16:45:00', '2026-05-12 16:45:00', 156.25, 1, TRUE),
(16, 2, 'P-12345', 12.00, '2025-08-01 10:00:00', '2025-08-01 10:00:00', '2025-09-01 10:00:00', 160.00, 2, FALSE);

INSERT INTO affiliated_businesses (name, tax_id, contact_name, email, phone, rate_per_hour, is_active) VALUES
('Restaurant El Portal', '12345678-9', 'Mario Hernandez', 'contacto@elportal.com', '50223456789', 8.00, TRUE),
('Cine Estrella', '23456789-0', 'Laura Guzman', 'info@cineestrella.com', '50223456790', 10.00, TRUE),
('Supermercado Maxi', '34567890-1', 'Carlos Molina', 'gerencia@supermaxi.com', '50223456791', 6.00, TRUE);

INSERT INTO branch_businesses (business_id, branch_id, benefit_type_id, settlement_period_type_id, is_active) VALUES
(1, 1, 2, 3, TRUE),
(1, 2, 2, 3, TRUE),
(2, 2, 1, 3, TRUE),
(3, 1, 2, 2, TRUE),
(3, 2, 2, 2, TRUE),
(3, 3, 2, 2, TRUE);

INSERT INTO tickets (branch_id, folio, license_plate, vehicle_type_id, entry_time, exit_time, subscription_id, is_subscriber, has_incident, status_type_id, qr_code) VALUES
(1, 'CTR-001', 'ABC-123', 2, '2025-10-17 08:30:00', NULL, 1, TRUE, FALSE, 1, 'QR-CTR-001-20251017'),
(2, 'PLZ-001', 'DEF-456', 2, '2025-10-17 09:15:00', NULL, 2, TRUE, FALSE, 1, 'QR-PLZ-001-20251017'),
(1, 'CTR-002', 'GHI789', 2, '2025-10-17 10:00:00', NULL, 3, TRUE, FALSE, 1, 'QR-CTR-002-20251017'),
(1, 'CTR-003', 'VST-111', 2, '2025-10-17 11:00:00', NULL, NULL, FALSE, FALSE, 1, 'QR-CTR-003-20251017'),
(2, 'PLZ-002', 'VST222', 2, '2025-10-17 11:30:00', NULL, NULL, FALSE, FALSE, 1, 'QR-PLZ-002-20251017'),
(3, 'NRT-001', 'VST-333', 2, '2025-10-17 12:00:00', NULL, NULL, FALSE, FALSE, 1, 'QR-NRT-001-20251017'),
(1, 'CTR-004', 'VST444', 1, '2025-10-17 13:00:00', NULL, NULL, FALSE, FALSE, 1, 'QR-CTR-004-20251017'),
(2, 'PLZ-003', 'VST-555', 1, '2025-10-17 14:00:00', NULL, NULL, FALSE, FALSE, 1, 'QR-PLZ-003-20251017'),
(1, 'CTR-H001', 'ABC-123', 2, '2025-10-01 08:00:00', '2025-10-01 12:00:00', 1, TRUE, FALSE, 2, 'QR-CTR-H001'),
(1, 'CTR-H002', 'ABC-123', 2, '2025-10-02 08:30:00', '2025-10-02 13:30:00', 1, TRUE, FALSE, 2, 'QR-CTR-H002'),
(2, 'PLZ-H001', 'DEF-456', 2, '2025-10-01 09:00:00', '2025-10-01 17:00:00', 2, TRUE, FALSE, 2, 'QR-PLZ-H001'),
(3, 'NRT-H001', 'GHI789', 2, '2025-10-03 10:00:00', '2025-10-03 14:00:00', 3, TRUE, FALSE, 2, 'QR-NRT-H001'),
(1, 'CTR-H003', 'VST-111', 2, '2025-10-04 10:00:00', '2025-10-04 12:30:00', NULL, FALSE, FALSE, 2, 'QR-CTR-H003'),
(2, 'PLZ-H002', 'VST222', 2, '2025-10-05 11:00:00', '2025-10-05 15:00:00', NULL, FALSE, FALSE, 2, 'QR-PLZ-H002'),
(3, 'NRT-H002', 'VST-333', 2, '2025-10-06 09:00:00', '2025-10-06 18:00:00', NULL, FALSE, TRUE, 2, 'QR-NRT-H002');

INSERT INTO business_free_hours (ticket_id, business_id, branch_id, granted_hours, granted_at, is_settled) VALUES
(9, 1, 1, 2.00, '2025-10-01 10:00:00', FALSE),
(10, 1, 1, 1.50, '2025-10-02 11:00:00', FALSE),
(11, 1, 2, 2.00, '2025-10-01 12:00:00', FALSE),
(14, 2, 2, 3.00, '2025-10-05 13:00:00', FALSE),
(15, 3, 3, 1.00, '2025-10-06 10:00:00', FALSE);

INSERT INTO subscription_overages (subscription_id, ticket_id, overage_hours, charged_amount, applied_rate, charged_at) VALUES
(6, 11, 4.50, 67.50, 15.00, '2025-10-01 17:00:00');

INSERT INTO stored_files (document_type_id, file_name, file_url, container_name, blob_name, file_size_bytes, mime_type, uploaded_by, uploaded_at) VALUES
(1, 'dpi-jorge-mejia.jpg', 'https://parkcontrolstorage.blob.core.windows.net/incident-evidence/2025/10/dpi-jorge-mejia.jpg', 'incident-evidence', '2025/10/dpi-jorge-mejia.jpg', 245678, 'image/jpeg', 4, '2025-10-06 18:25:00'),
(2, 'tarjeta-circulacion-vst333.jpg', 'https://parkcontrolstorage.blob.core.windows.net/incident-evidence/2025/10/tarjeta-vst333.jpg', 'incident-evidence', '2025/10/tarjeta-vst333.jpg', 198432, 'image/jpeg', 4, '2025-10-06 18:26:00'),
(6, 'foto-vehiculo-vst333.jpg', 'https://parkcontrolstorage.blob.core.windows.net/incident-evidence/2025/10/foto-vst333.jpg', 'incident-evidence', '2025/10/foto-vst333.jpg', 312456, 'image/jpeg', 4, '2025-10-06 18:27:00');

INSERT INTO incidents (ticket_id, branch_id, incident_type_id, customer_name, identification_type, identification_number, operator_id, resolution, incident_date) VALUES
(15, 3, 1, 'Jorge Mejia', 'DPI', '2345678901234', 4, 'Ticket localizado por placa. Cliente presento documentos validos. Salida procesada normalmente.', '2025-10-06 18:30:00');

INSERT INTO incident_files (incident_id, stored_file_id) VALUES
(1, 1),
(1, 2),
(1, 3);

INSERT INTO plate_change_requests (subscription_id, old_plate, new_plate, reason_id, status_type_id, requested_at, reviewed_by, reviewed_at, effective_date, observations) VALUES
(5, 'MNO345', 'NEW-789', 1, 1, '2025-10-10 09:00:00', NULL, NULL, NULL, NULL);

INSERT INTO temporal_permits (subscription_id, temporal_plate, start_date, end_date, max_uses, current_uses, allowed_branches, vehicle_type_id, status_type_id, approved_by) VALUES
(3, 'TMP-999', '2025-10-01 00:00:00', '2025-10-31 23:59:59', 20, 5, JSON_ARRAY(1, 2, 3), 2, 1, 5),
(4, 'TMP-111', '2025-09-01 00:00:00', '2025-09-15 23:59:59', 15, 15, JSON_ARRAY(1, 2), 2, 2, 5);

INSERT INTO fleet_companies (name, tax_id, contact_name, corporate_email, phone, corporate_discount_percentage, plate_limit, billing_period, months_unpaid, is_active) VALUES
('Transportes Rapidos SA', '45678901-2', 'Ricardo Flores', 'admin@transportesrapidos.com', '50234567890', 5.00, 10, 'MONTHLY', 0, TRUE),
('Logistica Global GT', '56789012-3', 'Daniela Vega', 'contacto@logisticaglobal.com', '50234567891', 7.00, 15, 'MONTHLY', 0, TRUE),
('Empresa Morosa SA', '67890123-4', 'Carlos Impago', 'admin@morosa.com', '50234567892', 3.00, 5, 'MONTHLY', 2, TRUE);

INSERT INTO fleet_vehicles (company_id, license_plate, plan_id, vehicle_type_id, assigned_employee, is_active) VALUES
(1, 'FLT-001', 1, 2, 'Jose Ramirez', TRUE),
(1, 'FLT002', 1, 2, 'Ana Silva', TRUE),
(1, 'FLT-003', 2, 2, 'Carlos Mendez', TRUE),
(1, 'FLT004', 2, 2, 'Laura Gomez', TRUE),
(1, 'FLT-005', 2, 2, 'Pedro Martinez', TRUE),
(2, 'LOG-001', 1, 2, 'Roberto Diaz', TRUE),
(2, 'LOG002', 1, 2, 'Patricia Herrera', TRUE),
(2, 'LOG-003', 2, 2, 'Fernando Cruz', TRUE);

INSERT INTO audit_log (user_id, module, entity, operation_type_id, description, previous_values, new_values, client_ip, created_at) VALUES
(1, 'Administracion', 'rate_base_history', 1, 'Nueva tarifa base creada: 12.00 por hora', NULL, JSON_OBJECT('amount_per_hour', 12.00, 'start_date', '2025-01-01 00:00:00'), '192.168.1.100', '2025-01-01 08:00:00'),
(1, 'Administracion', 'branches', 1, 'Nueva sucursal creada: Sucursal Centro', NULL, JSON_OBJECT('name', 'Sucursal Centro', 'capacity_2r', 50, 'capacity_4r', 100), '192.168.1.100', '2025-01-02 09:00:00'),
(6, 'Suscripciones', 'subscriptions', 1, 'Usuario maria.gonzalez@email.com compro plan Full Access', NULL, JSON_OBJECT('user_id', 6, 'plan_id', 1, 'license_plate', 'ABC-123'), '192.168.1.150', '2025-10-01 10:00:00'),
(1, 'Seguridad', 'users', 5, 'Usuario admin@parkcontrol.com inicio sesion', NULL, NULL, '192.168.1.100', '2025-10-17 07:00:00');

-- ============================================
-- VERIFICACIÓN DE DATOS CARGADOS
-- ============================================

SELECT '============================================' AS '';
SELECT 'DATOS CARGADOS EXITOSAMENTE' AS status;
SELECT '============================================' AS '';
SELECT 'Usuarios' AS tabla, COUNT(*) AS cantidad FROM users
UNION ALL SELECT 'Sucursales', COUNT(*) FROM branches
UNION ALL SELECT 'Planes Activos', COUNT(*) FROM subscription_plans WHERE is_active = TRUE
UNION ALL SELECT 'Suscripciones Activas', COUNT(*) FROM subscriptions WHERE status_type_id = 1
UNION ALL SELECT 'Tickets Totales', COUNT(*) FROM tickets
UNION ALL SELECT 'Tickets Activos', COUNT(*) FROM tickets WHERE status_type_id = 1
UNION ALL SELECT 'Tickets Finalizados', COUNT(*) FROM tickets WHERE status_type_id = 2
UNION ALL SELECT 'Comercios Afiliados', COUNT(*) FROM affiliated_businesses
UNION ALL SELECT 'Horas Gratis Otorgadas', COUNT(*) FROM business_free_hours
UNION ALL SELECT 'Excedentes Cobrados', COUNT(*) FROM subscription_overages
UNION ALL SELECT 'Incidencias', COUNT(*) FROM incidents
UNION ALL SELECT 'Permisos Temporales', COUNT(*) FROM temporal_permits
UNION ALL SELECT 'Empresas Flotilla', COUNT(*) FROM fleet_companies
UNION ALL SELECT 'Vehiculos Flotilla', COUNT(*) FROM fleet_vehicles
UNION ALL SELECT 'Registros Auditoria', COUNT(*) FROM audit_log;

SELECT '============================================' AS '';
SELECT 'SISTEMA LISTO PARA USAR' AS status;
SELECT '============================================' AS '';