# Etapa 1: build com Maven
# Usa uma imagem base com Maven + JDK 17 para compilar o projeto Java
FROM maven:3.9.6-eclipse-temurin-21 AS builder

# Define o diretório de trabalho dentro do container
WORKDIR /app

# Copia todos os arquivos do projeto para o diretório de trabalho no container
# mkdir -p /app && cd /app
# Se o diretório /app não existir, ele será criado automaticamente.
# Depois, o Docker define esse diretório como o diretório de trabalho atual para todas as instruções subsequentes (COPY, RUN, CMD, etc).
COPY . .

# Executa o build do projeto com Maven, ignorando os testes
RUN mvn clean package -DskipTests

# Etapa 2: runtime leve
# Usa uma imagem mais leve apenas com o JDK (sem Maven), ideal para rodar a aplicação
FROM eclipse-temurin:21-jdk-alpine

# Define novamente o diretório de trabalho no novo container
WORKDIR /app

# Copia o JAR gerado na etapa anterior para o novo container
COPY --from=builder /app/target/dslist-0.0.1-SNAPSHOT.jar app.jar

# Expõe a porta 9001, que é a porta padrão da aplicação Spring Boot
EXPOSE 9001

# Define o comando padrão para executar o JAR
ENTRYPOINT ["java", "-jar", "app.jar"]
