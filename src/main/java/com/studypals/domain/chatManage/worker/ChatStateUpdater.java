package com.studypals.domain.chatManage.worker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import jakarta.annotation.PostConstruct;

import lombok.RequiredArgsConstructor;

import com.studypals.domain.chatManage.dao.UserLastReadMessageRepository;
import com.studypals.domain.chatManage.entity.UserLastReadMessage;
import com.studypals.global.annotations.Worker;

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
 * @since 2025-07-24
 */
@Worker
@RequiredArgsConstructor
public class ChatStateUpdater {

    private static final int THREAD_POOL_SIZE = 4;
    private static final int MAX_BATCH_SIZE = 1024;
    private static final int MAX_WAIT_MS = 500;

    private static final AtomicInteger COUNTER = new AtomicInteger();

    private record UpdateReq(String roomId, Long userId, String chatId) {}

    private record PairKey<K1, K2> (K1 key1, K2 key2) {}


    private final UserLastReadMessageRepository userLastReadMessageRepository;

    private final BlockingQueue<UpdateReq> queue = new LinkedBlockingQueue<>();


    private final ScheduledExecutorService scheduler =
            new ScheduledThreadPoolExecutor(
                    THREAD_POOL_SIZE,
                    r -> {
                        Thread t = new Thread(r, "chat-state-updater-" + COUNTER.getAndIncrement());
                        t.setDaemon(true);
                        return t;
                    },
                    new ThreadPoolExecutor.CallerRunsPolicy()
            );

    @PostConstruct
    void init() {
        scheduler.scheduleAtFixedRate(this::flushS)
    }

    void flush() {

        List<UpdateDt>

    }
}
