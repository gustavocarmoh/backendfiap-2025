-- V7__insert_nutrition_plans_data_h2.sql
-- Migração para inserir planos nutricionais aleatórios no H2 (testes)

-- Inserir dados de subscription primeiro (se ainda não existirem)
MERGE INTO subscription_plans (
    id,
    name,
    price,
    is_active,
    nutrition_plans_limit,
    description
)
VALUES (
        RANDOM_UUID (),
        'Basic Plan',
        9.99,
        TRUE,
        10,
        'Plano básico com até 10 planos nutricionais por mês'
    ),
    (
        RANDOM_UUID (),
        'Standard Plan',
        19.99,
        TRUE,
        20,
        'Plano intermediário com até 20 planos nutricionais por mês'
    ),
    (
        RANDOM_UUID (),
        'Premium Plan',
        29.99,
        TRUE,
        NULL,
        'Plano premium com planos nutricionais ilimitados'
    ),
    (
        RANDOM_UUID (),
        'Enterprise Plan',
        49.99,
        TRUE,
        NULL,
        'Plano empresarial com planos nutricionais ilimitados'
    );

-- Inserir subscriptions para usuários de teste
INSERT INTO
    subscriptions (
        id,
        user_id,
        plan_id,
        status,
        subscription_date,
        approved_date,
        amount
    )
SELECT
    RANDOM_UUID (),
    u.id,
    sp.id,
    'APPROVED',
    DATEADD ('DAY', -30, CURRENT_TIMESTAMP),
    DATEADD ('DAY', -29, CURRENT_TIMESTAMP),
    sp.price
FROM users u
    CROSS JOIN subscription_plans sp
WHERE
    u.email = 'carlos.silva@example.com'
    AND sp.name = 'Basic Plan'
    AND NOT EXISTS (
        SELECT 1
        FROM subscriptions s
        WHERE
            s.user_id = u.id
    );

INSERT INTO
    subscriptions (
        id,
        user_id,
        plan_id,
        status,
        subscription_date,
        approved_date,
        amount
    )
SELECT
    RANDOM_UUID (),
    u.id,
    sp.id,
    'APPROVED',
    DATEADD ('DAY', -20, CURRENT_TIMESTAMP),
    DATEADD ('DAY', -19, CURRENT_TIMESTAMP),
    sp.price
FROM users u
    CROSS JOIN subscription_plans sp
WHERE
    u.email = 'maria.santos@example.com'
    AND sp.name = 'Standard Plan'
    AND NOT EXISTS (
        SELECT 1
        FROM subscriptions s
        WHERE
            s.user_id = u.id
    );

INSERT INTO
    subscriptions (
        id,
        user_id,
        plan_id,
        status,
        subscription_date,
        approved_date,
        amount
    )
SELECT
    RANDOM_UUID (),
    u.id,
    sp.id,
    'APPROVED',
    DATEADD ('DAY', -15, CURRENT_TIMESTAMP),
    DATEADD ('DAY', -14, CURRENT_TIMESTAMP),
    sp.price
FROM users u
    CROSS JOIN subscription_plans sp
WHERE
    u.email = 'joao.oliveira@example.com'
    AND sp.name = 'Premium Plan'
    AND NOT EXISTS (
        SELECT 1
        FROM subscriptions s
        WHERE
            s.user_id = u.id
    );

-- Inserir alguns planos nutricionais de exemplo
-- Usuário FREE (Alice) - até 5 planos
INSERT INTO
    nutrition_plans (
        id,
        user_id,
        plan_date,
        title,
        description,
        breakfast,
        morning_snack,
        lunch,
        afternoon_snack,
        dinner,
        total_calories,
        total_proteins,
        total_carbohydrates,
        total_fats,
        water_intake_ml,
        is_completed,
        notes
    )
SELECT
    RANDOM_UUID (),
    u.id,
    DATEADD (
        'DAY',
        - seq.val,
        CURRENT_DATE
    ),
    'Plano Nutricional ' || FORMATDATETIME (
        DATEADD (
            'DAY',
            - seq.val,
            CURRENT_DATE
        ),
        'dd/MM/yyyy'
    ),
    'Plano balanceado focado em saúde e bem-estar',
    'Aveia com frutas vermelhas e castanhas (300 kcal)',
    'Mix de castanhas (150 kcal)',
    'Frango grelhado com arroz integral e salada verde (420 kcal)',
    'Maçã com pasta de amendoim (180 kcal)',
    'Sopa de legumes com peito de frango desfiado (280 kcal)',
    1400 + (RAND () * 200),
    85.0 + (RAND () * 20),
    175.0 + (RAND () * 50),
    45.0 + (RAND () * 15),
    2500 + (RAND () * 500),
    CASE
        WHEN RAND () > 0.3 THEN TRUE
        ELSE FALSE
    END,
    CASE
        WHEN RAND () > 0.8 THEN 'Plano seguido conforme orientação'
        ELSE NULL
    END
FROM users u
    CROSS JOIN (
        SELECT 1 as val
        UNION
        SELECT 5
        UNION
        SELECT 10
        UNION
        SELECT 15
        UNION
        SELECT 20
    ) seq
WHERE
    u.email = 'alice@example.com';

-- Usuário Basic (Carlos) - até 10 planos
INSERT INTO
    nutrition_plans (
        id,
        user_id,
        plan_date,
        title,
        description,
        breakfast,
        morning_snack,
        lunch,
        afternoon_snack,
        dinner,
        total_calories,
        total_proteins,
        total_carbohydrates,
        total_fats,
        water_intake_ml,
        is_completed,
        notes
    )
SELECT
    RANDOM_UUID (),
    u.id,
    DATEADD (
        'DAY',
        - seq.val,
        CURRENT_DATE
    ),
    'Plano Nutricional ' || FORMATDATETIME (
        DATEADD (
            'DAY',
            - seq.val,
            CURRENT_DATE
        ),
        'dd/MM/yyyy'
    ),
    'Plano balanceado focado em saúde e bem-estar',
    CASE
        WHEN MOD(seq.val, 3) = 0 THEN 'Omelete de claras com espinafre e queijo cottage (280 kcal)'
        WHEN MOD(seq.val, 3) = 1 THEN 'Pão integral com abacate e ovo pochê (350 kcal)'
        ELSE 'Smoothie de banana com whey protein e aveia (320 kcal)'
    END,
    'Iogurte natural com frutas (120 kcal)',
    CASE
        WHEN MOD(seq.val, 3) = 0 THEN 'Salmão grelhado com quinoa e legumes refogados (450 kcal)'
        WHEN MOD(seq.val, 3) = 1 THEN 'Peixe assado com batata doce e brócolis (400 kcal)'
        ELSE 'Carne magra com arroz, feijão e verduras (480 kcal)'
    END,
    'Queijo cottage com tomate cereja (140 kcal)',
    CASE
        WHEN MOD(seq.val, 3) = 0 THEN 'Salada completa com grão de bico e azeite (320 kcal)'
        WHEN MOD(seq.val, 3) = 1 THEN 'Peixe grelhado com purê de couve-flor (290 kcal)'
        ELSE 'Frango desfiado com legumes refogados (300 kcal)'
    END,
    1600 + (RAND () * 300),
    95.0 + (RAND () * 25),
    190.0 + (RAND () * 60),
    50.0 + (RAND () * 20),
    2800 + (RAND () * 700),
    CASE
        WHEN RAND () > 0.3 THEN TRUE
        ELSE FALSE
    END,
    CASE
        WHEN RAND () > 0.8 THEN 'Ajustes necessários para próxima semana'
        ELSE NULL
    END
FROM users u
    CROSS JOIN (
        SELECT 1 as val
        UNION
        SELECT 3
        UNION
        SELECT 7
        UNION
        SELECT 12
        UNION
        SELECT 18
        UNION
        SELECT 25
        UNION
        SELECT 30
        UNION
        SELECT 35
        UNION
        SELECT 42
        UNION
        SELECT 50
    ) seq
WHERE
    u.email = 'carlos.silva@example.com';

-- Usuário Standard (Maria) - até 20 planos
INSERT INTO
    nutrition_plans (
        id,
        user_id,
        plan_date,
        title,
        description,
        breakfast,
        lunch,
        dinner,
        total_calories,
        total_proteins,
        total_carbohydrates,
        total_fats,
        water_intake_ml,
        is_completed
    )
SELECT
    RANDOM_UUID (),
    u.id,
    DATEADD (
        'DAY',
        - (seq.val * 2),
        CURRENT_DATE
    ),
    'Plano Nutricional ' || FORMATDATETIME (
        DATEADD (
            'DAY',
            - (seq.val * 2),
            CURRENT_DATE
        ),
        'dd/MM/yyyy'
    ),
    'Plano balanceado focado em saúde e bem-estar',
    'Tapioca com peito de peru e queijo branco (290 kcal)',
    'Peito de frango com macarrão integral e molho de tomate (440 kcal)',
    'Omelete de legumes com salada verde (250 kcal)',
    1800 + (RAND () * 400),
    105.0 + (RAND () * 30),
    210.0 + (RAND () * 70),
    55.0 + (RAND () * 25),
    3000 + (RAND () * 500),
    CASE
        WHEN RAND () > 0.2 THEN TRUE
        ELSE FALSE
    END
FROM users u
    CROSS JOIN (
        SELECT LEVEL as val
        FROM DUAL CONNECT BY LEVEL <= 20
    ) seq
WHERE
    u.email = 'maria.santos@example.com';

-- Usuário Premium (João) - planos ilimitados (simular 30 planos)
INSERT INTO
    nutrition_plans (
        id,
        user_id,
        plan_date,
        title,
        description,
        breakfast,
        lunch,
        dinner,
        total_calories,
        total_proteins,
        total_carbohydrates,
        total_fats,
        water_intake_ml,
        is_completed
    )
SELECT
    RANDOM_UUID (),
    u.id,
    DATEADD (
        'DAY',
        - seq.val,
        CURRENT_DATE
    ),
    'Plano Premium ' || FORMATDATETIME (
        DATEADD (
            'DAY',
            - seq.val,
            CURRENT_DATE
        ),
        'dd/MM/yyyy'
    ),
    'Plano premium personalizado com acompanhamento nutricional',
    'Panqueca de banana com farinha de aveia (330 kcal)',
    'Hambúrguer de peru com batata assada e salada (460 kcal)',
    'Wrap integral com frango e salada (310 kcal)',
    2000 + (RAND () * 400),
    120.0 + (RAND () * 35),
    230.0 + (RAND () * 80),
    65.0 + (RAND () * 30),
    3200 + (RAND () * 800),
    CASE
        WHEN RAND () > 0.1 THEN TRUE
        ELSE FALSE
    END
FROM users u
    CROSS JOIN (
        SELECT LEVEL as val
        FROM DUAL CONNECT BY LEVEL <= 30
    ) seq
WHERE
    u.email = 'joao.oliveira@example.com';

-- Inserir supermercados de São Paulo
INSERT INTO
    service_providers (
        id,
        name,
        latitude,
        longitude,
        description,
        endereco,
        active,
        created_by
    )
SELECT
    RANDOM_UUID (),
    nome,
    lat,
    lng,
    descricao,
    endereco,
    TRUE,
    (
        SELECT id
        FROM users
        WHERE
            email = 'admin@nutrixpert.local'
    )
FROM (
        SELECT
            'Pão de Açúcar - Paulista' as nome, -23.5618 as lat, -46.6563 as lng, 'Supermercado premium na Av. Paulista' as descricao, 'Av. Paulista, 2064 - Bela Vista, São Paulo - SP' as endereco
        UNION ALL
        SELECT 'Extra Hiper - Consolação', -23.5548, -46.6526, 'Hipermercado completo com produtos naturais', 'Rua da Consolação, 2787 - Consolação, São Paulo - SP'
        UNION ALL
        SELECT 'Carrefour Express - Augusta', -23.5567, -46.6584, 'Supermercado compacto e conveniente', 'Rua Augusta, 1508 - Consolação, São Paulo - SP'
        UNION ALL
        SELECT 'Pão de Açúcar - Faria Lima', -23.5729, -46.6928, 'Supermercado premium no coração financeiro', 'Av. Brigadeiro Faria Lima, 1821 - Jardim Paulistano, São Paulo - SP'
        UNION ALL
        SELECT 'Extra - Vila Olímpia', -23.5952, -46.6897, 'Supermercado estratégico para empresários', 'Rua Surubim, 577 - Vila Olímpia, São Paulo - SP'
    ) t;