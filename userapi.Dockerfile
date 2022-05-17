FROM adoptopenjdk:11-jre-hotspot
ARG JAR_FILE=service/build/libs/service-0.0.1-SNAPSHOT.jar
COPY ${JAR_FILE} application.jar
ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=local", "application.jar"]