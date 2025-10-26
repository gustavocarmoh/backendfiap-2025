# 📡 API Endpoints - NutriXpert

Documentação completa de todos os endpoints da API NutriXpert com exemplos de uso e comandos curl.

## 📋 Base URL

```
http://localhost:8000
```

## 🔐 Autenticação

A maioria dos endpoints requer autenticação via JWT token no header:
```bash
Authorization: Bearer {seu-jwt-token}
```

---

## 🔑 Autenticação (`/api/v1/auth`)

### 1. Health Check
**Endpoint:** `GET /api/v1/auth/health`  
**Descrição:** Verifica se o serviço de autenticação está funcionando  
**Autenticação:** Não requerida  

```bash
curl -X GET http://localhost:8000/api/v1/auth/health
```

**Resposta:**
```json
{
  "status": "OK",
  "message": "Auth service is running"
}
```

### 2. Registrar Usuário
**Endpoint:** `POST /api/v1/auth/register`  
**Descrição:** Cria uma nova conta de usuário  
**Autenticação:** Não requerida  

```bash
curl -X POST http://localhost:8000/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "usuario@exemplo.com",
    "password": "senha123",
    "fullName": "João Silva"
  }'
```

**Resposta:** `201 Created`

### 3. Login
**Endpoint:** `POST /api/v1/auth/login`  
**Descrição:** Autentica usuário e retorna JWT token  
**Autenticação:** Não requerida  

```bash
curl -X POST http://localhost:8000/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "usuario@exemplo.com",
    "password": "senha123"
  }'
```

**Resposta:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "type": "Bearer",
  "email": "usuario@exemplo.com",
  "expiresIn": 86400000
}
```

### 4. Perfil do Usuário
**Endpoint:** `GET /api/v1/auth/profile`  
**Descrição:** Retorna informações do usuário autenticado  
**Autenticação:** Bearer Token  

```bash
curl -X GET http://localhost:8000/api/v1/auth/profile \
  -H "Authorization: Bearer {seu-token}"
```

**Resposta:**
```json
{
  "email": "usuario@exemplo.com",
  "message": "You are authenticated!",
  "timestamp": 1693478400000
}
```

---

## 👤 Usuários (`/api/v1/users`)

### 5. Buscar Usuário por ID
**Endpoint:** `GET /api/v1/users/{id}`  
**Descrição:** Busca informações de um usuário específico  
**Autenticação:** Bearer Token  

```bash
curl -X GET http://localhost:8000/api/v1/users/550e8400-e29b-41d4-a716-446655440000 \
  -H "Authorization: Bearer {seu-token}"
```

### 6. Atualizar Usuário
**Endpoint:** `PUT /api/v1/users/{id}`  
**Descrição:** Atualiza informações de um usuário  
**Autenticação:** Bearer Token  

```bash
curl -X PUT http://localhost:8000/api/v1/users/550e8400-e29b-41d4-a716-446655440000 \
  -H "Authorization: Bearer {seu-token}" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "novo@email.com",
    "fullName": "Novo Nome"
  }'
```

### 7. Deletar Usuário (Admin)
**Endpoint:** `DELETE /api/v1/users/{id}`  
**Descrição:** Remove um usuário do sistema  
**Autenticação:** Admin role  

```bash
curl -X DELETE http://localhost:8000/api/v1/users/550e8400-e29b-41d4-a716-446655440000 \
  -H "Authorization: Bearer {admin-token}"
```

### 8. Promover para Admin (Admin)
**Endpoint:** `PUT /api/v1/users/{id}/promote`  
**Descrição:** Promove usuário para administrador  
**Autenticação:** Admin role  

```bash
curl -X PUT http://localhost:8000/api/v1/users/550e8400-e29b-41d4-a716-446655440000/promote \
  -H "Authorization: Bearer {admin-token}"
```

### 9. Atualizar Meu Perfil
**Endpoint:** `PATCH /api/v1/users/me`  
**Descrição:** Atualiza informações do próprio perfil  
**Autenticação:** Bearer Token  

```bash
curl -X PATCH http://localhost:8000/api/v1/users/me \
  -H "Authorization: Bearer {seu-token}" \
  -H "Content-Type: application/json" \
  -d '{
    "fullName": "Novo Nome Completo"
  }'
```

---

## 📊 Informações do Usuário (`/api/v1/user`)

### 10. Minhas Informações Completas
**Endpoint:** `GET /api/v1/user/me`  
**Descrição:** Retorna informações completas do usuário logado  
**Autenticação:** Bearer Token  

```bash
curl -X GET http://localhost:8000/api/v1/user/me \
  -H "Authorization: Bearer {seu-token}"
```

**Resposta:**
```json
{
  "email": "usuario@exemplo.com",
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "hasActivePlan": true,
  "activePlanName": "Plano Premium",
  "activePlanId": "plan-uuid",
  "hasProfilePhoto": true,
  "profilePhotoUrl": "/api/v1/user/photo/my-photo"
}
```

### 11. Meu Plano Ativo
**Endpoint:** `GET /api/v1/user/plan`  
**Descrição:** Retorna informações do plano ativo do usuário  
**Autenticação:** Bearer Token  

```bash
curl -X GET http://localhost:8000/api/v1/user/plan \
  -H "Authorization: Bearer {seu-token}"
```

### 12. Perfil Completo
**Endpoint:** `GET /api/v1/user/profile`  
**Descrição:** Retorna perfil completo incluindo foto  
**Autenticação:** Bearer Token  

```bash
curl -X GET http://localhost:8000/api/v1/user/profile \
  -H "Authorization: Bearer {seu-token}"
```

---

## 📸 Fotos de Usuário (`/api/v1/user/photo`)

### 13. Upload de Foto
**Endpoint:** `POST /api/v1/user/photo/upload`  
**Descrição:** Faz upload da foto de perfil  
**Autenticação:** Bearer Token  

```bash
curl -X POST http://localhost:8000/api/v1/user/photo/upload \
  -H "Authorization: Bearer {seu-token}" \
  -F "file=@/caminho/para/foto.jpg"
```

**Resposta:**
```json
{
  "success": true,
  "message": "Foto enviada com sucesso",
  "photoUrl": "/api/v1/user/photo/550e8400-e29b-41d4-a716-446655440000",
  "fileName": "foto.jpg",
  "fileSize": 2048000
}
```

### 14. Minha Foto
**Endpoint:** `GET /api/v1/user/photo/my-photo`  
**Descrição:** Retorna a foto de perfil do usuário logado  
**Autenticação:** Bearer Token  

```bash
curl -X GET http://localhost:8000/api/v1/user/photo/my-photo \
  -H "Authorization: Bearer {seu-token}" \
  --output minha-foto.jpg
```

### 15. Foto por ID
**Endpoint:** `GET /api/v1/user/photo/{photoId}`  
**Descrição:** Retorna foto específica pelo ID (público)  
**Autenticação:** Não requerida  

```bash
curl -X GET http://localhost:8000/api/v1/user/photo/550e8400-e29b-41d4-a716-446655440000 \
  --output foto.jpg
```

### 16. Deletar Minha Foto
**Endpoint:** `DELETE /api/v1/user/photo/my-photo`  
**Descrição:** Remove a foto de perfil do usuário  
**Autenticação:** Bearer Token  

```bash
curl -X DELETE http://localhost:8000/api/v1/user/photo/my-photo \
  -H "Authorization: Bearer {seu-token}"
```

### 17. Status da Foto
**Endpoint:** `GET /api/v1/user/photo/status`  
**Descrição:** Verifica se usuário possui foto e retorna metadados  
**Autenticação:** Bearer Token  

```bash
curl -X GET http://localhost:8000/api/v1/user/photo/status \
  -H "Authorization: Bearer {seu-token}"
```

### 18. Estatísticas de Fotos (Admin)
**Endpoint:** `GET /api/v1/user/photo/admin/stats`  
**Descrição:** Estatísticas de armazenamento de fotos  
**Autenticação:** Admin role  

```bash
curl -X GET http://localhost:8000/api/v1/user/photo/admin/stats \
  -H "Authorization: Bearer {admin-token}"
```

---

## 📋 Planos de Assinatura (`/api/v1/subscription-plans`)

### 19. Listar Planos Ativos
**Endpoint:** `GET /api/v1/subscription-plans`  
**Descrição:** Lista todos os planos de assinatura ativos  
**Autenticação:** Não requerida  

```bash
curl -X GET http://localhost:8000/api/v1/subscription-plans
```

**Resposta:**
```json
[
  {
    "id": "plan-uuid",
    "name": "Plano Básico",
    "description": "Ideal para iniciantes",
    "price": 19.90,
    "nutritionPlanLimit": 5,
    "isActive": true,
    "createdAt": "2024-01-15T10:30:00",
    "features": ["5 planos nutricionais", "Suporte básico"]
  }
]
```

### 20. Listar Todos os Planos (Admin)
**Endpoint:** `GET /api/v1/subscription-plans/all`  
**Descrição:** Lista todos os planos incluindo inativos  
**Autenticação:** Admin role  

```bash
curl -X GET http://localhost:8000/api/v1/subscription-plans/all \
  -H "Authorization: Bearer {admin-token}"
```

### 21. Buscar Plano por ID
**Endpoint:** `GET /api/v1/subscription-plans/{id}`  
**Descrição:** Retorna plano específico pelo ID  
**Autenticação:** Não requerida  

```bash
curl -X GET http://localhost:8000/api/v1/subscription-plans/550e8400-e29b-41d4-a716-446655440000
```

### 22. Criar Plano (Admin)
**Endpoint:** `POST /api/v1/subscription-plans`  
**Descrição:** Cria novo plano de assinatura  
**Autenticação:** Admin role  

```bash
curl -X POST http://localhost:8000/api/v1/subscription-plans \
  -H "Authorization: Bearer {admin-token}" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Plano Premium",
    "description": "Para usuários avançados",
    "price": 49.90,
    "nutritionPlanLimit": 15
  }'
```

### 23. Atualizar Plano (Admin)
**Endpoint:** `PUT /api/v1/subscription-plans/{id}`  
**Descrição:** Atualiza plano existente  
**Autenticação:** Admin role  

```bash
curl -X PUT http://localhost:8000/api/v1/subscription-plans/550e8400-e29b-41d4-a716-446655440000 \
  -H "Authorization: Bearer {admin-token}" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Plano Premium Atualizado",
    "price": 59.90,
    "nutritionPlanLimit": 20
  }'
```

### 24. Desativar Plano (Admin)
**Endpoint:** `PATCH /api/v1/subscription-plans/{id}/deactivate`  
**Descrição:** Desativa um plano  
**Autenticação:** Admin role  

```bash
curl -X PATCH http://localhost:8000/api/v1/subscription-plans/550e8400-e29b-41d4-a716-446655440000/deactivate \
  -H "Authorization: Bearer {admin-token}"
```

### 25. Ativar Plano (Admin)
**Endpoint:** `PATCH /api/v1/subscription-plans/{id}/activate`  
**Descrição:** Ativa um plano  
**Autenticação:** Admin role  

```bash
curl -X PATCH http://localhost:8000/api/v1/subscription-plans/550e8400-e29b-41d4-a716-446655440000/activate \
  -H "Authorization: Bearer {admin-token}"
```

### 26. Deletar Plano (Admin)
**Endpoint:** `DELETE /api/v1/subscription-plans/{id}`  
**Descrição:** Remove um plano permanentemente  
**Autenticação:** Admin role  

```bash
curl -X DELETE http://localhost:8000/api/v1/subscription-plans/550e8400-e29b-41d4-a716-446655440000 \
  -H "Authorization: Bearer {admin-token}"
```

---

## 🔄 Assinaturas (`/api/v1/subscriptions`)

### 27. Criar Assinatura
**Endpoint:** `POST /api/v1/subscriptions`  
**Descrição:** Cria nova assinatura para o usuário  
**Autenticação:** Bearer Token  

```bash
curl -X POST http://localhost:8000/api/v1/subscriptions \
  -H "Authorization: Bearer {seu-token}" \
  -H "Content-Type: application/json" \
  -d '{
    "subscriptionPlanId": "plan-uuid"
  }'
```

**Resposta:**
```json
{
  "id": "subscription-uuid",
  "user": {
    "id": "user-uuid",
    "email": "usuario@exemplo.com"
  },
  "plan": {
    "id": "plan-uuid",
    "name": "Plano Básico",
    "price": 19.90
  },
  "status": "PENDING",
  "createdAt": "2024-01-15T10:30:00"
}
```

### 28. Minhas Assinaturas
**Endpoint:** `GET /api/v1/subscriptions`  
**Descrição:** Lista assinaturas do usuário logado  
**Autenticação:** Bearer Token  

```bash
curl -X GET http://localhost:8000/api/v1/subscriptions \
  -H "Authorization: Bearer {seu-token}"
```

### 29. Buscar Assinatura por ID
**Endpoint:** `GET /api/v1/subscriptions/{id}`  
**Descrição:** Retorna assinatura específica  
**Autenticação:** Bearer Token  

```bash
curl -X GET http://localhost:8000/api/v1/subscriptions/550e8400-e29b-41d4-a716-446655440000 \
  -H "Authorization: Bearer {seu-token}"
```

### 30. Todas as Assinaturas (Admin)
**Endpoint:** `GET /api/v1/subscriptions/all`  
**Descrição:** Lista todas as assinaturas do sistema  
**Autenticação:** Admin role  

```bash
curl -X GET http://localhost:8000/api/v1/subscriptions/all \
  -H "Authorization: Bearer {admin-token}"
```

### 31. Assinaturas Pendentes (Admin)
**Endpoint:** `GET /api/v1/subscriptions/pending`  
**Descrição:** Lista assinaturas aguardando aprovação  
**Autenticação:** Admin role  

```bash
curl -X GET http://localhost:8000/api/v1/subscriptions/pending \
  -H "Authorization: Bearer {admin-token}"
```

### 32. Assinaturas por Status (Admin)
**Endpoint:** `GET /api/v1/subscriptions/status/{status}`  
**Descrição:** Lista assinaturas por status específico  
**Autenticação:** Admin role  

```bash
# Status: PENDING, ACTIVE, CANCELLED, EXPIRED
curl -X GET http://localhost:8000/api/v1/subscriptions/status/ACTIVE \
  -H "Authorization: Bearer {admin-token}"
```

### 33. Atualizar Status (Admin)
**Endpoint:** `PATCH /api/v1/subscriptions/{id}/status`  
**Descrição:** Atualiza status de uma assinatura  
**Autenticação:** Admin role  

```bash
curl -X PATCH http://localhost:8000/api/v1/subscriptions/550e8400-e29b-41d4-a716-446655440000/status \
  -H "Authorization: Bearer {admin-token}" \
  -H "Content-Type: application/json" \
  -d '{
    "status": "ACTIVE"
  }'
```

### 34. Aprovar Assinatura (Admin)
**Endpoint:** `PATCH /api/v1/subscriptions/{id}/approve`  
**Descrição:** Aprova uma assinatura pendente  
**Autenticação:** Admin role  

```bash
curl -X PATCH http://localhost:8000/api/v1/subscriptions/550e8400-e29b-41d4-a716-446655440000/approve \
  -H "Authorization: Bearer {admin-token}"
```

### 35. Rejeitar Assinatura (Admin)
**Endpoint:** `PATCH /api/v1/subscriptions/{id}/reject`  
**Descrição:** Rejeita uma assinatura pendente  
**Autenticação:** Admin role  

```bash
curl -X PATCH http://localhost:8000/api/v1/subscriptions/550e8400-e29b-41d4-a716-446655440000/reject \
  -H "Authorization: Bearer {admin-token}"
```

### 36. Cancelar Assinatura
**Endpoint:** `PATCH /api/v1/subscriptions/{id}/cancel`  
**Descrição:** Cancela uma assinatura  
**Autenticação:** Bearer Token ou Admin  

```bash
curl -X PATCH http://localhost:8000/api/v1/subscriptions/550e8400-e29b-41d4-a716-446655440000/cancel \
  -H "Authorization: Bearer {seu-token}"
```

### 37. Contar Assinaturas Ativas
**Endpoint:** `GET /api/v1/subscriptions/count/active`  
**Descrição:** Retorna número de assinaturas ativas do usuário  
**Autenticação:** Bearer Token  

```bash
curl -X GET http://localhost:8000/api/v1/subscriptions/count/active \
  -H "Authorization: Bearer {seu-token}"
```

---

## 🥗 Planos Nutricionais (`/api/nutrition-plans`)

### 38. Criar Plano Nutricional
**Endpoint:** `POST /api/nutrition-plans`  
**Descrição:** Cria novo plano nutricional  
**Autenticação:** Bearer Token  

```bash
curl -X POST http://localhost:8000/api/nutrition-plans \
  -H "Authorization: Bearer {seu-token}" \
  -H "Content-Type: application/json" \
  -d '{
    "date": "2024-01-15",
    "targetCalories": 2000,
    "meals": [
      {
        "name": "Café da Manhã",
        "targetCalories": 400,
        "foods": [
          {
            "name": "Aveia",
            "quantity": 50,
            "unit": "gramas",
            "calories": 190
          }
        ]
      }
    ]
  }'
```

### 39. Buscar Plano por ID
**Endpoint:** `GET /api/nutrition-plans/{planId}`  
**Descrição:** Retorna plano nutricional específico  
**Autenticação:** Bearer Token  

```bash
curl -X GET http://localhost:8000/api/nutrition-plans/550e8400-e29b-41d4-a716-446655440000 \
  -H "Authorization: Bearer {seu-token}"
```

### 40. Plano de Hoje
**Endpoint:** `GET /api/nutrition-plans/today`  
**Descrição:** Retorna plano nutricional do dia atual  
**Autenticação:** Bearer Token  

```bash
curl -X GET http://localhost:8000/api/nutrition-plans/today \
  -H "Authorization: Bearer {seu-token}"
```

### 41. Planos da Semana
**Endpoint:** `GET /api/nutrition-plans/week`  
**Descrição:** Retorna planos da semana atual  
**Autenticação:** Bearer Token  

```bash
curl -X GET http://localhost:8000/api/nutrition-plans/week \
  -H "Authorization: Bearer {seu-token}"
```

### 42. Planos de Semana Específica
**Endpoint:** `GET /api/nutrition-plans/week/{date}`  
**Descrição:** Retorna planos da semana que contém a data  
**Autenticação:** Bearer Token  

```bash
curl -X GET http://localhost:8000/api/nutrition-plans/week/2024-01-15 \
  -H "Authorization: Bearer {seu-token}"
```

### 43. Listar Planos (Paginado)
**Endpoint:** `GET /api/nutrition-plans`  
**Descrição:** Lista planos do usuário com paginação  
**Autenticação:** Bearer Token  

```bash
curl -X GET "http://localhost:8000/api/nutrition-plans?page=0&size=10" \
  -H "Authorization: Bearer {seu-token}"
```

### 44. Planos Pendentes
**Endpoint:** `GET /api/nutrition-plans/pending`  
**Descrição:** Retorna planos não completados  
**Autenticação:** Bearer Token  

```bash
curl -X GET http://localhost:8000/api/nutrition-plans/pending \
  -H "Authorization: Bearer {seu-token}"
```

### 45. Planos Futuros
**Endpoint:** `GET /api/nutrition-plans/future`  
**Descrição:** Retorna planos programados para o futuro  
**Autenticação:** Bearer Token  

```bash
curl -X GET http://localhost:8000/api/nutrition-plans/future \
  -H "Authorization: Bearer {seu-token}"
```

### 46. Atualizar Plano
**Endpoint:** `PUT /api/nutrition-plans/{planId}`  
**Descrição:** Atualiza plano nutricional existente  
**Autenticação:** Bearer Token  

```bash
curl -X PUT http://localhost:8000/api/nutrition-plans/550e8400-e29b-41d4-a716-446655440000 \
  -H "Authorization: Bearer {seu-token}" \
  -H "Content-Type: application/json" \
  -d '{
    "targetCalories": 2200,
    "meals": [...]
  }'
```

### 47. Alternar Conclusão
**Endpoint:** `PATCH /api/nutrition-plans/{planId}/completion`  
**Descrição:** Marca plano como completo/incompleto  
**Autenticação:** Bearer Token  

```bash
curl -X PATCH http://localhost:8000/api/nutrition-plans/550e8400-e29b-41d4-a716-446655440000/completion \
  -H "Authorization: Bearer {seu-token}"
```

### 48. Deletar Plano
**Endpoint:** `DELETE /api/nutrition-plans/{planId}`  
**Descrição:** Remove plano nutricional  
**Autenticação:** Bearer Token  

```bash
curl -X DELETE http://localhost:8000/api/nutrition-plans/550e8400-e29b-41d4-a716-446655440000 \
  -H "Authorization: Bearer {seu-token}"
```

---

## 💬 Chat (`/api/chat`)

### 49. Enviar Mensagem
**Endpoint:** `POST /api/chat/send`  
**Descrição:** Envia mensagem no chat  
**Autenticação:** Bearer Token  

```bash
curl -X POST http://localhost:8000/api/chat/send \
  -H "Authorization: Bearer {seu-token}" \
  -H "Content-Type: application/json" \
  -d '{
    "message": "Preciso de ajuda com minha dieta",
    "sessionId": "optional-session-uuid"
  }'
```

**Resposta:**
```json
{
  "id": "message-uuid",
  "sessionId": "session-uuid",
  "userMessage": "Preciso de ajuda com minha dieta",
  "botResponse": "Olá! Como posso ajudar com sua dieta?",
  "timestamp": "2024-01-15T10:30:00",
  "processed": true
}
```

### 50. Minhas Sessões
**Endpoint:** `GET /api/chat/sessions`  
**Descrição:** Lista sessões de chat do usuário  
**Autenticação:** Bearer Token  

```bash
curl -X GET http://localhost:8000/api/chat/sessions \
  -H "Authorization: Bearer {seu-token}"
```

### 51. Histórico da Sessão
**Endpoint:** `GET /api/chat/sessions/{sessionId}`  
**Descrição:** Retorna histórico completo de uma sessão  
**Autenticação:** Bearer Token  

```bash
curl -X GET http://localhost:8000/api/chat/sessions/550e8400-e29b-41d4-a716-446655440000 \
  -H "Authorization: Bearer {seu-token}"
```

### 52. Mensagens Paginadas
**Endpoint:** `GET /api/chat/messages`  
**Descrição:** Lista mensagens com paginação  
**Autenticação:** Bearer Token  

```bash
curl -X GET "http://localhost:8000/api/chat/messages?page=0&size=20&sort=createdAt,desc" \
  -H "Authorization: Bearer {seu-token}"
```

### 53. Estatísticas do Chat (Admin)
**Endpoint:** `GET /api/chat/admin/statistics`  
**Descrição:** Estatísticas de uso do chat  
**Autenticação:** Admin role  

```bash
curl -X GET http://localhost:8000/api/chat/admin/statistics \
  -H "Authorization: Bearer {admin-token}"
```

### 54. Status do Chat
**Endpoint:** `GET /api/chat/status`  
**Descrição:** Status atual do sistema de chat  
**Autenticação:** Bearer Token  

```bash
curl -X GET http://localhost:8000/api/chat/status \
  -H "Authorization: Bearer {seu-token}"
```

---

## 📊 Dashboard Admin (`/api/admin/dashboard`)

### 55. Dashboard Completo (Admin)
**Endpoint:** `GET /api/admin/dashboard`  
**Descrição:** Métricas completas do dashboard  
**Autenticação:** Admin role  

```bash
curl -X GET http://localhost:8000/api/admin/dashboard \
  -H "Authorization: Bearer {admin-token}"
```

**Resposta:**
```json
{
  "totalUsers": 1250,
  "activeSubscriptions": 850,
  "totalNutritionPlans": 5420,
  "monthlyRevenue": 25000.00,
  "growthRate": 15.2,
  "chartData": {
    "userRegistrations": [...],
    "subscriptionTrends": [...],
    "revenueByMonth": [...]
  }
}
```

### 56. Métricas de Crescimento (Admin)
**Endpoint:** `GET /api/admin/dashboard/growth`  
**Descrição:** Métricas de crescimento por período  
**Autenticação:** Admin role  

```bash
curl -X GET "http://localhost:8000/api/admin/dashboard/growth?startDate=2024-01-01&endDate=2024-01-31&period=daily" \
  -H "Authorization: Bearer {admin-token}"
```

### 57. Últimos 30 Dias (Admin)
**Endpoint:** `GET /api/admin/dashboard/growth/last-30-days`  
**Descrição:** Crescimento dos últimos 30 dias  
**Autenticação:** Admin role  

```bash
curl -X GET http://localhost:8000/api/admin/dashboard/growth/last-30-days \
  -H "Authorization: Bearer {admin-token}"
```

### 58. Últimos 12 Meses (Admin)
**Endpoint:** `GET /api/admin/dashboard/growth/last-12-months`  
**Descrição:** Crescimento dos últimos 12 meses  
**Autenticação:** Admin role  

```bash
curl -X GET http://localhost:8000/api/admin/dashboard/growth/last-12-months \
  -H "Authorization: Bearer {admin-token}"
```

### 59. Crescimento do Ano (Admin)
**Endpoint:** `GET /api/admin/dashboard/growth/this-year`  
**Descrição:** Métricas desde o início do ano  
**Autenticação:** Admin role  

```bash
curl -X GET http://localhost:8000/api/admin/dashboard/growth/this-year \
  -H "Authorization: Bearer {admin-token}"
```

### 60. Resumo Executivo (Admin)
**Endpoint:** `GET /api/admin/dashboard/summary`  
**Descrição:** Versão resumida do dashboard  
**Autenticação:** Admin role  

```bash
curl -X GET http://localhost:8000/api/admin/dashboard/summary \
  -H "Authorization: Bearer {admin-token}"
```

---

## 🏪 Fornecedores de Serviços (`/api/v1/service-providers`)

### 61. Fornecedores Ativos
**Endpoint:** `GET /api/v1/service-providers/active`  
**Descrição:** Lista fornecedores ativos com coordenadas  
**Autenticação:** Não requerida  

```bash
curl -X GET http://localhost:8000/api/v1/service-providers/active
```

**Resposta:**
```json
[
  {
    "id": "provider-uuid",
    "name": "Supermercado Central",
    "address": "Rua das Flores, 123",
    "latitude": -23.5505,
    "longitude": -46.6333,
    "phone": "(11) 1234-5678",
    "category": "SUPERMARKET",
    "isActive": true
  }
]
```

---

## 🖼️ Recursos de Fotos (`/api/photos`)

### 62. Foto Padrão
**Endpoint:** `GET /api/photos/default`  
**Descrição:** Retorna foto padrão do sistema  
**Autenticação:** Não requerida  

```bash
curl -X GET http://localhost:8000/api/photos/default \
  --output foto-padrao.jpg
```

---

## 📖 Documentação (`/api/v1/docs`)

### 63. Informações da API
**Endpoint:** `GET /api/v1/docs`  
**Descrição:** Informações básicas da API e links úteis  
**Autenticação:** Não requerida  

```bash
curl -X GET http://localhost:8000/api/v1/docs
```

### 64. Redirecionar para Swagger
**Endpoint:** `GET /api/v1/docs/swagger`  
**Descrição:** Redireciona para interface Swagger UI  
**Autenticação:** Não requerida  

```bash
curl -X GET http://localhost:8000/api/v1/docs/swagger
```

### 65. Status da Aplicação
**Endpoint:** `GET /api/v1/docs/health`  
**Descrição:** Status de saúde da aplicação  
**Autenticação:** Não requerida  

```bash
curl -X GET http://localhost:8000/api/v1/docs/health
```

---

## 🔧 Endpoints de Sistema

### 66. Health Check Actuator
**Endpoint:** `GET /actuator/health`  
**Descrição:** Health check detalhado do Spring Boot  
**Autenticação:** Não requerida  

```bash
curl -X GET http://localhost:8000/actuator/health
```

### 67. Informações Flyway
**Endpoint:** `GET /actuator/flyway`  
**Descrição:** Status das migrações do banco  
**Autenticação:** Não requerida  

```bash
curl -X GET http://localhost:8000/actuator/flyway
```

### 68. Métricas da Aplicação
**Endpoint:** `GET /actuator/metrics`  
**Descrição:** Métricas gerais da aplicação  
**Autenticação:** Não requerida  

```bash
curl -X GET http://localhost:8000/actuator/metrics
```

### 69. Swagger UI
**Endpoint:** `GET /swagger-ui.html`  
**Descrição:** Interface visual da documentação  
**Autenticação:** Não requerida  

```bash
# Acessar pelo browser
http://localhost:8000/swagger-ui.html
```

### 70. OpenAPI Docs
**Endpoint:** `GET /api-docs`  
**Descrição:** Especificação OpenAPI em JSON  
**Autenticação:** Não requerida  

```bash
curl -X GET http://localhost:8000/api-docs
```

---

## 📋 Resumo de Códigos de Status

| Código | Significado | Quando Ocorre |
|--------|-------------|---------------|
| **200** | OK | Operação realizada com sucesso |
| **201** | Created | Recurso criado com sucesso |
| **204** | No Content | Operação realizada, sem conteúdo retornado |
| **400** | Bad Request | Dados inválidos ou malformados |
| **401** | Unauthorized | Token inválido ou expirado |
| **403** | Forbidden | Sem permissão para acessar o recurso |
| **404** | Not Found | Recurso não encontrado |
| **409** | Conflict | Conflito com estado atual (ex: email já existe) |
| **500** | Internal Error | Erro interno do servidor |

## 🔐 Níveis de Autenticação

| Nível | Descrição | Endpoints |
|-------|-----------|-----------|
| **Público** | Sem autenticação | Health checks, planos ativos, swagger |
| **Usuário** | Token JWT válido | Perfil, planos nutricionais, fotos |
| **Admin** | Role ADMIN | Dashboard, gerenciar planos, estatísticas |

## 🛠️ Exemplos de Scripts

### Script de Login e Uso
```bash
#!/bin/bash

# 1. Fazer login
TOKEN=$(curl -s -X POST http://localhost:8000/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"usuario@exemplo.com","password":"senha123"}' \
  | jq -r '.token')

# 2. Usar token para acessar API
curl -H "Authorization: Bearer $TOKEN" \
  http://localhost:8000/api/v1/user/me

# 3. Criar plano nutricional
curl -X POST http://localhost:8000/api/nutrition-plans \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "date": "2024-01-15",
    "targetCalories": 2000,
    "meals": []
  }'
```

### Script de Testes
```bash
#!/bin/bash

BASE_URL="http://localhost:8000"

echo "Testando endpoints públicos..."
curl -s "$BASE_URL/api/v1/docs" | jq '.name'
curl -s "$BASE_URL/actuator/health" | jq '.status'

echo "Testando autenticação..."
curl -s -X POST "$BASE_URL/api/v1/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"email":"test@test.com","password":"test123"}' \
  | jq '.token'

echo "Todos os testes concluídos!"
```

---

**API NutriXpert - Documentação Completa de Endpoints 🚀**

> 💡 **Dica**: Use o Swagger UI em `/swagger-ui.html` para testar os endpoints interativamente!

---

# Endpoints Oracle PL/SQL — NutriXpert

## Functions

- `GET /api/v1/oracle/indicador-saude/{userId}?dias=30`  
  → Calcula score de saúde do usuário (function)

- `GET /api/v1/oracle/relatorio-nutricao/{userId}`  
  → Gera relatório nutricional formatado (function)

## Procedures

- `POST /api/v1/oracle/alertas-nutricionais/{userId}?dias=7`  
  → Registra alertas nutricionais automáticos (procedure)

- `POST /api/v1/oracle/relatorio-consumo/{userId}`  
  → Gera relatório semanal de consumo (procedure)

- `GET /api/v1/oracle/planos-dinamicos?where=plan_date >= SYSDATE - 7`  
  → Consulta dinâmica de planos nutricionais (procedure com Dynamic SQL)

## Auditoria (Triggers)

- `GET /api/v1/oracle/auditoria/planos`  
  → Consulta registros de auditoria de alterações em planos nutricionais (trigger)

---

**Valor agregado:**  
Esses endpoints permitem consumir diretamente toda a lógica Oracle (functions, procedures, triggers), garantindo flexibilidade, rastreabilidade e automação para o sistema NutriXpert.
