package com.studypals.domain.chatManage.worker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import lombok.RequiredArgsConstructor;

import com.studypals.domain.chatManage.dao.ChatMessageCacheRepository;
import com.studypals.domain.chatManage.dao.ChatMessageRepository;
import com.studypals.domain.chatManage.entity.ChatMessage;
import com.studypals.global.annotations.Worker;

/**
 * 채팅방 메시지 이력을 조회하는 Worker 클래스입니다.
 * <p>
 * 캐시와 영속 저장소를 함께 활용하여 기준 메시지 ID 이후의 채팅 내역을 조회합니다.
 * 캐시가 비어 있으면 저장소에서 데이터를 가져오고, 일부 구간을 캐시에 적재해 이후 요청의 조회 비용을 줄입니다.
 * <p>
 * 빈 관리:<br>
 * - ChatMessageRepository : RDBMS 등에 저장된 채팅 메시지를 조회하는 레포지토리<br>
 * - ChatMessageCacheRepository : 채팅 메시지를 캐싱하고 조회하는 레포지토리<br>
 * <p>
 * 외부 모듈:<br>
 * - Redis 등 캐시 스토리지를 사용해 최근 메시지를 메모리 기반으로 빠르게 조회하는 구조를 전제로 합니다.<br>
 *
 * @author jack8
 * @see com.studypals.domain.chatManage.entity.ChatMessage ChatMessage
 * @see com.studypals.domain.chatManage.dao.ChatMessageRepository ChatMessageRepository
 * @see com.studypals.domain.chatManage.dao.ChatMessageCacheRepository ChatMessageCacheRepository
 * @since 2025-11-18
 */
@Worker
@RequiredArgsConstructor
public class ChatMessageReader {
    private final ChatMessageRepository messageRepository;
    private final ChatMessageCacheRepository cacheRepository;

    /**
     * 채팅방의 기준 메시지 ID를 포함하여 이후 메시지를 조회합니다.
     * <p>
     * 동작 순서는 다음과 같습니다.<br>
     * - 캐시에서 기준 메시지 ID 이후의 메시지를 우선 조회합니다.<br>
     * - 캐시에 데이터가 없으면 전체 결과를 DB에서 조회해 반환하고, 일부 구간을 캐시에 저장합니다.<br>
     * - 캐시에 데이터가 있고 기준 ID가 캐시 범위 내부라면 캐시만 반환합니다.<br>
     * - 기준 ID가 캐시보다 더 과거라면 부족한 데이터만 DB에서 조회해 반환합니다.<br>
     * <p>
     * todo : paging 하기
     *
     * @param roomId 채팅방 ID
     * @param chatId 기준 메시지 ID
     * @return 기준 메시지 이후의 메시지를 포함한 리스트
     */
    public List<ChatMessage> getChatLog(String roomId, String chatId) {
        // 캐시에서 기준 메시지 이후 구간을 조회
        List<ChatMessage> cachedMessage = cacheRepository.fetchFromId(roomId, chatId);
        int maxLen = cacheRepository.getMaxLen();

        // 캐시에 데이터가 없을 경우 → 전체 구간을 DB에서 조회
        if (cachedMessage.isEmpty()) {
            List<ChatMessage> source = messageRepository.findByRoomAndIdGreaterThanEqualOrderByIdDesc(roomId, chatId);

            // 조회 결과가 없으면 빈 리스트 반환
            if (source == null || source.isEmpty()) return List.of();

            // 캐시에 저장할 구간을 최대 길이까지만 자름
            List<ChatMessage> forCached = new ArrayList<>(source.subList(0, Math.min(maxLen, source.size())));

            // DB 조회 결과는 내림차순이므로 캐시에 저장할 때는 순서를 뒤집어 오름차순으로 저장
            Collections.reverse(forCached);
            cacheRepository.saveAll(forCached);

            // DB 조회 전체 결과 반환
            return source;
        }

        // 캐시에 저장된 메시지 중 가장 오래된 ID
        String oldestId = cachedMessage.get(cachedMessage.size() - 1).getId();

        // 기준 ID가 캐시 범위 안에 있다면 캐시 데이터만으로 충분
        if (chatId.compareTo(oldestId) >= 0) {
            return cachedMessage;
        }

        // 기준 ID가 캐시 범위보다 더 과거라면 → 더 이전 데이터를 DB에서 조회해 반환
        List<ChatMessage> savedMessage = messageRepository.findRange(roomId, chatId, oldestId);
        List<ChatMessage> merged = new ArrayList<>();
        merged.addAll(cachedMessage);
        merged.addAll(savedMessage);
        return merged;
    }
}
