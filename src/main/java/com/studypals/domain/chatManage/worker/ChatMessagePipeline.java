package com.studypals.domain.chatManage.worker;

import org.springframework.stereotype.Component;

import com.studypals.domain.chatManage.entity.ChatMessage;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

/**
 * {@code Sink.many} 를 활용하여 요청 이벤트를 push 하고, 소비자에게 Flux로 생산자를 제공합니다.
 * <p>
 * {@code publish} 를 사용하여 요청을 넣을 수 있습니다.
 * <br>
 * {@code getStream} 을 사용하여 Flux 스트림을 받을 수 있습니다.
 *
 * <p><b>외부 모듈:</b><br>
 * spring webflux
 *
 * @author jack8
 * @see ChatMessage
 * @since 2025-07-14
 */
@Component
public class ChatMessagePipeline {

    private final Sinks.Many<ChatMessage> sink = Sinks.many().multicast().onBackpressureBuffer();

    /**
     * 채팅 메시지를 발행합니다. 추후 소비자가 이를 가공하여 처리합니다.
     * @param chatMessage 채팅 메시지
     */
    public void publish(ChatMessage chatMessage) {
        sink.tryEmitNext(chatMessage);
    }

    /**
     * 현재 쌓여있는 채팅 메시지를 Flux 스트림으로 반환합니다. 소비자는 이를 받아
     * 처리할 수 있습니다.
     * @return Flux<ChatMessage>
     */
    public Flux<ChatMessage> getStream() {
        return sink.asFlux();
    }
}
