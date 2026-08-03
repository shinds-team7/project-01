# project-01
신한DS 금융SW 아카데미 7기 1차 프로젝트

## 로컬 실행 방법

1. `.env.example`을 복사해 `.env`를 만들고 DB 계정 값을 채운다.
   ```
   cp .env.example .env
   ```
2. docker-compose로 로컬 MariaDB를 띄운다.
   ```
   docker-compose up -d
   ```
3. `src/main/resources/application-local.yaml.example`을 복사해
   `application-local.yaml`을 만들고, 1번에서 채운 값과 동일하게 계정 정보를 맞춘다.
   (`application-local.yaml`은 `.gitignore`에 등록되어 있어 커밋되지 않는다.)
4. 애플리케이션을 실행한다. 최초 실행 시 Flyway가 `db/migration`의 SQL을 순서대로 적용해
   테이블을 생성한다.
   ```
   ./gradlew bootRun
   ```
