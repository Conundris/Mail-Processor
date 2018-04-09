# Base Image
FROM openjdk:8-jre-alpine

# Environment variable passed from docker-compose file
ENV SPRING_ACTIVE_PROFILE dev

# Copy JAR to root folder
ADD ./target/pbm.jar pbm.jar

# Add logback configuration files
ADD logback.xml /

# Expose 8088 for Splunk Logging
EXPOSE 8088

## Execute JAR file on Start-Up
ENTRYPOINT ["sh", "-c", "java -Dspring.profiles.active=${SPRING_ACTIVE_PROFILE} -jar pbm.jar"]
