-- =====================================
-- V1: Sistema de usuários + roles para Oracle Database
-- Compatível com Spring Security
-- =====================================

-- Criar sequências para IDs
CREATE SEQUENCE roles_seq START WITH 1 INCREMENT BY 1;

-- Tabela de roles
CREATE TABLE roles (
    id NUMBER(19) PRIMARY KEY,
    name VARCHAR2(50) NOT NULL UNIQUE,
    CONSTRAINT chk_roles_name_length CHECK (LENGTH(name) BETWEEN 3 AND 50)
);

-- Tabela de usuários (com dados pessoais)
-- Oracle usa VARCHAR2(36) para UUIDs ou RAW(16)
CREATE TABLE users (
    id VARCHAR2(36) PRIMARY KEY,
    first_name VARCHAR2(150) NOT NULL,
    last_name VARCHAR2(150) NOT NULL,
    email VARCHAR2(255) NOT NULL UNIQUE,
    password_hash VARCHAR2(255) NOT NULL,
    phone_e164 VARCHAR2(20),
    address_street VARCHAR2(255),
    address_number VARCHAR2(20),
    address_complement VARCHAR2(100),
    address_district VARCHAR2(100),
    address_city VARCHAR2(100),
    address_state VARCHAR2(2),
    address_postal_code VARCHAR2(10),
    address_country VARCHAR2(2) DEFAULT 'BR',
    profile_photo_url VARCHAR2(500),
    is_active NUMBER(1) DEFAULT 1 NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT chk_first_name_length CHECK (LENGTH(first_name) BETWEEN 1 AND 150),
    CONSTRAINT chk_last_name_length CHECK (LENGTH(last_name) BETWEEN 1 AND 150),
    CONSTRAINT chk_email_format CHECK (INSTR(email, '@') > 1),
    CONSTRAINT chk_phone_format CHECK (
        phone_e164 IS NULL OR 
        REGEXP_LIKE(phone_e164, '^\+?[0-9]{8,15}$')
    ),
    CONSTRAINT chk_is_active CHECK (is_active IN (0, 1))
);

-- Índices para performance
CREATE INDEX idx_users_email_ci ON users (LOWER(email));

-- Trigger para gerar UUID automaticamente
CREATE OR REPLACE TRIGGER trg_users_id
BEFORE INSERT ON users
FOR EACH ROW
WHEN (NEW.id IS NULL)
BEGIN
    :NEW.id := LOWER(REGEXP_REPLACE(RAWTOHEX(SYS_GUID()), '([A-F0-9]{8})([A-F0-9]{4})([A-F0-9]{4})([A-F0-9]{4})([A-F0-9]{12})', '\1-\2-\3-\4-\5'));
END;
/

-- Trigger para atualizar updated_at automaticamente
CREATE OR REPLACE TRIGGER trg_users_updated_at
BEFORE UPDATE ON users
FOR EACH ROW
BEGIN
    :NEW.updated_at := CURRENT_TIMESTAMP;
END;
/

-- Relação n:n usuário↔role
CREATE TABLE user_roles (
    user_id VARCHAR2(36) NOT NULL,
    role_id NUMBER(19) NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_roles_role FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
);

-- Seeds de roles
INSERT INTO roles (id, name) VALUES (roles_seq.NEXTVAL, 'ADMIN');
INSERT INTO roles (id, name) VALUES (roles_seq.NEXTVAL, 'MANAGER');
INSERT INTO roles (id, name) VALUES (roles_seq.NEXTVAL, 'USER');

-- Seeds de usuários (senha em dev com {noop})
-- Usuario 1: Admin
DECLARE
    v_user_id VARCHAR2(36);
BEGIN
    v_user_id := SYS_GUID();
    INSERT INTO users (
        id, first_name, last_name, email, password_hash,
        phone_e164, address_street, address_number, address_complement,
        address_district, address_city, address_state, address_postal_code,
        address_country, profile_photo_url, is_active
    ) VALUES (
        v_user_id, 'System', 'Admin', 'admin@nutrixpert.local', '{noop}admin123',
        '+5531999990001', 'Av. Afonso Pena', '1000', 'Sala 101',
        'Centro', 'Belo Horizonte', 'MG', '30130-000',
        'BR', 'https://example.com/avatars/admin.png', 1
    );
END;
/

-- Usuario 2: Manager
DECLARE
    v_user_id VARCHAR2(36);
BEGIN
    v_user_id := SYS_GUID();
    INSERT INTO users (
        id, first_name, last_name, email, password_hash,
        phone_e164, address_street, address_number, address_complement,
        address_district, address_city, address_state, address_postal_code,
        address_country, profile_photo_url, is_active
    ) VALUES (
        v_user_id, 'Manager', 'User', 'manager@nutrixpert.local', '{noop}manager123',
        '+5531999990002', 'Rua dos Desenvolvedores', '42', NULL,
        'Funcionários', 'Belo Horizonte', 'MG', '30140-110',
        'BR', 'https://example.com/avatars/manager.png', 1
    );
END;
/

-- Usuario 3: Alice
DECLARE
    v_user_id VARCHAR2(36);
BEGIN
    v_user_id := SYS_GUID();
    INSERT INTO users (
        id, first_name, last_name, email, password_hash,
        phone_e164, address_street, address_number, address_complement,
        address_district, address_city, address_state, address_postal_code,
        address_country, profile_photo_url, is_active
    ) VALUES (
        v_user_id, 'Alice', 'Example', 'alice@example.com', '{noop}alice123',
        '+5531999990003', 'Rua das Flores', '200', 'Ap 12',
        'Savassi', 'Belo Horizonte', 'MG', '30110-000',
        'BR', 'https://example.com/avatars/alice.png', 1
    );
END;
/

-- Usuario 4: Bob
DECLARE
    v_user_id VARCHAR2(36);
BEGIN
    v_user_id := SYS_GUID();
    INSERT INTO users (
        id, first_name, last_name, email, password_hash,
        phone_e164, address_street, address_number, address_complement,
        address_district, address_city, address_state, address_postal_code,
        address_country, profile_photo_url, is_active
    ) VALUES (
        v_user_id, 'Bob', 'Example', 'bob@example.com', '{noop}bob123',
        '+5531999990004', 'Av. Brasil', '500', NULL,
        'Funcionários', 'Belo Horizonte', 'MG', '30140-000',
        'BR', 'https://example.com/avatars/bob.png', 1
    );
END;
/

-- Usuario 5: Carlos
DECLARE
    v_user_id VARCHAR2(36);
BEGIN
    v_user_id := SYS_GUID();
    INSERT INTO users (
        id, first_name, last_name, email, password_hash,
        phone_e164, address_street, address_number, address_complement,
        address_district, address_city, address_state, address_postal_code,
        address_country, profile_photo_url, is_active
    ) VALUES (
        v_user_id, 'Carlos', 'Silva', 'carlos.silva@example.com', '{noop}carlos123',
        '+5511999990005', 'Rua Augusta', '1234', 'Ap 45',
        'Consolação', 'São Paulo', 'SP', '01305-100',
        'BR', 'https://example.com/avatars/carlos.png', 1
    );
END;
/

-- Usuario 6: Maria
DECLARE
    v_user_id VARCHAR2(36);
BEGIN
    v_user_id := SYS_GUID();
    INSERT INTO users (
        id, first_name, last_name, email, password_hash,
        phone_e164, address_street, address_number, address_complement,
        address_district, address_city, address_state, address_postal_code,
        address_country, profile_photo_url, is_active
    ) VALUES (
        v_user_id, 'Maria', 'Santos', 'maria.santos@example.com', '{noop}maria123',
        '+5511999990006', 'Av. Paulista', '2000', 'Conj 12',
        'Bela Vista', 'São Paulo', 'SP', '01310-100',
        'BR', 'https://example.com/avatars/maria.png', 1
    );
END;
/

-- Usuario 7: João
DECLARE
    v_user_id VARCHAR2(36);
BEGIN
    v_user_id := SYS_GUID();
    INSERT INTO users (
        id, first_name, last_name, email, password_hash,
        phone_e164, address_street, address_number, address_complement,
        address_district, address_city, address_state, address_postal_code,
        address_country, profile_photo_url, is_active
    ) VALUES (
        v_user_id, 'João', 'Oliveira', 'joao.oliveira@example.com', '{noop}joao123',
        '+5511999990007', 'Rua Oscar Freire', '300', NULL,
        'Jardins', 'São Paulo', 'SP', '01426-001',
        'BR', 'https://example.com/avatars/joao.png', 1
    );
END;
/

-- Usuario 8: Ana
DECLARE
    v_user_id VARCHAR2(36);
BEGIN
    v_user_id := SYS_GUID();
    INSERT INTO users (
        id, first_name, last_name, email, password_hash,
        phone_e164, address_street, address_number, address_complement,
        address_district, address_city, address_state, address_postal_code,
        address_country, profile_photo_url, is_active
    ) VALUES (
        v_user_id, 'Ana', 'Costa', 'ana.costa@example.com', '{noop}ana123',
        '+5511999990008', 'Av. Faria Lima', '1500', 'Sala 200',
        'Itaim Bibi', 'São Paulo', 'SP', '04538-132',
        'BR', 'https://example.com/avatars/ana.png', 1
    );
END;
/

-- Usuario 9: Pedro
DECLARE
    v_user_id VARCHAR2(36);
BEGIN
    v_user_id := SYS_GUID();
    INSERT INTO users (
        id, first_name, last_name, email, password_hash,
        phone_e164, address_street, address_number, address_complement,
        address_district, address_city, address_state, address_postal_code,
        address_country, profile_photo_url, is_active
    ) VALUES (
        v_user_id, 'Pedro', 'Fernandes', 'pedro.fernandes@example.com', '{noop}pedro123',
        '+5511999990009', 'Rua da Consolação', '800', 'Ap 101',
        'Consolação', 'São Paulo', 'SP', '01302-000',
        'BR', 'https://example.com/avatars/pedro.png', 1
    );
END;
/

-- Atribuições de roles
-- ADMIN: Admin
BEGIN
    INSERT INTO user_roles (user_id, role_id)
    SELECT u.id, r.id
    FROM users u
    CROSS JOIN roles r
    WHERE LOWER(u.email) = 'admin@nutrixpert.local'
    AND r.name = 'ADMIN';
END;
/

-- MANAGER: Manager
BEGIN
    INSERT INTO user_roles (user_id, role_id)
    SELECT u.id, r.id
    FROM users u
    CROSS JOIN roles r
    WHERE LOWER(u.email) = 'manager@nutrixpert.local'
    AND r.name = 'MANAGER';
END;
/

-- USER: Demais usuários
BEGIN
    INSERT INTO user_roles (user_id, role_id)
    SELECT u.id, r.id
    FROM users u
    CROSS JOIN roles r
    WHERE LOWER(u.email) IN (
        'alice@example.com',
        'bob@example.com',
        'carlos.silva@example.com',
        'maria.santos@example.com',
        'joao.oliveira@example.com',
        'ana.costa@example.com',
        'pedro.fernandes@example.com'
    )
    AND r.name = 'USER';
END;
/

-- Vista útil: v_user_profile
CREATE OR REPLACE VIEW v_user_profile AS
SELECT
    u.id,
    u.first_name,
    u.last_name,
    u.email,
    u.phone_e164,
    u.address_street,
    u.address_number,
    u.address_complement,
    u.address_district,
    u.address_city,
    u.address_state,
    u.address_postal_code,
    u.address_country,
    u.profile_photo_url,
    u.is_active,
    u.created_at,
    u.updated_at,
    LISTAGG(r.name, ',') WITHIN GROUP (ORDER BY r.name) AS roles
FROM users u
LEFT JOIN user_roles ur ON ur.user_id = u.id
LEFT JOIN roles r ON r.id = ur.role_id
GROUP BY
    u.id, u.first_name, u.last_name, u.email, u.phone_e164,
    u.address_street, u.address_number, u.address_complement,
    u.address_district, u.address_city, u.address_state,
    u.address_postal_code, u.address_country, u.profile_photo_url,
    u.is_active, u.created_at, u.updated_at;

-- Função para upsert de role
CREATE OR REPLACE FUNCTION upsert_role(p_name IN VARCHAR2) 
RETURN NUMBER IS
    v_id NUMBER;
BEGIN
    SELECT id INTO v_id FROM roles WHERE name = p_name;
    RETURN v_id;
EXCEPTION
    WHEN NO_DATA_FOUND THEN
        v_id := roles_seq.NEXTVAL;
        INSERT INTO roles(id, name) VALUES (v_id, p_name);
        COMMIT;
        RETURN v_id;
END upsert_role;
/

-- Procedure para adicionar role a usuário
CREATE OR REPLACE PROCEDURE add_user_role(
    p_email IN VARCHAR2,
    p_role IN VARCHAR2
) IS
    v_user VARCHAR2(36);
    v_role NUMBER;
BEGIN
    SELECT id INTO v_user FROM users WHERE LOWER(email) = LOWER(p_email);
    SELECT id INTO v_role FROM roles WHERE name = p_role;
    
    IF v_user IS NOT NULL AND v_role IS NOT NULL THEN
        INSERT INTO user_roles(user_id, role_id) 
        VALUES (v_user, v_role);
        COMMIT;
    END IF;
EXCEPTION
    WHEN DUP_VAL_ON_INDEX THEN
        NULL; -- Ignora se já existe
    WHEN NO_DATA_FOUND THEN
        NULL; -- Ignora se usuário ou role não existe
END add_user_role;
/

COMMIT;
