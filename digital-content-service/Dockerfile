FROM openjdk:11-jdk-slim

# 設定時區
ENV TZ=Asia/Taipei
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# copy jar
COPY target/digital-content-service.jar /app.jar

# entrypoint
ENTRYPOINT ["java", "-jar", "/app.jar"]