-- Trigger de auditoria: registra alterações em nutrition_plans

CREATE TABLE nutrition_plans_audit (
    audit_id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    plan_id NUMBER,
    user_id VARCHAR2(100),
    action VARCHAR2(20),
    changed_at DATE DEFAULT SYSDATE,
    old_calorias NUMBER,
    new_calorias NUMBER
);

CREATE OR REPLACE TRIGGER trg_audit_nutrition_plans
AFTER UPDATE ON nutrition_plans
FOR EACH ROW
BEGIN
    INSERT INTO nutrition_plans_audit (
        plan_id, user_id, action, old_calorias, new_calorias
    ) VALUES (
        :OLD.id, :OLD.user_id, 'UPDATE', :OLD.calorias, :NEW.calorias
    );
END;
/

-- Documentação:
-- Trigger trg_audit_nutrition_plans registra toda alteração de calorias nos planos nutricionais,
-- permitindo rastreabilidade e auditoria para conformidade e análise histórica.
-- Tabela nutrition_plans_audit armazena os registros de auditoria.
