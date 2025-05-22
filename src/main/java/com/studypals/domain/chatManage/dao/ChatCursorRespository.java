package com.studypals.domain.chatManage.dao;

import java.util.*;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;

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
 * @since 2025-05-22
 */
@Repository
@RequiredArgsConstructor
public class ChatCursorRespository {
    private final RedisTemplate<String, String> redisTemplate;
    private final RedisScript<List> readCursorScript;

    //    public Map<Long, String> getUserToMsgMap(String roomId, Set<Long> userIds) {
    //        if (userIds.isEmpty()) return Map.of();
    //
    //        String hashKey = "room:" + roomId + ":reads";
    //
    //        List<Long> userIdList = new ArrayList<>(userIds);
    //        List<Object> keyList = userIdList.stream()
    //                .map(String::valueOf)
    //                .collect(Collectors.toList());
    //
    //        List<Object> values = redisTemplate.opsForHash()
    //                .multiGet(hashKey, keyList);
    //
    //        Map<Long, String> result = new HashMap<>();
    //        for (int i = 0; i < userIdList.size(); i++) {
    //            Object value = values.get(i);
    //            if (value != null) {
    //                result.put(userIdList.get(i), value.toString());
    //            }
    //        }
    //
    //        return result;
    //    }

    public Map<Long, String> getUserToMsgMapViaLua(String roomId, Set<Long> userIds) {
        if (userIds.isEmpty()) return Map.of();

        String hasKey = "room:" + roomId + ":reads";

        List<String> keys = userIds.stream().map(String::valueOf).toList();

        List<String> args = List.of(hasKey);

        @SuppressWarnings("unchecked")
        List<Object> rawResult = redisTemplate.execute(readCursorScript, keys, args.toArray());

        Map<Long, String> result = new HashMap<>();
        if (rawResult == null) return result;

        for (int i = 0; i < rawResult.size(); i += 2) {
            Long userId = Long.valueOf(rawResult.get(i).toString());
            String messageId = rawResult.get(i + 1).toString();
            result.put(userId, messageId);
        }
        return result;
    }
}
