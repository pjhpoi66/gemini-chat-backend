## Gemini API 캐릭터 채팅 (Backend)

Google Gemini API를 사용하는 캐릭터 채팅 웹 애플리케이션의 백엔드 서버입니다. RESTful API를 통해 사용자 인증, 채팅 데이터 관리, Gemini API 연동 기능을 제공합니다.

https://minga666.com

## 주요 기능

JWT 기반 인증: Spring Security와 JWT(JSON Web Token)를 이용한 사용자 회원가입 및 로그인 기능을 제공합니다.

채팅 데이터 관리: 채팅방 생성, 사용자의 전체 채팅방 목록 조회, 특정 채팅방의 대화 기록 저장 및 관리 기능을 수행합니다.

Gemini API 연동: 사용자 메시지와 대화 기록, 페르소나를 바탕으로 Google Gemini API를 호출하고 응답을 처리합니다.

데이터베이스 연동: Spring Data JPA를 사용하여 PostgreSQL 데이터베이스의 데이터를 관리합니다.

## API Endpoints

- POST	/api/auth/signup	회원가입	X
- POST	/api/auth/login	로그인 (JWT 토큰 발급)	X
- POST	/api/chats	새 채팅방 생성	O
- GET	/api/chats	사용자의 전체 채팅방 목록 조회	O
- GET	/api/chats/{sessionId}/messages	특정 채팅방의 대화 내역 조회	O
- POST	/api/chats/{sessionId}/messages	메시지 전송 및 AI 응답 받기	O

## 기술 스택

Framework: Spring Boot 3.4.10

Language: Java 21

Build Tool: Gradle

Authentication: Spring Security, JWT

Database: PostgreSQL

ORM: Spring Data JPA (Hibernate)

Containerization: Docker

##시작하기

Repository 클론:

```bash
https://github.com/pjhpoi66/gemini-chat-backend.git
```
JAR 파일 빌드:

```bash
./gradlew build
```

Docker 이미지 빌드 및 실행:

프로젝트에 포함된 Dockerfile을 사용하여 컨테이너 이미지로 빌드하고 실행할 수 있습니다.

```bash
# 이미지 빌드
docker build -t gemini-backend .

# 컨테이너 실행
docker run -p 8081:8081 [다른 옵션들] gemini-backend
```
