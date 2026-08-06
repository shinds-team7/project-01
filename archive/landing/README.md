# 보관: 데스크톱 랜딩 페이지

`src/main/resources/templates/places.html` 로 서비스되던 **데스크톱 웹 랜딩 페이지**입니다.

앱 전체를 Pet NOW 앱 프로토타입(`PetNow.dc.html`) 기준의 모바일 앱 디자인으로 통일하면서
라우팅에서 내렸습니다. 삭제하지 않고 여기에 보관합니다.

## 보관 이유

프로토타입에는 대응하는 화면이 없지만, 서비스 소개·마케팅 진입점이 따로 필요해질 경우
(예: 검색 엔진 유입용 랜딩, 투자/발표용 소개 페이지) 재사용할 수 있습니다.

## 구성

| 파일 | 설명 |
|---|---|
| `landing.html` | 랜딩 페이지 템플릿 (원래 이름 `places.html`) |
| `pet-now.js` | 랜딩 전용 스크립트 — 필터·지도 토글·다이얼로그·토스트 |
| `pet-now-api.js` | 랜딩 전용 스크립트 — `/api/frontend/capabilities` 조회 후 미연동 기능 잠금 처리 |

## 되살리는 방법

1. `landing.html` 을 `src/main/resources/templates/` 아래로 옮깁니다.
2. `pet-now.js`, `pet-now-api.js` 를 `src/main/resources/static/js/` 아래로 옮깁니다.
3. 컨트롤러에 매핑을 추가합니다. 앱의 `/` 는 홈 화면이 쓰고 있으므로 `/about` 등 다른 경로를 권합니다.

## 주의

이 템플릿은 `css/pet-now.css`, `css/app-flow.css` 에 의존합니다. 두 파일은 남은 화면들이
앱 디자인으로 전환되는 동안 `src/main/resources/static/css/` 에 유지되다가, 전환이 끝나면
이 폴더로 함께 옮겨집니다.
