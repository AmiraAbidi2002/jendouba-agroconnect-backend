FROM eclipse-temurin:21-jre
WORKDIR /app

# Copy the pre-built JAR
COPY target/*.jar app.jar

# Copy Dropwizard configuration
COPY config.yml config.yml

# Expose Dropwizard port
EXPOSE 8080

# Run the application
CMD ["java", "-jar", "app.jar", "server", "config.yml"]
