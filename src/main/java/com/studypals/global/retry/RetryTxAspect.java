package com.studypals.global.retry;

import java.lang.reflect.UndeclaredThrowableException;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

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
 * @since 2026-01-03
 */
@Slf4j
@Aspect
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RetryTxAspect {

    @Around("@annotation(retryTx)")
    public Object around(ProceedingJoinPoint pjp, RetryTx retryTx) throws Throwable {

        int maxAttempts = Math.max(1, retryTx.maxAttempts());
        long backoff = Math.max(0, retryTx.backoffMs());
        double multiplier = Math.max(1.0, retryTx.multiplier());
        long maxBackoff = Math.max(backoff, retryTx.maxBackoffMs());

        Throwable last = null;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return pjp.proceed(); // 다음 advice(@Transactional) 실행
            } catch (Throwable raw) { // raw throwable 받음
                Throwable ex = unwrap(raw);
                last = ex;

                if (!isRetryable(ex, retryTx)) { // 어노테이션에서 정의한 exception 필터링
                    throw ex;
                }

                if (attempt == maxAttempts) { // 재시도 최대 횟수 초과 시 종료
                    throw ex;
                }

                long sleepMs = calcBackoff(backoff, multiplier, maxBackoff, attempt); // 재시도 시 일정 기간 waiting

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

        // 실제 여기까지 도달하지 않음
        throw last;
    }

    private boolean isRetryable(Throwable ex, RetryTx cfg) {
        // Error는 retry 대상 아님
        if (ex instanceof Error) return false;

        // noRetryFor 우선
        for (Class<? extends Throwable> c : cfg.noRetryFor()) {
            if (c.isInstance(ex)) return false;
        }

        Class<? extends Throwable>[] retryFor = cfg.retryFor();
        if (retryFor == null || retryFor.length == 0) {
            // 비워뒀다면 보수적으로 RuntimeException만
            return ex instanceof RuntimeException;
        }

        for (Class<? extends Throwable> c : retryFor) {
            if (c.isInstance(ex)) return true;
        }
        return false;
    }

    private long calcBackoff(long base, double multiplier, long max, int attempt) {
        // attempt=1 실패 후 대기 => base
        // attempt=2 실패 후 대기 => base * multiplier
        double pow = Math.pow(multiplier, Math.max(0, attempt - 1));
        long v = (long) Math.round(base * pow);
        if (v < 0) v = max;
        return Math.min(v, max);
    }

    private void sleep(long ms) {
        if (ms <= 0) return;
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }

    private Throwable unwrap(Throwable t) {
        if (t instanceof UndeclaredThrowableException ute && ute.getUndeclaredThrowable() != null) {
            return ute.getUndeclaredThrowable();
        }
        return t;
    }
}
