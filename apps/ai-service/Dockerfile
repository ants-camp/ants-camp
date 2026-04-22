FROM gradle:8.5-jdk17 AS builder
WORKDIR /app

# 루트 빌드 설정 파일 복사
COPY build.gradle settings.gradle gradlew ./
COPY gradle ./gradle

# common-libs 복사
COPY common ./common

# ai-service 소스 복사
COPY ai-service ./ai-service

RUN chmod +x gradlew
RUN ./gradlew :ai-service:clean :ai-service:build -x test --no-daemon

FROM eclipse-temurin:17-jre

WORKDIR /app

COPY --from=builder /app/ai-service/build/libs/*.jar app.jar

EXPOSE 8096

ENTRYPOINT ["java", "-jar", "app.jar"]