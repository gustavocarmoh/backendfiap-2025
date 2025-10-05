#!/bin/bash
# Script para iniciar a aplicação com as configurações corretas do Oracle

export SPRING_DATASOURCE_URL="jdbc:oracle:thin:@oracle:1521/XEPDB1"
export SPRING_DATASOURCE_USERNAME="nutrixpert"
export SPRING_DATASOURCE_PASSWORD="NutriXpert2025!"
export SPRING_PROFILES_ACTIVE="dev"

echo "=========================================="
echo "  Iniciando NutriXpert API"
echo "=========================================="
echo "Database URL: $SPRING_DATASOURCE_URL"
echo "Username: $SPRING_DATASOURCE_USERNAME"
echo "Profile: $SPRING_PROFILES_ACTIVE"
echo "=========================================="
echo ""

cd /workspaces/project2025
mvn spring-boot:run
