# Use OpenJDK 21 base image
FROM eclipse-temurin:21-jre

# Set working directory
WORKDIR /app

# Copy jar file after build
COPY target/simple-java-app-1.0-SNAPSHOT.jar app.jar

# Run the application
CMD ["java", "-jar", "app.jar"]
