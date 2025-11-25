package com.studypals.domain.chatManage.worker;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.annotation.PostConstruct;

import org.springframework.data.redis.core.ReactiveRedisTemplate;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.studypals.domain.chatManage.dao.ChatMessageCacheRepository;
import com.studypals.domain.chatManage.dao.ChatMessageReactiveRepository;
import com.studypals.domain.chatManage.entity.ChatMessage;
import com.studypals.global.annotations.Worker;

import reactor.core.publisher.BufferOverflowStrategy;
import reactor.core.publisher.Flux;

/**
 * {@link ChatMessagePipeline} 에서 flux 파이프라인을 받아, 채팅 메시지를 mongoDB에
 * 비동기적으로 저장합니다. 가장 최신의 메시지에 대해 redis에 갱신합니다.
 * <p>
 * 하나의 워커 스레드가 daemon으로 등록되어 JVM 백그라운드에서 해당 작업을 지속하여 수행합니다. user thread 와 다르게,
 * jvm이 종료 시 강제로 스레드 역시 종료되며, interruptException 과 같은 의도적인 중지를 제외하고는, 장애를 복구하고
 * 작업을 지속합니다.
 * <br>
 * 상기 명시된 chatMessagePipline 으로부터 Flux 스트림을 받습니다. 스트림에 512개의 데이터가 쌓이거나(생산자가 구독 없이
 * 데이터를 생산했거나), 0.5초가 지나면 해당 스트림의 데이터를 subscribe 하여 처리합니다.
 * <br>
 * 이 경우, saveAll 을 이용해 배치 처리된 데이터를 reactive mongo DB 에 저장하고 각 채팅방 별 가장 최신의
 * 메시지를 redis에 갱신합니다.
 * <br>
 * TODO : redis에 갱신하기 위해 가장 최신의 메시지를 추리는 방식에 대한 성능 테스트 및 개선이 필요할 수 있습니다.
 * <p><b>외부 모듈:</b><br>
 * reative mongo, reactive redis, spring webflux
 *
 * @author jack8
 * @see ChatMessagePipeline
 * @since 2025-07-14
 */
@Worker
@Slf4j
@RequiredArgsConstructor
public class ReactiveChatSaveWorker {

    private final ChatMessageReactiveRepository chatMessageReactiveRepository;
    private final ReactiveRedisTemplate<String, Object> redisTemplate;
    private final ChatMessageCacheRepository cacheRepository;

    private final ChatMessagePipeline chatMessagePipeline;

    /**
     * 빈에 올라가는 동시에, Flux 기반의 메시지 스트림을 정의합니다. maxBufferSize 만큼의 데이터가 쌓이거나,
     * maxBufferTime 이 지나면 {@code saveBatchAndCacheLatest} 메서드가 호출됩니다.
     * <pre>
     *     - backpressure 가 존재합니다.
     *     - 버퍼가 비어 있으면 넘어갑니다.
     * </pre>
     */
    @PostConstruct
    public void init() {
        Flux<ChatMessage> messageStream = chatMessagePipeline.getStream();

        int maxBufferSize = 512;
        int maxBufferTime = 500;
        messageStream
                .onBackpressureBuffer(
                        10_000, msg -> log.error("chat message dropeed: {}", msg), BufferOverflowStrategy.DROP_LATEST)
                .bufferTimeout(maxBufferSize, Duration.ofMillis(maxBufferTime))
                .filter(batch -> !batch.isEmpty())
                .flatMap(this::saveBatchAndCacheLatest)
                .subscribe();
    }

    /**
     * 비동기적 dao 인 {@code MongoReactiveRepository} 인 {@link ChatMessageReactiveRepository} 에 대해,
     * 벌크 데이터 저장을 시도합니다.
     * @param messages 저장할 {@code ChatMessage} 에 대한 리스트
     * @return 저장된 ChatMessage 들에 대한 Flux 파이프라인
     */
    private Flux<ChatMessage> saveBatchAndCacheLatest(List<ChatMessage> messages) {
        // saveAll 시 저장된 데이터에 대해, 이후 처리를 수행합니다. List 로 모읍니다.
        return chatMessageReactiveRepository.saveAll(messages).collectList().flatMapMany(savedMessages -> {
            Map<String, ChatMessage> latestByRoom = new HashMap<>();

            // 채팅방에 대해 - 가장 마지막 메시지를 저장합니다. 모든 메시지에 대해, 크기 비교를 통해 더 큰 값이(최신값) 들어옵니다.
            for (ChatMessage msg : savedMessages) {
                latestByRoom.merge(msg.getRoom(), msg, (a, b) -> a.getId().compareTo(b.getId()) < 0 ? b : a);
            }
            cacheRepository.saveAll(savedMessages); // 캐시에 해당 데이터를 전부 저장합니다.

            // 이후 해당 값은 단순한 key-value 쌍에 대해 redis 에 저장합니다.
            // todo : 해당하는 redis 도 적절히 변경 필요할듯 , 혹은, cache 에서 가져오면 될 것 같은데?
            return Flux.fromIterable(latestByRoom.entrySet())
                    .flatMap(
                            entry -> redisTemplate.opsForValue().set("chat:latest:" + entry.getKey(), entry.getValue()))
                    .thenMany(Flux.fromIterable(savedMessages));
        });
    }
}
