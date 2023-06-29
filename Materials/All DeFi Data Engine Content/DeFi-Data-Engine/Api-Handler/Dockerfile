FROM maven:3.8.6-eclipse-temurin-17-alpine

ENV APP_NAME api-handler
ENV PORT 8080
ENV OUTPUT /Documents/Handler-Output

EXPOSE ${PORT}

COPY ./ ./
RUN mvn clean package spring-boot:repackage -Dmaven.skip.test=true
CMD ["java", "-jar", "target/api-handler-0.0.1.jar"]