package com.studypals.global.redis.redisHashRepository;

import java.time.Duration;
import java.util.*;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

/**
 * 코드에 대한 전체적인 역할을 적습니다.
 * <p>
 * 코드에 대한 작동 원리 등을 적습니다.
 *
 * <p><b>상속 정보:</b><br>
 * 상속 정보를 적습니다.
 *
 * <p><b>주요 생성자:</b><br>
 * {@code ExampleClass(String example)}  <br>
 * 주요 생성자와 그 매개변수에 대한 설명을 적습니다. <br>
 *
 * <p><b>빈 관리:</b><br>
 * 필요 시 빈 관리에 대한 내용을 적습니다.
 *
 * <p><b>외부 모듈:</b><br>
 * 필요 시 외부 모듈에 대한 내용을 적습니다.
 *
 * @author jack8
 * @see
 * @since 2025-05-27
 */
public class SimpleRedisHashRepository<E, ID> implements RedisHashRepository<E, ID> {

    private final RedisTemplate<String, String> tpl;
    private final EntityMeta meta;

    public SimpleRedisHashRepository(RedisTemplate<String, String> tpl, EntityMeta meta) {
        this.tpl = tpl;
        this.meta = meta;
    }

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

    @Override
    public void save(E entity) {
        try {
            String key = meta.idGetter().invoke(entity).toString();
            if (Boolean.TRUE.equals(tpl.hasKey(key))) {
                throw new DuplicateKeyException("key duplicated");
            }
            Map<String, String> map = RedisEntityMapper.toHash(entity, meta);
            tpl.opsForHash().putAll(key, map);
            if (meta.ttlValue() > 0)
                tpl.expire(key, Duration.of(meta.ttlValue(), meta.ttlUnit().toChronoUnit()));
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    @Override
    public Optional<E> findById(ID id) {
        Map<Object, Object> raw = tpl.opsForHash().entries(id.toString());
        if (raw == null || raw.isEmpty()) return Optional.empty();

        @SuppressWarnings("unchecked")
        E entity = (E) RedisEntityMapper.fromHash(id.toString(), raw, meta);
        return Optional.of(entity);
    }

    @Override
    public void delete(ID id) {
        tpl.delete(id.toString());
    }

    @Override
    public boolean existById(ID id) {
        return Boolean.TRUE.equals(tpl.hasKey(id.toString()));
    }

    @Override
    public Iterable<E> findAllById(Iterable<ID> ids) {
        List<E> list = new ArrayList<>();
        for (ID id : ids) findById(id).ifPresent(list::add);
        return list;
    }

    @Override
    public Map<String, String> findHashFieldsById(ID hashKey, List<String> fieldKey) {
        @SuppressWarnings("unchecked")
        List<Object> flat =
                (List<Object>) tpl.execute(HGET_MULTI_SCRIPT, List.of(hashKey.toString()), fieldKey.toArray());
        Map<String, String> map = new LinkedHashMap<>();
        if (flat != null) {
            for (int i = 0; i < flat.size(); i += 2) {
                map.put(flat.get(i).toString(), flat.get(i + 1).toString());
            }
        }
        return map;
    }

    @Override
    public void saveMapById(ID hashKey, Map<String, String> map) {
        tpl.opsForHash().putAll(hashKey.toString(), map);
        if (meta.ttlValue() > 0) {
            tpl.expire(
                    hashKey.toString(),
                    Duration.of(meta.ttlValue(), meta.ttlUnit().toChronoUnit()));
        }
    }

    @Override
    public void deleteMapById(ID hashKey, List<String> fieldKey) {
        tpl.opsForHash().delete(hashKey.toString(), fieldKey.toArray());
    }
}
