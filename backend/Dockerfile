FROM openjdk:17

# Argument settings - JAR_File
ARG JAR_FILE=build/libs/*.jar

# Clone jar file
COPY ${JAR_FILE} app.jar

# Run command
ENTRYPOINT ["java", "-jar", "app.jar"]