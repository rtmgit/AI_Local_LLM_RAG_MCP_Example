# ===== Stage 1: Build the application (compile + package) =====
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /workspace

# Copy only pom first for better caching of dependencies
COPY pom.xml .
RUN mvn -q -e -DskipTests dependency:go-offline

# Now copy source and build
COPY src ./src
RUN mvn -q -DskipTests clean package

# ===== Stage 2: Run the packaged jar =====
FROM eclipse-temurin:17-jre
WORKDIR /app

# Copy the jar built in stage 1
COPY --from=build /workspace/target/*.jar app.jar

EXPOSE 8081
ENV JAVA_OPTS=""

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
