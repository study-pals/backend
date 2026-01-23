package com.studypals.domain.groupManage.dao;

import java.util.Optional;

import com.studypals.domain.groupManage.entity.GroupEntryCode;

/**
 * GroupEntryCode 도메인에 대한 Redis Custom Repository 계약을 정의합니다.
 * <p>
 * 이 인터페이스는 기본 Redis Repository가 제공하지 않는
 * 조회 및 저장 행위를 명시적으로 분리하여 정의합니다.
 *
 * <p>
 * 특히 groupId 기반 조회를 지원하기 위해
 * 보조 인덱스(String KV)를 활용하는 저장 및 조회 계약을 제공합니다.
 *
 * <p><b>외부 모듈:</b><br>
 * Spring Data Redis
 *
 * @since 2026-01-13
 */
public interface GroupEntryCodeRedisRepositoryCustom {

    /**
     * GroupEntryCode 엔티티에 대한 역방향 인덱스를 저장하거나 갱신합니다.
     * <p>
     * 이 메서드는 {@code groupId → code} 조회를 가능하게 하기 위한
     * 보조 인덱스 저장 계약을 정의합니다.
     *
     * <p>
     * 엔티티 본문 저장 여부 및 방식은
     * 구현체에 위임됩니다.
     *
     * @param entryCode 저장 또는 갱신 대상 GroupEntryCode 엔티티
     */
    void saveIdx(GroupEntryCode entryCode);

    /**
     * groupId를 기준으로 GroupEntryCode 엔티티를 조회합니다.
     * <p>
     * 조회 방식 및 내부 Redis 구조는
     * 구현체에서 결정하며,
     * 결과가 존재하지 않는 경우 빈 Optional을 반환합니다.
     *
     * @param groupId 조회 대상 그룹 ID
     * @return 조회된 GroupEntryCode, 존재하지 않으면 Optional.empty()
     */
    Optional<GroupEntryCode> findByGroupId(Long groupId);
}
