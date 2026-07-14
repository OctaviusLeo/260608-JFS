# --- Stage 1: Extract layers from the fat JAR ---
# Use Java 21 to match the toolchain defined in build.gradle.kts
FROM eclipse-temurin:21-jre-jammy AS preparer
WORKDIR /opt/app
COPY build/libs/todo-0.0.1-SNAPSHOT.jar app.jar
# Extract layers into a dedicated subdirectory so stage 2 COPY paths are unambiguous.
# Spring Boot 3.3+ uses jarmode=tools (jarmode=layertools was removed).
RUN java -Djarmode=tools -jar app.jar extract --layers --launcher --destination extracted

# --- Stage 2: Create the minimal runtime image ---
FROM eclipse-temurin:21-jre-jammy
WORKDIR /opt/app

# Create a non-root user to run the app securely
RUN addgroup --system spring && adduser --system spring --ingroup spring
USER spring:spring

# Copy extracted application layers from the preparer stage.
# Ordered from least to most frequently changed for optimal Docker layer cache reuse.
COPY --from=preparer /opt/app/extracted/dependencies/ ./
COPY --from=preparer /opt/app/extracted/spring-boot-loader/ ./
COPY --from=preparer /opt/app/extracted/snapshot-dependencies/ ./
COPY --from=preparer /opt/app/extracted/application/ ./

# Inform Docker that the container listens on the standard port
EXPOSE 8080

# JarLauncher is the entry point for Spring Boot's exploded layer structure
ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]
