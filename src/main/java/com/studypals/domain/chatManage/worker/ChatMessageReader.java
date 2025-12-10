package com.studypals.domain.chatManage.worker;

import java.util.*;

import lombok.RequiredArgsConstructor;

import com.studypals.domain.chatManage.dao.ChatMessageCacheRepository;
import com.studypals.domain.chatManage.dao.ChatMessageRepository;
import com.studypals.domain.chatManage.dto.ChatroomLatestInfo;
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
            List<ChatMessage> source = messageRepository.findRecent(roomId, chatId);

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

        // 만약 캐시에 100개 이하의 데이터가 들어있고, 실제 채팅 내역은 그 이상일 때, 캐시를 전부 채워줌
        if (!savedMessage.isEmpty() && cachedMessage.size() < maxLen) {
            rebuildCacheFromRecent(roomId, merged, maxLen);
        }
        return merged;
    }

    /**
     *
     * @param cursor 채팅방 별 마지막 메시지, 모두 유효한 값을 가지고 와야한다.
     * @return 각 채팅방에 대한 언리드 카운트, 최신 메시지 정보
     */
    public Map<String, ChatroomLatestInfo> getLatestInfo(Map<String, String> cursor) {
        Map<String, ChatroomLatestInfo> result = cacheRepository.countAllToLatest(cursor);

        Iterator<Map.Entry<String, ChatroomLatestInfo>> it = result.entrySet().iterator();

        while (it.hasNext()) {
            Map.Entry<String, ChatroomLatestInfo> entry = it.next();
            ChatroomLatestInfo cached = entry.getValue();

            if (cached == null) {
                it.remove();
                continue;
            }

            if (cached.getCnt() < 0) {
                Optional<ChatMessage> latestMessageOp = messageRepository.findTopByRoomOrderByIdDesc(entry.getKey());

                if (latestMessageOp.isEmpty()) {
                    it.remove();
                    continue;
                }

                ChatMessage message = latestMessageOp.get();

                // 새 값은 entry.setValue 로 교체 (구조 변경 아님)
                ChatroomLatestInfo newInfo = new ChatroomLatestInfo(
                        0, message.getId(), message.getType(), message.getMessage(), message.getSender());
                entry.setValue(newInfo);

                // 비교는 "원래 캐시값" 기준으로 해야 함
                if (message.getId().equals(cached.getId())) {
                    cacheRepository.save(message);
                } else {
                    List<ChatMessage> messages =
                            new ArrayList<>(messageRepository.findTop100ByRoomOrderByIdDesc(entry.getKey()));
                    Collections.reverse(messages);
                    cacheRepository.clear(message.getRoom());
                    cacheRepository.saveAll(messages);
                }
            } else {
                long cnt = cached.getCnt();
                cnt = cnt > 100 ? 100 : cnt;

                ChatroomLatestInfo capped = new ChatroomLatestInfo(
                        cnt, cached.getId(), cached.getType(), cached.getMessage(), cached.getSender());

                entry.setValue(capped); // 구조 변경 없이 value만 교체
            }
        }

        return result;
    }

    /**
     * 캐시에 데이터가 올바르지 않게 들어가 있는 경우, 캐시 내역을 삭제하고, 데이터를 집어 넣습니다.
     * @param roomId 초기화 및 정상화 할 캐시 key
     * @param source 정상화 시 넣을 데이터
     * @param maxLen 캐시 최대 길이
     */
    private void rebuildCacheFromRecent(String roomId, List<ChatMessage> source, int maxLen) {
        if (source == null || source.isEmpty()) {
            cacheRepository.clear(roomId); // 방 캐시 전체 삭제 (필요하면 구현)
            return;
        }

        // 원본 merged 를 건드리지 않도록 복사본 생성
        List<ChatMessage> copy = new ArrayList<>(source);

        // id 기준 오름차순 정렬 (id 가 시간 순서를 반영한다고 가정)
        copy.sort(Comparator.comparing(ChatMessage::getId));

        // 최신 maxLen 개만 남기기
        int size = copy.size();
        List<ChatMessage> forCache = new ArrayList<>(copy.subList(Math.max(size - maxLen, 0), size));

        // 기존 캐시 초기화 후 재저장
        cacheRepository.clear(roomId);
        cacheRepository.saveAll(forCache);
    }
}
