FROM adoptopenjdk:11-jre-hotspot
ARG JAR_FILE=service/build/libs/service-0.0.1-SNAPSHOT.jar
ARG WORKDIR=$(pwd)
COPY ${JAR_FILE} ${WORKDIR}/application.jar
WORKDIR ${WORKDIR}
ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=local", "application.jar"]