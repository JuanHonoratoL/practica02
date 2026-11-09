# Etapa 1: Compilación con Maven y JDK 25 (java.version del pom.xml)
FROM maven:3.9-eclipse-temurin-25 AS build
WORKDIR /app

# Descargar dependencias para aprovechar la caché de Docker
COPY pom.xml .
RUN mvn -B dependency:go-offline

# Copiar el código fuente y empaquetar el JAR
COPY src ./src
RUN mvn -B clean package -DskipTests

# Etapa 2: Imagen de ejecución
FROM eclipse-temurin:25-jre
WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

# Render asigna el puerto en la variable PORT; application.properties la lee con server.port=${PORT:8080}
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
