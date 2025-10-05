# 🔧 Troubleshooting DevContainer - Spring Boot Beans

## Problema: VS Code não reconhece beans do Spring Boot

### Sintomas
- Autocomplete não funciona para `@Autowired`
- Beans não são detectados
- Import automático não sugere classes Spring
- Dashboard Spring Boot vazio

---

## ✅ Soluções

### 1. Rebuild Completo do DevContainer

```bash
# No VS Code:
Ctrl/Cmd+Shift+P → "Dev Containers: Rebuild Container Without Cache"
```

Aguarde 5-10 minutos para:
- Reinstalar Oracle Instant Client
- Instalar Maven
- Baixar dependências Spring Boot

---

### 2. Executar Setup Manual

Após o container iniciar:

```bash
cd /workspaces/project2025
./setup-dev.sh
```

Este script irá:
- ✅ Verificar Java 21
- ✅ Verificar Maven
- ✅ Verificar Oracle Database
- ✅ Instalar dependências (`mvn clean install -DskipTests`)
- ✅ Validar Spring Boot

---

### 3. Forçar Reload do Java Language Server

```bash
# No VS Code:
Ctrl/Cmd+Shift+P → "Java: Clean Java Language Server Workspace"
```

Depois:

```bash
Ctrl/Cmd+Shift+P → "Developer: Reload Window"
```

---

### 4. Verificar Configurações Java

Execute no terminal:

```bash
# Verificar JAVA_HOME
echo $JAVA_HOME
# Esperado: /usr/local/sdkman/candidates/java/current

# Verificar versão Java
java -version
# Esperado: openjdk version "21..."

# Verificar Maven
mvn -version
# Esperado: Apache Maven 3.x

# Verificar dependências Maven
cd /workspaces/project2025
mvn dependency:tree | head -20
```

---

### 5. Limpar Cache Maven

```bash
cd /workspaces/project2025

# Remover cache local
rm -rf ~/.m2/repository

# Reinstalar dependências
mvn clean install -DskipTests
```

---

### 6. Verificar pom.xml

Confirme que o `pom.xml` tem o Spring Boot parent:

```xml
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>3.3.2</version>
</parent>
```

E as dependências principais:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
```

---

### 7. Verificar Extensões VS Code

Certifique-se que estas extensões estão instaladas:

- ✅ **Extension Pack for Java** (vscjava.vscode-java-pack)
- ✅ **Spring Boot Extension Pack** (vmware.vscode-boot-dev-pack)
- ✅ **Spring Boot Dashboard** (vscjava.vscode-spring-boot-dashboard)
- ✅ **Spring Initializr** (vscjava.vscode-spring-initializr)

Execute:

```bash
# Listar extensões instaladas
code --list-extensions | grep -i spring
code --list-extensions | grep -i java
```

---

### 8. Verificar .vscode/settings.json

O arquivo `/workspaces/project2025/.vscode/settings.json` deve conter:

```json
{
  "java.configuration.updateBuildConfiguration": "automatic",
  "java.autobuild.enabled": true,
  "java.import.maven.enabled": true,
  "spring-boot.ls.checkJVM": false,
  "spring-boot.ls.java.home": "${env:JAVA_HOME}"
}
```

---

### 9. Testar Spring Boot Dashboard

1. Abra o painel **Spring Boot Dashboard** (ícone da mola no lado esquerdo)
2. Clique em **Refresh** (ícone de reload)
3. Deve aparecer: `project2025`
4. Clique com botão direito → **Start**

Se não aparecer:
```bash
cd /workspaces/project2025
mvn spring-boot:run
```

---

### 10. Verificar Logs do Java Language Server

```bash
# No VS Code:
View → Output → Selecione "Language Support for Java"
```

Procure por erros como:
- `ClassNotFoundException`
- `Maven build failed`
- `JAVA_HOME not found`

---

## 🚀 Script de Diagnóstico Completo

Execute este script para diagnóstico automático:

```bash
#!/bin/bash
echo "=== DIAGNÓSTICO DEVCONTAINER ==="
echo ""

echo "1. Java:"
java -version 2>&1 | head -2
echo ""

echo "2. JAVA_HOME:"
echo $JAVA_HOME
echo ""

echo "3. Maven:"
mvn -version 2>&1 | head -2
echo ""

echo "4. Oracle Database:"
docker ps | grep oracle || echo "Oracle não está rodando"
echo ""

echo "5. Spring Boot no pom.xml:"
cd /workspaces/project2025
grep -A 2 "<parent>" pom.xml | head -3
echo ""

echo "6. Dependências Maven baixadas:"
ls -lh ~/.m2/repository/org/springframework/boot/ 2>/dev/null | wc -l
echo ""

echo "7. Target compilado:"
ls -lh /workspaces/project2025/target/*.jar 2>/dev/null || echo "JAR não encontrado"
echo ""

echo "8. Extensões Java instaladas:"
code --list-extensions | grep -i "java\|spring" | wc -l
echo ""

echo "=== FIM DIAGNÓSTICO ==="
```

Salve como `diagnostic.sh` e execute:

```bash
chmod +x diagnostic.sh
./diagnostic.sh
```

---

## 🆘 Último Recurso: Reset Completo

Se nada funcionar:

```bash
# 1. Sair do devcontainer
# VS Code: File → Close Remote Connection

# 2. Remover volumes
docker volume rm .devcontainer_maven-repo
docker volume rm .devcontainer_oracle_data

# 3. Remover containers
docker rm -f nutrixpert-oracle-xe
docker rm -f .devcontainer-app-1

# 4. Rebuild
# VS Code: Dev Containers: Rebuild Container Without Cache
```

---

## 📞 Checklist Rápido

Antes de reportar problema, verifique:

- [ ] Rebuild do container executado
- [ ] `mvn clean install` executado sem erros
- [ ] Java Language Server reiniciado
- [ ] `.vscode/settings.json` existe e está correto
- [ ] Extensões Java/Spring instaladas
- [ ] `pom.xml` tem Spring Boot parent
- [ ] Oracle Database está rodando
- [ ] Aguardou 2-3 minutos após rebuild

---

## 💡 Dica

O VS Code leva alguns minutos para indexar o projeto Spring Boot na primeira vez. Seja paciente e aguarde a mensagem:

```
✓ Java projects loaded successfully
```

No canto inferior direito da tela.

---

**Última atualização**: Outubro 2025  
**Versões**: Java 21, Maven 3.9, Spring Boot 3.3.2, Oracle 21c XE
