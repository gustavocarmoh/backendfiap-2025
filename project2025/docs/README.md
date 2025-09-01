# Documentação do Projeto NutriXpert

Este diretório contém toda a documentação técnica e funcional do sistema NutriXpert.

## 🚀 Como Começar

### **[COMO RODAR O PROJETO](./COMO_RODAR_PROJETO.md)** 👈 **COMECE AQUI!**
Guia completo para executar o sistema em qualquer ambiente.

## 📋 Índice da Documentação

### Guias de Instalação e Execução
- **[COMO_RODAR_PROJETO.md](./COMO_RODAR_PROJETO.md)** - Guia completo para executar o sistema
- **[TROUBLESHOOTING.md](./TROUBLESHOOTING.md)** - Solução de problemas comuns
- **[SISTEMA_FUNCIONANDO.md](./SISTEMA_FUNCIONANDO.md)** - Status atual e problemas resolvidos

### Sistema Principal
- **[SERVICOS_E_REGRAS_NEGOCIO.md](./SERVICOS_E_REGRAS_NEGOCIO.md)** - **🔥 NOVO!** Documentação completa com fluxogramas de todos os serviços
- **[SUBSCRIPTION_PLANS_SYSTEM.md](./SUBSCRIPTION_PLANS_SYSTEM.md)** - **⭐ NOVO!** Sistema completo de planos de usuário e assinaturas  
- **[NUTRITION_PLAN_SYSTEM.md](./NUTRITION_PLAN_SYSTEM.md)** - Documentação completa do sistema de planos nutricionais
- **[SINGLE_ACTIVE_PLAN_RULE.md](./SINGLE_ACTIVE_PLAN_RULE.md)** - Regras de negócio para planos ativos únicos

### Sistemas de Suporte
- **[CHAT_SYSTEM.md](./CHAT_SYSTEM.md)** - Sistema de chat com preparação para IA/n8n
- **[ADMIN_DASHBOARD_SYSTEM.md](./ADMIN_DASHBOARD_SYSTEM.md)** - Dashboard administrativo

### Autenticação e Segurança
- **[JWT_IMPLEMENTATION_SUMMARY.md](./JWT_IMPLEMENTATION_SUMMARY.md)** - Implementação de JWT e segurança
- **[SUBSCRIPTION_GUIDE.md](./SUBSCRIPTION_GUIDE.md)** - Sistema de assinaturas e planos

### Dados e Exemplos
- **[NUTRITION_PLANS_DATA_SUMMARY.md](./NUTRITION_PLANS_DATA_SUMMARY.md)** - Resumo dos dados nutricionais
- **[dashboard_examples.md](./dashboard_examples.md)** - Exemplos de uso do dashboard

## 🛠️ Tecnologias Utilizadas

- **Spring Boot 3.3.2** - Framework principal
- **PostgreSQL** - Banco de dados
- **JWT** - Autenticação e autorização
- **Flyway** - Migrações de banco
- **Maven** - Gerenciamento de dependências
- **Docker** - Containerização (opcional)

## 🔥 Novo: Documentação Completa de Serviços

A documentação agora inclui **análise completa de todos os serviços** com fluxogramas detalhados:

### Serviços Documentados
| Serviço | Responsabilidade | Fluxogramas |
|---------|------------------|-------------|
| **AuthService** | Autenticação e JWT | ✅ Login, Validação |
| **SubscriptionService** | Gestão de assinaturas | ✅ Criação, Aprovação, Cancelamento |
| **NutritionPlanService** | Planos nutricionais | ✅ Criação, Validação de limites |
| **ChatService** | Sistema de chat | ✅ Mensagens, Integração IA |
| **AdminDashboardService** | Métricas e relatórios | ✅ Geração de dashboard |
| **ServiceProviderService** | Supermercados | ✅ CRUD de fornecedores |

### Características da Documentação
- ✅ **Fluxogramas Mermaid** para cada processo
- ✅ **Regras de negócio críticas** detalhadas  
- ✅ **APIs e exemplos** de código
- ✅ **Sequência de processos** complexos
- ✅ **Monitoramento e logs** estruturados

**👀 Consulte:** [`SERVICOS_E_REGRAS_NEGOCIO.md`](./SERVICOS_E_REGRAS_NEGOCIO.md) para a documentação técnica completa.

## 📊 Sistema de Planos de Usuário

O sistema conta com **planos de assinatura completos**:

### Tipos de Planos
| Plano | Preço | Planos Nutricionais | Status |
|-------|-------|-------------------|--------|
| **FREE** | R$ 0,00 | 5 planos | Padrão |
| **BASIC** | R$ 19,99 | 10 planos | Aprovação necessária |
| **STANDARD** | R$ 39,99 | 20 planos | Aprovação necessária |
| **PREMIUM** | R$ 79,99 | **Ilimitado** | Aprovação necessária |
| **ENTERPRISE** | R$ 199,99 | **Ilimitado** | Aprovação necessária |

### Regras de Negócio Críticas
- 🔐 **Um plano ativo por usuário** - Sistema cancela automaticamente assinatura anterior
- 📊 **Limites dinâmicos** - Validação automática baseada no plano atual
- 👥 **Processo de aprovação** - Administradores aprovam/rejeitam solicitações
- 🔄 **Estados controlados** - PENDING → APPROVED/REJECTED/CANCELLED

## 📞 Suporte

Em caso de problemas ou dúvidas:

1. Consulte o [guia de execução](./COMO_RODAR_PROJETO.md)
2. Verifique a seção de [troubleshooting](./TROUBLESHOOTING.md)  
3. Analise os [serviços e regras de negócio](./SERVICOS_E_REGRAS_NEGOCIO.md)
4. Entre em contato com a equipe de desenvolvimento

## 🎯 Destaques da Documentação

- 📖 **Guias práticos** com comandos copy-paste
- 🔄 **Fluxogramas detalhados** para cada processo crítico
- 🧪 **Exemplos de código** funcionais
- 🛠️ **Troubleshooting** abrangente
- 📊 **Diagramas e tabelas** explicativas
- 🔍 **Análise técnica** profunda de todos os serviços

---

**Documentação mantida atualizada - FIAP Projeto 2025**
