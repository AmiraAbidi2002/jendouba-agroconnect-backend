# 1. use Java 17
FROM openjdk:17-jdk-slim

# 2. define  the working directory
WORKDIR /app

# 3. Copy the entire project into the container
COPY . /app

# 4. Install Maven
RUN apt-get update && apt-get install -y maven

# 5. Build the project
RUN mvn clean package

# 6. Install Command to start the Dropwizard server
CMD ["java", "-jar", "target/jendouba_agroconnect_backend-1.0-SNAPSHOT.jar", "server", "config.yml"]
