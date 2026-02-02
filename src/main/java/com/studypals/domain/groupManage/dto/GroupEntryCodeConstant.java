package com.studypals.domain.groupManage.dto;

/**
 * GroupEntryCode 도메인에서 사용하는 Redis 키 관련 상수를 정의합니다.
 * <p>
 * GroupEntryCode 엔티티 및 그에 대한 보조 인덱스를
 * Redis에 저장할 때 사용하는 키 네이밍 규칙을
 * 하나의 상수 클래스로 모아 관리합니다.
 *
 * <p>
 * 이 클래스는 Redis 키 충돌 방지와
 * 네이밍 규칙의 일관성을 유지하기 위한 목적을 가집니다.
 *
 * <p><b>주요 생성자:</b><br>
 * {@code GroupEntryCodeConstant()} <br>
 * 인스턴스 생성을 방지하기 위한 private 생성자입니다.
 *
 * @author jack8
 * @since 2026-01-13
 */
public class GroupEntryCodeConstant {

    /**
     * GroupEntryCode 도메인을 식별하기 위한 기본 식별자입니다.
     */
    public static final String IDENTIFIER = "entryCode";

    /**
     * GroupEntryCode 엔티티 본문 저장 시 사용하는 Redis 키 prefix입니다.
     */
    public static final String PREFIX = IDENTIFIER + ":";

    /**
     * 인스턴스 생성을 방지하기 위한 private 생성자입니다.
     */
    public static final String REVERSE_IDX_PREFIX = IDENTIFIER + ":groupId:";

    private GroupEntryCodeConstant() {}
}
