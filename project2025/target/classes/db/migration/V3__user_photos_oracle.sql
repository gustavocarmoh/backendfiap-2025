-- =====================================
-- V3: Sistema de Fotos de Usuário para Oracle
-- =====================================

CREATE TABLE user_photos (
    id VARCHAR2(36) PRIMARY KEY ,
    user_id VARCHAR2(36) NOT NULL UNIQUE,
    photo_data BLOB NOT NULL,
    file_name VARCHAR2(100),
    content_type VARCHAR2(50),
    file_size NUMBER(19),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT fk_user_photos_user_id FOREIGN KEY (user_id) 
        REFERENCES users(id) ON DELETE CASCADE
);

-- Trigger para gerar UUID automaticamente para user_photos
CREATE OR REPLACE TRIGGER trg_user_photos_id
BEFORE INSERT ON user_photos
FOR EACH ROW
WHEN (NEW.id IS NULL)
BEGIN
    :NEW.id := LOWER(REGEXP_REPLACE(RAWTOHEX(SYS_GUID()), '([A-F0-9]{8})([A-F0-9]{4})([A-F0-9]{4})([A-F0-9]{4})([A-F0-9]{12})', '\1-\2-\3-\4-\5'));
END;
/

-- Índice user_id não é necessário pois a constraint UNIQUE já cria um índice automaticamente
CREATE INDEX idx_user_photos_created_at ON user_photos(created_at);

CREATE OR REPLACE TRIGGER trg_user_photos_upd
BEFORE UPDATE ON user_photos
FOR EACH ROW
BEGIN
    :NEW.updated_at := CURRENT_TIMESTAMP;
END;
/
