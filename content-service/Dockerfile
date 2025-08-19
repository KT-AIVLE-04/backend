FROM eclipse-temurin:17-jre

RUN apt-get update \
 && apt-get install -y --no-install-recommends ffmpeg \
 && rm -rf /var/lib/apt/lists/*

WORKDIR /app
COPY build/libs/*SNAPSHOT.jar app.jar

ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=70 -XX:InitialRAMPercentage=50"

EXPOSE 8080
ENTRYPOINT ["java","-Duser.timezone=Asia/Seoul","-Djava.security.egd=file:/dev/./urandom","-jar","app.jar"]