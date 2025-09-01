# 📚 Guia do SpringDoc OpenAPI - NutriXpert API

## 🎯 Visão Geral

Este guia explica como utilizar o **SpringDoc OpenAPI 3** para testar e explorar a API do NutriXpert de forma interativa. O SpringDoc é a biblioteca moderna e recomendada para documentação de APIs Spring Boot 3.x.

## 🚀 Acessando o Swagger UI

### Métodos de Acesso (Sem Autenticação Necessária)

✅ **O Swagger UI é totalmente público e não requer login para acesso!**

1. **Direto via Swagger UI:**
   ```
   http://localhost:8000/swagger-ui.html
   ```

2. **Via endpoint de documentação:**
   ```
   http://localhost:8000/api/v1/docs/swagger?redirect=true
   ```

3. **Via endpoint direto:**
   ```
   http://localhost:8000/api/v1/docs/swagger-ui
   ```

### 🆓 Acesso Público
- **Não é necessário fazer login** para acessar o Swagger UI
- **Não é necessário token** para visualizar a documentação
- Você pode explorar todos os endpoints e suas especificações livremente

## 🗂️ Grupos da API

O SpringDoc organiza automaticamente a API em grupos lógicos:

### 📱 **Grupos Disponíveis:**
1. **🔓 Endpoints Públicos** - Autenticação e documentação
2. **👤 Gestão de Usuários** - Perfis e dados pessoais
3. **💳 Sistema de Assinaturas** - Planos e assinaturas
4. **🥗 Planos Nutricionais** - Alimentação personalizada
5. **💬 Sistema de Chat** - Mensagens e conversas
6. **👥 Prestadores de Serviços** - Rede de profissionais
7. **🎛️ Dashboard Administrativo** - Métricas e gestão

### 🔄 **Como usar os grupos:**
1. Acesse `/swagger-ui.html`
2. Use o dropdown **"Select a definition"** no topo
3. Escolha o grupo que deseja explorar
4. Visualize apenas os endpoints daquele grupo

## 🔐 Autenticação no Swagger (Opcional)

### ℹ️ Quando Usar Autenticação
- **Visualizar documentação**: ❌ **Não é necessário**
- **Testar endpoints públicos**: ❌ **Não é necessário** 
- **Testar endpoints protegidos**: ✅ **Necessário**

### Endpoints Públicos (Sem Token)
Você pode testar estes endpoints sem autenticação:
- `POST /api/v1/auth/login` - Login
- `POST /api/v1/auth/register` - Registro  
- `GET /api/v1/auth/health` - Health check
- `GET /api/v1/docs/**` - Documentação
- `GET /actuator/health` - Status da aplicação

### Endpoints Protegidos (Com Token)
Para testar endpoints que requerem autenticação:

#### 1. Fazendo Login
Obtenha um token JWT usando o endpoint de login (direto no Swagger):

```bash
curl -X POST "http://localhost:8000/api/v1/auth/login" \
     -H "Content-Type: application/json" \
     -d '{
       "email": "seu-email@email.com",
       "password": "sua-senha"
     }'
```

### 2. Configurando Autenticação no Swagger
1. Clique no botão **"Authorize"** no topo da página
2. Digite: `Bearer {seu-token-jwt}`
3. Clique em **"Authorize"**
4. Agora você pode testar endpoints protegidos

## 📋 Explorando a API

### Estrutura da Documentação

A API está organizada nos seguintes grupos:

#### 🔑 **Authentication**
- Login de usuários
- Registro de novos usuários
- Renovação de tokens

#### 👤 **User Management**
- Perfil do usuário
- Atualização de dados
- Upload de foto
- Recuperação de senha

#### 📊 **Subscription Plans**
- Listagem de planos
- Criação de planos (admin)
- Detalhes de planos

#### 💳 **Subscriptions**
- Assinatura de planos
- Histórico de assinaturas
- Cancelamento

#### 🥗 **Nutrition Plans**
- Planos personalizados
- Gestão de refeições
- Receitas e ingredientes

#### 💬 **Chat System**
- Conversas entre usuários
- Mensagens em tempo real
- Histórico de conversas

#### 👥 **Service Providers**
- Busca de nutricionistas
- Perfis de prestadores
- Avaliações

#### 🎛️ **Admin Dashboard**
- Métricas do sistema
- Gestão de usuários
- Relatórios

#### 📖 **Documentation**
- Endpoints de documentação
- Status da aplicação

## 🧪 Testando Endpoints

### Fluxo Básico de Teste

1. **Registrar um usuário:**
   ```
   POST /api/v1/auth/register
   ```

2. **Fazer login:**
   ```
   POST /api/v1/auth/login
   ```

3. **Configurar autenticação no Swagger**

4. **Testar endpoints protegidos:**
   ```
   GET /api/v1/users/profile
   GET /api/v1/subscription-plans
   POST /api/v1/subscriptions
   ```

### Exemplo Prático

#### 1. Registro de Usuário
```json
{
  "name": "João Silva",
  "email": "joao@email.com",
  "password": "senha123",
  "phone": "+5511999999999",
  "dateOfBirth": "1990-01-01",
  "gender": "M",
  "activityLevel": "MODERATE",
  "dietaryRestrictions": "Vegetariano"
}
```

#### 2. Login
```json
{
  "email": "joao@email.com",
  "password": "senha123"
}
```

#### 3. Criar Assinatura
```json
{
  "planId": 1,
  "paymentMethod": "CREDIT_CARD"
}
```

## 📝 Interpretando Respostas

### Códigos de Status

- **200**: Sucesso
- **201**: Criado com sucesso
- **400**: Erro de validação
- **401**: Não autorizado
- **403**: Acesso negado
- **404**: Não encontrado
- **500**: Erro interno

### Estrutura de Erro Padrão
```json
{
  "timestamp": "2024-01-15T10:30:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/v1/users/register"
}
```

## 🛠️ Recursos Avançados

### 1. Filtros e Paginação
Muitos endpoints suportam filtros:
```
GET /api/v1/subscription-plans?active=true&page=0&size=10
```

### 2. Upload de Arquivos
Para endpoints de upload (foto do usuário):
1. Selecione o arquivo
2. Configure o tipo de conteúdo
3. Execute a requisição

### 3. Exportação de Especificação
- **JSON**: `/api-docs`
- **YAML**: `/api-docs.yaml`
- **OpenAPI 3.0**: `/v3/api-docs`

## 🚨 Dicas de Troubleshooting

### Problemas Comuns

1. **Token expirado:**
   - Faça login novamente
   - Configure novo token no Authorize

2. **Erro 403:**
   - Verifique se tem permissão
   - Confirme se o token está válido

3. **Erro de validação:**
   - Verifique campos obrigatórios
   - Confirme formatos de data/email

4. **Swagger não carrega:**
   - Verifique se a aplicação está rodando
   - Acesse: http://localhost:8000/actuator/health

## 📚 Recursos Adicionais

### Documentação Relacionada
- [API Endpoints](/docs/API_ENDPOINTS.md)
- [Serviços e Regras de Negócio](/docs/SERVICOS_E_REGRAS_NEGOCIO.md)
- [Sistema de Assinaturas](/docs/SUBSCRIPTION_PLANS_SYSTEM.md)
- [Troubleshooting](/docs/TROUBLESHOOTING.md)

### URLs Úteis
- **Swagger UI**: http://localhost:8000/swagger-ui.html
- **Health Check**: http://localhost:8000/actuator/health
- **API Docs JSON**: http://localhost:8000/api-docs
- **API Docs YAML**: http://localhost:8000/api-docs.yaml
- **OpenAPI 3.0**: http://localhost:8000/v3/api-docs
- **Flyway Info**: http://localhost:8000/actuator/flyway

### Endpoints SpringDoc
- **Informações do SpringDoc**: http://localhost:8000/api/v1/springdoc/info
- **Grupos da API**: http://localhost:8000/api/v1/springdoc/groups
- **Status do SpringDoc**: http://localhost:8000/api/v1/springdoc/health

## 🎉 Começando Agora

1. Acesse http://localhost:8000/swagger-ui.html
2. Registre um usuário em `/api/v1/auth/register`
3. Faça login em `/api/v1/auth/login`
4. Configure a autenticação clicando em "Authorize"
5. Explore os endpoints disponíveis!

---

**💡 Dica:** Use o Swagger UI como sua principal ferramenta de desenvolvimento e teste da API. Ele oferece uma interface intuitiva e completa para explorar todas as funcionalidades do sistema.
