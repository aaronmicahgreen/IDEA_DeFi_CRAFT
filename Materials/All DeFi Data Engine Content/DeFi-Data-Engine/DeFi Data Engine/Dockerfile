FROM maven:3.8.6-eclipse-temurin-17
COPY ./ ./
RUN mvn clean compile assembly:single -Dmaven.skip.test=true
CMD ["java", "-jar", "target/defi-data-engine-0.0.1-jar-with-dependencies.jar"]