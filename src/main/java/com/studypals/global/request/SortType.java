package com.studypals.global.request;

import org.springframework.data.domain.Sort;

/**
 * 정렬 방식 관련 메서드를 포함하는 인터페이스입니다.
 * 공통 혹은 각 도메인 엔티티 별 정렬 조건을 enum 으로 정의해
 * 해당 인터페이스를 상속, 구현해야 합니다.
 *
 * @author s0o0bn
 * @since 2025-06-05
 */
public interface SortType {
    /**
     * 정렬 조건에 해당하는 enum 타입명을 반환합니다.
     * enum class 에서 상속 시, 자동으로 구현됩니다.
     *
     * @return enum 타입명
     */
    String name();

    /**
     * 정렬 시 기준이 될 칼럼명을 반환합니다.
     * DB 테이블 기준이 아닌 엔티티 클래스의 필드명이어야합니다.
     *
     * @return 해당 정렬 조건의 엔티티 필드명
     */
    String getField();

    /**
     * 오름차순, 내림차순에 해당하는 정렬 방향을 반환합니다.
     *
     * @return {@link org.springframework.data.domain.Sort.Direction}
     */
    Sort.Direction getDirection();
}
