# DevContainer — nutrixpert-api (Java 21 + Spring 3.3 + Postgres 16)

## Requisitos
- Docker + Docker Compose
- VS Code com a extensão **Dev Containers**

## Como usar
1. Abra o repositório no VS Code.
2. **Ctrl/Cmd+Shift+P** → *Dev Containers: Reopen in Container*.
3. O compose vai subir `postgres` e `app`. O app roda com:
   - Perfil: `dev`
   - Porta HTTP: `http://localhost:8080`
   - Debug JDWP: `5005`

## Hot reload
- Inclua `spring-boot-devtools` no `pom.xml` (veja snippet abaixo).
- Mudanças em `src/main/java` recompilam e recarregam automaticamente com `spring-boot:run`.

## JDBC & Atalhos
- JDBC (dev): `jdbc:postgresql://postgres:5432/nutrixpert` (usuário `nutri`, senha `nutrisecret`).
- Healthcheck do app (exige Actuator): `http://localhost:8080/actuator/health`.

## Comandos úteis
- Rodar testes: `mvn -q test`
- Limpar/Build: `mvn -q -DskipTests package`
- Subir só o banco: `docker compose up -d postgres`

## Debug
- O container expõe `:5005` e o Maven injeta o JDWP em `spring-boot:run`.
- Use *Java: Attach to Port* no VS Code (`localhost:5005`) se quiser anexar manualmente.

## Persistência
- Volume `db-data` preserva dados do Postgres.
- Volume `maven-repo` preserva cache Maven.

## Flyway
- Habilitado no `application-dev.yml`.
- Exemplo de migração inicial em `src/main/resources/db/migration/V1__init.sql`.
