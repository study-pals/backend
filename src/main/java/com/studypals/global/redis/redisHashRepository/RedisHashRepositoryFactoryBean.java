package com.studypals.global.redis.redisHashRepository;

import java.lang.reflect.ParameterizedType;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.data.redis.core.RedisTemplate;

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
 * @since 2025-05-26
 */
public class RedisHashRepositoryFactoryBean<T> implements FactoryBean<T> {

    private final Class<T> repoIntf;
    private final RedisTemplate<String, String> template;
    private T proxy; // ← ByteBuddy 구현 인스턴스

    public RedisHashRepositoryFactoryBean(Class<T> repoIntf, RedisTemplate<String, String> template) {
        this.repoIntf = repoIntf;
        this.template = template;
    }

    @Override
    public T getObject() {
        if (proxy == null) init();
        return proxy;
    }

    @Override
    public Class<?> getObjectType() {
        return repoIntf;
    }

    private void init() {
        // 제네릭 파라미터 추출
        ParameterizedType g = (ParameterizedType) repoIntf.getGenericInterfaces()[0];
        Class<?> entityType = (Class<?>) g.getActualTypeArguments()[0];
        Class<?> idType = (Class<?>) g.getActualTypeArguments()[1];

        DynamicRepositoryBuilder builder = new DynamicRepositoryBuilder(template);
        proxy = builder.build(entityType, idType, repoIntf, RedisEntityMetadataReader.get(entityType));
    }
}
