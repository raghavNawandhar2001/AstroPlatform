# Build stage
FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /app
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
COPY src src
# Grant execution rights to the maven wrapper
RUN chmod +x ./mvnw
RUN ./mvnw clean package -DskipTests

# Run stage
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

# Restrict JVM memory to fit inside Render's 512MB limit
ENV JAVA_TOOL_OPTIONS="-Xmx300m -Xss512k -XX:CICompilerCount=2 -Dfile.encoding=UTF-8"

EXPOSE 9090
ENTRYPOINT ["java", "-jar", "app.jar"]