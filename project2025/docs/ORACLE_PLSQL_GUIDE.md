# Guia Prático de Functions e Procedures PL/SQL - NutriXpert

## 📚 Visão Geral

Este guia apresenta exemplos práticos de uso das functions e procedures PL/SQL implementadas no sistema NutriXpert, incluindo chamadas diretas via SQL e integração com Java através da API REST.

---

## 🎯 Functions PL/SQL

### 1. calcular_indicador_saude_usuario

**Objetivo**: Calcular um indicador de saúde nutricional (0-100) baseado em dados históricos do usuário.

#### 📋 Assinatura

```sql
FUNCTION calcular_indicador_saude_usuario(
    p_user_id IN VARCHAR2,
    p_dias_analise IN NUMBER DEFAULT 30
) RETURN NUMBER
```

#### 🔧 Parâmetros

- **p_user_id**: ID do usuário (UUID)
- **p_dias_analise**: Número de dias para análise (padrão: 30)

#### 📊 Componentes do Score

O score é calculado com base em 4 componentes (cada um vale 25 pontos):

1. **Completude** (25 pts): Percentual de planos completados vs criados
2. **Calorias** (25 pts): Proximidade da meta de 2000 kcal/dia
3. **Macronutrientes** (25 pts): Equilíbrio de proteínas, carboidratos e gorduras
4. **Hidratação** (25 pts): Ingestão adequada de água (2000ml)

#### 💻 Exemplos de Uso

##### Exemplo 1: Uso Direto no SQL

```sql
-- Calcular score dos últimos 30 dias
SELECT calcular_indicador_saude_usuario(
    '12345678-1234-1234-1234-123456789012',  -- user_id
    30                                        -- dias
) AS score_saude
FROM DUAL;

-- Resultado: 72.45
```

##### Exemplo 2: Com Classificação

```sql
SELECT 
    u.first_name || ' ' || u.last_name AS usuario,
    calcular_indicador_saude_usuario(u.id, 30) AS score,
    CASE 
        WHEN calcular_indicador_saude_usuario(u.id, 30) >= 85 THEN 'EXCELENTE'
        WHEN calcular_indicador_saude_usuario(u.id, 30) >= 70 THEN 'BOM'
        WHEN calcular_indicador_saude_usuario(u.id, 30) >= 50 THEN 'REGULAR'
        ELSE 'PRECISA MELHORAR'
    END AS classificacao
FROM users u
WHERE u.email = 'alice@example.com';
```

##### Exemplo 3: Ranking de Usuários

```sql
-- Top 10 usuários com melhor score de saúde
SELECT 
    u.first_name || ' ' || u.last_name AS usuario,
    u.email,
    ROUND(calcular_indicador_saude_usuario(u.id, 30), 2) AS score,
    RANK() OVER (ORDER BY calcular_indicador_saude_usuario(u.id, 30) DESC) AS ranking
FROM users u
WHERE EXISTS (
    SELECT 1 FROM nutrition_plans np 
    WHERE np.user_id = u.id
)
ORDER BY score DESC
FETCH FIRST 10 ROWS ONLY;
```

##### Exemplo 4: Análise Temporal

```sql
-- Comparar score de diferentes períodos
SELECT 
    'Últimos 7 dias' AS periodo,
    ROUND(calcular_indicador_saude_usuario(:user_id, 7), 2) AS score
FROM DUAL
UNION ALL
SELECT 
    'Últimos 30 dias' AS periodo,
    ROUND(calcular_indicador_saude_usuario(:user_id, 30), 2) AS score
FROM DUAL
UNION ALL
SELECT 
    'Últimos 90 dias' AS periodo,
    ROUND(calcular_indicador_saude_usuario(:user_id, 90), 2) AS score
FROM DUAL;
```

#### 🌐 Uso via API REST

```bash
# GET /api/v1/oracle/indicador-saude/{userId}
curl -X GET "http://localhost:8080/api/v1/oracle/indicador-saude/12345678-1234-1234-1234-123456789012?diasAnalise=30" \
  -H "Authorization: Bearer {seu-token-jwt}"
```

**Response**:
```json
{
  "sucesso": true,
  "userId": "12345678-1234-1234-1234-123456789012",
  "diasAnalise": 30,
  "scoreSaude": 72.45,
  "classificacao": "BOM",
  "mensagem": "Indicador de saúde calculado com sucesso"
}
```

---

### 2. formatar_relatorio_nutricao

**Objetivo**: Gerar relatório formatado em texto com estatísticas nutricionais detalhadas.

#### 📋 Assinatura

```sql
FUNCTION formatar_relatorio_nutricao(
    p_user_id IN VARCHAR2,
    p_data_inicio IN DATE DEFAULT TRUNC(SYSDATE) - 7,
    p_data_fim IN DATE DEFAULT TRUNC(SYSDATE)
) RETURN CLOB
```

#### 🔧 Parâmetros

- **p_user_id**: ID do usuário
- **p_data_inicio**: Data inicial do período (padrão: hoje - 7 dias)
- **p_data_fim**: Data final do período (padrão: hoje)

#### 💻 Exemplos de Uso

##### Exemplo 1: Relatório dos Últimos 7 Dias

```sql
-- Gerar relatório padrão (7 dias)
SELECT formatar_relatorio_nutricao(
    '12345678-1234-1234-1234-123456789012'
) AS relatorio
FROM DUAL;
```

##### Exemplo 2: Relatório de Período Customizado

```sql
-- Relatório do último mês
SELECT formatar_relatorio_nutricao(
    '12345678-1234-1234-1234-123456789012',
    TO_DATE('2025-09-01', 'YYYY-MM-DD'),
    TO_DATE('2025-09-30', 'YYYY-MM-DD')
) AS relatorio
FROM DUAL;
```

##### Exemplo 3: Salvar Relatório em Tabela

```sql
-- Criar tabela para histórico de relatórios
CREATE TABLE relatorios_historico (
    id VARCHAR2(36) PRIMARY KEY DEFAULT SYS_GUID(),
    user_id VARCHAR2(36) NOT NULL,
    data_inicio DATE NOT NULL,
    data_fim DATE NOT NULL,
    conteudo CLOB NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Inserir relatório
INSERT INTO relatorios_historico (user_id, data_inicio, data_fim, conteudo)
SELECT 
    :user_id,
    TRUNC(SYSDATE) - 30,
    TRUNC(SYSDATE),
    formatar_relatorio_nutricao(:user_id, TRUNC(SYSDATE) - 30, TRUNC(SYSDATE))
FROM DUAL;

COMMIT;
```

##### Exemplo 4: Exportar para Arquivo

```sql
-- PL/SQL para salvar em arquivo
DECLARE
    v_relatorio CLOB;
    v_file UTL_FILE.FILE_TYPE;
BEGIN
    -- Gerar relatório
    SELECT formatar_relatorio_nutricao(
        '12345678-1234-1234-1234-123456789012',
        TRUNC(SYSDATE) - 30,
        TRUNC(SYSDATE)
    ) INTO v_relatorio
    FROM DUAL;
    
    -- Abrir arquivo
    v_file := UTL_FILE.FOPEN('EXPORT_DIR', 'relatorio_nutricao.txt', 'W');
    
    -- Escrever conteúdo
    UTL_FILE.PUT(v_file, v_relatorio);
    
    -- Fechar arquivo
    UTL_FILE.FCLOSE(v_file);
    
    DBMS_OUTPUT.PUT_LINE('Relatório exportado com sucesso!');
END;
/
```

#### 🌐 Uso via API REST

```bash
# GET /api/v1/oracle/relatorio-nutricao/{userId}
curl -X GET "http://localhost:8080/api/v1/oracle/relatorio-nutricao/12345678-1234-1234-1234-123456789012?dataInicio=2025-09-01&dataFim=2025-09-30" \
  -H "Authorization: Bearer {seu-token-jwt}" \
  -H "Accept: text/plain"
```

**Response** (formato texto):
```
═══════════════════════════════════════════════════════
   RELATÓRIO NUTRICIONAL - NUTRIXPERT SYSTEM
═══════════════════════════════════════════════════════

Usuário: Alice Example
Período: 01/09/2025 até 30/09/2025
Data do Relatório: 30/09/2025 14:30:00

───────────────────────────────────────────────────────
  ESTATÍSTICAS GERAIS
───────────────────────────────────────────────────────
Total de Planos Criados: 25
Planos Completados: 20
Taxa de Completude: 80.0%

───────────────────────────────────────────────────────
  VALORES NUTRICIONAIS MÉDIOS (Planos Completados)
───────────────────────────────────────────────────────
Calorias Diárias: 1950 kcal
Proteínas: 65.5 g
Carboidratos: 240.0 g
Gorduras: 68.2 g
Água: 2100 ml
Total de Calorias no Período: 39000 kcal

───────────────────────────────────────────────────────
  INDICADOR DE SAÚDE NUTRICIONAL
───────────────────────────────────────────────────────
Score: 78.5/100
Classificação: BOM

───────────────────────────────────────────────────────
  PLANOS RECENTES (Últimos 5)
───────────────────────────────────────────────────────
30/09/2025 - Dieta Equilibrada (1920 kcal) [✓ Completado]
29/09/2025 - Plano Low Carb (1850 kcal) [✓ Completado]
28/09/2025 - Dieta Mediterrânea (2050 kcal) [✓ Completado]
27/09/2025 - Plano Detox (1680 kcal) [○ Pendente]
26/09/2025 - Dieta Balanceada (1980 kcal) [✓ Completado]

═══════════════════════════════════════════════════════
  Relatório gerado automaticamente pelo sistema
  NutriXpert © 2025
═══════════════════════════════════════════════════════
```

---

## ⚙️ Procedures PL/SQL

### 1. proc_registrar_alerta_nutricional

**Objetivo**: Analisar planos nutricionais e registrar alertas automáticos sobre situações críticas.

#### 📋 Assinatura

```sql
PROCEDURE proc_registrar_alerta_nutricional(
    p_user_id IN VARCHAR2,
    p_dias_analise IN NUMBER DEFAULT 7,
    p_alertas_gerados OUT NUMBER
)
```

#### 🔧 Parâmetros

- **p_user_id** (IN): ID do usuário
- **p_dias_analise** (IN): Dias para análise (padrão: 7)
- **p_alertas_gerados** (OUT): Número de alertas criados

#### 🚨 Tipos de Alertas Gerados

| Tipo | Condição | Severidade |
|------|----------|------------|
| INATIVIDADE | >3 dias sem criar plano | WARNING |
| CALORIAS_BAIXAS | <1200 kcal | CRITICAL |
| CALORIAS_ALTAS | >3500 kcal | WARNING |
| PROTEINAS_BAIXAS | <40g | WARNING |
| HIDRATACAO_BAIXA | <1500ml | WARNING |
| GORDURAS_ALTAS | >40% das calorias | WARNING |
| BAIXA_ADESAO | ≥5 planos não completados | WARNING |
| SCORE_BAIXO | Score <50 | CRITICAL |
| SCORE_EXCELENTE | Score ≥85 | INFO |

#### 💻 Exemplos de Uso

##### Exemplo 1: Execução Básica

```sql
-- Declarar variável para receber resultado
DECLARE
    v_alertas_gerados NUMBER;
BEGIN
    -- Executar procedure
    proc_registrar_alerta_nutricional(
        p_user_id => '12345678-1234-1234-1234-123456789012',
        p_dias_analise => 7,
        p_alertas_gerados => v_alertas_gerados
    );
    
    -- Exibir resultado
    DBMS_OUTPUT.PUT_LINE('Alertas gerados: ' || v_alertas_gerados);
END;
/
```

##### Exemplo 2: Processar Múltiplos Usuários

```sql
-- Gerar alertas para todos os usuários ativos
DECLARE
    v_alertas_gerados NUMBER;
    v_total_alertas NUMBER := 0;
BEGIN
    FOR rec IN (SELECT id, first_name, last_name FROM users WHERE is_active = 1) LOOP
        BEGIN
            proc_registrar_alerta_nutricional(
                p_user_id => rec.id,
                p_dias_analise => 7,
                p_alertas_gerados => v_alertas_gerados
            );
            
            v_total_alertas := v_total_alertas + v_alertas_gerados;
            
            DBMS_OUTPUT.PUT_LINE(
                rec.first_name || ' ' || rec.last_name || 
                ': ' || v_alertas_gerados || ' alerta(s)'
            );
        EXCEPTION
            WHEN OTHERS THEN
                DBMS_OUTPUT.PUT_LINE('Erro para ' || rec.first_name || ': ' || SQLERRM);
        END;
    END LOOP;
    
    DBMS_OUTPUT.PUT_LINE('Total geral: ' || v_total_alertas || ' alertas');
END;
/
```

##### Exemplo 3: Job Agendado

```sql
-- Criar job para executar diariamente às 08:00
BEGIN
    DBMS_SCHEDULER.CREATE_JOB (
        job_name        => 'JOB_ALERTAS_NUTRICIONAIS',
        job_type        => 'PLSQL_BLOCK',
        job_action      => '
            DECLARE
                v_alertas NUMBER;
            BEGIN
                FOR rec IN (SELECT id FROM users WHERE is_active = 1) LOOP
                    proc_registrar_alerta_nutricional(rec.id, 7, v_alertas);
                END LOOP;
            END;',
        start_date      => SYSTIMESTAMP,
        repeat_interval => 'FREQ=DAILY; BYHOUR=8; BYMINUTE=0',
        enabled         => TRUE,
        comments        => 'Gerar alertas nutricionais diários'
    );
END;
/
```

##### Exemplo 4: Consultar Alertas Gerados

```sql
-- Ver alertas recentes do usuário
SELECT 
    alert_type,
    severity,
    alert_message,
    created_at,
    CASE WHEN is_read = 1 THEN 'Lido' ELSE 'Não lido' END AS status
FROM nutrition_alerts
WHERE user_id = :user_id
ORDER BY created_at DESC
FETCH FIRST 10 ROWS ONLY;
```

#### 🌐 Uso via API REST

```bash
# POST /api/v1/oracle/alertas-nutricionais/{userId}
curl -X POST "http://localhost:8080/api/v1/oracle/alertas-nutricionais/12345678-1234-1234-1234-123456789012?diasAnalise=7" \
  -H "Authorization: Bearer {seu-token-jwt}" \
  -H "Content-Type: application/json"
```

**Response**:
```json
{
  "sucesso": true,
  "userId": "12345678-1234-1234-1234-123456789012",
  "diasAnalise": 7,
  "alertasGerados": 3,
  "mensagem": "3 alerta(s) registrado(s) com sucesso"
}
```

---

### 2. proc_gerar_relatorio_consumo

**Objetivo**: Gerar relatório detalhado com análise semanal, rankings e estatísticas consolidadas.

#### 📋 Assinatura

```sql
PROCEDURE proc_gerar_relatorio_consumo(
    p_user_id IN VARCHAR2,
    p_data_inicio IN DATE DEFAULT TRUNC(SYSDATE) - 30,
    p_data_fim IN DATE DEFAULT TRUNC(SYSDATE),
    p_relatorio OUT CLOB,
    p_sucesso OUT NUMBER
)
```

#### 🔧 Parâmetros

- **p_user_id** (IN): ID do usuário
- **p_data_inicio** (IN): Data inicial (padrão: hoje - 30)
- **p_data_fim** (IN): Data final (padrão: hoje)
- **p_relatorio** (OUT): Relatório gerado
- **p_sucesso** (OUT): 1 = sucesso, 0 = erro

#### 💻 Exemplos de Uso

##### Exemplo 1: Execução Básica

```sql
DECLARE
    v_relatorio CLOB;
    v_sucesso NUMBER;
BEGIN
    -- Executar procedure
    proc_gerar_relatorio_consumo(
        p_user_id => '12345678-1234-1234-1234-123456789012',
        p_data_inicio => TO_DATE('2025-09-01', 'YYYY-MM-DD'),
        p_data_fim => TO_DATE('2025-09-30', 'YYYY-MM-DD'),
        p_relatorio => v_relatorio,
        p_sucesso => v_sucesso
    );
    
    -- Verificar sucesso
    IF v_sucesso = 1 THEN
        DBMS_OUTPUT.PUT_LINE('Relatório gerado com sucesso!');
        DBMS_OUTPUT.PUT_LINE('Tamanho: ' || DBMS_LOB.GETLENGTH(v_relatorio) || ' bytes');
        
        -- Exibir primeiras linhas
        DBMS_OUTPUT.PUT_LINE(SUBSTR(v_relatorio, 1, 500));
    ELSE
        DBMS_OUTPUT.PUT_LINE('Erro ao gerar relatório');
    END IF;
END;
/
```

##### Exemplo 2: Enviar por Email (Simulado)

```sql
DECLARE
    v_relatorio CLOB;
    v_sucesso NUMBER;
    v_email VARCHAR2(255);
BEGIN
    -- Buscar email do usuário
    SELECT email INTO v_email
    FROM users
    WHERE id = '12345678-1234-1234-1234-123456789012';
    
    -- Gerar relatório
    proc_gerar_relatorio_consumo(
        p_user_id => '12345678-1234-1234-1234-123456789012',
        p_data_inicio => TRUNC(SYSDATE) - 30,
        p_data_fim => TRUNC(SYSDATE),
        p_relatorio => v_relatorio,
        p_sucesso => v_sucesso
    );
    
    IF v_sucesso = 1 THEN
        -- Em produção, usar UTL_MAIL ou similar
        DBMS_OUTPUT.PUT_LINE('Enviando relatório para: ' || v_email);
        DBMS_OUTPUT.PUT_LINE('Tamanho: ' || DBMS_LOB.GETLENGTH(v_relatorio));
        
        -- Aqui você chamaria sua rotina de envio de email
        -- send_email(v_email, 'Relatório Nutricional', v_relatorio);
    END IF;
END;
/
```

##### Exemplo 3: Comparação de Períodos

```sql
DECLARE
    v_relatorio_mes_atual CLOB;
    v_relatorio_mes_anterior CLOB;
    v_sucesso NUMBER;
    v_user_id VARCHAR2(36) := '12345678-1234-1234-1234-123456789012';
BEGIN
    -- Relatório do mês atual
    proc_gerar_relatorio_consumo(
        p_user_id => v_user_id,
        p_data_inicio => TRUNC(SYSDATE, 'MM'),
        p_data_fim => TRUNC(SYSDATE),
        p_relatorio => v_relatorio_mes_atual,
        p_sucesso => v_sucesso
    );
    
    -- Relatório do mês anterior
    proc_gerar_relatorio_consumo(
        p_user_id => v_user_id,
        p_data_inicio => ADD_MONTHS(TRUNC(SYSDATE, 'MM'), -1),
        p_data_fim => TRUNC(SYSDATE, 'MM') - 1,
        p_relatorio => v_relatorio_mes_anterior,
        p_sucesso => v_sucesso
    );
    
    DBMS_OUTPUT.PUT_LINE('=== MÊS ATUAL ===');
    DBMS_OUTPUT.PUT_LINE(SUBSTR(v_relatorio_mes_atual, 1, 1000));
    DBMS_OUTPUT.PUT_LINE('');
    DBMS_OUTPUT.PUT_LINE('=== MÊS ANTERIOR ===');
    DBMS_OUTPUT.PUT_LINE(SUBSTR(v_relatorio_mes_anterior, 1, 1000));
END;
/
```

#### 🌐 Uso via API REST

```bash
# POST /api/v1/oracle/relatorio-consumo
curl -X POST "http://localhost:8080/api/v1/oracle/relatorio-consumo" \
  -H "Authorization: Bearer {seu-token-jwt}" \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "12345678-1234-1234-1234-123456789012",
    "dataInicio": "2025-09-01",
    "dataFim": "2025-09-30"
  }'
```

**Response**:
```json
{
  "sucesso": true,
  "relatorio": "╔════════════════════════════════════════════╗\n║  RELATÓRIO DETALHADO DE CONSUMO...  ║\n...",
  "tamanho": 2847
}
```

---

## 🔄 Análise Completa (Combo)

### Uso via API REST

Execute análise completa combinando function e procedures:

```bash
# POST /api/v1/oracle/analise-completa/{userId}
curl -X POST "http://localhost:8080/api/v1/oracle/analise-completa/12345678-1234-1234-1234-123456789012?diasAnalise=7" \
  -H "Authorization: Bearer {seu-token-jwt}"
```

**Response**:
```json
{
  "sucesso": true,
  "mensagem": "Análise completa executada com sucesso",
  "scoreSaude": 75.3,
  "classificacao": "BOM",
  "alertasGerados": 2,
  "relatorio": "═══════════════════════════════════════...",
  "tamanhoRelatorio": 1842
}
```

---

## 📊 Consultas Úteis

### Estatísticas de Alertas

```sql
-- Resumo de alertas por tipo
SELECT 
    alert_type,
    severity,
    COUNT(*) as total,
    COUNT(CASE WHEN is_read = 1 THEN 1 END) as lidos,
    COUNT(CASE WHEN is_read = 0 THEN 1 END) as nao_lidos
FROM nutrition_alerts
WHERE user_id = :user_id
AND created_at >= TRUNC(SYSDATE) - 30
GROUP BY alert_type, severity
ORDER BY severity DESC, total DESC;
```

### Performance das Functions

```sql
-- Medir tempo de execução
SET TIMING ON
SET SERVEROUTPUT ON

DECLARE
    v_inicio TIMESTAMP;
    v_fim TIMESTAMP;
    v_score NUMBER;
BEGIN
    v_inicio := SYSTIMESTAMP;
    
    v_score := calcular_indicador_saude_usuario(:user_id, 30);
    
    v_fim := SYSTIMESTAMP;
    
    DBMS_OUTPUT.PUT_LINE('Score: ' || v_score);
    DBMS_OUTPUT.PUT_LINE('Tempo: ' || 
        EXTRACT(SECOND FROM (v_fim - v_inicio)) || ' segundos');
END;
/
```

---

## 🎓 Boas Práticas

### 1. Tratamento de Exceções

```sql
BEGIN
    proc_registrar_alerta_nutricional(:user_id, 7, :alertas);
EXCEPTION
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('Erro: ' || SQLERRM);
        DBMS_OUTPUT.PUT_LINE('Code: ' || SQLCODE);
END;
/
```

### 2. Uso de Variáveis Bind

```sql
-- Evite concatenação de strings
-- ✓ CORRETO
DECLARE
    v_user_id VARCHAR2(36) := :user_id;
    v_score NUMBER;
BEGIN
    v_score := calcular_indicador_saude_usuario(v_user_id, 30);
END;
/

-- ✗ ERRADO (SQL Injection risk)
-- v_sql := 'SELECT calcular... WHERE id = ' || p_user_id;
```

### 3. Limpeza de Alertas Antigos

```sql
-- Deletar alertas lidos com mais de 90 dias
DELETE FROM nutrition_alerts
WHERE is_read = 1
AND created_at < TRUNC(SYSDATE) - 90;

COMMIT;
```

---

## 🔍 Troubleshooting

### Problema: Function retorna NULL

```sql
-- Verificar se usuário tem dados
SELECT COUNT(*) FROM nutrition_plans WHERE user_id = :user_id;

-- Se 0, usuário não tem planos criados
-- Function retornará 0 (zero), não NULL
```

### Problema: Procedure demora muito

```sql
-- Verificar plano de execução
EXPLAIN PLAN FOR
SELECT * FROM nutrition_plans 
WHERE user_id = :user_id 
AND plan_date >= TRUNC(SYSDATE) - 30;

SELECT * FROM TABLE(DBMS_XPLAN.DISPLAY);

-- Criar índices se necessário
CREATE INDEX idx_nutrition_plans_analysis 
ON nutrition_plans(user_id, plan_date, is_completed);
```

---

## 📝 Conclusão

Este guia apresentou exemplos práticos de uso das functions e procedures PL/SQL implementadas no NutriXpert. Para mais informações sobre a estrutura do banco de dados, consulte `ORACLE_DATABASE_MODEL.md`.

**Recursos Demonstrados**:
- ✅ Chamadas diretas via SQL
- ✅ Integração com Java (REST API)
- ✅ Tratamento de exceções
- ✅ Uso de cursores e loops
- ✅ Manipulação de CLOB
- ✅ Jobs agendados
- ✅ Otimização de performance

**Autor**: NutriXpert Development Team  
**Versão**: 1.0.0  
**Data**: Setembro 2025
