#!/bin/bash
# Quick Fix: Spring Boot Beans não aparecem no VS Code

echo "🔧 QUICK FIX - Spring Boot Beans"
echo ""

# Verificar Maven
if ! command -v mvn &> /dev/null; then
    echo "❌ PROBLEMA: Maven não instalado"
    echo ""
    echo "SOLUÇÃO:"
    echo "  1. No VS Code: Ctrl/Cmd+Shift+P"
    echo "  2. Digite: Dev Containers: Rebuild Container"
    echo "  3. Aguarde 5-10 minutos"
    echo ""
    echo "O Dockerfile foi corrigido para instalar Maven automaticamente."
    exit 1
fi

echo "✅ Maven instalado"
echo ""

# Navegar para projeto
cd /workspaces/project2025

# Verificar dependências
echo "📦 Baixando dependências Maven..."
mvn clean install -DskipTests

if [ $? -eq 0 ]; then
    echo ""
    echo "✅ Dependências instaladas com sucesso!"
    echo ""
    echo "📋 PRÓXIMOS PASSOS:"
    echo ""
    echo "1. Reiniciar Java Language Server:"
    echo "   Ctrl/Cmd+Shift+P → 'Java: Clean Java Language Server Workspace'"
    echo ""
    echo "2. Recarregar VS Code:"
    echo "   Ctrl/Cmd+Shift+P → 'Developer: Reload Window'"
    echo ""
    echo "3. Aguardar indexação (1-2 minutos)"
    echo ""
    echo "4. Abrir qualquer Controller e verificar autocomplete em @Autowired"
    echo ""
    echo "5. Verificar Spring Boot Dashboard (ícone da mola no lado esquerdo)"
    echo ""
    echo "✅ Se ainda não funcionar, execute: ./diagnostic.sh"
else
    echo ""
    echo "❌ ERRO ao instalar dependências"
    echo ""
    echo "Verifique:"
    echo "  - Conexão com internet"
    echo "  - Espaço em disco"
    echo "  - pom.xml está correto"
    echo ""
    echo "Execute ./diagnostic.sh para mais informações"
fi
