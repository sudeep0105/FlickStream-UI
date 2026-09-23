FROM maven:3.9.16-eclipse-temurin-25-noble AS build
WORKDIR /workspace
COPY pom.xml .
COPY src ./src
RUN mvn -B -DskipTests package

FROM eclipse-temurin:25-jre-noble
WORKDIR /app
COPY --from=build --chown=10001:0 /workspace/target/flickstream-0.0.1-SNAPSHOT.jar /app/flickstream.jar
USER 10001:0
ENV PORT=8080
EXPOSE 8080
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "/app/flickstream.jar"]
