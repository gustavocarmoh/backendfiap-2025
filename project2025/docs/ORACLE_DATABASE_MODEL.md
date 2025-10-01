# Modelo de Banco de Dados Oracle - NutriXpert System

## 📊 Visão Geral

Este documento descreve o modelo lógico e físico do banco de dados Oracle implementado para o sistema NutriXpert, incluindo todas as tabelas, relacionamentos, functions e procedures PL/SQL.

---

## 🗂️ Diagrama Entidade-Relacionamento (DER)

### Modelo Conceitual

```
┌─────────────┐       ┌──────────────────┐       ┌─────────────────┐
│   ROLES     │──────<│   USER_ROLES    │>──────│     USERS       │
│             │       │                  │       │                 │
│ - id        │       │ - user_id (FK)   │       │ - id (PK)       │
│ - name      │       │ - role_id (FK)   │       │ - first_name    │
└─────────────┘       └──────────────────┘       │ - last_name     │
                                                  │ - email         │
                                                  │ - password_hash │
                                                  └─────────────────┘
                                                          │
                      ┌───────────────────────────────────┼─────────────────────────────┐
                      │                                   │                             │
                      ▼                                   ▼                             ▼
           ┌──────────────────┐              ┌────────────────────┐        ┌───────────────────┐
           │ SUBSCRIPTIONS    │              │  NUTRITION_PLANS   │        │  USER_PHOTOS      │
           │                  │              │                    │        │                   │
           │ - id (PK)        │              │ - id (PK)          │        │ - id (PK)         │
           │ - user_id (FK)   │              │ - user_id (FK)     │        │ - user_id (FK)    │
           │ - plan_id (FK)   │              │ - plan_date        │        │ - photo_data      │
           │ - status         │              │ - title            │        │ - content_type    │
           │ - amount         │              │ - total_calories   │        └───────────────────┘
           └──────────────────┘              │ - total_proteins   │
                      │                      │ - is_completed     │
                      │                      └────────────────────┘
                      ▼                                  │
           ┌──────────────────┐                         │
           │ SUBSCRIPTION_    │                         ▼
           │    PLANS         │              ┌────────────────────┐
           │                  │              │ NUTRITION_ALERTS   │
           │ - id (PK)        │              │                    │
           │ - name           │              │ - id (PK)          │
           │ - price          │              │ - user_id (FK)     │
           │ - nutrition_     │              │ - nutrition_       │
           │   plans_limit    │              │   plan_id (FK)     │
           └──────────────────┘              │ - alert_type       │
                                             │ - alert_message    │
                                             │ - severity         │
                                             └────────────────────┘

           ┌──────────────────┐              ┌────────────────────┐
           │ SERVICE_         │              │  CHAT_MESSAGES     │
           │ PROVIDERS        │              │                    │
           │                  │              │ - id (PK)          │
           │ - id (PK)        │              │ - session_id       │
           │ - name           │              │ - user_id (FK)     │
           │ - latitude       │              │ - message_content  │
           │ - longitude      │              │ - response_content │
           │ - endereco       │              │ - message_type     │
           │ - active         │              │ - status           │
           └──────────────────┘              └────────────────────┘
```

---

## 📋 Descrição das Tabelas

### 1. USERS (Usuários)
**Descrição**: Tabela principal de usuários do sistema com informações completas de perfil.

| Coluna | Tipo | Descrição | Constraints |
|--------|------|-----------|-------------|
| id | VARCHAR2(36) | Identificador único (UUID) | PRIMARY KEY |
| first_name | VARCHAR2(150) | Primeiro nome | NOT NULL |
| last_name | VARCHAR2(150) | Sobrenome | NOT NULL |
| email | VARCHAR2(255) | Email único | NOT NULL, UNIQUE |
| password_hash | VARCHAR2(255) | Hash da senha | NOT NULL |
| phone_e164 | VARCHAR2(20) | Telefone no formato E.164 | |
| address_street | VARCHAR2(255) | Logradouro | |
| address_number | VARCHAR2(20) | Número | |
| address_complement | VARCHAR2(100) | Complemento | |
| address_district | VARCHAR2(100) | Bairro | |
| address_city | VARCHAR2(100) | Cidade | |
| address_state | VARCHAR2(2) | Estado (UF) | |
| address_postal_code | VARCHAR2(10) | CEP | |
| address_country | VARCHAR2(2) | País (padrão: BR) | DEFAULT 'BR' |
| profile_photo_url | VARCHAR2(500) | URL da foto de perfil | |
| is_active | NUMBER(1) | Usuário ativo | DEFAULT 1, CHECK (0 ou 1) |
| created_at | TIMESTAMP | Data de criação | DEFAULT CURRENT_TIMESTAMP |
| updated_at | TIMESTAMP | Data de atualização | DEFAULT CURRENT_TIMESTAMP |

**Índices**:
- `idx_users_email_ci`: Índice case-insensitive no email
- Trigger `trg_users_updated_at`: Atualiza automaticamente `updated_at`

---

### 2. ROLES (Perfis de Acesso)
**Descrição**: Define os papéis/perfis de usuário no sistema.

| Coluna | Tipo | Descrição | Constraints |
|--------|------|-----------|-------------|
| id | NUMBER(19) | Identificador único | PRIMARY KEY |
| name | VARCHAR2(50) | Nome do perfil | NOT NULL, UNIQUE |

**Dados Iniciais**: ADMIN, MANAGER, USER

---

### 3. USER_ROLES (Relacionamento Usuário-Perfil)
**Descrição**: Tabela associativa N:N entre usuários e perfis.

| Coluna | Tipo | Descrição | Constraints |
|--------|------|-----------|-------------|
| user_id | VARCHAR2(36) | ID do usuário | FK para USERS, PK composta |
| role_id | NUMBER(19) | ID do perfil | FK para ROLES, PK composta |

---

### 4. SUBSCRIPTION_PLANS (Planos de Assinatura)
**Descrição**: Define os planos de assinatura disponíveis.

| Coluna | Tipo | Descrição | Constraints |
|--------|------|-----------|-------------|
| id | VARCHAR2(36) | Identificador único | PRIMARY KEY |
| name | VARCHAR2(255) | Nome do plano | NOT NULL, UNIQUE |
| price | NUMBER(10,2) | Preço mensal | NOT NULL, > 0 |
| description | CLOB | Descrição detalhada | |
| nutrition_plans_limit | NUMBER(10) | Limite de planos (NULL = ilimitado) | |
| is_active | NUMBER(1) | Plano ativo | DEFAULT 1 |
| created_at | TIMESTAMP | Data de criação | DEFAULT CURRENT_TIMESTAMP |
| updated_at | TIMESTAMP | Data de atualização | DEFAULT CURRENT_TIMESTAMP |

**Planos Padrão**:
- **Basic Plan**: R$ 9,99 - até 10 planos/mês
- **Standard Plan**: R$ 19,99 - até 20 planos/mês
- **Premium Plan**: R$ 29,99 - ilimitado
- **Enterprise Plan**: R$ 49,99 - ilimitado

---

### 5. SUBSCRIPTIONS (Assinaturas)
**Descrição**: Registro de assinaturas dos usuários.

| Coluna | Tipo | Descrição | Constraints |
|--------|------|-----------|-------------|
| id | VARCHAR2(36) | Identificador único | PRIMARY KEY |
| user_id | VARCHAR2(36) | ID do usuário | FK para USERS |
| plan_id | VARCHAR2(36) | ID do plano | FK para SUBSCRIPTION_PLANS |
| status | VARCHAR2(50) | Status da assinatura | CHECK (PENDING, APPROVED, REJECTED, CANCELLED) |
| approved_by_user_id | VARCHAR2(36) | ID do aprovador | FK para USERS |
| subscription_date | TIMESTAMP | Data da assinatura | DEFAULT CURRENT_TIMESTAMP |
| approved_date | TIMESTAMP | Data da aprovação | |
| amount | NUMBER(10,2) | Valor pago | NOT NULL |
| created_at | TIMESTAMP | Data de criação | DEFAULT CURRENT_TIMESTAMP |
| updated_at | TIMESTAMP | Data de atualização | DEFAULT CURRENT_TIMESTAMP |

---

### 6. NUTRITION_PLANS (Planos Nutricionais)
**Descrição**: Planos nutricionais diários dos usuários.

| Coluna | Tipo | Descrição | Constraints |
|--------|------|-----------|-------------|
| id | VARCHAR2(36) | Identificador único | PRIMARY KEY |
| user_id | VARCHAR2(36) | ID do usuário | FK para USERS |
| plan_date | DATE | Data do plano | NOT NULL |
| title | VARCHAR2(200) | Título do plano | NOT NULL |
| description | CLOB | Descrição geral | |
| breakfast | CLOB | Café da manhã | |
| morning_snack | CLOB | Lanche da manhã | |
| lunch | CLOB | Almoço | |
| afternoon_snack | CLOB | Lanche da tarde | |
| dinner | CLOB | Jantar | |
| evening_snack | CLOB | Ceia | |
| total_calories | NUMBER(10) | Total de calorias | CHECK (0-10000) |
| total_proteins | NUMBER(8,2) | Total de proteínas (g) | CHECK (0-1000) |
| total_carbohydrates | NUMBER(8,2) | Total de carboidratos (g) | CHECK (0-2000) |
| total_fats | NUMBER(8,2) | Total de gorduras (g) | CHECK (0-500) |
| water_intake_ml | NUMBER(10) | Ingestão de água (ml) | CHECK (0-10000) |
| is_completed | NUMBER(1) | Plano completado | DEFAULT 0 |
| completed_at | TIMESTAMP | Data de conclusão | |
| notes | CLOB | Observações | |
| created_at | TIMESTAMP | Data de criação | DEFAULT CURRENT_TIMESTAMP |
| updated_at | TIMESTAMP | Data de atualização | DEFAULT CURRENT_TIMESTAMP |

**Índices Importantes**:
- `uk_nutrition_plan_user_date`: UNIQUE (user_id, plan_date) - garante 1 plano por usuário por dia
- `idx_nutrition_plan_user_date_completed`: Composto para queries de análise

---

### 7. NUTRITION_ALERTS (Alertas Nutricionais)
**Descrição**: Alertas gerados automaticamente pela análise de planos.

| Coluna | Tipo | Descrição | Constraints |
|--------|------|-----------|-------------|
| id | VARCHAR2(36) | Identificador único | PRIMARY KEY |
| user_id | VARCHAR2(36) | ID do usuário | FK para USERS |
| nutrition_plan_id | VARCHAR2(36) | ID do plano relacionado | FK para NUTRITION_PLANS |
| alert_type | VARCHAR2(50) | Tipo do alerta | NOT NULL |
| alert_message | CLOB | Mensagem do alerta | NOT NULL |
| severity | VARCHAR2(20) | Severidade | CHECK (INFO, WARNING, CRITICAL) |
| is_read | NUMBER(1) | Alerta lido | DEFAULT 0 |
| created_at | TIMESTAMP | Data de criação | DEFAULT CURRENT_TIMESTAMP |

**Tipos de Alerta**:
- INATIVIDADE, CALORIAS_BAIXAS, CALORIAS_ALTAS
- PROTEINAS_BAIXAS, HIDRATACAO_BAIXA, GORDURAS_ALTAS
- BAIXA_ADESAO, SCORE_BAIXO, SCORE_EXCELENTE

---

### 8. USER_PHOTOS (Fotos de Usuário)
**Descrição**: Armazena fotos de perfil no banco de dados.

| Coluna | Tipo | Descrição | Constraints |
|--------|------|-----------|-------------|
| id | VARCHAR2(36) | Identificador único | PRIMARY KEY |
| user_id | VARCHAR2(36) | ID do usuário | FK para USERS, UNIQUE |
| photo_data | BLOB | Dados binários da imagem | NOT NULL |
| file_name | VARCHAR2(100) | Nome do arquivo | |
| content_type | VARCHAR2(50) | Tipo MIME | |
| file_size | NUMBER(19) | Tamanho em bytes | |
| created_at | TIMESTAMP | Data de criação | DEFAULT CURRENT_TIMESTAMP |
| updated_at | TIMESTAMP | Data de atualização | DEFAULT CURRENT_TIMESTAMP |

---

### 9. SERVICE_PROVIDERS (Provedores de Serviço)
**Descrição**: Supermercados e estabelecimentos parceiros com localização geográfica.

| Coluna | Tipo | Descrição | Constraints |
|--------|------|-----------|-------------|
| id | VARCHAR2(36) | Identificador único | PRIMARY KEY |
| name | VARCHAR2(150) | Nome do estabelecimento | NOT NULL |
| latitude | NUMBER(10,8) | Latitude | NOT NULL, CHECK (-90 a 90) |
| longitude | NUMBER(11,8) | Longitude | NOT NULL, CHECK (-180 a 180) |
| description | VARCHAR2(500) | Descrição dos serviços | |
| endereco | VARCHAR2(300) | Endereço completo | NOT NULL |
| active | NUMBER(1) | Estabelecimento ativo | DEFAULT 1 |
| created_by | VARCHAR2(36) | ID do criador | FK para USERS |
| updated_by | VARCHAR2(36) | ID do atualizador | FK para USERS |
| created_at | TIMESTAMP | Data de criação | DEFAULT CURRENT_TIMESTAMP |
| updated_at | TIMESTAMP | Data de atualização | DEFAULT CURRENT_TIMESTAMP |

**Índices Geoespaciais**:
- `idx_svc_providers_location`: (latitude, longitude)
- `idx_svc_providers_bounds`: (latitude, longitude, active)

---

### 10. CHAT_MESSAGES (Mensagens de Chat)
**Descrição**: Sistema de chat com IA para suporte aos usuários.

| Coluna | Tipo | Descrição | Constraints |
|--------|------|-----------|-------------|
| id | VARCHAR2(36) | Identificador único | PRIMARY KEY |
| session_id | VARCHAR2(36) | ID da sessão de chat | NOT NULL |
| user_id | VARCHAR2(36) | ID do usuário | FK para USERS |
| message_content | CLOB | Conteúdo da mensagem | NOT NULL |
| response_content | CLOB | Resposta do sistema | |
| message_type | VARCHAR2(50) | Tipo da mensagem | CHECK (tipos predefinidos) |
| status | VARCHAR2(20) | Status do processamento | CHECK (PENDING, PROCESSING, RESPONDED, ERROR, IGNORED) |
| is_from_user | NUMBER(1) | Origem da mensagem | DEFAULT 1 |
| processed_at | TIMESTAMP | Data do processamento | |
| error_message | CLOB | Mensagem de erro | |
| created_at | TIMESTAMP | Data de criação | DEFAULT CURRENT_TIMESTAMP |
| updated_at | TIMESTAMP | Data de atualização | DEFAULT CURRENT_TIMESTAMP |

---

## 🔧 Functions PL/SQL

### 1. calcular_indicador_saude_usuario
**Propósito**: Calcular um score de saúde nutricional (0-100) baseado em múltiplos fatores.

**Parâmetros**:
- `p_user_id IN VARCHAR2`: ID do usuário
- `p_dias_analise IN NUMBER`: Dias para análise (padrão: 30)

**Retorno**: `NUMBER` - Score de 0 a 100

**Componentes do Score**:
- **Completude (25 pontos)**: Percentual de planos completados
- **Calorias (25 pontos)**: Proximidade das calorias ideais (2000 kcal)
- **Macronutrientes (25 pontos)**: Equilíbrio de proteínas, carboidratos e gorduras
- **Hidratação (25 pontos)**: Ingestão de água adequada (2000ml)

**Classificação**:
- 85-100: EXCELENTE ⭐⭐⭐⭐⭐
- 70-84: BOM ⭐⭐⭐⭐
- 50-69: REGULAR ⭐⭐⭐
- 0-49: PRECISA MELHORAR ⭐

---

### 2. formatar_relatorio_nutricao
**Propósito**: Gerar relatório formatado em texto com estatísticas nutricionais.

**Parâmetros**:
- `p_user_id IN VARCHAR2`: ID do usuário
- `p_data_inicio IN DATE`: Data inicial (padrão: hoje - 7 dias)
- `p_data_fim IN DATE`: Data final (padrão: hoje)

**Retorno**: `CLOB` - Relatório formatado em texto

**Conteúdo do Relatório**:
- Cabeçalho com informações do usuário
- Estatísticas gerais (total de planos, taxa de completude)
- Valores nutricionais médios
- Indicador de saúde
- Lista dos 5 planos mais recentes

---

## ⚙️ Procedures PL/SQL

### 1. proc_registrar_alerta_nutricional
**Propósito**: Analisar planos nutricionais e registrar alertas automáticos.

**Parâmetros**:
- `p_user_id IN VARCHAR2`: ID do usuário
- `p_dias_analise IN NUMBER`: Dias para análise (padrão: 7)
- `p_alertas_gerados OUT NUMBER`: Número de alertas criados

**Lógica de Análise**:
1. **Inatividade**: Verifica dias sem criar plano
2. **Calorias**: Detecta consumo muito baixo (<1200) ou alto (>3500)
3. **Proteínas**: Alerta se abaixo de 40g
4. **Hidratação**: Verifica ingestão menor que 1500ml
5. **Macronutrientes**: Detecta desequilíbrios (ex: >40% gorduras)
6. **Adesão**: Alerta sobre muitos planos não completados
7. **Score de Saúde**: Alerta se score < 50 ou parabeniza se > 85

**Recursos PL/SQL Utilizados**:
- `CURSOR`: Iteração sobre planos
- `LOOP`: Análise de cada plano
- `IF/ELSIF/ELSE`: Lógica condicional complexa
- `EXCEPTION`: Tratamento de erros
- Procedures aninhadas (registrar_alerta)

---

### 2. proc_gerar_relatorio_consumo
**Propósito**: Gerar relatório detalhado com análise semanal e estatísticas consolidadas.

**Parâmetros**:
- `p_user_id IN VARCHAR2`: ID do usuário
- `p_data_inicio IN DATE`: Data inicial (padrão: hoje - 30)
- `p_data_fim IN DATE`: Data final (padrão: hoje)
- `p_relatorio OUT CLOB`: Relatório gerado
- `p_sucesso OUT NUMBER`: Status de sucesso (1 ou 0)

**Conteúdo do Relatório**:
1. Dados do usuário
2. Estatísticas gerais do período
3. Médias nutricionais
4. **Análise semanal**: Consumo calórico por semana
5. **Top 5**: Dias com mais calorias
6. Indicador de saúde com classificação
7. Rodapé com data/hora de geração

**Recursos PL/SQL Utilizados**:
- `CURSOR`: Múltiplos cursors para diferentes análises
- `TYPE... TABLE OF`: Coleções tipadas
- `LOOP`: Iteração sobre resultados
- `DBMS_LOB`: Manipulação de CLOB
- `TO_CHAR`: Formatação de datas e números
- Chamada a function externa (calcular_indicador_saude_usuario)

---

## 🔐 Segurança e Integridade

### Constraints de Integridade Referencial
- Todas as FKs com `ON DELETE CASCADE` ou `ON DELETE SET NULL` conforme necessidade
- Unique constraints para prevenir duplicação

### Validações
- CHECK constraints para valores numéricos (ranges válidos)
- CHECK constraints para enumerações (status, severidade, etc.)
- UNIQUE indexes para garantir 1 plano por usuário por dia

### Triggers
- Todos as tabelas possuem trigger para atualizar `updated_at` automaticamente

---

## 📈 Performance e Otimização

### Índices Estratégicos
- Índices compostos para queries frequentes
- Índices em FKs para JOINs eficientes
- Índices geoespaciais para busca por localização

### Particionamento (Recomendado para Produção)
- `NUTRITION_PLANS`: Particionar por `plan_date` (mensal ou trimestral)
- `CHAT_MESSAGES`: Particionar por `created_at`
- `NUTRITION_ALERTS`: Particionar por `created_at`

---

## 🚀 Migração de PostgreSQL para Oracle

### Principais Mudanças

| PostgreSQL | Oracle | Observação |
|------------|--------|------------|
| SERIAL/BIGSERIAL | SEQUENCE | Uso de sequences explícitas |
| UUID (nativo) | VARCHAR2(36) | UUID como string |
| BOOLEAN | NUMBER(1) | 0 ou 1 com CHECK constraint |
| TEXT | CLOB | Para textos longos |
| BYTEA | BLOB | Para dados binários |
| gen_random_uuid() | SYS_GUID() | Geração de UUIDs |
| NOW() | CURRENT_TIMESTAMP | Timestamp atual |
| array_agg() | LISTAGG() | Agregação de strings |

---

## 📦 Backup e Manutenção

### Estratégia de Backup
```sql
-- Export de dados
expdp username/password schemas=NUTRIXPERT directory=BACKUP_DIR dumpfile=nutrixpert_%date%.dmp

-- Import de dados
impdp username/password schemas=NUTRIXPERT directory=BACKUP_DIR dumpfile=nutrixpert_20250930.dmp
```

### Manutenção de Índices
```sql
-- Rebuild de índices
ALTER INDEX idx_nutrition_plan_user_date REBUILD;

-- Análise de estatísticas
EXEC DBMS_STATS.GATHER_SCHEMA_STATS('NUTRIXPERT');
```

---

## 📝 Notas Finais

Este modelo foi projetado para:
- ✅ Alta performance em queries analíticas
- ✅ Integridade referencial garantida
- ✅ Escalabilidade horizontal (particionamento)
- ✅ Segurança através de constraints e validações
- ✅ Auditoria completa (created_at, updated_at, created_by)
- ✅ Suporte a lógica de negócio no banco (PL/SQL)

**Autor**: NutriXpert Development Team  
**Versão**: 1.0.0  
**Data**: Setembro 2025
