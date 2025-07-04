FROM openjdk:17-jdk-slim
WORKDIR /app
COPY Scheduler-backend-2-0.0.1-SNAPSHOT.jar /app/app.jar
COPY logback-spring.xml /app/logback-spring.xml
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]