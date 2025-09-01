-- V5: Criação do sistema de planos nutricionais (H2 Database)

-- Atualizar tabela de planos de assinatura com limites de planos nutricionais
ALTER TABLE subscription_plans
ADD COLUMN nutrition_plans_limit INTEGER;

ALTER TABLE subscription_plans ADD COLUMN description CLOB;

-- Criar tabela de planos nutricionais
CREATE TABLE nutrition_plans (
    id UUID PRIMARY KEY DEFAULT RANDOM_UUID (),
    user_id UUID NOT NULL,
    plan_date DATE NOT NULL,
    title VARCHAR(200) NOT NULL,
    description CLOB,
    breakfast CLOB,
    morning_snack CLOB,
    lunch CLOB,
    afternoon_snack CLOB,
    dinner CLOB,
    evening_snack CLOB,
    total_calories INTEGER CHECK (
        total_calories >= 0
        AND total_calories <= 10000
    ),
    total_proteins DECIMAL(8, 2) CHECK (
        total_proteins >= 0
        AND total_proteins <= 1000
    ),
    total_carbohydrates DECIMAL(8, 2) CHECK (
        total_carbohydrates >= 0
        AND total_carbohydrates <= 2000
    ),
    total_fats DECIMAL(8, 2) CHECK (
        total_fats >= 0
        AND total_fats <= 500
    ),
    water_intake_ml INTEGER CHECK (
        water_intake_ml >= 0
        AND water_intake_ml <= 10000
    ),
    is_completed BOOLEAN NOT NULL DEFAULT FALSE,
    completed_at TIMESTAMP,
    notes CLOB,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

-- Índices para performance
CREATE INDEX idx_nutrition_plan_user ON nutrition_plans (user_id);

CREATE INDEX idx_nutrition_plan_date ON nutrition_plans (plan_date);

CREATE INDEX idx_nutrition_plan_user_date ON nutrition_plans (user_id, plan_date);

CREATE INDEX idx_nutrition_plan_completed ON nutrition_plans (user_id, is_completed);

CREATE INDEX idx_nutrition_plan_user_date_completed ON nutrition_plans (
    user_id,
    plan_date,
    is_completed
);

-- Constraint única para garantir um plano por usuário por data
CREATE UNIQUE INDEX uk_nutrition_plan_user_date ON nutrition_plans (user_id, plan_date);

-- Dados iniciais para os planos de assinatura (atualizar limites existentes)
UPDATE subscription_plans
SET
    nutrition_plans_limit = CASE
        WHEN name = 'Basic' THEN 10
        WHEN name = 'Premium' THEN 50
        WHEN name = 'Pro' THEN NULL -- Ilimitado
        ELSE nutrition_plans_limit
    END,
    description = CASE
        WHEN name = 'Basic' THEN 'Plano básico com funcionalidades essenciais. Inclui até 10 planos nutricionais.'
        WHEN name = 'Premium' THEN 'Plano premium com recursos avançados. Inclui até 50 planos nutricionais e relatórios detalhados.'
        WHEN name = 'Pro' THEN 'Plano profissional com todos os recursos. Planos nutricionais ilimitados e suporte prioritário.'
        ELSE description
    END
WHERE
    name IN ('Basic', 'Premium', 'Pro');