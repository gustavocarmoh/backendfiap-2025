-- Sistema de usuários + roles (PostgreSQL 16)
-- Compatível com Spring Security (seed com {noop} em dev)
CREATE EXTENSION IF NOT EXISTS pgcrypto;
-- gen_random_uuid()
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
-- uuid_generate_v4()

-- Função/trigger para updated_at
CREATE OR REPLACE FUNCTION set_updated_at()
RETURNS TRIGGER AS $$
BEGIN
  NEW.updated_at = NOW();
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Tabela de roles
CREATE TABLE IF NOT EXISTS roles (
    id BIGSERIAL PRIMARY KEY,
    name TEXT NOT NULL UNIQUE CHECK (
        char_length(name) BETWEEN 3 AND 50
    )
);

-- Tabela de usuários (com dados pessoais)
CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid (),
    first_name TEXT NOT NULL CHECK (
        char_length(first_name) BETWEEN 1 AND 150
    ),
    last_name TEXT NOT NULL CHECK (
        char_length(last_name) BETWEEN 1 AND 150
    ),
    email TEXT NOT NULL UNIQUE,
    password_hash TEXT NOT NULL, -- use BCrypt em prod. Em dev, {noop}SENHA
    phone_e164 TEXT, -- +5511999999999
    address_street TEXT,
    address_number TEXT,
    address_complement TEXT,
    address_district TEXT,
    address_city TEXT,
    address_state TEXT,
    address_postal_code TEXT, -- CEP
    address_country TEXT DEFAULT 'BR',
    profile_photo_url TEXT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT email_format_chk CHECK (position('@' IN email) > 1),
    CONSTRAINT phone_format_chk CHECK (
        phone_e164 IS NULL
        OR phone_e164 ~ '^\+?[0-9]{8,15}$'
    )
);

CREATE INDEX IF NOT EXISTS idx_users_email_ci ON users (lower(email));

-- Atualiza updated_at automaticamente
DROP TRIGGER IF EXISTS trg_users_updated_at ON users;

CREATE TRIGGER trg_users_updated_at
BEFORE UPDATE ON users
FOR EACH ROW EXECUTE FUNCTION set_updated_at();

-- Relação n:n usuário↔role
CREATE TABLE IF NOT EXISTS user_roles (
    user_id UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    role_id BIGINT NOT NULL REFERENCES roles (id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
);

-- Seeds de roles
INSERT INTO
    roles (name)
VALUES ('ADMIN'),
    ('MANAGER'),
    ('USER') ON CONFLICT (name) DO NOTHING;

-- Seeds de usuários (senha em dev com {noop})
-- Troque para BCrypt em prod (ex.: {bcrypt}$2a$10$...)
INSERT INTO
    users (
        first_name,
        last_name,
        email,
        password_hash,
        phone_e164,
        address_street,
        address_number,
        address_complement,
        address_district,
        address_city,
        address_state,
        address_postal_code,
        address_country,
        profile_photo_url,
        is_active
    )
VALUES (
        'System',
        'Admin',
        'admin@nutrixpert.local',
        '{noop}admin123',
        '+5531999990001',
        'Av. Afonso Pena',
        '1000',
        'Sala 101',
        'Centro',
        'Belo Horizonte',
        'MG',
        '30130-000',
        'BR',
        'https://example.com/avatars/admin.png',
        TRUE
    ),
    (
        'Manager',
        'User',
        'manager@nutrixpert.local',
        '{noop}manager123',
        '+5531999990002',
        'Rua dos Desenvolvedores',
        '42',
        NULL,
        'Funcionários',
        'Belo Horizonte',
        'MG',
        '30140-110',
        'BR',
        'https://example.com/avatars/manager.png',
        TRUE
    ),
    (
        'Alice',
        'Example',
        'alice@example.com',
        '{noop}alice123',
        '+5531999990003',
        'Rua das Flores',
        '200',
        'Ap 12',
        'Savassi',
        'Belo Horizonte',
        'MG',
        '30110-000',
        'BR',
        'https://example.com/avatars/alice.png',
        TRUE
    ),
    (
        'Bob',
        'Example',
        'bob@example.com',
        '{noop}bob123',
        '+5531999990004',
        'Av. Brasil',
        '500',
        NULL,
        'Funcionários',
        'Belo Horizonte',
        'MG',
        '30140-000',
        'BR',
        'https://example.com/avatars/bob.png',
        TRUE
    ) ON CONFLICT (email) DO NOTHING;

-- Atribuições de roles
-- ADMIN: Admin
INSERT INTO
    user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u
    CROSS JOIN roles r
WHERE
    lower(u.email) = 'admin@nutrixpert.local'
    AND r.name = 'ADMIN' ON CONFLICT DO NOTHING;

-- MANAGER: Manager
INSERT INTO
    user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u
    CROSS JOIN roles r
WHERE
    lower(u.email) = 'manager@nutrixpert.local'
    AND r.name = 'MANAGER' ON CONFLICT DO NOTHING;

-- USER: Alice e Bob
INSERT INTO
    user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u
    CROSS JOIN roles r
WHERE
    lower(u.email) IN (
        'alice@example.com',
        'bob@example.com'
    )
    AND r.name = 'USER' ON CONFLICT DO NOTHING;

-- Vistas úteis (opcional)
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
    COALESCE(
        array_agg (
            r.name
            ORDER BY r.name
        ) FILTER (
            WHERE
                r.name IS NOT NULL
        ),
        '{}'
    ) AS roles
FROM
    users u
    LEFT JOIN user_roles ur ON ur.user_id = u.id
    LEFT JOIN roles r ON r.id = ur.role_id
GROUP BY
    u.id;

-- Exemplos de upserts convenientes (funcões auxiliares, opcionais)
CREATE OR REPLACE FUNCTION upsert_role(p_name TEXT) RETURNS BIGINT AS $$
DECLARE v_id BIGINT;
BEGIN
  INSERT INTO roles(name) VALUES (p_name)
  ON CONFLICT (name) DO UPDATE SET name = EXCLUDED.name
  RETURNING id INTO v_id;
  RETURN v_id;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION add_user_role(p_email TEXT, p_role TEXT) RETURNS VOID AS $$
DECLARE v_user UUID; v_role BIGINT;
BEGIN
  SELECT id INTO v_user FROM users WHERE lower(email) = lower(p_email);
  SELECT id INTO v_role FROM roles WHERE name = p_role;
  IF v_user IS NOT NULL AND v_role IS NOT NULL THEN
    INSERT INTO user_roles(user_id, role_id) VALUES (v_user, v_role)
    ON CONFLICT DO NOTHING;
  END IF;
END;
$$ LANGUAGE plpgsql;