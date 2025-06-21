# Use OpenJDK 17 lightweight image 
FROM eclipse-temurin:21-jdk

#Working directory in the container 
WORKDIR /app

#Copy the compiled JAR from local to the container
COPY target/*.jar app.jar

#Expose port used by the Spring Boot
EXPOSE 8080

#Start the app
ENTRYPOINT ["java" , "-jar", "app.jar"]
