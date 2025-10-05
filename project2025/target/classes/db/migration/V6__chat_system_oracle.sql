-- =====================================
-- V6: Sistema de Chat para Oracle
-- =====================================

CREATE TABLE chat_messages (
    id VARCHAR2(36) PRIMARY KEY ,
    session_id VARCHAR2(36) NOT NULL,
    user_id VARCHAR2(36) NOT NULL,
    message_content CLOB NOT NULL,
    response_content CLOB,
    message_type VARCHAR2(50) NOT NULL,
    status VARCHAR2(20) DEFAULT 'PENDING' NOT NULL,
    is_from_user NUMBER(1) DEFAULT 1 NOT NULL,
    processed_at TIMESTAMP,
    error_message CLOB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT fk_chat_messages_user FOREIGN KEY (user_id) 
        REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT chk_message_type CHECK (message_type IN (
        'GENERAL_QUESTION', 'NUTRITION_QUESTION', 'SUBSCRIPTION_QUESTION',
        'TECHNICAL_SUPPORT', 'FEATURE_REQUEST', 'COMPLAINT', 
        'COMPLIMENT', 'OTHER'
    )),
    CONSTRAINT chk_message_status CHECK (status IN (
        'PENDING', 'PROCESSING', 'RESPONDED', 'ERROR', 'IGNORED'
    )),
    CONSTRAINT chk_is_from_user CHECK (is_from_user IN (0, 1))
);

-- Índices para performance
CREATE INDEX idx_chat_session ON chat_messages(session_id);
CREATE INDEX idx_chat_user ON chat_messages(user_id);
CREATE INDEX idx_chat_timestamp ON chat_messages(created_at);
CREATE INDEX idx_chat_status ON chat_messages(status);
CREATE INDEX idx_chat_message_type ON chat_messages(message_type);
CREATE INDEX idx_chat_session_timestamp ON chat_messages(session_id, created_at);

-- Trigger para updated_at
CREATE OR REPLACE TRIGGER trg_chat_messages_upd
BEFORE UPDATE ON chat_messages
FOR EACH ROW
BEGIN
    :NEW.updated_at := CURRENT_TIMESTAMP;
END;
/


