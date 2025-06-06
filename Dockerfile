# Usa una imagen base con JDK para construir el proyecto
FROM eclipse-temurin:21-jdk AS build

# Establece el directorio de trabajo
WORKDIR /app

# Copia el archivo de configuración de Maven y el archivo pom.xml
COPY pom.xml .

# Descarga las dependencias necesarias (esto se hace antes de copiar el código para aprovechar la caché de Docker)
RUN ./mvnw dependency:go-offline -B

# Copia el resto del código fuente del proyecto y compila en una sola capa para optimizar la caché
COPY src ./src
RUN ./mvnw clean package -DskipTests

# Usa una imagen base más ligera para ejecutar la aplicación
FROM eclipse-temurin:21-jre

# Establece el directorio de trabajo en la imagen de ejecución
WORKDIR /app

# Copia el archivo JAR construido desde la etapa anterior
COPY --from=build /app/target/*.jar app.jar

# Expone el puerto 8080
EXPOSE 8080

# Define el punto de entrada
ENTRYPOINT ["java", "-jar", "app.jar"]
