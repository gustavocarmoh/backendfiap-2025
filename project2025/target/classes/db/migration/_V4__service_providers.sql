-- Migration para criar tabela de fornecedores de serviços
-- Inclui dados geográficos para plotagem em mapas e controle administrativo

-- Criar tabela para fornecedores de serviços
CREATE TABLE service_providers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid (),
    name VARCHAR(150) NOT NULL,
    longitude DECIMAL(11, 8) NOT NULL,
    latitude DECIMAL(10, 8) NOT NULL,
    description VARCHAR(500),
    active BOOLEAN NOT NULL DEFAULT true,
    endereco VARCHAR(300) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by UUID,
    updated_by UUID,
    CONSTRAINT fk_service_providers_created_by FOREIGN KEY (created_by) REFERENCES users (id) ON DELETE SET NULL,
    CONSTRAINT fk_service_providers_updated_by FOREIGN KEY (updated_by) REFERENCES users (id) ON DELETE SET NULL
);

-- Criar índices para melhor performance
CREATE INDEX idx_service_providers_name ON service_providers (name);

CREATE INDEX idx_service_providers_active ON service_providers (active);

CREATE INDEX idx_service_providers_location ON service_providers (latitude, longitude);

CREATE INDEX idx_service_providers_created_at ON service_providers (created_at);

CREATE INDEX idx_service_providers_updated_at ON service_providers (updated_at);

-- Índice composto para busca por área geográfica
CREATE INDEX idx_service_providers_bounds ON service_providers (latitude, longitude, active);

-- Índice para busca por nome (case-insensitive)
CREATE INDEX idx_service_providers_name_lower ON service_providers (LOWER(name));

-- Constraint para validar coordenadas geográficas
ALTER TABLE service_providers
ADD CONSTRAINT chk_longitude_range CHECK (
    longitude >= -180.0
    AND longitude <= 180.0
);

ALTER TABLE service_providers
ADD CONSTRAINT chk_latitude_range CHECK (
    latitude >= -90.0
    AND latitude <= 90.0
);

-- Constraint para garantir que nome não seja vazio
ALTER TABLE service_providers
ADD CONSTRAINT chk_name_not_empty CHECK (TRIM(name) != '');

-- Constraint para garantir que endereço não seja vazio
ALTER TABLE service_providers
ADD CONSTRAINT chk_endereco_not_empty CHECK (TRIM(endereco) != '');

-- Comentários para documentação
COMMENT ON
TABLE service_providers IS 'Fornecedores de serviços com dados geográficos para plotagem em mapas';

COMMENT ON COLUMN service_providers.id IS 'Identificador único do fornecedor';

COMMENT ON COLUMN service_providers.name IS 'Nome do fornecedor de serviços';

COMMENT ON COLUMN service_providers.longitude IS 'Longitude geográfica em graus decimais (-180 a 180)';

COMMENT ON COLUMN service_providers.latitude IS 'Latitude geográfica em graus decimais (-90 a 90)';

COMMENT ON COLUMN service_providers.description IS 'Descrição dos serviços oferecidos';

COMMENT ON COLUMN service_providers.active IS 'Indica se o fornecedor está ativo no sistema';

COMMENT ON COLUMN service_providers.endereco IS 'Endereço completo do fornecedor';

COMMENT ON COLUMN service_providers.created_at IS 'Data de criação do registro';

COMMENT ON COLUMN service_providers.updated_at IS 'Data da última atualização';

COMMENT ON COLUMN service_providers.created_by IS 'ID do administrador que criou o registro';

COMMENT ON COLUMN service_providers.updated_by IS 'ID do administrador que fez a última atualização';

-- Trigger para atualizar updated_at automaticamente
CREATE OR REPLACE FUNCTION update_service_providers_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER trigger_service_providers_updated_at
    BEFORE UPDATE ON service_providers
    FOR EACH ROW
    EXECUTE FUNCTION update_service_providers_updated_at();

-- Inserir alguns dados de exemplo (opcional - remover em produção)
INSERT INTO
    service_providers (
        name,
        longitude,
        latitude,
        description,
        endereco,
        active,
        created_by
    )
VALUES (
        'TechFix Assistência Técnica',
        -46.633308,
        -23.550520,
        'Assistência técnica especializada em equipamentos eletrônicos e informática',
        'Rua Augusta, 123 - Consolação, São Paulo - SP',
        true,
        NULL
    ),
    (
        'CleanMax Limpeza',
        -46.654295,
        -23.563986,
        'Serviços de limpeza residencial e comercial com produtos ecológicos',
        'Av. Paulista, 456 - Bela Vista, São Paulo - SP',
        true,
        NULL
    ),
    (
        'GreenGarden Jardinagem',
        -46.625290,
        -23.533773,
        'Serviços de jardinagem, paisagismo e manutenção de áreas verdes',
        'Rua Haddock Lobo, 789 - Cerqueira César, São Paulo - SP',
        true,
        NULL
    ),
    (
        'FastDelivery Entregas',
        -46.640010,
        -23.546687,
        'Serviços de entrega rápida e logística urbana',
        'Rua Oscar Freire, 321 - Jardins, São Paulo - SP',
        true,
        NULL
    ),
    (
        'RepairPro Manutenção',
        -46.673020,
        -23.574560,
        'Manutenção predial, hidráulica e elétrica residencial e comercial',
        'Av. Rebouças, 987 - Pinheiros, São Paulo - SP',
        false,
        NULL
    );