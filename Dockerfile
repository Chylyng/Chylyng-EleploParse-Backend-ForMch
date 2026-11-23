FROM maven:3.8.7-eclipse-temurin-11 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

FROM eclipse-temurin:11-jre
WORKDIR /app

# 建立非 root 使用者
RUN groupadd -r app && useradd -r -g app app

COPY --from=build --chown=app:app /app/target/*.jar app.jar

USER app

ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=prod"]
