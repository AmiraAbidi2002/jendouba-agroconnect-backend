# Use a lightweight JRE for runtime
FROM eclipse-temurin:21-jre
WORKDIR /app

# Copy the pre-built JAR
COPY target/jendouba_agroconnect_backend-1.0-SNAPSHOT.jar app.jar

# Copy Dropwizard configuration
COPY config.yml config.yml

# Expose Dropwizard port
EXPOSE 8080

# Run the application
CMD ["java", "-jar", "app.jar", "server", "config.yml"]
