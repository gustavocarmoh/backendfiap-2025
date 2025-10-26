#!/bin/bash
# Script para iniciar a aplicação com as configurações corretas do Oracle
set -euo pipefail

# Configurações (podem ser sobrescritas por env)
export SPRING_DATASOURCE_URL="${SPRING_DATASOURCE_URL:-jdbc:oracle:thin:@oracle:1521/XEPDB1}"
export SPRING_DATASOURCE_USERNAME="${SPRING_DATASOURCE_USERNAME:-nutrixpert}"
export SPRING_DATASOURCE_PASSWORD="${SPRING_DATASOURCE_PASSWORD:-NutriXpert2025!}"
export SPRING_PROFILES_ACTIVE="${SPRING_PROFILES_ACTIVE:-dev}"

ORACLE_HOST="${ORACLE_HOST:-oracle}"
ORACLE_PORT="${ORACLE_PORT:-1521}"
WAIT_TIMEOUT="${WAIT_TIMEOUT_SECONDS:-300}" # segundos
SLEEP_INTERVAL=5

echo "=========================================="
echo "  Iniciando NutriXpert API"
echo "=========================================="
echo "Database URL: $SPRING_DATASOURCE_URL"
echo "Username: $SPRING_DATASOURCE_USERNAME"
echo "Profile: $SPRING_PROFILES_ACTIVE"
echo "=========================================="
echo ""

cd /workspaces/project2025

# Função para checar porta TCP
check_tcp() {
  (exec 3<>/dev/tcp/"$1"/"$2") >/dev/null 2>&1 && return 0 || return 1
}

# Aguarda Oracle ficar disponível
echo "Aguardando Oracle em $ORACLE_HOST:$ORACLE_PORT (timeout ${WAIT_TIMEOUT}s)..."
SECONDS=0
while true; do
  if check_tcp "$ORACLE_HOST" "$ORACLE_PORT"; then
    echo "Porta $ORACLE_PORT aberta em $ORACLE_HOST"
    # Se sqlplus estiver disponível, testar conexão com SERVICE XEPDB1
    if command -v sqlplus >/dev/null 2>&1; then
      if echo "SELECT 1 FROM DUAL;" | sqlplus -s "$SPRING_DATASOURCE_USERNAME"/"$SPRING_DATASOURCE_PASSWORD"@"//${ORACLE_HOST}:${ORACLE_PORT}/XEPDB1" >/dev/null 2>&1; then
        echo "Teste de login bem sucedido usando sqlplus."
        break
      else
        echo "Login via sqlplus falhou — verificando se SYS está acessível para validar DB..."
        if echo "SELECT 1 FROM DUAL;" | sqlplus -s sys/OraclePassword123@"//${ORACLE_HOST}:${ORACLE_PORT}/XEPDB1" as sysdba >/dev/null 2>&1; then
          echo "Banco disponível (acesso SYS). Prosseguindo."
          break
        fi
      fi
    else
      # sem sqlplus, apenas prosseguir porque porta aberta
      break
    fi
  fi

  if [ "$SECONDS" -ge "$WAIT_TIMEOUT" ]; then
    echo "Timeout aguardando Oracle ($WAIT_TIMEOUT segundos). Abortando."
    exit 1
  fi

  sleep "$SLEEP_INTERVAL"
done

# Executar migrações Flyway antes de subir a aplicação (dev)
echo "Executando Flyway migrations (dev)..."
mvn -q -DskipTests flyway:migrate

echo "Iniciando aplicação Spring Boot..."
mvn -Dspring-boot.run.profiles="$SPRING_PROFILES_ACTIVE" spring-boot:run
