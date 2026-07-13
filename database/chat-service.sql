CREATE DATABASE IF NOT EXISTS lms_chat_service CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
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
