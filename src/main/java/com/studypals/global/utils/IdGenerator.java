package com.studypals.global.utils;

import static java.lang.System.currentTimeMillis;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;

import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * 고유한 64비트 ID를 생성하는 유틸리티 클래스입니다.
 * <p>
 * ID는 총 64비트로 구성되며 다음과 같은 구조를 따릅니다:
 * <pre>
 * [ 41bit 타임스탬프 ][ 4bit 서버 ID ][ 9bit 시퀀스 ][ 5bit 타입 ][ 5bit 서브타입 ]
 * </pre>
 * - 타임스탬프는 2025-01-01 00:00:00(KST)을 기준으로 ms 단위 경과 시간입니다. <br>
 * - 시퀀스는 동일한 밀리초 내에서 ID 충돌을 방지하기 위한 증가값입니다. <br>
 * - 타입 및 서브타입은 ID의 용도 및 구체적인 구분을 나타냅니다. <br>
 * <br>
 * 해당 클래스는 내부적으로 단일 데몬 스레드를 통해 ID를 생성하며,
 * 외부에서는 {@link #requestId(int, int)} 메서드를 통해 ID를 요청합니다.
 * 요청은 {@link CompletableFuture}를 통해 비동기적으로 응답됩니다.
 *
 * <h2>비동기 처리 방식</h2>
 * <ul>
 *   <li>생산자 스레드는 {@code requestId(type, subtype)}를 호출하여 ID 생성을 요청합니다.</li>
 *   <li>요청은 내부 {@code BlockingQueue}에 저장되며, 백그라운드 워커 스레드가 처리합니다.</li>
 *   <li>ID가 생성되면 {@code CompletableFuture}를 통해 생산자에게 응답이 전달됩니다.</li>
 * </ul>
 *
 * <h2>동기 방식으로 결과 받기</h2>
 * <pre>{@code
 * CompletableFuture<Long> future = idGenerator.requestId(1, 2);
 * Long id = future.get(100, TimeUnit.MILLISECONDS); // 동기 방식으로 결과 대기
 * }</pre>
 *
 * <h2>비동기 방식으로 결과 처리</h2>
 * <pre>{@code
 * idGenerator.requestId(1, 2)
 *     .thenAccept(id -> {
 *         // ID 수신 후 비동기 처리
 *         System.out.println("Received ID: " + id);
 *     });
 * }</pre>
 *
 * <p>
 * 본 클래스는 멀티 스레드 환경에서 안전하지 않으므로 반드시 단일 워커 스레드 기반에서만 동작해야 합니다.
 * 멀티 워커 환경이 필요할 경우, {@code AtomicLong} 기반의 CAS 처리로 전환이 필요합니다.
 *
 * @author jack8
 * @since 2025-07-09
 */
@Component
@Slf4j
public class IdGenerator {

    // 기준 시각 (2025-01-01 00:00:00)
    private static final long EPOCH = 1735689600000L;

    // 전체 ID 비트 수
    private static final int ID_BITS = 64;
    // 시간(ms) 비트 수
    private static final int TIME_BITS = 41;
    // 서버 인스턴스 식별자 비트 수
    private static final int SERVER_ID_BITS = 4;
    // 동일 ms 내 순차 증가용 시퀀스
    private static final int SEQ_BITS = 9;
    // ID 용도 구분을 위한 상위 타입
    private static final int TYPE_BITS = 5;
    // ID 용도 구분을 위한 하위 타입
    private static final int SUBTYPE_BITS = 5;

    // 최대 순차 증가용 시퀀스 값
    private static final int MAX_SEQUENCE = (1 << SEQ_BITS) - 1;

    // 서버 id 시작 비트 위치
    private static final int SERVER_ID_SHIFT = ID_BITS - TIME_BITS - SERVER_ID_BITS;
    // 시퀀스 넘버 시작 비트 위치
    private static final int SEQ_SHIFT = SERVER_ID_SHIFT - SEQ_BITS;
    // ID 용도 구분 상위 타입 시작 비트 위치
    private static final int TYPE_SHIFT = SEQ_SHIFT - TYPE_BITS;
    // ID 용도 구분 하위 타입 시작 비트 위치
    private static final int SUBTYPE_SHIFT = TYPE_SHIFT - SUBTYPE_BITS;

    // 생성자 주입을 통해 받는 서버 ID
    private final int serverId;

    /**
     * 생산자 스레드가 아이디 생성을 요청하게 되면, 해당 큐에 생성 요청에 대한 데이터가 담김.
     * {@code BlockingQueue} 는 capacity가 꽉 찬 경우, 스레드를 wait 상태로 두며 blocking 함.
     * <pre>
     *     - 생산자 스레드는 이를 호출할 때 소비자 스레드가 살아 있음을 명확히 해야 함
     *     - 소비자 스레드가 정상적으로 작동 함을 감시하는 watchdog 가 필요할 수도 있음
     * </pre>
     */
    private final BlockingQueue<IdRequest> queue = new LinkedBlockingQueue<>(5000);

    // private final AtomicLong state = new AtomicLong(0L); // packed: (41bit ts << 8) | 8bit seq
    // seq 현재 상태
    private long state = 0L;

    public IdGenerator(@Value("${server.info.instanceId}") int serverId) {
        this.serverId = serverId;
    }

    /**
     * 생산자 스레드는 아이디 생성을 요청할 때 해당 메서드에 접근함. 이에 대한 반환은 비동기 방식으로 전해짐.
     * @param type 상위 id 타입 구분자
     * @param subtype 하위 id 타입 구분자
     * @return CompletableFuture 이며, timeout 을 통해 스레드가 죽었을 경우를 대비해야 함
     */
    public CompletableFuture<Long> requestId(int type, int subtype) {
        IdRequest req = new IdRequest(type, subtype);
        boolean offered = queue.offer(req);

        if (!offered) {
            req.future.completeExceptionally(new RejectedExecutionException("ID generator overloaded"));
        }
        return req.future;
    }

    @PostConstruct
    private void startWorker() {
        // 메서드 참조 스레드 정의
        Thread worker = new Thread(this::runWorker, "idGeneratorWorker");
        // JVM 과 생명주기를 공유하도록 데몬 스레드로 설정
        worker.setDaemon(true);
        // 스레드 실행
        worker.start();
    }

    /**
     * 스레드에 의해 자동으로 시작하고 백그라운드에서 작동하는 메서드입니다. BlockingQueue 를 사용하여
     * 요청을 받고, 이를 처리합니다. 만약 큐가 비어 있다면 while 을 돌지 않고 block 상태에 돌입합니다.
     * 만약 큐에 무언가 차기 시작한다면 block 상태를 해제하고 큐에서 요청을 소비하여 가공합니다.
     */
    private void runWorker() {
        while (true) {
            try {
                IdRequest req = queue.take();
                long id = generate(req.type, req.subtype);
                req.future.complete(id);
            } catch (InterruptedException ie) { // 의도적인 중단 요청 시 / 중단
                Thread.currentThread().interrupt();
                break;
            } catch (Throwable th) { // 내부적으로 예외 발생 시, 로그를 표시 하고 속행
                log.error("ID 생성 중 예외 발생", th);
            }
        }
    }

    //    private long generate(int type, int subtype) {
    //        long now = currentTimeMillis() - EPOCH;
    //        long base = now << SEQ_BITS;
    //
    //        long finalState;
    //
    //        while (true) {
    //            long prev = state.get();
    //            long prevTs = prev >>> SEQ_BITS;
    //            int seq = (int) (prev & MAX_SEQUENCE);
    //
    //            if (prevTs < now) {
    //                finalState = base;
    //                if (state.compareAndSet(prev, base)) {
    //                    break;
    //                }
    //            } else if (prevTs == now) {
    //                if (seq < MAX_SEQUENCE) {
    //                    finalState = prev + 1;
    //                    if (state.compareAndSet(prev, finalState)) {
    //                        break;
    //                    }
    //                } else {
    //                    now = waitNextMillis(prevTs);
    //                    base = now << SEQ_BITS;
    //                }
    //            } else {
    //                now = prevTs;
    //                base = now << SEQ_BITS;
    //            }
    //        }
    //
    //        long seq = finalState & MAX_SEQUENCE;
    //
    //        return (now << (64 - 41))
    //                  | ((long) serverId << SERVER_ID_SHIFT)
    //                  | (seq << SEQ_SHIFT)
    //                  | ((long) type << TYPE_SHIFT)
    //                  | ((long) subtype << SUBTYPE_SHIFT);
    //    }

    /**
     * 아이디 생성 로직 - 멀티 스레드 환경을 상정하고 생성된 것이 아니기에
     * 단일 스레드 기반으로 작동해야 합니다. 멀티 환경의 경우 state 를 {@code AtomicLong} 으로
     * 두고 CAS 를 통한 값의 update 과정이 필요합니다.
     * @param type 상위 식별자 타입
     * @param subtype 하위 식별자 타입
     * @return 생성된 id 의 long 타입
     */
    private long generate(int type, int subtype) {

        long nowMs = currentTimeMillis() - EPOCH;

        long prevTs = state >>> SEQ_BITS;
        int seq = (int) (state & MAX_SEQUENCE);

        if (prevTs != nowMs) {
            seq = 0;
        } else {
            if (seq == MAX_SEQUENCE) {
                nowMs = waitNextMillis(prevTs);
                seq = 0;
            } else {
                seq++;
            }
        }

        state = (nowMs << SEQ_BITS) | seq;

        return (nowMs << (64 - TIME_BITS))
                | ((long) serverId << SERVER_ID_SHIFT)
                | ((long) seq << SEQ_SHIFT)
                | ((long) type << TYPE_SHIFT)
                | ((long) subtype << SUBTYPE_SHIFT);
    }

    private long waitNextMillis(long lastTimestamp) {
        long timestamp = System.currentTimeMillis() - EPOCH;
        while (timestamp <= lastTimestamp) {
            Thread.onSpinWait();
            timestamp = System.currentTimeMillis() - EPOCH;
        }
        return timestamp;
    }

    private static class IdRequest {
        final int type;
        final int subtype;
        final CompletableFuture<Long> future = new CompletableFuture<>();

        public IdRequest(int type, int subtype) {
            this.type = type;
            this.subtype = subtype;
        }
    }
}
