# 명함 관리 시스템 (Business Card Management System)

## 프로젝트 소개

명함을 효율적으로 관리할 수 있는 웹 기반 시스템입니다. 명함 정보를 등록, 수정, 삭제하고 엑셀 파일로 일괄 업로드/다운로드할 수 있으며, 이메일 주소만 추출하여 내보낼 수 있습니다.

## 주요 기능

### 1. 명함 관리
- 명함 등록/수정/삭제
- 명함 상세 정보 조회
- 이전/다음 명함 네비게이션
- 카테고리별 분류 (일반기업, 학교, 협회, 관공서)

### 2. 검색 및 정렬
- 다중 조건 검색
  - 전체 필드 검색
  - 이름으로 검색
  - 회사명으로 검색
  - 직함으로 검색
  - 주소로 검색
- 카테고리 필터링
- 정렬 옵션
  - 최근 저장순
  - 오래된순
  - 이름순 (가나다순)
- 페이징 (20개씩 표시)

### 3. 엑셀 업로드/다운로드
- 엑셀 파일(.xlsx) 일괄 업로드
- 중복 처리 옵션 (건너뛰기/덮어쓰기)
- 검색 결과 엑셀 다운로드
- 빈 셀 안전 처리

### 4. 이메일 추출 기능
- 이메일 주소만 TXT 파일로 다운로드
- 중복 이메일 자동 제거
- 회사별 제외 필터 (부분 매칭)
- 세미콜론(;) 구분자 자동 추가
- 10개씩 줄바꿈 정렬

### 5. 중복 관리
- 중복 명함 자동 감지
  - 이름 + 휴대폰
  - 이름 + 이메일
- 중복 제거 전략 선택
  - 가장 오래된 것 유지
  - 가장 최근 것 유지
- 개별 삭제 기능

### 6. 통계 및 대시보드
- 전체 명함 수 표시
- 카테고리별 통계
- 최근 등록 명함 목록

### 7. 사용자 인증
- 회원가입/로그인
- 비밀번호 암호화 (BCrypt)
- 세션 기반 인증
- 사용자별 데이터 격리

## 기술 스택

### Backend
- Java 17
- Spring Boot 4.0.0
- Spring Data JPA
- Spring Security
- MySQL

### Frontend
- Thymeleaf
- HTML5 / CSS3
- JavaScript

### Library
- Apache POI 5.3.0
- Lombok

### Build Tool
- Gradle


## 데이터베이스 스키마

### users 테이블
```sql
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    name VARCHAR(50) NOT NULL,
    created_at DATETIME,
    last_login DATETIME
);
```

### business_card 테이블
```sql
CREATE TABLE business_card (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    company VARCHAR(100),
    department VARCHAR(50),
    position VARCHAR(50),
    address VARCHAR(800),
    office_phone VARCHAR(500),
    office_fax VARCHAR(500),
    mobile_phone VARCHAR(500),
    email VARCHAR(100),
    website VARCHAR(100),
    category VARCHAR(20) NOT NULL,
    notes VARCHAR(500),
    user_id BIGINT NOT NULL,
    created_at DATETIME,
    updated_at DATETIME,
    FOREIGN KEY (user_id) REFERENCES users(id)
);
```

## 설치 및 실행

### 1. 사전 요구사항
- JDK 17 이상
- MySQL 8.0 이상
- Gradle 8.x

### 2. 데이터베이스 설정

MySQL에서 데이터베이스 생성:
```sql
CREATE DATABASE card_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 3. 애플리케이션 설정

`src/main/resources/application.properties` 파일 수정:
```properties
# Database Configuration
spring.datasource.url=jdbc:mysql://localhost:3306/card_db?useSSL=false&serverTimezone=Asia/Seoul&characterEncoding=UTF-8
spring.datasource.username=username
spring.datasource.password=your_password

# JPA Configuration
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
```

### 4. 빌드 및 실행

```bash
# 프로젝트 빌드
./gradlew build

# 애플리케이션 실행
./gradlew bootRun
```

### 5. 접속

브라우저에서 `http://localhost:8081` 접속

## 엑셀 업로드 형식

### 엑셀 파일 구조
엑셀 파일의 첫 번째 행부터 데이터를 입력하세요.

| 열 | 필드명 | 필수 여부 | 설명 |
|---|---|---|---|
| A | 이름 | 필수 | 명함 소유자 이름 |
| B | 회사 | 선택 | 회사명 |
| C | 부서 | 선택 | 부서명 |
| D | 직함 | 선택 | 직책/직함 |
| E | 주소 | 선택 | 회사 또는 개인 주소 |
| F | 근무처 전화 | 선택 | 회사 전화번호 |
| G | 근무처 팩스 | 선택 | 회사 팩스번호 |
| H | 휴대폰 | 선택 | 휴대전화번호 |
| I | 이메일 | 선택 | 이메일 주소 |
| J | 웹사이트 | 선택 | 홈페이지 주소 |
| K | 비고 | 선택 | 추가 메모 |



### 중복 처리
- 이름 + 휴대폰이 같으면 중복으로 판단
- 이름 + 이메일이 같으면 중복으로 판단
- 중복 발견 시: 건너뛰기 또는 덮어쓰기 선택 가능


## 이메일 추출 기능 사용법

### 1. 기본 사용
1. 명함 목록 페이지에서 검색/필터 적용
2. "이메일 다운로드" 버튼 클릭
3. 다운로드 버튼 클릭

### 2. 회사 제외 필터
특정 회사를 제외하고 이메일을 추출할 수 있습니다.


## 주요 비즈니스 로직

### 1. 중복 검사 로직
```
1순위: 이름 + 휴대폰
2순위: 이름 + 이메일
3순위: 휴대폰 단독
4순위: 이름 + 회사 (전화번호와 이메일이 둘 다 없을 때만)
```

### 2. 검색 로직
- 카테고리 필터 + 키워드 검색 + 정렬 조합
- 대소문자 구분 없는 LIKE 검색
- 페이징 처리 (20개씩)

### 3. 데이터 정리 로직
엑셀 업로드 시 자동으로 적용:
- 이메일 유효성 검사
- 전화번호 형식 정리


## 보안

### 인증 및 권한
- 세션 기반 인증
- 로그인하지 않은 사용자는 자동으로 로그인 페이지로 리다이렉트
- 사용자는 자신의 명함만 조회/수정/삭제 가능

### 비밀번호 암호화
- BCrypt 알고리즘 사용
- 솔트(Salt) 자동 생성


## 개발 환경

- IDE: IntelliJ IDEA
- OS: Windows / macOS / Linux
- Java: OpenJDK 17
- Database: MySQL 8.0.33

---

Made with Spring Boot
