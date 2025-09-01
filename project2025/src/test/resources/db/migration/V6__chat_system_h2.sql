-- V6__chat_system_h2.sql
-- Migração para criar o sistema de chat no H2 (testes)

-- Criação da tabela de mensagens de chat
CREATE TABLE chat_messages (
    id UUID PRIMARY KEY DEFAULT RANDOM_UUID(),
    session_id UUID NOT NULL,
    user_id UUID NOT NULL,
    message_content CLOB NOT NULL,
    response_content CLOB,
    message_type VARCHAR(50) NOT NULL CHECK (message_type IN (
        'GENERAL_QUESTION',
        'NUTRITION_QUESTION', 
        'SUBSCRIPTION_QUESTION',
        'TECHNICAL_SUPPORT',
        'FEATURE_REQUEST',
        'COMPLAINT',
        'COMPLIMENT',
        'OTHER'
    )),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' CHECK (status IN (
        'PENDING',
        'PROCESSING', 
        'RESPONDED',
        'ERROR',
        'IGNORED'
    )),
    is_from_user BOOLEAN NOT NULL DEFAULT true,
    processed_at TIMESTAMP,
    error_message CLOB,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

-- Chaves estrangeiras
CONSTRAINT fk_chat_messages_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Índices para performance
CREATE INDEX idx_chat_session ON chat_messages (session_id);

CREATE INDEX idx_chat_user ON chat_messages (user_id);

CREATE INDEX idx_chat_timestamp ON chat_messages (created_at);

CREATE INDEX idx_chat_status ON chat_messages (status);

CREATE INDEX idx_chat_message_type ON chat_messages (message_type);

CREATE INDEX idx_chat_session_timestamp ON chat_messages (session_id, created_at);