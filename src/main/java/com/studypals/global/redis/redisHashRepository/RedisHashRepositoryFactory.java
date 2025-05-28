package com.studypals.global.redis.redisHashRepository;

import java.util.Optional;

import org.jetbrains.annotations.NotNull;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.repository.core.EntityInformation;
import org.springframework.data.repository.core.RepositoryInformation;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;
import org.springframework.data.repository.query.QueryLookupStrategy;
import org.springframework.data.repository.query.ValueExpressionDelegate;

/**
 * {@link RedisHashRepository} 인터페이스를 동적으로 구현하는 Spring Data Repository 팩토리입니다.
 *
 * <p>Spring Data가 {@code RepositoryFactorySupport}를 기반으로 사용자 정의 Repository를 생성할 수 있도록 확장한 클래스입니다.
 * {@link SimpleRedisHashRepository}를 구현체로 사용하며, 엔티티 메타정보를 기반으로 프록시 객체를 반환합니다.
 *
 * <p>또한, {@link com.studypals.global.redis.redisHashRepository.annotations.LuaQuery LuaQuery}를 기반으로 한 커스텀 쿼리 전략도 함께 제공합니다.
 *
 * @author jack8
 * @since 2025-05-25
 */
public class RedisHashRepositoryFactory extends RepositoryFactorySupport {

    /** Redis 연동을 위한 RedisTemplate */
    private final RedisTemplate<String, String> tpl;

    private EntityMeta entityMeta;

    /**
     * 생성자
     *
     * @param tpl RedisTemplate 인스턴스 (String 기반 직렬화 사용 전제)
     */
    public RedisHashRepositoryFactory(RedisTemplate<String, String> tpl) {
        this.tpl = tpl;
    }

    /**
     * 리포지토리 구현체로 사용할 클래스 지정
     * 이 클래스는 실제 동작을 수행하는 concrete 클래스입니다.
     */
    @NotNull
    @Override
    protected Class<?> getRepositoryBaseClass(@NotNull RepositoryMetadata metadata) {
        return SimpleRedisHashRepository.class;
    }

    /**
     * 엔티티 클래스에 대한 메타데이터 및 ID 처리 정보를 제공합니다.
     *
     * @param domainClass 엔티티 클래스
     * @param <T> 엔티티 타입
     * @param <ID> ID 타입
     * @return EntityInformation 객체
     */
    @NotNull
    @Override
    public <T, ID> EntityInformation<T, ID> getEntityInformation(@NotNull Class<T> domainClass) {
        entityMeta = RedisEntityMetadataReader.get(domainClass);
        return new RedisHashEntityInformation<>(domainClass, entityMeta);
    }

    /**
     * 실제 리포지토리 프록시 객체의 내부 구현 인스턴스를 생성합니다.
     *
     * @param metadata 리포지토리 정보 (도메인 타입 등 포함)
     * @return 실제 동작을 수행할 구현체 인스턴스
     */
    @NotNull
    @Override
    protected Object getTargetRepository(RepositoryInformation metadata) {
        Class<?> entityType = metadata.getDomainType();
        EntityMeta em = RedisEntityMetadataReader.get(entityType);
        return new SimpleRedisHashRepository<>(tpl, em);
    }

    /**
     * 쿼리 메서드 커스텀 전략 등록.
     * {@link com.studypals.global.redis.redisHashRepository.annotations.LuaQuery} 어노테이션이 붙은 Repository 메서드를 지원합니다.
     */
    @NotNull
    @Override
    protected Optional<QueryLookupStrategy> getQueryLookupStrategy(
            @NotNull QueryLookupStrategy.Key key, @NotNull ValueExpressionDelegate valueExpressionDelegate) {
        return Optional.of((method, md, proj, named) -> new RedisLuaQuery(tpl, method, md, proj, entityMeta));
    }
}
