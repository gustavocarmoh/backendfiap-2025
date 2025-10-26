-- =====================================
-- V8: Functions e Procedures PL/SQL para Oracle
-- =====================================
-- Implementação de funções e procedures com lógica de negócio
-- para demonstrar capacidades avançadas do Oracle Database
-- =====================================

-- Package de funções NutriXpert
CREATE OR REPLACE PACKAGE pkg_nutrixpert_func AS
    -- Calcula score de saúde do usuário
    FUNCTION calcular_indicador_saude_usuario(p_user_id IN VARCHAR2, p_dias IN NUMBER) RETURN NUMBER;

    -- Gera relatório nutricional formatado
    FUNCTION formatar_relatorio_nutricao(p_user_id IN VARCHAR2) RETURN CLOB;
END pkg_nutrixpert_func;
/

CREATE OR REPLACE PACKAGE BODY pkg_nutrixpert_func AS
    FUNCTION calcular_indicador_saude_usuario(p_user_id IN VARCHAR2, p_dias IN NUMBER) RETURN NUMBER IS
        -- Variáveis para cálculo
        v_total_planos NUMBER := 0;
        v_planos_completados NUMBER := 0;
        v_media_calorias NUMBER := 0;
        v_media_proteinas NUMBER := 0;
        v_media_carboidratos NUMBER := 0;
        v_media_gorduras NUMBER := 0;
        v_media_agua NUMBER := 0;
        
        -- Constantes recomendadas (valores médios)
        c_calorias_ideal CONSTANT NUMBER := 2000;
        c_proteinas_ideal CONSTANT NUMBER := 60;
        c_carboidratos_ideal CONSTANT NUMBER := 250;
        c_gorduras_ideal CONSTANT NUMBER := 70;
        c_agua_ideal CONSTANT NUMBER := 2000;
        
        -- Scores parciais
        v_score_completude NUMBER := 0;
        v_score_calorias NUMBER := 0;
        v_score_macros NUMBER := 0;
        v_score_hidratacao NUMBER := 0;
        v_score_final NUMBER := 0;
        
        -- Exceções customizadas
        e_usuario_invalido EXCEPTION;
        e_sem_dados EXCEPTION;
        
    BEGIN
        -- Validação de entrada
        IF p_user_id IS NULL THEN
            RAISE e_usuario_invalido;
        END IF;
        
        -- Contar total de planos no período
        SELECT COUNT(*)
        INTO v_total_planos
        FROM nutrition_plans
        WHERE user_id = p_user_id
        AND plan_date >= TRUNC(SYSDATE) - p_dias
        AND plan_date <= TRUNC(SYSDATE);
        
        -- Se não houver dados, retornar 0
        IF v_total_planos = 0 THEN
            RETURN 0;
        END IF;
        
        -- Contar planos completados
        SELECT COUNT(*)
        INTO v_planos_completados
        FROM nutrition_plans
        WHERE user_id = p_user_id
        AND plan_date >= TRUNC(SYSDATE) - p_dias
        AND plan_date <= TRUNC(SYSDATE)
        AND is_completed = 1;
        
        -- Calcular médias dos valores nutricionais
        SELECT 
            NVL(AVG(total_calories), 0),
            NVL(AVG(total_proteins), 0),
            NVL(AVG(total_carbohydrates), 0),
            NVL(AVG(total_fats), 0),
            NVL(AVG(water_intake_ml), 0)
        INTO 
            v_media_calorias,
            v_media_proteinas,
            v_media_carboidratos,
            v_media_gorduras,
            v_media_agua
        FROM nutrition_plans
        WHERE user_id = p_user_id
        AND plan_date >= TRUNC(SYSDATE) - p_dias
        AND plan_date <= TRUNC(SYSDATE)
        AND is_completed = 1;
        
        -- Calcular score de completude (0-25 pontos)
        v_score_completude := (v_planos_completados / v_total_planos) * 25;
        
        -- Calcular score de calorias (0-25 pontos)
        -- Penaliza desvios muito grandes (mais de 30%)
        IF v_media_calorias BETWEEN c_calorias_ideal * 0.7 AND c_calorias_ideal * 1.3 THEN
            v_score_calorias := 25 - (ABS(v_media_calorias - c_calorias_ideal) / c_calorias_ideal * 100 * 0.5);
        ELSE
            v_score_calorias := 5; -- Pontuação mínima para desvios grandes
        END IF;
        
        -- Calcular score de macronutrientes (0-25 pontos)
        DECLARE
            v_desvio_proteinas NUMBER;
            v_desvio_carboidratos NUMBER;
            v_desvio_gorduras NUMBER;
        BEGIN
            v_desvio_proteinas := ABS(v_media_proteinas - c_proteinas_ideal) / c_proteinas_ideal;
            v_desvio_carboidratos := ABS(v_media_carboidratos - c_carboidratos_ideal) / c_carboidratos_ideal;
            v_desvio_gorduras := ABS(v_media_gorduras - c_gorduras_ideal) / c_gorduras_ideal;
            
            -- Média dos desvios, convertida em score
            v_score_macros := 25 - ((v_desvio_proteinas + v_desvio_carboidratos + v_desvio_gorduras) / 3 * 50);
            v_score_macros := GREATEST(0, v_score_macros); -- Não permite score negativo
        END;
        
        -- Calcular score de hidratação (0-25 pontos)
        IF v_media_agua >= c_agua_ideal THEN
            v_score_hidratacao := 25;
        ELSIF v_media_agua >= c_agua_ideal * 0.8 THEN
            v_score_hidratacao := 20;
        ELSIF v_media_agua >= c_agua_ideal * 0.6 THEN
            v_score_hidratacao := 15;
        ELSIF v_media_agua >= c_agua_ideal * 0.4 THEN
            v_score_hidratacao := 10;
        ELSE
            v_score_hidratacao := 5;
        END IF;
        
        -- Score final (soma dos componentes)
        v_score_final := v_score_completude + v_score_calorias + v_score_macros + v_score_hidratacao;
        
        -- Garantir que está entre 0 e 100
        v_score_final := LEAST(100, GREATEST(0, v_score_final));
        
        RETURN ROUND(v_score_final, 2);
        
    EXCEPTION
        WHEN e_usuario_invalido THEN
            RAISE_APPLICATION_ERROR(-20001, 'ID de usuário inválido');
        WHEN NO_DATA_FOUND THEN
            RETURN 0;
        WHEN OTHERS THEN
            -- Log do erro (em produção, seria registrado em tabela de log)
            RAISE_APPLICATION_ERROR(-20002, 'Erro ao calcular indicador de saúde: ' || SQLERRM);
    END calcular_indicador_saude_usuario;

    FUNCTION formatar_relatorio_nutricao(p_user_id IN VARCHAR2) RETURN CLOB IS
        -- Variáveis para construção do relatório
        v_relatorio CLOB;
        v_user_name VARCHAR2(300);
        v_total_planos NUMBER := 0;
        v_planos_completados NUMBER := 0;
        v_percentual_completude NUMBER := 0;
        
        -- Estatísticas nutricionais
        v_media_calorias NUMBER := 0;
        v_media_proteinas NUMBER := 0;
        v_media_carboidratos NUMBER := 0;
        v_media_gorduras NUMBER := 0;
        v_media_agua NUMBER := 0;
        v_total_calorias NUMBER := 0;
        
        -- Score de saúde
        v_score_saude NUMBER := 0;
        v_classificacao VARCHAR2(50);
        
        -- Cursor para planos recentes
        CURSOR c_planos_recentes IS
            SELECT plan_date, title, total_calories, is_completed
            FROM nutrition_plans
            WHERE user_id = p_user_id
            AND plan_date BETWEEN p_data_inicio AND p_data_fim
            ORDER BY plan_date DESC
            FETCH FIRST 5 ROWS ONLY;
        
        -- Exceções
        e_usuario_invalido EXCEPTION;
        e_datas_invalidas EXCEPTION;
        
    BEGIN
        -- Validações
        IF p_user_id IS NULL THEN
            RAISE e_usuario_invalido;
        END IF;
        
        IF p_data_inicio > p_data_fim THEN
            RAISE e_datas_invalidas;
        END IF;
        
        -- Inicializar CLOB
        DBMS_LOB.CREATETEMPORARY(v_relatorio, TRUE);
        
        -- Buscar nome do usuário
        BEGIN
            SELECT first_name || ' ' || last_name
            INTO v_user_name
            FROM users
            WHERE id = p_user_id;
        EXCEPTION
            WHEN NO_DATA_FOUND THEN
                v_user_name := 'Usuário Desconhecido';
        END;
        
        -- Cabeçalho do relatório
        DBMS_LOB.APPEND(v_relatorio, '═══════════════════════════════════════════════════════' || CHR(10));
        DBMS_LOB.APPEND(v_relatorio, '   RELATÓRIO NUTRICIONAL - NUTRIXPERT SYSTEM' || CHR(10));
        DBMS_LOB.APPEND(v_relatorio, '═══════════════════════════════════════════════════════' || CHR(10));
        DBMS_LOB.APPEND(v_relatorio, CHR(10));
        DBMS_LOB.APPEND(v_relatorio, 'Usuário: ' || v_user_name || CHR(10));
        DBMS_LOB.APPEND(v_relatorio, 'Período: ' || TO_CHAR(p_data_inicio, 'DD/MM/YYYY') || 
                                      ' até ' || TO_CHAR(p_data_fim, 'DD/MM/YYYY') || CHR(10));
        DBMS_LOB.APPEND(v_relatorio, 'Data do Relatório: ' || TO_CHAR(SYSDATE, 'DD/MM/YYYY HH24:MI:SS') || CHR(10));
        DBMS_LOB.APPEND(v_relatorio, CHR(10));
        
        -- Contar planos
        SELECT COUNT(*), SUM(CASE WHEN is_completed = 1 THEN 1 ELSE 0 END)
        INTO v_total_planos, v_planos_completados
        FROM nutrition_plans
        WHERE user_id = p_user_id
        AND plan_date BETWEEN p_data_inicio AND p_data_fim;
        
        IF v_total_planos = 0 THEN
            DBMS_LOB.APPEND(v_relatorio, '───────────────────────────────────────────────────────' || CHR(10));
            DBMS_LOB.APPEND(v_relatorio, 'AVISO: Nenhum plano nutricional encontrado no período.' || CHR(10));
            DBMS_LOB.APPEND(v_relatorio, '───────────────────────────────────────────────────────' || CHR(10));
            RETURN v_relatorio;
        END IF;
        
        -- Calcular percentual de completude
        v_percentual_completude := ROUND((v_planos_completados / v_total_planos) * 100, 1);
        
        -- Estatísticas gerais
        DBMS_LOB.APPEND(v_relatorio, '───────────────────────────────────────────────────────' || CHR(10));
        DBMS_LOB.APPEND(v_relatorio, '  ESTATÍSTICAS GERAIS' || CHR(10));
        DBMS_LOB.APPEND(v_relatorio, '───────────────────────────────────────────────────────' || CHR(10));
        DBMS_LOB.APPEND(v_relatorio, 'Total de Planos Criados: ' || v_total_planos || CHR(10));
        DBMS_LOB.APPEND(v_relatorio, 'Planos Completados: ' || v_planos_completados || CHR(10));
        DBMS_LOB.APPEND(v_relatorio, 'Taxa de Completude: ' || v_percentual_completude || '%' || CHR(10));
        DBMS_LOB.APPEND(v_relatorio, CHR(10));
        
        -- Buscar médias nutricionais
        SELECT 
            NVL(AVG(total_calories), 0),
            NVL(AVG(total_proteins), 0),
            NVL(AVG(total_carbohydrates), 0),
            NVL(AVG(total_fats), 0),
            NVL(AVG(water_intake_ml), 0),
            NVL(SUM(total_calories), 0)
        INTO 
            v_media_calorias, v_media_proteinas, v_media_carboidratos,
            v_media_gorduras, v_media_agua, v_total_calorias
        FROM nutrition_plans
        WHERE user_id = p_user_id
        AND plan_date BETWEEN p_data_inicio AND p_data_fim
        AND is_completed = 1;
        
        -- Valores nutricionais médios
        DBMS_LOB.APPEND(v_relatorio, '───────────────────────────────────────────────────────' || CHR(10));
        DBMS_LOB.APPEND(v_relatorio, '  VALORES NUTRICIONAIS MÉDIOS (Planos Completados)' || CHR(10));
        DBMS_LOB.APPEND(v_relatorio, '───────────────────────────────────────────────────────' || CHR(10));
        DBMS_LOB.APPEND(v_relatorio, 'Calorias Diárias: ' || ROUND(v_media_calorias, 0) || ' kcal' || CHR(10));
        DBMS_LOB.APPEND(v_relatorio, 'Proteínas: ' || ROUND(v_media_proteinas, 1) || ' g' || CHR(10));
        DBMS_LOB.APPEND(v_relatorio, 'Carboidratos: ' || ROUND(v_media_carboidratos, 1) || ' g' || CHR(10));
        DBMS_LOB.APPEND(v_relatorio, 'Gorduras: ' || ROUND(v_media_gorduras, 1) || ' g' || CHR(10));
        DBMS_LOB.APPEND(v_relatorio, 'Água: ' || ROUND(v_media_agua, 0) || ' ml' || CHR(10));
        DBMS_LOB.APPEND(v_relatorio, 'Total de Calorias no Período: ' || ROUND(v_total_calorias, 0) || ' kcal' || CHR(10));
        DBMS_LOB.APPEND(v_relatorio, CHR(10));
        
        -- Calcular e adicionar indicador de saúde
        v_score_saude := calcular_indicador_saude_usuario(p_user_id, p_data_fim - p_data_inicio);
        
        -- Classificação baseada no score
        IF v_score_saude >= 85 THEN
            v_classificacao := 'EXCELENTE';
        ELSIF v_score_saude >= 70 THEN
            v_classificacao := 'BOM';
        ELSIF v_score_saude >= 50 THEN
            v_classificacao := 'REGULAR';
        ELSE
            v_classificacao := 'PRECISA MELHORAR';
        END IF;
        
        DBMS_LOB.APPEND(v_relatorio, '───────────────────────────────────────────────────────' || CHR(10));
        DBMS_LOB.APPEND(v_relatorio, '  INDICADOR DE SAÚDE NUTRICIONAL' || CHR(10));
        DBMS_LOB.APPEND(v_relatorio, '───────────────────────────────────────────────────────' || CHR(10));
        DBMS_LOB.APPEND(v_relatorio, 'Score: ' || v_score_saude || '/100' || CHR(10));
        DBMS_LOB.APPEND(v_relatorio, 'Classificação: ' || v_classificacao || CHR(10));
        DBMS_LOB.APPEND(v_relatorio, CHR(10));
        
        -- Listar planos recentes
        DBMS_LOB.APPEND(v_relatorio, '───────────────────────────────────────────────────────' || CHR(10));
        DBMS_LOB.APPEND(v_relatorio, '  PLANOS RECENTES (Últimos 5)' || CHR(10));
        DBMS_LOB.APPEND(v_relatorio, '───────────────────────────────────────────────────────' || CHR(10));
        
        FOR rec IN c_planos_recentes LOOP
            DBMS_LOB.APPEND(v_relatorio, 
                TO_CHAR(rec.plan_date, 'DD/MM/YYYY') || ' - ' || 
                rec.title || ' (' || 
                NVL(TO_CHAR(rec.total_calories), 'N/A') || ' kcal) ' ||
                CASE WHEN rec.is_completed = 1 THEN '[✓ Completado]' ELSE '[○ Pendente]' END ||
                CHR(10)
            );
        END LOOP;
        
        -- Rodapé
        DBMS_LOB.APPEND(v_relatorio, CHR(10));
        DBMS_LOB.APPEND(v_relatorio, '═══════════════════════════════════════════════════════' || CHR(10));
        DBMS_LOB.APPEND(v_relatorio, '  Relatório gerado automaticamente pelo sistema' || CHR(10));
        DBMS_LOB.APPEND(v_relatorio, '  NutriXpert © 2025' || CHR(10));
        DBMS_LOB.APPEND(v_relatorio, '═══════════════════════════════════════════════════════' || CHR(10));
        
        RETURN v_relatorio;
        
    EXCEPTION
        WHEN e_usuario_invalido THEN
            RAISE_APPLICATION_ERROR(-20003, 'ID de usuário inválido');
        WHEN e_datas_invalidas THEN
            RAISE_APPLICATION_ERROR(-20004, 'Data de início deve ser menor que data de fim');
        WHEN OTHERS THEN
            RAISE_APPLICATION_ERROR(-20005, 'Erro ao gerar relatório: ' || SQLERRM);
    END formatar_relatorio_nutricao;
END pkg_nutrixpert_func;
/

-- Documentação:
-- Package pkg_nutrixpert_func agrupa funções de cálculo e geração de relatórios nutricionais,
-- facilitando reuso e manutenção centralizada das regras de negócio.

COMMIT;
    WHEN e_usuario_invalido THEN
        RAISE_APPLICATION_ERROR(-20003, 'ID de usuário inválido');
    WHEN e_datas_invalidas THEN
        RAISE_APPLICATION_ERROR(-20004, 'Data de início deve ser menor que data de fim');
    WHEN OTHERS THEN
        RAISE_APPLICATION_ERROR(-20005, 'Erro ao gerar relatório: ' || SQLERRM);
END formatar_relatorio_nutricao;
/

COMMIT;
