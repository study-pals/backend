package com.studypals.global.websocket.SessionSave;

import java.time.Instant;

/**
 * 세션 정보에 대한 record dto 클래스입니다. 때로는, map 상에 저장될 수 있습니다.
 *
 * @author jack8
 * @see RedisSessionRepository
 * @see InMemorySessionRepository
 * @since 2025-04-22
 */
public record SessionInfo(String sessionId, Long userId, String roomId, Instant connectedAt) {}
