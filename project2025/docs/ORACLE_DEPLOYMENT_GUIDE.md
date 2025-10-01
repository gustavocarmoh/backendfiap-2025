# 🚀 Guia de Deployment - Oracle Database

## Pré-requisitos

### Software Necessário
- ✅ Oracle Database 19c ou superior (ou Oracle XE 21c para desenvolvimento)
- ✅ Java 21
- ✅ Maven 3.8+
- ✅ Git

---

## 📋 Passo 1: Preparar o Oracle Database

### Opção A: Oracle XE (Desenvolvimento - Recomendado)

```bash
# Docker (mais fácil)
docker run -d \
  --name oracle-xe \
  -p 1521:1521 \
  -p 5500:5500 \
  -e ORACLE_PWD=OraclePassword123 \
  container-registry.oracle.com/database/express:21.3.0-xe

# Aguardar inicialização (pode levar 2-3 minutos)
docker logs -f oracle-xe
```

### Opção B: Oracle Database em VM/Servidor

Instale seguindo a documentação oficial da Oracle.

---

## 📋 Passo 2: Criar Usuário e Schema

```sql
-- Conectar como SYSDBA
sqlplus sys/OraclePassword123@localhost:1521/XE as sysdba

-- Criar usuário
CREATE USER nutrixpert IDENTIFIED BY NutriXpert2025!;

-- Conceder permissões
GRANT CONNECT, RESOURCE TO nutrixpert;
GRANT CREATE VIEW TO nutrixpert;
GRANT CREATE PROCEDURE TO nutrixpert;
GRANT CREATE SEQUENCE TO nutrixpert;
GRANT CREATE TRIGGER TO nutrixpert;
GRANT UNLIMITED TABLESPACE TO nutrixpert;

-- Verificar
SELECT username, account_status FROM dba_users WHERE username = 'NUTRIXPERT';

-- Conectar como nutrixpert
CONN nutrixpert/NutriXpert2025!@localhost:1521/XE
```

---

## 📋 Passo 3: Configurar Variáveis de Ambiente

### Linux/Mac

Adicione ao `~/.bashrc` ou `~/.zshrc`:

```bash
# Oracle Database - NutriXpert
export SPRING_DATASOURCE_URL="jdbc:oracle:thin:@localhost:1521:XE"
export SPRING_DATASOURCE_USERNAME="nutrixpert"
export SPRING_DATASOURCE_PASSWORD="NutriXpert2025!"
export ORACLE_SCHEMA="NUTRIXPERT"

# Aplicar
source ~/.bashrc
```

### Windows

```cmd
setx SPRING_DATASOURCE_URL "jdbc:oracle:thin:@localhost:1521:XE"
setx SPRING_DATASOURCE_USERNAME "nutrixpert"
setx SPRING_DATASOURCE_PASSWORD "NutriXpert2025!"
setx ORACLE_SCHEMA "NUTRIXPERT"
```

### Docker Compose (Produção)

Crie `docker-compose.yml`:

```yaml
version: '3.8'

services:
  oracle:
    image: container-registry.oracle.com/database/express:21.3.0-xe
    container_name: nutrixpert-oracle
    ports:
      - "1521:1521"
      - "5500:5500"
    environment:
      - ORACLE_PWD=OraclePassword123
    volumes:
      - oracle-data:/opt/oracle/oradata
    networks:
      - nutrixpert-network

  app:
    build: .
    container_name: nutrixpert-app
    ports:
      - "8080:8080"
    environment:
      - SPRING_DATASOURCE_URL=jdbc:oracle:thin:@oracle:1521:XE
      - SPRING_DATASOURCE_USERNAME=nutrixpert
      - SPRING_DATASOURCE_PASSWORD=NutriXpert2025!
      - ORACLE_SCHEMA=NUTRIXPERT
    depends_on:
      - oracle
    networks:
      - nutrixpert-network

volumes:
  oracle-data:

networks:
  nutrixpert-network:
    driver: bridge
```

---

## 📋 Passo 4: Build e Deploy

### 4.1. Clone o Repositório

```bash
git clone https://github.com/gustavocarmoh/backendfiap-2025.git
cd backendfiap-2025/project2025
```

### 4.2. Validar Migração

```bash
./validate-oracle-migration.sh
```

**Resultado esperado**: ✅ 27/27 testes passaram

### 4.3. Compilar Projeto

```bash
mvn clean install -DskipTests
```

### 4.4. Executar Migrations

```bash
# Verificar configuração Flyway
mvn flyway:info

# Executar migrations
mvn flyway:migrate

# Verificar resultado
mvn flyway:info
```

**Resultado esperado**:
```
+------------+------------------------------------+-------------+----------+
| Version    | Description                        | Status      | State    |
+------------+------------------------------------+-------------+----------+
| 1          | init oracle                        | Success     | Success  |
| 2          | subscription system oracle         | Success     | Success  |
| 3          | user photos oracle                 | Success     | Success  |
| 4          | service providers oracle           | Success     | Success  |
| 5          | nutrition plans oracle             | Success     | Success  |
| 6          | chat system oracle                 | Success     | Success  |
| 7          | insert nutrition plans data oracle | Success     | Success  |
| 8          | plsql functions oracle             | Success     | Success  |
| 9          | plsql procedures oracle            | Success     | Success  |
+------------+------------------------------------+-------------+----------+
```

---

## 📋 Passo 5: Validar Instalação no Banco

```sql
-- Conectar como nutrixpert
sqlplus nutrixpert/NutriXpert2025!@localhost:1521/XE

-- Verificar tabelas
SELECT table_name FROM user_tables ORDER BY table_name;
/*
CHAT_MESSAGES
NUTRITION_ALERTS
NUTRITION_PLANS
ROLES
SERVICE_PROVIDERS
SUBSCRIPTION_PLANS
SUBSCRIPTIONS
USER_PHOTOS
USER_ROLES
USERS
*/

-- Verificar functions
SELECT object_name, object_type, status 
FROM user_objects 
WHERE object_type = 'FUNCTION'
ORDER BY object_name;
/*
CALCULAR_INDICADOR_SAUDE_USUARIO  FUNCTION  VALID
FORMATAR_RELATORIO_NUTRICAO       FUNCTION  VALID
*/

-- Verificar procedures
SELECT object_name, object_type, status 
FROM user_objects 
WHERE object_type = 'PROCEDURE'
ORDER BY object_name;
/*
PROC_GERAR_RELATORIO_CONSUMO        PROCEDURE  VALID
PROC_REGISTRAR_ALERTA_NUTRICIONAL   PROCEDURE  VALID
*/

-- Contar dados seed
SELECT 'USERS' AS tabela, COUNT(*) AS total FROM users
UNION ALL
SELECT 'ROLES', COUNT(*) FROM roles
UNION ALL
SELECT 'SUBSCRIPTION_PLANS', COUNT(*) FROM subscription_plans
UNION ALL
SELECT 'SERVICE_PROVIDERS', COUNT(*) FROM service_providers;
/*
USERS                9
ROLES                3
SUBSCRIPTION_PLANS   4
SERVICE_PROVIDERS   12
*/

-- Testar function
SELECT calcular_indicador_saude_usuario(
    (SELECT id FROM users WHERE email = 'alice@example.com'),
    30
) AS score_saude FROM DUAL;
```

---

## 📋 Passo 6: Iniciar Aplicação

### Modo Desenvolvimento

```bash
mvn spring-boot:run
```

### Modo Produção (JAR)

```bash
# Build
mvn clean package -DskipTests

# Executar
java -jar target/project2025-1.0.0.jar
```

### Docker

```bash
# Build imagem
docker build -t nutrixpert:1.0.0 .

# Executar
docker run -d \
  --name nutrixpert-app \
  -p 8080:8080 \
  -e SPRING_DATASOURCE_URL="jdbc:oracle:thin:@host.docker.internal:1521:XE" \
  -e SPRING_DATASOURCE_USERNAME="nutrixpert" \
  -e SPRING_DATASOURCE_PASSWORD="NutriXpert2025!" \
  nutrixpert:1.0.0
```

---

## 📋 Passo 7: Validar Aplicação

### 7.1. Health Check

```bash
curl http://localhost:8080/actuator/health
```

**Esperado**: `{"status":"UP"}`

### 7.2. Swagger UI

Abra no navegador:
```
http://localhost:8080/swagger-ui.html
```

Navegue até a seção **"Oracle PL/SQL"**

### 7.3. Testar Endpoints

```bash
# 1. Fazer login
TOKEN=$(curl -s -X POST "http://localhost:8080/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "alice@example.com",
    "password": "alice123"
  }' | jq -r '.token')

# 2. Obter ID do usuário Alice
USER_ID=$(curl -s -X GET "http://localhost:8080/api/v1/users/me" \
  -H "Authorization: Bearer $TOKEN" | jq -r '.id')

echo "User ID: $USER_ID"

# 3. Calcular indicador de saúde
curl -X GET "http://localhost:8080/api/v1/oracle/indicador-saude/$USER_ID?diasAnalise=30" \
  -H "Authorization: Bearer $TOKEN" | jq '.'

# 4. Registrar alertas nutricionais
curl -X POST "http://localhost:8080/api/v1/oracle/alertas-nutricionais/$USER_ID?diasAnalise=7" \
  -H "Authorization: Bearer $TOKEN" | jq '.'

# 5. Gerar relatório de nutrição (texto)
curl -X GET "http://localhost:8080/api/v1/oracle/relatorio-nutricao/$USER_ID?dataInicio=2025-09-01&dataFim=2025-09-30" \
  -H "Authorization: Bearer $TOKEN"

# 6. Análise completa
curl -X POST "http://localhost:8080/api/v1/oracle/analise-completa/$USER_ID?diasAnalise=7" \
  -H "Authorization: Bearer $TOKEN" | jq '.'
```

---

## 🔧 Troubleshooting

### Problema 1: Erro de conexão Oracle

```
ORA-12541: TNS:no listener
```

**Solução**:
```bash
# Verificar se Oracle está rodando
docker ps | grep oracle

# Se não estiver, iniciar
docker start oracle-xe

# Verificar logs
docker logs oracle-xe
```

### Problema 2: Functions/Procedures INVALID

```sql
-- Verificar erros de compilação
SELECT * FROM user_errors 
WHERE name IN (
    'CALCULAR_INDICADOR_SAUDE_USUARIO',
    'FORMATAR_RELATORIO_NUTRICAO',
    'PROC_REGISTRAR_ALERTA_NUTRICIONAL',
    'PROC_GERAR_RELATORIO_CONSUMO'
);

-- Recompilar
ALTER FUNCTION calcular_indicador_saude_usuario COMPILE;
ALTER FUNCTION formatar_relatorio_nutricao COMPILE;
ALTER PROCEDURE proc_registrar_alerta_nutricional COMPILE;
ALTER PROCEDURE proc_gerar_relatorio_consumo COMPILE;
```

### Problema 3: Flyway Migration Failed

```bash
# Ver histórico
mvn flyway:info

# Reparar último erro
mvn flyway:repair

# Limpar e recriar (CUIDADO: apaga todos os dados!)
mvn flyway:clean
mvn flyway:migrate
```

### Problema 4: Erro de Autenticação JWT

```bash
# Verificar usuários seed
sqlplus nutrixpert/NutriXpert2025!@localhost:1521/XE

SELECT email, password_hash FROM users;

# Usuários disponíveis:
# admin@nutrixpert.local / admin123
# manager@nutrixpert.local / manager123
# alice@example.com / alice123
# bob@example.com / bob123
```

---

## 📊 Monitoramento

### Logs da Aplicação

```bash
# Seguir logs em tempo real
tail -f logs/application.log

# Grep por erros
grep ERROR logs/application.log

# Grep por execuções Oracle
grep "OracleStoredProcedureService" logs/application.log
```

### Monitorar Oracle

```sql
-- Sessões ativas
SELECT username, program, status, logon_time 
FROM v$session 
WHERE username = 'NUTRIXPERT';

-- Queries em execução
SELECT sql_text, elapsed_time, cpu_time
FROM v$sql
WHERE parsing_schema_name = 'NUTRIXPERT'
ORDER BY elapsed_time DESC
FETCH FIRST 10 ROWS ONLY;

-- Estatísticas de tabelas
SELECT table_name, num_rows, last_analyzed
FROM user_tables
ORDER BY num_rows DESC;
```

---

## 🔐 Segurança em Produção

### 1. Alterar Senhas

```sql
-- Alterar senha do usuário Oracle
ALTER USER nutrixpert IDENTIFIED BY "SenhaForte@2025!";
```

```bash
# Atualizar variável de ambiente
export SPRING_DATASOURCE_PASSWORD="SenhaForte@2025!"
```

### 2. Usar Secrets Manager

Para produção, não use variáveis de ambiente diretas. Use:
- AWS Secrets Manager
- Azure Key Vault
- HashiCorp Vault

### 3. SSL/TLS

```bash
# Conexão com SSL
export SPRING_DATASOURCE_URL="jdbc:oracle:thin:@(DESCRIPTION=(ADDRESS=(PROTOCOL=TCPS)(HOST=host)(PORT=2484))(CONNECT_DATA=(SERVICE_NAME=service)))"
```

---

## 📈 Performance Tuning

### 1. Atualizar Estatísticas

```sql
-- Manualmente
EXEC DBMS_STATS.GATHER_SCHEMA_STATS('NUTRIXPERT');

-- Agendar job (todas as noites às 2h)
BEGIN
    DBMS_SCHEDULER.CREATE_JOB (
        job_name        => 'UPDATE_STATS_JOB',
        job_type        => 'PLSQL_BLOCK',
        job_action      => 'BEGIN DBMS_STATS.GATHER_SCHEMA_STATS(''NUTRIXPERT''); END;',
        start_date      => SYSTIMESTAMP,
        repeat_interval => 'FREQ=DAILY; BYHOUR=2',
        enabled         => TRUE
    );
END;
/
```

### 2. Adicionar Índices (se necessário)

```sql
-- Analisar queries lentas
SELECT sql_text, executions, elapsed_time
FROM v$sql
WHERE parsing_schema_name = 'NUTRIXPERT'
AND elapsed_time > 1000000
ORDER BY elapsed_time DESC;

-- Adicionar índice se necessário
-- CREATE INDEX idx_custom ON tabela(coluna);
```

---

## ✅ Checklist Final

Antes de marcar como concluído:

- [ ] Oracle Database instalado e rodando
- [ ] Usuário `nutrixpert` criado com permissões
- [ ] Variáveis de ambiente configuradas
- [ ] Script de validação passou (27/27)
- [ ] Migrations executadas com sucesso (9/9)
- [ ] 10 tabelas criadas
- [ ] 2 functions compiladas (VALID)
- [ ] 2 procedures compiladas (VALID)
- [ ] Dados seed inseridos (9 usuários)
- [ ] Aplicação iniciada sem erros
- [ ] Swagger UI acessível
- [ ] Endpoint de login funcional
- [ ] 6 endpoints Oracle testados
- [ ] Documentação revisada

---

## 📞 Suporte

Para problemas ou dúvidas:
1. Consulte `TROUBLESHOOTING.md`
2. Verifique logs: `logs/application.log`
3. Consulte documentação Oracle: https://docs.oracle.com/

---

**Boa sorte com o deployment! 🚀**

NutriXpert Development Team  
Setembro 2025
