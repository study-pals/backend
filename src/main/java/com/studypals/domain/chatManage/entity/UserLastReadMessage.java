package com.studypals.domain.chatManage.entity;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import com.studypals.global.redis.redisHashRepository.annotations.RedisHashEntity;
import com.studypals.global.redis.redisHashRepository.annotations.RedisHashMapField;
import com.studypals.global.redis.redisHashRepository.annotations.RedisId;

/**
 * 특정 채팅방에 대해, 사용자 별 가장 마지막으로 읽은 메시지 정보를 기록합니다.
 * userId - chatId 로 저장되며, Redis 의 Hash 자료구조로 괸리됩니다.
 * RedisHashRepository 에 사용되는 엔티티입니다.
 *
 * @author jack8
 * @since 2025-07-17
 */
@RedisHashEntity(value = "lastRead")
@AllArgsConstructor
@NoArgsConstructor
public class UserLastReadMessage {

    @RedisId
    private String roomId;

    @RedisHashMapField
    @Getter
    private Map<Long, String> lastMessage;
}
