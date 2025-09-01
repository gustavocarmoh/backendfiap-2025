#!/bin/bash

echo "=== TESTE DE SEGURANÇA JWT ==="
echo

# Cores para output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

BASE_URL="http://localhost:8000/api/v1"

echo -e "${YELLOW}1. Testando endpoint público /auth/health...${NC}"
curl -s "$BASE_URL/auth/health" | python3 -m json.tool || echo "Erro no health check"
echo

echo -e "${YELLOW}2. Testando login para obter JWT...${NC}"
LOGIN_RESPONSE=$(curl -s -X POST "$BASE_URL/auth/login" \
    -H "Content-Type: application/json" \
    -d '{"email": "admin@nutrixpert.local", "password": "admin123"}')

if echo "$LOGIN_RESPONSE" | grep -q "token"; then
    echo -e "${GREEN}✅ Login bem-sucedido!${NC}"
    TOKEN=$(echo "$LOGIN_RESPONSE" | python3 -c "import sys, json; print(json.load(sys.stdin)['token'])" 2>/dev/null)
    echo "Token JWT obtido: ${TOKEN:0:50}..."
else
    echo -e "${RED}❌ Falha no login:${NC}"
    echo "$LOGIN_RESPONSE"
    exit 1
fi
echo

echo -e "${YELLOW}3. Testando endpoint protegido /auth/profile SEM token...${NC}"
UNAUTH_RESPONSE=$(curl -s -w "%{http_code}" "$BASE_URL/auth/profile")
if echo "$UNAUTH_RESPONSE" | grep -q "401\|403"; then
    echo -e "${GREEN}✅ Acesso negado sem token (como esperado)${NC}"
else
    echo -e "${RED}❌ Endpoint deveria estar protegido!${NC}"
    echo "$UNAUTH_RESPONSE"
fi
echo

echo -e "${YELLOW}4. Testando endpoint protegido /auth/profile COM token...${NC}"
AUTH_RESPONSE=$(curl -s -H "Authorization: Bearer $TOKEN" "$BASE_URL/auth/profile")
if echo "$AUTH_RESPONSE" | grep -q "email"; then
    echo -e "${GREEN}✅ Acesso autorizado com token!${NC}"
    echo "$AUTH_RESPONSE" | python3 -m json.tool
else
    echo -e "${RED}❌ Falha na autenticação com token:${NC}"
    echo "$AUTH_RESPONSE"
fi
echo

echo -e "${YELLOW}5. Testando endpoint ADMIN /users/promote SEM ser admin...${NC}"
echo "(Este teste requer um usuário não-admin - pulando por enquanto)"
echo

echo -e "${YELLOW}6. Testando endpoint ADMIN /users/promote COM admin...${NC}"
PROMOTE_RESPONSE=$(curl -s -w "%{http_code}" -X PUT \
    -H "Authorization: Bearer $TOKEN" \
    "$BASE_URL/users/00000000-0000-0000-0000-000000000001/promote")

echo "Response: $PROMOTE_RESPONSE"
echo

echo -e "${GREEN}=== TESTE CONCLUÍDO ===${NC}"
