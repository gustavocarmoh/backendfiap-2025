# 🔧 Troubleshooting - NutriXpert

Guia completo para solução de problemas comuns no sistema NutriXpert.

## 📋 Índice

- [🚀 Problemas de Inicialização](#-problemas-de-inicialização)
- [🗄️ Problemas de Banco de Dados](#️-problemas-de-banco-de-dados)
- [🔐 Problemas de Autenticação](#-problemas-de-autenticação)
- [📦 Problemas de Dependências](#-problemas-de-dependências)
- [🌐 Problemas de API](#-problemas-de-api)
- [💾 Problemas de Migração](#-problemas-de-migração)
- [🔄 Problemas de Assinaturas](#-problemas-de-assinaturas)
- [📱 Problemas do Frontend](#-problemas-do-frontend)
- [🐳 Problemas com Docker](#-problemas-com-docker)
- [⚡ Problemas de Performance](#-problemas-de-performance)

---

## 🚀 Problemas de Inicialização

### Erro: "Port 8000 is already in use"

**Sintoma:**
```
***************************
APPLICATION FAILED TO START
***************************

Description:
Web server failed to start. Port 8000 was already in use.

Action:
Identify and stop the process that's listening on port 8000 or configure this application to listen on another port.
```

**Soluções:**

1. **Verificar processo usando a porta:**
```bash
# Linux/Mac
lsof -i :8000
netstat -tulpn | grep :8000

# Windows
netstat -ano | findstr :8000
```

2. **Matar processo específico:**
```bash
# Linux/Mac
kill -9 <PID>

# Windows
taskkill /PID <PID> /F
```

3. **Alterar porta da aplicação:**
```properties
# application.properties
server.port=8001
```

4. **Usar script para matar processos:**
```bash
# Criar script kill-java.sh
#!/bin/bash
pkill -f "java.*project2025"
echo "Processos Java do projeto finalizados"
```

### Erro: "Could not resolve placeholder 'JWT_SECRET'"

**Sintoma:**
```
Caused by: java.lang.IllegalArgumentException: Could not resolve placeholder 'JWT_SECRET' in value "${JWT_SECRET}"
```

**Soluções:**

1. **Criar arquivo .env:**
```bash
# .env na raiz do projeto
JWT_SECRET=your-super-secret-key-that-is-at-least-256-bits-long
DATABASE_URL=jdbc:postgresql://localhost:5432/nutriexpert_db
DATABASE_USERNAME=nutriexpert_user
DATABASE_PASSWORD=nutriexpert_pass
```

2. **Definir variáveis no application.properties:**
```properties
jwt.secret=your-super-secret-key-that-is-at-least-256-bits-long
jwt.expiration=86400000
```

3. **Definir variáveis de ambiente:**
```bash
export JWT_SECRET="your-super-secret-key-that-is-at-least-256-bits-long"
export DATABASE_URL="jdbc:postgresql://localhost:5432/nutriexpert_db"
```

### Erro: "Failed to configure a DataSource"

**Sintoma:**
```
***************************
APPLICATION FAILED TO START
***************************

Description:
Failed to configure a DataSource: 'url' attribute is not specified and no embedded datasource could be configured.
```

**Soluções:**

1. **Verificar se PostgreSQL está rodando:**
```bash
# Linux/Mac
sudo service postgresql status
# ou
brew services list | grep postgresql

# Windows
net start postgresql-x64-13
```

2. **Configurar datasource corretamente:**
```properties
# application.properties
spring.datasource.url=jdbc:postgresql://localhost:5432/nutriexpert_db
spring.datasource.username=nutriexpert_user
spring.datasource.password=nutriexpert_pass
spring.datasource.driver-class-name=org.postgresql.Driver
```

3. **Testar conexão manualmente:**
```bash
psql -h localhost -p 5432 -U nutriexpert_user -d nutriexpert_db
```

---

## 🗄️ Problemas de Banco de Dados

### Erro: "relation does not exist"

**Sintoma:**
```
org.postgresql.util.PSQLException: ERROR: relation "users" does not exist
Position: 15
```

**Soluções:**

1. **Verificar se migrações foram executadas:**
```bash
# Executar migrações manualmente
./run-migrations.sh
```

2. **Verificar tabelas existentes:**
```sql
-- Conectar ao banco
psql -h localhost -U nutriexpert_user -d nutriexpert_db

-- Listar tabelas
\dt

-- Verificar schema
\dn

-- Sair
\q
```

3. **Recriar banco do zero:**
```bash
# Deletar e recriar banco
psql -h localhost -U postgres -c "DROP DATABASE IF EXISTS nutriexpert_db;"
psql -h localhost -U postgres -c "CREATE DATABASE nutriexpert_db OWNER nutriexpert_user;"

# Executar migrações
./run-migrations.sh
```

4. **Verificar versão das migrações:**
```sql
SELECT * FROM flyway_schema_history ORDER BY installed_on DESC;
```

### Erro: "password authentication failed"

**Sintoma:**
```
org.postgresql.util.PSQLException: FATAL: password authentication failed for user "nutriexpert_user"
```

**Soluções:**

1. **Verificar credenciais:**
```bash
# Testar login manual
psql -h localhost -U nutriexpert_user -d nutriexpert_db
```

2. **Recriar usuário:**
```sql
-- Como superuser (postgres)
DROP USER IF EXISTS nutriexpert_user;
CREATE USER nutriexpert_user WITH PASSWORD 'nutriexpert_pass';
GRANT ALL PRIVILEGES ON DATABASE nutriexpert_db TO nutriexpert_user;
```

3. **Verificar pg_hba.conf:**
```bash
# Localizar arquivo de configuração
sudo find / -name pg_hba.conf 2>/dev/null

# Editar para permitir autenticação md5
# Adicionar linha:
# local   all   nutriexpert_user   md5
```

### Erro: "connection refused"

**Sintoma:**
```
org.postgresql.util.PSQLException: Connection to localhost:5432 refused. Check that the hostname and port are correct and that the postmaster is accepting TCP/IP connections.
```

**Soluções:**

1. **Verificar se PostgreSQL está rodando:**
```bash
# Linux
sudo systemctl status postgresql
sudo systemctl start postgresql

# Mac
brew services start postgresql

# Windows
net start postgresql-x64-13
```

2. **Verificar porta:**
```bash
netstat -tulpn | grep 5432
```

3. **Verificar configuração postgresql.conf:**
```bash
# Encontrar arquivo
sudo find / -name postgresql.conf 2>/dev/null

# Verificar se aceita conexões TCP/IP
grep listen_addresses /etc/postgresql/*/main/postgresql.conf
```

---

## 🔐 Problemas de Autenticação

### Erro: "JWT signature does not match"

**Sintoma:**
```
io.jsonwebtoken.security.SignatureException: JWT signature does not match locally computed signature. JWT validity cannot be asserted and should not be trusted.
```

**Soluções:**

1. **Verificar se JWT_SECRET está consistente:**
```bash
# Verificar variável de ambiente
echo $JWT_SECRET

# Deve ser a mesma em toda aplicação
```

2. **Limpar tokens existentes:**
```javascript
// No frontend
localStorage.clear();
sessionStorage.clear();
```

3. **Regenerar secret mais forte:**
```bash
# Gerar novo secret de 256 bits
openssl rand -base64 32
```

### Erro: "Token has expired"

**Sintoma:**
```
{
  "error": "UNAUTHORIZED",
  "message": "JWT token has expired"
}
```

**Soluções:**

1. **Fazer logout e login novamente**

2. **Verificar configuração de expiração:**
```properties
# application.properties
jwt.expiration=86400000  # 24 horas em millisegundos
```

3. **Implementar refresh token (desenvolvimento futuro)**

### Erro: "Access Denied"

**Sintoma:**
```
{
  "error": "FORBIDDEN", 
  "message": "Access Denied"
}
```

**Soluções:**

1. **Verificar roles do usuário:**
```sql
-- Verificar roles no banco
SELECT u.email, r.name as role 
FROM users u 
JOIN user_roles ur ON u.id = ur.user_id 
JOIN roles r ON ur.role_id = r.id;
```

2. **Verificar endpoint requer autenticação:**
```java
// No controller, verificar anotações
@PreAuthorize("hasRole('ADMIN')")
@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
```

3. **Verificar token no header:**
```bash
curl -H "Authorization: Bearer seu-token-aqui" http://localhost:8000/api/endpoint
```

---

## 📦 Problemas de Dependências

### Erro: "Could not resolve dependencies"

**Sintoma:**
```
[ERROR] Failed to execute goal on project nutriexpert: Could not resolve dependencies for project fiap:nutriexpert:jar:1.0-SNAPSHOT
```

**Soluções:**

1. **Limpar cache do Maven:**
```bash
mvn clean
rm -rf ~/.m2/repository
mvn install
```

2. **Verificar conectividade:**
```bash
ping repo1.maven.org
```

3. **Usar Maven Wrapper:**
```bash
./mvnw clean install
```

4. **Verificar versão do Java:**
```bash
java -version
mvn -version

# Deve ser Java 21
```

### Erro: "Package does not exist"

**Sintoma:**
```
[ERROR] symbol:   class JwtUtil
[ERROR] location: package fiap.backend.util
```

**Soluções:**

1. **Recompilar projeto:**
```bash
mvn clean compile
```

2. **Verificar estrutura de packages:**
```bash
find src/main/java -name "*.java" | head -20
```

3. **Verificar imports:**
```java
// Corrigir imports no arquivo problemático
import fiap.backend.util.JwtUtil;
```

---

## 🌐 Problemas de API

### Erro: "404 Not Found"

**Sintoma:**
```json
{
  "timestamp": "2024-01-15T10:30:00.000Z",
  "status": 404,
  "error": "Not Found",
  "path": "/api/nutrition-plans"
}
```

**Soluções:**

1. **Verificar se endpoint existe:**
```bash
# Listar todos os endpoints
curl http://localhost:8000/actuator/mappings | jq '.contexts.application.mappings.dispatcherServlet.mappings'
```

2. **Verificar path completo:**
```bash
# Testar com path completo
curl http://localhost:8000/api/nutrition-plans
```

3. **Verificar se controller está anotado:**
```java
@RestController
@RequestMapping("/api")
public class NutritionPlanController {
```

### Erro: "400 Bad Request - Validation failed"

**Sintoma:**
```json
{
  "error": "VALIDATION_FAILED",
  "message": "Validation failed for object='nutritionPlanRequest'",
  "fieldErrors": [
    {
      "field": "name",
      "message": "Nome é obrigatório"
    }
  ]
}
```

**Soluções:**

1. **Verificar payload da requisição:**
```bash
curl -X POST http://localhost:8000/api/nutrition-plans \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer seu-token" \
  -d '{
    "name": "Plano Teste",
    "description": "Descrição do plano",
    "calories": 2000
  }'
```

2. **Verificar anotações de validação:**
```java
public class NutritionPlanRequest {
    @NotBlank(message = "Nome é obrigatório")
    private String name;
}
```

### Erro: "500 Internal Server Error"

**Sintoma:**
```json
{
  "timestamp": "2024-01-15T10:30:00.000Z",
  "status": 500,
  "error": "Internal Server Error"
}
```

**Soluções:**

1. **Verificar logs da aplicação:**
```bash
tail -f logs/application.log
# ou
docker logs nutriexpert-app
```

2. **Verificar stack trace:**
```bash
# Habilitar debug
curl http://localhost:8000/actuator/loggers/fiap.backend -X POST \
  -H "Content-Type: application/json" \
  -d '{"configuredLevel": "DEBUG"}'
```

3. **Testar com dados mínimos:**
```bash
curl -X POST http://localhost:8000/api/test \
  -H "Content-Type: application/json" \
  -d '{}'
```

---

## 💾 Problemas de Migração

### Erro: "Migration checksum mismatch"

**Sintoma:**
```
org.flywaydb.core.api.FlywayException: Validate failed: Migration checksum mismatch for migration version 1
```

**Soluções:**

1. **Verificar se arquivo de migração foi modificado:**
```bash
# Verificar histórico de migrações
SELECT * FROM flyway_schema_history;
```

2. **Reparar migração:**
```bash
# Executar repair
./mvnw flyway:repair
```

3. **Reset completo (cuidado em produção):**
```bash
# Deletar histórico e re-executar
./mvnw flyway:clean
./mvnw flyway:migrate
```

### Erro: "Migration failed"

**Sintoma:**
```
org.flywaydb.core.api.FlywayException: Migration V7__add_subscription_tables.sql failed
```

**Soluções:**

1. **Verificar logs detalhados:**
```bash
./mvnw flyway:migrate -X
```

2. **Executar migração manualmente:**
```sql
-- Executar SQL da migração no psql
\i src/main/resources/db/migration/V7__add_subscription_tables.sql
```

3. **Verificar se tabela já existe:**
```sql
\dt subscription*
```

4. **Marcar migração como executada (se já foi aplicada):**
```sql
INSERT INTO flyway_schema_history (installed_rank, version, description, type, script, checksum, installed_by, execution_time, success) 
VALUES (7, '7', 'add subscription tables', 'SQL', 'V7__add_subscription_tables.sql', 123456789, 'nutriexpert_user', 1000, true);
```

---

## 🔄 Problemas de Assinaturas

### Erro: "User already has active subscription"

**Sintoma:**
```json
{
  "error": "BUSINESS_EXCEPTION",
  "message": "Usuário já possui uma assinatura ativa"
}
```

**Soluções:**

1. **Verificar assinaturas do usuário:**
```sql
SELECT * FROM subscriptions WHERE user_id = 'user-uuid' AND status IN ('ACTIVE', 'PENDING');
```

2. **Cancelar assinatura anterior:**
```bash
curl -X PUT http://localhost:8000/api/admin/subscriptions/uuid/cancel \
  -H "Authorization: Bearer admin-token"
```

3. **Verificar regra de negócio:**
```java
// Apenas uma assinatura ativa por usuário é permitida
// Verificar SubscriptionService.validateSingleActiveSubscription()
```

### Erro: "Nutrition plan limit exceeded"

**Sintoma:**
```json
{
  "error": "LIMIT_EXCEEDED",
  "message": "Limite de 3 planos nutricionais atingido para sua assinatura"
}
```

**Soluções:**

1. **Verificar limite atual:**
```sql
SELECT sp.nutrition_plan_limit, COUNT(np.id) as used
FROM subscriptions s
JOIN subscription_plans sp ON s.plan_id = sp.id
LEFT JOIN nutrition_plans np ON np.user_id = s.user_id
WHERE s.user_id = 'user-uuid' AND s.status = 'ACTIVE'
GROUP BY sp.nutrition_plan_limit;
```

2. **Excluir planos antigos:**
```bash
curl -X DELETE http://localhost:8000/api/nutrition-plans/uuid \
  -H "Authorization: Bearer user-token"
```

3. **Fazer upgrade de plano:**
```bash
# Cancelar atual e criar novo
curl -X POST http://localhost:8000/api/subscriptions \
  -H "Authorization: Bearer user-token" \
  -d '{"subscriptionPlanId": "premium-plan-uuid"}'
```

---

## 📱 Problemas do Frontend

### Erro: "CORS policy blocked"

**Sintoma:**
```
Access to XMLHttpRequest at 'http://localhost:8000/api/auth/login' from origin 'http://localhost:3000' has been blocked by CORS policy
```

**Soluções:**

1. **Verificar configuração CORS:**
```java
@Configuration
public class CorsConfig {
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:3000"));
        configuration.setAllowedMethods(List.of("*"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
```

2. **Verificar URL da API:**
```javascript
// Verificar se URL está correta
const API_BASE_URL = 'http://localhost:8000';
```

### Erro: "Token not found"

**Sintoma:**
```javascript
// Console do browser
Error: Token not found in localStorage
```

**Soluções:**

1. **Verificar se login foi realizado:**
```javascript
console.log(localStorage.getItem('authToken'));
```

2. **Verificar se token não expirou:**
```javascript
const token = localStorage.getItem('authToken');
if (token) {
  const payload = JSON.parse(atob(token.split('.')[1]));
  console.log('Token expires at:', new Date(payload.exp * 1000));
}
```

3. **Fazer login novamente:**
```javascript
localStorage.removeItem('authToken');
// Redirecionar para login
```

---

## 🐳 Problemas com Docker

### Erro: "Container not starting"

**Sintoma:**
```bash
docker-compose up
nutriexpert-app exited with code 1
```

**Soluções:**

1. **Verificar logs do container:**
```bash
docker-compose logs nutriexpert-app
```

2. **Verificar se banco está ready:**
```bash
docker-compose logs postgres
# Procurar por "database system is ready to accept connections"
```

3. **Verificar ordem de inicialização:**
```yaml
# docker-compose.yml
services:
  app:
    depends_on:
      postgres:
        condition: service_healthy
```

4. **Rebuild dos containers:**
```bash
docker-compose down
docker-compose build --no-cache
docker-compose up
```

### Erro: "Port already allocated"

**Sintoma:**
```
ERROR: for nutriexpert-app  Cannot start service app: driver failed programming external connectivity on endpoint: Bind for 0.0.0.0:8000 failed: port is already allocated
```

**Soluções:**

1. **Parar containers conflitantes:**
```bash
docker ps | grep :8000
docker stop <container-id>
```

2. **Alterar porta no docker-compose:**
```yaml
services:
  app:
    ports:
      - "8001:8000"  # Usar porta 8001 externamente
```

### Erro: "Volume mount failed"

**Sintoma:**
```
ERROR: for postgres  Cannot start service postgres: invalid mount config
```

**Soluções:**

1. **Verificar permissões do diretório:**
```bash
ls -la ./postgres-data/
chmod 755 ./postgres-data/
```

2. **Usar volume nomeado:**
```yaml
services:
  postgres:
    volumes:
      - postgres_data:/var/lib/postgresql/data

volumes:
  postgres_data:
```

---

## ⚡ Problemas de Performance

### Problema: "Application startup is slow"

**Sintomas:**
- Aplicação demora mais de 2 minutos para iniciar
- Timeout em health checks

**Soluções:**

1. **Verificar logs de inicialização:**
```bash
# Habilitar logs de timing
java -Dspring.profiles.active=dev -Ddebug=true -jar target/nutriexpert-1.0-SNAPSHOT.jar
```

2. **Otimizar configuração JVM:**
```bash
# Adicionar flags JVM
java -Xms512m -Xmx1024m -XX:+UseG1GC -jar target/nutriexpert-1.0-SNAPSHOT.jar
```

3. **Verificar conexões de banco:**
```properties
# application.properties
spring.datasource.hikari.maximum-pool-size=5
spring.datasource.hikari.minimum-idle=2
spring.datasource.hikari.connection-timeout=20000
```

### Problema: "High memory usage"

**Sintomas:**
- OutOfMemoryError
- Performance degradada

**Soluções:**

1. **Monitorar heap:**
```bash
# Habilitar GC logs
java -XX:+PrintGCDetails -XX:+PrintGCTimeStamps -jar app.jar
```

2. **Configurar limites de memória:**
```bash
# Docker
docker run -m 1g nutriexpert-app

# Kubernetes
resources:
  limits:
    memory: "1Gi"
  requests:
    memory: "512Mi"
```

3. **Verificar vazamentos:**
```bash
# JProfiler ou similar
jcmd <pid> GC.run_finalization
jcmd <pid> GC.run
```

---

## 🛠️ Ferramentas de Debug

### Scripts Úteis

1. **Script de verificação completa:**
```bash
#!/bin/bash
# check-system.sh

echo "=== Verificação do Sistema NutriXpert ==="

echo "1. Verificando Java..."
java -version

echo "2. Verificando PostgreSQL..."
pg_isready -h localhost -p 5432

echo "3. Verificando aplicação..."
curl -s http://localhost:8000/actuator/health | jq .

echo "4. Verificando banco de dados..."
psql -h localhost -U nutriexpert_user -d nutriexpert_db -c "\dt" | head -10

echo "5. Verificando logs recentes..."
tail -n 20 logs/application.log

echo "=== Verificação concluída ==="
```

2. **Script de limpeza:**
```bash
#!/bin/bash
# clean-restart.sh

echo "Parando aplicação..."
pkill -f "java.*nutriexpert"

echo "Limpando caches..."
rm -rf target/
rm -rf ~/.m2/repository/fiap/

echo "Recompilando..."
mvn clean install -DskipTests

echo "Reiniciando..."
nohup java -jar target/nutriexpert-1.0-SNAPSHOT.jar > logs/app.log 2>&1 &

echo "Aplicação reiniciada!"
```

### Comandos de Monitoramento

```bash
# Verificar saúde da aplicação
curl http://localhost:8000/actuator/health

# Verificar métricas
curl http://localhost:8000/actuator/metrics

# Verificar configurações
curl http://localhost:8000/actuator/configprops

# Verificar endpoints
curl http://localhost:8000/actuator/mappings

# Verificar logs em tempo real
tail -f logs/application.log

# Verificar conexões de banco
netstat -tulpn | grep :5432

# Verificar processos Java
jps -l
```

---

## 📞 Suporte e Escalação

### Quando Escalar o Problema

1. **Problemas de segurança** (JWT, autenticação)
2. **Perda de dados** ou **corrupção de banco**
3. **Performance crítica** em produção
4. **Falhas de integração** com sistemas externos

### Informações para Coleta

Antes de reportar um problema, colete:

```bash
# 1. Informações do sistema
uname -a
java -version
mvn -version

# 2. Status da aplicação
curl http://localhost:8000/actuator/health
curl http://localhost:8000/actuator/info

# 3. Logs relevantes
tail -n 100 logs/application.log
docker-compose logs --tail=50

# 4. Estado do banco
psql -h localhost -U nutriexpert_user -d nutriexpert_db -c "SELECT version();"
psql -h localhost -U nutriexpert_user -d nutriexpert_db -c "SELECT * FROM flyway_schema_history ORDER BY installed_on DESC LIMIT 5;"

# 5. Configurações sensíveis (sem senhas)
grep -v password application.properties
```

### Template de Bug Report

```markdown
## 🐛 Bug Report

### Ambiente
- OS: [Linux/Mac/Windows]
- Java Version: [21.x.x]
- Spring Boot Version: [3.3.2]
- Database: [PostgreSQL 15.x]

### Problema
[Descrição clara do problema]

### Passos para Reproduzir
1. [Primeiro passo]
2. [Segundo passo]
3. [Ver erro]

### Comportamento Esperado
[O que deveria acontecer]

### Comportamento Atual
[O que está acontecendo]

### Logs
```
[Colar logs relevantes aqui]
```

### Screenshots
[Se aplicável]

### Informações Adicionais
[Qualquer contexto adicional]
```

---

## ✅ Checklist de Resolução

Antes de considerar o problema resolvido, verifique:

- [ ] Aplicação inicia sem erros
- [ ] Banco de dados conecta corretamente
- [ ] Migrações executaram com sucesso
- [ ] Endpoints principais respondem (200/201)
- [ ] Autenticação funciona
- [ ] Logs não mostram erros críticos
- [ ] Testes passam (se aplicável)
- [ ] Performance está aceitável
- [ ] Problema original foi reproduzido e corrigido

---

> 💡 **Dica**: Mantenha sempre logs detalhados e monitore a aplicação em tempo real para detectar problemas rapidamente.
