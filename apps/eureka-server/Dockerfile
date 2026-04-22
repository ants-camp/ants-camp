FROM gradle:8.5-jdk17 AS builder
WORKDIR /app

# 루트 빌드 설정 파일 복사
COPY build.gradle settings.gradle gradlew ./
COPY gradle ./gradle

# common-libs 복사
COPY common ./common

# eureka-service 소스 복사
COPY eureka-server ./eureka-server

RUN chmod +x gradlew
RUN ./gradlew :eureka-server:clean :eureka-server:build -x test --no-daemon

FROM eclipse-temurin:17-jre

WORKDIR /app

COPY --from=builder /app/eureka-server/build/libs/*.jar app.jar

EXPOSE 8761

ENTRYPOINT ["java", "-jar", "app.jar"]
