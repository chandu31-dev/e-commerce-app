FROM maven:3.8.8-eclipse-temurin-17 AS build
WORKDIR /app

# copy maven wrapper and project files
COPY mvnw pom.xml ./
COPY .mvn .mvn
COPY src ./src

RUN chmod +x ./mvnw && ./mvnw -DskipTests package -B

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080
ENV JAVA_OPTS=""

CMD ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
