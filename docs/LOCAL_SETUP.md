# 로컬 개발 환경 세팅

petNow 를 로컬에서 처음 실행할 때 한 번만 하면 되는 작업입니다.

| 항목 | 값 |
| --- | --- |
| DB | MariaDB 11.4 |
| DB 이름 | `petnow` |
| 포트 | `3306` |
| 계정 | `.env`의 `DB_USERNAME` / `DB_PASSWORD` |
| 스키마 관리 | `src/main/resources/db/migration`의 Flyway SQL |
| 통합 DDL 스냅샷 | [`docs/db/schema.sql`](db/schema.sql) |
| 앱 포트 | `8080` |

---

## 1. DB 띄우기

### 방법 A. Docker (권장)

`docker-compose.yml` 이 DB 계정을 `.env` 에서 읽으므로 **`.env` 를 먼저 만들어야 합니다.**
`.env` 없이 `docker compose up` 을 하면 계정이 빈 값으로 만들어져 나중에 `Access denied` 가 납니다.

프로젝트 루트에서:

```bash
cp .env.example .env
docker compose up -d
```

`.env.example` 의 기본값(`petnow` / `petnow1234`)은 로컬 전용이라 그대로 써도 됩니다.
값을 바꿨다면 2번 단계의 `application-local.yaml` 도 **같은 값으로** 맞춰야 합니다.

컨테이너는 빈 `petnow` 데이터베이스만 준비합니다. 애플리케이션을 처음 실행하면
Flyway가 마이그레이션을 순서대로 적용해 테이블을 생성합니다.

확인:

```bash
docker compose ps
```

스키마를 갈아엎고 다시 만들고 싶을 때 (데이터 전부 삭제됨):

```bash
docker compose down -v
docker compose up -d
./gradlew bootRun
```

### 방법 B. 로컬에 설치된 MariaDB 사용

이미 MariaDB가 깔려 있다면 root로 접속해서 계정과 빈 DB를 만듭니다.

```sql
CREATE DATABASE IF NOT EXISTS petnow
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;
CREATE USER IF NOT EXISTS 'petnow'@'localhost' IDENTIFIED BY 'petnow1234';
GRANT ALL PRIVILEGES ON petnow.* TO 'petnow'@'localhost';
FLUSH PRIVILEGES;
```

테이블은 애플리케이션 실행 시 Flyway가 생성합니다. `docs/db/schema.sql`은 전체
구조를 한 번에 확인하거나 새 DB를 수동 구축할 때 사용하는 스냅샷입니다.
파일 앞부분에 `DROP TABLE`이 있으므로 기존 데이터베이스에는 실행하지 않습니다.

### DataGrip / IntelliJ Database 연결 정보

```text
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
클론 후 예제 파일을 복사해 주세요.

**경로:** `src/main/resources/application-local.yaml`

```bash
cp src/main/resources/application-local.yaml.example \
   src/main/resources/application-local.yaml
```

예제 파일의 계정(`petnow` / `petnow1234`)은 `.env.example` 의 기본값과 일치하므로,
둘 다 기본값을 쓴다면 고칠 것이 없습니다.

DB 포트나 계정을 다르게 쓴다면 본인 환경에 맞게 고치면 됩니다.
이때 **`.env` 의 `DB_USERNAME` / `DB_PASSWORD` 와 반드시 같은 값**이어야 합니다.
나머지 설정(MyBatis, 커넥션 풀, 서버 포트 등)은 공통 `application.yaml` 에 있으니 여기서 다시 쓸 필요 없습니다.

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

```text
src/main/resources/
├── application.yaml        # 공통 (Git O) - 앱 이름, MyBatis, 커넥션 풀, 서버 포트
├── application-local.yaml.example # 로컬 설정 예시 (Git O)
├── application-local.yaml  # 로컬 실제 설정 (Git X)
└── application-prod.yaml   # 운영  (Git O) - 값은 전부 환경변수로 주입
```

운영 배포 시에는 아래 환경변수가 필요합니다.

| 환경변수 | 필수 | 기본값 |
| --- | --- | --- |
| `SPRING_PROFILES_ACTIVE` | O | (`prod` 로 지정) |
| `DB_HOST` | O | - |
| `DB_USERNAME` | O | - |
| `DB_PASSWORD` | O | - |

---

## 트러블슈팅

**`Access denied for user 'petnow'@'localhost'`**
계정이 안 만들어졌거나 비밀번호가 다릅니다. Docker 를 쓴다면 `docker compose down -v` 후 다시 올려 주세요. (볼륨이 남아 있으면 환경변수를 바꿔도 계정이 갱신되지 않습니다.)

**`Table 'petnow.users' doesn't exist`**
애플리케이션이 아직 실행되지 않았거나 Flyway 적용에 실패한 상태입니다.
애플리케이션 로그와 `flyway_schema_history` 테이블을 확인하세요.

**`Port 3306 is already allocated`**
로컬에 이미 MySQL/MariaDB 가 떠 있습니다. 그걸 그대로 쓰거나(방법 B), 아래처럼
`docker-compose.override.yml` 을 만들어 포트만 바꿉니다. (이 파일은 `.gitignore` 되어 있어
공용 `docker-compose.yml` 을 건드리지 않아도 됩니다.)

```yaml
# docker-compose.override.yml
services:
  mariadb:
    ports: !override
      - "3307:3306"
```

그리고 `application-local.yaml` 의 URL 도 `3307` 로 맞춥니다.

> `!override` 를 빼면 포트 설정이 교체되지 않고 **덧붙여져서** 3306 을 그대로 물기 때문에
> 똑같은 에러가 다시 납니다.

**`Failed to configure a DataSource: 'url' attribute is not specified`**
`application-local.yaml` 을 안 만들었습니다. 2번 단계를 진행하세요.
