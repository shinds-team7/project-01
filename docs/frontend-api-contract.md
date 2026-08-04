# Pet NOW 프론트엔드 장소 API 계약

프론트엔드는 페이지가 열릴 때 `GET /api/places?size=12`를 호출한다. 이 요청이 JSON 목록을 반환하면 장소 검색 영역의 개발용 플레이스홀더를 실제 데이터 카드로 자동 교체한다.

## 지원하는 응답 외형

다음 네 가지 중 하나를 사용할 수 있다.

```json
[{ "id": 1, "name": "포근한 우리집" }]
```

```json
{ "items": [{ "id": 1, "name": "포근한 우리집" }] }
```

```json
{ "content": [{ "id": 1, "name": "포근한 우리집" }] }
```

```json
{ "data": [{ "id": 1, "name": "포근한 우리집" }] }
```

## 장소 필드

| 필드 | 필수 | 설명 |
|---|---:|---|
| `id` | 예 | 장소 ID |
| `name` 또는 `title` | 예 | 장소 이름 |
| `description` | 아니오 | 장소 설명 |
| `placeType` 또는 `type` | 아니오 | `HOUSE`, `APARTMENT`, `OFFICETEL` |
| `sigungu` 또는 `region` | 아니오 | 지역 표시값 |
| `distanceKm` 또는 `distance` | 아니오 | 거리 |
| `averageRating` 또는 `rating` | 아니오 | 평균 평점 |
| `reviewCount` | 아니오 | 리뷰 수 |
| `hourlyPrice` 또는 `price` | 아니오 | 시간당 가격 |
| `tags` | 아니오 | 문자열 배열 |
| `imageUrl` 또는 `thumbnailUrl` | 아니오 | 대표 이미지 URL |
| `availableNow` | 아니오 | 현재 예약 가능 여부 |

검색 요청은 같은 엔드포인트에 `keyword`, `regions`, `startDate`, `endDate`, `startTime`, `endTime`, `pets`, `placeType`, `sort`, `size` 쿼리 파라미터를 사용한다.

지도는 실제 위·경도 데이터와 지도 SDK, 찜은 인증된 북마크 API가 별도로 필요하므로 장소 목록 API만 추가됐다고 자동 활성화되지 않는다.
