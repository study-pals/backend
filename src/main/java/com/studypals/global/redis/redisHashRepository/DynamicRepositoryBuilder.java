package com.studypals.global.redis.redisHashRepository;

import static net.bytebuddy.matcher.ElementMatchers.is;
import static net.bytebuddy.matcher.ElementMatchers.named;

import java.lang.reflect.Method;
import java.time.Duration;
import java.util.List;
import java.util.Map;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import com.studypals.global.redis.redisHashRepository.annotations.LuaQuery;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;

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
 * @since 2025-05-25
 */
public final class DynamicRepositoryBuilder {

    /* ────────────────────────── 핸들러 static 클래스들 ───────────────────────── */

    /** save 용 delegate */
    public static class SaveDelegate {
        private final RedisTemplate<String, String> tpl;
        private final EntityMeta meta;

        public SaveDelegate(RedisTemplate<String, String> tpl, EntityMeta meta) {
            this.tpl = tpl;
            this.meta = meta;
        }

        public void save(Object entity) {
            try {
                String key = meta.idGetter().invoke(entity).toString();
                Map<String, String> map = RedisEntityMapper.toHash(entity, meta);
                tpl.opsForHash().putAll(key, map);
                if (meta.ttlValue() > 0)
                    tpl.expire(key, Duration.of(meta.ttlValue(), meta.ttlUnit().toChronoUnit()));
            } catch (Throwable t) {
                throw new RuntimeException(t);
            }
        }
    }

    /** findById delegate */
    public static class FindDelegate {
        private final RedisTemplate<String, String> tpl;
        private final EntityMeta meta;

        public FindDelegate(RedisTemplate<String, String> tpl, EntityMeta meta) {
            this.tpl = tpl;
            this.meta = meta;
        }

        @RuntimeType
        public Object invoke(@AllArguments Object[] args) {
            String key = args[0].toString(); // id
            Map<Object, Object> raw = tpl.opsForHash().entries(key);
            Object entity = raw == null || raw.isEmpty() ? null : RedisEntityMapper.fromHash(raw, meta);
            /* 호출 측이 Optional<T> 원하면 Optional.ofNullable…로 감싸도 됨 */
            return entity;
        }
    }

    /** delete delegate */
    public static class DeleteDelegate {
        private final RedisTemplate<String, String> tpl;

        public DeleteDelegate(RedisTemplate<String, String> tpl) {
            this.tpl = tpl;
        }

        public void deleteById(Object id) {
            tpl.delete(id.toString());
        }
    }

    /** exists delegate */
    public static class ExistsDelegate {
        private final RedisTemplate<String, String> tpl;

        public ExistsDelegate(RedisTemplate<String, String> tpl) {
            this.tpl = tpl;
        }

        public boolean existsById(Object id) {
            return Boolean.TRUE.equals(tpl.hasKey(id.toString()));
        }
    }

    /** Lua delegate (스크립트·설정 각각 인스턴스 보유) */
    public static class LuaDelegate {
        private final RedisTemplate<String, String> tpl;
        private final DefaultRedisScript<?> script;

        public LuaDelegate(RedisTemplate<String, String> tpl, DefaultRedisScript<?> script) {
            this.tpl = tpl;
            this.script = script;
        }

        @RuntimeType
        public Object invoke(List<String> keys, List<Object> argv) {
            return tpl.execute(script, keys, argv.toArray());
        }
    }

    /* ────────────────────────── 빌더 본체 ───────────────────────── */

    private final RedisTemplate<String, String> template;

    public DynamicRepositoryBuilder(RedisTemplate<String, String> template) {
        this.template = template;
    }

    @SuppressWarnings("unchecked")
    public <E, ID, T> T build(Class<E> entityType, Class<ID> idType, Class<T> repoIntf, EntityMeta meta) {

        var bb = new ByteBuddy().subclass(repoIntf).name(repoIntf.getName() + "Impl");

        bb = bb.method(named("save")).intercept(MethodDelegation.to(new SaveDelegate(template, meta)));

        bb = bb.method(named("findById")).intercept(MethodDelegation.to(new FindDelegate(template, meta)));

        bb = bb.method(named("deleteById")).intercept(MethodDelegation.to(new DeleteDelegate(template)));

        bb = bb.method(named("existsById")).intercept(MethodDelegation.to(new ExistsDelegate(template)));

        /* LuaQuery 메서드 처리 */
        for (Method m : repoIntf.getMethods()) {
            LuaQuery q = m.getAnnotation(LuaQuery.class);
            if (q == null) continue;

            DefaultRedisScript<?> scr = new DefaultRedisScript<>(q.value(), q.resultType());
            LuaDelegate del = new LuaDelegate(template, scr);

            bb = bb.method(is(m)).intercept(MethodDelegation.to(del));
        }

        try (var unloaded = bb.make()) {
            Class<? extends T> impl = (Class<? extends T>)
                    unloaded.load(repoIntf.getClassLoader(), ClassLoadingStrategy.Default.INJECTION)
                            .getLoaded();
            return impl.getDeclaredConstructor().newInstance();
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }
}
