-- V6__chat_system.sql
-- Migração para criar o sistema de chat

-- Criação da tabela de mensagens de chat
CREATE TABLE chat_messages (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    session_id UUID NOT NULL,
    user_id UUID NOT NULL,
    message_content TEXT NOT NULL,
    response_content TEXT,
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
    error_message TEXT,
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

-- Função para atualizar o updated_at automaticamente
CREATE OR REPLACE FUNCTION update_chat_message_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Trigger para atualizar updated_at
CREATE TRIGGER update_chat_message_updated_at_trigger
    BEFORE UPDATE ON chat_messages
    FOR EACH ROW
    EXECUTE FUNCTION update_chat_message_updated_at();

-- Comentários para documentação
COMMENT ON TABLE chat_messages IS 'Tabela para armazenar mensagens do sistema de chat';

COMMENT ON COLUMN chat_messages.session_id IS 'ID da sessão de chat (UUID gerado para agrupar mensagens)';

COMMENT ON COLUMN chat_messages.user_id IS 'ID do usuário que enviou a mensagem';

COMMENT ON COLUMN chat_messages.message_content IS 'Conteúdo da mensagem enviada pelo usuário';

COMMENT ON COLUMN chat_messages.response_content IS 'Resposta gerada pelo sistema (IA ou automática)';

COMMENT ON COLUMN chat_messages.message_type IS 'Tipo/categoria da mensagem para melhor processamento';

COMMENT ON COLUMN chat_messages.status IS 'Status do processamento da mensagem';

COMMENT ON COLUMN chat_messages.is_from_user IS 'Indica se a mensagem é do usuário (true) ou do sistema (false)';

COMMENT ON COLUMN chat_messages.processed_at IS 'Timestamp de quando a mensagem foi processada';

COMMENT ON COLUMN chat_messages.error_message IS 'Mensagem de erro caso o processamento falhe';