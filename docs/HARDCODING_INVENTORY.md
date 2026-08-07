# develop 하드코딩 인벤토리

기준 커밋: `origin/develop` (`9586db2`)

화면의 Thymeleaf fallback 문구처럼 실제 모델 값이 없을 때만 보이는 예시 데이터와,
서버 로직·설정에 직접 박힌 운영 값을 구분해 조사했다. 색상·간격·반응형 수치와 외부
라이브러리의 공식 URL은 디자인 토큰 또는 의존성 선언으로 보고 이번 목록에서 제외했다.

## 정리 대상 및 작업 단위

| 영역 | 위치 | 하드코딩 내용 | 처리 PR | 상태 |
|---|---|---|---|---|
| 세션 키·예약 유형 | `controller/HostController.java`, `controller/PlaceController.java`, `service/ReservationServiceImpl.java` | `"loginUserId"`, `"당일"`, `"숙박"` 문자열이 로직에 직접 결합 | [#84](https://github.com/shinds-team7/project-01/pull/84) | 기존 PR |
| 마이페이지 샘플 데이터 | `templates/mypage.html`, `templates/mypage/index.html` | 사용자명, 반려동물, 예약·리뷰 예시 데이터 | [#80](https://github.com/shinds-team7/project-01/pull/80) | 기존 PR |
| 로컬 DB 설정 | `application-local.yaml.example`, `.env.example`, `docs/LOCAL_SETUP.md` | `localhost`, 예시 계정·비밀번호, 파일별 불일치 기본값 | [#104](https://github.com/shinds-team7/project-01/pull/104) | 기존 PR |
| 예약번호 생성 | `entity/Reservation.java` | `PN`, `yyyyMMdd`, 랜덤 코드 길이 `8` | [#113](https://github.com/shinds-team7/project-01/pull/113) | 신규 PR |
| 예약·결제 화면 예시 | `templates/booking-*.html`, `templates/payment.html`, `templates/reservations/reservationDetail.html`, `templates/reviews/*.html` | `지우`, `초코`, 날짜·시간·금액 등의 Thymeleaf fallback | 후속 화면 연동 작업 | 별도 작업 필요 |
| 홈·호스트 탐색 예시 | `templates/home.html`, `templates/nearby.html`, `templates/place-detail.html`, `templates/host/*.html` | 장소명, 지역, 평점, 거리, 호스트 프로필 등 정적 카드 데이터 | 장소 조회 모델 연결 후 분리 | 별도 작업 필요 |
| 프론트엔드 API 경로 | `static/js/pet-now-api.js`, `static/js/pet-form.js`, `static/js/host-place.js` | 엔드포인트 문자열이 여러 JS 파일에 분산 | API 경로 설정 모듈화 | 별도 작업 필요 |

## 제외한 항목

- `PlaceType`, `PlaceStatus`, `ReservationStatus`, 오류 코드 enum의 값: 도메인 계약값이므로 임의 상수화하지 않는다.
- Bean Validation의 최대 길이·최대 수용량: 현재 DTO와 DB 스키마의 입력 제약이며, 정책 변경 시 함께 변경해야 한다.
- Thymeleaf·MyBatis·Google Fonts 등의 공식 외부 URL: 애플리케이션 환경별로 바뀌는 서버 설정값이 아니다.
- CSS의 색상·간격·반응형 수치: 디자인 토큰 후보이지 서버 하드코딩으로 분류하지 않는다.

## 완료 기준

각 후속 PR은 한 영역의 예시 값 또는 정책 값을 모델·설정·도메인 타입으로 옮기고,
기존 라우팅·DB 저장 형식을 유지하며, 해당 영역을 검증하는 테스트 또는 렌더링 확인을 포함한다.
