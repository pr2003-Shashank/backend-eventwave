# Step 1: Build the application
FROM eclipse-temurin:21-jdk as builder

WORKDIR /app

COPY . .

# Make mvnw script executable
RUN chmod +x mvnw

RUN ./mvnw clean package -DskipTests

# Step 2: Run the application
FROM eclipse-temurin:21-jdk

WORKDIR /app

COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
