# Backend FIAP 2025

## Como rodar

1. Configure o PostgreSQL e ajuste o `application.properties`.
2. Rode o projeto:
   ```
   ./mvnw spring-boot:run
   ```
3. Acesse a documentação Swagger em `/swagger-ui.html`.

## Endpoints

- CRUD de usuários em `/api/users`
- Autenticação básica (pode ser expandida para JWT)
