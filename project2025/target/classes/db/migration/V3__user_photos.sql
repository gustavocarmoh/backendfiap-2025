-- Migration para adicionar tabela de fotos de usuário
-- Mantém compatibilidade com campo profile_photo_url existente

-- Criar tabela para armazenar fotos de usuário
CREATE TABLE user_photos (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid (),
    user_id UUID NOT NULL UNIQUE,
    photo_data BYTEA NOT NULL,
    file_name VARCHAR(100),
    content_type VARCHAR(50),
    file_size BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_user_photos_user_id FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

-- Criar índices para melhor performance
CREATE INDEX idx_user_photos_user_id ON user_photos (user_id);

CREATE INDEX idx_user_photos_created_at ON user_photos (created_at);

-- Comentários para documentação
COMMENT ON
TABLE user_photos IS 'Armazena fotos de perfil dos usuários no banco de dados';

COMMENT ON COLUMN user_photos.id IS 'Identificador único da foto';

COMMENT ON COLUMN user_photos.user_id IS 'Referência ao usuário proprietário da foto';

COMMENT ON COLUMN user_photos.photo_data IS 'Dados binários da imagem (BLOB)';

COMMENT ON COLUMN user_photos.file_name IS 'Nome original do arquivo enviado';

COMMENT ON COLUMN user_photos.content_type IS 'Tipo MIME da imagem (image/jpeg, image/png, etc.)';

COMMENT ON COLUMN user_photos.file_size IS 'Tamanho do arquivo em bytes';

COMMENT ON COLUMN user_photos.created_at IS 'Data de criação da foto';

COMMENT ON COLUMN user_photos.updated_at IS 'Data da última atualização';

-- Trigger para atualizar updated_at automaticamente
CREATE OR REPLACE FUNCTION update_user_photos_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER trigger_user_photos_updated_at
    BEFORE UPDATE ON user_photos
    FOR EACH ROW
    EXECUTE FUNCTION update_user_photos_updated_at();