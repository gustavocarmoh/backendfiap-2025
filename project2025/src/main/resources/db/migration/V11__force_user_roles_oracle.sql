-- =====================================
-- V11: Forçar associação de roles aos usuários existentes
-- Versão mais robusta usando SQL direto
-- =====================================

-- Limpar roles existentes (caso haja algum problema)
DELETE FROM user_roles;

-- Associar role ADMIN ao usuário admin
DECLARE
    v_user_id VARCHAR2(36);
    v_role_id NUMBER;
BEGIN
    -- Buscar ID do usuário admin
    SELECT id INTO v_user_id 
    FROM users 
    WHERE LOWER(email) = LOWER('admin@nutrixpert.local');
    
    -- Buscar ID da role ADMIN
    SELECT id INTO v_role_id 
    FROM roles 
    WHERE name = 'ADMIN';
    
    -- Inserir associação
    INSERT INTO user_roles (user_id, role_id) 
    VALUES (v_user_id, v_role_id);
    
    DBMS_OUTPUT.PUT_LINE('Role ADMIN associada ao usuário admin@nutrixpert.local');
EXCEPTION
    WHEN NO_DATA_FOUND THEN
        DBMS_OUTPUT.PUT_LINE('ERRO: Usuário admin ou role ADMIN não encontrados');
    WHEN DUP_VAL_ON_INDEX THEN
        DBMS_OUTPUT.PUT_LINE('Role ADMIN já estava associada ao admin');
END;
/

-- Associar role MANAGER ao usuário manager
DECLARE
    v_user_id VARCHAR2(36);
    v_role_id NUMBER;
BEGIN
    SELECT id INTO v_user_id 
    FROM users 
    WHERE LOWER(email) = LOWER('manager@nutrixpert.local');
    
    SELECT id INTO v_role_id 
    FROM roles 
    WHERE name = 'MANAGER';
    
    INSERT INTO user_roles (user_id, role_id) 
    VALUES (v_user_id, v_role_id);
    
    DBMS_OUTPUT.PUT_LINE('Role MANAGER associada ao usuário manager@nutrixpert.local');
EXCEPTION
    WHEN NO_DATA_FOUND THEN
        DBMS_OUTPUT.PUT_LINE('ERRO: Usuário manager ou role MANAGER não encontrados');
    WHEN DUP_VAL_ON_INDEX THEN
        DBMS_OUTPUT.PUT_LINE('Role MANAGER já estava associada ao manager');
END;
/

-- Associar role USER a TODOS os outros usuários
DECLARE
    v_role_id NUMBER;
    v_count NUMBER := 0;
BEGIN
    -- Buscar ID da role USER
    SELECT id INTO v_role_id 
    FROM roles 
    WHERE name = 'USER';
    
    -- Associar role USER a todos os usuários que ainda não têm esta role
    FOR user_rec IN (
        SELECT DISTINCT u.id 
        FROM users u
        WHERE NOT EXISTS (
            SELECT 1 
            FROM user_roles ur 
            WHERE ur.user_id = u.id 
            AND ur.role_id = v_role_id
        )
    ) LOOP
        BEGIN
            INSERT INTO user_roles (user_id, role_id) 
            VALUES (user_rec.id, v_role_id);
            v_count := v_count + 1;
        EXCEPTION
            WHEN DUP_VAL_ON_INDEX THEN
                NULL;
        END;
    END LOOP;
    
    DBMS_OUTPUT.PUT_LINE('Role USER associada a ' || v_count || ' usuários');
EXCEPTION
    WHEN NO_DATA_FOUND THEN
        DBMS_OUTPUT.PUT_LINE('ERRO: Role USER não encontrada');
END;
/

-- Verificar resultado
SELECT 
    u.email,
    r.name as role_name,
    'OK' as status
FROM users u
JOIN user_roles ur ON u.id = ur.user_id
JOIN roles r ON r.id = ur.role_id
ORDER BY u.email, r.name;

COMMIT;
