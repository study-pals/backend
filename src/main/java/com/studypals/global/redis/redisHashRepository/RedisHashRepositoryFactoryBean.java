package com.studypals.global.redis.redisHashRepository;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.core.support.RepositoryFactoryBeanSupport;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;

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
public class RedisHashRepositoryFactoryBean<S, ID, T extends Repository<S, ID>>
        extends RepositoryFactoryBeanSupport<T, S, ID> {

    private final RedisTemplate<String, String> template;

    public RedisHashRepositoryFactoryBean(Class<T> repositoryInterface, RedisTemplate<String, String> template) {
        super(repositoryInterface);
        this.template = template;
    }

    @Override
    protected RepositoryFactorySupport createRepositoryFactory() {
        return new RedisHashRepositoryFactory(template);
    }
}
