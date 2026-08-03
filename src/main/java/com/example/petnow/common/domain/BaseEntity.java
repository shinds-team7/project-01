package com.example.petnow.common.domain;

import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 생성/수정 일시를 담는 공통 부모.
 *
 * 값을 채우는 주체는 DB.
 * 모든 테이블의 감사 컬럼이
 * {@code created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP},
 * {@code updated_at ... ON UPDATE CURRENT_TIMESTAMP} 로 선언되어 있어서
 * INSERT/UPDATE 문에 이 컬럼을 넣지 않아도 DB 가 알아서 기록한다.
 *
**/

@Getter
public abstract class BaseEntity {

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
