FROM openjdk:17-jdk-alpine

WORKDIR /app

COPY build/libs/*SNAPSHOT.jar app.jar

EXPOSE 8080

ENV TZ=Asia/Seoul
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

ENTRYPOINT ["java","-Xmx400M","-Djava.security.egd=file:/dev/./urandom","-jar","app.jar"]