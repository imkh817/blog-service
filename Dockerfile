# =============================================
# 1단계: 빌드
# =============================================
FROM gradle:8.5-jdk21 AS build

WORKDIR /app

# 의존성 캐시 최적화 (소스 변경 시 재다운로드 방지)
COPY build.gradle settings.gradle ./
COPY gradle ./gradle
RUN gradle dependencies --no-daemon || true

# 소스 복사 및 빌드 (테스트 제외)
COPY src ./src
RUN gradle bootJar --no-daemon -x test

# =============================================
# 2단계: 실행
# =============================================
FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=prod", "app.jar"]