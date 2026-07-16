# Stage 1 — compile the app inside Docker so no local Gradle install is needed
# Using the official Gradle image means the build environment is always consistent
FROM gradle:8-jdk21 AS builder
WORKDIR /app

# Copy dependency files first so Docker can cache this layer
# The full source copy below won't bust this cache unless gradle files change
COPY build.gradle.kts settings.gradle.kts ./
COPY gradle/ gradle/

# Copy the rest of the source code and build the fat JAR
# Tests are skipped here because they run separately in the pipeline
COPY src/ src/
RUN gradle bootJar -x test --no-daemon

# Stage 2 — extract the JAR into layers for better Docker layer caching
# Splitting the JAR means dependencies don't get re-uploaded on every code change
FROM eclipse-temurin:21-jre-jammy AS preparer
WORKDIR /opt/app
COPY --from=builder /app/build/libs/todo-0.0.1-SNAPSHOT.jar app.jar
RUN java -Djarmode=tools -jar app.jar extract --layers --launcher --destination extracted

# Stage 3 — the final image that actually runs in production
FROM eclipse-temurin:21-jre-jammy
WORKDIR /opt/app

# gosu lets us start as root, fix folder permissions, then switch to a normal user
RUN apt-get update && apt-get install -y --no-install-recommends gosu && rm -rf /var/lib/apt/lists/*

# Create a non-root user so the app doesn't run as root inside the container
RUN addgroup --system spring && adduser --system spring --ingroup spring \
    && mkdir -p /opt/app/data && chown spring:spring /opt/app/data

# Copy the app layers in order from least to most likely to change
# This keeps the big dependencies layer cached unless a library actually changes
COPY --from=preparer /opt/app/extracted/dependencies/ ./
COPY --from=preparer /opt/app/extracted/spring-boot-loader/ ./
COPY --from=preparer /opt/app/extracted/snapshot-dependencies/ ./
COPY --from=preparer /opt/app/extracted/application/ ./

COPY entrypoint.sh /entrypoint.sh
RUN chmod +x /entrypoint.sh

# The database file lives at /opt/app/data/todo.db
# Mount a volume here to keep it alive across container restarts
EXPOSE 8080

ENTRYPOINT ["/entrypoint.sh"]
