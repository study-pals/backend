package com.studypals.domain.groupManage.dao;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisKeyValueTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import lombok.RequiredArgsConstructor;

import com.studypals.domain.groupManage.dto.GroupEntryCodeConstant;
import com.studypals.domain.groupManage.entity.GroupEntryCode;

/**
 * GroupEntryCode 도메인의 Redis 저장 및 조회를 담당하는 Custom Repository 구현체입니다.
 * <p>
 * 하나의 GroupEntryCode 엔티티를 단일 Redis 구조로 관리하지 않고,
 * 목적에 따라 두 가지 Redis 자료구조로 분리하여 관리합니다.
 *
 * <p>
 * 1) 엔티티 본문은 Redis Hash 구조로 저장되며,
 * {@code @TimeToLive} 기반 TTL 처리는 {@link RedisKeyValueTemplate}에 위임됩니다.
 * <br>
 * 2) groupId → code 매핑을 위한 역방향 인덱스는
 * String KV 구조로 별도 관리되어, 조회 경로 단순화 및 성능을 보장합니다.
 *
 * <p>
 * 이 클래스는 단순 CRUD 저장소가 아니라,
 * 엔티티 상태와 보조 인덱스 상태를 함께 동기화하는 책임을 가집니다.
 *
 * <p><b>상속 정보:</b><br>
 * {@link GroupEntryCodeRedisRepositoryCustom} 인터페이스를 구현합니다.
 *
 * <p><b>빈 관리:</b><br>
 * Spring Data Redis Repository 확장 구조에 의해
 * Custom Repository 구현체로 관리됩니다.
 *
 * <p><b>외부 모듈:</b><br>
 * Spring Data Redis
 *
 * @author jack8
 * @since 2026-01-13
 */
@RequiredArgsConstructor
public class GroupEntryCodeRedisRepositoryCustomImpl implements GroupEntryCodeRedisRepositoryCustom {

    private final RedisKeyValueTemplate redisKeyValueTemplate;
    private final StringRedisTemplate stringRedisTemplate;

    /**
     * GroupEntryCode 엔티티에 대한 역방향 인덱스를 저장하거나 갱신합니다.
     * <p>
     * 이 메서드는 단순 저장 로직이 아니라,
     * {@code groupId → code} 조회를 위한 보조 인덱스를
     * 엔티티 상태와 함께 동기화하는 진입점 역할을 합니다.
     *
     * <p>
     * entryCode, code, groupId는
     * 이후 Redis 키 및 값으로 직접 사용되므로
     * 이 메서드 진입 시점에서 null을 허용하지 않습니다.
     *
     * @param entryCode 저장 또는 갱신 대상 GroupEntryCode 엔티티
     * @throws NullPointerException entryCode, code, groupId 중 하나라도 null인 경우
     */
    @Override
    public void saveIdx(GroupEntryCode entryCode) {
        Objects.requireNonNull(entryCode, "entryCode");
        Objects.requireNonNull(entryCode.getCode(), "entryCode.code");
        Objects.requireNonNull(entryCode.getGroupId(), "entryCode.groupId");

        saveReverseIndex(entryCode);
    }

    /**
     * groupId를 기준으로 GroupEntryCode 엔티티를 조회합니다.
     * <p>
     * 먼저 String KV 기반의 역방향 인덱스에서
     * {@code groupId → code} 매핑을 조회한 뒤,
     * 해당 code를 기준으로 Redis Hash 엔티티를 조회합니다.
     *
     * <p>
     * 역방향 인덱스가 존재하지 않거나,
     * TTL 만료 등으로 code를 조회할 수 없는 경우
     * 빈 Optional을 반환합니다.
     *
     * @param groupId 조회 대상 그룹 ID
     * @return 조회된 GroupEntryCode, 존재하지 않으면 Optional.empty()
     */
    @Override
    public Optional<GroupEntryCode> findByGroupId(Long groupId) {
        if (groupId == null) {
            return Optional.empty();
        }

        String indexKey = indexKey(groupId);
        ValueOperations<String, String> ops = stringRedisTemplate.opsForValue();
        String code = ops.get(indexKey);
        if (code == null || code.isBlank()) {
            return Optional.empty();
        }

        return redisKeyValueTemplate.findById(code, GroupEntryCode.class);
    }

    /**
     * GroupEntryCode 엔티티에 대한 역방향 인덱스를 Redis에 저장합니다.
     * <p>
     * {@code groupId → code} 형태의 String KV 구조로 저장되며,
     * 엔티티의 TTL 값에 따라 인덱스의 수명도 함께 설정됩니다.
     *
     * <p>
     * TTL이 null이거나 음수인 경우,
     * 역방향 인덱스는 만료되지 않는 키로 저장됩니다.
     *
     * <p>
     * 이 메서드는 엔티티 본문 저장을 수행하지 않으며,
     * 오직 보조 인덱스 관리만을 책임집니다.
     *
     * @param entryCode 역방향 인덱스를 생성할 GroupEntryCode 엔티티
     */
    private void saveReverseIndex(GroupEntryCode entryCode) {
        String indexKey = indexKey(entryCode.getGroupId());
        String code = entryCode.getCode();
        ValueOperations<String, String> ops = stringRedisTemplate.opsForValue();

        Long ttl = entryCode.getTtl();
        if (ttl == null || ttl < 0) {
            ops.set(indexKey, code);
            return;
        }

        ops.set(indexKey, code, ttl, TimeUnit.SECONDS);
    }

    /**
     * groupId를 기반으로 역방향 인덱스용 Redis 키를 생성합니다.
     * <p>
     * 키 충돌 방지를 위해
     * 사전에 정의된 prefix를 항상 포함합니다.
     *
     * @param groupId 그룹 ID
     * @return 역방향 인덱스 Redis 키
     */
    private String indexKey(Long groupId) {
        return GroupEntryCodeConstant.REVERSE_IDX_PREFIX + groupId;
    }
}
