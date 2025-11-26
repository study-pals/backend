package com.studypals.global.redis.redisHashRepository;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * Redis Hash 자료구조와 엔티티 간의 변환을 담당하는 유틸리티 클래스입니다.
 *
 * <p>{@link EntityMeta}를 기반으로 객체 필드를 Redis Hash 구조로 직렬화하거나,
 * Redis에서 읽어온 Hash 데이터를 다시 객체로 역직렬화하는 역할을 합니다.
 *
 * <p>일반 필드는 `"f:"` prefix를 붙여 구분되며,
 * {@code @RedisHashMapField}로 지정된 Map 필드는 키 그대로 저장됩니다.
 *
 * <p><b>설계 철학:</b><br>
 * - ID 필드는 Redis Hash 내부에 저장되지 않고 Redis Key 자체로 사용됩니다.<br>
 * - MethodHandle 기반 Getter/Setter를 통해 성능 손실 없이 필드 접근을 수행합니다.
 *
 * @author jack8
 * @since 2025-05-25
 */
public final class RedisEntityMapper {

    /** 일반 필드와 Map 필드의 키를 구분하기 위한 접두사 */
    private static final String FIELD_PREFIX = "f:";

    /** 정적 클래스이므로 인스턴스화 방지 */
    private RedisEntityMapper() {}

    /**
     * 엔티티 객체를 Redis Hash에 저장 가능한 Map 형태로 변환합니다.
     *
     * @param entity 변환 대상 객체
     * @param m      해당 객체의 메타 정보
     * @return Redis에 저장 가능한 key-value 형태의 Map
     */
    public static Map<String, String> toHash(Object entity, EntityMeta m) {
        Map<String, String> map = new HashMap<>();
        try {

            // 일반 필드 직렬화
            for (Field f : m.valueFields()) {
                Object val = m.getters().get(f).invoke(entity);
                if (val != null) {
                    map.put(FIELD_PREFIX + f.getName(), String.valueOf(val));
                }
            }
            // map field 직렬화
            if (m.mapField() != null) {
                Field mf = m.mapField();
                Object raw = m.getters().get(mf).invoke(entity);
                if (raw != null) {
                    Map<?, ?> src = (Map<?, ?>) raw;
                    src.forEach((k, v) -> {
                        if (k != null && v != null) {
                            map.put(String.valueOf(k), String.valueOf(v));
                        }
                    });
                }
            }
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }

        return map;
    }

    /**
     * Redis Hash 데이터(Map)를 엔티티 객체로 역직렬화합니다.
     *
     * @param idKey Redis Key (ID 값)
     * @param hash  Redis에서 조회한 해시 데이터
     * @param m     엔티티의 메타 정보
     * @param <T>   반환할 객체 타입
     * @return 역직렬화된 객체 인스턴스
     */
    @SuppressWarnings("unchecked")
    public static <T> T fromHash(String idKey, Map<Object, Object> hash, EntityMeta m) {
        try {
            T obj = (T) m.type().getDeclaredConstructor().newInstance();
            if (!idKey.startsWith(m.keyPrefix())) {
                throw new IllegalArgumentException("not invalid entity type");
            }
            idKey = idKey.substring(m.keyPrefix().length());
            // ID 주입
            m.idSetter().invoke(obj, convert(idKey, m.idField().getType()));

            // Map 필드 역직렬화
            if (m.mapField() != null) {

                Field mf = m.mapField();
                Map<Object, Object> mp = new HashMap<>();
                hash.forEach((k, v) -> {
                    String key = k.toString();
                    if (!key.startsWith(FIELD_PREFIX) // 일반 필드 아님
                            && !key.equals(m.idField().getName())) { // ID 아님
                        mp.put(convert(key, m.mapKeyType()), convert(v, m.mapValueType()));
                    }
                });
                m.setters().get(mf).invoke(obj, mp);
            }

            // 일반 필드 역직렬화
            for (Field f : m.valueFields()) {

                String redisKey = FIELD_PREFIX + f.getName();
                Object ov = hash.get(redisKey);
                if (ov != null) {
                    m.setters().get(f).invoke(obj, convert(ov, f.getType()));
                }
            }
            return obj;
        } catch (Throwable t) {
            throw new RuntimeException("fromHash mapping failed", t);
        }
    }

    /**
     * Redis에서 가져온 값(String)을 대상 타입으로 변환합니다.
     *
     * @param o      원본 값
     * @param target 변환할 타입
     * @return 변환된 값
     */
    private static Object convert(Object o, Class<?> target) {
        if (o == null) return null;
        String s = o.toString();
        if (target == String.class) return s;
        if (target == int.class || target == Integer.class) return Integer.parseInt(s);
        if (target == long.class || target == Long.class) return Long.parseLong(s);
        if (target == double.class || target == Double.class) return Double.parseDouble(s);
        if (target == char.class || target == Character.class) {
            if (s.isEmpty()) throw new IllegalArgumentException("Empty char field");
            return s.charAt(0);
        }
        throw new IllegalArgumentException("Unsupported conversion target: " + target);
    }
}
