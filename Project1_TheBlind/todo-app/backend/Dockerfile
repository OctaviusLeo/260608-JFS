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

# Stage 3: final runtime image — alpine is much smaller than jammy (~150MB vs ~400MB)
# This keeps the deployed image small so it fits on the EC2 disk alongside Jenkins
FROM eclipse-temurin:21-jre-alpine
WORKDIR /opt/app

# gosu on Alpine comes from the apk package manager
RUN apk add --no-cache gosu

# Non-root user for security
RUN addgroup -S spring && adduser -S spring -G spring \
    && mkdir -p /opt/app/data && chown spring:spring /opt/app/data

# Copy layers from least to most likely to change for better cache reuse
COPY --from=preparer /opt/app/extracted/dependencies/ ./
COPY --from=preparer /opt/app/extracted/spring-boot-loader/ ./
COPY --from=preparer /opt/app/extracted/snapshot-dependencies/ ./
COPY --from=preparer /opt/app/extracted/application/ ./

COPY entrypoint.sh /entrypoint.sh
# Strip Windows line endings so the shebang is valid on Linux, then make executable
RUN sed -i 's/\r//' /entrypoint.sh && chmod +x /entrypoint.sh

# todo.db writes to /opt/app/data, mount a volume here for persistence
EXPOSE 8080

ENTRYPOINT ["/entrypoint.sh"]
