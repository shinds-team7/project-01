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

## 카카오 주소 지오코딩

장소 등록 시 도로명 주소를 좌표로 변환하려면 카카오 개발자 콘솔의 **REST API 키**를
`KAKAO_REST_API_KEY` 환경변수로 주입한다. 이 키는 서버 전용이며 HTML이나 JavaScript에
노출하면 안 된다. 키가 없거나 카카오 API 호출이 실패해도 장소 등록은 성공하고 좌표만 비워 둔다.

기존 `place_addresses`의 빈 좌표는 아래처럼 백필을 명시적으로 켠 한 번의 실행에서 채운다.
기본 배치 크기는 100건이며 `KAKAO_LOCAL_API_BACKFILL_BATCH_SIZE`로 최대 1,000건까지 조절할 수 있다.
카카오 일일 쿼터를 확인한 뒤 실행하고, 완료 후 `KAKAO_LOCAL_API_BACKFILL_ENABLED`를 반드시 끈다.

```powershell
$env:KAKAO_REST_API_KEY = "발급받은-REST-API-키"
$env:KAKAO_LOCAL_API_BACKFILL_ENABLED = "true"
./gradlew.bat bootRun
```
