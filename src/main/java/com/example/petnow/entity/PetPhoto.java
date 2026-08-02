package com.example.petnow.entity;

import lombok.*;

import java.time.LocalDateTime;

/**
 * pet_photos 테이블 매핑. 필드명은 ERD 컬럼명 기준.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PetPhoto {

    private Long petPhotoId;

    private Long petId;

    private String fileUrl;

    /** 노출 순서. 0이 대표 사진 */
    private Integer sortOrder;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

}
