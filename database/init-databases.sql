-- LMS Mini database initialization
-- MySQL 8+
-- Run with: mysql -uroot -proot < database/init-databases.sql

CREATE DATABASE IF NOT EXISTS lms_authn_service CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS lms_author_service CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS lms_course_service CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS lms_learning_service CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS lms_quiz_service CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS lms_notice_service CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS lms_billing_service CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS lms_chat_service CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- =========================================================
-- chat-service
-- =========================================================
USE lms_chat_service;

CREATE TABLE IF NOT EXISTS tbl_chat_conversations (
    id VARCHAR(36) PRIMARY KEY,
    version BIGINT NOT NULL DEFAULT 0,
    created_by VARCHAR(50),
    created_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_modified_by VARCHAR(50),
    last_modified_date DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME NULL,
    access_token_hash CHAR(64) NOT NULL,
    status VARCHAR(20) NOT NULL,
    assistant_processing BOOLEAN NOT NULL DEFAULT FALSE,
    last_message VARCHAR(500),
    last_message_at DATETIME NULL,
    KEY idx_chat_conversations_status (status),
    KEY idx_chat_conversations_last_message_at (last_message_at),
    KEY idx_chat_conversations_deleted_at (deleted_at)
);

CREATE TABLE IF NOT EXISTS tbl_chat_messages (
    id VARCHAR(36) PRIMARY KEY,
    version BIGINT NOT NULL DEFAULT 0,
    created_by VARCHAR(50),
    created_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_modified_by VARCHAR(50),
    last_modified_date DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME NULL,
    conversation_id VARCHAR(36) NOT NULL,
    sender_type VARCHAR(20) NOT NULL,
    content TEXT NOT NULL,
    recommendations_json TEXT,
    error_message BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_chat_messages_conversation
        FOREIGN KEY (conversation_id) REFERENCES tbl_chat_conversations(id),
    KEY idx_chat_messages_conversation_created (conversation_id, created_date),
    KEY idx_chat_messages_deleted_at (deleted_at)
);

+
CREATE TABLE IF NOT EXISTS tbl_support_conversations (
    id VARCHAR(36) PRIMARY KEY,
    version BIGINT NOT NULL DEFAULT 0,
    created_by VARCHAR(50),
    created_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_modified_by VARCHAR(50),
    last_modified_date DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME NULL,
    course_id VARCHAR(36) NOT NULL,
    course_name VARCHAR(255) NOT NULL,
    student_id VARCHAR(36) NOT NULL,
    student_name VARCHAR(255) NOT NULL,
    instructor_id VARCHAR(36) NOT NULL,
    instructor_name VARCHAR(255) NOT NULL,
    status VARCHAR(20) NOT NULL,
    last_message VARCHAR(500),
    last_message_at DATETIME NULL,
    UNIQUE KEY uk_support_conversation_course_student (course_id, student_id),
    KEY idx_support_conversations_student (student_id, last_message_at),
    KEY idx_support_conversations_instructor (instructor_id, last_message_at),
    KEY idx_support_conversations_deleted_at (deleted_at)
);

CREATE TABLE IF NOT EXISTS tbl_support_messages (
    id VARCHAR(36) PRIMARY KEY,
    version BIGINT NOT NULL DEFAULT 0,
    created_by VARCHAR(50),
    created_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_modified_by VARCHAR(50),
    last_modified_date DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME NULL,
    conversation_id VARCHAR(36) NOT NULL,
    sender_id VARCHAR(36) NOT NULL,
    content TEXT NOT NULL,
    read_at DATETIME NULL,
    CONSTRAINT fk_support_messages_conversation
        FOREIGN KEY (conversation_id) REFERENCES tbl_support_conversations(id),
    KEY idx_support_messages_conversation_created (conversation_id, created_date),
    KEY idx_support_messages_unread (conversation_id, sender_id, read_at),
    KEY idx_support_messages_deleted_at (deleted_at)
);


-- =========================================================
-- billing-service
-- =========================================================
USE lms_billing_service;

CREATE TABLE IF NOT EXISTS tbl_payments (
    id VARCHAR(36) PRIMARY KEY,
    version BIGINT NOT NULL DEFAULT 0,
    created_by VARCHAR(50),
    created_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_modified_by VARCHAR(50),
    last_modified_date DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME NULL,

    user_id VARCHAR(36) NOT NULL,
    course_id VARCHAR(36) NOT NULL,
    amount DECIMAL(18, 2) NOT NULL,
    provider VARCHAR(30) NOT NULL,
    provider_order_code BIGINT UNIQUE,
    provider_payment_link_id VARCHAR(150),
    provider_checkout_url VARCHAR(500),
    provider_qr_code TEXT,
    transfer_content VARCHAR(100),
    provider_transaction_id VARCHAR(150),
    invoice_code VARCHAR(50) UNIQUE,
    invoice_issued_at DATETIME NULL,
    status VARCHAR(30) NOT NULL,
    paid_at DATETIME NULL,
    raw_webhook TEXT,

    KEY idx_payments_user_id (user_id),
    KEY idx_payments_course_id (course_id),
    KEY idx_payments_user_course_status (user_id, course_id, status),
    KEY idx_payments_status (status),
    KEY idx_payments_paid_at (paid_at),
    KEY idx_payments_deleted_at (deleted_at)
);

-- =========================================================
-- invoices (thuộc billing-service)
-- =========================================================
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

-- =========================================================
-- authn-service
-- =========================================================
USE lms_authn_service;

CREATE TABLE IF NOT EXISTS tbl_users (
    id VARCHAR(36) PRIMARY KEY,
    version BIGINT NOT NULL DEFAULT 0,
    created_by VARCHAR(50),
    created_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_modified_by VARCHAR(50),
    last_modified_date DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME NULL,

    username VARCHAR(100) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    email VARCHAR(255),
    phone VARCHAR(30),
    full_name VARCHAR(255),
    language VARCHAR(10) DEFAULT 'vi',
    status VARCHAR(30) NOT NULL,
    current_access_token_id VARCHAR(100),
    access_token_expired_at DATETIME NULL,
    failed_login_attempts INT DEFAULT 0,
    account_locked_until DATETIME NULL,
    last_login_at DATETIME NULL,
    password_change_at DATETIME NULL,

    UNIQUE KEY uk_users_username (username),
    UNIQUE KEY uk_users_email (email),
    KEY idx_users_status (status),
    KEY idx_users_deleted_at (deleted_at)
);

CREATE TABLE IF NOT EXISTS tbl_refresh_tokens (
    id VARCHAR(36) PRIMARY KEY,
    version BIGINT NOT NULL DEFAULT 0,
    created_by VARCHAR(50),
    created_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_modified_by VARCHAR(50),
    last_modified_date DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME NULL,

    user_id VARCHAR(36) NOT NULL,
    token_id VARCHAR(100) NOT NULL,
    refresh_token_hash VARCHAR(255) NOT NULL,
    device_id VARCHAR(255),
    user_agent VARCHAR(1000),
    ip_address VARCHAR(100),
    issued_at DATETIME NOT NULL,
    expired_at DATETIME NOT NULL,
    revoked_at DATETIME NULL,
    replaced_by_token_id VARCHAR(100),
    status VARCHAR(30) NOT NULL,

    UNIQUE KEY uk_refresh_tokens_token_id (token_id),
    KEY idx_refresh_tokens_user_id (user_id),
    KEY idx_refresh_tokens_status (status),
    KEY idx_refresh_tokens_expired_at (expired_at),
    KEY idx_refresh_tokens_deleted_at (deleted_at)
);

-- =========================================================
-- author-service
-- =========================================================
USE lms_author_service;

CREATE TABLE IF NOT EXISTS tbl_roles (
    id VARCHAR(36) PRIMARY KEY,
    version BIGINT NOT NULL DEFAULT 0,
    created_by VARCHAR(50),
    created_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_modified_by VARCHAR(50),
    last_modified_date DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME NULL,

    name VARCHAR(255) NOT NULL,
    code VARCHAR(100) NOT NULL,
    description TEXT,
    status VARCHAR(30) NOT NULL,

    UNIQUE KEY uk_roles_code (code),
    KEY idx_roles_status (status),
    KEY idx_roles_deleted_at (deleted_at)
);

CREATE TABLE IF NOT EXISTS tbl_user_roles (
    id VARCHAR(36) PRIMARY KEY,
    version BIGINT NOT NULL DEFAULT 0,
    created_by VARCHAR(50),
    created_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_modified_by VARCHAR(50),
    last_modified_date DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME NULL,

    user_id VARCHAR(36) NOT NULL,
    role_id VARCHAR(36) NOT NULL,

    UNIQUE KEY uk_user_roles_user_role (user_id, role_id),
    KEY idx_user_roles_user_id (user_id),
    KEY idx_user_roles_role_id (role_id),
    KEY idx_user_roles_deleted_at (deleted_at)
);

CREATE TABLE IF NOT EXISTS tbl_permissions (
    id VARCHAR(36) PRIMARY KEY,
    version BIGINT NOT NULL DEFAULT 0,
    created_by VARCHAR(50),
    created_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_modified_by VARCHAR(50),
    last_modified_date DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME NULL,

    name VARCHAR(255) NOT NULL,
    code VARCHAR(100) NOT NULL,
    description TEXT,
    status VARCHAR(30) NOT NULL,

    UNIQUE KEY uk_permissions_code (code),
    KEY idx_permissions_status (status),
    KEY idx_permissions_deleted_at (deleted_at)
);

CREATE TABLE IF NOT EXISTS tbl_role_permissions (
    id VARCHAR(36) PRIMARY KEY,
    version BIGINT NOT NULL DEFAULT 0,
    created_by VARCHAR(50),
    created_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_modified_by VARCHAR(50),
    last_modified_date DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME NULL,

    role_id VARCHAR(36) NOT NULL,
    permission_id VARCHAR(36) NOT NULL,

    UNIQUE KEY uk_role_permissions_role_permission (role_id, permission_id),
    KEY idx_role_permissions_role_id (role_id),
    KEY idx_role_permissions_permission_id (permission_id),
    KEY idx_role_permissions_deleted_at (deleted_at)
);

INSERT INTO tbl_roles (id, code, name, description, status, created_by, created_date, last_modified_by, last_modified_date, deleted_at)
SELECT UUID(), 'ADMIN', 'Admin', 'System administrator', 'ACTIVE', 'system', NOW(), 'system', NOW(), NULL
WHERE NOT EXISTS (SELECT 1 FROM tbl_roles WHERE code = 'ADMIN');

INSERT INTO tbl_roles (id, code, name, description, status, created_by, created_date, last_modified_by, last_modified_date, deleted_at)
SELECT UUID(), 'INSTRUCTOR', 'Instructor', 'Course instructor', 'ACTIVE', 'system', NOW(), 'system', NOW(), NULL
WHERE NOT EXISTS (SELECT 1 FROM tbl_roles WHERE code = 'INSTRUCTOR');

INSERT INTO tbl_roles (id, code, name, description, status, created_by, created_date, last_modified_by, last_modified_date, deleted_at)
SELECT UUID(), 'STUDENT', 'Student', 'Course student', 'ACTIVE', 'system', NOW(), 'system', NOW(), NULL
WHERE NOT EXISTS (SELECT 1 FROM tbl_roles WHERE code = 'STUDENT');

INSERT INTO tbl_permissions (id, name, code, description, status, created_by, created_date, last_modified_by, last_modified_date, deleted_at)
SELECT UUID(), 'Permission Manage', 'PERMISSION_MANAGE', 'Manage permissions and role mappings', 'ACTIVE', 'system', NOW(), 'system', NOW(), NULL
WHERE NOT EXISTS (SELECT 1 FROM tbl_permissions WHERE code = 'PERMISSION_MANAGE');

INSERT INTO tbl_permissions (id, name, code, description, status, created_by, created_date, last_modified_by, last_modified_date, deleted_at)
SELECT UUID(), 'Role View', 'ROLE_VIEW', 'View roles', 'ACTIVE', 'system', NOW(), 'system', NOW(), NULL
WHERE NOT EXISTS (SELECT 1 FROM tbl_permissions WHERE code = 'ROLE_VIEW');

INSERT INTO tbl_permissions (id, name, code, description, status, created_by, created_date, last_modified_by, last_modified_date, deleted_at)
SELECT UUID(), 'User View', 'USER_VIEW', 'View users', 'ACTIVE', 'system', NOW(), 'system', NOW(), NULL
WHERE NOT EXISTS (SELECT 1 FROM tbl_permissions WHERE code = 'USER_VIEW');

INSERT INTO tbl_permissions (id, name, code, description, status, created_by, created_date, last_modified_by, last_modified_date, deleted_at)
SELECT UUID(), 'User Update', 'USER_UPDATE', 'Update users', 'ACTIVE', 'system', NOW(), 'system', NOW(), NULL
WHERE NOT EXISTS (SELECT 1 FROM tbl_permissions WHERE code = 'USER_UPDATE');

INSERT INTO tbl_permissions (id, name, code, description, status, created_by, created_date, last_modified_by, last_modified_date, deleted_at)
SELECT UUID(), 'Course View', 'COURSE_VIEW', 'View courses', 'ACTIVE', 'system', NOW(), 'system', NOW(), NULL
WHERE NOT EXISTS (SELECT 1 FROM tbl_permissions WHERE code = 'COURSE_VIEW');

INSERT INTO tbl_permissions (id, name, code, description, status, created_by, created_date, last_modified_by, last_modified_date, deleted_at)
SELECT UUID(), 'Course Manage', 'COURSE_MANAGE', 'Manage courses', 'ACTIVE', 'system', NOW(), 'system', NOW(), NULL
WHERE NOT EXISTS (SELECT 1 FROM tbl_permissions WHERE code = 'COURSE_MANAGE');

INSERT INTO tbl_permissions (id, name, code, description, status, created_by, created_date, last_modified_by, last_modified_date, deleted_at)
SELECT UUID(), 'Lesson Manage', 'LESSON_MANAGE', 'Manage lessons', 'ACTIVE', 'system', NOW(), 'system', NOW(), NULL
WHERE NOT EXISTS (SELECT 1 FROM tbl_permissions WHERE code = 'LESSON_MANAGE');

INSERT INTO tbl_permissions (id, name, code, description, status, created_by, created_date, last_modified_by, last_modified_date, deleted_at)
SELECT UUID(), 'Quiz Manage', 'QUIZ_MANAGE', 'Manage quizzes', 'ACTIVE', 'system', NOW(), 'system', NOW(), NULL
WHERE NOT EXISTS (SELECT 1 FROM tbl_permissions WHERE code = 'QUIZ_MANAGE');

INSERT INTO tbl_permissions (id, name, code, description, status, created_by, created_date, last_modified_by, last_modified_date, deleted_at)
SELECT UUID(), 'Quiz Attempt', 'QUIZ_ATTEMPT', 'Start quiz attempt', 'ACTIVE', 'system', NOW(), 'system', NOW(), NULL
WHERE NOT EXISTS (SELECT 1 FROM tbl_permissions WHERE code = 'QUIZ_ATTEMPT');

INSERT INTO tbl_permissions (id, name, code, description, status, created_by, created_date, last_modified_by, last_modified_date, deleted_at)
SELECT UUID(), 'Notice View', 'NOTICE_VIEW', 'View notices', 'ACTIVE', 'system', NOW(), 'system', NOW(), NULL
WHERE NOT EXISTS (SELECT 1 FROM tbl_permissions WHERE code = 'NOTICE_VIEW');

INSERT INTO tbl_permissions (id, name, code, description, status, created_by, created_date, last_modified_by, last_modified_date, deleted_at)
SELECT UUID(), 'Notice Send', 'NOTICE_SEND', 'Send notices', 'ACTIVE', 'system', NOW(), 'system', NOW(), NULL
WHERE NOT EXISTS (SELECT 1 FROM tbl_permissions WHERE code = 'NOTICE_SEND');

INSERT INTO tbl_role_permissions (id, role_id, permission_id, created_by, created_date, last_modified_by, last_modified_date, deleted_at)
SELECT UUID(), r.id, p.id, 'system', NOW(), 'system', NOW(), NULL
FROM tbl_roles r
JOIN tbl_permissions p ON p.code IN ('PERMISSION_MANAGE','ROLE_VIEW','USER_VIEW','USER_UPDATE','COURSE_VIEW','COURSE_MANAGE','LESSON_MANAGE','QUIZ_MANAGE','QUIZ_ATTEMPT','NOTICE_VIEW','NOTICE_SEND')
WHERE r.code = 'ADMIN'
    AND NOT EXISTS (
            SELECT 1 FROM tbl_role_permissions rp
            WHERE rp.role_id = r.id AND rp.permission_id = p.id
    );

INSERT INTO tbl_role_permissions (id, role_id, permission_id, created_by, created_date, last_modified_by, last_modified_date, deleted_at)
SELECT UUID(), r.id, p.id, 'system', NOW(), 'system', NOW(), NULL
FROM tbl_roles r
JOIN tbl_permissions p ON p.code IN ('COURSE_VIEW','COURSE_MANAGE','LESSON_MANAGE','QUIZ_MANAGE','QUIZ_ATTEMPT','NOTICE_VIEW')
WHERE r.code = 'INSTRUCTOR'
    AND NOT EXISTS (
            SELECT 1 FROM tbl_role_permissions rp
            WHERE rp.role_id = r.id AND rp.permission_id = p.id
    );

INSERT INTO tbl_role_permissions (id, role_id, permission_id, created_by, created_date, last_modified_by, last_modified_date, deleted_at)
SELECT UUID(), r.id, p.id, 'system', NOW(), 'system', NOW(), NULL
FROM tbl_roles r
JOIN tbl_permissions p ON p.code IN ('COURSE_VIEW','QUIZ_ATTEMPT','NOTICE_VIEW')
WHERE r.code = 'STUDENT'
    AND NOT EXISTS (
            SELECT 1 FROM tbl_role_permissions rp
            WHERE rp.role_id = r.id AND rp.permission_id = p.id
    );

-- =========================================================
-- course-service
-- =========================================================
USE lms_course_service;

CREATE TABLE IF NOT EXISTS tbl_course_categories (
    id VARCHAR(36) PRIMARY KEY,
    version BIGINT NOT NULL DEFAULT 0,
    created_by VARCHAR(50),
    created_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_modified_by VARCHAR(50),
    last_modified_date DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME NULL,

    name VARCHAR(255) NOT NULL,
    code VARCHAR(100) NOT NULL,
    description TEXT,
    status VARCHAR(30) NOT NULL,

    UNIQUE KEY uk_course_categories_code (code),
    KEY idx_course_categories_status (status),
    KEY idx_course_categories_deleted_at (deleted_at)
);

CREATE TABLE IF NOT EXISTS tbl_courses (
    id VARCHAR(36) PRIMARY KEY,
    version BIGINT NOT NULL DEFAULT 0,
    created_by VARCHAR(50),
    created_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_modified_by VARCHAR(50),
    last_modified_date DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME NULL,

    category_id VARCHAR(36) NOT NULL,
    instructor_id VARCHAR(36) NOT NULL,
    name VARCHAR(255) NOT NULL,
    code VARCHAR(100) NOT NULL,
    description TEXT,
    level VARCHAR(30),
    duration_minutes INT DEFAULT 0,
    price DECIMAL(18,2) DEFAULT 0,
    status VARCHAR(30) NOT NULL,
    reject_reason TEXT,
    published_at DATETIME NULL,

    UNIQUE KEY uk_courses_code (code),
    KEY idx_courses_category_id (category_id),
    KEY idx_courses_instructor_id (instructor_id),
    KEY idx_courses_status (status),
    KEY idx_courses_deleted_at (deleted_at)
);

CREATE TABLE IF NOT EXISTS tbl_lessons (
    id VARCHAR(36) PRIMARY KEY,
    version BIGINT NOT NULL DEFAULT 0,
    created_by VARCHAR(50),
    created_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_modified_by VARCHAR(50),
    last_modified_date DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME NULL,

    course_id VARCHAR(36) NOT NULL,
    title VARCHAR(255) NOT NULL,
    code VARCHAR(100),
    content TEXT,
    video_url VARCHAR(1000),
    order_index INT NOT NULL,
    duration_minutes INT DEFAULT 0,
    status VARCHAR(30) NOT NULL,

    KEY idx_lessons_course_id (course_id),
    KEY idx_lessons_status (status),
    KEY idx_lessons_order_index (order_index),
    KEY idx_lessons_deleted_at (deleted_at)
);

CREATE TABLE IF NOT EXISTS tbl_lesson_resources (
    id VARCHAR(36) PRIMARY KEY,
    version BIGINT NOT NULL DEFAULT 0,
    created_by VARCHAR(50),
    created_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_modified_by VARCHAR(50),
    last_modified_date DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME NULL,

    lesson_id VARCHAR(36) NOT NULL,
    title VARCHAR(255) NOT NULL,
    resource_type VARCHAR(50) NOT NULL,
    file_path VARCHAR(1000),
    external_url VARCHAR(1000),
    status VARCHAR(30) NOT NULL,

    KEY idx_lesson_resources_lesson_id (lesson_id),
    KEY idx_lesson_resources_status (status),
    KEY idx_lesson_resources_deleted_at (deleted_at)
);

CREATE TABLE IF NOT EXISTS tbl_images (
    id VARCHAR(36) PRIMARY KEY,
    version BIGINT NOT NULL DEFAULT 0,
    created_by VARCHAR(50),
    created_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_modified_by VARCHAR(50),
    last_modified_date DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME NULL,

    object_type VARCHAR(50) NOT NULL,
    object_id VARCHAR(36) NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    file_path VARCHAR(1000) NOT NULL,
    file_url VARCHAR(1000),
    content_type VARCHAR(100),
    file_size BIGINT,
    is_primary BOOLEAN DEFAULT FALSE,
    status VARCHAR(30) NOT NULL,

    KEY idx_images_object (object_type, object_id),
    KEY idx_images_status (status),
    KEY idx_images_deleted_at (deleted_at)
);

-- =========================================================
-- learning-service
-- =========================================================
USE lms_learning_service;

CREATE TABLE IF NOT EXISTS tbl_enrollments (
    id VARCHAR(36) PRIMARY KEY,
    version BIGINT NOT NULL DEFAULT 0,
    created_by VARCHAR(50),
    created_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_modified_by VARCHAR(50),
    last_modified_date DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME NULL,

    user_id VARCHAR(36) NOT NULL,
    course_id VARCHAR(36) NOT NULL,
    enrolled_at DATETIME NOT NULL,
    completed_at DATETIME NULL,
    progress_percent DOUBLE DEFAULT 0,
    status VARCHAR(30) NOT NULL,

    UNIQUE KEY uk_enrollments_user_course (user_id, course_id),
    KEY idx_enrollments_user_id (user_id),
    KEY idx_enrollments_course_id (course_id),
    KEY idx_enrollments_status (status),
    KEY idx_enrollments_deleted_at (deleted_at)
);

CREATE TABLE IF NOT EXISTS tbl_learning_progress (
    id VARCHAR(36) PRIMARY KEY,
    version BIGINT NOT NULL DEFAULT 0,
    created_by VARCHAR(50),
    created_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_modified_by VARCHAR(50),
    last_modified_date DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME NULL,

    enrollment_id VARCHAR(36) NOT NULL,
    lesson_id VARCHAR(36) NOT NULL,
    started_at DATETIME NULL,
    completed_at DATETIME NULL,
    status VARCHAR(30) NOT NULL,

    UNIQUE KEY uk_learning_progress_enrollment_lesson (enrollment_id, lesson_id),
    KEY idx_learning_progress_enrollment_id (enrollment_id),
    KEY idx_learning_progress_lesson_id (lesson_id),
    KEY idx_learning_progress_status (status),
    KEY idx_learning_progress_deleted_at (deleted_at)
);

CREATE TABLE IF NOT EXISTS tbl_certificates (
    id VARCHAR(36) PRIMARY KEY,
    version BIGINT NOT NULL DEFAULT 0,
    created_by VARCHAR(50),
    created_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_modified_by VARCHAR(50),
    last_modified_date DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME NULL,

    user_id VARCHAR(36) NOT NULL,
    course_id VARCHAR(36) NOT NULL,
    enrollment_id VARCHAR(36) NOT NULL,
    certificate_code VARCHAR(100) NOT NULL,
    issued_at DATETIME NOT NULL,
    status VARCHAR(30) NOT NULL,

    UNIQUE KEY uk_certificates_code (certificate_code),
    UNIQUE KEY uk_certificates_user_course (user_id, course_id),
    KEY idx_certificates_enrollment_id (enrollment_id),
    KEY idx_certificates_status (status),
    KEY idx_certificates_deleted_at (deleted_at)
);

-- =========================================================
-- quiz-service
-- =========================================================
USE lms_quiz_service;

CREATE TABLE IF NOT EXISTS tbl_quizzes (
    id VARCHAR(36) PRIMARY KEY,
    version BIGINT NOT NULL DEFAULT 0,
    created_by VARCHAR(50),
    created_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_modified_by VARCHAR(50),
    last_modified_date DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME NULL,

    course_id VARCHAR(36),
    lesson_id VARCHAR(36),
    title VARCHAR(255) NOT NULL,
    pass_score DECIMAL(5,2) NOT NULL,
    max_attempts INT DEFAULT 3,
    required_to_complete BOOLEAN DEFAULT TRUE,
    status VARCHAR(30) NOT NULL,

    KEY idx_quizzes_course_id (course_id),
    KEY idx_quizzes_lesson_id (lesson_id),
    KEY idx_quizzes_status (status),
    KEY idx_quizzes_deleted_at (deleted_at)
);

CREATE TABLE IF NOT EXISTS tbl_questions (
    id VARCHAR(36) PRIMARY KEY,
    version BIGINT NOT NULL DEFAULT 0,
    created_by VARCHAR(50),
    created_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_modified_by VARCHAR(50),
    last_modified_date DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME NULL,

    quiz_id VARCHAR(36) NOT NULL,
    content TEXT NOT NULL,
    question_type VARCHAR(50) NOT NULL,
    score DECIMAL(5,2) NOT NULL,
    order_index INT NOT NULL,

    KEY idx_questions_quiz_id (quiz_id),
    KEY idx_questions_order_index (order_index),
    KEY idx_questions_deleted_at (deleted_at)
);

CREATE TABLE IF NOT EXISTS tbl_answers (
    id VARCHAR(36) PRIMARY KEY,
    version BIGINT NOT NULL DEFAULT 0,
    created_by VARCHAR(50),
    created_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_modified_by VARCHAR(50),
    last_modified_date DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME NULL,

    question_id VARCHAR(36) NOT NULL,
    content TEXT NOT NULL,
    is_correct BOOLEAN DEFAULT FALSE,
    order_index INT NOT NULL,

    KEY idx_answers_question_id (question_id),
    KEY idx_answers_order_index (order_index),
    KEY idx_answers_deleted_at (deleted_at)
);

CREATE TABLE IF NOT EXISTS tbl_quiz_attempts (
    id VARCHAR(36) PRIMARY KEY,
    version BIGINT NOT NULL DEFAULT 0,
    created_by VARCHAR(50),
    created_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_modified_by VARCHAR(50),
    last_modified_date DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME NULL,

    quiz_id VARCHAR(36) NOT NULL,
    user_id VARCHAR(36) NOT NULL,
    enrollment_id VARCHAR(36) NOT NULL,
    score DECIMAL(5,2) DEFAULT 0,
    passed BOOLEAN DEFAULT FALSE,
    started_at DATETIME NOT NULL,
    submitted_at DATETIME NULL,
    status VARCHAR(30) NOT NULL,

    KEY idx_quiz_attempts_quiz_id (quiz_id),
    KEY idx_quiz_attempts_user_id (user_id),
    KEY idx_quiz_attempts_enrollment_id (enrollment_id),
    KEY idx_quiz_attempts_status (status),
    KEY idx_quiz_attempts_deleted_at (deleted_at)
);

CREATE TABLE IF NOT EXISTS tbl_quiz_attempt_answers (
    id VARCHAR(36) PRIMARY KEY,
    version BIGINT NOT NULL DEFAULT 0,
    created_by VARCHAR(50),
    created_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_modified_by VARCHAR(50),
    last_modified_date DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME NULL,

    attempt_id VARCHAR(36) NOT NULL,
    question_id VARCHAR(36) NOT NULL,
    answer_id VARCHAR(36),
    is_correct BOOLEAN DEFAULT FALSE,
    score DECIMAL(5,2) DEFAULT 0,

    KEY idx_quiz_attempt_answers_attempt_id (attempt_id),
    KEY idx_quiz_attempt_answers_question_id (question_id),
    KEY idx_quiz_attempt_answers_answer_id (answer_id),
    KEY idx_quiz_attempt_answers_deleted_at (deleted_at)
);

-- =========================================================
-- notice-service
-- =========================================================
USE lms_notice_service;

CREATE TABLE IF NOT EXISTS tbl_user_devices (
    id VARCHAR(36) PRIMARY KEY,
    version BIGINT NOT NULL DEFAULT 0,
    created_by VARCHAR(50),
    created_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_modified_by VARCHAR(50),
    last_modified_date DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME NULL,

    user_id VARCHAR(36) NOT NULL,
    device_id VARCHAR(255),
    device_type VARCHAR(30),
    fcm_token TEXT NOT NULL,
    app_version VARCHAR(50),
    status VARCHAR(30) NOT NULL,
    last_active_at DATETIME NULL,

    KEY idx_user_devices_user_id (user_id),
    KEY idx_user_devices_device_id (device_id),
    KEY idx_user_devices_status (status),
    KEY idx_user_devices_deleted_at (deleted_at)
);

CREATE TABLE IF NOT EXISTS tbl_notices (
    id VARCHAR(36) PRIMARY KEY,
    version BIGINT NOT NULL DEFAULT 0,
    created_by VARCHAR(50),
    created_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_modified_by VARCHAR(50),
    last_modified_date DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME NULL,

    title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    notice_type VARCHAR(50) NOT NULL,
    target_type VARCHAR(50) NOT NULL,
    data TEXT,
    status VARCHAR(30) NOT NULL,
    sent_at DATETIME NULL,

    KEY idx_notices_notice_type (notice_type),
    KEY idx_notices_target_type (target_type),
    KEY idx_notices_status (status),
    KEY idx_notices_deleted_at (deleted_at)
);

CREATE TABLE IF NOT EXISTS tbl_notice_recipients (
    id VARCHAR(36) PRIMARY KEY,
    version BIGINT NOT NULL DEFAULT 0,
    created_by VARCHAR(50),
    created_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_modified_by VARCHAR(50),
    last_modified_date DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME NULL,

    notice_id VARCHAR(36) NOT NULL,
    user_id VARCHAR(36) NOT NULL,
    delivery_status VARCHAR(30) NOT NULL,
    read_status VARCHAR(30) NOT NULL,
    sent_at DATETIME NULL,
    read_at DATETIME NULL,
    failure_reason TEXT,

    KEY idx_notice_recipients_notice_id (notice_id),
    KEY idx_notice_recipients_user_id (user_id),
    KEY idx_notice_recipients_delivery_status (delivery_status),
    KEY idx_notice_recipients_read_status (read_status),
    KEY idx_notice_recipients_deleted_at (deleted_at)
);

CREATE TABLE IF NOT EXISTS tbl_notice_delivery_logs (
    id VARCHAR(36) PRIMARY KEY,
    version BIGINT NOT NULL DEFAULT 0,
    created_by VARCHAR(50),
    created_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_modified_by VARCHAR(50),
    last_modified_date DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME NULL,

    notice_id VARCHAR(36) NOT NULL,
    recipient_id VARCHAR(36),
    user_id VARCHAR(36),
    device_id VARCHAR(255),
    fcm_token TEXT,
    provider VARCHAR(50) NOT NULL,
    provider_message_id VARCHAR(255),
    status VARCHAR(30) NOT NULL,
    error_code VARCHAR(100),
    error_message TEXT,

    KEY idx_notice_delivery_logs_notice_id (notice_id),
    KEY idx_notice_delivery_logs_recipient_id (recipient_id),
    KEY idx_notice_delivery_logs_user_id (user_id),
    KEY idx_notice_delivery_logs_status (status),
    KEY idx_notice_delivery_logs_deleted_at (deleted_at)
);
