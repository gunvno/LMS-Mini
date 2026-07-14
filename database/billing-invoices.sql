-- Migration gộp invoice-service vào billing-service.
-- Script giữ nguyên database lms_invoice_service để có thể đối soát/rollback sau khi chuyển dữ liệu.
CREATE DATABASE IF NOT EXISTS lms_billing_service CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE lms_billing_service;

CREATE TABLE IF NOT EXISTS tbl_invoices (
    id VARCHAR(36) PRIMARY KEY,
    version BIGINT NOT NULL DEFAULT 0,
    created_by VARCHAR(50),
    created_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_modified_by VARCHAR(50),
    last_modified_date DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME NULL,
    payment_id VARCHAR(36) NOT NULL UNIQUE,
    invoice_code VARCHAR(50) NOT NULL UNIQUE,
    user_id VARCHAR(36) NOT NULL,
    course_id VARCHAR(36) NOT NULL,
    amount DECIMAL(18, 2) NOT NULL,
    provider VARCHAR(30) NOT NULL,
    provider_transaction_id VARCHAR(150),
    status VARCHAR(30) NOT NULL,
    issued_at DATETIME NOT NULL,
    paid_at DATETIME NULL,
    KEY idx_invoices_user_id (user_id),
    KEY idx_invoices_course_id (course_id),
    KEY idx_invoices_status (status),
    KEY idx_invoices_issued_at (issued_at),
    KEY idx_invoices_deleted_at (deleted_at)
);

-- Chuyển hóa đơn từ database invoice cũ nếu bảng cũ tồn tại.
SET @legacy_invoice_table_exists = (
    SELECT COUNT(*)
    FROM information_schema.tables
    WHERE table_schema = 'lms_invoice_service'
      AND table_name = 'tbl_invoices'
);

SET @copy_legacy_invoices_sql = IF(
    @legacy_invoice_table_exists > 0,
    'INSERT IGNORE INTO lms_billing_service.tbl_invoices (
        id, version, created_by, created_date, last_modified_by, last_modified_date,
        deleted_at, payment_id, invoice_code, user_id, course_id, amount, provider,
        provider_transaction_id, status, issued_at, paid_at
     )
     SELECT id, version, created_by, created_date, last_modified_by, last_modified_date,
        deleted_at, payment_id, invoice_code, user_id, course_id, amount, provider,
        provider_transaction_id, status, issued_at, paid_at
     FROM lms_invoice_service.tbl_invoices',
    'SELECT ''Không có bảng invoice cũ, bỏ qua bước sao chép'' AS migration_message'
);

PREPARE copy_legacy_invoices_stmt FROM @copy_legacy_invoices_sql;
EXECUTE copy_legacy_invoices_stmt;
DEALLOCATE PREPARE copy_legacy_invoices_stmt;

-- Bổ sung các payment PAID chưa từng sinh hóa đơn.
INSERT IGNORE INTO lms_billing_service.tbl_invoices (
    id, version, created_by, created_date, last_modified_by, last_modified_date,
    deleted_at, payment_id, invoice_code, user_id, course_id, amount, provider,
    provider_transaction_id, status, issued_at, paid_at
)
SELECT UUID(), 0, p.created_by, COALESCE(p.created_date, CURRENT_TIMESTAMP),
       p.last_modified_by, p.last_modified_date, p.deleted_at, p.id, p.invoice_code,
       p.user_id, p.course_id, p.amount, p.provider, p.provider_transaction_id,
       p.status, COALESCE(p.invoice_issued_at, p.paid_at, CURRENT_TIMESTAMP), p.paid_at
FROM lms_billing_service.tbl_payments p
WHERE p.status = 'PAID'
  AND p.invoice_code IS NOT NULL
  AND NOT EXISTS (
      SELECT 1
      FROM lms_billing_service.tbl_invoices i
      WHERE i.payment_id = p.id
  );

SELECT COUNT(*) AS invoice_count
FROM lms_billing_service.tbl_invoices
WHERE deleted_at IS NULL;
