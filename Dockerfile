# 빌드 스테이지
FROM eclipse-temurin:21-jdk AS build

# 작업 디렉토리 설정
WORKDIR /app

# Gradle Wrapper 및 프로젝트 파일 복사
COPY gradlew gradlew
COPY gradle gradle
COPY build.gradle build.gradle
COPY settings.gradle settings.gradle
COPY src src

# gradlew에 실행 권한 부여
RUN chmod +x gradlew

# 애플리케이션 빌드
RUN ./gradlew clean build -x test

# 실행 스테이지
FROM eclipse-temurin:21-jre

# 작업 디렉토리 설정
WORKDIR /app

# 빌드된 JAR 파일 복사
COPY --from=build /app/build/libs/*.jar app.jar

# 필요한 포트 노출 (예: 8080)
EXPOSE 8080

# 애플리케이션 실행
ENTRYPOINT ["java", "-jar", "app.jar"]
