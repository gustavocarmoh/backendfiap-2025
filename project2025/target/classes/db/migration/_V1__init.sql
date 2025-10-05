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
    ('USER')
ON CONFLICT (name) DO NOTHING;

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
    ),
    -- Usuários adicionais para diferentes tipos de planos de assinatura
    (
        'Carlos',
        'Silva',
        'carlos.silva@example.com',
        '{noop}carlos123',
        '+5511999990005',
        'Rua Augusta',
        '1234',
        'Ap 45',
        'Consolação',
        'São Paulo',
        'SP',
        '01305-100',
        'BR',
        'https://example.com/avatars/carlos.png',
        TRUE
    ),
    (
        'Maria',
        'Santos',
        'maria.santos@example.com',
        '{noop}maria123',
        '+5511999990006',
        'Av. Paulista',
        '2000',
        'Conj 12',
        'Bela Vista',
        'São Paulo',
        'SP',
        '01310-100',
        'BR',
        'https://example.com/avatars/maria.png',
        TRUE
    ),
    (
        'João',
        'Oliveira',
        'joao.oliveira@example.com',
        '{noop}joao123',
        '+5511999990007',
        'Rua Oscar Freire',
        '300',
        NULL,
        'Jardins',
        'São Paulo',
        'SP',
        '01426-001',
        'BR',
        'https://example.com/avatars/joao.png',
        TRUE
    ),
    (
        'Ana',
        'Costa',
        'ana.costa@example.com',
        '{noop}ana123',
        '+5511999990008',
        'Av. Faria Lima',
        '1500',
        'Sala 200',
        'Itaim Bibi',
        'São Paulo',
        'SP',
        '04538-132',
        'BR',
        'https://example.com/avatars/ana.png',
        TRUE
    ),
    (
        'Pedro',
        'Fernandes',
        'pedro.fernandes@example.com',
        '{noop}pedro123',
        '+5511999990009',
        'Rua da Consolação',
        '800',
        'Ap 101',
        'Consolação',
        'São Paulo',
        'SP',
        '01302-000',
        'BR',
        'https://example.com/avatars/pedro.png',
        TRUE
    )
ON CONFLICT (email) DO NOTHING;

-- Atribuições de roles
-- ADMIN: Admin
INSERT INTO
    user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u
    CROSS JOIN roles r
WHERE
    lower(u.email) = 'admin@nutrixpert.local'
    AND r.name = 'ADMIN'
ON CONFLICT DO NOTHING;

-- MANAGER: Manager
INSERT INTO
    user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u
    CROSS JOIN roles r
WHERE
    lower(u.email) = 'manager@nutrixpert.local'
    AND r.name = 'MANAGER'
ON CONFLICT DO NOTHING;

-- USER: Alice e Bob + novos usuários
INSERT INTO
    user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u
    CROSS JOIN roles r
WHERE
    lower(u.email) IN (
        'alice@example.com',
        'bob@example.com',
        'carlos.silva@example.com',
        'maria.santos@example.com',
        'joao.oliveira@example.com',
        'ana.costa@example.com',
        'pedro.fernandes@example.com'
    )
    AND r.name = 'USER'
ON CONFLICT DO NOTHING;

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
        array_agg(
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

-- CREATE OR REPLACE FUNCTION add_user_role(p_email TEXT, p_role TEXT) RETURNS VOID AS $$
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

-- =====================================
-- DADOS ADICIONAIS PARA PLANOS E SUPERMERCADOS
-- =====================================

-- Aguardar criação das tabelas de subscription_plans (V2) e service_providers (V4)
-- Os dados serão inseridos através de triggers após a criação das tabelas

-- Função para inserir dados após a migração V2 (subscription_plans)
CREATE OR REPLACE FUNCTION insert_subscription_data() RETURNS VOID AS $$
BEGIN
    -- Verificar se a tabela subscription_plans existe
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'subscription_plans') THEN
        
        -- Atualizar planos existentes com limites nutricionais corretos
        UPDATE subscription_plans 
        SET 
            nutrition_plans_limit = CASE 
                WHEN name = 'Basic Plan' THEN 10
                WHEN name = 'Standard Plan' THEN 20  
                WHEN name = 'Premium Plan' THEN NULL -- Ilimitado
                WHEN name = 'Enterprise Plan' THEN NULL -- Ilimitado
                ELSE nutrition_plans_limit
            END,
            description = CASE
                WHEN name = 'Basic Plan' THEN 'Plano básico com até 10 planos nutricionais por mês'
                WHEN name = 'Standard Plan' THEN 'Plano intermediário com até 20 planos nutricionais por mês'
                WHEN name = 'Premium Plan' THEN 'Plano premium com planos nutricionais ilimitados'
                WHEN name = 'Enterprise Plan' THEN 'Plano empresarial com planos nutricionais ilimitados'
                ELSE description
            END
        WHERE name IN ('Basic Plan', 'Standard Plan', 'Premium Plan', 'Enterprise Plan');

        -- Criar subscrições para usuários específicos
        -- Carlos: Basic Plan (10 planos)
        INSERT INTO subscriptions (user_id, plan_id, status, subscription_date, approved_date, amount)
        SELECT 
            u.id, 
            sp.id, 
            'APPROVED', 
            CURRENT_TIMESTAMP - INTERVAL '30 days',
            CURRENT_TIMESTAMP - INTERVAL '29 days',
            sp.price
        FROM users u
        CROSS JOIN subscription_plans sp
        WHERE u.email = 'carlos.silva@example.com' 
        AND sp.name = 'Basic Plan'
        ON CONFLICT DO NOTHING;

        -- Maria: Standard Plan (20 planos) 
        INSERT INTO subscriptions (user_id, plan_id, status, subscription_date, approved_date, amount)
        SELECT 
            u.id, 
            sp.id, 
            'APPROVED', 
            CURRENT_TIMESTAMP - INTERVAL '20 days',
            CURRENT_TIMESTAMP - INTERVAL '19 days',
            sp.price
        FROM users u
        CROSS JOIN subscription_plans sp
        WHERE u.email = 'maria.santos@example.com' 
        AND sp.name = 'Standard Plan'
        ON CONFLICT DO NOTHING;

        -- João: Premium Plan (ilimitado)
        INSERT INTO subscriptions (user_id, plan_id, status, subscription_date, approved_date, amount)
        SELECT 
            u.id, 
            sp.id, 
            'APPROVED', 
            CURRENT_TIMESTAMP - INTERVAL '15 days',
            CURRENT_TIMESTAMP - INTERVAL '14 days',
            sp.price
        FROM users u
        CROSS JOIN subscription_plans sp
        WHERE u.email = 'joao.oliveira@example.com' 
        AND sp.name = 'Premium Plan'
        ON CONFLICT DO NOTHING;

        -- Ana: Enterprise Plan (ilimitado)
        INSERT INTO subscriptions (user_id, plan_id, status, subscription_date, approved_date, amount)
        SELECT 
            u.id, 
            sp.id, 
            'APPROVED', 
            CURRENT_TIMESTAMP - INTERVAL '10 days',
            CURRENT_TIMESTAMP - INTERVAL '9 days',
            sp.price
        FROM users u
        CROSS JOIN subscription_plans sp
        WHERE u.email = 'ana.costa@example.com' 
        AND sp.name = 'Enterprise Plan'
        ON CONFLICT DO NOTHING;

        -- Alice e Bob ficam como usuários FREE (sem plano)
        -- Pedro também fica como usuário FREE
        -- Admin nunca tem plano
        
    END IF;
END;
$$ LANGUAGE plpgsql;

-- Função para inserir provedores de serviço (supermercados de São Paulo)
CREATE OR REPLACE FUNCTION insert_service_providers_data() RETURNS VOID AS $$
BEGIN
    -- Verificar se a tabela service_providers existe
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'service_providers') THEN
        
        -- Inserir supermercados reais de São Paulo
        INSERT INTO service_providers (name, latitude, longitude, description, endereco, active, created_by)
        SELECT 
            nome,
            lat,
            lng,
            descricao,
            endereco,
            true,
            (SELECT id FROM users WHERE email = 'admin@nutrixpert.local' LIMIT 1)
        FROM (VALUES 
            -- Zona Central
            ('Pão de Açúcar - Paulista', -23.5618, -46.6563, 'Supermercado premium na Av. Paulista com ampla variedade de produtos orgânicos e importados', 'Av. Paulista, 2064 - Bela Vista, São Paulo - SP'),
            ('Extra Hiper - Consolação', -23.5548, -46.6526, 'Hipermercado completo com seção especial de produtos naturais e diet', 'Rua da Consolação, 2787 - Consolação, São Paulo - SP'),
            ('Carrefour Express - Augusta', -23.5567, -46.6584, 'Supermercado compacto com foco em conveniência e produtos frescos', 'Rua Augusta, 1508 - Consolação, São Paulo - SP'),
            
            -- Zona Oeste  
            ('Pão de Açúcar - Faria Lima', -23.5729, -46.6928, 'Supermercado premium no coração financeiro com produtos gourmet', 'Av. Brigadeiro Faria Lima, 1821 - Jardim Paulistano, São Paulo - SP'),
            ('Carrefour - Shopping Villa Lobos', -23.5447, -46.7208, 'Hipermercado dentro do shopping com grande variedade de marcas', 'Av. das Nações Unidas, 4777 - Alto de Pinheiros, São Paulo - SP'),
            ('Extra - Butantã', -23.5651, -46.7286, 'Supermercado familiar com preços competitivos e produtos locais', 'Av. Corifeu de Azevedo Marques, 5559 - Butantã, São Paulo - SP'),
            ('Walmart - Lapa', -23.5281, -46.7019, 'Hipermercado com foco em economia familiar e variedade', 'Rua Guaicurus, 1000 - Lapa, São Paulo - SP'),
            
            -- Zona Sul
            ('Pão de Açúcar - Ibirapuera', -23.5915, -46.6575, 'Supermercado premium próximo ao parque com seção orgânica', 'Av. Ibirapuera, 2332 - Moema, São Paulo - SP'),
            ('Carrefour - Morumbi', -23.6284, -46.7019, 'Hipermercado moderno com ampla seção de produtos saudáveis', 'Av. Dr. Chucri Zaidan, 902 - Vila Cordeiro, São Paulo - SP'),
            ('Extra - Vila Olímpia', -23.5952, -46.6897, 'Supermercado estratégico para empresários da região', 'Rua Surubim, 577 - Vila Olímpia, São Paulo - SP'),
            ('Sonda Supermercados - Brooklin', -23.6145, -46.6784, 'Rede tradicional paulista com produtos regionais', 'Av. Engenheiro Luís Carlos Berrini, 1140 - Brooklin, São Paulo - SP'),
            
            -- Zona Leste
            ('Extra Hiper - Aricanduva', -23.5623, -46.5134, 'Maior hipermercado da zona leste com variedade completa', 'Av. Aricanduva, 5555 - Vila Matilde, São Paulo - SP'),
            ('Carrefour - Tatuapé', -23.5386, -46.5764, 'Supermercado moderno no centro comercial da zona leste', 'Rua Tuiuti, 2100 - Tatuapé, São Paulo - SP'),
            ('Atacadão - Penha', -23.5284, -46.5456, 'Atacarejo com preços populares e grande volume', 'Av. Celso Garcia, 4798 - Penha, São Paulo - SP'),
            ('BIG - Itaquera', -23.5403, -46.4566, 'Hipermercado popular com foco em economia familiar', 'Av. José Pinheiro Borges, s/n - Itaquera, São Paulo - SP'),
            
            -- Zona Norte
            ('Extra - Santana', -23.5035, -46.6291, 'Supermercado tradicional da zona norte com produtos regionais', 'Av. Cruzeiro do Sul, 1100 - Santana, São Paulo - SP'),
            ('Carrefour - Marginal Tietê', -23.5108, -46.6108, 'Hipermercado estratégico para quem vem do interior', 'Av. Marginal Direita do Tietê, 500 - Vila Guilherme, São Paulo - SP'),
            ('Pão de Açúcar - Tucuruvi', -23.4845, -46.6123, 'Supermercado de bairro com atendimento personalizado', 'Av. Dr. Antônio Maria Laet, 566 - Tucuruvi, São Paulo - SP'),
            ('Walmart - Casa Verde', -23.5067, -46.6589, 'Hipermercado econômico para famílias da região', 'Av. Casa Verde, 3097 - Casa Verde, São Paulo - SP'),
            
            -- ABC Paulista (próximo a SP)
            ('Extra Hiper - Santo André', -23.6634, -46.5423, 'Grande hipermercado servindo o ABC paulista', 'Av. Industrial, 600 - Santo André - SP'),
            ('Carrefour - São Bernardo', -23.6939, -46.5645, 'Supermercado estratégico na divisa com São Paulo', 'Av. Kennedy, 700 - São Bernardo do Campo - SP'),
            
            -- Supermercados especializados em produtos naturais
            ('Mundo Verde - Jardins', -23.5641, -46.6743, 'Rede especializada em produtos naturais, orgânicos e suplementos', 'Rua Oscar Freire, 909 - Jardins, São Paulo - SP'),
            ('Hortifruti - Vila Madalena', -23.5445, -46.6899, 'Especializado em frutas, verduras e produtos frescos premium', 'Rua Harmonia, 85 - Vila Madalena, São Paulo - SP'),
            ('Casa Santa Luzia - Higienópolis', -23.5478, -46.6523, 'Tradicional casa de produtos naturais e especiarias', 'Av. Higienópolis, 618 - Higienópolis, São Paulo - SP'),
            ('Empório Santa Maria - Itaim', -23.5789, -46.6789, 'Empório gourmet com produtos orgânicos e importados', 'Rua João Cachoeira, 278 - Itaim Bibi, São Paulo - SP')
        ) AS t(nome, lat, lng, descricao, endereco)
        ON CONFLICT DO NOTHING;
        
    END IF;
END;
$$ LANGUAGE plpgsql;