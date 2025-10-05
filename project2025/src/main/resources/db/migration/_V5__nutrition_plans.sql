-- V5: Criação do sistema de planos nutricionais

-- Atualizar tabela de planos de assinatura com limites de planos nutricionais
ALTER TABLE subscription_plans
ADD COLUMN nutrition_plans_limit INTEGER,
ADD COLUMN description TEXT;

-- Comentários para os novos campos
COMMENT ON COLUMN subscription_plans.nutrition_plans_limit IS 'Limite de planos nutricionais (NULL = ilimitado)';

COMMENT ON COLUMN subscription_plans.description IS 'Descrição detalhada do plano de assinatura';

-- Criar tabela de planos nutricionais
CREATE TABLE nutrition_plans (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid (),
    user_id UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    plan_date DATE NOT NULL,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    breakfast TEXT,
    morning_snack TEXT,
    lunch TEXT,
    afternoon_snack TEXT,
    dinner TEXT,
    evening_snack TEXT,
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
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
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

-- Comentários para documentação
COMMENT ON
TABLE nutrition_plans IS 'Planos nutricionais diários dos usuários';

COMMENT ON COLUMN nutrition_plans.user_id IS 'Referência ao usuário proprietário do plano';

COMMENT ON COLUMN nutrition_plans.plan_date IS 'Data do plano nutricional';

COMMENT ON COLUMN nutrition_plans.title IS 'Título do plano nutricional';

COMMENT ON COLUMN nutrition_plans.description IS 'Descrição geral do plano';

COMMENT ON COLUMN nutrition_plans.breakfast IS 'Descrição do café da manhã';

COMMENT ON COLUMN nutrition_plans.morning_snack IS 'Descrição do lanche da manhã';

COMMENT ON COLUMN nutrition_plans.lunch IS 'Descrição do almoço';

COMMENT ON COLUMN nutrition_plans.afternoon_snack IS 'Descrição do lanche da tarde';

COMMENT ON COLUMN nutrition_plans.dinner IS 'Descrição do jantar';

COMMENT ON COLUMN nutrition_plans.evening_snack IS 'Descrição da ceia';

COMMENT ON COLUMN nutrition_plans.total_calories IS 'Total de calorias planejadas';

COMMENT ON COLUMN nutrition_plans.total_proteins IS 'Total de proteínas em gramas';

COMMENT ON COLUMN nutrition_plans.total_carbohydrates IS 'Total de carboidratos em gramas';

COMMENT ON COLUMN nutrition_plans.total_fats IS 'Total de gorduras em gramas';

COMMENT ON COLUMN nutrition_plans.water_intake_ml IS 'Meta de ingestão de água em mililitros';

COMMENT ON COLUMN nutrition_plans.is_completed IS 'Indica se o plano foi cumprido';

COMMENT ON COLUMN nutrition_plans.completed_at IS 'Data e hora quando o plano foi marcado como completo';

COMMENT ON COLUMN nutrition_plans.notes IS 'Observações adicionais sobre o plano';

-- Trigger para atualizar updated_at automaticamente
CREATE OR REPLACE FUNCTION update_nutrition_plans_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_update_nutrition_plans_updated_at
    BEFORE UPDATE ON nutrition_plans
    FOR EACH ROW
    EXECUTE FUNCTION update_nutrition_plans_updated_at();

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