# 🚀 Configuração SpringDoc OpenAPI - NutriXpert API

## 📋 Visão Geral

Este documento descreve a configuração e implementação do **SpringDoc OpenAPI 3** no projeto NutriXpert. O SpringDoc é a biblioteca moderna e recomendada para documentação de APIs Spring Boot 3.x.

## 🔧 Configuração Técnica

### 📦 Dependências

```xml
<!-- SpringDoc OpenAPI 3 para Spring Boot 3.x -->
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.2.0</version>
</dependency>
```

### ⚙️ Arquivos de Configuração

#### 1. **OpenApiConfig.java**
```java
@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI customOpenAPI() {
        // Configuração principal da OpenAPI
        // Define informações da API, servidores, autenticação e tags
    }
}
```

#### 2. **SpringDocConfig.java**
```java
@Configuration
public class SpringDocConfig {
    // Configuração de grupos da API
    // Organiza endpoints em categorias lógicas
}
```

#### 3. **application.properties**
```properties
# Configurações básicas
springdoc.api-docs.path=/api-docs
springdoc.swagger-ui.path=/swagger-ui.html
springdoc.swagger-ui.enabled=true

# Configurações avançadas
springdoc.swagger-ui.display-request-duration=true
springdoc.swagger-ui.display-operation-id=true
springdoc.swagger-ui.doc-expansion=none
```

### 🔐 Configuração de Segurança

#### SecurityConfig.java
```java
.requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**", "/api-docs/**")
.permitAll()
```

## 🗂️ Grupos da API

### Organização por Módulos

| Grupo | Nome | Caminhos | Descrição |
|-------|------|----------|-----------|
| `public` | Endpoints Públicos | `/api/v1/auth/**`, `/api/v1/docs/**` | Sem autenticação |
| `user` | Gestão de Usuários | `/api/v1/users/**` | Perfis e dados |
| `subscription` | Assinaturas | `/api/v1/subscription*/**` | Planos e assinaturas |
| `nutrition` | Nutrição | `/api/v1/nutrition-plans/**` | Planos alimentares |
| `chat` | Chat | `/api/v1/chat/**` | Mensagens |
| `service-provider` | Prestadores | `/api/v1/service-providers/**` | Profissionais |
| `admin` | Administrativo | `/api/v1/admin/**` | Dashboard admin |

## 📊 Recursos Implementados

### ✅ **Funcionalidades Ativas**

1. **Interface Swagger UI Moderna**
   - Design responsivo e intuitivo
   - Suporte completo ao OpenAPI 3.0
   - Agrupamento dinâmico de endpoints

2. **Autenticação JWT Integrada**
   - Botão "Authorize" configurado
   - Suporte ao Bearer Token
   - Teste direto de endpoints protegidos

3. **Documentação Automática**
   - Schemas automáticos a partir das classes
   - Validação de entrada/saída
   - Exemplos gerados automaticamente

4. **Múltiplos Formatos**
   - JSON: `/api-docs`
   - YAML: `/api-docs.yaml`
   - OpenAPI 3.0: `/v3/api-docs`

5. **Monitoramento**
   - Status de saúde do SpringDoc
   - Informações de versão
   - Verificação de disponibilidade

## 🌐 URLs de Acesso

### 🎯 **URLs Principais**

```bash
# Interface principal
http://localhost:8080/swagger-ui.html

# Documentação em JSON
http://localhost:8080/api-docs

# Documentação em YAML
http://localhost:8080/api-docs.yaml

# Especificação OpenAPI 3.0
http://localhost:8080/v3/api-docs
```

### 🔍 **Endpoints de Informação**

```bash
# Informações do SpringDoc
GET /api/v1/springdoc/info

# Lista de grupos
GET /api/v1/springdoc/groups

# Status de saúde
GET /api/v1/springdoc/health
```

## 🎨 Personalização

### 📝 **Anotações Utilizadas**

```java
@Tag(name = "Nome", description = "Descrição")
@Operation(summary = "Resumo", description = "Descrição detalhada")
@ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Sucesso"),
    @ApiResponse(responseCode = "400", description = "Erro de validação")
})
```

### 🏷️ **Tags Configuradas**

- **Autenticação**: Login, registro e tokens
- **Usuários**: Gestão de perfis
- **Planos de Assinatura**: Gestão de planos
- **Assinaturas**: Gerenciamento de assinaturas
- **Planos Nutricionais**: Alimentação personalizada
- **Chat**: Sistema de mensagens
- **Prestadores de Serviços**: Rede de profissionais
- **Dashboard Administrativo**: Métricas e gestão
- **Documentação**: Status e informações

## 🔧 Manutenção

### 📈 **Atualizações**

Para atualizar o SpringDoc:

1. **Verificar versão mais recente**
2. **Atualizar dependência no pom.xml**
3. **Testar compatibilidade**
4. **Verificar configurações depreciadas**

### 🐛 **Troubleshooting**

#### Problemas Comuns:

1. **Swagger UI não carrega:**
   - Verificar se a aplicação está rodando
   - Conferir configurações de segurança
   - Validar URLs no SecurityConfig

2. **Grupos não aparecem:**
   - Verificar configuração dos grupos
   - Validar padrões de path
   - Conferir anotações nos controllers

3. **Autenticação não funciona:**
   - Verificar configuração do SecurityScheme
   - Validar token JWT
   - Conferir formato Bearer

## 📚 Referências

- [SpringDoc Official Documentation](https://springdoc.org/)
- [OpenAPI 3.0 Specification](https://spec.openapis.org/oas/v3.0.3/)
- [Spring Boot 3.x Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/)

---

**💡 Dica:** O SpringDoc é mantido ativamente e oferece a melhor compatibilidade com Spring Boot 3.x e Jakarta EE. É a escolha recomendada para novos projetos.
