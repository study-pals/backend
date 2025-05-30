package com.studypals.global.redis.redisHashRepository;

import org.jetbrains.annotations.NotNull;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.core.support.RepositoryFactoryBeanSupport;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;

/**
 * {@link RedisHashRepository}의 구현체 생성을 Spring Data에 위임하기 위한 FactoryBean 클래스입니다.
 *
 * <p>Spring Data Repository 확장 시 필요한 {@link RepositoryFactoryBeanSupport}를 상속하며,
 * 내부적으로 {@link RedisHashRepositoryFactory}를 생성하여 Redis 전용 Repository 프록시를 제공합니다.
 *
 * <p>이 클래스는 {@link RedisHashRepositoriesRegistrar}에 의해 BeanDefinition으로 등록되며,
 * 주입된 Repository 인터페이스에 대한 프록시 구현체를 생성합니다.
 *
 * @param <S>  도메인 타입
 * @param <ID> ID 타입
 * @param <T>  Repository 인터페이스 타입
 *
 * @author jack8
 * @since 2025-05-25
 */
public class RedisHashRepositoryFactoryBean<S, ID, T extends Repository<S, ID>>
        extends RepositoryFactoryBeanSupport<T, S, ID> {

    /** Redis 연동용 RedisTemplate (String 기반 직렬화 전제) */
    private final RedisTemplate<String, String> template;

    /**
     * 생성자
     *
     * @param repositoryInterface Repository 인터페이스 타입
     * @param template RedisTemplate 인스턴스
     */
    public RedisHashRepositoryFactoryBean(Class<T> repositoryInterface, RedisTemplate<String, String> template) {
        super(repositoryInterface);
        this.template = template;
    }

    /**
     * 실제 리포지토리 프록시 객체를 생성할 팩토리 구현체를 반환합니다.
     *
     * @return {@link RedisHashRepositoryFactory} 인스턴스
     */
    @NotNull
    @Override
    protected RepositoryFactorySupport createRepositoryFactory() {
        return new RedisHashRepositoryFactory(template);
    }
}
