-- =====================================
-- V4: Sistema de Provedores de Serviço para Oracle
-- =====================================

CREATE TABLE service_providers (
    id VARCHAR2(36) PRIMARY KEY ,
    name VARCHAR2(150) NOT NULL,
    longitude NUMBER(11, 8) NOT NULL,
    latitude NUMBER(10, 8) NOT NULL,
    description VARCHAR2(500),
    active NUMBER(1) DEFAULT 1 NOT NULL,
    endereco VARCHAR2(300) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    created_by VARCHAR2(36),
    updated_by VARCHAR2(36),
    CONSTRAINT fk_svc_providers_created_by FOREIGN KEY (created_by) 
        REFERENCES users(id) ON DELETE SET NULL,
    CONSTRAINT fk_svc_providers_updated_by FOREIGN KEY (updated_by) 
        REFERENCES users(id) ON DELETE SET NULL,
    CONSTRAINT chk_longitude_range CHECK (longitude BETWEEN -180.0 AND 180.0),
    CONSTRAINT chk_latitude_range CHECK (latitude BETWEEN -90.0 AND 90.0),
    CONSTRAINT chk_active_provider CHECK (active IN (0, 1)),
    CONSTRAINT chk_name_not_empty CHECK (TRIM(name) IS NOT NULL),
    CONSTRAINT chk_endereco_not_empty CHECK (TRIM(endereco) IS NOT NULL)
);

CREATE INDEX idx_svc_providers_name ON service_providers(name);
CREATE INDEX idx_svc_providers_active ON service_providers(active);
CREATE INDEX idx_svc_providers_location ON service_providers(latitude, longitude);
CREATE INDEX idx_svc_providers_created_at ON service_providers(created_at);
CREATE INDEX idx_svc_providers_bounds ON service_providers(latitude, longitude, active);
CREATE INDEX idx_svc_providers_name_lower ON service_providers(LOWER(name));

-- Trigger para gerar UUID automaticamente para service_providers
CREATE OR REPLACE TRIGGER trg_service_providers_id
BEFORE INSERT ON service_providers
FOR EACH ROW
WHEN (NEW.id IS NULL)
BEGIN
    :NEW.id := LOWER(REGEXP_REPLACE(RAWTOHEX(SYS_GUID()), '([A-F0-9]{8})([A-F0-9]{4})([A-F0-9]{4})([A-F0-9]{4})([A-F0-9]{12})', '\1-\2-\3-\4-\5'));
END;
/

CREATE OR REPLACE TRIGGER trg_service_providers_upd
BEFORE UPDATE ON service_providers
FOR EACH ROW
BEGIN
    :NEW.updated_at := CURRENT_TIMESTAMP;
END;
/

-- Inserir supermercados de São Paulo
DECLARE
    v_admin_id VARCHAR2(36);
BEGIN
    SELECT id INTO v_admin_id FROM users WHERE email = 'admin@nutrixpert.local' AND ROWNUM = 1;
    
    -- Zona Central
    INSERT INTO service_providers (name, latitude, longitude, description, endereco, active, created_by)
    VALUES ('Pão de Açúcar - Paulista', -23.5618, -46.6563, 
        'Supermercado premium na Av. Paulista com ampla variedade de produtos orgânicos e importados',
        'Av. Paulista, 2064 - Bela Vista, São Paulo - SP', 1, v_admin_id);
    
    INSERT INTO service_providers (name, latitude, longitude, description, endereco, active, created_by)
    VALUES ('Extra Hiper - Consolação', -23.5548, -46.6526,
        'Hipermercado completo com seção especial de produtos naturais e diet',
        'Rua da Consolação, 2787 - Consolação, São Paulo - SP', 1, v_admin_id);
    
    INSERT INTO service_providers (name, latitude, longitude, description, endereco, active, created_by)
    VALUES ('Carrefour Express - Augusta', -23.5567, -46.6584,
        'Supermercado compacto com foco em conveniência e produtos frescos',
        'Rua Augusta, 1508 - Consolação, São Paulo - SP', 1, v_admin_id);
    
    -- Zona Oeste
    INSERT INTO service_providers (name, latitude, longitude, description, endereco, active, created_by)
    VALUES ('Pão de Açúcar - Faria Lima', -23.5729, -46.6928,
        'Supermercado premium no coração financeiro com produtos gourmet',
        'Av. Brigadeiro Faria Lima, 1821 - Jardim Paulistano, São Paulo - SP', 1, v_admin_id);
    
    INSERT INTO service_providers (name, latitude, longitude, description, endereco, active, created_by)
    VALUES ('Carrefour - Shopping Villa Lobos', -23.5447, -46.7208,
        'Hipermercado dentro do shopping com grande variedade de marcas',
        'Av. das Nações Unidas, 4777 - Alto de Pinheiros, São Paulo - SP', 1, v_admin_id);
    
    -- Zona Sul
    INSERT INTO service_providers (name, latitude, longitude, description, endereco, active, created_by)
    VALUES ('Pão de Açúcar - Ibirapuera', -23.5915, -46.6575,
        'Supermercado premium próximo ao parque com seção orgânica',
        'Av. Ibirapuera, 2332 - Moema, São Paulo - SP', 1, v_admin_id);
    
    INSERT INTO service_providers (name, latitude, longitude, description, endereco, active, created_by)
    VALUES ('Carrefour - Morumbi', -23.6284, -46.7019,
        'Hipermercado moderno com ampla seção de produtos saudáveis',
        'Av. Dr. Chucri Zaidan, 902 - Vila Cordeiro, São Paulo - SP', 1, v_admin_id);
    
    -- Zona Leste
    INSERT INTO service_providers (name, latitude, longitude, description, endereco, active, created_by)
    VALUES ('Extra Hiper - Aricanduva', -23.5623, -46.5134,
        'Maior hipermercado da zona leste com variedade completa',
        'Av. Aricanduva, 5555 - Vila Matilde, São Paulo - SP', 1, v_admin_id);
    
    INSERT INTO service_providers (name, latitude, longitude, description, endereco, active, created_by)
    VALUES ('Carrefour - Tatuapé', -23.5386, -46.5764,
        'Supermercado moderno no centro comercial da zona leste',
        'Rua Tuiuti, 2100 - Tatuapé, São Paulo - SP', 1, v_admin_id);
    
    -- Zona Norte
    INSERT INTO service_providers (name, latitude, longitude, description, endereco, active, created_by)
    VALUES ('Extra - Santana', -23.5035, -46.6291,
        'Supermercado tradicional da zona norte com produtos regionais',
        'Av. Cruzeiro do Sul, 1100 - Santana, São Paulo - SP', 1, v_admin_id);
    
    INSERT INTO service_providers (name, latitude, longitude, description, endereco, active, created_by)
    VALUES ('Mundo Verde - Jardins', -23.5641, -46.6743,
        'Rede especializada em produtos naturais, orgânicos e suplementos',
        'Rua Oscar Freire, 909 - Jardins, São Paulo - SP', 1, v_admin_id);
    
    INSERT INTO service_providers (name, latitude, longitude, description, endereco, active, created_by)
    VALUES ('Hortifruti - Vila Madalena', -23.5445, -46.6899,
        'Especializado em frutas, verduras e produtos frescos premium',
        'Rua Harmonia, 85 - Vila Madalena, São Paulo - SP', 1, v_admin_id);
    
EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK;
        RAISE;
END;
/
