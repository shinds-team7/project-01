package com.example.petnow.entity;

import com.example.petnow.common.domain.BaseEntity;
import lombok.*;

/**
 * reviews 테이블 매핑. 필드명은 ERD 컬럼명 기준.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Review extends BaseEntity {

    private Long reviewId;

    private Long reservationId;     // FK

    /** 1 ~ 5 */
    private Integer rating;

    private String content;

    private Boolean isReadByHost;
}
