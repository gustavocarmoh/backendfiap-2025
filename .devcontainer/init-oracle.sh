#!/bin/bash
# Script de inicialização para Oracle Database
# Executado automaticamente quando o container Oracle inicia pela primeira vez

set -e

echo "=========================================="
echo "  NutriXpert Oracle Initialization"
echo "=========================================="
echo ""

# Aguardar o banco estar pronto
echo "[1/5] Aguardando Oracle Database inicializar..."
sleep 30

# Função para executar SQL
run_sql() {
    local sql="$1"
    echo "$sql" | sqlplus -s sys/OraclePassword123@//localhost:1521/XEPDB1 as sysdba
}

echo "[2/5] Verificando se usuário nutrixpert já existe..."
USER_EXISTS=$(run_sql "SELECT COUNT(*) FROM dba_users WHERE username = 'NUTRIXPERT';" | grep -o '[0-9]' | head -1)

if [ "$USER_EXISTS" = "0" ]; then
    echo "[3/5] Criando usuário nutrixpert..."
    run_sql "CREATE USER nutrixpert IDENTIFIED BY NutriXpert2025!;"
    
    echo "[4/5] Concedendo permissões..."
    run_sql "GRANT CONNECT, RESOURCE TO nutrixpert;"
    run_sql "GRANT CREATE VIEW TO nutrixpert;"
    run_sql "GRANT CREATE PROCEDURE TO nutrixpert;"
    run_sql "GRANT CREATE SEQUENCE TO nutrixpert;"
    run_sql "GRANT CREATE TRIGGER TO nutrixpert;"
    run_sql "GRANT UNLIMITED TABLESPACE TO nutrixpert;"
    
    echo "[5/5] Verificando criação..."
    run_sql "SELECT username, account_status, created FROM dba_users WHERE username = 'NUTRIXPERT';"
    
    echo ""
    echo "✅ Usuário nutrixpert criado com sucesso!"
else
    echo "✅ Usuário nutrixpert já existe."
fi

echo ""
echo "=========================================="
echo "  Informações de Conexão"
echo "=========================================="
echo "Host: localhost (ou oracle no container)"
echo "Port: 1521"
echo "SID: XE"
echo "Service Name: XE"
echo ""
echo "Usuário Admin:"
echo "  Username: sys"
echo "  Password: OraclePassword123"
echo "  Role: SYSDBA"
echo ""
echo "Usuário da Aplicação:"
echo "  Username: nutrixpert"
echo "  Password: NutriXpert2025!"
echo "  Schema: NUTRIXPERT"
echo ""
echo "JDBC URL: jdbc:oracle:thin:@localhost:1521/XEPDB1"
echo "Oracle EM Express: https://localhost:5500/em"
echo ""
echo "=========================================="
echo "  Inicialização Concluída!"
echo "=========================================="
