#!/bin/bash
# Script de validação da migração Oracle
# NutriXpert System - Setembro 2025

echo "═══════════════════════════════════════════════════════════"
echo "  VALIDAÇÃO DA MIGRAÇÃO ORACLE - NUTRIXPERT SYSTEM"
echo "═══════════════════════════════════════════════════════════"
echo ""

# Cores para output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Contadores
SUCCESS=0
FAILED=0

# Função para testar
test_item() {
    local description=$1
    local test_result=$2
    
    if [ "$test_result" = "0" ]; then
        echo -e "${GREEN}✓${NC} $description"
        ((SUCCESS++))
    else
        echo -e "${RED}✗${NC} $description"
        ((FAILED++))
    fi
}

echo "1. Verificando estrutura do projeto..."
echo "──────────────────────────────────────────────────────────"

# Verificar arquivos de migração Oracle
test_item "V1__init_oracle.sql existe" "$([ -f "src/main/resources/db/migration/V1__init_oracle.sql" ]; echo $?)"
test_item "V2__subscription_system_oracle.sql existe" "$([ -f "src/main/resources/db/migration/V2__subscription_system_oracle.sql" ]; echo $?)"
test_item "V3__user_photos_oracle.sql existe" "$([ -f "src/main/resources/db/migration/V3__user_photos_oracle.sql" ]; echo $?)"
test_item "V4__service_providers_oracle.sql existe" "$([ -f "src/main/resources/db/migration/V4__service_providers_oracle.sql" ]; echo $?)"
test_item "V5__nutrition_plans_oracle.sql existe" "$([ -f "src/main/resources/db/migration/V5__nutrition_plans_oracle.sql" ]; echo $?)"
test_item "V6__chat_system_oracle.sql existe" "$([ -f "src/main/resources/db/migration/V6__chat_system_oracle.sql" ]; echo $?)"
test_item "V7__insert_nutrition_plans_data_oracle.sql existe" "$([ -f "src/main/resources/db/migration/V7__insert_nutrition_plans_data_oracle.sql" ]; echo $?)"
test_item "V8__plsql_functions_oracle.sql existe" "$([ -f "src/main/resources/db/migration/V8__plsql_functions_oracle.sql" ]; echo $?)"
test_item "V9__plsql_procedures_oracle.sql existe" "$([ -f "src/main/resources/db/migration/V9__plsql_procedures_oracle.sql" ]; echo $?)"

echo ""
echo "2. Verificando classes Java..."
echo "──────────────────────────────────────────────────────────"

# Verificar classes Java
test_item "OracleStoredProcedureService.java existe" "$([ -f "src/main/java/fiap/backend/service/OracleStoredProcedureService.java" ]; echo $?)"
test_item "OracleProcedureController.java existe" "$([ -f "src/main/java/fiap/backend/controller/OracleProcedureController.java" ]; echo $?)"
test_item "AnaliseNutricionalRequest.java existe" "$([ -f "src/main/java/fiap/backend/dto/AnaliseNutricionalRequest.java" ]; echo $?)"
test_item "AnaliseNutricionalResponse.java existe" "$([ -f "src/main/java/fiap/backend/dto/AnaliseNutricionalResponse.java" ]; echo $?)"

echo ""
echo "3. Verificando documentação..."
echo "──────────────────────────────────────────────────────────"

# Verificar documentação
test_item "ORACLE_DATABASE_MODEL.md existe" "$([ -f "docs/ORACLE_DATABASE_MODEL.md" ]; echo $?)"
test_item "ORACLE_PLSQL_GUIDE.md existe" "$([ -f "docs/ORACLE_PLSQL_GUIDE.md" ]; echo $?)"
test_item "ORACLE_MIGRATION_README.md existe" "$([ -f "docs/ORACLE_MIGRATION_README.md" ]; echo $?)"

echo ""
echo "4. Verificando pom.xml..."
echo "──────────────────────────────────────────────────────────"

# Verificar dependências no pom.xml
if grep -q "ojdbc11" pom.xml; then
    test_item "Dependência ojdbc11 configurada" "0"
else
    test_item "Dependência ojdbc11 configurada" "1"
fi

if grep -q "flyway-database-oracle" pom.xml; then
    test_item "Dependência flyway-database-oracle configurada" "0"
else
    test_item "Dependência flyway-database-oracle configurada" "1"
fi

echo ""
echo "5. Verificando application.properties..."
echo "──────────────────────────────────────────────────────────"

# Verificar configurações
if grep -q "OracleDialect" src/main/resources/application.properties; then
    test_item "OracleDialect configurado" "0"
else
    test_item "OracleDialect configurado" "1"
fi

if grep -q "oracle.jdbc.OracleDriver" src/main/resources/application.properties; then
    test_item "Oracle Driver configurado" "0"
else
    test_item "Oracle Driver configurado" "1"
fi

echo ""
echo "6. Verificando sintaxe PL/SQL..."
echo "──────────────────────────────────────────────────────────"

# Verificar palavras-chave PL/SQL nos scripts
if grep -q "CREATE OR REPLACE FUNCTION" src/main/resources/db/migration/V8__plsql_functions_oracle.sql; then
    test_item "Functions PL/SQL declaradas" "0"
else
    test_item "Functions PL/SQL declaradas" "1"
fi

if grep -q "CREATE OR REPLACE PROCEDURE" src/main/resources/db/migration/V9__plsql_procedures_oracle.sql; then
    test_item "Procedures PL/SQL declaradas" "0"
else
    test_item "Procedures PL/SQL declaradas" "1"
fi

if grep -q "CURSOR" src/main/resources/db/migration/V9__plsql_procedures_oracle.sql; then
    test_item "Uso de CURSOR implementado" "0"
else
    test_item "Uso de CURSOR implementado" "1"
fi

if grep -q "LOOP" src/main/resources/db/migration/V9__plsql_procedures_oracle.sql; then
    test_item "Uso de LOOP implementado" "0"
else
    test_item "Uso de LOOP implementado" "1"
fi

if grep -q "EXCEPTION" src/main/resources/db/migration/V9__plsql_procedures_oracle.sql; then
    test_item "Tratamento de EXCEPTION implementado" "0"
else
    test_item "Tratamento de EXCEPTION implementado" "1"
fi

echo ""
echo "7. Contando elementos implementados..."
echo "──────────────────────────────────────────────────────────"

# Contar functions
FUNCTION_COUNT=$(grep -c "CREATE OR REPLACE FUNCTION" src/main/resources/db/migration/V8__plsql_functions_oracle.sql)
if [ "$FUNCTION_COUNT" -ge 2 ]; then
    test_item "Pelo menos 2 functions implementadas ($FUNCTION_COUNT encontradas)" "0"
else
    test_item "Pelo menos 2 functions implementadas ($FUNCTION_COUNT encontradas)" "1"
fi

# Contar procedures
PROCEDURE_COUNT=$(grep -c "CREATE OR REPLACE PROCEDURE" src/main/resources/db/migration/V9__plsql_procedures_oracle.sql)
if [ "$PROCEDURE_COUNT" -ge 2 ]; then
    test_item "Pelo menos 2 procedures implementadas ($PROCEDURE_COUNT encontradas)" "0"
else
    test_item "Pelo menos 2 procedures implementadas ($PROCEDURE_COUNT encontradas)" "1"
fi

# Contar tabelas criadas
TABLE_COUNT=$(grep -c "CREATE TABLE" src/main/resources/db/migration/V*_oracle.sql)
echo -e "${YELLOW}ℹ${NC} Total de tabelas criadas: $TABLE_COUNT"

echo ""
echo "═══════════════════════════════════════════════════════════"
echo "  RESULTADO DA VALIDAÇÃO"
echo "═══════════════════════════════════════════════════════════"
echo ""
echo -e "Testes bem-sucedidos: ${GREEN}$SUCCESS${NC}"
echo -e "Testes falhados: ${RED}$FAILED${NC}"
echo ""

if [ $FAILED -eq 0 ]; then
    echo -e "${GREEN}✓ TODOS OS TESTES PASSARAM!${NC}"
    echo ""
    echo "A migração para Oracle foi implementada com sucesso!"
    echo ""
    echo "Próximos passos:"
    echo "1. Configure as variáveis de ambiente para conexão Oracle"
    echo "2. Execute: mvn clean install"
    echo "3. Execute: mvn flyway:migrate"
    echo "4. Execute: mvn spring-boot:run"
    echo "5. Acesse: http://localhost:8080/swagger-ui.html"
    echo ""
    exit 0
else
    echo -e "${RED}✗ ALGUNS TESTES FALHARAM${NC}"
    echo ""
    echo "Por favor, verifique os itens marcados com ✗ acima."
    echo ""
    exit 1
fi
