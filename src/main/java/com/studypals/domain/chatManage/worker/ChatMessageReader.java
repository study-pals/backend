package com.studypals.domain.chatManage.worker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import lombok.RequiredArgsConstructor;

import com.studypals.domain.chatManage.dao.ChatMessageCacheRepository;
import com.studypals.domain.chatManage.dao.ChatMessageRepository;
import com.studypals.domain.chatManage.entity.ChatMessage;
import com.studypals.global.annotations.Worker;

import reactor.core.publisher.Flux;
import reactor.util.function.Tuple2;

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
 * @since 2025-11-18
 */
@Worker
@RequiredArgsConstructor
public class ChatMessageReader {
    private final ChatMessageRepository messageRepository;
    private final ChatMessageCacheRepository cacheRepository;

    public Flux<ChatMessage> getChatLog(String roomId, String chatId) {
        List<ChatMessage> cachedMessage = cacheRepository.fetchFromId(roomId, chatId);
        int maxLen = cacheRepository.getMaxLen();
        if (cachedMessage.isEmpty()) {
            Flux<ChatMessage> source = messageRepository.findByRoomAndIdGreaterThanEqualOrderByIdDesc(roomId, chatId);
            List<ChatMessage> forCached = new ArrayList<>();
            return source.index()
                    .doOnNext(t -> {
                        long i = t.getT1();
                        ChatMessage msg = t.getT2();
                        if (i < maxLen) forCached.add(msg);
                    })
                    .doOnComplete(() -> {
                        Collections.reverse(forCached);
                        cacheRepository.saveAll(forCached);
                    })
                    .map(Tuple2::getT2);
        }
        String oldestId = cachedMessage.get(cachedMessage.size() - 1).getId();
        if (chatId.compareTo(oldestId) >= 0) {
            return Flux.fromIterable(cachedMessage);
        } else {
            return Flux.fromIterable(cachedMessage).concatWith(messageRepository.findRange(roomId, chatId, oldestId));
        }
    }
}
