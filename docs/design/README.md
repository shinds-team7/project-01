# 프로토타입 원본

`PetNow.dc.html` 은 Pet NOW 앱의 디자인 원본입니다. 화면 19개와 바텀시트·모달 10개가
한 파일에 인라인 스타일로 들어 있습니다.

## 왜 repo 에 넣었나

이 파일이 repo 밖 로컬에만 있어서 이슈 #183 이 생겼습니다.

- 프로토타입 원본이 한 사람의 로컬에만 있었다
- 이식본인 `app.css` 만 커밋됐다
- 그 `app.css` 를 담은 PR #110 이 머지 없이 닫혔다
- 디자인의 유일한 사본이 agent 브랜치에 갇혀 develop 에 한 줄도 반영되지 않았다

원본이 repo 에 있으면 브랜치가 또 닫혀도 근거는 남습니다.

## 여는 방법

`PetNow.dc.html` 을 브라우저로 그냥 열면 됩니다. 같은 폴더의 `support.js` 가
`<x-dc>` / `<sc-if>` / `<sc-for>` 커스텀 태그를 렌더하는 런타임입니다.
둘은 같이 있어야 하고, `support.js` 는 생성물이라 직접 고치지 않습니다.

## 화면 목록과 이식 상태

| 프로토타입 화면 | 이식된 템플릿 |
| --- | --- |
| HOME | `home.html` |
| SEARCH LIST + MAP | `places/list.html` |
| SEARCH INPUT | 없음 (`/search` → `coming-soon.html`) |
| NEARBY | `nearby.html` |
| DETAIL | `place-detail.html` |
| BOOKING REQUEST | `booking-request.html` |
| PAYMENT | `payment.html` (매핑 없음) |
| SUCCESS | `reservations/success.html` |
| MY PAGE | `mypage.html` |
| BOOKING LIST | `reservations/reservationList.html` |
| BOOKING DETAIL | `reservations/reservationDetail.html` |
| BOOKMARKS | 없음 (`/bookmarks` → `coming-soon.html`) |
| REVIEWS | `reviews/list.html` |
| REVIEW WRITE | `reviews/create.html` |
| HOST HOME | `host/dashboard.html` |
| HOST BOOKING DETAIL | `host/booking-detail.html` |
| PET CREATE | `pet-form.html`, `mypage/petUpdate.html` |
| HOST REVIEWS | `host/reviews.html` (매핑 없음) |
| HOST POST CREATE | `host/create.html` |

프로토타입에 원본이 없는 화면: `auth/login.html`, `auth/signup.html`,
`host/success.html`, `mypage/petDelete.html`, `coming-soon.html`, `error.html`.
이 화면들은 `app.css` 의 공용 조각으로 결만 맞췄습니다.
