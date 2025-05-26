package com.studypals.global.redis.redisHashRepository;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import com.studypals.global.redis.redisHashRepository.annotations.Expires;
import com.studypals.global.redis.redisHashRepository.annotations.RedisHashEntity;
import com.studypals.global.redis.redisHashRepository.annotations.RedisHashMapField;
import com.studypals.global.redis.redisHashRepository.annotations.RedisId;

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
public final class RedisEntityMetadataReader {

    private static final Map<Class<?>, EntityMeta> CACHE = new ConcurrentHashMap<>();

    public static EntityMeta get(Class<?> type) {
        return CACHE.computeIfAbsent(type, RedisEntityMetadataReader::scan);
    }

    private static EntityMeta scan(Class<?> type) {

        RedisHashEntity rh = type.getAnnotation(RedisHashEntity.class);

        if (!type.isAnnotationPresent(RedisHashEntity.class)) {
            throw new IllegalArgumentException(type + " missing @RedisHashEntity");
        }

        // key prefix
        String keyPrefix = rh.value().isBlank()
                ? Character.toLowerCase(type.getSimpleName().charAt(0))
                        + type.getSimpleName().substring(1)
                : rh.value();

        // TTL setting
        Expires expires = type.getAnnotation(Expires.class);
        long ttlValue = expires != null ? expires.value() : -1;
        TimeUnit ttlUnit = expires != null ? expires.unit() : TimeUnit.HOURS;

        // field 분류
        Field idField = null;
        Field mapField = null;
        List<Field> valueFields = new ArrayList<>();

        for (Field f : type.getDeclaredFields()) {
            if (Modifier.isStatic(f.getModifiers()) || f.isSynthetic()) continue;

            if (f.isAnnotationPresent(RedisId.class)) {
                if (idField != null) {
                    throw new IllegalArgumentException(type + " has multiple @RedisId");
                }
                idField = f;
            } else if (f.isAnnotationPresent(RedisHashMapField.class)) {
                if (mapField != null) {
                    throw new IllegalArgumentException(type + " has multiple @RedisHashMapField");
                }
                if (!Map.class.isAssignableFrom(f.getType())) {
                    throw new IllegalArgumentException("@RedisHashMapField field '" + f.getName() + "' must be map");
                }
                mapField = f;
            } else {
                if (!isAllowedType(f.getType()))
                    throw new IllegalStateException("Unsupported field type: " + f.getType());
                valueFields.add(f);
            }
        }
        if (idField == null) {
            throw new IllegalArgumentException(type + " has no @RedisId field");
        }

        MethodHandles.Lookup lookup = MethodHandles.lookup();

        MethodHandle idGetter, idSetter;

        try {
            idField.setAccessible(true);
            idGetter = lookup.unreflectGetter(idField);
            idSetter = lookup.unreflectSetter(idField);

            Map<Field, MethodHandle> getters = new HashMap<>();
            Map<Field, MethodHandle> setters = new HashMap<>();

            for (Field f : valueFields) {
                f.setAccessible(true);
                getters.put(f, lookup.unreflectGetter(f));
                setters.put(f, lookup.unreflectSetter(f));
            }
            if (mapField != null) {
                mapField.setAccessible(true);
                getters.put(mapField, lookup.unreflectGetter(mapField));
                setters.put(mapField, lookup.unreflectSetter(mapField));
            }

            return new EntityMeta(
                    type,
                    keyPrefix,
                    ttlValue,
                    ttlUnit,
                    idField,
                    idGetter,
                    idSetter,
                    List.copyOf(valueFields),
                    mapField,
                    Map.copyOf(getters),
                    Map.copyOf(setters));
        } catch (IllegalAccessException iae) {
            throw new RuntimeException("MethodHandle init failed for " + type, iae);
        }
    }

    private static List<Field> concat(List<Field> a, List<Field> b) {
        List<Field> all = new ArrayList<>(a.size() + b.size());
        all.addAll(a);
        all.addAll(b);
        return all;
    }

    private static boolean isAllowedType(Class<?> t) {
        return t == String.class
                || t == int.class
                || t == Integer.class
                || t == long.class
                || t == Long.class
                || t == double.class
                || t == Double.class
                || t == char.class
                || t == Character.class;
    }
}
