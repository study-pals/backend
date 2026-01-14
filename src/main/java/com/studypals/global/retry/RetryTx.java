package com.studypals.global.retry;

import java.lang.annotation.*;

import org.springframework.core.annotation.AliasFor;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * 트랜잭션 메서드에 재시도(retry) 동작을 적용하기 위해 사용하는 선언적 어노테이션입니다.
 *
 * <p>
 * 이 어노테이션이 부착된 메서드는 {@link RetryTxAspect}에 의해 감지되며,
 * 지정된 조건에 따라 동일 메서드가 여러 번 재호출될 수 있습니다.
 * 재시도 로직의 상세한 동작 방식은 Aspect 구현에 위임됩니다.
 *
 * <p>
 * {@link Transactional}을 메타 어노테이션으로 포함하고 있으며,
 * 트랜잭션 전파 수준, 격리 수준 등은 이 어노테이션의 속성을 통해 함께 설정할 수 있습니다.
 *
 * <p><b>사용 전제:</b><br>
 * 구현 클래스의 실제 메서드에 부착하는 사용을 전제로 합니다.
 *
 * @see RetryTxAspect
 * @author jack8
 * @since 2026-01-03
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Transactional
public @interface RetryTx {

    /**
     * 최대 재시도 횟수 (최초 실행 포함).
     */
    int maxAttempts() default 3;

    /**
     * 재시도 간 기본 대기 시간(ms).
     */
    long backoffMs() default 200;

    /**
     * 재시도 간 대기 시간 증가 배수.
     */
    double multiplier() default 1.0;

    /**
     * 재시도 대기 시간의 최대 상한(ms).
     */
    long maxBackoffMs() default 2_000;

    /**
     * 재시도를 수행할 예외 타입 목록.
     */
    Class<? extends Throwable>[] retryFor() default {
        PessimisticLockingFailureException.class, TransientDataAccessException.class
    };

    /**
     * 재시도를 수행하지 않을 예외 타입 목록.
     */
    Class<? extends Throwable>[] noRetryFor() default {};

    /**
     * 트랜잭션 전파 수준.
     */
    @AliasFor(annotation = Transactional.class, attribute = "propagation")
    Propagation propagation() default Propagation.REQUIRED;

    /**
     * 트랜잭션 격리 수준.
     */
    @AliasFor(annotation = Transactional.class, attribute = "isolation")
    Isolation isolation() default Isolation.DEFAULT;

    /**
     * 트랜잭션 타임아웃(초).
     */
    @AliasFor(annotation = Transactional.class, attribute = "timeout")
    int timeout() default -1;

    /**
     * 읽기 전용 트랜잭션 여부.
     */
    @AliasFor(annotation = Transactional.class, attribute = "readOnly")
    boolean readOnly() default false;

    /**
     * 롤백 대상 예외 타입 목록.
     */
    @AliasFor(annotation = Transactional.class, attribute = "rollbackFor")
    Class<? extends Throwable>[] rollbackFor() default {};

    /**
     * 롤백하지 않을 예외 타입 목록.
     */
    @AliasFor(annotation = Transactional.class, attribute = "noRollbackFor")
    Class<? extends Throwable>[] noRollbackFor() default {};
}
