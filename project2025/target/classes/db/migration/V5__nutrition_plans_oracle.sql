-- =====================================
-- V5: Sistema de Planos Nutricionais para Oracle
-- =====================================

-- Criar tabela de planos nutricionais
CREATE TABLE nutrition_plans (
    id VARCHAR2(36) PRIMARY KEY ,
    user_id VARCHAR2(36) NOT NULL,
    plan_date DATE NOT NULL,
    title VARCHAR2(200) NOT NULL,
    description CLOB,
    breakfast CLOB,
    morning_snack CLOB,
    lunch CLOB,
    afternoon_snack CLOB,
    dinner CLOB,
    evening_snack CLOB,
    total_calories NUMBER(10) CHECK (total_calories BETWEEN 0 AND 10000),
    total_proteins NUMBER(8, 2) CHECK (total_proteins BETWEEN 0 AND 1000),
    total_carbohydrates NUMBER(8, 2) CHECK (total_carbohydrates BETWEEN 0 AND 2000),
    total_fats NUMBER(8, 2) CHECK (total_fats BETWEEN 0 AND 500),
    water_intake_ml NUMBER(10) CHECK (water_intake_ml BETWEEN 0 AND 10000),
    is_completed NUMBER(1) DEFAULT 0 NOT NULL,
    completed_at TIMESTAMP,
    notes CLOB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT fk_nutrition_plans_user FOREIGN KEY (user_id) 
        REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT chk_is_completed CHECK (is_completed IN (0, 1))
);

-- Constraint única para garantir um plano por usuário por data
CREATE UNIQUE INDEX uk_nutrition_plan_user_date ON nutrition_plans(user_id, plan_date);

-- Índices para performance (não criar índice duplicado em user_id, plan_date pois já tem o UNIQUE)
CREATE INDEX idx_nutrition_plan_user ON nutrition_plans(user_id);
CREATE INDEX idx_nutrition_plan_date ON nutrition_plans(plan_date);
CREATE INDEX idx_nutrition_plan_completed ON nutrition_plans(user_id, is_completed);
CREATE INDEX idx_nutrition_plan_user_date_completed ON nutrition_plans(user_id, plan_date, is_completed);

-- Trigger para updated_at
CREATE OR REPLACE TRIGGER trg_nutrition_plans_upd
BEFORE UPDATE ON nutrition_plans
FOR EACH ROW
BEGIN
    :NEW.updated_at := CURRENT_TIMESTAMP;
END;
/

-- Tabela de alertas nutricionais (para a procedure)
CREATE TABLE nutrition_alerts (
    id VARCHAR2(36) PRIMARY KEY ,
    user_id VARCHAR2(36) NOT NULL,
    nutrition_plan_id VARCHAR2(36),
    alert_type VARCHAR2(50) NOT NULL,
    alert_message CLOB NOT NULL,
    severity VARCHAR2(20) DEFAULT 'INFO',
    is_read NUMBER(1) DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT fk_nutrition_alerts_user FOREIGN KEY (user_id) 
        REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_nutrition_alerts_plan FOREIGN KEY (nutrition_plan_id) 
        REFERENCES nutrition_plans(id) ON DELETE SET NULL,
    CONSTRAINT chk_alert_severity CHECK (severity IN ('INFO', 'WARNING', 'CRITICAL')),
    CONSTRAINT chk_alert_is_read CHECK (is_read IN (0, 1))
);

CREATE INDEX idx_nutrition_alerts_user ON nutrition_alerts(user_id);
CREATE INDEX idx_nutrition_alerts_read ON nutrition_alerts(user_id, is_read);
CREATE INDEX idx_nutrition_alerts_created ON nutrition_alerts(created_at);




