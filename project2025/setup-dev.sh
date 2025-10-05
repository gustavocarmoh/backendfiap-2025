#!/bin/bash
# Script de setup do ambiente de desenvolvimento
# Este script é executado manualmente quando necessário

set -e

echo "=========================================="
echo "  NutriXpert - Setup Ambiente Dev"
echo "=========================================="
echo ""

# Cores
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Navegar para o diretório do projeto
cd /workspaces/project2025

echo -e "${YELLOW}[1/5]${NC} Verificando Java..."
java -version
echo ""

echo -e "${YELLOW}[2/5]${NC} Verificando Maven..."
mvn -version
echo ""

echo -e "${YELLOW}[3/5]${NC} Verificando Oracle Database..."
if docker ps | grep -q nutrixpert-oracle-xe; then
    echo -e "${GREEN}✓${NC} Oracle Database está rodando"
    docker exec nutrixpert-oracle-xe bash -c "echo 'SELECT 1 FROM DUAL;' | sqlplus -s sys/OraclePassword123@//localhost:1521/XE as sysdba" > /dev/null 2>&1
    if [ $? -eq 0 ]; then
        echo -e "${GREEN}✓${NC} Oracle Database está acessível"
    else
        echo -e "${RED}✗${NC} Oracle Database não está respondendo. Aguarde mais alguns minutos..."
    fi
else
    echo -e "${RED}✗${NC} Oracle Database não está rodando"
    echo "Execute: docker start nutrixpert-oracle-xe"
fi
echo ""

echo -e "${YELLOW}[4/5]${NC} Limpando e instalando dependências Maven..."
mvn clean install -DskipTests
echo ""

echo -e "${YELLOW}[5/5]${NC} Verificando Spring Boot..."
if mvn help:evaluate -Dexpression=project.parent.artifactId -q -DforceStdout | grep -q spring-boot; then
    echo -e "${GREEN}✓${NC} Spring Boot detectado no projeto"
else
    echo -e "${YELLOW}!${NC} Spring Boot não detectado como parent (verificar pom.xml)"
fi
echo ""

echo "=========================================="
echo -e "${GREEN}  Setup Concluído!${NC}"
echo "=========================================="
echo ""
echo "Comandos úteis:"
echo "  mvn spring-boot:run          # Iniciar aplicação"
echo "  mvn test                     # Executar testes"
echo "  mvn flyway:migrate           # Executar migrations"
echo "  mvn flyway:info              # Ver status migrations"
echo ""
echo "  ./validate-oracle-migration.sh  # Validar instalação Oracle"
echo ""
echo "Aguarde alguns segundos para o VS Code indexar o projeto..."
