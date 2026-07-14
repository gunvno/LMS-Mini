USE lms_chat_service;


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

