-- V2__subscription_system_h2.sql
-- Migration for H2 database (test environment)

-- Create subscription_plans table
CREATE TABLE subscription_plans (
    id UUID PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    price DECIMAL(10, 2) NOT NULL,
    duration_days INTEGER NOT NULL,
    features JSON,
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create subscriptions table
CREATE TABLE subscriptions (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    plan_id UUID NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    start_date TIMESTAMP,
    end_date TIMESTAMP,
    auto_renew BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    FOREIGN KEY (plan_id) REFERENCES subscription_plans (id) ON DELETE RESTRICT
);

-- Create indexes for better performance
CREATE INDEX idx_subscriptions_user_id ON subscriptions (user_id);

CREATE INDEX idx_subscriptions_plan_id ON subscriptions (plan_id);

CREATE INDEX idx_subscriptions_status ON subscriptions (status);

CREATE INDEX idx_subscription_plans_active ON subscription_plans (is_active);

-- Insert default subscription plans
INSERT INTO
    subscription_plans (
        id,
        name,
        price,
        duration_days,
        features,
        is_active
    )
VALUES (
        'a1b2c3d4-e5f6-7890-abcd-ef1234567890',
        'BASIC',
        29.90,
        30,
        '[
    "Até 50 análises nutricionais por mês",
    "Relatórios básicos",
    "Suporte por email"
]',
        true
    ),
    (
        'b2c3d4e5-f6g7-8901-bcde-f23456789012',
        'PREMIUM',
        59.90,
        30,
        '[
    "Análises nutricionais ilimitadas",
    "Relatórios avançados",
    "Suporte prioritário",
    "API access básico"
]',
        true
    ),
    (
        'c3d4e5f6-g7h8-9012-cdef-345678901234',
        'ENTERPRISE',
        149.90,
        30,
        '[
    "Análises nutricionais ilimitadas",
    "Relatórios customizados",
    "Suporte 24/7",
    "API access completo",
    "Integração com sistemas externos",
    "Dashboard administrativo"
]',
        true
    );