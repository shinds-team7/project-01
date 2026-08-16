# CONVENTIONS.md — petNow 팀 공통 규칙

팀 전원이 지켜야 하는 규칙만 모았다. PR 을 올리기 전에 마지막 장의 셀프 체크리스트를 확인한다.

---

## 1. 네이밍

| 대상 | 규칙 | 예 |
|------|------|-----|
| 클래스 | PascalCase | `UserService`, `ReservationController` |
| 메서드 | camelCase + **동사로 시작** | `createReservation()`, `findByEmail()` |
| 변수 | camelCase | `detailAddress`, `birthYear` |
| 상수 / enum 값 | UPPER_SNAKE_CASE | `DUPLICATE_EMAIL`, `PENDING` |
| Boolean | `is` / `has` 접두 | `isNeutered`, `hasPermission` |

- 축약어를 만들지 않는다. `Reserv` → `Reservation`, `ec` → `errorCode`.
- 메서드는 동사만으로 끝내지 않는다. `insert()` → `insertPlace()`.

> **필드명을 바꾸면 3곳을 함께 바꿔야 한다.**
> ① Entity / DTO 필드명 ② Mapper XML 의 `#{...}` ③ Thymeleaf 의 `${...}`
> DB 컬럼명은 바꾸지 않아도 된다. `map-underscore-to-camel-case` 는 SELECT 결과 매핑에만 적용되므로,
> 컬럼명과 필드명이 달라지는 SELECT 에는 `AS` 별칭을 붙인다. 예: `SELECT neutered AS is_neutered`

---

## 2. 패키지 구조 — 레이어 우선(layer-first)

도메인별 패키지를 만들지 않고, 레이어별 패키지 아래에 도메인 클래스를 나란히 둔다. 패키지명은 전부 소문자.

```
com.example.petnow
├── common          ← 전 도메인 공용 (config, constant, controller, domain)
├── config
├── controller
├── dto
│   ├── request
│   └── response
├── entity
├── exception
├── mapper
└── service
```

MyBatis 를 쓰므로 `repository` 레이어는 존재하지 않는다.

---

## 3. DTO

- 요청 DTO → `dto.request`, 응답 DTO → `dto.response`. `dto` 바로 아래에 두지 않는다.
- 이름은 `{도메인}{동작}Request` / `{도메인}{동작}Response`. **`DTO` 접미사 금지.**
- **DTO ↔ Entity 변환은 `ServiceImpl` 안에서 빌더로** 한다. DTO 에 `toEntity()` 를 두지 않는다.
  판단 로직(`optionEtc ? optionText : null` 같은)이 DTO 에 있으면 안 된다. 그건 서비스의 일이다.
- 목록 조회처럼 SELECT 결과를 그대로 받는 경우는 Mapper XML 의 `resultType` 에 응답 DTO 를 직접 지정해도 된다.

---

## 4. 코드 스타일

- **들여쓰기는 스페이스 4칸.** 파일마다 개별로 고치지 말 것 — 섞어서 고치면 diff 가 폭발해서 리뷰가 불가능해진다.
- 중괄호는 같은 줄에서 시작하고 앞에 공백을 둔다. `implements PlaceService {`
- 의미 있는 이름을 쓴다. `result`, `ec`, `AAA` 금지.
- **주석 처리된 죽은 코드를 남기지 않는다.** Git 에 이력이 남는다.
- 바로 아래 코드가 그대로 말해주는 주석은 쓰지 않는다. (`// Pet Entity 생성`)
- **코드와 같은 줄에 주석을 쓰지 않는다.** 메서드 주석은 선언 위에 Javadoc 으로.
- 나중에 할 일은 코드에 TODO 로 남기지 말고 **GitHub Issue 로 옮긴다.**

---

## 5. 예외 처리

전역 예외 처리로 통일한다. 도메인별로 제각각 try-catch 하거나 컨트롤러에서 직접 에러 응답을 만들지 않는다.

`ErrorCode` 는 **인터페이스**이고, 도메인별 enum 이 이를 구현한다. (`AuthErrorCode`, `UserErrorCode`, `PlaceErrorCode`, `CommonErrorCode` …)

```java
// Service 에서
throw new BusinessException(UserErrorCode.USER_NOT_FOUND);

// 메시지를 바꿔야 하면
throw new BusinessException(CommonErrorCode.FORBIDDEN, "본인의 예약만 취소할 수 있습니다");
```

**에러 코드 추가 규칙**

- 자기 도메인의 enum 에 추가한다. 여러 도메인이 함께 쓰는 것만 `CommonErrorCode` 에 넣는다.
- HTTP 상태 그룹 주석(`// 400`, `// 404` …) 안, 맨 끝에 추가한다.
- 이름은 `{도메인}_{사유}` 형태: `PLACE_NOT_FOUND`, `RESERVATION_ALREADY_CANCELED`
- 기본 메시지 끝에 마침표를 붙이지 않는다.

**표준 예외를 직접 던지지 않는다.** `IllegalStateException`, `IllegalArgumentException` 금지 → `BusinessException`.

화면(MVC) 예외는 `MvcExceptionHandler`, JSON 응답은 `GlobalExceptionHandler` 가 담당한다. 둘의 역할을 섞지 않는다.

---

## 6. 엔드포인트

- **URL 에 `/api` 를 붙이지 않는다.** MVC 구조다.
- 리소스는 **복수형 소문자**: `/pets`, `/places`, `/reviews`, `/reservations`
- 호스트(장소 주인) 전용 화면은 `/host` 접두: `/host/places`
- 클래스에 `@RequestMapping("/도메인")` 을 두고, 메서드에는 나머지 경로만 쓴다.
- URL 에 동사를 넣지 않는다. `/addpet` → `POST /pets`

---

## 7. 레이어별 규칙

### 7.1 Controller

이 프로젝트는 Spring MVC + Thymeleaf 다. **`@RestController` 를 쓰지 않는다.**

| 상황 | 반환 |
|------|------|
| 화면을 그린다 | `@Controller` + 뷰 이름 → `return "mypage/index";` |
| 데이터를 변경한 뒤 이동한다 | `redirect:` → `return "redirect:/mypage";` |
| 화면 데이터 전달 | `Model` 파라미터 + `model.addAttribute(...)` |
| 폼 데이터 수신 | `@ModelAttribute` (생략 가능) |

**`@RequestBody` 를 쓰지 않는다.** HTML `<form>` 은 `application/x-www-form-urlencoded` 로 보내므로 **415 Unsupported Media Type** 이 난다.

**PRG(Post-Redirect-Get) 를 지킨다.** POST 처리 후 뷰 이름을 반환하면 새로고침 시 중복 등록이 된다.

```java
// 나쁨 — 새로고침하면 또 등록된다
@PostMapping
public String addPet(@ModelAttribute PetCreateRequest request) {
    petService.createPet(userId, request);
    return "mypage";
}

// 좋음
@PostMapping
public String addPet(@ModelAttribute PetCreateRequest request) {
    petService.createPet(userId, request);
    return "redirect:/mypage";
}
```

로그인 사용자 ID 는 세션에서 꺼낸다. **하드코딩 금지.** (`common/constant/SessionConst` 참고)

### 7.2 Service

- `XxxService`(인터페이스) + `XxxServiceImpl`(구현체) 로 나눈다.
- 의존성 주입은 `private final` 필드 + 클래스에 `@RequiredArgsConstructor`. 생성자를 손으로 쓰지 않는다.

**트랜잭션 경계는 `ServiceImpl` 의 메서드 단위다.**

- `@Transactional` 은 **메서드 위**에 붙인다. 클래스 전체에 붙이면 조회까지 쓰기 트랜잭션이 된다.
- INSERT / UPDATE / DELETE 하는 메서드에는 **반드시** 붙인다. 특히 **DB 를 두 번 이상 건드리는 메서드**는 필수다.
- 조회 전용 메서드에는 `@Transactional(readOnly = true)`.
- import 는 `org.springframework.transaction.annotation.Transactional` (jakarta 아님).
- Controller / Mapper 에는 붙이지 않는다.

### 7.3 Entity

- Lombok 조합: `@Getter` `@Setter` `@NoArgsConstructor` `@AllArgsConstructor` `@Builder`
  **`@Data` 금지** — `equals`/`hashCode`/`toString` 이 자동 생성되어 연관 객체가 있으면 무한 재귀가 난다.
- 생성/수정 일시는 **`BaseEntity` 를 상속**해서 쓴다. `createdAt` / `updatedAt` 을 직접 선언하지 않는다.
  `@Builder` 는 상위 클래스 필드를 포함하지 않으므로 `@SuperBuilder` 가 필요한지 확인할 것.
- enum 은 **별도 top-level 파일**로 만든다. Entity 안 중첩 enum 금지.
- 쓰지 않는 import 를 남기지 않는다. 특히 IDE 자동완성으로 들어오는 Spring 내부 클래스를 주의할 것.

### 7.4 Mapper 인터페이스

- 위치는 `com.example.petnow.mapper`, 이름은 `{도메인}Mapper`, 어노테이션은 `@Mapper`.
- 메서드 이름은 **SQL 동작 + 대상**: `insertPet`, `findByUserId`, `updatePet`, `deleteByPetId`
  비즈니스 유스케이스 이름(`signup`)을 쓰지 않는다. 서비스와 이름이 겹쳐 계층 구분이 안 된다.
- 파라미터가 원시 타입 / `Long` / `String` 이면 **개수와 무관하게 `@Param` 을 붙인다.**
  DTO / Entity 하나만 넘길 때는 붙이지 않는다.
- **어노테이션 SQL(`@Select`, `@Insert`) 금지.** SQL 은 전부 XML 에 쓴다.

### 7.5 Mapper XML

1. 위치는 `src/main/resources/mapper/` (**단수형**). `application.yaml` 의 `mapper-locations` 가 이 경로만 읽는다.
2. 파일명은 인터페이스와 동일: `PetMapper.java` ↔ `PetMapper.xml`
3. DTD 는 `https://` 로 쓴다.
4. `namespace` / `resultType` / `parameterType` 은 **완전 경로**로 쓴다.
5. **`id` 는 인터페이스 메서드명과 정확히 같아야 한다.** 다르면 런타임에 죽는다.
6. **인터페이스에 없는 statement 를 남기지 않는다.**
7. PK 를 돌려받아야 하는 INSERT 는 `useGeneratedKeys="true" keyProperty="id"`
8. **`created_at` / `updated_at` 은 INSERT / UPDATE 문에 넣지 않는다.** DB 가 `DEFAULT CURRENT_TIMESTAMP` / `ON UPDATE CURRENT_TIMESTAMP` 로 채운다.
9. **`SELECT *` 금지.** 컬럼을 전부 나열한다.
10. 컬럼명과 필드명이 다르면 `AS` 별칭을 붙인다. 안 붙이면 조용히 `null` 이 된다.
11. 연관 객체가 있으면 `resultMap` + `<association>` 을 쓴다.

### 7.6 Thymeleaf 템플릿

- 경로는 `templates/{도메인}/{화면}.html`. **`templates/` 바로 아래에 파일을 두지 않는다.**
- 컨트롤러가 반환하는 뷰 이름은 확장자 없는 경로: `return "host/places/success";`
- 파일명은 소문자 + 하이픈: `place-detail.html` (camelCase 금지)
- `<html lang="ko">` 를 쓰고, `<title>` 에 의미 있는 값을 넣는다.
- 모든 화면은 공통 레이아웃 프래그먼트(`fragments/layout`, `header`, `footer`)를 쓴다.
- 템플릿에서 getter 를 직접 호출하지 않는다. 프로퍼티 표기 + null 안전 연산자를 쓴다.
  `${pet.getSizeCode().getLabel()}` → `${pet.sizeCode?.label}`

### 7.7 정적 리소스

```
src/main/resources/static/
├── css/     common.css, {도메인}.css
├── js/      common.js, {도메인}.js
└── images/
```

- **HTML 안에 `<style>` / `<script>` 블록을 쓰지 않는다.** 전부 위 파일로 뺀다.
- 템플릿에서 참조할 때는 반드시 `@{...}` 를 쓴다 (context path 대응).
  ```html
  <link rel="stylesheet" th:href="@{/css/common.css}">
  <script th:src="@{/js/place.js}"></script>
  ```
- 파일명은 소문자 + 하이픈: `place-detail.css`

---

## 8. 설정 파일

- `application.yaml` 은 **전 팀 공용이다. 개인 브랜치에서 임의로 수정하지 않는다.**
  바꿔야 하면 팀 채널에 공유하고 develop 에 반영한다.
- **자격증명을 파일에 직접 쓰지 않는다.** 환경변수로 주입한다. 로컬 실행 방법은 `docs/LOCAL_SETUP.md` 참고.
- 스키마 변경은 `src/main/resources/db/migration` 에 **다음 버전의 Flyway 파일을 새로 추가**한다.
  이미 적용된 마이그레이션 파일은 수정하지 않는다. 자세한 내용은 해당 디렉터리의 `README.md` 참고.

---

## 9. 이미지 업로드

이미지 저장은 `common/storage` 의 `FileStorage` 하나로만 한다. **도메인 코드에 S3 나 저장 경로 문자열이 나오면 안 된다.**
구현체는 프로필이 고른다 — 운영은 `S3FileStorage`, 로컬은 `LocalFileStorage`.

### 9.1 허용 형식과 용량

| 항목 | 값 |
|------|-----|
| 형식 | `jpg` `jpeg` `png` `webp` |
| 한 장 크기 | 5MB (`spring.servlet.multipart.max-file-size`) |
| 요청 전체 | 30MB |
| 최대 장수 | 프로필 1 · 반려동물 1 · 장소 10 · 리뷰 5 |

- SVG 는 `<script>` 를 품을 수 있어 막는다. GIF 는 쓸 일이 없는데 용량만 크므로 막는다.
- 검사는 **확장자 + Content-Type + 매직바이트** 세 가지를 모두 본다. 앞의 둘은 보내는 쪽이 위조할 수 있다.
- 정책을 바꿀 때 고치는 파일은 두 개뿐이다. 장수·경로는 `ImageCategory`, 허용 형식은 `ImageType`.

### 9.2 폼과 요청 DTO

- `<form>` 에 **`enctype="multipart/form-data"` 를 반드시 넣는다.** 빠뜨리면 파일이 아니라 파일명 문자열만 넘어온다.
- 요청 DTO 필드는 `MultipartFile` 로 받고 이름은 **한 장이면 `image`, 여러 장이면 `images`** 로 통일한다.
  HTML `<input>` 의 `name` 도 같은 이름으로 맞춘다.
- **DTO 에 저장 로직이나 URL 조립 코드를 두지 않는다.** DTO 는 파일을 들고만 있는다. (§3)

### 9.3 서비스 호출 순서

```java
@Transactional
public void createPet(Long userId, PetCreateRequest request) {
    Pet pet = Pet.builder()/* ... */.build();
    petMapper.insertPet(pet);

    MultipartFile image = request.getImage();
    if (image != null && !image.isEmpty()) {
        String imageUrl = fileStorage.uploadImage(image, ImageCategory.PET);
        petPhotoMapper.insertPhoto(PetPhoto.builder()
                .petId(pet.getId())
                .imageUrl(imageUrl)
                .sortOrder(0)
                .build());
    }
}
```

1. 도메인 행을 **먼저** INSERT 한다. 이미지 테이블이 PK 를 필요로 한다.
2. `fileStorage.uploadImage(...)` 로 URL 을 받는다. 형식·용량 검사는 이 안에서 이미 끝난다. **도메인에서 다시 검사하지 않는다.**
3. 받은 URL 을 `image_url` 컬럼에 넣는다. **전체 URL 을 저장한다.** key 만 저장하지 않는다.

여러 장은 `uploadImages(files, category)` 를 쓴다. 최대 장수 검사가 여기 들어 있으므로 도메인에서 개수를 세지 않는다.

업로드 도중 예외가 나면 DB 는 롤백되고 S3 에는 파일이 남는다. **이 고아 객체를 지우는 보정 코드를 만들지 않는다.**
S3 라이프사이클이 정리한다. 보정 로직이 오히려 멀쩡한 파일을 지우는 사고를 낸다.

### 9.4 수정 · 삭제

- **교체 순서를 지킨다.** 새 이미지 업로드 → 행 UPDATE → 예전 URL 로 `deleteImage`. 순서를 뒤집으면 실패했을 때 되돌릴 이미지가 없다.
- 행을 지울 때 `deleteImage` 도 같이 부른다. 안 부르면 버킷에 계속 쌓인다.
- `deleteImage` 는 트랜잭션 안에서 부르면 **커밋 이후**에 실제로 지운다. 도메인에서 따로 미루지 않는다.

### 9.5 로컬 개발

로컬 프로필은 `uploads/` 디렉터리에 저장하고 `/uploads/**` 로 열어 준다. **AWS 자격증명 없이 업로드 기능을 전부 개발하고 테스트할 수 있다.**
경로는 `app.storage.local.directory` 로 바꾼다. `uploads/` 는 `.gitignore` 에 들어 있다.

---

## 10. Git

### 10.1 브랜치

`feat/{이슈번호}-{기능}` — 예: `feat/6-user-signup`

- 이슈번호를 빠뜨리지 않는다.
- **`#` 를 쓰지 않는다.** 셸에서 주석 문자로 해석돼 `git push` 가 조용히 실패한다.
- `-clean`, `-v2` 같은 접미사를 붙이지 않는다.

### 10.2 커밋

`{타입}: {한글 설명}`

```
feat: 회원가입 요청 DTO 추가
fix: 예약 취소 시 권한 검사 누락 수정
test: 회원가입 서비스 테스트 추가
```

**타입:** `feat` / `fix` / `refactor` / `test` / `docs` / `style` / `chore`

- `(scope)` 를 쓰지 않는다. `feat(config):` → `feat:`
- 타입을 빠뜨리지 않는다.
- 설명은 한글로 쓴다.
- 임시 파일 / 테스트 결과물을 커밋하지 않는다.

### 10.3 PR

- **제목:** `[Feat] 회원가입 기능 구현` — 대괄호 안 타입은 첫 글자만 대문자
  (`[Feat]` `[Fix]` `[Refactor]` `[Test]` `[Docs]` `[Chore]`)
- **본문에 `Closes #{이슈번호}` 를 반드시 포함한다.** 없으면 머지해도 이슈가 닫히지 않는다.

```markdown
## 작업 내용
- 회원가입 요청 DTO 추가
- 회원가입 서비스 로직 구현

## 테스트 방법
- POST /users/signup 으로 요청 후 users 테이블에 행이 생기는지 확인

Closes #6
```

---

## 11. 커밋 전 셀프 체크리스트

**Git**
- [ ] 브랜치 이름이 `feat/{이슈번호}-{기능}` 인가? (`#` 없음)
- [ ] 커밋 메시지가 `{타입}: {한글 설명}` 인가? (`(scope)` 없음)
- [ ] PR 본문에 `Closes #{이슈번호}` 가 있는가?

**Controller**
- [ ] URL 에 `/api` 가 없고 리소스가 복수형인가?
- [ ] `@RestController` / `@RequestBody` / `@ResponseBody` 를 쓰지 않았는가?
- [ ] 뷰 이름 또는 `redirect:` 를 반환하는가? (POST 는 redirect)
- [ ] 사용자 ID 를 하드코딩하지 않고 세션에서 꺼냈는가?

**Service**
- [ ] 인터페이스 + Impl 로 나뉘어 있는가?
- [ ] DB 를 변경하는 메서드에 `@Transactional` 이 있는가?
- [ ] 조회 전용 메서드에 `@Transactional(readOnly = true)` 가 있는가?
- [ ] 예외를 `BusinessException` + 도메인 `ErrorCode` 로 던졌는가?

**Mapper**
- [ ] XML 이 `resources/mapper/` 아래에 있고, 파일명 = 인터페이스명인가?
- [ ] XML 의 `id` 가 인터페이스 메서드명과 **정확히** 같은가?
- [ ] 인터페이스에 없는 statement 가 남아 있지 않은가?
- [ ] `SELECT *` 가 없고, 컬럼명 ≠ 필드명인 곳에 `AS` 별칭이 있는가?
- [ ] INSERT / UPDATE 에 `created_at` / `updated_at` 이 없는가?

**DTO / Entity**
- [ ] DTO 가 `dto/request` 또는 `dto/response` 에 있고 이름에 `DTO` 접미사가 없는가?
- [ ] DTO ↔ Entity 변환이 `ServiceImpl` 안에 있는가?
- [ ] Entity 가 `BaseEntity` 를 상속하고 `createdAt`/`updatedAt` 을 직접 선언하지 않는가?
- [ ] Entity 에 `@Data` 를 쓰지 않았는가?

**이미지 업로드 (해당하면)**
- [ ] `<form>` 에 `enctype="multipart/form-data"` 가 있는가?
- [ ] 저장·삭제를 `FileStorage` 로만 했는가? (도메인에 S3·경로 문자열 없음)
- [ ] 행 INSERT → 업로드 → URL 저장 순서인가? 저장한 값이 전체 URL 인가?
- [ ] 행을 지우는 곳에서 `deleteImage` 도 불렀는가?

**마무리**
- [ ] 사용하지 않는 import 를 지웠는가? (IntelliJ: `Ctrl+Alt+O`)
- [ ] 주석 처리된 죽은 코드를 지웠는가?
- [ ] 코드와 같은 줄에 주석이 없는가?
- [ ] `application.yaml` 을 임의로 건드리지 않았는가?
