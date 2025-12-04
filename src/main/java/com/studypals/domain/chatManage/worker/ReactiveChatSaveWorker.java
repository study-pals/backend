package com.studypals.domain.chatManage.worker;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.annotation.PostConstruct;

import org.springframework.data.redis.core.ReactiveRedisTemplate;

import lombok.RequiredArgsConstructor;

import com.studypals.domain.chatManage.dao.ChatMessageRepository;
import com.studypals.domain.chatManage.entity.ChatMessage;
import com.studypals.global.annotations.Worker;

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
@RequiredArgsConstructor
public class ReactiveChatSaveWorker {

    private final ChatMessageRepository chatMessageRepository;
    private final ReactiveRedisTemplate<String, Object> redisTemplate;

    private final ChatMessagePipeline chatMessagePipeline;

    @PostConstruct
    public void init() {
        Flux<ChatMessage> messageStream = chatMessagePipeline.getStream();

        int maxBufferSize = 512;
        int maxBufferTime = 500;
        messageStream
                .bufferTimeout(maxBufferSize, Duration.ofMillis(maxBufferTime))
                .filter(batch -> !batch.isEmpty())
                .flatMap(this::saveBatchAndCacheLatest)
                .subscribe();
    }

    private Flux<ChatMessage> saveBatchAndCacheLatest(List<ChatMessage> messages) {
        return chatMessageRepository.saveAll(messages).collectList().flatMapMany(savedMessages -> {
            Map<String, ChatMessage> latestByRoom = new HashMap<>();

            for (ChatMessage msg : savedMessages) {
                latestByRoom.merge(msg.getRoom(), msg, (a, b) -> a.getId().compareTo(b.getId()) < 0 ? b : a);
            }

            return Flux.fromIterable(latestByRoom.entrySet())
                    .flatMap(
                            entry -> redisTemplate.opsForValue().set("chat:latest:" + entry.getKey(), entry.getValue()))
                    .thenMany(Flux.fromIterable(savedMessages));
        });
    }
}
