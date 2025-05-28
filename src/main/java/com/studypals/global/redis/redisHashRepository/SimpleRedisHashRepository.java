package com.studypals.global.redis.redisHashRepository;

import java.time.Duration;
import java.util.*;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

/**
 * {@link RedisHashRepository}의 기본 구현체로, Redis Hash 자료구조 기반의 CRUD 기능을 제공합니다.
 *
 * <p>Spring Data의 Repository 프록시가 위임하는 실제 동작을 수행하며,
 * 엔티티 메타 정보({@link EntityMeta})를 기반으로 Redis에 데이터를 저장/조회/삭제합니다.
 *
 * <p>RedisTemplate의 String 기반 연산을 통해 키-필드-값을 구성하며,
 * Lua 스크립트를 활용하여 멀티 필드 조회도 지원합니다.
 *
 * @param <E> 엔티티 타입
 * @param <ID> Redis 해시 키 타입
 *
 * @author jack8
 * @since 2025-05-25
 */
public class SimpleRedisHashRepository<E, ID> implements RedisHashRepository<E, ID> {

    /** Redis 접근에 사용되는 RedisTemplate */
    private final RedisTemplate<String, String> tpl;
    /** 엔티티 메타 정보 (리플렉션 기반 필드 정의 및 TTL 등 포함) */
    private final EntityMeta meta;

    /**
     * 생성자
     *
     * @param tpl RedisTemplate 인스턴스
     * @param meta 대상 엔티티의 메타데이터
     */
    public SimpleRedisHashRepository(RedisTemplate<String, String> tpl, EntityMeta meta) {
        this.tpl = tpl;
        this.meta = meta;
    }

    /**
     * Lua 기반 멀티 필드 조회 스크립트.
     * 주어진 필드 목록 중 존재하는 필드만 field-value 쌍으로 반환합니다.
     */
    private static final DefaultRedisScript<List> HGET_MULTI_SCRIPT = new DefaultRedisScript<>(
            """
                            local result = {}
                            for i = 1, #ARGV do
                                local field = ARGV[i]
                                local val   = redis.call("HGET", KEYS[1], field)
                                if val then
                                    result[#result + 1] = field
                                    result[#result + 1] = val
                                end
                            end
                            return result
                    """,
            List.class);

    /**
     * Redis에 엔티티를 저장합니다.
     * ID가 이미 존재하면 {@link org.springframework.dao.DuplicateKeyException} 발생.
     */
    @Override
    public void save(E entity) {
        try {
            String key = meta.keyPrefix() + meta.idGetter().invoke(entity).toString();
            Map<String, String> map = RedisEntityMapper.toHash(entity, meta);
            tpl.opsForHash().putAll(key, map);
            if (meta.ttlValue() > 0)
                tpl.expire(key, Duration.of(meta.ttlValue(), meta.ttlUnit().toChronoUnit()));
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    /**
     * Redis에서 주어진 키에 해당하는 엔티티를 조회합니다.
     */
    @Override
    public Optional<E> findById(ID id) {
        String keyPrefix = meta.keyPrefix();
        String key = keyPrefix + id.toString();
        Map<Object, Object> raw = tpl.opsForHash().entries(key);
        if (raw == null || raw.isEmpty()) return Optional.empty();

        @SuppressWarnings("unchecked")
        E entity = (E) RedisEntityMapper.fromHash(id.toString(), raw, meta);
        return Optional.of(entity);
    }

    /**
     * Redis에서 해당 키의 Hash를 삭제합니다.
     */
    @Override
    public void delete(ID id) {
        String key = meta.keyPrefix() + id.toString();
        tpl.delete(key);
    }

    /**
     * Redis에 해당 키가 존재하는지 확인합니다.
     */
    @Override
    public boolean existById(ID id) {
        return Boolean.TRUE.equals(tpl.hasKey(id.toString()));
    }

    /**
     * 여러 키에 대해 한 번에 조회합니다.
     * 존재하는 엔티티만 반환합니다.
     */
    @Override
    public Iterable<E> findAllById(Iterable<ID> ids) {
        List<E> list = new ArrayList<>();
        for (ID id : ids) findById(id).ifPresent(list::add);
        return list;
    }

    /**
     * Lua 스크립트를 사용하여 지정된 필드 키만 조회합니다.
     * 존재하는 필드만 field-value 쌍으로 반환됩니다.
     */
    @Override
    public Map<String, String> findHashFieldsById(ID hashKey, List<String> fieldKey) {
        String keyPrefix = meta.keyPrefix();
        @SuppressWarnings("unchecked")
        List<Object> flat = (List<Object>)
                tpl.execute(HGET_MULTI_SCRIPT, List.of(keyPrefix + hashKey.toString()), fieldKey.toArray());
        Map<String, String> map = new LinkedHashMap<>();
        if (flat != null) {
            for (int i = 0; i < flat.size(); i += 2) {
                map.put(flat.get(i).toString(), flat.get(i + 1).toString());
            }
        }
        return map;
    }

    /**
     * Redis 해시 내에 필드 키-값을 추가하거나 덮어씁니다.
     * TTL이 설정되어 있으면 연장합니다.
     */
    @Override
    public void saveMapById(ID hashKey, Map<String, String> map) {
        String keyPrefix = meta.keyPrefix();
        tpl.opsForHash().putAll(keyPrefix + hashKey.toString(), map);
        if (meta.ttlValue() > 0) {
            tpl.expire(
                    hashKey.toString(),
                    Duration.of(meta.ttlValue(), meta.ttlUnit().toChronoUnit()));
        }
    }

    /**
     * Redis 해시 내에서 지정된 필드 키들을 삭제합니다.
     */
    @Override
    public void deleteMapById(ID hashKey, List<String> fieldKey) {
        String keyPrefix = meta.keyPrefix();
        tpl.opsForHash().delete(keyPrefix + hashKey.toString(), fieldKey.toArray());
    }
}
