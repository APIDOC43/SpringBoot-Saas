# 실행 스테이지  docker build -t hocsserver --platform linux/amd64 .
# docker run --name hocsserver -p 8080:8080 --cpus="6" --memory="6g" hocsserver
#docker run --name hocsserver -p 8080:8080 -p 9010:9010 --cpus="6" --memory="6g" hocsserver
# Eclipse Temurin JRE 이미지를 사용
FROM eclipse-temurin:21-jre-alpine

# 환경 변수 설정
ENV SPRING_PROFILES_ACTIVE=docker

# 작업 디렉토리 설정
WORKDIR /app

# 빌드된 JAR 파일 복사
COPY build/libs/hocsserver-0.0.1-SNAPSHOT.jar app.jar

# VisualVM 연결을 위한 JMX 포트 및 애플리케이션 포트 노출
EXPOSE 8080 9010

# VisualVM 연결을 위한 JVM 옵션 설정
ENTRYPOINT ["java", \
    "-Dcom.sun.management.jmxremote=true", \
    "-Dcom.sun.management.jmxremote.local.only=false", \
    "-Dcom.sun.management.jmxremote.port=9010", \
    "-Dcom.sun.management.jmxremote.ssl=false", \
    "-Dcom.sun.management.jmxremote.authenticate=false", \
    "-Djava.rmi.server.hostname=127.0.0.1", \
    "-Dcom.sun.management.jmxremote.rmi.port=9010", \
    "-jar", \
    "app.jar"]
