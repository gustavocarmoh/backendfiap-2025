-- Migration for subscription system
-- Add subscription plans table
CREATE TABLE IF NOT EXISTS subscription_plans (
    id UUID DEFAULT gen_random_uuid () PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    price DECIMAL(10, 2) NOT NULL CHECK (price > 0),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_active BOOLEAN DEFAULT TRUE
);

-- Add subscriptions table
CREATE TABLE IF NOT EXISTS subscriptions (
    id UUID DEFAULT gen_random_uuid () PRIMARY KEY,
    user_id UUID NOT NULL,
    plan_id UUID NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    approved_by_user_id UUID,
    subscription_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    approved_date TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    amount DECIMAL(10, 2) NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    FOREIGN KEY (plan_id) REFERENCES subscription_plans (id) ON DELETE RESTRICT,
    FOREIGN KEY (approved_by_user_id) REFERENCES users (id) ON DELETE SET NULL,
    CHECK (
        status IN (
            'PENDING',
            'APPROVED',
            'REJECTED',
            'CANCELLED'
        )
    )
);

-- Add indexes for better performance
CREATE INDEX IF NOT EXISTS idx_subscriptions_user_id ON subscriptions (user_id);

CREATE INDEX IF NOT EXISTS idx_subscriptions_plan_id ON subscriptions (plan_id);

CREATE INDEX IF NOT EXISTS idx_subscriptions_status ON subscriptions (status);

CREATE INDEX IF NOT EXISTS idx_subscriptions_created_at ON subscriptions (created_at);

CREATE INDEX IF NOT EXISTS idx_subscription_plans_is_active ON subscription_plans (is_active);

-- Insert some default subscription plans
INSERT INTO
    subscription_plans (name, price, is_active)
VALUES ('Basic Plan', 9.99, TRUE),
    ('Standard Plan', 19.99, TRUE),
    ('Premium Plan', 29.99, TRUE),
    (
        'Enterprise Plan',
        49.99,
        TRUE
    )
ON CONFLICT (name) DO NOTHING;

-- Update trigger for subscription_plans
CREATE OR REPLACE FUNCTION update_subscription_plans_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_subscription_plans_updated_at
    BEFORE UPDATE ON subscription_plans
    FOR EACH ROW
    EXECUTE FUNCTION update_subscription_plans_updated_at();

-- Update trigger for subscriptions
CREATE OR REPLACE FUNCTION update_subscriptions_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_subscriptions_updated_at
    BEFORE UPDATE ON subscriptions
    FOR EACH ROW
    EXECUTE FUNCTION update_subscriptions_updated_at();