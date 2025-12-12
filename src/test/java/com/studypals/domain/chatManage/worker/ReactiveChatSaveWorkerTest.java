package com.studypals.domain.chatManage.worker;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.lang.reflect.Method;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.studypals.domain.chatManage.dao.ChatMessageCacheRepository;
import com.studypals.domain.chatManage.dao.ChatMessageReactiveRepository;
import com.studypals.domain.chatManage.entity.ChatMessage;

import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

/**
 *
 * @author jack8
 * @since 2025-11-24
 */
@ExtendWith(MockitoExtension.class)
class ReactiveChatSaveWorkerTest {

    @Mock
    ChatMessageReactiveRepository chatMessageReactiveRepository;

    @Mock
    ChatMessageCacheRepository cacheRepository;

    @Mock
    ChatMessagePipeline chatMessagePipeline; // 생성자 때문에 필요, 테스트에서는 안 씀

    ReactiveChatSaveWorker worker;

    @BeforeEach
    void setUp() {
        worker = new ReactiveChatSaveWorker(chatMessageReactiveRepository, cacheRepository, chatMessagePipeline);
    }

    @Test
    void saveBatchAndCacheLatest_success() throws Exception {
        // given
        ChatMessage msg1 = ChatMessage.builder()
                .id("1-0")
                .roomId("room1")
                .sender(1L)
                .content("hello")
                .build();

        ChatMessage msg2 = ChatMessage.builder()
                .id("2-0")
                .roomId("room1")
                .sender(2L)
                .content("world")
                .build();

        List<ChatMessage> input = List.of(msg1, msg2);

        // saveAll 이 저장 후 동일 객체를 반환한다고 가정
        given(chatMessageReactiveRepository.saveAll(input)).willReturn(Flux.fromIterable(input));

        // private 메서드 호출 (필요하면 메서드를 package-private 으로 바꿔도 됨)
        Method m = ReactiveChatSaveWorker.class.getDeclaredMethod("saveBatchAndCacheLatest", List.class);
        m.setAccessible(true);

        @SuppressWarnings("unchecked")
        Flux<ChatMessage> result = (Flux<ChatMessage>) m.invoke(worker, input);

        // then: 결과 Flux 검증
        StepVerifier.create(result).expectNext(msg1, msg2).verifyComplete();

        // 그리고 캐시 저장이 호출되었는지 검증
        verify(cacheRepository).saveAll(input);
        // 필요하면 savedMessages 캡쳐해서 검사할 수도 있음
    }
}
