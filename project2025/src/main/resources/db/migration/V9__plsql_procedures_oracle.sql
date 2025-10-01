-- =====================================
-- V9: Procedures PL/SQL para Oracle
-- =====================================
-- Implementação de procedures com lógica de negócio complexa
-- Utilizando cursores, loops, condicionais e tratamento de exceções
-- =====================================

-- =====================================
-- PROCEDURE 1: Registrar Alerta Nutricional
-- =====================================
-- Analisa os planos nutricionais de um usuário e registra alertas
-- quando detecta situações críticas ou que necessitam atenção
-- Utiliza: CURSOR, LOOP, IF, EXCEPTION
-- =====================================

CREATE OR REPLACE PROCEDURE proc_registrar_alerta_nutricional(
    p_user_id IN VARCHAR2,
    p_dias_analise IN NUMBER DEFAULT 7,
    p_alertas_gerados OUT NUMBER
) IS
    -- Variáveis de controle
    v_alert_count NUMBER := 0;
    v_plano_id VARCHAR2(36);
    v_plan_date DATE;
    v_calorias NUMBER;
    v_proteinas NUMBER;
    v_carboidratos NUMBER;
    v_gorduras NUMBER;
    v_agua NUMBER;
    v_is_completed NUMBER;
    
    -- Constantes para limites críticos
    c_calorias_min CONSTANT NUMBER := 1200;
    c_calorias_max CONSTANT NUMBER := 3500;
    c_proteinas_min CONSTANT NUMBER := 40;
    c_agua_min CONSTANT NUMBER := 1500;
    c_dias_sem_plano_max CONSTANT NUMBER := 3;
    
    -- Variáveis de análise
    v_dias_consecutivos_sem_completar NUMBER := 0;
    v_data_ultimo_plano DATE;
    v_dias_sem_plano NUMBER;
    
    -- Cursor para análise dos planos
    CURSOR c_planos IS
        SELECT 
            id, plan_date, total_calories, total_proteins,
            total_carbohydrates, total_fats, water_intake_ml, is_completed
        FROM nutrition_plans
        WHERE user_id = p_user_id
        AND plan_date >= TRUNC(SYSDATE) - p_dias_analise
        AND plan_date <= TRUNC(SYSDATE)
        ORDER BY plan_date DESC;
    
    -- Cursor para verificar planos não completados consecutivos
    CURSOR c_nao_completados IS
        SELECT COUNT(*) as total
        FROM nutrition_plans
        WHERE user_id = p_user_id
        AND plan_date >= TRUNC(SYSDATE) - 7
        AND is_completed = 0;
    
    -- Exceções customizadas
    e_usuario_invalido EXCEPTION;
    e_dias_invalidos EXCEPTION;
    
    -- Procedure interna para registrar alerta
    PROCEDURE registrar_alerta(
        p_tipo VARCHAR2,
        p_mensagem CLOB,
        p_severidade VARCHAR2,
        p_plano_id VARCHAR2 DEFAULT NULL
    ) IS
    BEGIN
        INSERT INTO nutrition_alerts (
            user_id, nutrition_plan_id, alert_type, 
            alert_message, severity, is_read
        ) VALUES (
            p_user_id, p_plano_id, p_tipo, 
            p_mensagem, p_severidade, 0
        );
        v_alert_count := v_alert_count + 1;
    EXCEPTION
        WHEN OTHERS THEN
            -- Log do erro mas continua processamento
            DBMS_OUTPUT.PUT_LINE('Erro ao registrar alerta: ' || SQLERRM);
    END registrar_alerta;
    
BEGIN
    -- Validações de entrada
    IF p_user_id IS NULL THEN
        RAISE e_usuario_invalido;
    END IF;
    
    IF p_dias_analise <= 0 OR p_dias_analise > 365 THEN
        RAISE e_dias_invalidos;
    END IF;
    
    -- Inicializar contador
    v_alert_count := 0;
    
    -- Verificar se usuário existe
    DECLARE
        v_user_exists NUMBER;
    BEGIN
        SELECT COUNT(*) INTO v_user_exists
        FROM users
        WHERE id = p_user_id;
        
        IF v_user_exists = 0 THEN
            RAISE e_usuario_invalido;
        END IF;
    END;
    
    -- Verificar data do último plano
    BEGIN
        SELECT MAX(plan_date) 
        INTO v_data_ultimo_plano
        FROM nutrition_plans
        WHERE user_id = p_user_id;
        
        IF v_data_ultimo_plano IS NOT NULL THEN
            v_dias_sem_plano := TRUNC(SYSDATE) - v_data_ultimo_plano;
            
            -- Alerta: muitos dias sem criar plano
            IF v_dias_sem_plano > c_dias_sem_plano_max THEN
                registrar_alerta(
                    'INATIVIDADE',
                    'Você está há ' || v_dias_sem_plano || ' dias sem criar um plano nutricional. ' ||
                    'Manter a regularidade é importante para alcançar seus objetivos de saúde!',
                    'WARNING',
                    NULL
                );
            END IF;
        END IF;
    EXCEPTION
        WHEN NO_DATA_FOUND THEN
            -- Usuário nunca criou um plano
            registrar_alerta(
                'PRIMEIRO_PLANO',
                'Bem-vindo! Crie seu primeiro plano nutricional para começar a monitorar sua saúde.',
                'INFO',
                NULL
            );
    END;
    
    -- Analisar cada plano nutricional usando cursor
    OPEN c_planos;
    LOOP
        FETCH c_planos INTO 
            v_plano_id, v_plan_date, v_calorias, v_proteinas,
            v_carboidratos, v_gorduras, v_agua, v_is_completed;
        
        EXIT WHEN c_planos%NOTFOUND;
        
        -- Verificar apenas planos completados
        IF v_is_completed = 1 THEN
            
            -- Alerta: Calorias muito baixas (risco de desnutrição)
            IF v_calorias IS NOT NULL AND v_calorias < c_calorias_min THEN
                registrar_alerta(
                    'CALORIAS_BAIXAS',
                    'ATENÇÃO: Consumo calórico muito baixo no dia ' || 
                    TO_CHAR(v_plan_date, 'DD/MM/YYYY') || ' (' || v_calorias || ' kcal). ' ||
                    'Recomendação mínima: ' || c_calorias_min || ' kcal. ' ||
                    'Consulte um nutricionista.',
                    'CRITICAL',
                    v_plano_id
                );
            END IF;
            
            -- Alerta: Calorias muito altas
            IF v_calorias IS NOT NULL AND v_calorias > c_calorias_max THEN
                registrar_alerta(
                    'CALORIAS_ALTAS',
                    'Aviso: Consumo calórico elevado no dia ' || 
                    TO_CHAR(v_plan_date, 'DD/MM/YYYY') || ' (' || v_calorias || ' kcal). ' ||
                    'Verifique se está dentro de seus objetivos.',
                    'WARNING',
                    v_plano_id
                );
            END IF;
            
            -- Alerta: Proteínas insuficientes
            IF v_proteinas IS NOT NULL AND v_proteinas < c_proteinas_min THEN
                registrar_alerta(
                    'PROTEINAS_BAIXAS',
                    'Consumo de proteínas abaixo do recomendado no dia ' || 
                    TO_CHAR(v_plan_date, 'DD/MM/YYYY') || ' (' || 
                    ROUND(v_proteinas, 1) || ' g). ' ||
                    'Recomendação mínima: ' || c_proteinas_min || ' g.',
                    'WARNING',
                    v_plano_id
                );
            END IF;
            
            -- Alerta: Hidratação insuficiente
            IF v_agua IS NOT NULL AND v_agua < c_agua_min THEN
                registrar_alerta(
                    'HIDRATACAO_BAIXA',
                    'Hidratação insuficiente no dia ' || 
                    TO_CHAR(v_plan_date, 'DD/MM/YYYY') || ' (' || v_agua || ' ml). ' ||
                    'Recomendação: mínimo ' || c_agua_min || ' ml de água por dia.',
                    'WARNING',
                    v_plano_id
                );
            END IF;
            
            -- Alerta: Desequilíbrio de macronutrientes
            IF v_proteinas IS NOT NULL AND v_carboidratos IS NOT NULL AND v_gorduras IS NOT NULL THEN
                DECLARE
                    v_cal_proteinas NUMBER := v_proteinas * 4;
                    v_cal_carboidratos NUMBER := v_carboidratos * 4;
                    v_cal_gorduras NUMBER := v_gorduras * 9;
                    v_total_cal_macro NUMBER;
                    v_perc_gorduras NUMBER;
                BEGIN
                    v_total_cal_macro := v_cal_proteinas + v_cal_carboidratos + v_cal_gorduras;
                    
                    IF v_total_cal_macro > 0 THEN
                        v_perc_gorduras := (v_cal_gorduras / v_total_cal_macro) * 100;
                        
                        -- Alerta: Muita gordura (mais de 40% das calorias)
                        IF v_perc_gorduras > 40 THEN
                            registrar_alerta(
                                'GORDURAS_ALTAS',
                                'Alto percentual de gorduras no dia ' || 
                                TO_CHAR(v_plan_date, 'DD/MM/YYYY') || ' (' || 
                                ROUND(v_perc_gorduras, 1) || '%). ' ||
                                'Recomendação: 20-35% das calorias totais.',
                                'WARNING',
                                v_plano_id
                            );
                        END IF;
                    END IF;
                END;
            END IF;
            
        END IF; -- Fim verificação is_completed
        
    END LOOP;
    CLOSE c_planos;
    
    -- Verificar planos não completados consecutivos
    FOR rec IN c_nao_completados LOOP
        IF rec.total >= 5 THEN
            registrar_alerta(
                'BAIXA_ADESAO',
                'Você tem ' || rec.total || ' planos não completados nos últimos 7 dias. ' ||
                'A consistência é fundamental para alcançar resultados. ' ||
                'Tente completar seus planos diariamente!',
                'WARNING',
                NULL
            );
        END IF;
    END LOOP;
    
    -- Calcular score de saúde e alertar se muito baixo
    DECLARE
        v_score_saude NUMBER;
    BEGIN
        v_score_saude := calcular_indicador_saude_usuario(p_user_id, p_dias_analise);
        
        IF v_score_saude < 50 THEN
            registrar_alerta(
                'SCORE_BAIXO',
                'Seu indicador de saúde está em ' || ROUND(v_score_saude, 0) || '/100 (PRECISA MELHORAR). ' ||
                'Recomendamos revisar seus hábitos alimentares e consultar um profissional de nutrição.',
                'CRITICAL',
                NULL
            );
        ELSIF v_score_saude >= 85 THEN
            registrar_alerta(
                'SCORE_EXCELENTE',
                'Parabéns! Seu indicador de saúde está em ' || ROUND(v_score_saude, 0) || '/100 (EXCELENTE). ' ||
                'Continue com seus bons hábitos alimentares!',
                'INFO',
                NULL
            );
        END IF;
    END;
    
    -- Commit das alterações
    COMMIT;
    
    -- Retornar número de alertas gerados
    p_alertas_gerados := v_alert_count;
    
    -- Log de sucesso
    DBMS_OUTPUT.PUT_LINE('Procedure executada com sucesso. Alertas gerados: ' || v_alert_count);
    
EXCEPTION
    WHEN e_usuario_invalido THEN
        ROLLBACK;
        RAISE_APPLICATION_ERROR(-20010, 'ID de usuário inválido ou não encontrado');
    WHEN e_dias_invalidos THEN
        ROLLBACK;
        RAISE_APPLICATION_ERROR(-20011, 'Período de análise deve estar entre 1 e 365 dias');
    WHEN OTHERS THEN
        ROLLBACK;
        RAISE_APPLICATION_ERROR(-20012, 'Erro ao processar alertas: ' || SQLERRM);
END proc_registrar_alerta_nutricional;
/

-- =====================================
-- PROCEDURE 2: Gerar Relatório de Consumo Nutricional
-- =====================================
-- Gera um relatório detalhado consolidando estatísticas nutricionais
-- por usuário, incluindo comparações e recomendações
-- Utiliza: CURSOR, LOOP, IF, EXCEPTION, variáveis compostas
-- =====================================

CREATE OR REPLACE PROCEDURE proc_gerar_relatorio_consumo(
    p_user_id IN VARCHAR2,
    p_data_inicio IN DATE DEFAULT TRUNC(SYSDATE) - 30,
    p_data_fim IN DATE DEFAULT TRUNC(SYSDATE),
    p_relatorio OUT CLOB,
    p_sucesso OUT NUMBER
) IS
    -- Variáveis de usuário
    v_user_name VARCHAR2(300);
    v_user_email VARCHAR2(255);
    
    -- Estatísticas gerais
    v_total_planos NUMBER := 0;
    v_planos_completados NUMBER := 0;
    v_taxa_completude NUMBER := 0;
    
    -- Estatísticas por semana
    TYPE t_semana_stats IS RECORD (
        semana NUMBER,
        ano NUMBER,
        total_calorias NUMBER,
        media_calorias NUMBER,
        planos_count NUMBER
    );
    TYPE t_semanas IS TABLE OF t_semana_stats;
    v_semanas t_semanas;
    
    -- Cursor para análise semanal
    CURSOR c_analise_semanal IS
        SELECT 
            TO_CHAR(plan_date, 'IW') as semana,
            TO_CHAR(plan_date, 'IYYY') as ano,
            SUM(total_calories) as total_calorias,
            AVG(total_calories) as media_calorias,
            COUNT(*) as planos_count
        FROM nutrition_plans
        WHERE user_id = p_user_id
        AND plan_date BETWEEN p_data_inicio AND p_data_fim
        AND is_completed = 1
        GROUP BY TO_CHAR(plan_date, 'IW'), TO_CHAR(plan_date, 'IYYY')
        ORDER BY ano, semana;
    
    -- Cursor para top 5 dias com mais calorias
    CURSOR c_top_calorias IS
        SELECT plan_date, title, total_calories
        FROM nutrition_plans
        WHERE user_id = p_user_id
        AND plan_date BETWEEN p_data_inicio AND p_data_fim
        AND is_completed = 1
        AND total_calories IS NOT NULL
        ORDER BY total_calories DESC
        FETCH FIRST 5 ROWS ONLY;
    
    -- Cursor para dias com menos calorias
    CURSOR c_low_calorias IS
        SELECT plan_date, title, total_calories
        FROM nutrition_plans
        WHERE user_id = p_user_id
        AND plan_date BETWEEN p_data_inicio AND p_data_fim
        AND is_completed = 1
        AND total_calories IS NOT NULL
        ORDER BY total_calories ASC
        FETCH FIRST 5 ROWS ONLY;
    
    -- Variáveis nutricionais
    v_media_calorias NUMBER;
    v_media_proteinas NUMBER;
    v_media_carboidratos NUMBER;
    v_media_gorduras NUMBER;
    v_media_agua NUMBER;
    v_score_saude NUMBER;
    
    -- Exceções
    e_usuario_invalido EXCEPTION;
    e_datas_invalidas EXCEPTION;
    e_periodo_muito_longo EXCEPTION;
    
BEGIN
    -- Validações
    IF p_user_id IS NULL THEN
        RAISE e_usuario_invalido;
    END IF;
    
    IF p_data_inicio > p_data_fim THEN
        RAISE e_datas_invalidas;
    END IF;
    
    IF (p_data_fim - p_data_inicio) > 365 THEN
        RAISE e_periodo_muito_longo;
    END IF;
    
    -- Inicializar CLOB
    DBMS_LOB.CREATETEMPORARY(p_relatorio, TRUE);
    
    -- Buscar dados do usuário
    BEGIN
        SELECT first_name || ' ' || last_name, email
        INTO v_user_name, v_user_email
        FROM users
        WHERE id = p_user_id;
    EXCEPTION
        WHEN NO_DATA_FOUND THEN
            RAISE e_usuario_invalido;
    END;
    
    -- Cabeçalho
    DBMS_LOB.APPEND(p_relatorio, '╔════════════════════════════════════════════════════════════╗' || CHR(10));
    DBMS_LOB.APPEND(p_relatorio, '║     RELATÓRIO DETALHADO DE CONSUMO NUTRICIONAL           ║' || CHR(10));
    DBMS_LOB.APPEND(p_relatorio, '║            NutriXpert Analysis System                     ║' || CHR(10));
    DBMS_LOB.APPEND(p_relatorio, '╚════════════════════════════════════════════════════════════╝' || CHR(10));
    DBMS_LOB.APPEND(p_relatorio, CHR(10));
    
    -- Informações do usuário
    DBMS_LOB.APPEND(p_relatorio, '┌─ DADOS DO USUÁRIO ─────────────────────────────────────┐' || CHR(10));
    DBMS_LOB.APPEND(p_relatorio, '│ Nome: ' || RPAD(v_user_name, 50) || '│' || CHR(10));
    DBMS_LOB.APPEND(p_relatorio, '│ Email: ' || RPAD(v_user_email, 49) || '│' || CHR(10));
    DBMS_LOB.APPEND(p_relatorio, '│ Período: ' || TO_CHAR(p_data_inicio, 'DD/MM/YYYY') || 
                                  ' a ' || TO_CHAR(p_data_fim, 'DD/MM/YYYY') || 
                                  RPAD('', 28) || '│' || CHR(10));
    DBMS_LOB.APPEND(p_relatorio, '└────────────────────────────────────────────────────────┘' || CHR(10));
    DBMS_LOB.APPEND(p_relatorio, CHR(10));
    
    -- Estatísticas gerais
    SELECT 
        COUNT(*), 
        SUM(CASE WHEN is_completed = 1 THEN 1 ELSE 0 END)
    INTO v_total_planos, v_planos_completados
    FROM nutrition_plans
    WHERE user_id = p_user_id
    AND plan_date BETWEEN p_data_inicio AND p_data_fim;
    
    IF v_total_planos > 0 THEN
        v_taxa_completude := ROUND((v_planos_completados / v_total_planos) * 100, 1);
    END IF;
    
    DBMS_LOB.APPEND(p_relatorio, '┌─ ESTATÍSTICAS GERAIS ──────────────────────────────────┐' || CHR(10));
    DBMS_LOB.APPEND(p_relatorio, '│ Planos Criados: ' || LPAD(v_total_planos, 38) || '│' || CHR(10));
    DBMS_LOB.APPEND(p_relatorio, '│ Planos Completados: ' || LPAD(v_planos_completados, 34) || '│' || CHR(10));
    DBMS_LOB.APPEND(p_relatorio, '│ Taxa de Completude: ' || LPAD(v_taxa_completude || '%', 34) || '│' || CHR(10));
    DBMS_LOB.APPEND(p_relatorio, '└────────────────────────────────────────────────────────┘' || CHR(10));
    DBMS_LOB.APPEND(p_relatorio, CHR(10));
    
    -- Médias nutricionais
    IF v_planos_completados > 0 THEN
        SELECT 
            ROUND(AVG(total_calories), 0),
            ROUND(AVG(total_proteins), 1),
            ROUND(AVG(total_carbohydrates), 1),
            ROUND(AVG(total_fats), 1),
            ROUND(AVG(water_intake_ml), 0)
        INTO 
            v_media_calorias, v_media_proteinas, v_media_carboidratos,
            v_media_gorduras, v_media_agua
        FROM nutrition_plans
        WHERE user_id = p_user_id
        AND plan_date BETWEEN p_data_inicio AND p_data_fim
        AND is_completed = 1;
        
        DBMS_LOB.APPEND(p_relatorio, '┌─ MÉDIAS NUTRICIONAIS (Planos Completados) ─────────────┐' || CHR(10));
        DBMS_LOB.APPEND(p_relatorio, '│ Calorias Diárias: ' || LPAD(v_media_calorias || ' kcal', 36) || '│' || CHR(10));
        DBMS_LOB.APPEND(p_relatorio, '│ Proteínas: ' || LPAD(v_media_proteinas || ' g', 43) || '│' || CHR(10));
        DBMS_LOB.APPEND(p_relatorio, '│ Carboidratos: ' || LPAD(v_media_carboidratos || ' g', 40) || '│' || CHR(10));
        DBMS_LOB.APPEND(p_relatorio, '│ Gorduras: ' || LPAD(v_media_gorduras || ' g', 44) || '│' || CHR(10));
        DBMS_LOB.APPEND(p_relatorio, '│ Água: ' || LPAD(v_media_agua || ' ml', 48) || '│' || CHR(10));
        DBMS_LOB.APPEND(p_relatorio, '└────────────────────────────────────────────────────────┘' || CHR(10));
        DBMS_LOB.APPEND(p_relatorio, CHR(10));
    END IF;
    
    -- Análise semanal
    DBMS_LOB.APPEND(p_relatorio, '┌─ ANÁLISE SEMANAL ──────────────────────────────────────┐' || CHR(10));
    
    FOR rec IN c_analise_semanal LOOP
        DBMS_LOB.APPEND(p_relatorio, 
            '│ Semana ' || rec.semana || '/' || rec.ano || ': ' ||
            ROUND(rec.media_calorias, 0) || ' kcal/dia (' || 
            rec.planos_count || ' planos)' ||
            RPAD('', 15) || '│' || CHR(10)
        );
    END LOOP;
    
    DBMS_LOB.APPEND(p_relatorio, '└────────────────────────────────────────────────────────┘' || CHR(10));
    DBMS_LOB.APPEND(p_relatorio, CHR(10));
    
    -- Top 5 dias com mais calorias
    DBMS_LOB.APPEND(p_relatorio, '┌─ TOP 5 DIAS COM MAIS CALORIAS ─────────────────────────┐' || CHR(10));
    FOR rec IN c_top_calorias LOOP
        DBMS_LOB.APPEND(p_relatorio, 
            '│ ' || TO_CHAR(rec.plan_date, 'DD/MM') || ': ' ||
            SUBSTR(rec.title, 1, 30) || ' - ' ||
            rec.total_calories || ' kcal' ||
            RPAD('', 5) || '│' || CHR(10)
        );
    END LOOP;
    DBMS_LOB.APPEND(p_relatorio, '└────────────────────────────────────────────────────────┘' || CHR(10));
    DBMS_LOB.APPEND(p_relatorio, CHR(10));
    
    -- Indicador de saúde
    v_score_saude := calcular_indicador_saude_usuario(p_user_id, p_data_fim - p_data_inicio);
    
    DBMS_LOB.APPEND(p_relatorio, '┌─ INDICADOR DE SAÚDE NUTRICIONAL ───────────────────────┐' || CHR(10));
    DBMS_LOB.APPEND(p_relatorio, '│ Score: ' || LPAD(ROUND(v_score_saude, 0) || '/100', 47) || '│' || CHR(10));
    DBMS_LOB.APPEND(p_relatorio, '│ Status: ');
    
    IF v_score_saude >= 85 THEN
        DBMS_LOB.APPEND(p_relatorio, LPAD('EXCELENTE ★★★★★', 46));
    ELSIF v_score_saude >= 70 THEN
        DBMS_LOB.APPEND(p_relatorio, LPAD('BOM ★★★★', 46));
    ELSIF v_score_saude >= 50 THEN
        DBMS_LOB.APPEND(p_relatorio, LPAD('REGULAR ★★★', 46));
    ELSE
        DBMS_LOB.APPEND(p_relatorio, LPAD('PRECISA MELHORAR ★', 46));
    END IF;
    
    DBMS_LOB.APPEND(p_relatorio, '│' || CHR(10));
    DBMS_LOB.APPEND(p_relatorio, '└────────────────────────────────────────────────────────┘' || CHR(10));
    DBMS_LOB.APPEND(p_relatorio, CHR(10));
    
    -- Rodapé
    DBMS_LOB.APPEND(p_relatorio, '════════════════════════════════════════════════════════════' || CHR(10));
    DBMS_LOB.APPEND(p_relatorio, 'Relatório gerado em: ' || TO_CHAR(SYSDATE, 'DD/MM/YYYY HH24:MI:SS') || CHR(10));
    DBMS_LOB.APPEND(p_relatorio, 'NutriXpert © 2025 - Tecnologia para sua saúde' || CHR(10));
    DBMS_LOB.APPEND(p_relatorio, '════════════════════════════════════════════════════════════' || CHR(10));
    
    -- Sucesso
    p_sucesso := 1;
    COMMIT;
    
    DBMS_OUTPUT.PUT_LINE('Relatório gerado com sucesso para usuário: ' || v_user_name);
    
EXCEPTION
    WHEN e_usuario_invalido THEN
        p_sucesso := 0;
        ROLLBACK;
        RAISE_APPLICATION_ERROR(-20020, 'Usuário inválido ou não encontrado');
    WHEN e_datas_invalidas THEN
        p_sucesso := 0;
        ROLLBACK;
        RAISE_APPLICATION_ERROR(-20021, 'Data de início deve ser anterior à data de fim');
    WHEN e_periodo_muito_longo THEN
        p_sucesso := 0;
        ROLLBACK;
        RAISE_APPLICATION_ERROR(-20022, 'Período de análise não pode exceder 365 dias');
    WHEN OTHERS THEN
        p_sucesso := 0;
        ROLLBACK;
        RAISE_APPLICATION_ERROR(-20023, 'Erro ao gerar relatório: ' || SQLERRM);
END proc_gerar_relatorio_consumo;
/

COMMIT;
