package com.studypals.domain.chatManage.worker;

import java.time.Duration;

import jakarta.annotation.PostConstruct;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.studypals.domain.chatManage.dao.ChatMessageMongoRepository;
import com.studypals.domain.chatManage.entity.ChatMessage;
import com.studypals.global.annotations.Worker;

import reactor.core.publisher.Sinks;

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
 * @since 2025-04-23
 */
@Worker
@RequiredArgsConstructor
@Slf4j
public class ChatMessageBufferWriter {

    private final ChatMessageMongoRepository chatMessageMongoRepository;

    private final Sinks.Many<ChatMessage> sink = Sinks.many().multicast().onBackpressureBuffer();

    @PostConstruct
    public void init() {
        sink.asFlux()
                .bufferTimeout(1000, Duration.ofMillis(200))
                .filter(batch -> !batch.isEmpty())
                .flatMap(chatMessageMongoRepository::saveAll)
                .onErrorContinue((e, obj) -> log.error("save error " + e))
                .subscribe();
    }

    public void push(ChatMessage message) {
        sink.tryEmitNext(message);
    }
}
