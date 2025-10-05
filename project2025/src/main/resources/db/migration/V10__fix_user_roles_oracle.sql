-- =====================================
-- V10: Associar roles aos usuários existentes
-- Garante que todo usuário tenha ao menos a role USER
-- =====================================

-- Associar role ADMIN ao usuário admin
BEGIN
    add_user_role('admin@nutrixpert.local', 'ADMIN');
END;
/

-- Associar role MANAGER ao usuário manager
BEGIN
    add_user_role('manager@nutrixpert.local', 'MANAGER');
END;
/

-- Associar role USER aos demais usuários
BEGIN
    add_user_role('alice@example.com', 'USER');
    add_user_role('bob@example.com', 'USER');
    add_user_role('carlos.silva@example.com', 'USER');
    add_user_role('maria.santos@example.com', 'USER');
    add_user_role('joao.oliveira@example.com', 'USER');
    add_user_role('julia.costa@example.com', 'USER');
    add_user_role('pedro.almeida@example.com', 'USER');
    add_user_role('ana.rodrigues@example.com', 'USER');
END;
/

-- Criar trigger para associar role USER automaticamente a novos usuários
CREATE OR REPLACE TRIGGER trg_user_default_role
AFTER INSERT ON users
FOR EACH ROW
DECLARE
    v_user_role_id NUMBER;
BEGIN
    -- Busca o ID da role USER
    SELECT id INTO v_user_role_id FROM roles WHERE name = 'USER';
    
    -- Associa a role USER ao novo usuário
    INSERT INTO user_roles(user_id, role_id) 
    VALUES (:NEW.id, v_user_role_id);
EXCEPTION
    WHEN DUP_VAL_ON_INDEX THEN
        NULL; -- Ignora se já existe
    WHEN NO_DATA_FOUND THEN
        NULL; -- Ignora se role USER não existe
END;
/

COMMIT;
