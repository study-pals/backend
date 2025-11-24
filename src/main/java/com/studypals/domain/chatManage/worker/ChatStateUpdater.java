package com.studypals.domain.chatManage.worker;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessageSendingOperations;

import lombok.RequiredArgsConstructor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.studypals.domain.chatManage.dao.UserLastReadMessageRepository;
import com.studypals.domain.chatManage.dto.ChatType;
import com.studypals.domain.chatManage.dto.ChatUpdateDto;
import com.studypals.domain.chatManage.dto.OutgoingMessage;
import com.studypals.global.annotations.Worker;

/**
 * <pre><code>
 * 각 채팅방 별로, 채팅 읽음 내역을 저장 및 발송합니다. {@code ScheduledExecutorService} 를 사용하여
 * 데몬 스레드가 작업을 수행합니다. 작업 자체는 다음과 같은 과정을 따릅니다.
 *
 * 1. 사용자의 "읽음 완료" 메시지를 받으면, 이를 가공하여 {@code buckets} 에 저장합니다.
 * 2. 이때, {@code MAX_BATCH_SIZE (1024)} 이상의 메시지가 쌓이면 flush 를 시작합니다.
 * 3. flush 하는 경우, buckets 의 snapshot 을 복사하고 초기화합니다.
 * 4. 초기화 후 저장에 용이한 형태로 가공합니다.
 * 5. 해당 데이터를 각 유저에게 보내고, Redis 에 해당 내용을 저장합니다.
 * </code></pre>
 *
 * @author jack8
 * @since 2025-11-28
 */
@Worker
@RequiredArgsConstructor
public class ChatStateUpdater {

    // bucket 에 들어가는 최대 데이터 크기
    private static final int MAX_BATCH_SIZE = 1024;
    // scheduler 가 실행되는 주기
    private static final int MAX_WAIT_MS = 500;
    // 채팅 전송 시 주소 prefix
    @Value("${chat.subscribe.address.default}")
    private String DESTINATION_PREFIX;

    // 멀티 스레딩 사용 시 스레딩 식별자 - deprecated 됨(1개의 스레드사용)
    @Deprecated
    private static final AtomicInteger COUNTER = new AtomicInteger();
    // 현재 flushing 작업 중인지에 대한 여부
    private final AtomicBoolean flushing = new AtomicBoolean(false);

    // bucket 에서 key 로서 사용되는 객체.
    private record PairKey(String roomId, Long userId) {}

    private final UserLastReadMessageRepository userLastReadMessageRepository;
    private final SimpMessageSendingOperations template;
    private final ObjectMapper objectMapper;

    /**
     * scheduler 정의.
     */
    private final ScheduledExecutorService scheduler = new ScheduledThreadPoolExecutor(
            1,
            r -> {
                Thread t = new Thread(r, "chat-state-updater");
                t.setDaemon(true);
                return t;
            },
            new ThreadPoolExecutor.CallerRunsPolicy());

    /**
     * 읽음 내역이 도착하여 임시로 저장되는 장소입니다. <br>
     * key -> roomId , userId    ///     value -> chatId <br>
     */
    private final ConcurrentMap<PairKey, String> buckets = new ConcurrentHashMap<>();

    /**
     * scheduler 를 구동합니다. 지정할 작업, 실행 주기에 대해 정의합니다.
     */
    @PostConstruct
    public void startScheduler() {
        scheduler.scheduleAtFixedRate(
                () -> {
                    // flush 필요할 때만 실행
                    if (!buckets.isEmpty()) {
                        triggerFlushAsync();
                    }
                },
                MAX_WAIT_MS,
                MAX_WAIT_MS,
                TimeUnit.MILLISECONDS);
    }

    /**
     * JVM 이 종료되기 전에 scheduler 를 안전하게 종료합니다.
     */
    @PreDestroy
    public void stopScheduler() {
        scheduler.shutdown();
    }

    /**
     * 외부에서 채팅 읽음 데이터를 처리하기를 기대하며 호출하는 메서드입니다.
     * 해당 객체의 유일한 외부 접촉점이며, 온전히 처리되기 까지는 약 0.5 초의 딜레이가 있습니다.
     * @param dto 채팅방 아이디, 사용자 아이디, 채팅 아이디
     */
    public void update(ChatUpdateDto dto) {
        PairKey keys = new PairKey(dto.roomId(), dto.userId());
        buckets.put(keys, dto.chatId());

        // 배치 크기가 커지면, 스케쥴러의 스레드가 작업을 진행하도록 강제합니다.
        if (buckets.size() >= MAX_BATCH_SIZE) {
            triggerFlushAsync();
        }
    }

    /**
     * scheduler 의 데몬 스레드 작업을 유발합니다.
     * 단, 시스템 전역에서 동시에 한 번만 진행되어야 합니다.
     */
    private void triggerFlushAsync() {
        if (!flushing.compareAndSet(false, true)) {
            // 이미 누군가 flush 중이면 패스
            return;
        }

        scheduler.execute(() -> {
            try {
                flushOnce();
            } finally {
                flushing.set(false);
            }
        });
    }

    /**
     * 실질적인 로직이 포함되는 메서드입니다. 복사 - 전송 - 저장에 대한 내용이 포함되어 있습니다.
     */
    private void flushOnce() {
        // snapshot 을 복사하고, 크기를 검사하여 값이 없으면 종료합니다.
        Map<PairKey, String> snapshot = snapshotAndClear();
        if (snapshot.isEmpty()) {
            return;
        }

        // roomId -> (userId -> chatId) / 적절한 데이터로 변환합니다.
        Map<String, Map<String, String>> messages = new HashMap<>();
        for (Map.Entry<PairKey, String> e : snapshot.entrySet()) {
            PairKey key = e.getKey();
            String chatId = e.getValue();

            messages.computeIfAbsent(key.roomId(), k -> new HashMap<>()).put(String.valueOf(key.userId()), chatId);
        }

        // 1) 브로드캐스트
        sendToClients(messages);

        // 2) DB 저장
        userLastReadMessageRepository.saveMapById(messages);
    }

    /**
     * bucket 에 대한 복사본을 만들어 반환합니다. flush 중 도착하는 메시지의 정상적인
     * 저장을 위해, 복사를 하고, bucket 을 지웁니다. 단, snapshot에 포함되어 있는 데이터만 지웁니다.
     * @return 복사된 bucket 의 snapshot
     */
    private Map<PairKey, String> snapshotAndClear() {
        if (buckets.isEmpty()) {
            return Map.of();
        }
        Map<PairKey, String> snapshot = new HashMap<>(buckets);
        buckets.keySet().removeAll(snapshot.keySet());
        return snapshot;
    }

    /**
     * 채팅 읽음 내역을 현재 접속 중인 사용자에게 전송합니다. json 방식으로 전송되며,
     * 사용자의 아이디에 따른 채팅 아이디 리스트가 전송됩니다.
     * @param messages 가공된 읽음 데이터
     */
    private void sendToClients(Map<String, Map<String, String>> messages) {
        for (Map.Entry<String, Map<String, String>> entry : messages.entrySet()) {
            String roomId = entry.getKey();
            Map<String, String> userLastReads = entry.getValue();

            try {
                OutgoingMessage outgoingMessage =
                        new OutgoingMessage(null, ChatType.STAT, objectMapper.writeValueAsString(userLastReads), null);
                template.convertAndSend(DESTINATION_PREFIX + roomId, outgoingMessage);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }
    }
}
