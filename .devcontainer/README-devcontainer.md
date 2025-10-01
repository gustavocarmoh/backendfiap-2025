# DevContainer — NutriXpert API (Java 21 + Spring 3.3 + Oracle Database XE 21c)

## 🎯 Requisitos
- Docker + Docker Compose
- VS Code com a extensão **Dev Containers**
- Pelo menos 8GB RAM disponível (Oracle requer recursos)

## 🚀 Como usar

### 1. Iniciar o DevContainer
1. Abra o repositório no VS Code
2. **Ctrl/Cmd+Shift+P** → *Dev Containers: Reopen in Container*
3. Aguarde a inicialização (primeira vez pode levar 5-10 minutos):
   - Download da imagem Oracle XE (2.5GB)
   - Build do Dockerfile com Oracle Instant Client
   - Inicialização do banco Oracle
   - Criação automática do usuário `nutrixpert`

### 2. Verificar Status do Oracle
```bash
# Dentro do devcontainer
docker logs nutrixpert-oracle-xe

# Aguardar mensagem: "DATABASE IS READY TO USE!"
```

### 3. Conectar ao Oracle

#### Via SQL*Plus (linha de comando)
```bash
# Como SYSDBA
sqlplus sys/OraclePassword123@//oracle:1521/XE as sysdba

# Como usuário nutrixpert
sqlplus nutrixpert/NutriXpert2025!@//oracle:1521/XE

# Verificar tabelas
SELECT table_name FROM user_tables ORDER BY table_name;
```

#### Via VS Code (extensão Oracle Developer Tools)
- Host: `oracle` (ou `localhost` do host)
- Port: `1521`
- SID: `XE`
- Username: `nutrixpert`
- Password: `NutriXpert2025!`

## 🔌 Conexões e Portas

### Aplicação Spring Boot
- **HTTP**: http://localhost:8000
- **Debug JDWP**: `localhost:5005`
- **Perfil ativo**: `dev`

### Oracle Database
- **Database**: `jdbc:oracle:thin:@oracle:1521:XE` (interno) ou `jdbc:oracle:thin:@localhost:1521:XE` (host)
- **Username**: `nutrixpert`
- **Password**: `NutriXpert2025!`
- **Schema**: `NUTRIXPERT`
- **Oracle EM Express**: https://localhost:5500/em
  - Username: `sys`
  - Password: `OraclePassword123`
  - Container name: `XE`
  - As SYSDBA: ✅

## 🛠️ Comandos Úteis

### Maven
```bash
# Compilar projeto
mvn clean install -DskipTests

# Rodar testes
mvn test

# Executar aplicação
mvn spring-boot:run

# Build JAR
mvn package -DskipTests
```

### Flyway
```bash
# Ver status das migrations
mvn flyway:info

# Executar migrations Oracle
mvn flyway:migrate

# Limpar banco (CUIDADO!)
mvn flyway:clean
```

### Oracle SQL*Plus
```bash
# Executar script SQL
sqlplus nutrixpert/NutriXpert2025!@//oracle:1521/XE @/workspaces/project2025/docs/exemplo.sql

# Query inline
echo "SELECT COUNT(*) FROM users;" | sqlplus -s nutrixpert/NutriXpert2025!@//oracle:1521/XE

# Ver functions/procedures
sqlplus nutrixpert/NutriXpert2025!@//oracle:1521/XE <<EOF
SELECT object_name, object_type, status FROM user_objects WHERE object_type IN ('FUNCTION', 'PROCEDURE');
EOF
```

### Docker
```bash
# Ver logs do Oracle
docker logs -f nutrixpert-oracle-xe

# Reiniciar Oracle
docker restart nutrixpert-oracle-xe

# Ver containers
docker ps

# Espaço usado
docker system df
```

## 🔥 Hot Reload

- Inclua `spring-boot-devtools` no `pom.xml`
- Mudanças em `src/main/java` recompilam automaticamente
- Use `mvn spring-boot:run` no terminal do devcontainer

## 🐛 Debug

### Attach Debugger
1. Certifique-se que aplicação está rodando com `mvn spring-boot:run`
2. No VS Code: **Run** → **Start Debugging** (F5)
3. Selecione **"Java: Attach to Remote Program"**
4. Host: `localhost`, Port: `5005`

### Debug Queries Oracle
```sql
-- Ativar trace
ALTER SESSION SET SQL_TRACE = TRUE;

-- Ver queries em execução
SELECT sql_text, elapsed_time 
FROM v$sql 
WHERE parsing_schema_name = 'NUTRIXPERT'
ORDER BY elapsed_time DESC
FETCH FIRST 10 ROWS ONLY;
```

## 💾 Persistência

### Volumes Docker
- **oracle_data**: Dados do Oracle Database (`/opt/oracle/oradata`)
- **maven-repo**: Cache Maven (`/home/vscode/.m2`)

### Backup Manual
```bash
# Exportar schema nutrixpert
docker exec nutrixpert-oracle-xe bash -c \
  "expdp nutrixpert/NutriXpert2025!@XE schemas=nutrixpert directory=DATA_PUMP_DIR dumpfile=nutrixpert_backup.dmp logfile=export.log"

# Importar schema
docker exec nutrixpert-oracle-xe bash -c \
  "impdp nutrixpert/NutriXpert2025!@XE schemas=nutrixpert directory=DATA_PUMP_DIR dumpfile=nutrixpert_backup.dmp logfile=import.log"
```

## 📁 Estrutura de Migrations

```
src/main/resources/db/migration/
├── V1__init_oracle.sql                      # Tabelas base (users, roles)
├── V2__subscription_system_oracle.sql       # Sistema de assinaturas
├── V3__user_photos_oracle.sql               # Fotos (BLOB)
├── V4__service_providers_oracle.sql         # Prestadores de serviço
├── V5__nutrition_plans_oracle.sql           # Planos nutricionais
├── V6__chat_system_oracle.sql               # Sistema de chat
├── V7__insert_nutrition_plans_data_oracle.sql # Dados exemplo
├── V8__plsql_functions_oracle.sql           # 2 Functions PL/SQL
└── V9__plsql_procedures_oracle.sql          # 2 Procedures PL/SQL
```

## 🧪 Validação da Instalação

```bash
# Executar script de validação
cd /workspaces/project2025
./validate-oracle-migration.sh

# Resultado esperado: ✅ 27/27 testes passaram
```

## 🔧 Troubleshooting

### Problema: Oracle não inicia
```bash
# Ver logs
docker logs nutrixpert-oracle-xe

# Se houver erro de memória, ajustar SHM
# Editar docker-compose.yml: shm_size: "2gb"

# Remover container e recriar
docker rm -f nutrixpert-oracle-xe
docker volume rm .devcontainer_oracle_data
# Reabrir devcontainer
```

### Problema: "ORA-12541: TNS:no listener"
```bash
# Aguardar mais tempo (Oracle leva 2-3 min para iniciar)
docker logs nutrixpert-oracle-xe | grep "DATABASE IS READY"

# Verificar porta
docker port nutrixpert-oracle-xe
```

### Problema: Flyway falha
```bash
# Verificar se usuário existe
sqlplus sys/OraclePassword123@//oracle:1521/XE as sysdba <<EOF
SELECT username, account_status FROM dba_users WHERE username = 'NUTRIXPERT';
EOF

# Se não existir, executar manualmente
bash /workspaces/.devcontainer/init-oracle.sh
```

### Problema: Functions/Procedures INVALID
```sql
-- Conectar como nutrixpert
sqlplus nutrixpert/NutriXpert2025!@//oracle:1521/XE

-- Ver erros
SELECT * FROM user_errors WHERE name LIKE '%INDICADOR%' OR name LIKE '%PROC_%';

-- Recompilar
ALTER FUNCTION calcular_indicador_saude_usuario COMPILE;
ALTER PROCEDURE proc_registrar_alerta_nutricional COMPILE;
```

## 📚 Documentação Adicional

- **ORACLE_DATABASE_MODEL.md** - Modelo de dados e DER
- **ORACLE_PLSQL_GUIDE.md** - Exemplos de uso das functions/procedures
- **ORACLE_MIGRATION_README.md** - Guia completo de migração
- **ORACLE_DEPLOYMENT_GUIDE.md** - Guia de deployment produção

## 🎓 Recursos Oracle

- Oracle XE Docs: https://docs.oracle.com/en/database/oracle/oracle-database/21/
- PL/SQL Guide: https://docs.oracle.com/en/database/oracle/oracle-database/21/lnpls/
- JDBC Driver: https://docs.oracle.com/en/database/oracle/oracle-database/21/jjdbc/

---

**Desenvolvido com ❤️ pela equipe NutriXpert**  
Outubro 2025
