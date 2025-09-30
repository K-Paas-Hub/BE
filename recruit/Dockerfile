# ---------- Build Stage ----------
FROM eclipse-temurin:17-jdk AS builder
WORKDIR /app

# Gradle Wrapper & 설정만 복사 (의존성 캐시 최적화)
COPY gradlew .
COPY gradle gradle
COPY build.gradle settings.gradle ./

# 의존성만 먼저 다운로드 (캐시 활용)
RUN ./gradlew dependencies --no-daemon || return 0

# 전체 소스 복사
COPY . .

# 깨끗하게 빌드 (테스트 제외)
RUN ./gradlew clean build -x test --no-daemon

# ---------- Run Stage ----------
FROM eclipse-temurin:17-jre
WORKDIR /app

# 기본 환경변수 (prod profile 활성화)
ENV SPRING_PROFILES_ACTIVE=prod

# 빌드 결과만 복사
COPY --from=builder /app/build/libs/*.jar app.jar

# 실행 (환경변수에 따라 profile 달라짐)
ENTRYPOINT ["java","-jar","/app/app.jar"]
