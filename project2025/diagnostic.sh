#!/bin/bash
# Script de diagnóstico do ambiente DevContainer

echo "=========================================="
echo "  DIAGNÓSTICO DEVCONTAINER"
echo "=========================================="
echo ""

# Cores
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

check_command() {
    if command -v $1 &> /dev/null; then
        echo -e "${GREEN}✓${NC} $1 instalado"
        return 0
    else
        echo -e "${RED}✗${NC} $1 NÃO instalado"
        return 1
    fi
}

echo -e "${BLUE}[1/10]${NC} Verificando Java..."
if check_command java; then
    java -version 2>&1 | head -2
else
    echo "ERRO: Java não encontrado!"
fi
echo ""

echo -e "${BLUE}[2/10]${NC} Verificando JAVA_HOME..."
if [ -n "$JAVA_HOME" ]; then
    echo -e "${GREEN}✓${NC} JAVA_HOME definido: $JAVA_HOME"
else
    echo -e "${RED}✗${NC} JAVA_HOME não definido"
fi
echo ""

echo -e "${BLUE}[3/10]${NC} Verificando Maven..."
if check_command mvn; then
    mvn -version 2>&1 | head -2
else
    echo "ERRO: Maven não encontrado!"
    echo "Solução: Rebuild do devcontainer necessário"
fi
echo ""

echo -e "${BLUE}[4/10]${NC} Verificando Oracle Database..."
if docker ps | grep -q nutrixpert-oracle-xe; then
    echo -e "${GREEN}✓${NC} Container Oracle rodando"
    
    # Testar conexão
    if docker exec nutrixpert-oracle-xe bash -c "echo 'SELECT 1 FROM DUAL;' | sqlplus -s sys/OraclePassword123@//localhost:1521/XE as sysdba" &> /dev/null; then
        echo -e "${GREEN}✓${NC} Oracle Database respondendo"
    else
        echo -e "${YELLOW}!${NC} Oracle Database iniciando... (aguarde 2-3 min)"
    fi
else
    echo -e "${RED}✗${NC} Container Oracle não está rodando"
fi
echo ""

echo -e "${BLUE}[5/10]${NC} Verificando projeto Spring Boot..."
cd /workspaces/project2025 2>/dev/null
if [ -f pom.xml ]; then
    echo -e "${GREEN}✓${NC} pom.xml encontrado"
    
    if grep -q "spring-boot-starter-parent" pom.xml; then
        echo -e "${GREEN}✓${NC} Spring Boot parent configurado"
    else
        echo -e "${RED}✗${NC} Spring Boot parent NÃO encontrado no pom.xml"
    fi
else
    echo -e "${RED}✗${NC} pom.xml não encontrado"
fi
echo ""

echo -e "${BLUE}[6/10]${NC} Verificando dependências Maven..."
if [ -d ~/.m2/repository/org/springframework/boot ]; then
    DEPS=$(ls ~/.m2/repository/org/springframework/boot/ 2>/dev/null | wc -l)
    echo -e "${GREEN}✓${NC} $DEPS dependências Spring Boot baixadas"
else
    echo -e "${YELLOW}!${NC} Dependências não baixadas. Execute: mvn clean install"
fi
echo ""

echo -e "${BLUE}[7/10]${NC} Verificando build do projeto..."
if [ -d /workspaces/project2025/target/classes ]; then
    echo -e "${GREEN}✓${NC} Projeto compilado"
    
    if [ -f /workspaces/project2025/target/*.jar ]; then
        echo -e "${GREEN}✓${NC} JAR gerado"
    fi
else
    echo -e "${YELLOW}!${NC} Projeto não compilado. Execute: mvn clean install"
fi
echo ""

echo -e "${BLUE}[8/10]${NC} Verificando configurações VS Code..."
if [ -f /workspaces/project2025/.vscode/settings.json ]; then
    echo -e "${GREEN}✓${NC} .vscode/settings.json existe"
else
    echo -e "${YELLOW}!${NC} .vscode/settings.json não encontrado"
fi
echo ""

echo -e "${BLUE}[9/10]${NC} Verificando extensões VS Code..."
JAVA_EXTS=$(code --list-extensions 2>/dev/null | grep -i "java\|spring" | wc -l)
if [ $JAVA_EXTS -gt 0 ]; then
    echo -e "${GREEN}✓${NC} $JAVA_EXTS extensões Java/Spring instaladas"
else
    echo -e "${YELLOW}!${NC} Extensões Java/Spring não detectadas"
fi
echo ""

echo -e "${BLUE}[10/10]${NC} Verificando porta da aplicação..."
if lsof -i :8000 &> /dev/null; then
    echo -e "${GREEN}✓${NC} Porta 8000 em uso (aplicação pode estar rodando)"
else
    echo -e "${YELLOW}!${NC} Porta 8000 livre (aplicação não está rodando)"
fi
echo ""

echo "=========================================="
echo -e "  ${GREEN}DIAGNÓSTICO CONCLUÍDO${NC}"
echo "=========================================="
echo ""

# Recomendações
echo "📋 RECOMENDAÇÕES:"
echo ""

if ! command -v mvn &> /dev/null; then
    echo -e "${RED}CRÍTICO:${NC} Maven não instalado"
    echo "  Solução: Rebuild do devcontainer"
    echo "  Comando: Dev Containers: Rebuild Container"
    echo ""
fi

if [ ! -d ~/.m2/repository/org/springframework/boot ]; then
    echo -e "${YELLOW}AVISO:${NC} Dependências Maven não baixadas"
    echo "  Solução: cd /workspaces/project2025 && mvn clean install -DskipTests"
    echo ""
fi

if ! docker ps | grep -q nutrixpert-oracle-xe; then
    echo -e "${YELLOW}AVISO:${NC} Oracle Database não está rodando"
    echo "  Solução: docker start nutrixpert-oracle-xe"
    echo ""
fi

echo "Para mais informações, consulte:"
echo "  /workspaces/.devcontainer/TROUBLESHOOTING_BEANS.md"
echo ""
