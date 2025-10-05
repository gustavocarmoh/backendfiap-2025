-- =====================================
-- V2: Sistema de Planos de Assinatura para Oracle
-- =====================================

-- Criar sequência para subscription_plans
CREATE SEQUENCE subscription_plans_seq START WITH 1 INCREMENT BY 1;

-- Tabela de planos de assinatura
CREATE TABLE subscription_plans (
    id VARCHAR2(36) PRIMARY KEY ,
    name VARCHAR2(255) NOT NULL UNIQUE,
    price NUMBER(10, 2) NOT NULL,
    description CLOB,
    nutrition_plans_limit NUMBER(10),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_active NUMBER(1) DEFAULT 1,
    CONSTRAINT chk_price_positive CHECK (price > 0),
    CONSTRAINT chk_is_active_plan CHECK (is_active IN (0, 1))
);

-- Trigger para gerar UUID automaticamente para subscription_plans
CREATE OR REPLACE TRIGGER trg_subscription_plans_id
BEFORE INSERT ON subscription_plans
FOR EACH ROW
WHEN (NEW.id IS NULL)
BEGIN
    :NEW.id := LOWER(REGEXP_REPLACE(RAWTOHEX(SYS_GUID()), '([A-F0-9]{8})([A-F0-9]{4})([A-F0-9]{4})([A-F0-9]{4})([A-F0-9]{12})', '\1-\2-\3-\4-\5'));
END;
/

-- Tabela de assinaturas
CREATE TABLE subscriptions (
    id VARCHAR2(36) PRIMARY KEY ,
    user_id VARCHAR2(36) NOT NULL,
    plan_id VARCHAR2(36) NOT NULL,
    status VARCHAR2(50) DEFAULT 'PENDING' NOT NULL,
    approved_by_user_id VARCHAR2(36),
    subscription_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    approved_date TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    amount NUMBER(10, 2) NOT NULL,
    CONSTRAINT fk_subscriptions_user FOREIGN KEY (user_id) 
        REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_subscriptions_plan FOREIGN KEY (plan_id) 
        REFERENCES subscription_plans(id),
    CONSTRAINT fk_subscriptions_approver FOREIGN KEY (approved_by_user_id) 
        REFERENCES users(id) ON DELETE SET NULL,
    CONSTRAINT chk_subscription_status CHECK (
        status IN ('PENDING', 'APPROVED', 'REJECTED', 'CANCELLED')
    )
);

-- Trigger para gerar UUID automaticamente para subscriptions
CREATE OR REPLACE TRIGGER trg_subscriptions_id
BEFORE INSERT ON subscriptions
FOR EACH ROW
WHEN (NEW.id IS NULL)
BEGIN
    :NEW.id := LOWER(REGEXP_REPLACE(RAWTOHEX(SYS_GUID()), '([A-F0-9]{8})([A-F0-9]{4})([A-F0-9]{4})([A-F0-9]{4})([A-F0-9]{12})', '\1-\2-\3-\4-\5'));
END;
/

-- Índices para performance
CREATE INDEX idx_subscriptions_user_id ON subscriptions(user_id);
CREATE INDEX idx_subscriptions_plan_id ON subscriptions(plan_id);
CREATE INDEX idx_subscriptions_status ON subscriptions(status);
CREATE INDEX idx_subscriptions_created_at ON subscriptions(created_at);
CREATE INDEX idx_subscription_plans_is_active ON subscription_plans(is_active);

-- Triggers para updated_at
CREATE OR REPLACE TRIGGER trg_subscription_plans_upd
BEFORE UPDATE ON subscription_plans
FOR EACH ROW
BEGIN
    :NEW.updated_at := CURRENT_TIMESTAMP;
END;
/

CREATE OR REPLACE TRIGGER trg_subscriptions_upd
BEFORE UPDATE ON subscriptions
FOR EACH ROW
BEGIN
    :NEW.updated_at := CURRENT_TIMESTAMP;
END;
/

-- Inserir planos padrão
INSERT INTO subscription_plans (name, price, is_active, description, nutrition_plans_limit)
VALUES ('Basic Plan', 9.99, 1, 'Plano básico com até 10 planos nutricionais por mês', 10);

INSERT INTO subscription_plans (name, price, is_active, description, nutrition_plans_limit)
VALUES ('Standard Plan', 19.99, 1, 'Plano intermediário com até 20 planos nutricionais por mês', 20);

INSERT INTO subscription_plans (name, price, is_active, description, nutrition_plans_limit)
VALUES ('Premium Plan', 29.99, 1, 'Plano premium com planos nutricionais ilimitados', NULL);

INSERT INTO subscription_plans (name, price, is_active, description, nutrition_plans_limit)
VALUES ('Enterprise Plan', 49.99, 1, 'Plano empresarial com planos nutricionais ilimitados', NULL);

-- Criar subscrições para usuários específicos
-- Carlos: Basic Plan
BEGIN
    INSERT INTO subscriptions (user_id, plan_id, status, subscription_date, approved_date, amount)
    SELECT 
        u.id, 
        sp.id, 
        'APPROVED', 
        CURRENT_TIMESTAMP - INTERVAL '30' DAY,
        CURRENT_TIMESTAMP - INTERVAL '29' DAY,
        sp.price
    FROM users u
    CROSS JOIN subscription_plans sp
    WHERE u.email = 'carlos.silva@example.com' 
    AND sp.name = 'Basic Plan';
END;
/

-- Maria: Standard Plan
BEGIN
    INSERT INTO subscriptions (user_id, plan_id, status, subscription_date, approved_date, amount)
    SELECT 
        u.id, 
        sp.id, 
        'APPROVED', 
        CURRENT_TIMESTAMP - INTERVAL '20' DAY,
        CURRENT_TIMESTAMP - INTERVAL '19' DAY,
        sp.price
    FROM users u
    CROSS JOIN subscription_plans sp
    WHERE u.email = 'maria.santos@example.com' 
    AND sp.name = 'Standard Plan';
END;
/

-- João: Premium Plan
BEGIN
    INSERT INTO subscriptions (user_id, plan_id, status, subscription_date, approved_date, amount)
    SELECT 
        u.id, 
        sp.id, 
        'APPROVED', 
        CURRENT_TIMESTAMP - INTERVAL '15' DAY,
        CURRENT_TIMESTAMP - INTERVAL '14' DAY,
        sp.price
    FROM users u
    CROSS JOIN subscription_plans sp
    WHERE u.email = 'joao.oliveira@example.com' 
    AND sp.name = 'Premium Plan';
END;
/

-- Ana: Enterprise Plan
BEGIN
    INSERT INTO subscriptions (user_id, plan_id, status, subscription_date, approved_date, amount)
    SELECT 
        u.id, 
        sp.id, 
        'APPROVED', 
        CURRENT_TIMESTAMP - INTERVAL '10' DAY,
        CURRENT_TIMESTAMP - INTERVAL '9' DAY,
        sp.price
    FROM users u
    CROSS JOIN subscription_plans sp
    WHERE u.email = 'ana.costa@example.com' 
    AND sp.name = 'Enterprise Plan';
END;
/

COMMIT;
