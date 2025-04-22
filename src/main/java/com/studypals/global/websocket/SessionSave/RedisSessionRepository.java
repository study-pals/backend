package com.studypals.global.websocket.SessionSave;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;

/**
 * Redis 에 stomp 세션에 대한 정보를 저장하는 repository 입니다.
 * <p>
 *
 * <p><b>상속 정보:</b><br>
 * {@link SessionRepository} 의 구현 클래스입니다.
 *
 * <p><b>외부 모듈:</b><br>
 * StringRedisTemplate
 *
 * @author jack8
 * @see SessionRepository
 * @since 2025-04-22
 */
@Repository("redis-session")
@RequiredArgsConstructor
public class RedisSessionRepository implements SessionRepository {

    private static final Duration TTL = Duration.ofHours(24);
    private static final String SESSION_REDIS_PREFIX = "ws:session:";
    private final StringRedisTemplate redis;

    private String key(String sessionId) {
        return SESSION_REDIS_PREFIX + sessionId;
    }

    @Override
    public void save(SessionInfo info) {
        Map<String, String> values = Map.of(
                "userId", info.userId().toString(),
                "roomId", info.roomId() == null ? "" : info.roomId(),
                "connectedAt", info.connectedAt().toString());

        redis.opsForHash().putAll(key(info.sessionId()), values);
        redis.expire(key(info.sessionId()), TTL);
    }

    @Override
    public Optional<SessionInfo> findBySessionId(String sessionId) {
        Map<Object, Object> map = redis.opsForHash().entries(key(sessionId));
        if (map.isEmpty()) return Optional.empty();

        return Optional.of(new SessionInfo(
                sessionId,
                Long.valueOf((String) map.get("userId")),
                map.get("roomId") == null || map.get("roomId").toString().isBlank()
                        ? null
                        : map.get("roomId").toString(),
                Instant.parse((String) map.get("connectedAt"))));
    }

    @Override
    public void updateRoom(String sessionId, String roomId) {
        redis.opsForHash().put(key(sessionId), "roomId", roomId);
        redis.expire(key(sessionId), TTL);
    }

    @Override
    public void clear(String sessionId) {
        redis.delete(key(sessionId));
    }
}
