package com.studypals.global.redis.redisHashRepository;

import org.springframework.data.repository.core.support.AbstractEntityInformation;

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
public class RedisHashEntityInformation<T, ID> extends AbstractEntityInformation<T, ID> {
    private final EntityMeta entityMeta;

    RedisHashEntityInformation(Class<T> domainClass, EntityMeta entityMeta) {
        super(domainClass);
        this.entityMeta = entityMeta;
    }

    @Override
    @SuppressWarnings("unchecked")
    public ID getId(T entity) {
        try {
            return (ID) entityMeta.idGetter().invoke(entity);
        } catch (Throwable t) {
            throw new RuntimeException("ID access failed", t);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Class<ID> getIdType() {
        return (Class<ID>) entityMeta.idField().getType();
    }
}
