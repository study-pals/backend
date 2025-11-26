package com.studypals.global.redis.redisHashRepository;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * {@code @RedisHashEntity}로 선언된 엔티티 클래스의 메타데이터를 담는 불변 객체입니다.
 *
 * <p>해당 클래스는 리플렉션 기반의 분석 결과를 정리하여,
 * Redis 직렬화/역직렬화 및 TTL 설정, 필드 접근 등을 효율적으로 수행하기 위한 정보를 제공합니다.
 * 주로 {@link RedisEntityMetadataReader}에 의해 생성되며 캐싱되어 재사용됩니다.
 *
 * <p><b>사용 목적:</b>
 * 엔티티를 Redis Hash로 변환하거나, Redis Hash를 엔티티로 역변환할 때 필요한
 * 모든 메타정보(id 필드, 일반 필드, Map 필드, 접근자 메서드 등)를 포함합니다.
 *
 * @param type        실제 엔티티 클래스 타입
 * @param keyPrefix   Redis key 생성 시 사용할 prefix (현재는 사용하지 않음)
 * @param ttlValue    TTL 값 (예: 10L)
 * @param ttlUnit     TTL 단위 (예: TimeUnit.HOURS)
 * @param idField     @RedisId가 붙은 필드 (Redis Hash의 Key로 사용됨)
 * @param idGetter    idField에 대한 MethodHandle 기반 getter
 * @param idSetter    idField에 대한 MethodHandle 기반 setter
 * @param valueFields 일반 필드 목록 (String, int, long 등)
 * @param mapField    @RedisHashMapField가 붙은 필드 (Map 자료구조)
 * @param getters     valueFields 및 mapField에 대한 필드별 getter 핸들러
 * @param setters     valueFields 및 mapField에 대한 필드별 setter 핸들러
 *
 * @author jack8
 * @since 2025-05-25
 */
public record EntityMeta(
        Class<?> type,
        String keyPrefix,
        long ttlValue,
        TimeUnit ttlUnit,
        Field idField,
        MethodHandle idGetter,
        MethodHandle idSetter,
        List<Field> valueFields,
        Field mapField,
        Class<?> mapKeyType,
        Class<?> mapValueType,
        Map<Field, MethodHandle> getters,
        Map<Field, MethodHandle> setters) {}
