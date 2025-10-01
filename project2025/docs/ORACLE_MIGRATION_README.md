# Migração Oracle Database - NutriXpert System

## 🎯 Visão Geral

Este documento descreve a migração completa do projeto NutriXpert de **PostgreSQL** para **Oracle Database**, incluindo implementação de functions e procedures PL/SQL avançadas.

---

## ✅ Componentes Implementados

### 1. 📦 Dependências Maven
- ✅ Oracle JDBC Driver (ojdbc11)
- ✅ Flyway Oracle Database Support
- ✅ Remoção das dependências PostgreSQL

### 2. ⚙️ Configurações
- ✅ `application.properties` atualizado para Oracle
- ✅ Dialeto Hibernate configurado para Oracle
- ✅ Flyway configurado para migrations Oracle

### 3. 🗄️ Scripts de Migração (Oracle)
- ✅ `V1__init_oracle.sql` - Tabelas de usuários e roles
- ✅ `V2__subscription_system_oracle.sql` - Sistema de assinaturas
- ✅ `V3__user_photos_oracle.sql` - Fotos de usuário
- ✅ `V4__service_providers_oracle.sql` - Provedores de serviço
- ✅ `V5__nutrition_plans_oracle.sql` - Planos nutricionais
- ✅ `V6__chat_system_oracle.sql` - Sistema de chat
- ✅ `V7__insert_nutrition_plans_data_oracle.sql` - Dados de exemplo
- ✅ `V8__plsql_functions_oracle.sql` - Functions PL/SQL
- ✅ `V9__plsql_procedures_oracle.sql` - Procedures PL/SQL

### 4. 🔧 Functions PL/SQL

#### **calcular_indicador_saude_usuario**
- Calcula score de saúde nutricional (0-100)
- Considera: completude, calorias, macronutrientes, hidratação
- Usa: variáveis, cálculos complexos, tratamento de exceções

#### **formatar_relatorio_nutricao**
- Gera relatório formatado em texto
- Inclui: estatísticas, médias, score de saúde, histórico
- Usa: CLOB, cursors, formatação avançada

### 5. ⚙️ Procedures PL/SQL

#### **proc_registrar_alerta_nutricional**
- Analisa planos e registra alertas automáticos
- 9 tipos de alertas (INATIVIDADE, CALORIAS_BAIXAS, etc.)
- Usa: **CURSOR**, **LOOP**, **IF/ELSIF**, **EXCEPTION**, procedures aninhadas

#### **proc_gerar_relatorio_consumo**
- Relatório detalhado com análise semanal
- Inclui: rankings, comparações, indicadores
- Usa: **CURSOR**, **LOOP**, **TYPE...TABLE OF**, **DBMS_LOB**

### 6. ☕ Integração Java

#### **OracleStoredProcedureService**
Serviço Java para executar functions e procedures:
- `calcularIndicadorSaude()` - Executa function via JDBC
- `formatarRelatorioNutricao()` - Retorna CLOB como String
- `registrarAlertasNutricionais()` - Executa procedure com OUT parameter
- `gerarRelatorioConsumo()` - Procedure com múltiplos OUT parameters
- `executarAnaliseCompleta()` - Combina function e procedures

#### **OracleProcedureController**
Controller REST com endpoints:
- `GET /api/v1/oracle/indicador-saude/{userId}` - Calcula score
- `GET /api/v1/oracle/relatorio-nutricao/{userId}` - Gera relatório texto
- `POST /api/v1/oracle/alertas-nutricionais/{userId}` - Registra alertas
- `POST /api/v1/oracle/relatorio-consumo` - Relatório detalhado
- `POST /api/v1/oracle/analise-completa/{userId}` - Análise completa
- `GET /api/v1/oracle/estatisticas/{userId}` - Estatísticas básicas

### 7. 📚 Documentação
- ✅ `ORACLE_DATABASE_MODEL.md` - Modelo completo com DER
- ✅ `ORACLE_PLSQL_GUIDE.md` - Guia prático com exemplos

---

## 🚀 Como Usar

### Pré-requisitos

1. **Oracle Database** instalado e rodando
   - Oracle XE 21c (recomendado para desenvolvimento)
   - Oracle Database 19c ou superior (produção)

2. **Variáveis de Ambiente**
```bash
export SPRING_DATASOURCE_URL="jdbc:oracle:thin:@localhost:1521:XE"
export SPRING_DATASOURCE_USERNAME="seu_usuario"
export SPRING_DATASOURCE_PASSWORD="sua_senha"
export ORACLE_SCHEMA="NUTRIXPERT"
```

### Instalação

1. **Criar usuário e schema no Oracle**
```sql
-- Como SYSDBA
CREATE USER nutrixpert IDENTIFIED BY senha_segura;
GRANT CONNECT, RESOURCE, CREATE VIEW, CREATE PROCEDURE TO nutrixpert;
GRANT UNLIMITED TABLESPACE TO nutrixpert;

-- Conectar como nutrixpert
CONN nutrixpert/senha_segura@XE
```

2. **Compilar o projeto**
```bash
cd /workspaces/project2025
mvn clean install
```

3. **Executar migrations**
```bash
mvn flyway:migrate
```

4. **Iniciar aplicação**
```bash
mvn spring-boot:run
```

### Testar Functions e Procedures

#### Via SQL*Plus / SQL Developer

```sql
-- Testar function
SELECT calcular_indicador_saude_usuario(
    (SELECT id FROM users WHERE email = 'alice@example.com'),
    30
) AS score FROM DUAL;

-- Testar procedure
DECLARE
    v_alertas NUMBER;
BEGIN
    proc_registrar_alerta_nutricional(
        (SELECT id FROM users WHERE email = 'alice@example.com'),
        7,
        v_alertas
    );
    DBMS_OUTPUT.PUT_LINE('Alertas: ' || v_alertas);
END;
/
```

#### Via REST API

```bash
# Obter token JWT primeiro
TOKEN=$(curl -X POST "http://localhost:8080/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"email":"alice@example.com","password":"alice123"}' \
  | jq -r '.token')

# Calcular indicador de saúde
curl -X GET "http://localhost:8080/api/v1/oracle/indicador-saude/{userId}?diasAnalise=30" \
  -H "Authorization: Bearer $TOKEN"

# Registrar alertas
curl -X POST "http://localhost:8080/api/v1/oracle/alertas-nutricionais/{userId}?diasAnalise=7" \
  -H "Authorization: Bearer $TOKEN"

# Análise completa
curl -X POST "http://localhost:8080/api/v1/oracle/analise-completa/{userId}?diasAnalise=7" \
  -H "Authorization: Bearer $TOKEN"
```

---

## 🔄 Principais Diferenças PostgreSQL → Oracle

| Aspecto | PostgreSQL | Oracle |
|---------|-----------|--------|
| **Tipos de Dados** | SERIAL, UUID, BOOLEAN, TEXT | SEQUENCE, VARCHAR2(36), NUMBER(1), CLOB |
| **Autoincremento** | SERIAL/BIGSERIAL | SEQUENCE + TRIGGER |
| **UUID** | UUID nativo | VARCHAR2(36) com SYS_GUID() |
| **Boolean** | BOOLEAN | NUMBER(1) com CHECK (0,1) |
| **Texto longo** | TEXT | CLOB |
| **Binário** | BYTEA | BLOB |
| **Função timestamp** | NOW() | CURRENT_TIMESTAMP |
| **Concatenação** | \|\| | \|\| (igual) |
| **Case insensitive** | ILIKE | LOWER() com índice |
| **Agregação string** | array_agg() | LISTAGG() |
| **Intervalos** | INTERVAL '7 days' | INTERVAL '7' DAY |

---

## 📊 Estrutura de Tabelas Oracle

### Tabelas Principais
1. **USERS** - Usuários do sistema
2. **ROLES** - Perfis de acesso
3. **USER_ROLES** - Relacionamento N:N
4. **SUBSCRIPTION_PLANS** - Planos de assinatura
5. **SUBSCRIPTIONS** - Assinaturas dos usuários
6. **NUTRITION_PLANS** - Planos nutricionais diários
7. **NUTRITION_ALERTS** - Alertas automáticos
8. **USER_PHOTOS** - Fotos armazenadas no banco
9. **SERVICE_PROVIDERS** - Supermercados e parceiros
10. **CHAT_MESSAGES** - Sistema de chat

### Functions e Procedures
- 2 Functions com cálculos e formatação
- 2 Procedures com CURSOR, LOOP, EXCEPTION

---

## 🎓 Conceitos PL/SQL Implementados

### Functions
✅ Parâmetros IN com valores DEFAULT  
✅ RETURN de tipos simples (NUMBER) e complexos (CLOB)  
✅ Variáveis locais e constantes  
✅ Blocos DECLARE aninhados  
✅ Tratamento de exceções (EXCEPTION WHEN)  
✅ Exceções customizadas  
✅ Uso de funções built-in (NVL, AVG, SUM, etc.)  
✅ Cálculos matemáticos complexos  
✅ CASE/IF para lógica condicional  

### Procedures
✅ Parâmetros IN, OUT e IN OUT  
✅ **CURSOR** explícito com OPEN/FETCH/CLOSE  
✅ **CURSOR FOR LOOP** simplificado  
✅ **LOOP/EXIT WHEN** para iteração  
✅ **IF/ELSIF/ELSE** para decisões  
✅ **EXCEPTION** com múltiplos handlers  
✅ Procedures aninhadas (local procedures)  
✅ **COMMIT/ROLLBACK** explícitos  
✅ **DBMS_OUTPUT** para logging  
✅ **DBMS_LOB** para manipulação de CLOB  
✅ **TYPE...RECORD** e **TYPE...TABLE OF** (coleções)  

---

## 🔐 Segurança

### Práticas Implementadas
- ✅ Prepared Statements (previne SQL Injection)
- ✅ Validação de parâmetros nas functions/procedures
- ✅ Tratamento robusto de exceções
- ✅ Uso de variáveis bind no Java
- ✅ Constraints de integridade referencial
- ✅ CHECK constraints para validações

---

## 📈 Performance

### Otimizações
- ✅ Índices em todas as FKs
- ✅ Índices compostos para queries frequentes
- ✅ Índices geoespaciais (latitude, longitude)
- ✅ Unique indexes para evitar duplicação
- ✅ Triggers eficientes (apenas UPDATE)

### Recomendações para Produção
```sql
-- Particionar tabelas grandes
ALTER TABLE nutrition_plans
PARTITION BY RANGE (plan_date)
INTERVAL (NUMTOYMINTERVAL(1, 'MONTH'));

-- Atualizar estatísticas
EXEC DBMS_STATS.GATHER_SCHEMA_STATS('NUTRIXPERT');

-- Monitorar performance
SELECT * FROM V$SQL WHERE SQL_TEXT LIKE '%nutrition_plans%';
```

---

## 🧪 Testes

### Verificar Instalação

```sql
-- Contar tabelas criadas
SELECT COUNT(*) FROM user_tables;
-- Esperado: 10 tabelas

-- Verificar functions
SELECT object_name, object_type, status 
FROM user_objects 
WHERE object_type = 'FUNCTION';
-- Esperado: 2 functions (VALID)

-- Verificar procedures
SELECT object_name, object_type, status 
FROM user_objects 
WHERE object_type = 'PROCEDURE';
-- Esperado: 2 procedures (VALID)

-- Contar usuários seed
SELECT COUNT(*) FROM users;
-- Esperado: 9 usuários

-- Verificar planos de assinatura
SELECT name, price FROM subscription_plans;
-- Esperado: 4 planos
```

---

## 📞 Endpoints REST Disponíveis

### Oracle PL/SQL Endpoints
```
GET    /api/v1/oracle/indicador-saude/{userId}
GET    /api/v1/oracle/relatorio-nutricao/{userId}
POST   /api/v1/oracle/alertas-nutricionais/{userId}
POST   /api/v1/oracle/relatorio-consumo
POST   /api/v1/oracle/analise-completa/{userId}
GET    /api/v1/oracle/estatisticas/{userId}
```

### Documentação Swagger
Acesse: `http://localhost:8080/swagger-ui.html`

Navegue até a seção **"Oracle PL/SQL"** para ver todos os endpoints e testá-los interativamente.

---

## 📖 Documentação Adicional

- **[ORACLE_DATABASE_MODEL.md](./ORACLE_DATABASE_MODEL.md)** - Modelo completo do banco de dados
- **[ORACLE_PLSQL_GUIDE.md](./ORACLE_PLSQL_GUIDE.md)** - Guia prático de functions e procedures
- **[API_ENDPOINTS.md](./API_ENDPOINTS.md)** - Documentação de todos os endpoints
- **[TROUBLESHOOTING.md](./TROUBLESHOOTING.md)** - Soluções para problemas comuns

---

## 🎯 Resultado Final

### ✅ Requisitos Atendidos

1. **Modelo Lógico/Físico Oracle** ✅
   - 10 tabelas adaptadas para Oracle
   - Sequences, triggers, constraints
   - Documentado com DER

2. **Functions PL/SQL** ✅
   - `calcular_indicador_saude_usuario` - Cálculo de indicador
   - `formatar_relatorio_nutricao` - Formatação de dados
   - Uso de parâmetros IN e RETURN
   - Tratamento de exceções
   - Comentários e documentação

3. **Procedures PL/SQL** ✅
   - `proc_registrar_alerta_nutricional` - Análise automática
   - `proc_gerar_relatorio_consumo` - Relatório detalhado
   - Uso de CURSOR, LOOP, IF, EXCEPTION
   - Lógica complexa de negócio

4. **Integração Java** ✅
   - Service layer para executar functions/procedures
   - Controller REST com 6 endpoints
   - Documentação Swagger completa

5. **Documentação** ✅
   - Modelo de dados com DER
   - Guia prático com exemplos
   - README completo

---

## 🤝 Contribuindo

Para reportar problemas ou sugerir melhorias:
1. Abra uma issue no GitHub
2. Descreva o problema ou sugestão
3. Inclua exemplos de código quando relevante

---

## 📄 Licença

Este projeto é proprietário da NutriXpert System.

---

## 👥 Equipe

**NutriXpert Development Team**  
**Data**: Setembro 2025  
**Versão**: 1.0.0

---

## 🎉 Conclusão

A migração para Oracle Database foi concluída com sucesso, incluindo:
- ✅ Todas as tabelas adaptadas
- ✅ Functions PL/SQL implementadas
- ✅ Procedures PL/SQL com lógica avançada
- ✅ Integração Java completa
- ✅ API REST funcional
- ✅ Documentação abrangente

O sistema agora aproveita os recursos avançados do Oracle Database, incluindo PL/SQL para lógica de negócio no banco de dados, proporcionando melhor performance e manutenibilidade.
