FROM gradle:9.7.0-jdk21-alpine AS build
WORKDIR /workspace
COPY --chown=gradle:gradle build.gradle.kts settings.gradle.kts ./
RUN gradle dependencies --no-daemon
COPY --chown=gradle:gradle src src
RUN gradle build --no-daemon

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S spring && adduser -S spring -G spring
COPY --from=build /workspace/build/libs/*.jar app.jar
USER spring:spring
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
