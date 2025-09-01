# .devcontainer/Dockerfile — Java 21 (Dev Containers base)
FROM mcr.microsoft.com/devcontainers/java:1-21

USER root

# Utilitários extras
ENV DEBIAN_FRONTEND=noninteractive
RUN apt-get update \
    && apt-get install -y --no-install-recommends \
    netcat-openbsd curl ca-certificates \
    && rm -rf /var/lib/apt/lists/*

# Script para esperar o Postgres
COPY .devcontainer/wait-for-it.sh /usr/local/bin/wait-for-it
RUN chmod +x /usr/local/bin/wait-for-it

# (Imagem já vem com usuário 'vscode')
USER vscode
WORKDIR /workspaces

ENV MAVEN_CONFIG=/home/vscode/.m2 \
    JAVA_TOOL_OPTIONS="-XX:+UseZGC -Dfile.encoding=UTF-8 -Duser.timezone=America/Sao_Paulo"

EXPOSE 8000 5005
SHELL ["/bin/bash", "-lc"]
