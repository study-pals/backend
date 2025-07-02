package com.studypals.global.websocket.subscibeManage;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import com.studypals.global.redis.redisHashRepository.annotations.RedisHashEntity;
import com.studypals.global.redis.redisHashRepository.annotations.RedisHashMapField;
import com.studypals.global.redis.redisHashRepository.annotations.RedisId;

/**
 * WebSocket 세션 단위로 사용자의 채팅방 구독 정보를 저장하는 Redis 기반 엔티티입니다.
 * <p>
 * 각 사용자의 세션 ID를 기준으로, 해당 세션에서 구독한 채팅방(roomId)과 관련된 메타 정보
 * (예: 구독 시간 또는 읽은 메시지 인덱스 등)를 Redis Hash 자료구조로 저장합니다.
 *
 * <p>
 * {@link RedisHashEntity}, {@link RedisHashMapField}, {@link RedisId} 등의 커스텀 어노테이션을 통해
 * 본 객체는 Redis 내 Hash 구조로 자동 변환되며, sessionId를 키로 사용하여 저장됩니다.
 *
 * @author jack8
 * @since 2025-06-26
 */
@RedisHashEntity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class UserSubscribeInfo {
    @RedisId
    private String sessionId;

    private Long userId;

    @RedisHashMapField
    private Map<String, Integer> roomList;
}
