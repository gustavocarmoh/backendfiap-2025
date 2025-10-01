# .devcontainer/Dockerfile — Java 21 + Oracle Instant Client (Dev Containers base)
FROM mcr.microsoft.com/devcontainers/java:1-21

USER root

# Utilitários extras + Oracle dependencies
ENV DEBIAN_FRONTEND=noninteractive
RUN apt-get update \
    && apt-get install -y --no-install-recommends \
    netcat-openbsd curl ca-certificates wget unzip libaio1 alien \
    && rm -rf /var/lib/apt/lists/*

# Instalar Oracle Instant Client 21c (Basic + SQLPlus)
# Necessário para ferramentas de linha de comando Oracle
WORKDIR /tmp
RUN wget -q https://download.oracle.com/otn_software/linux/instantclient/219000/oracle-instantclient-basic-21.9.0.0.0-1.el8.x86_64.rpm \
    && wget -q https://download.oracle.com/otn_software/linux/instantclient/219000/oracle-instantclient-sqlplus-21.9.0.0.0-1.el8.x86_64.rpm \
    && alien -i oracle-instantclient-basic-21.9.0.0.0-1.el8.x86_64.rpm \
    && alien -i oracle-instantclient-sqlplus-21.9.0.0.0-1.el8.x86_64.rpm \
    && rm -f oracle-instantclient-*.rpm \
    && echo "/usr/lib/oracle/21/client64/lib" > /etc/ld.so.conf.d/oracle-instantclient.conf \
    && ldconfig

# Configurar variáveis de ambiente Oracle
ENV ORACLE_HOME=/usr/lib/oracle/21/client64 \
    LD_LIBRARY_PATH=/usr/lib/oracle/21/client64/lib:$LD_LIBRARY_PATH \
    PATH=/usr/lib/oracle/21/client64/bin:$PATH

# Script para esperar o Oracle estar pronto
COPY .devcontainer/wait-for-it.sh /usr/local/bin/wait-for-it
RUN chmod +x /usr/local/bin/wait-for-it

# (Imagem já vem com usuário 'vscode')
USER vscode
WORKDIR /workspaces

ENV MAVEN_CONFIG=/home/vscode/.m2 \
    JAVA_TOOL_OPTIONS="-XX:+UseZGC -Dfile.encoding=UTF-8 -Duser.timezone=America/Sao_Paulo"

EXPOSE 8000 5005
SHELL ["/bin/bash", "-lc"]
