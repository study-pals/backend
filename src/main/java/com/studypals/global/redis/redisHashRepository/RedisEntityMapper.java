package com.studypals.global.redis.redisHashRepository;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

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
 * @since 2025-05-25
 */
public final class RedisEntityMapper {
    private static final String FIELD_PREFIX = "f:";

    private RedisEntityMapper() {}

    // entity -> hash
    public static Map<String, String> toHash(Object entity, EntityMeta m) {
        Map<String, String> map = new HashMap<>();
        try {
            for (Field f : m.valueFields()) {
                Object val = m.getters().get(f).invoke(entity);
                if (val != null) {
                    map.put(FIELD_PREFIX + f.getName(), String.valueOf(val));
                }
            }

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

    // hash -> entity
    @SuppressWarnings("unchecked")
    public static <T> T fromHash(String idKey, Map<Object, Object> hash, EntityMeta m) {
        try {
            T obj = (T) m.type().getDeclaredConstructor().newInstance();
            m.idSetter().invoke(obj, convert(idKey, m.idField().getType()));

            for (Field f : m.valueFields()) {
                String redisKey = FIELD_PREFIX + f.getName();
                Object ov = hash.get(redisKey);
                if (ov != null) {
                    m.setters().get(f).invoke(obj, convert(ov, f.getType()));
                }

                if (m.mapField() != null) {
                    Field mf = m.mapField();
                    Map<String, String> mp = new HashMap<>();
                    hash.forEach((k, v) -> {
                        String key = k.toString();
                        if (!key.startsWith(FIELD_PREFIX) // 일반 필드 아님
                                && !key.equals(m.idField().getName())) { // ID 아님
                            mp.put(key, v.toString());
                        }
                    });
                    m.setters().get(mf).invoke(obj, mp);
                }
            }
            return obj;
        } catch (Throwable t) {
            throw new RuntimeException("fromHash mapping failed", t);
        }
    }

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
