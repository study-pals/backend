package com.studypals.global.redis.redisHashRepository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.repository.Repository;

/**
 * Redis Hash 기반 도메인 객체를 저장하고 조회하기 위한 Repository 인터페이스입니다.
 *
 * <p>{@link org.springframework.data.repository.Repository}를 확장하며,
 * Redis의 Hash 자료구조에 맞춘 CRUD 유틸리티 메서드를 정의합니다.
 * 이 인터페이스를 상속한 Repository는 동적으로 구현체가 생성되어
 * {@code SimpleRedisHashRepository} 등에 위임됩니다.
 *
 * <p>Hash 구조의 특성상 전체 도메인을 Scan 하지 않으며, ID 기반으로 접근합니다.
 *
 * @param <E> 엔티티 타입
 * @param <ID> Redis 해시 키 타입
 *
 * @author jack8
 * @since 2025-05-25
 */
public interface RedisHashRepository<E, ID> extends Repository<E, ID> {

    /**
     * 주어진 엔티티를 Redis Hash로 저장합니다.
     * 일반 필드는 접두사("f:")가 붙고, Map 필드는 그대로 삽입됩니다.
     *
     * @param entity 저장할 객체
     */
    void save(E entity);

    /**
     * 해시 키에 해당하는 Redis 객체를 조회합니다.
     *
     * @param id Redis Hash 키
     * @return 존재하면 Optional로 감싼 엔티티, 없으면 Optional.empty()
     */
    Optional<E> findById(ID id);

    /**
     * 주어진 키에 해당하는 Redis Hash를 삭제합니다.
     *
     * @param id 삭제할 Redis Hash 키
     */
    void delete(ID id);

    /**
     * 해당 키를 가진 Redis Hash가 존재하는지 확인합니다.
     *
     * @param id Redis Hash 키
     * @return 존재 여부
     */
    boolean existById(ID id);

    /**
     * 다수의 키에 대해 Redis Hash를 조회합니다.
     *
     * @param ids 조회할 키 목록
     * @return 존재하는 엔티티만 포함된 Iterable
     */
    Iterable<E> findAllById(Iterable<ID> ids);

    /**
     * 특정 Redis Hash 키에 대해, 내부 필드 중 일부 키를 기반으로 값을 조회합니다.
     *
     * @param hashKey Redis 해시 키
     * @param fieldKey 해시 내부 필드 키 목록
     * @return 필드 키-값 쌍의 Map
     */
    Map<String, String> findHashFieldsById(ID hashKey, List<String> fieldKey);

    /**
     * 특정 Redis 해시에 필드 키-값을 추가하거나 덮어씁니다.
     * 이 작업은 {@code @RedisHashMapField}와 연결된 필드에만 영향을 줍니다.
     *
     * @param hashKey Redis 해시 키
     * @param map 필드 키-값 쌍
     */
    void saveMapById(ID hashKey, Map<String, String> map);

    /**
     * Redis 해시에서 특정 필드 키들을 삭제합니다.
     *
     * @param hashKey 대상 해시 키
     * @param fieldKey 삭제할 필드 키 목록
     */
    void deleteMapById(ID hashKey, List<String> fieldKey);
}
