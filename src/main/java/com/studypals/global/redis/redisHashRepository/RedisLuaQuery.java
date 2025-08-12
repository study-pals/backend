package com.studypals.global.redis.redisHashRepository;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.query.QueryMethod;
import org.springframework.data.repository.query.RepositoryQuery;

import com.studypals.global.redis.redisHashRepository.annotations.LuaQuery;

/**
 * {@link LuaQuery} 어노테이션이 붙은 Repository 메서드를 처리하기 위한 쿼리 실행 클래스입니다.
 *
 * <p>{@link org.springframework.data.repository.query.RepositoryQuery}를 구현하여,
 * 메서드 정의에 포함된 Lua 스크립트를 Redis에 실행하고 결과를 반환합니다.
 *
 * <p>QueryMethod 정보와 함께, RedisTemplate, 스크립트 본문, 반환 타입 등을 사용해
 * 동적으로 Redis Lua 쿼리를 실행합니다.
 *
 * @see LuaQuery
 * @see org.springframework.data.repository.query.QueryLookupStrategy
 * @see org.springframework.data.redis.core.script.DefaultRedisScript
 *
 * @author jack8
 * @since 2025-05-25
 */
public class RedisLuaQuery implements RepositoryQuery {

    /** RedisTemplate (String 기반 직렬화 사용) */
    private final RedisTemplate<String, String> template;
    /** 실행할 Lua 스크립트 정보 */
    private final DefaultRedisScript<?> script;
    /** Spring Data가 제공하는 쿼리 메타데이터 객체 */
    private final QueryMethod queryMethod;

    /** 엔티티 데이터에 대한 메타데이터 객체 */
    private final EntityMeta entityMeta;

    /**
     * 생성자 - 어노테이션 정보 기반으로 Lua 스크립트 및 반환 타입을 초기화합니다.
     *
     * @param template RedisTemplate
     * @param method Repository 인터페이스 내 선언된 메서드
     * @param metadata Repository 메타정보
     * @param factory 프로젝션 팩토리 (쿼리 반환 구조 처리용)
     */
    RedisLuaQuery(
            RedisTemplate<String, String> template,
            Method method,
            RepositoryMetadata metadata,
            ProjectionFactory factory,
            EntityMeta entityMeta) {

        this.template = template;
        this.queryMethod = new QueryMethod(method, metadata, factory);
        LuaQuery ann = method.getAnnotation(LuaQuery.class);
        this.script = new DefaultRedisScript<>(ann.value(), ann.resultType());
        this.entityMeta = entityMeta;
    }

    /**
     * Repository 메서드 호출 시 Lua 쿼리를 실행합니다.
     * <p>args[0] = KEYS (Redis key list), args[1] = ARGV (스크립트 인자)
     *
     * @param args 호출 시 전달된 인자 배열
     * @return Lua 스크립트 실행 결과
     */
    @Override
    public Object execute(Object[] args) {
        if (args.length == 0) {
            throw new IllegalArgumentException("@LuaQuery need parameters");
        }

        Object id = args[0];
        String redisKey = entityMeta.keyPrefix() + id;

        Object[] argv = flattenArgs(Arrays.copyOfRange(args, 1, args.length));
        return template.execute(script, Collections.singletonList(redisKey), argv);
    }

    private static Object[] flattenArgs(Object[] in) {
        List<Object> out = new ArrayList<>();
        for (Object a : in) {
            if (a == null) continue;
            Class<?> c = a.getClass();

            if (c.isArray()) {
                int len = Array.getLength(a);
                for (int i = 0; i < len; i++) out.add(Array.get(a, i));
            } else if (a instanceof Iterable<?> it) {
                for (Object e : it) if (e != null) out.add(e);
            } else {
                out.add(a);
            }
        }
        return out.toArray();
    }

    /**
     * Spring Data 내부 처리를 위한 QueryMethod 반환
     */
    @Override
    public QueryMethod getQueryMethod() {
        return queryMethod;
    }
}
