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

운영(EC2)에서는 `~/app/.env`에 아래 한 줄을 넣고 다시 올린 뒤, 로그로 결과를 확인하고
그 줄을 지워 다시 올린다. 배포 워크플로는 이 값을 내보내지 않으므로 `.env`가 그대로 적용된다.

```bash
echo 'KAKAO_LOCAL_API_BACKFILL_ENABLED=true' >> ~/app/.env
docker compose -f docker-compose.prod.yml up -d
docker compose -f docker-compose.prod.yml logs app | grep 백필
```

## 카카오 지도 (내 주변)

`/nearby`의 지도는 **JavaScript 키**를 `KAKAO_MAP_JAVASCRIPT_KEY`로 받는다. 이 키는 HTML에
그대로 실려 나가는 것이 정상이라 숨길 수 없고, 카카오 개발자 콘솔의 **플랫폼 > Web > 사이트 도메인**에
`http://localhost:8080`과 `https://petnow.duckdns.org`를 등록해 사용처를 제한하는 것으로 막는다.
키가 비어 있으면 지도 없이 목록만 그린다. 앱은 그대로 뜬다.

REST API 키를 이 자리에 잘못 넣으면 서버 전용 키가 모든 방문자에게 노출되므로,
두 키가 같은 값이면 `KakaoKeyGuard`가 기동을 막는다.
