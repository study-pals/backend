package com.studypals.global.retry;

import java.lang.reflect.UndeclaredThrowableException;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * {@link RetryTx} 어노테이션이 적용된 메서드에 대해 재시도 정책을 수행하는 AOP Aspect 입니다.
 *
 * <p>
 * 대상 메서드 실행 중 지정한 예외({@link RetryTx#retryFor()})가 발생하면,
 * 최대 시도 횟수({@link RetryTx#maxAttempts()})까지 재호출을 수행합니다.
 * 재시도 간 대기(backoff)는 base 값({@link RetryTx#backoffMs()})과 배수({@link RetryTx#multiplier()})를 기반으로 증가하며,
 * 최대 대기 시간({@link RetryTx#maxBackoffMs()})을 초과하지 않습니다.
 *
 * <p><b>빈 관리:</b><br>
 * {@code @Component} 로 스프링 빈으로 등록되며, {@code @Aspect} 를 통해 Advisor(포인트컷 + 어드바이스)로 구성됩니다. <br>
 * 스프링 AOP 프록시 체인에 포함되어, {@link RetryTx} 대상 메서드 호출을 가로채 재시도 로직을 적용합니다. <br>
 * {@code @Order(Ordered.HIGHEST_PRECEDENCE)} 로 우선순위를 높게 설정하여, 다른 Advisor(예: 트랜잭션)보다 먼저 실행되도록 합니다.
 *
 * <p><b>외부 모듈:</b><br>
 * Spring AOP / AspectJ 표현식 기반 포인트컷을 사용합니다. <br>
 * - {@code org.springframework.boot:spring-boot-starter-aop} <br>
 * @author jack8
 * @since 2026-01-03
 */
@Slf4j
@Aspect
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RetryTxAspect {

    /**
     * {@link RetryTx}가 붙은 메서드를 가로채 재시도를 수행합니다.
     *
     * <p>
     * 주의: 현재 구현은 {@code MethodSignature#getMethod()}로 어노테이션을 조회합니다. <br>
     * 어노테이션 조회가 {@code null}이 될 수 있습니다. <br>
     * 따라서, 구현 클래스의 메서드에만 사용이 가능합니다.
     */
    @Around("@annotation(com.studypals.global.retry.RetryTx)")
    public Object around(ProceedingJoinPoint pjp) throws Throwable {
        // 어노테이션 설정값 조회
        MethodSignature methodSignature = (MethodSignature) pjp.getSignature();
        RetryTx retryTx = methodSignature.getMethod().getAnnotation(RetryTx.class);

        // 방어적 보정 (이상값을 넣어도 최소 1회는 실행되도록)
        int maxAttempts = Math.max(1, retryTx.maxAttempts());
        long backoff = Math.max(0, retryTx.backoffMs());
        double multiplier = Math.max(1.0, retryTx.multiplier());
        long maxBackoff = Math.max(backoff, retryTx.maxBackoffMs());

        Throwable last = null;

        // attempt는 1부터 시작 (1회차가 최초 실행)
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                // 다음 advice(예: @Transactional) 및 실제 타깃 메서드 실행
                return pjp.proceed();
            } catch (Throwable raw) {
                // 프록시/리플렉션 래핑 예외를 풀고 실제 예외를 기준으로 판단
                Throwable ex = unwrap(raw);
                last = ex;

                // 재시도 불가 예외면 즉시 종료
                if (!isRetryable(ex, retryTx)) {
                    throw ex;
                }

                // 최대 횟수 초과 시 종료
                if (attempt == maxAttempts) {
                    throw ex;
                }

                // 대기 시간 계산 후 sleep
                long sleepMs = calcBackoff(backoff, multiplier, maxBackoff, attempt);

                log.warn(
                        "[RetryTx] attempt {}/{} failed: {}: {} (sleep {}ms) - {}",
                        attempt,
                        maxAttempts,
                        ex.getClass().getSimpleName(),
                        ex.getMessage(),
                        sleepMs,
                        pjp.getSignature().toShortString());

                sleep(sleepMs);
            }
        }

        // 논리상 도달하지 않지만, 컴파일러/흐름 안정성을 위해 유지
        throw last;
    }

    /**
     * 재시도 대상 예외인지 판단합니다.
     *
     * <p>
     * 우선순위: {@code noRetryFor}가 가장 우선이며, 그 다음 {@code retryFor} 기준으로 판정합니다.
     */
    private boolean isRetryable(Throwable ex, RetryTx cfg) {
        // JVM Error 계열은 재시도 대상으로 보지 않음
        if (ex instanceof Error) return false;

        // noRetryFor가 우선
        for (Class<? extends Throwable> c : cfg.noRetryFor()) {
            if (c.isInstance(ex)) return false;
        }

        // retryFor 미지정 시 보수적으로 RuntimeException만 재시도
        Class<? extends Throwable>[] retryFor = cfg.retryFor();
        if (retryFor == null || retryFor.length == 0) {
            return ex instanceof RuntimeException;
        }

        for (Class<? extends Throwable> c : retryFor) {
            if (c.isInstance(ex)) return true;
        }
        return false;
    }

    /**
     * 재시도 간 backoff 시간을 계산합니다.
     *
     * <p>
     * attempt=1 실패 후 대기: base <br>
     * attempt=2 실패 후 대기: base * multiplier <br>
     * ...
     */
    private long calcBackoff(long base, double multiplier, long max, int attempt) {
        double pow = Math.pow(multiplier, Math.max(0, attempt - 1));
        long v = (long) Math.round(base * pow);

        // 오버플로 등 비정상 값 방어
        if (v < 0) v = max;

        return Math.min(v, max);
    }

    /**
     * 지정한 시간(ms)만큼 대기합니다.
     * 인터럽트 발생 시 인터럽트 플래그를 복구합니다.
     */
    private void sleep(long ms) {
        if (ms <= 0) return;

        try {
            Thread.sleep(ms);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * 프록시 계층에서 감싸진 예외를 실제 예외로 단순화합니다.
     * (예: {@link UndeclaredThrowableException})
     */
    private Throwable unwrap(Throwable t) {
        if (t instanceof UndeclaredThrowableException ute && ute.getUndeclaredThrowable() != null) {
            return ute.getUndeclaredThrowable();
        }
        return t;
    }
}
