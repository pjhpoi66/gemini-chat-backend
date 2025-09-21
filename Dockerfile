# --- 1. 빌드 단계 (Builder Stage) ---
# Java 21과 Gradle이 설치된 이미지를 사용하여 프로젝트를 빌드합니다.
FROM gradle:8.5-jdk21 AS builder

# 작업 디렉터리 설정
WORKDIR /build

# 전체 프로젝트 파일을 복사
COPY . .

# Gradle을 사용하여 실행 권한을 부여하고 프로젝트를 빌드합니다.
# 빌드 캐시를 활용하여 다음 빌드부터 속도를 높입니다.
RUN chmod +x ./gradlew
RUN ./gradlew build --no-daemon


# --- 2. 실행 단계 (Final Stage) ---
# 실제 서비스에 사용할 가벼운 Java 21 런타임 이미지를 사용합니다.
FROM eclipse-temurin:21-jre-jammy

# 작업 디렉터리 설정
WORKDIR /home/ec2-user/gemini-chat-backend

# 빌드 단계(builder)에서 생성된 JAR 파일을 복사해옵니다.
# JAR 파일 이름은 본인 프로젝트에 맞게 수정해야 할 수 있습니다.
COPY --from=builder /build/libs/gemini-chat-0.0.1-SNAPSHOT.jar app.jar

# 컨테이너 외부로 노출할 포트를 지정합니다 (백엔드는 8081 포트 사용).
EXPOSE 8081

# 컨테이너가 시작될 때 실행할 명령어를 정의합니다.
ENTRYPOINT ["java", "-jar", "app.jar"]