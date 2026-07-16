# Stage 1: compile the app using the official Gradle image
FROM gradle:8-jdk21 AS builder
WORKDIR /app

# Copy build files before source so this layer gets cached
COPY build.gradle.kts settings.gradle.kts ./
COPY gradle/ gradle/

# Build the fat JAR, skip tests since they run separately
COPY src/ src/
RUN gradle bootJar -x test --no-daemon

# Stage 2: extract the JAR into layers
FROM eclipse-temurin:21-jre-jammy AS preparer
WORKDIR /opt/app
COPY --from=builder /app/build/libs/todo-0.0.1-SNAPSHOT.jar app.jar
RUN java -Djarmode=tools -jar app.jar extract --layers --launcher --destination extracted

# Stage 3: final runtime image
FROM eclipse-temurin:21-jre-jammy
WORKDIR /opt/app

# gosu is needed to drop from root to the spring user at startup
RUN apt-get update && apt-get install -y --no-install-recommends gosu && rm -rf /var/lib/apt/lists/*

# Non-root user for security
RUN addgroup --system spring && adduser --system spring --ingroup spring \
    && mkdir -p /opt/app/data && chown spring:spring /opt/app/data

# Copy layers from least to most likely to change for better cache reuse
COPY --from=preparer /opt/app/extracted/dependencies/ ./
COPY --from=preparer /opt/app/extracted/spring-boot-loader/ ./
COPY --from=preparer /opt/app/extracted/snapshot-dependencies/ ./
COPY --from=preparer /opt/app/extracted/application/ ./

COPY entrypoint.sh /entrypoint.sh
RUN chmod +x /entrypoint.sh

# todo.db writes to /opt/app/data, mount a volume here for persistence
EXPOSE 8080

ENTRYPOINT ["/entrypoint.sh"]
