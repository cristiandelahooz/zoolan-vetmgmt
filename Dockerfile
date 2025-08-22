FROM eclipse-temurin:21-jre
COPY target/petcare.jar petcare.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/petcare.jar"]
