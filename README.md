# 🏥 NutriXpert Backend API - FIAP 2025

> Sistema completo de gestão nutricional com Oracle Database, PL/SQL avançado e Spring Boot 3.3

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.java.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.2-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Oracle](https://img.shields.io/badge/Oracle-21c%20XE-red.svg)](https://www.oracle.com/database/)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

## 📋 Índice

- [Sobre o Projeto](#-sobre-o-projeto)
- [Tecnologias](#-tecnologias)
- [Arquitetura Oracle](#-arquitetura-oracle)
- [Início Rápido](#-início-rápido)
- [DevContainer](#-devcontainer)
- [Documentação](#-documentação)
- [Endpoints](#-endpoints)
- [Contribuindo](#-contribuindo)

## 🎯 Sobre o Projeto

O **NutriXpert** é uma plataforma completa de gestão nutricional que oferece:

- 👥 **Gestão de Usuários** com autenticação JWT e controle de roles
- 📊 **Planos Nutricionais** personalizados com análise detalhada
- 🏥 **Sistema de Alertas** automáticos baseados em consumo nutricional
- 💬 **Chat** entre usuários e prestadores de serviço
- 📸 **Upload de Fotos** de perfil com armazenamento em BLOB
- 🔔 **Assinaturas** com diferentes níveis de funcionalidades
- 🤖 **PL/SQL Avançado** com functions e procedures complexas

### ✨ Diferenciais

- ✅ **2 Functions PL/SQL** com cálculos de saúde e geração de relatórios
- ✅ **2 Procedures PL/SQL** com CURSOR, LOOP, EXCEPTION e lógica de negócio
- ✅ **Integração Java↔Oracle** via JDBC para execução de stored procedures
- ✅ **6 Endpoints REST** dedicados para operações Oracle
- ✅ **Migrations Flyway** com suporte completo Oracle
- ✅ **DevContainer** pronto para desenvolvimento

## 🚀 Tecnologias

### Backend
- **Java 21** (LTS)
- **Spring Boot 3.3.2**
  - Spring Security (JWT)
  - Spring Data JPA
  - Spring Web
  - Validation
- **Hibernate 6.4** (OracleDialect)
- **Flyway** (Oracle migrations)

### Database
- **Oracle Database 21c Express Edition**
- **PL/SQL** (Functions, Procedures, Triggers, Cursors)
- **JDBC** (ojdbc11)

### DevOps
- **Docker** + **Docker Compose**
- **Maven 3.9**
- **DevContainers** (VS Code)

### Documentação
- **SpringDoc OpenAPI 3** (Swagger UI)
- **Markdown** (Guias técnicos)

## 🗄️ Arquitetura Oracle

### Modelo de Dados (10 Tabelas)

```
users ─┬─ user_roles ─── roles
       ├─ subscriptions ─── subscription_plans
       ├─ user_photos
       ├─ service_providers
       ├─ nutrition_plans ─── nutrition_alerts
       └─ chat_messages
```

### PL/SQL Objects

#### Functions (2)
1. **`calcular_indicador_saude_usuario`**
   - Calcula score de saúde (0-100) baseado em 30 dias
   - Analisa: completude, calorias, macronutrientes, hidratação
   - Retorna: `NUMBER`

2. **`formatar_relatorio_nutricao`**
   - Gera relatório formatado em texto
   - Inclui estatísticas e recomendações
   - Retorna: `CLOB`

#### Procedures (2)
1. **`proc_registrar_alerta_nutricional`**
   - Analisa planos nutricionais com **CURSOR**
   - Registra alertas automáticos (9 tipos)
   - Usa: `LOOP`, `EXIT WHEN`, `IF/ELSIF/ELSE`, `EXCEPTION`
   - Retorna: contador de alertas via `OUT` parameter

2. **`proc_gerar_relatorio_consumo`**
   - Relatório semanal completo
   - Múltiplos `CURSOR FOR LOOP`
   - Calcula médias e tendências
   - Retorna: relatório CLOB + total de dias

### Integração Java ↔ Oracle

```java
// Exemplo: Chamar function
String sql = "SELECT calcular_indicador_saude_usuario(?, ?) FROM DUAL";
Double score = jdbcTemplate.queryForObject(sql, Double.class, userId, dias);

// Exemplo: Chamar procedure
CallableStatement stmt = connection.prepareCall(
    "{CALL proc_registrar_alerta_nutricional(?, ?, ?)}"
);
stmt.setString(1, userId);
stmt.setInt(2, diasAnalise);
stmt.registerOutParameter(3, Types.INTEGER);
stmt.execute();
Integer alertasRegistrados = stmt.getInt(3);
```

## ⚡ Início Rápido

### Pré-requisitos

- Java 21+
- Maven 3.8+
- Oracle Database 21c (ou Docker)

### 1. Clone o Repositório

```bash
git clone https://github.com/gustavocarmoh/backendfiap-2025.git
cd backendfiap-2025/project2025
```

### 2. Configure Oracle Database

#### Opção A: Docker (Recomendado)

```bash
docker run -d \
  --name oracle-xe \
  -p 1521:1521 \
  -p 5500:5500 \
  -e ORACLE_PWD=OraclePassword123 \
  container-registry.oracle.com/database/express:21.3.0-xe

# Aguardar inicialização (2-3 minutos)
docker logs -f oracle-xe
```

#### Opção B: Instalação Local

Siga o guia oficial da Oracle: https://www.oracle.com/database/technologies/xe-downloads.html

### 3. Criar Usuário e Schema

```bash
# Conectar como SYSDBA
sqlplus sys/OraclePassword123@localhost:1521/XE as sysdba

# Executar
CREATE USER nutrixpert IDENTIFIED BY NutriXpert2025!;
GRANT CONNECT, RESOURCE TO nutrixpert;
GRANT CREATE VIEW, CREATE PROCEDURE, CREATE SEQUENCE, CREATE TRIGGER TO nutrixpert;
GRANT UNLIMITED TABLESPACE TO nutrixpert;
```

### 4. Configurar Variáveis de Ambiente

```bash
export SPRING_DATASOURCE_URL="jdbc:oracle:thin:@localhost:1521:XE"
export SPRING_DATASOURCE_USERNAME="nutrixpert"
export SPRING_DATASOURCE_PASSWORD="NutriXpert2025!"
export ORACLE_SCHEMA="NUTRIXPERT"
```

### 5. Executar Migrations

```bash
mvn flyway:migrate
```

**Resultado esperado**: 9 migrations executadas com sucesso ✅

### 6. Validar Instalação

```bash
./validate-oracle-migration.sh
```

**Resultado esperado**: ✅ 27/27 testes passaram

### 7. Iniciar Aplicação

```bash
mvn spring-boot:run
```

### 8. Acessar Swagger UI

Abra no navegador: **http://localhost:8080/swagger-ui.html**

## 🐳 DevContainer

Para desenvolvimento com **VS Code Dev Containers**:

1. Instale a extensão **Dev Containers** no VS Code
2. Abra o projeto
3. `Ctrl/Cmd+Shift+P` → **Dev Containers: Reopen in Container**
4. Aguarde a inicialização (5-10 minutos na primeira vez)

O devcontainer inclui:
- ✅ Oracle Database XE 21c
- ✅ Java 21 + Maven
- ✅ Oracle Instant Client + SQL*Plus
- ✅ Extensões VS Code (Java, Oracle, Docker)
- ✅ Script de inicialização automática

Leia mais: [.devcontainer/README-devcontainer.md](.devcontainer/README-devcontainer.md)

## 📚 Documentação

### Guias Principais

| Documento | Descrição |
|-----------|-----------|
| [ORACLE_DATABASE_MODEL.md](project2025/docs/ORACLE_DATABASE_MODEL.md) | Modelo de dados completo com DER |
| [ORACLE_PLSQL_GUIDE.md](project2025/docs/ORACLE_PLSQL_GUIDE.md) | 32 exemplos práticos de PL/SQL |
| [ORACLE_MIGRATION_README.md](project2025/docs/ORACLE_MIGRATION_README.md) | Guia de migração PostgreSQL→Oracle |
| [ORACLE_DEPLOYMENT_GUIDE.md](project2025/docs/ORACLE_DEPLOYMENT_GUIDE.md) | Deploy em produção |
| [ORACLE_MIGRATION_SUMMARY.md](project2025/ORACLE_MIGRATION_SUMMARY.md) | Resumo executivo do projeto |

### Guias Adicionais

- [API_ENDPOINTS.md](project2025/docs/API_ENDPOINTS.md) - Lista completa de endpoints
- [JWT_IMPLEMENTATION_SUMMARY.md](project2025/docs/JWT_IMPLEMENTATION_SUMMARY.md) - Autenticação JWT
- [SUBSCRIPTION_PLANS_SYSTEM.md](project2025/docs/SUBSCRIPTION_PLANS_SYSTEM.md) - Sistema de assinaturas
- [TROUBLESHOOTING.md](project2025/docs/TROUBLESHOOTING.md) - Resolução de problemas

## 🔗 Endpoints

### Autenticação
```
POST   /api/auth/register   # Registrar novo usuário
POST   /api/auth/login      # Login (retorna JWT)
GET    /api/auth/me         # Dados do usuário autenticado
```

### Usuários
```
GET    /api/v1/users        # Listar usuários
GET    /api/v1/users/{id}   # Buscar por ID
POST   /api/v1/users        # Criar usuário
PUT    /api/v1/users/{id}   # Atualizar
DELETE /api/v1/users/{id}   # Remover
```

### Oracle PL/SQL ⭐
```
GET    /api/v1/oracle/indicador-saude/{userId}              # Function: Calcular saúde
GET    /api/v1/oracle/relatorio-nutricao/{userId}           # Function: Relatório texto
GET    /api/v1/oracle/relatorio-json/{userId}               # Function: Relatório JSON
POST   /api/v1/oracle/alertas-nutricionais/{userId}         # Procedure: Registrar alertas
GET    /api/v1/oracle/relatorio-consumo/{userId}            # Procedure: Relatório completo
POST   /api/v1/oracle/analise-completa/{userId}             # Análise full (chama todos)
```

### Planos Nutricionais
```
GET    /api/v1/nutrition-plans                    # Listar planos
GET    /api/v1/nutrition-plans/user/{userId}      # Por usuário
POST   /api/v1/nutrition-plans                    # Criar plano
PUT    /api/v1/nutrition-plans/{id}               # Atualizar
DELETE /api/v1/nutrition-plans/{id}               # Remover
```

### Assinaturas
```
GET    /api/v1/subscriptions                      # Listar assinaturas
GET    /api/v1/subscriptions/user/{userId}        # Por usuário
POST   /api/v1/subscriptions                      # Criar assinatura
```

### Outras APIs
- **Fotos de Perfil**: `/api/v1/photos`
- **Prestadores de Serviço**: `/api/v1/service-providers`
- **Chat**: `/api/v1/chat`
- **Health Check**: `/actuator/health`

## 🧪 Testes

```bash
# Executar todos os testes
mvn test

# Testes específicos
mvn test -Dtest=UserServiceTest

# Com cobertura
mvn test jacoco:report
```

## 🔒 Segurança

- **JWT Authentication** com refresh tokens
- **BCrypt** para hash de senhas
- **Role-based Access Control** (ADMIN, MANAGER, USER)
- **@PreAuthorize** em endpoints sensíveis
- **CORS** configurado
- **SQL Injection** prevenido (PreparedStatement)

### Usuários Seed

| Email | Senha | Role |
|-------|-------|------|
| admin@nutrixpert.local | admin123 | ADMIN |
| manager@nutrixpert.local | manager123 | MANAGER |
| alice@example.com | alice123 | USER |
| bob@example.com | bob123 | USER |

## 📊 Estrutura do Projeto

```
project2025/
├── src/main/java/fiap/backend/
│   ├── config/              # Configurações (Security, Swagger, etc)
│   ├── controller/          # REST Controllers
│   ├── service/             # Camada de negócio
│   ├── repository/          # JPA Repositories
│   ├── domain/              # Entidades JPA
│   └── dto/                 # Data Transfer Objects
├── src/main/resources/
│   ├── application.properties
│   ├── application-dev.yml
│   └── db/migration/        # Scripts Flyway Oracle
├── docs/                    # Documentação completa
├── validate-oracle-migration.sh
└── pom.xml
```

## 📄 Licença

Este projeto está sob a licença MIT. Veja o arquivo [LICENSE](LICENSE) para mais detalhes.