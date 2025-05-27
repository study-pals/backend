package com.studypals.global.redis.redisHashRepository;

import java.lang.reflect.Method;
import java.util.List;

import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.query.QueryMethod;
import org.springframework.data.repository.query.RepositoryQuery;

import com.studypals.global.redis.redisHashRepository.annotations.LuaQuery;

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
public class RedisLuaQuery implements RepositoryQuery {

    private final RedisTemplate<String, String> template;
    private final DefaultRedisScript<?> script;
    private final QueryMethod queryMethod;

    RedisLuaQuery(
            RedisTemplate<String, String> template,
            Method method,
            RepositoryMetadata metadata,
            ProjectionFactory factory) {

        this.template = template;
        this.queryMethod = new QueryMethod(method, metadata, factory);
        LuaQuery ann = method.getAnnotation(LuaQuery.class);
        this.script = new DefaultRedisScript<>(ann.value(), ann.resultType());
    }

    @Override
    public Object execute(Object[] args) {
        String hashKey = (String) args[0];
        List<?> fieldKeys = (List<?>) args[1];
        return template.execute(script, List.of(hashKey), fieldKeys.toArray());
    }

    @Override
    public QueryMethod getQueryMethod() {
        return queryMethod;
    }
}
