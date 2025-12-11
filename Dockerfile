# Etapa de construcción (Build Stage)
# Usamos una imagen de Maven con JDK 17 para compilar el proyecto
FROM maven:3.9.6-eclipse-temurin-17 AS build

# Establecemos el directorio de trabajo dentro del contenedor
WORKDIR /app

# Copiamos primero el archivo pom.xml para descargar las dependencias
# Esto aprovecha la caché de Docker si no han cambiado las dependencias
COPY pom.xml .

# Descargamos las dependencias (opcional, pero mejora tiempos de rebuild)
RUN mvn dependency:go-offline -B

# Copiamos el código fuente del proyecto
COPY src ./src

# Compilamos el proyecto y empaquetamos el JAR, saltando los tests para agilizar
RUN mvn clean package -DskipTests

# Etapa de ejecución (Run Stage)
# Usamos una imagen ligera de JRE 17 (Alpine) para ejecutar la aplicación
# "solo lo que tiene mi proyecto" -> solo el JAR compilado y el entorno necesario para correrlo
FROM eclipse-temurin:17-jre-alpine

# Argumento para especificar el puerto, por defecto 8084 (basado en application.properties)
ENV SERVER_PORT=8084

# Creamos un usuario no root por seguridad (buena práctica)
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copiamos el JAR generado desde la etapa de construcción anterior
# Se renombra a app.jar para facilitar su ejecución
COPY --from=build /app/target/sistema-hotelero-0.0.1-SNAPSHOT.jar app.jar

# Exponemos el puerto configurado en la aplicación
EXPOSE 8084

# Comando para ejecutar la aplicación
ENTRYPOINT ["java", "-jar", "/app.jar"]
