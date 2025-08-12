package com.studypals.global.redis.redisHashRepository;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;

import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.ReturnType;
import org.springframework.data.redis.core.*;

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

    @Override
    @SuppressWarnings("unchecked")
    public void saveAll(Collection<E> entities) {
        tpl.executePipelined(new SessionCallback<Void>() {
            @Override
            public Void execute(RedisOperations operations) {
                HashOperations<String, String, String> hashOps = operations.opsForHash();

                for (E entity : entities) {
                    try {
                        String key = meta.keyPrefix() + meta.idGetter().invoke(entity);

                        Map<String, String> map = RedisEntityMapper.toHash(entity, meta);

                        hashOps.putAll(key, map);

                        if (meta.ttlValue() > 0) {
                            operations.expire(
                                    key,
                                    Duration.of(meta.ttlValue(), meta.ttlUnit().toChronoUnit()));
                        }
                    } catch (Throwable t) {
                        throw new RuntimeException(t);
                    }
                }
                return null; // SessionCallback의 반환값
            }
        });
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
        E entity = (E) RedisEntityMapper.fromHash(key, raw, meta);
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

    @Override
    @SuppressWarnings("unchecked")
    public void deleteAll(Collection<ID> ids) {
        String keyPrefix = meta.keyPrefix();
        tpl.executePipelined(new SessionCallback<Void>() {
            @Override
            public Void execute(RedisOperations operations) throws DataAccessException {
                for (ID id : ids) {
                    operations.delete(keyPrefix + id);
                }
                return null;
            }
        });
    }

    /**
     * Redis에 해당 키가 존재하는지 확인합니다.
     */
    @Override
    public boolean existById(ID id) {
        String keyPrefix = meta.keyPrefix();
        return Boolean.TRUE.equals(tpl.hasKey(keyPrefix + id.toString()));
    }

    /**
     * 여러 키에 대해 한 번에 조회합니다.
     * 존재하는 엔티티만 반환합니다.
     */
    @Override
    @SuppressWarnings("unchecked")
    public Iterable<E> findAllById(Iterable<ID> ids) {
        List<ID> idList = new ArrayList<>();
        ids.forEach(idList::add);

        // 2) SessionCallback + Pipeline 실행
        List<Object> rawResults = tpl.executePipelined(new SessionCallback<Object>() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {

                HashOperations<String, String, String> hashOps = operations.opsForHash();

                String prefix = meta.keyPrefix();
                for (ID id : idList) {
                    hashOps.entries(prefix + id.toString());
                }
                return null;
            }
        });

        // 3) 파이프라인 결과를 엔티티 리스트로 변환
        List<E> entities = new ArrayList<>(rawResults.size());
        for (int i = 0; i < rawResults.size(); i++) {
            Map<Object, Object> map = (Map<Object, Object>) rawResults.get(i);
            if (map == null || map.isEmpty()) continue;

            String fullKey = meta.keyPrefix() + idList.get(i).toString();
            entities.add(RedisEntityMapper.fromHash(fullKey, map, meta));
        }
        return entities;
    }

    /**
     * Lua 스크립트를 사용하여 지정된 필드 키만 조회합니다.
     * 존재하는 필드만 field-value 쌍으로 반환됩니다.
     */
    @Override
    public Map<String, String> findHashFieldsById(ID hashKey, List<String> fieldKey) {
        return findHashFieldsById(Map.of(hashKey, fieldKey)).get(hashKey);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<ID, Map<String, String>> findHashFieldsById(Map<ID, List<String>> fieldKey) {
        List<ID> idList = new ArrayList<>(fieldKey.keySet());
        String keyPrefix = meta.keyPrefix();

        List<Object> result = tpl.executePipelined(new SessionCallback<Object>() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                HashOperations<String, String, String> hashOps = operations.opsForHash();

                for (ID id : idList) {
                    String key = keyPrefix + id.toString();
                    List<String> fields = fieldKey.get(id);
                    hashOps.multiGet(key, fields);
                }
                return null;
            }
        });

        Map<ID, Map<String, String>> resultMap = new LinkedHashMap<>();

        for (int i = 0; i < idList.size(); i++) {
            ID id = idList.get(i);
            List<String> fields = fieldKey.get(id);
            List<String> values = (List<String>) result.get(i);

            Map<String, String> fieldMap = new LinkedHashMap<>();
            for (int j = 0; j < fields.size(); j++) {
                String value = values.get(j);
                if (value != null) {
                    fieldMap.put(fields.get(j), values.get(j));
                }
            }
            resultMap.put(id, fieldMap);
        }

        return resultMap;
    }

    /**
     * Redis 해시 내에 필드 키-값을 추가하거나 덮어씁니다.
     * TTL이 설정되어 있으면 연장합니다.
     */
    @SuppressWarnings("unchecked")
    @Override
    public void saveMapById(Map<ID, Map<String, String>> data) {
        String keyPrefix = meta.keyPrefix();
        tpl.executePipelined(new SessionCallback<Void>() {
            @Override
            public Void execute(RedisOperations operations) throws DataAccessException {
                HashOperations<String, String, String> hashOps = operations.opsForHash();

                for (Map.Entry<ID, Map<String, String>> value : data.entrySet()) {
                    String key = keyPrefix + value.getKey();
                    hashOps.putAll(key, value.getValue());
                    if (meta.ttlValue() > 0) {
                        operations.expire(
                                key, Duration.of(meta.ttlValue(), meta.ttlUnit().toChronoUnit()));
                    }
                }
                return null;
            }
        });
    }

    @Override
    public void saveMapById(ID id, Map<String, String> data) {
        saveMapById(Map.of(id, data));
    }

    /**
     * Redis 해시 내에서 지정된 필드 키들을 삭제합니다.
     */
    @Override
    @SuppressWarnings("unchecked")
    public void deleteMapById(Map<ID, Set<String>> fieldKey) {
        String keyPrefix = meta.keyPrefix();
        tpl.executePipelined(new SessionCallback<Void>() {
            @Override
            public Void execute(RedisOperations operations) throws DataAccessException {
                HashOperations<String, String, String> hashOps = operations.opsForHash();

                for (Map.Entry<ID, Set<String>> id : fieldKey.entrySet()) {
                    String key = keyPrefix + id.getKey();
                    hashOps.delete(key, id.getValue().toArray());
                }
                return null;
            }
        });
    }

    @Override
    public void deleteMapById(ID hashKey, Set<String> fieldKey) {
        deleteMapById(Map.of(hashKey, fieldKey));
    }

    @Override
    public void deleteMapById(ID hashKey, String fieldKey) {
        deleteMapById(Map.of(hashKey, Set.of(fieldKey)));
    }

    @Override
    public String tryLock(ID id, Duration ttl) {
        if (ttl == null || ttl.isZero() || ttl.isNegative()) {
            throw new IllegalArgumentException("ttl value error");
        }
        String key = lockKeyOf(id);
        String token = UUID.randomUUID().toString();
        Boolean ok = tpl.opsForValue().setIfAbsent(key, token, ttl);
        return Boolean.TRUE.equals(ok) ? token : null;
    }

    @Override
    public boolean unlock(ID id, String token) {
        if (token == null || token.isBlank()) return false;
        String key = lockKeyOf(id);

        Long res = tpl.execute((RedisCallback<Long>) conn -> conn.scriptingCommands()
                .eval(
                        UNLOCK_LUA,
                        ReturnType.INTEGER,
                        1,
                        key.getBytes(StandardCharsets.UTF_8),
                        token.getBytes(StandardCharsets.UTF_8)));
        return res != null && res > 0;
    }

    @Override
    public boolean refreshLock(ID id, String token, Duration ttl) {
        if (token == null || token.isBlank()) return false;
        if (ttl == null || ttl.isZero() || ttl.isNegative()) {
            throw new IllegalArgumentException("ttl must be > 0");
        }
        String key = lockKeyOf(id);
        Long res = tpl.execute((RedisCallback<Long>) conn -> conn.scriptingCommands()
                .eval(
                        REFRESH_LUA,
                        ReturnType.INTEGER,
                        1,
                        key.getBytes(StandardCharsets.UTF_8),
                        token.getBytes(StandardCharsets.UTF_8),
                        String.valueOf(ttl.toMillis()).getBytes(StandardCharsets.UTF_8)));
        return res != null && res > 0;
    }

    private String lockKeyOf(ID id) {
        return meta.lockPrefix() + id.toString();
    }

    private static final byte[] UNLOCK_LUA =
            """
            if redis.call('get', KEYS[1]) == ARGV[1] then
                return call('del', KEYS[1])
            else return 0 end
            """
                    .getBytes(StandardCharsets.UTF_8);

    private static final byte[] REFRESH_LUA = ("if redis.call('get', KEYS[1]) == ARGV[1] then "
                    + "  return redis.call('pexpire', KEYS[1], ARGV[2]) "
                    + "else return 0 end")
            .getBytes(StandardCharsets.UTF_8);

    /** 현재 토큰 보유자가 TTL을 연장(밀리초 단위). 성공 시 true */
}
