# -------- Stage 1: Build with Maven (Java 21)
# ... existing code ...
FROM maven:3.9.8-eclipse-temurin-21 AS builder
WORKDIR /app

# Cache dependencies
# ... existing code ...
COPY pom.xml ./
RUN mvn -B -q -e -DskipTests dependency:go-offline

# Copy sources and build
# ... existing code ...
COPY src ./src
RUN mvn -B -q -e -DskipTests package

# -------- Stage 2: Runtime (JRE 21)
# ... existing code ...
FROM eclipse-temurin:21-jre
ENV TZ=UTC
WORKDIR /app

# Copy fat jar from builder
# ... existing code ...
COPY --from=builder /app/target/wedding-0.0.1-SNAPSHOT.jar /app/app.jar

EXPOSE 8080

# Health-friendly Java flags
# ... existing code ...
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:InitialRAMPercentage=50.0 -XX:+ExitOnOutOfMemoryError"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
