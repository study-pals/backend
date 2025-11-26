package com.studypals.global.redis.redisHashRepository;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
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
 * {@code @RedisHashEntity} 어노테이션이 붙은 클래스의 메타데이터를 분석하고 캐싱하는 리더 클래스입니다.
 *
 * <p>엔티티 클래스의 ID 필드, 일반 필드, Map 필드, TTL 설정 등을 분석하여 {@link EntityMeta} 객체로 변환합니다.
 * 변환된 메타데이터는 내부 캐시를 통해 재사용되므로 성능 손실 없이 반복 호출이 가능합니다.
 *
 * <p><b>동작 원리:</b><br>
 * - 클래스 리플렉션으로 필드를 분류하고 {@link MethodHandle} 기반 접근자를 생성합니다.<br>
 * - ID 필드는 Redis의 키로, 일반 필드는 "f:fieldName"으로 저장되며, Map 필드는 그대로 Redis Hash에 저장됩니다.<br>
 * - 유효성 검사 및 어노테이션 유무 검사를 통해 올바른 구조의 엔티티만 등록됩니다.
 *
 * @author jack8
 * @since 2025-05-25
 */
public final class RedisEntityMetadataReader {

    /**
     * Entity 클래스에 대한 메타데이터 캐시.
     * 한 번 분석된 클래스는 이후 재사용됩니다.
     */
    private static final Map<Class<?>, EntityMeta> CACHE = new ConcurrentHashMap<>();

    /**
     * 주어진 엔티티 클래스에 대한 {@link EntityMeta} 정보를 반환합니다.
     * 캐시가 존재하면 재사용하고, 없으면 새로 분석합니다.
     *
     * @param type 분석 대상 엔티티 클래스
     * @return 메타데이터 객체
     */
    public static EntityMeta get(Class<?> type) {
        return CACHE.computeIfAbsent(type, RedisEntityMetadataReader::scan);
    }

    /**
     * 엔티티 클래스를 분석하여 {@link EntityMeta}를 생성합니다.
     * 어노테이션, 필드, TTL, 접근자 등을 구성합니다.
     */
    private static EntityMeta scan(Class<?> type) {

        RedisHashEntity rh = type.getAnnotation(RedisHashEntity.class);
        if (rh == null) {
            throw new IllegalArgumentException(type + " missing @RedisHashEntity");
        }

        // key prefix
        String keyPrefix = rh.value().isBlank()
                ? Character.toLowerCase(type.getSimpleName().charAt(0))
                        + type.getSimpleName().substring(1)
                : rh.value();
        keyPrefix = keyPrefix + ":";

        // TTL setting
        Expires expires = type.getAnnotation(Expires.class);
        long ttlValue = expires != null ? expires.value() : -1;
        TimeUnit ttlUnit = expires != null ? expires.unit() : TimeUnit.HOURS;

        // field 분류
        Field idField = null;
        Field mapField = null;

        Class<?> mapKeyType = null;
        Class<?> mapValueType = null;
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
                Class<?> keyType = String.class;
                Class<?> valueType = String.class;

                Type genericType = f.getGenericType();
                if (genericType instanceof ParameterizedType pt) {
                    Type[] args = pt.getActualTypeArguments();
                    if (args.length == 2 && args[0] instanceof Class<?> k && args[1] instanceof Class<?> v) {
                        keyType = k;
                        valueType = v;
                    }
                }

                mapKeyType = keyType;
                mapValueType = valueType;
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

        try {
            MethodHandle idGetter, idSetter;
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
                    mapKeyType,
                    mapValueType,
                    Map.copyOf(getters),
                    Map.copyOf(setters));
        } catch (IllegalAccessException iae) {
            throw new RuntimeException("MethodHandle init failed for " + type, iae);
        }
    }

    /**
     * Redis 직렬화 대상이 될 수 있는 필드 타입인지 검사합니다.
     * 현재는 String, 숫자형, char 만 지원합니다.
     */
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
