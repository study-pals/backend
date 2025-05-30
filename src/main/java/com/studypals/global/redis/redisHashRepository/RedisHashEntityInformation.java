package com.studypals.global.redis.redisHashRepository;

import org.springframework.data.repository.core.support.AbstractEntityInformation;

/**
 * Redis 엔티티에 대한 ID 접근 및 메타정보를 제공하는 Spring Data용 구현체입니다.
 *
 * <p>{@link AbstractEntityInformation}을 상속하여,
 * Redis 기반 엔티티의 ID 조회 및 ID 타입 정보를 제공하도록 구현되었습니다.
 * 내부적으로 {@link EntityMeta}를 활용하여 ID 필드에 접근합니다.
 *
 * <p>이 클래스는 Spring Data Repository 구현부에서 Repository 인터페이스를 처리할 때,
 * 해당 엔티티의 ID 필드 및 ID 타입을 결정하는 데 사용됩니다.
 *
 * @param <T> 엔티티 타입
 * @param <ID> ID 타입
 *
 * @author jack8
 * @since 2025-05-25
 */
public class RedisHashEntityInformation<T, ID> extends AbstractEntityInformation<T, ID> {

    /** 분석된 엔티티 메타데이터 */
    private final EntityMeta entityMeta;

    /**
     * 생성자
     *
     * @param domainClass 엔티티 클래스
     * @param entityMeta  엔티티 메타정보 (리플렉션 기반)
     */
    RedisHashEntityInformation(Class<T> domainClass, EntityMeta entityMeta) {
        super(domainClass);
        this.entityMeta = entityMeta;
    }

    /**
     * 주어진 엔티티 인스턴스로부터 ID 값을 추출합니다.
     * 내부적으로 {@link java.lang.invoke.MethodHandle MethodHandle} 기반 getter를 사용하여 성능 손실을 최소화합니다.
     *
     * @param entity 대상 엔티티
     * @return 해당 엔티티의 ID 값
     * @throws RuntimeException ID 추출 실패 시 래핑된 예외
     */
    @Override
    @SuppressWarnings("unchecked")
    public ID getId(T entity) {
        try {
            return (ID) entityMeta.idGetter().invoke(entity);
        } catch (Throwable t) {
            throw new RuntimeException("ID access failed", t);
        }
    }

    /**
     * 엔티티의 ID 타입을 반환합니다.
     * 예: {@code String.class}, {@code Long.class} 등
     *
     * @return ID 필드의 클래스 타입
     */
    @Override
    @SuppressWarnings("unchecked")
    public Class<ID> getIdType() {
        return (Class<ID>) entityMeta.idField().getType();
    }
}
