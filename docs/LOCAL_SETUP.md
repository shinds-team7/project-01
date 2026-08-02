# 로컬 개발 환경 세팅

petNow 를 로컬에서 처음 실행할 때 한 번만 하면 되는 작업입니다.

| 항목 | 값 |
| --- | --- |
| DB | MariaDB 11.4 |
| DB 이름 | `petnow` |
| 포트 | `3306` |
| 계정 | `petnow` / `petnow1234` |
| 스키마 DDL | [`docs/db/schema.sql`](db/schema.sql) |
| 앱 포트 | `8080` |

---

## 1. DB 띄우기

### 방법 A. Docker (권장)

프로젝트 루트에서:

```bash
docker compose up -d
```

최초 기동 시 `docs/db/schema.sql` 이 자동으로 실행되어 `petnow` DB와 테이블 15개가 생성됩니다.

확인:

```bash
docker compose ps
docker exec -it petnow-mariadb mariadb -upetnow -ppetnow1234 petnow -e "SHOW TABLES;"
```

스키마를 갈아엎고 다시 만들고 싶을 때 (데이터 전부 삭제됨):

```bash
docker compose down -v && docker compose up -d
```

### 방법 B. 로컬에 설치된 MariaDB 사용

이미 MariaDB 가 깔려 있다면 root 로 접속해서 계정과 DB를 만들고 DDL을 실행합니다.

```sql
CREATE USER 'petnow'@'localhost' IDENTIFIED BY 'petnow1234';
GRANT ALL PRIVILEGES ON petnow.* TO 'petnow'@'localhost';
FLUSH PRIVILEGES;
```

```bash
mariadb -uroot -p < docs/db/schema.sql
```

> `schema.sql` 은 맨 앞에서 `DROP TABLE IF EXISTS` 를 수행합니다. **기존 petnow DB에 실행하면 데이터가 전부 날아갑니다.**

### DataGrip / IntelliJ Database 연결 정보

```
Host     : localhost
Port     : 3306
Database : petnow
User     : petnow
Password : petnow1234
Driver   : MariaDB
```

---

## 2. `application-local.yaml` 만들기

`application-local.yaml` 은 개인 DB 계정이 들어가므로 **Git에 올리지 않습니다** (`.gitignore` 처리됨).
클론 후 아래 파일을 직접 만들어 주세요.

**경로:** `src/main/resources/application-local.yaml`

```yaml
spring:
  config:
    activate:
      on-profile: local

  datasource:
    url: jdbc:mariadb://localhost:3306/petnow
    username: petnow
    password: petnow1234

  thymeleaf:
    cache: false

logging:
  level:
    com.example.petnow: debug
    com.example.petnow.mapper: trace
```

DB 포트나 계정을 다르게 쓴다면 본인 환경에 맞게 고치면 됩니다. 나머지 설정(MyBatis, 커넥션 풀, 서버 포트 등)은 공통 `application.yaml` 에 있으니 여기서 다시 쓸 필요 없습니다.

---

## 3. 실행

```bash
./gradlew bootRun
```

기본 프로필이 `local` 이라 별도 옵션 없이 실행하면 됩니다.
IntelliJ 에서 실행한다면 Run Configuration → Active profiles 에 `local` 을 넣어도 동일합니다.

접속: http://localhost:8080

---

## 설정 파일 구조

```
src/main/resources/
├── application.yaml        # 공통 (Git O) - 앱 이름, MyBatis, 커넥션 풀, 서버 포트
├── application-local.yaml  # 로컬  (Git X) - 각자 생성
└── application-prod.yaml   # 운영  (Git O) - 값은 전부 환경변수로 주입
```

운영 배포 시에는 아래 환경변수가 필요합니다.

| 환경변수 | 필수 | 기본값 |
| --- | --- | --- |
| `SPRING_PROFILES_ACTIVE` | O | (`prod` 로 지정) |
| `DB_HOST` | O | - |
| `DB_PORT` | X | `3306` |
| `DB_NAME` | X | `petnow` |
| `DB_USERNAME` | O | - |
| `DB_PASSWORD` | O | - |
| `SERVER_PORT` | X | `8080` |

---

## 트러블슈팅

**`Access denied for user 'petnow'@'localhost'`**
계정이 안 만들어졌거나 비밀번호가 다릅니다. Docker 를 쓴다면 `docker compose down -v` 후 다시 올려 주세요. (볼륨이 남아 있으면 환경변수를 바꿔도 계정이 갱신되지 않습니다.)

**`Table 'petnow.users' doesn't exist`**
DDL 이 실행되지 않았습니다. `docker compose down -v && docker compose up -d` 로 초기화 스크립트를 다시 태우거나, 방법 B 로 직접 `schema.sql` 을 실행하세요.

**`Port 3306 is already allocated`**
로컬에 이미 MySQL/MariaDB 가 떠 있습니다. 그걸 그대로 쓰거나(방법 B), `docker-compose.yml` 의 포트를 `"3307:3306"` 으로 바꾸고 `application-local.yaml` 의 URL 도 `3307` 로 맞추세요.

**`Failed to configure a DataSource: 'url' attribute is not specified`**
`application-local.yaml` 을 안 만들었습니다. 2번 단계를 진행하세요.
