# 🏗️ Documentação Completa de Serviços e Regras de Negócio - NutriXpert

Esta documentação apresenta todos os serviços do sistema, suas regras de negócio e fluxogramas detalhados.

## 📋 Índice de Serviços

1. [AuthService - Autenticação e Autorização](#1-authservice)
2. [SubscriptionService - Gestão de Assinaturas](#2-subscriptionservice)
3. [SubscriptionPlanService - Gestão de Planos](#3-subscriptionplanservice)
4. [NutritionPlanService - Planos Nutricionais](#4-nutritionplanservice)
5. [ChatService - Sistema de Chat](#5-chatservice)
6. [AdminDashboardService - Dashboard Administrativo](#6-admindashboardservice)
7. [ServiceProviderService - Fornecedores](#7-serviceproviderservice)
8. [UserPhotoService - Fotos de Usuários](#8-userphotoservice)
9. [CurrentUserService - Contexto do Usuário](#9-currentuserservice)

---

## 1. AuthService

### 📝 Responsabilidades
- Autenticação de usuários
- Geração e validação de tokens JWT
- Gerenciamento de sessões
- Validação de credenciais

### 🔄 Fluxograma: Processo de Login

```mermaid
graph TD
    A[Usuário envia credenciais] --> B[Validar email e senha]
    B --> C{Credenciais válidas?}
    
    C -->|Não| D[Retorna erro 401]
    C -->|Sim| E[Verificar se usuário está ativo]
    
    E --> F{Usuário ativo?}
    F -->|Não| G[Retorna erro 403 - Conta inativa]
    F -->|Sim| H[Gerar token JWT]
    
    H --> I[Buscar roles do usuário]
    I --> J[Incluir roles no token]
    J --> K[Retornar token + dados do usuário]
    
    D --> L[Log da tentativa]
    G --> L
    K --> M[Login bem-sucedido]
```

### 🛡️ Regras de Negócio

1. **Validação de Credenciais**
   - Email deve existir no sistema
   - Senha deve corresponder ao hash armazenado
   - Usuário deve estar ativo (`isActive = true`)

2. **Geração de Token JWT**
   - Token expira em 24 horas
   - Inclui roles e permissions do usuário
   - Assinado com chave secreta configurável

3. **Controle de Acesso**
   - ADMIN: Acesso total ao sistema
   - MANAGER: Gestão de usuários e relatórios
   - USER: Acesso às funcionalidades básicas

### 📊 APIs Principais

```java
// Login
POST /auth/login
{
  "email": "user@example.com",
  "password": "password123"
}

// Resposta
{
  "token": "eyJhbGciOiJIUzI1NiIs...",
  "expiresIn": 86400000,
  "user": {
    "id": "uuid",
    "email": "user@example.com",
    "roles": ["USER"]
  }
}
```

---

## 2. SubscriptionService

### 📝 Responsabilidades
- Criação de assinaturas de planos
- Aprovação/rejeição por administradores
- Cancelamento de assinaturas
- Regra de um plano ativo por usuário

### 🔄 Fluxograma: Criação de Assinatura

```mermaid
graph TD
    A[Usuário solicita plano] --> B[Validar se usuário existe]
    B --> C[Validar se plano existe]
    C --> D[Verificar se plano está ativo]
    
    D --> E{Plano ativo?}
    E -->|Não| F[Erro: Plano indisponível]
    E -->|Sim| G[Criar assinatura status PENDING]
    
    G --> H[Salvar no banco]
    H --> I[Notificar admins]
    I --> J[Retornar confirmação]
```

### 🔄 Fluxograma: Aprovação de Assinatura

```mermaid
graph TD
    A[Admin analisa assinatura PENDING] --> B[Verificar se ainda está PENDING]
    B --> C{Status válido?}
    
    C -->|Não| D[Erro: Não pode aprovar]
    C -->|Sim| E[Cancelar assinaturas ativas do usuário]
    
    E --> F[Aprovar nova assinatura]
    F --> G[Definir admin aprovador]
    G --> H[Atualizar status para APPROVED]
    H --> I[Salvar alterações]
    I --> J[Notificar usuário]
```

### 🛡️ Regras de Negócio Críticas

1. **Um Plano Ativo por Usuário**
   ```java
   // Antes de aprovar nova assinatura
   private void cancelActiveSubscriptionsForUser(UUID userId) {
       List<Subscription> activeSubscriptions = 
           subscriptionRepository.findActiveSubscriptionsByUserId(userId);
       
       activeSubscriptions.forEach(subscription -> {
           subscription.cancel();
           subscriptionRepository.save(subscription);
       });
   }
   ```

2. **Estados da Assinatura**
   - **PENDING**: Aguardando aprovação admin
   - **APPROVED**: Ativa e funcionando
   - **REJECTED**: Rejeitada pelo admin
   - **CANCELLED**: Cancelada pelo usuário/admin

3. **Validações de Negócio**
   - Só admins podem aprovar/rejeitar
   - Usuários só podem cancelar suas próprias assinaturas
   - Apenas assinaturas APPROVED podem ser canceladas
   - Apenas assinaturas PENDING podem ser aprovadas/rejeitadas

---

## 3. SubscriptionPlanService

### 📝 Responsabilidades
- CRUD de planos de assinatura
- Ativação/desativação de planos
- Validação de unicidade de nomes
- Controle de limites nutricionais

### 🔄 Fluxograma: Criação de Plano

```mermaid
graph TD
    A[Admin cria novo plano] --> B[Validar dados de entrada]
    B --> C[Verificar nome único]
    C --> D{Nome já existe?}
    
    D -->|Sim| E[Erro: Nome duplicado]
    D -->|Não| F[Definir limite nutricional]
    
    F --> G[Criar entidade SubscriptionPlan]
    G --> H[Salvar no banco]
    H --> I[Retornar plano criado]
```

### 🛡️ Regras de Negócio

1. **Tipos de Planos e Limites**
   ```java
   // Limites por tipo de plano
   FREE: 5 planos nutricionais
   BASIC: 10 planos nutricionais  
   STANDARD: 20 planos nutricionais
   PREMIUM: null (ilimitado)
   ENTERPRISE: null (ilimitado)
   ```

2. **Validações**
   - Nome do plano deve ser único
   - Preço deve ser positivo
   - Apenas admins podem gerenciar planos

---

## 4. NutritionPlanService

### 📝 Responsabilidades
- Criação de planos nutricionais diários
- Validação de limites por tipo de assinatura
- Gerenciamento de refeições
- Cálculo de valores nutricionais

### 🔄 Fluxograma: Criação de Plano Nutricional

```mermaid
graph TD
    A[Usuário cria plano nutricional] --> B[Buscar usuário por email]
    B --> C[Validar limites do plano]
    C --> D{Atingiu limite?}
    
    D -->|Sim| E[Erro: Limite atingido<br/>Sugerir upgrade]
    D -->|Não| F[Verificar data única]
    
    F --> G{Já existe plano na data?}
    G -->|Sim| H[Erro: Data já utilizada]
    G -->|Não| I[Criar NutritionPlan]
    
    I --> J[Salvar no banco]
    J --> K[Retornar plano criado]
```

### 🔄 Fluxograma: Validação de Limites

```mermaid
graph TD
    A[Verificar limites] --> B[Buscar assinaturas ativas]
    B --> C{Tem assinatura ativa?}
    
    C -->|Não| D[Aplicar limite FREE: 5 planos]
    C -->|Sim| E[Buscar limite do plano]
    
    E --> F{Plano tem limite?}
    F -->|Não| G[Planos ilimitados]
    F -->|Sim| H[Contar planos existentes]
    
    D --> H
    H --> I{Atingiu limite?}
    I -->|Sim| J[Bloquear criação]
    I -->|Não| K[Permitir criação]
    G --> K
```

### 🛡️ Regras de Negócio

1. **Limites por Assinatura**
   ```java
   private void validateNutritionPlanLimits(User user) {
       List<Subscription> activeSubscriptions = 
           subscriptionRepository.findActiveSubscriptionsByUserId(user.getId());
       
       Integer limit;
       if (activeSubscriptions.isEmpty()) {
           limit = 5; // FREE
       } else {
           limit = activeSubscriptions.get(0).getPlan().getNutritionPlansLimit();
       }
       
       if (limit != null) {
           long currentCount = nutritionPlanRepository.countByUser(user);
           if (currentCount >= limit) {
               throw new PlanLimitExceededException();
           }
       }
   }
   ```

2. **Estrutura de Refeições**
   - Café da manhã (breakfast)
   - Lanche da manhã (morningSnack)
   - Almoço (lunch)  
   - Lanche da tarde (afternoonSnack)
   - Jantar (dinner)
   - Ceia (eveningSnack)

3. **Cálculos Nutricionais**
   - Calorias totais = soma de todas as refeições
   - Proteínas, carboidratos e gorduras por refeição
   - Controle de ingestão de água

---

## 5. ChatService

### 📝 Responsabilidades
- Processamento de mensagens
- Histórico de conversas
- Preparação para integração com IA
- Suporte multi-sessão

### 🔄 Fluxograma: Envio de Mensagem

```mermaid
graph TD
    A[Usuário envia mensagem] --> B[Validar usuário autenticado]
    B --> C[Gerar/validar sessionId]
    C --> D[Criar ChatMessage]
    
    D --> E[Salvar mensagem]
    E --> F[Verificar triggers IA]
    F --> G{Deve acionar IA?}
    
    G -->|Sim| H[Preparar payload para IA]
    G -->|Não| I[Gerar resposta padrão]
    
    H --> J[Enviar para n8n/webhook]
    I --> K[Retornar resposta]
    J --> L[Aguardar resposta IA]
    L --> K
```

### 🔄 Fluxograma: Preparação para IA

```mermaid
graph TD
    A[Mensagem recebida] --> B[Analisar conteúdo]
    B --> C[Verificar palavras-chave]
    C --> D{Contém triggers IA?}
    
    D -->|Não| E[Resposta padrão]
    D -->|Sim| F[Construir contexto]
    
    F --> G[Buscar planos do usuário]
    G --> H[Buscar histórico recente]
    H --> I[Montar payload completo]
    I --> J[Enviar para n8n]
    
    J --> K[Webhook de resposta]
    K --> L[Processar resposta IA]
    L --> M[Salvar resposta como mensagem]
    M --> N[Notificar usuário]
```

### 🛡️ Regras de Negócio

1. **Triggers para IA**
   ```java
   List<String> aiTriggers = Arrays.asList(
       "nutrição", "dieta", "calorias", "peso", "exercício",
       "receita", "ingrediente", "vitamina", "proteína"
   );
   ```

2. **Contexto para IA**
   - Mensagem atual do usuário
   - Histórico de 5 mensagens anteriores
   - Planos nutricionais ativos
   - Perfil do usuário (peso, altura, objetivos)

3. **Limitações**
   - Máximo 1000 caracteres por mensagem
   - Rate limit: 10 mensagens por minuto
   - Histórico mantido por 30 dias

---

## 6. AdminDashboardService

### 📝 Responsabilidades
- Geração de métricas administrativas
- Relatórios de uso do sistema
- KPIs de negócio
- Análises de crescimento

### 🔄 Fluxograma: Geração de Dashboard

```mermaid
graph TD
    A[Admin solicita dashboard] --> B[Validar permissões ADMIN]
    B --> C[Coletar métricas de usuários]
    C --> D[Coletar métricas de assinaturas]
    D --> E[Coletar métricas de planos]
    E --> F[Coletar métricas de receita]
    F --> G[Calcular KPIs]
    G --> H[Calcular crescimento mensal]
    H --> I[Montar dashboard completo]
    I --> J[Retornar métricas]
```

### 📊 Métricas Calculadas

1. **Métricas de Usuários**
   ```java
   - Total de usuários
   - Usuários ativos
   - Novos usuários (mês atual)
   - Taxa de crescimento mensal
   ```

2. **Métricas de Assinaturas**
   ```java
   - Total de assinaturas
   - Assinaturas ativas
   - Assinaturas pendentes
   - Taxa de conversão (FREE → PAID)
   ```

3. **Métricas Financeiras**
   ```java
   - Receita mensal recorrente (MRR)
   - Receita total
   - ARPU (Average Revenue Per User)
   - Churn rate
   ```

---

## 7. ServiceProviderService

### 📝 Responsabilidades
- Gestão de supermercados e fornecedores
- CRUD restrito a administradores
- Validação de dados de localização
- Controle de status ativo/inativo

### 🔄 Fluxograma: Cadastro de Fornecedor

```mermaid
graph TD
    A[Admin cadastra fornecedor] --> B[Validar dados obrigatórios]
    B --> C[Verificar nome único]
    C --> D{Nome já existe?}
    
    D -->|Sim| E[Erro: Nome duplicado]
    D -->|Não| F[Validar dados de endereço]
    
    F --> G[Criar ServiceProvider]
    G --> H[Salvar no banco]
    H --> I[Retornar fornecedor criado]
```

### 🛡️ Regras de Negócio

1. **Validações**
   - Nome deve ser único
   - Tipo deve ser válido (SUPERMARKET, RESTAURANT, etc.)
   - Endereço completo obrigatório
   - Telefone e email válidos

2. **Controle de Acesso**
   - Apenas admins podem gerenciar fornecedores
   - Usuários só podem visualizar fornecedores ativos

---

## 8. UserPhotoService

### 📝 Responsabilidades
- Upload de fotos de perfil
- Validação de formatos e tamanhos
- Controle de permissões de acesso
- Limpeza de arquivos órfãos

### 🔄 Fluxograma: Upload de Foto

```mermaid
graph TD
    A[Usuário faz upload] --> B[Validar formato arquivo]
    B --> C{Formato válido?}
    
    C -->|Não| D[Erro: Formato inválido]
    C -->|Sim| E[Validar tamanho]
    
    E --> F{Tamanho OK?}
    F -->|Não| G[Erro: Arquivo muito grande]
    F -->|Sim| H[Gerar nome único]
    
    H --> I[Salvar arquivo físico]
    I --> J[Criar registro UserPhoto]
    J --> K[Atualizar perfil usuário]
    K --> L[Retornar URL da foto]
```

### 🛡️ Regras de Negócio

1. **Validações de Arquivo**
   - Formatos aceitos: JPG, PNG, GIF
   - Tamanho máximo: 5MB
   - Dimensões mínimas: 100x100px

2. **Segurança**
   - Usuários só podem gerenciar suas próprias fotos
   - Arquivos salvos fora do diretório web
   - Validação de content-type

---

## 9. CurrentUserService

### 📝 Responsabilidades
- Contexto do usuário autenticado
- Informações de plano atual
- Validação de permissões
- Cache de dados do usuário

### 🔄 Fluxograma: Obter Contexto do Usuário

```mermaid
graph TD
    A[Requisição autenticada] --> B[Extrair email do token]
    B --> C[Buscar usuário no banco]
    C --> D[Buscar assinatura ativa]
    D --> E[Buscar planos nutricionais]
    E --> F[Calcular limites e uso]
    F --> G[Montar contexto completo]
    G --> H[Cachear por 5 minutos]
    H --> I[Retornar contexto]
```

---

## 🔄 Fluxogramas de Processos Críticos

### Processo Completo: Do Cadastro ao Plano Nutricional

```mermaid
graph TD
    A[Usuário se cadastra] --> B[Status: FREE - 5 planos]
    B --> C[Cria primeiro plano nutricional]
    C --> D[Usa 4 planos restantes]
    D --> E[Tenta criar 6º plano]
    E --> F[Sistema bloqueia]
    F --> G[Usuário solicita upgrade]
    G --> H[Admin aprova BASIC]
    H --> I[Cancela assinatura anterior]
    I --> J[Ativa nova assinatura]
    J --> K[Agora tem 10 planos]
    K --> L[Pode criar mais planos]
```

### Processo de Aprovação de Assinatura

```mermaid
sequenceDiagram
    participant U as Usuário
    participant S as Sistema
    participant A as Admin
    participant DB as Database
    
    U->>S: Solicita plano PREMIUM
    S->>DB: Cria subscription (PENDING)
    S->>U: Confirmação enviada
    
    A->>S: Lista pendências
    S->>A: Mostra assinatura pendente
    A->>S: Aprova assinatura
    
    S->>DB: Cancela assinaturas ativas
    S->>DB: Aprova nova assinatura
    S->>U: Notifica aprovação
    U->>S: Acessa recursos premium
```

---

## 🚨 Regras de Negócio Críticas

### 1. Regra do Plano Único
```java
// Um usuário só pode ter UMA assinatura ativa por vez
@Transactional
public void approveSubscription(UUID subscriptionId, UUID adminId) {
    // 1. Cancela todas assinaturas ativas
    cancelActiveSubscriptionsForUser(userId);
    
    // 2. Ativa a nova assinatura
    subscription.approve(adminId);
}
```

### 3. Limites Dinâmicos
```java
// Limites se ajustam automaticamente ao plano do usuário
private Integer getUserNutritionPlanLimit(User user) {
    return subscriptionRepository.findActiveSubscriptionsByUserId(user.getId())
        .stream()
        .findFirst()
        .map(sub -> sub.getPlan().getNutritionPlansLimit())
        .orElse(5); // FREE default
}
```

### 3. Validação de Permissões
```java
// Diferentes níveis de acesso
@PreAuthorize("hasRole('ADMIN')")
public void manageSubscriptionPlans() { }

@PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")  
public void viewDashboard() { }

@PreAuthorize("@nutritionPlanService.isOwner(#planId, authentication.name)")
public void editNutritionPlan(UUID planId) { }
```

---

## 📊 Monitoramento e Métricas

### KPIs por Serviço

1. **AuthService**
   - Taxa de login bem-sucedido
   - Tentativas de login inválidas
   - Tokens expirados

2. **SubscriptionService**
   - Taxa de conversão FREE → PAID
   - Tempo médio de aprovação
   - Taxa de cancelamento (churn)

3. **NutritionPlanService**
   - Planos criados por dia/mês
   - Utilização de limite por tipo de plano
   - Taxa de abandono

4. **ChatService**
   - Mensagens por usuário
   - Taxa de resposta da IA
   - Sessões ativas

---

## 🔍 Logs e Auditoria

### Eventos Auditados

```java
// Eventos críticos que geram logs
- Criação/aprovação/cancelamento de assinaturas
- Criação de planos nutricionais
- Mudanças de permissões
- Uploads de arquivos
- Tentativas de acesso negado
- Erros de sistema
```

### Formato de Log

```json
{
  "timestamp": "2025-08-31T14:30:00Z",
  "service": "SubscriptionService",
  "action": "APPROVE_SUBSCRIPTION",
  "userId": "uuid",
  "adminId": "uuid", 
  "subscriptionId": "uuid",
  "previousStatus": "PENDING",
  "newStatus": "APPROVED",
  "success": true
}
```

---

## 🎯 Próximas Evoluções

### Serviços Planejados

1. **NotificationService** - Notificações push e email
2. **ReportService** - Relatórios personalizados
3. **IntegrationService** - APIs externas
4. **AnalyticsService** - Análise comportamental
5. **PaymentService** - Gateway de pagamentos

### Melhorias Técnicas

- Cache distribuído (Redis)
- Processamento assíncrono (RabbitMQ)
- Circuit breakers
- Rate limiting avançado
- Monitoramento com Prometheus

---

**Documentação técnica completa - FIAP Projeto 2025**
