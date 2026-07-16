# Stage 1 — pull the layers out of the fat JAR so we can copy them separately
# This gives us better Docker layer caching (dependencies don't rebuild every time)
FROM eclipse-temurin:21-jre-jammy AS preparer
WORKDIR /opt/app
COPY build/libs/todo-0.0.1-SNAPSHOT.jar app.jar
RUN java -Djarmode=tools -jar app.jar extract --layers --launcher --destination extracted

# Stage 2 — build the actual image we'll run
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

EXPOSE 8080

ENTRYPOINT ["/entrypoint.sh"]
