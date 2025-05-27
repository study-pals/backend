package com.studypals.global.redis.redisHashRepository;

import java.util.Optional;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.repository.core.EntityInformation;
import org.springframework.data.repository.core.RepositoryInformation;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;
import org.springframework.data.repository.query.QueryLookupStrategy;
import org.springframework.data.repository.query.ValueExpressionDelegate;

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
public class RedisHashRepositoryFactory extends RepositoryFactorySupport {

    private final RedisTemplate<String, String> tpl;

    public RedisHashRepositoryFactory(RedisTemplate<String, String> tpl) {
        this.tpl = tpl;
    }

    @Override
    protected Class<?> getRepositoryBaseClass(RepositoryMetadata metadata) {
        return SimpleRedisHashRepository.class;
    }

    @Override
    public <T, ID> EntityInformation<T, ID> getEntityInformation(Class<T> domainClass) {
        EntityMeta entityMeta = RedisEntityMetadataReader.get(domainClass);
        return new RedisHashEntityInformation<>(domainClass, entityMeta);
    }

    @Override
    protected Object getTargetRepository(RepositoryInformation metadata) {
        Class<?> entityType = metadata.getDomainType();
        EntityMeta em = RedisEntityMetadataReader.get(entityType);
        return new SimpleRedisHashRepository<>(tpl, em);
    }

    @Override
    protected Optional<QueryLookupStrategy> getQueryLookupStrategy(
            QueryLookupStrategy.Key key, ValueExpressionDelegate valueExpressionDelegate) {
        return Optional.of((method, md, proj, named) -> new RedisLuaQuery(tpl, method, md, proj));
    }
}
