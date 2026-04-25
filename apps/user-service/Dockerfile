FROM gradle:8.5-jdk17 AS builder
WORKDIR /app

# 루트 빌드 설정 파일 복사
COPY build.gradle settings.gradle gradlew ./
COPY gradle ./gradle

# common-libs 복사
COPY common ./common

# user-service 소스 복사
COPY apps/user-service ./apps/user-service

RUN chmod +x gradlew
RUN ./gradlew :apps:user-service:clean :apps:user-service:build -x test --no-daemon

FROM eclipse-temurin:17-jre

WORKDIR /app

COPY --from=builder /app/apps/user-service/build/libs/*.jar app.jar

EXPOSE 8082

ENTRYPOINT ["java", "-jar", "app.jar"]